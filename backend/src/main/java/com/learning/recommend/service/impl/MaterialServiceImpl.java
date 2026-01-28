package com.learning.recommend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Constants;
import com.learning.recommend.dto.MaterialQueryDTO;
import com.learning.recommend.entity.*;
import com.learning.recommend.mapper.*;
import com.learning.recommend.service.MaterialService;
import com.learning.recommend.service.PythonAIService;
import com.learning.recommend.vo.MaterialVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 学习资料服务实现
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private LearningMaterialMapper materialMapper;

    @Autowired
    private MaterialSummaryMapper summaryMapper;

    @Autowired
    private UserBehaviorMapper behaviorMapper;

    @Autowired
    private UserCollectionMapper collectionMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private PythonAIService pythonAIService;

    @Override
    public Page<MaterialVO> getMaterialList(MaterialQueryDTO queryDTO, Long userId) {
        Page<LearningMaterial> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(LearningMaterial::getTitle, queryDTO.getKeyword())
                    .or().like(LearningMaterial::getDescription, queryDTO.getKeyword()));
        }
        
        // 分类筛选
        if (queryDTO.getCategory() != null && !queryDTO.getCategory().isEmpty()) {
            wrapper.eq(LearningMaterial::getCategory, queryDTO.getCategory());
        }
        
        if (queryDTO.getSubCategory() != null && !queryDTO.getSubCategory().isEmpty()) {
            wrapper.eq(LearningMaterial::getSubCategory, queryDTO.getSubCategory());
        }
        
        if (queryDTO.getDifficulty() != null && !queryDTO.getDifficulty().isEmpty()) {
            wrapper.eq(LearningMaterial::getDifficulty, queryDTO.getDifficulty());
        }
        
        // 排序
        String orderBy = queryDTO.getOrderBy() != null ? queryDTO.getOrderBy() : "createTime";
        boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getOrderType());
        
        switch (orderBy) {
            case "viewCount":
                wrapper.orderBy(true, isAsc, LearningMaterial::getViewCount);
                break;
            case "avgRating":
                wrapper.orderBy(true, isAsc, LearningMaterial::getAvgRating);
                break;
            default:
                wrapper.orderBy(true, isAsc, LearningMaterial::getCreateTime);
        }
        
        Page<LearningMaterial> materialPage = materialMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<MaterialVO> voPage = new Page<>(materialPage.getCurrent(), materialPage.getSize(), materialPage.getTotal());
        List<MaterialVO> voList = materialPage.getRecords().stream().map(material -> {
            MaterialVO vo = new MaterialVO();
            BeanUtils.copyProperties(material, vo);
            
            // 解析tags
            if (material.getTags() != null) {
                vo.setTags(JSONUtil.toList(material.getTags(), String.class));
            }
            
            // 查询是否收藏
            if (userId != null) {
                LambdaQueryWrapper<UserCollection> collectionWrapper = new LambdaQueryWrapper<>();
                collectionWrapper.eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getMaterialId, material.getId());
                vo.setIsCollected(collectionMapper.selectCount(collectionWrapper) > 0);
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public MaterialVO getMaterialDetail(Long materialId, Long userId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        
        // 解析tags
        if (material.getTags() != null) {
            vo.setTags(JSONUtil.toList(material.getTags(), String.class));
        }
        
        // 查询摘要
        LambdaQueryWrapper<MaterialSummary> summaryWrapper = new LambdaQueryWrapper<>();
        summaryWrapper.eq(MaterialSummary::getMaterialId, materialId);
        MaterialSummary summary = summaryMapper.selectOne(summaryWrapper);

        if (summary != null) {
            vo.setSummaryText(summary.getSummaryText());
            if (summary.getKeyPoints() != null) {
                vo.setKeyPoints(JSONUtil.toList(summary.getKeyPoints(), String.class));
            }
            if (summary.getKeywords() != null) {
                vo.setKeywords(JSONUtil.toList(summary.getKeywords(), String.class));
            }
        }
        
        // 查询用户相关信息
        if (userId != null) {
            // 是否收藏
            LambdaQueryWrapper<UserCollection> collectionWrapper = new LambdaQueryWrapper<>();
            collectionWrapper.eq(UserCollection::getUserId, userId)
                    .eq(UserCollection::getMaterialId, materialId);
            vo.setIsCollected(collectionMapper.selectCount(collectionWrapper) > 0);
            
            // 用户评分
            LambdaQueryWrapper<UserBehavior> ratingWrapper = new LambdaQueryWrapper<>();
            ratingWrapper.eq(UserBehavior::getUserId, userId)
                    .eq(UserBehavior::getMaterialId, materialId)
                    .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_RATE)
                    .orderByDesc(UserBehavior::getCreateTime)
                    .last("LIMIT 1");
            UserBehavior ratingBehavior = behaviorMapper.selectOne(ratingWrapper);
            if (ratingBehavior != null) {
                vo.setUserRating(ratingBehavior.getRating());
            }
        }
        
        return vo;
    }

    @Override
    public MaterialVO getMaterialDetail(Long materialId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        
        // 解析tags
        if (material.getTags() != null) {
            vo.setTags(JSONUtil.toList(material.getTags(), String.class));
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long materialId, Long userId, Integer duration) {
        // 记录浏览行为
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setMaterialId(materialId);
        behavior.setBehaviorType(Constants.BEHAVIOR_VIEW);
        behavior.setDuration(duration != null ? duration : 0);
        behaviorMapper.insert(behavior);
        
        // 更新浏览量
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material != null) {
            material.setViewCount(material.getViewCount() + 1);
            materialMapper.updateById(material);
        }
        
        // 更新用户画像：累计学习时长
        if (duration != null && duration > 0) {
            LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProfile::getUserId, userId);
            UserProfile profile = userProfileMapper.selectOne(wrapper);
            
            if (profile != null) {
                Integer currentDuration = profile.getLearningDuration() != null ? profile.getLearningDuration() : 0;
                profile.setLearningDuration(currentDuration + duration);
                profile.setLastActiveTime(LocalDateTime.now());
                userProfileMapper.updateById(profile);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCollect(Long materialId, Long userId) {
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getMaterialId, materialId);
        
        UserCollection collection = collectionMapper.selectOne(wrapper);
        
        if (collection != null) {
            // 取消收藏
            collectionMapper.deleteById(collection.getId());
            
            // 记录行为
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setMaterialId(materialId);
            behavior.setBehaviorType(Constants.BEHAVIOR_UNCOLLECT);
            behaviorMapper.insert(behavior);
            
            // 更新收藏量
            LearningMaterial material = materialMapper.selectById(materialId);
            if (material != null && material.getCollectCount() > 0) {
                material.setCollectCount(material.getCollectCount() - 1);
                materialMapper.updateById(material);
            }
        } else {
            // 收藏
            UserCollection newCollection = new UserCollection();
            newCollection.setUserId(userId);
            newCollection.setMaterialId(materialId);
            collectionMapper.insert(newCollection);
            
            // 记录行为
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setMaterialId(materialId);
            behavior.setBehaviorType(Constants.BEHAVIOR_COLLECT);
            behaviorMapper.insert(behavior);
            
            // 更新收藏量
            LearningMaterial material = materialMapper.selectById(materialId);
            if (material != null) {
                material.setCollectCount(material.getCollectCount() + 1);
                materialMapper.updateById(material);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateMaterial(Long materialId, Long userId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        // 记录评分行为
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setMaterialId(materialId);
        behavior.setBehaviorType(Constants.BEHAVIOR_RATE);
        behavior.setRating(rating);
        behaviorMapper.insert(behavior);
        
        // 重新计算平均评分
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getMaterialId, materialId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_RATE);
        List<UserBehavior> ratings = behaviorMapper.selectList(wrapper);
        
        if (!ratings.isEmpty()) {
            double avgRating = ratings.stream()
                    .mapToInt(UserBehavior::getRating)
                    .average()
                    .orElse(0.0);
            
            LearningMaterial material = materialMapper.selectById(materialId);
            if (material != null) {
                material.setAvgRating(avgRating);
                material.setRatingCount(ratings.size());
                materialMapper.updateById(material);
            }
        }
    }

    @Override
    public Page<MaterialVO> getUserCollections(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserCollection> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
                .orderByDesc(UserCollection::getCreateTime);
        
        Page<UserCollection> collectionPage = collectionMapper.selectPage(page, wrapper);
        
        // 转换为MaterialVO
        Page<MaterialVO> voPage = new Page<>(collectionPage.getCurrent(), collectionPage.getSize(), collectionPage.getTotal());
        List<MaterialVO> voList = collectionPage.getRecords().stream().map(collection -> {
            LearningMaterial material = materialMapper.selectById(collection.getMaterialId());
            if (material == null) {
                return null;
            }
            
            MaterialVO vo = new MaterialVO();
            BeanUtils.copyProperties(material, vo);
            
            if (material.getTags() != null) {
                vo.setTags(JSONUtil.toList(material.getTags(), String.class));
            }
            
            vo.setIsCollected(true);
            return vo;
        }).filter(vo -> vo != null).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    // ========== 管理员功能实现 ==========

    @Override
    public Page<MaterialVO> getAdminMaterialList(MaterialQueryDTO queryDTO) {
        Page<LearningMaterial> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        // 管理员可以查看所有状态的资料
        if (queryDTO.getStatus() != null) {
            wrapper.eq(LearningMaterial::getStatus, queryDTO.getStatus());
        }
        
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(LearningMaterial::getTitle, queryDTO.getKeyword())
                    .or().like(LearningMaterial::getDescription, queryDTO.getKeyword()));
        }
        
        // 分类筛选
        if (queryDTO.getCategory() != null && !queryDTO.getCategory().isEmpty()) {
            wrapper.eq(LearningMaterial::getCategory, queryDTO.getCategory());
        }
        
        if (queryDTO.getDifficulty() != null && !queryDTO.getDifficulty().isEmpty()) {
            wrapper.eq(LearningMaterial::getDifficulty, queryDTO.getDifficulty());
        }
        
        // 排序
        wrapper.orderByDesc(LearningMaterial::getCreateTime);
        
        Page<LearningMaterial> materialPage = materialMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<MaterialVO> voPage = new Page<>(materialPage.getCurrent(), materialPage.getSize(), materialPage.getTotal());
        List<MaterialVO> voList = materialPage.getRecords().stream().map(material -> {
            MaterialVO vo = new MaterialVO();
            BeanUtils.copyProperties(material, vo);
            
            if (material.getTags() != null) {
                vo.setTags(JSONUtil.toList(material.getTags(), String.class));
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaterialStatus(Long materialId, Integer status) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        
        // 校验状态是否相同
        if (material.getStatus().equals(status)) {
            String statusText = status == 1 ? "上架" : (status == 0 ? "下架" : "待审核");
            throw new RuntimeException("该资料已经是" + statusText + "状态");
        }
        
        material.setStatus(status);
        materialMapper.updateById(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaterialInfo(Long materialId, MaterialVO materialVO) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        
        // 更新字段
        if (materialVO.getTitle() != null) {
            material.setTitle(materialVO.getTitle());
        }
        if (materialVO.getDescription() != null) {
            material.setDescription(materialVO.getDescription());
        }
        if (materialVO.getCategory() != null) {
            material.setCategory(materialVO.getCategory());
        }
        if (materialVO.getSubCategory() != null) {
            material.setSubCategory(materialVO.getSubCategory());
        }
        if (materialVO.getDifficulty() != null) {
            material.setDifficulty(materialVO.getDifficulty());
        }
        if (materialVO.getTags() != null) {
            material.setTags(JSONUtil.toJsonStr(materialVO.getTags()));
        }
        if (materialVO.getFileUrl() != null) {
            material.setFileUrl(materialVO.getFileUrl());
        }
        
        materialMapper.updateById(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(Long materialId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        
        // 删除资料（级联删除摘要、行为记录、收藏记录）
        materialMapper.deleteById(materialId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMaterial(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new RuntimeException("请选择要删除的资料");
        }
        
        materialMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(Long[] ids, Integer status) {
        if (ids == null || ids.length == 0) {
            throw new RuntimeException("请选择要操作的资料");
        }
        
        // 过滤出需要更新的资料（排除已是目标状态的）
        int updatedCount = 0;
        for (Long id : ids) {
            LearningMaterial material = materialMapper.selectById(id);
            if (material != null && !material.getStatus().equals(status)) {
                material.setStatus(status);
                materialMapper.updateById(material);
                updatedCount++;
            }
        }
        
        if (updatedCount == 0) {
            String statusText = status == 1 ? "上架" : (status == 0 ? "下架" : "待审核");
            throw new RuntimeException("所选资料已经是" + statusText + "状态");
        }
    }

    @Override
    public Map<String, Object> getMaterialStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数
        Long total = materialMapper.selectCount(null);
        stats.put("total", total);
        
        // 在线数量
        LambdaQueryWrapper<LearningMaterial> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_ONLINE);
        Long online = materialMapper.selectCount(onlineWrapper);
        stats.put("online", online);
        
        // 下架数量
        LambdaQueryWrapper<LearningMaterial> offlineWrapper = new LambdaQueryWrapper<>();
        offlineWrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_OFFLINE);
        Long offline = materialMapper.selectCount(offlineWrapper);
        stats.put("offline", offline);
        
        // 待审核数量
        LambdaQueryWrapper<LearningMaterial> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(LearningMaterial::getStatus, Constants.MATERIAL_STATUS_PENDING);
        Long pending = materialMapper.selectCount(pendingWrapper);
        stats.put("pending", pending);
        
        // 总浏览量
        List<LearningMaterial> materials = materialMapper.selectList(null);
        int totalViews = materials.stream()
                .mapToInt(m -> m.getViewCount() != null ? m.getViewCount() : 0)
                .sum();
        stats.put("totalViews", totalViews);
        
        // 总收藏量
        int totalCollects = materials.stream()
                .mapToInt(m -> m.getCollectCount() != null ? m.getCollectCount() : 0)
                .sum();
        stats.put("totalCollects", totalCollects);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMaterial(MaterialVO materialVO, Integer status) {
        // 检查标题是否已存在（防止重复上传）
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getTitle, materialVO.getTitle())
                .eq(LearningMaterial::getFileUrl, materialVO.getFileUrl());
        Long count = materialMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("资料已存在，无法重复上传");
        }

        LearningMaterial material = new LearningMaterial();
        material.setTitle(materialVO.getTitle());
        material.setDescription(materialVO.getDescription());
        material.setCategory(materialVO.getCategory());
        material.setDifficulty(materialVO.getDifficulty());
        material.setFileUrl(materialVO.getFileUrl());
        material.setStatus(status); // 1-直接上架，2-待审核
        material.setViewCount(0);
        material.setCollectCount(0);
        material.setAvgRating(0.0);

        // 处理标签
        if (materialVO.getTags() != null && !materialVO.getTags().isEmpty()) {
            material.setTags(JSONUtil.toJsonStr(materialVO.getTags()));
        }

        materialMapper.insert(material);
    }
    
    @Override
    @Cacheable(value = "materialCache", key = "{#ids, #userId}")
    public List<MaterialVO> getBatchMaterialDetails(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询资料数据
        List<LearningMaterial> materials = materialMapper.selectBatchIds(ids);
        
        // 转换为VO
        List<MaterialVO> vos = materials.stream().map(material -> {
            MaterialVO vo = new MaterialVO();
            BeanUtils.copyProperties(material, vo);
            
            // 解析tags
            if (material.getTags() != null) {
                vo.setTags(JSONUtil.toList(material.getTags(), String.class));
            }
            
            // 查询摘要
            LambdaQueryWrapper<MaterialSummary> summaryWrapper = new LambdaQueryWrapper<>();
            summaryWrapper.eq(MaterialSummary::getMaterialId, material.getId());
            MaterialSummary summary = summaryMapper.selectOne(summaryWrapper);
            
            if (summary != null) {
                vo.setSummaryText(summary.getSummaryText());
                if (summary.getKeyPoints() != null) {
                    vo.setKeyPoints(JSONUtil.toList(summary.getKeyPoints(), String.class));
                }
                if (summary.getKeywords() != null) {
                    vo.setKeywords(JSONUtil.toList(summary.getKeywords(), String.class));
                }
            }
            
            // 查询用户相关信息
            if (userId != null) {
                // 是否收藏
                LambdaQueryWrapper<UserCollection> collectionWrapper = new LambdaQueryWrapper<>();
                collectionWrapper.eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getMaterialId, material.getId());
                vo.setIsCollected(collectionMapper.selectCount(collectionWrapper) > 0);
                
                // 用户评分
                LambdaQueryWrapper<UserBehavior> ratingWrapper = new LambdaQueryWrapper<>();
                ratingWrapper.eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getMaterialId, material.getId())
                        .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_RATE)
                        .orderByDesc(UserBehavior::getCreateTime)
                        .last("LIMIT 1");
                UserBehavior ratingBehavior = behaviorMapper.selectOne(ratingWrapper);
                if (ratingBehavior != null) {
                    vo.setUserRating(ratingBehavior.getRating());
                }
            } else {
                vo.setIsCollected(false);
                vo.setUserRating(null);
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return vos;
    }
    
    @Override
    public Map<Long, List<String>> batchValidateContent(List<MaterialVO> requests) {
        Map<Long, List<String>> validationErrors = new HashMap<>();
        
        if (requests == null || requests.isEmpty()) {
            return validationErrors;
        }
        
        for (MaterialVO request : requests) {
            List<String> errors = new ArrayList<>();
            
            // 验证标题
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                errors.add("标题不能为空");
            } else if (request.getTitle().length() > 200) {
                errors.add("标题长度不能超过200字符");
            }
            
            // 验证描述
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                errors.add("描述不能为空");
            } else if (request.getDescription().length() > 500) {
                errors.add("描述长度不能超过500字符");
            }
            
            // 验证分类
            if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
                errors.add("分类不能为空");
            }
            
            // 验证难度
            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                errors.add("难度不能为空");
            }
            
            // 验证文件URL
            if (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty()) {
                errors.add("文件URL不能为空");
            }
            
            if (!errors.isEmpty()) {
                validationErrors.put(request.getId() != null ? request.getId() : -1L, errors);
            }
        }
        
        return validationErrors;
    }
}
