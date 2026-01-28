package com.learning.recommend.service;

import com.learning.recommend.entity.UserProfile;

import java.util.Map;

/**
 * 用户画像服务接口
 */
public interface UserProfileService {
    
    /**
     * 获取用户画像
     */
    UserProfile getUserProfile(Long userId);
    
    /**
     * 初始化用户画像
     */
    void initUserProfile(Long userId);
    
    /**
     * 更新用户画像（异步）
     */
    void updateUserProfile(Long userId);
    
    /**
     * 记录知识点浏览行为
     */
    void recordKnowledgeView(Long userId, Long knowledgePointId, String category, Integer duration);
    
    /**
     * 记录知识点收藏行为
     */
    void recordKnowledgeCollect(Long userId, Long knowledgePointId, String category);
    
    /**
     * 记录问答行为
     */
    void recordQABehavior(Long userId, String question, String category);
    
    /**
     * 获取用户学习统计
     */
    Map<String, Object> getUserLearningStats(Long userId);
    
    /**
     * 获取用户偏好分析
     */
    Map<String, Object> getUserPreferences(Long userId);
}
