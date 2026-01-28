package com.learning.recommend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.recommend.common.Constants;
import com.learning.recommend.entity.*;
import com.learning.recommend.mapper.*;
import com.learning.recommend.service.RecommendService;
import com.learning.recommend.vo.MaterialVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private LearningMaterialMapper materialMapper;

    @Autowired
    private UserBehaviorMapper behaviorMapper;

    @Autowired
    private UserCollectionMapper collectionMapper;

    @Autowired
    private UserProfileMapper profileMapper;

    @Override
    public List<MaterialVO> getPersonalizedRecommendations(Long userId, Integer limit) {
        // 1. 获取用户画像
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = profileMapper.selectOne(profileWrapper);

        List<Long> recommendedIds = new ArrayList<>();

        // 2. 基于协同过滤的推荐
        List<Long> cfRecommendations = collaborativeFiltering(userId, limit / 2);
        recommendedIds.addAll(cfRecommendations);

        // 3. 基于内容的推荐（标签匹配）
        if (profile != null && profile.getInterestTags() != null) {
            List<String> userTags = JSONUtil.toList(profile.getInterestTags(), String.class);
            List<Long> cbRecommendations = contentBasedFiltering(userTags, userId, limit / 2);
            recommendedIds.addAll(cbRecommendations);
        }

        // 4. 如果推荐不足，补充热门资料
        if (recommendedIds.size() < limit) {
            List<Long> hotIds = getHotMaterialIds(limit - recommendedIds.size(), userId);
            recommendedIds.addAll(hotIds);
        }

        // 5. 去重并转换为VO
        return convertToVOList(recommendedIds.stream().distinct().limit(limit).collect(Collectors.toList()));
    }

    @Override
    public List<MaterialVO> getHotRecommendations(Integer limit) {
        // 按浏览量和评分综合排序
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        wrapper.orderByDesc(LearningMaterial::getViewCount);
        wrapper.last("LIMIT " + limit);

        List<LearningMaterial> materials = materialMapper.selectList(wrapper);
        return materials.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<MaterialVO> getSimilarMaterials(Long materialId, Integer limit) {
        // 获取目标资料
        LearningMaterial targetMaterial = materialMapper.selectById(materialId);
        if (targetMaterial == null) {
            return Collections.emptyList();
        }

        // 基于分类和标签找相似资料
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        wrapper.ne(LearningMaterial::getId, materialId);
        
        // 优先同分类
        wrapper.eq(LearningMaterial::getCategory, targetMaterial.getCategory());
        wrapper.orderByDesc(LearningMaterial::getAvgRating);
        wrapper.last("LIMIT " + limit);

        List<LearningMaterial> materials = materialMapper.selectList(wrapper);
        return materials.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 协同过滤推荐（基于用户行为）
     */
    private List<Long> collaborativeFiltering(Long userId, Integer limit) {
        // 1. 获取当前用户的浏览和收藏记录
        Set<Long> userInteractedMaterials = getUserInteractedMaterials(userId);

        if (userInteractedMaterials.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 找到有相似行为的用户
        Map<Long, Integer> similarUsers = findSimilarUsers(userId, userInteractedMaterials);

        // 3. 推荐相似用户喜欢但当前用户未接触的资料
        Map<Long, Double> materialScores = new HashMap<>();
        
        for (Map.Entry<Long, Integer> entry : similarUsers.entrySet()) {
            Long similarUserId = entry.getKey();
            Integer similarity = entry.getValue();
            
            Set<Long> similarUserMaterials = getUserInteractedMaterials(similarUserId);
            for (Long materialId : similarUserMaterials) {
                if (!userInteractedMaterials.contains(materialId)) {
                    materialScores.put(materialId, 
                        materialScores.getOrDefault(materialId, 0.0) + similarity);
                }
            }
        }

        // 4. 按分数排序返回
        return materialScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 基于内容的过滤（标签匹配）
     */
    private List<Long> contentBasedFiltering(List<String> userTags, Long userId, Integer limit) {
        Set<Long> userInteractedMaterials = getUserInteractedMaterials(userId);

        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        
        List<LearningMaterial> allMaterials = materialMapper.selectList(wrapper);
        
        // 计算资料与用户兴趣的匹配度
        Map<Long, Integer> materialScores = new HashMap<>();
        
        for (LearningMaterial material : allMaterials) {
            if (userInteractedMaterials.contains(material.getId())) {
                continue;
            }
            
            int score = 0;
            if (material.getTags() != null) {
                List<String> materialTags = JSONUtil.toList(material.getTags(), String.class);
                for (String userTag : userTags) {
                    if (materialTags.contains(userTag)) {
                        score += 10;
                    }
                }
            }
            
            if (score > 0) {
                materialScores.put(material.getId(), score);
            }
        }

        return materialScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户交互过的资料ID集合
     */
    private Set<Long> getUserInteractedMaterials(Long userId) {
        Set<Long> materialIds = new HashSet<>();

        // 浏览记录
        LambdaQueryWrapper<UserBehavior> viewWrapper = new LambdaQueryWrapper<>();
        viewWrapper.eq(UserBehavior::getUserId, userId);
        viewWrapper.eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_VIEW);
        List<UserBehavior> viewBehaviors = behaviorMapper.selectList(viewWrapper);
        materialIds.addAll(viewBehaviors.stream()
                .map(UserBehavior::getMaterialId)
                .collect(Collectors.toSet()));

        // 收藏记录
        LambdaQueryWrapper<UserCollection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(UserCollection::getUserId, userId);
        List<UserCollection> collections = collectionMapper.selectList(collectionWrapper);
        materialIds.addAll(collections.stream()
                .map(UserCollection::getMaterialId)
                .collect(Collectors.toSet()));

        return materialIds;
    }

    /**
     * 找到相似用户
     */
    private Map<Long, Integer> findSimilarUsers(Long userId, Set<Long> userMaterials) {
        Map<Long, Integer> similarUsers = new HashMap<>();

        // 查找浏览过相同资料的其他用户
        for (Long materialId : userMaterials) {
            LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserBehavior::getMaterialId, materialId);
            wrapper.ne(UserBehavior::getUserId, userId);
            
            List<UserBehavior> behaviors = behaviorMapper.selectList(wrapper);
            for (UserBehavior behavior : behaviors) {
                similarUsers.put(behavior.getUserId(), 
                    similarUsers.getOrDefault(behavior.getUserId(), 0) + 1);
            }
        }

        // 只返回相似度较高的用户（至少有2个共同交互）
        return similarUsers.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 获取热门资料ID
     */
    private List<Long> getHotMaterialIds(Integer limit, Long userId) {
        Set<Long> userMaterials = getUserInteractedMaterials(userId);

        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        wrapper.orderByDesc(LearningMaterial::getViewCount);
        
        List<LearningMaterial> materials = materialMapper.selectList(wrapper);
        
        return materials.stream()
                .filter(m -> !userMaterials.contains(m.getId()))
                .limit(limit)
                .map(LearningMaterial::getId)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO列表
     */
    private List<MaterialVO> convertToVOList(List<Long> materialIds) {
        return materialIds.stream()
                .map(id -> materialMapper.selectById(id))
                .filter(Objects::nonNull)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private MaterialVO convertToVO(LearningMaterial material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        
        if (material.getTags() != null) {
            vo.setTags(JSONUtil.toList(material.getTags(), String.class));
        }
        
        return vo;
    }
}
