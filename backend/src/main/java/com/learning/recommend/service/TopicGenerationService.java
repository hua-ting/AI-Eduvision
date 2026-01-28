package com.learning.recommend.service;

import com.learning.recommend.vo.TopicVO;

import java.util.List;

/**
 * 主题生成服务接口
 */
public interface TopicGenerationService {
    
    /**
     * 生成主题列表
     * @param userProfile 用户画像信息
     * @param count 主题数量
     * @return 主题列表
     */
    List<TopicVO> generateTopics(String userProfile, int count);
    
    /**
     * 清除用户主题缓存
     * @param userId 用户ID
     */
    void clearUserTopicCache(Long userId);
}