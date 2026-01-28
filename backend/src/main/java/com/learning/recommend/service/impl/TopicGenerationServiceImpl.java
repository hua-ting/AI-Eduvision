package com.learning.recommend.service.impl;

import com.learning.recommend.service.AIService;
import com.learning.recommend.service.TopicGenerationService;
import com.learning.recommend.utils.RedisUtil;
import com.learning.recommend.vo.TopicVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 主题生成服务实现
 */
@Service
@Slf4j
public class TopicGenerationServiceImpl implements TopicGenerationService {

    @Autowired
    private AIService aiService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 主题缓存过期时间：30分钟
    private static final long TOPIC_CACHE_TTL = 1800L;

    @Override
    public List<TopicVO> generateTopics(String userProfile, int count) {
        // 尝试从缓存获取
        String cacheKey = "daily:topics:profile:" + userProfile.hashCode();
        Object cachedData = redisUtil.get(cacheKey);

        if (cachedData != null) {
            log.info("从缓存获取主题列表");
            try {
                @SuppressWarnings("unchecked")
                List<TopicVO> cachedTopics = (List<TopicVO>) cachedData;
                int actualCount = Math.min(count, cachedTopics.size());
                // 创建新的ArrayList以避免subList序列化问题
                return new ArrayList<>(cachedTopics.subList(0, actualCount));
            } catch (Exception e) {
                log.warn("缓存数据解析失败，重新生成", e);
            }
        }

        // 缓存未命中，生成主题
        List<TopicVO> topics = generateTopicsFromAI(userProfile, count);

        // 缓存结果
        redisUtil.set(cacheKey, topics, TOPIC_CACHE_TTL);
        log.info("生成主题列表并缓存，数量：{}", topics.size());

        int actualCount = Math.min(count, topics.size());
        // 创建新的ArrayList以避免subList序列化问题
        return new ArrayList<>(topics.subList(0, actualCount));
    }

    /**
     * 从AI服务生成主题
     */
    private List<TopicVO> generateTopicsFromAI(String userProfile, int count) {
        try {
            log.info("调用AI服务生成主题，用户画像：{}, 数量：{}", userProfile, count);
            
            // 构建主题生成提示词
            String prompt = buildTopicGenerationPrompt(userProfile, count);
            
            // 调用AI服务
            String aiResponse = aiService.answerQuestion(prompt);
            
            // 解析AI返回的JSON
            List<TopicVO> topics = parseTopics(aiResponse);
            
            if (topics.isEmpty()) {
                log.warn("AI返回的JSON解析失败，使用默认主题");
                topics = generateDefaultTopics(userProfile, count);
            }
            
            return topics;
        } catch (Exception e) {
            log.error("AI生成主题失败，使用默认主题", e);
            return generateDefaultTopics(userProfile, count);
        }
    }

    /**
     * 构建主题生成提示词
     */
    private String buildTopicGenerationPrompt(String userProfile, int count) {
        return String.format(
            "根据以下用户信息生成 %d 个学习主题：\n\n%s\n\n要求：\n" +
            "1. 主题要实用，适合用户当前水平\n" +
            "2. 涵盖不同但相关的技术领域\n" +
            "3. 难度适中，适合进阶学习\n\n" +
            "只返回 JSON 数组：\n[\n" +
            "  {\n" +
            "    \"title\": \"主题名称\",\n" +
            "    \"description\": \"简要描述学习价值\",\n" +
            "    \"category\": \"所属分类\",\n" +
            "    \"difficulty\": \"初级/中级/高级\"\n" +
            "  }\n" +
            "]\n\n" +
            "注意：只返回JSON，不要其他内容。",
            count, userProfile
        );
    }

    /**
     * 解析AI返回的主题JSON
     */
    private List<TopicVO> parseTopics(String aiResponse) {
        try {
            // 提取JSON部分
            int startIndex = aiResponse.indexOf('[');
            int endIndex = aiResponse.lastIndexOf(']') + 1;
            
            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonArray = aiResponse.substring(startIndex, endIndex);
                return objectMapper.readValue(jsonArray, new TypeReference<List<TopicVO>>() {});
            }
        } catch (Exception e) {
            log.warn("解析AI返回的主题JSON失败", e);
        }
        
        return new ArrayList<>();
    }

    /**
     * 生成默认主题（AI服务不可用时的备用方案）
     */
    private List<TopicVO> generateDefaultTopics(String userProfile, int count) {
        List<TopicVO> defaultTopics = new ArrayList<>();
        
        // 根据用户画像生成一些默认主题
        String[] categories = {"算法", "数据库", "人工智能", "前端开发", "后端开发", "计算机网络", "操作系统"};
        String[] difficulties = {"初级", "中级", "高级"};
        String[] prefixes = {"核心概念", "实战应用", "性能优化", "最佳实践", "常见问题", "进阶技巧"};
        
        for (int i = 0; i < count; i++) {
            String category = categories[ThreadLocalRandom.current().nextInt(categories.length)];
            String difficulty = difficulties[ThreadLocalRandom.current().nextInt(difficulties.length)];
            String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
            
            TopicVO topic = new TopicVO(
                category + prefix,
                "掌握" + category + "的" + prefix + "，提升技术水平",
                category,
                difficulty
            );
            
            defaultTopics.add(topic);
        }
        
        return defaultTopics;
    }

    @Override
    public void clearUserTopicCache(Long userId) {
        // 由于主题是基于用户画像生成的，我们可以基于用户ID清除相关缓存
        String cacheKey = "daily:topics:user:" + userId;
        redisUtil.del(cacheKey);
        log.info("清除用户{}的主题缓存", userId);
    }
}