package com.learning.recommend.service;

import com.learning.recommend.vo.KnowledgePointVO;
import com.learning.recommend.vo.TopicVO;

import java.util.List;

/**
 * 每日推荐服务接口
 */
public interface DailyRecommendService {
    
    /**
     * 获取每日推荐知识点
     * @param userId 用户ID
     * @param count 推荐数量
     * @return 推荐知识点列表
     */
    List<KnowledgePointVO> getDailyRecommendations(Long userId, Integer count);
    
    /**
     * 基于用户画像生成推荐知识点
     * @param userId 用户ID
     * @param category 分类
     * @return 生成的知识点ID
     */
    Long generateRecommendedKnowledgePoint(Long userId, String category);

    /**
     * 获取用户缓存的推荐知识点
     * @param userId 用户ID
     * @param count 推荐数量
     * @return 推荐知识点列表
     */
    List<KnowledgePointVO> getCachedRecommendations(Long userId, Integer count);

    /**
     * 缓存用户的推荐知识点
     * @param userId 用户ID
     * @param recommendations 推荐知识点列表
     * @param ttl 缓存时间（秒）
     * @return 是否缓存成功
     */
    boolean cacheRecommendations(Long userId, List<KnowledgePointVO> recommendations, long ttl);

    /**
     * 清除用户缓存的推荐知识点
     * @param userId 用户ID
     * @return 是否清除成功
     */
    boolean clearCachedRecommendations(Long userId);

    /**
     * 生成主题列表（用于AI知识点创作页面）
     * @param userId 用户ID
     * @param count 主题数量
     * @return 主题列表
     */
    List<TopicVO> generateTopics(Long userId, Integer count);

    /**
     * 清除用户主题缓存
     * @param userId 用户ID
     * @return 是否清除成功
     */
    void clearUserTopicCache(Long userId);
}
