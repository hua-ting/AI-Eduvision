package com.learning.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.recommend.entity.UserBehavior;
import com.learning.recommend.entity.UserProfile;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.mapper.UserBehaviorMapper;
import com.learning.recommend.mapper.UserProfileMapper;
import com.learning.recommend.mapper.KnowledgePointMapper;
import com.learning.recommend.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户画像服务实现
 */
@Slf4j
@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    public UserProfile getUserProfile(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        
        if (profile == null) {
            // 自动初始化
            initUserProfile(userId);
            profile = userProfileMapper.selectOne(wrapper);
        }
        
        return profile;
    }

    @Override
    @Transactional
    public void initUserProfile(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile existing = userProfileMapper.selectOne(wrapper);
        
        if (existing != null) {
            return; // 已存在
        }
        
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setLearningLevel("初级");
        profile.setInterestTags("[]");
        profile.setQaTopics("{}");
        profile.setKnowledgePreferences("{}");
        profile.setTotalQaCount(0);
        profile.setTotalKnowledgeViews(0);
        profile.setTotalKnowledgeCollects(0);
        profile.setLearningDuration(0);
        profile.setLastActiveTime(LocalDateTime.now());
        
        userProfileMapper.insert(profile);
        log.info("初始化用户画像成功, userId={}", userId);
    }

    @Override
    @Async
    @Transactional
    public void updateUserProfile(Long userId) {
        UserProfile profile = getUserProfile(userId);
        
        // 统计知识点浏览次数
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getBehaviorType, "view")
               .isNotNull(UserBehavior::getKnowledgePointId);
        long viewCount = userBehaviorMapper.selectCount(wrapper);
        profile.setTotalKnowledgeViews((int) viewCount);
        
        // 统计知识点收藏次数
        wrapper.clear();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getBehaviorType, "collect")
               .isNotNull(UserBehavior::getKnowledgePointId);
        long collectCount = userBehaviorMapper.selectCount(wrapper);
        profile.setTotalKnowledgeCollects((int) collectCount);
        
        // 分析知识点分类偏好
        Map<String, Double> categoryPreferences = analyzeKnowledgePreferences(userId);
        profile.setKnowledgePreferences(JSON.toJSONString(categoryPreferences));
        
        // 分析兴趣标签
        List<String> interestTags = extractInterestTags(userId);
        profile.setInterestTags(JSON.toJSONString(interestTags));
        
        // 判断学习水平
        profile.setLearningLevel(determineLearningLevel(profile));
        
        profile.setLastActiveTime(LocalDateTime.now());
        userProfileMapper.updateById(profile);
        
        log.info("更新用户画像成功, userId={}", userId);
    }

    @Override
    @Transactional
    public void recordKnowledgeView(Long userId, Long knowledgePointId, String category, Integer duration) {
        UserProfile profile = getUserProfile(userId);
        
        // 更新浏览次数
        profile.setTotalKnowledgeViews(profile.getTotalKnowledgeViews() + 1);
        
        // 更新学习时长
        if (duration != null && duration > 0) {
            int durationMinutes = duration / 60;
            profile.setLearningDuration(profile.getLearningDuration() + durationMinutes);
        }
        
        // 更新分类偏好
        updateCategoryPreference(profile, category, 0.1);
        
        profile.setLastActiveTime(LocalDateTime.now());
        userProfileMapper.updateById(profile);
    }

    @Override
    @Transactional
    public void recordKnowledgeCollect(Long userId, Long knowledgePointId, String category) {
        UserProfile profile = getUserProfile(userId);
        
        profile.setTotalKnowledgeCollects(profile.getTotalKnowledgeCollects() + 1);
        
        // 收藏行为权重更高
        updateCategoryPreference(profile, category, 0.3);
        
        profile.setLastActiveTime(LocalDateTime.now());
        userProfileMapper.updateById(profile);
    }

    @Override
    @Transactional
    public void recordQABehavior(Long userId, String question, String category) {
        UserProfile profile = getUserProfile(userId);
        
        profile.setTotalQaCount(profile.getTotalQaCount() + 1);
        
        // 更新问答主题分布
        JSONObject qaTopics = JSON.parseObject(profile.getQaTopics());
        if (qaTopics == null) {
            qaTopics = new JSONObject();
        }
        int count = qaTopics.getIntValue(category);
        qaTopics.put(category, count + 1);
        profile.setQaTopics(qaTopics.toJSONString());
        
        // 更新分类偏好
        updateCategoryPreference(profile, category, 0.2);
        
        profile.setLastActiveTime(LocalDateTime.now());
        userProfileMapper.updateById(profile);
    }

    @Override
    public Map<String, Object> getUserLearningStats(Long userId) {
        UserProfile profile = getUserProfile(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQaCount", profile.getTotalQaCount());
        stats.put("totalKnowledgeViews", profile.getTotalKnowledgeViews());
        stats.put("totalKnowledgeCollects", profile.getTotalKnowledgeCollects());
        stats.put("learningDuration", profile.getLearningDuration());
        stats.put("learningLevel", profile.getLearningLevel());
        stats.put("lastActiveTime", profile.getLastActiveTime());
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserPreferences(Long userId) {
        UserProfile profile = getUserProfile(userId);
        
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("interestTags", JSON.parseArray(profile.getInterestTags()));
        preferences.put("qaTopics", JSON.parseObject(profile.getQaTopics()));
        preferences.put("knowledgePreferences", JSON.parseObject(profile.getKnowledgePreferences()));
        preferences.put("favoriteCategory", profile.getFavoriteCategory());
        
        return preferences;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 分析知识点分类偏好
     */
    private Map<String, Double> analyzeKnowledgePreferences(Long userId) {
        // 查询最近的知识点行为
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .isNotNull(UserBehavior::getKnowledgePointId)
               .orderByDesc(UserBehavior::getCreateTime)
               .last("LIMIT 100");
        
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(wrapper);
        
        Map<String, Double> categoryScores = new HashMap<>();
        if (behaviors == null || behaviors.isEmpty()) {
            return categoryScores;
        }
        
        // 批量查询知识点分类，避免循环中频繁访问数据库
        Set<Long> kpIds = new HashSet<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior.getKnowledgePointId() != null) {
                kpIds.add(behavior.getKnowledgePointId());
            }
        }
        
        Map<Long, String> kpCategoryMap = new HashMap<>();
        if (!kpIds.isEmpty()) {
            List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectBatchIds(kpIds);
            for (KnowledgePoint kp : knowledgePoints) {
                if (kp != null && kp.getId() != null && kp.getCategory() != null) {
                    kpCategoryMap.put(kp.getId(), kp.getCategory());
                }
            }
        }
        
        for (UserBehavior behavior : behaviors) {
            String category = kpCategoryMap.get(behavior.getKnowledgePointId());
            if (category == null || category.isEmpty()) {
                continue;
            }
            double weight = "collect".equals(behavior.getBehaviorType()) ? 0.5 : 0.1;
            categoryScores.merge(category, weight, Double::sum);
        }
        
        // 归一化
        double maxScore = categoryScores.values().stream().max(Double::compare).orElse(1.0);
        if (maxScore > 0) {
            categoryScores.replaceAll((k, v) -> v / maxScore);
        }
        
        return categoryScores;
    }

    /**
     * 提取兴趣标签
     */
    private List<String> extractInterestTags(Long userId) {
        // 基于问答主题和知识点分类
        UserProfile profile = getUserProfile(userId);
        
        Set<String> tags = new HashSet<>();
        
        // 从问答主题提取
        JSONObject qaTopics = JSON.parseObject(profile.getQaTopics());
        if (qaTopics != null) {
            tags.addAll(qaTopics.keySet());
        }
        
        // 从知识点偏好提取
        JSONObject kpPrefs = JSON.parseObject(profile.getKnowledgePreferences());
        if (kpPrefs != null) {
            tags.addAll(kpPrefs.keySet());
        }
        
        return new ArrayList<>(tags);
    }

    /**
     * 判断学习水平
     */
    private String determineLearningLevel(UserProfile profile) {
        int totalActivity = profile.getTotalQaCount() + 
                          profile.getTotalKnowledgeViews() + 
                          profile.getTotalKnowledgeCollects();
        
        if (totalActivity < 10) {
            return "初级";
        } else if (totalActivity < 50) {
            return "中级";
        } else {
            return "高级";
        }
    }

    /**
     * 更新分类偏好
     */
    private void updateCategoryPreference(UserProfile profile, String category, double weight) {
        if (category == null || category.isEmpty()) {
            return;
        }
        
        JSONObject prefs = JSON.parseObject(profile.getKnowledgePreferences());
        if (prefs == null) {
            prefs = new JSONObject();
        }
        
        double currentScore = prefs.getDoubleValue(category);
        prefs.put(category, currentScore + weight);
        
        profile.setKnowledgePreferences(prefs.toJSONString());
        
        // 更新最喜欢的分类
        String favoriteCategory = category;
        double maxScore = currentScore + weight;
        
        for (Map.Entry<String, Object> entry : prefs.entrySet()) {
            double score = Double.parseDouble(entry.getValue().toString());
            if (score > maxScore) {
                maxScore = score;
                favoriteCategory = entry.getKey();
            }
        }
        
        profile.setFavoriteCategory(favoriteCategory);
    }
}
