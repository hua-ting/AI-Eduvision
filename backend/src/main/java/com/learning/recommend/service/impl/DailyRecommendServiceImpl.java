package com.learning.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.entity.UserProfile;
import com.learning.recommend.mapper.KnowledgePointMapper;
import com.learning.recommend.mapper.UserProfileMapper;
import com.learning.recommend.service.AIService;
import com.learning.recommend.service.DailyRecommendService;
import com.learning.recommend.service.KnowledgePointService;
import com.learning.recommend.service.TopicGenerationService;
import com.learning.recommend.utils.RedisUtil;
import com.learning.recommend.vo.KnowledgePointVO;
import com.learning.recommend.vo.TopicVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 每日推荐服务实现
 */
@Service
@Slf4j
public class DailyRecommendServiceImpl implements DailyRecommendService {

    // JVM级别缓存，用于快速访问近期查询结果
    private static final Map<String, List<Long>> jvmCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> jvmCacheTimestamps = new ConcurrentHashMap<>();
    private static final long JVM_CACHE_TTL = 5 * 60 * 1000; // 5分钟

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @Autowired
    private TopicGenerationService topicGenerationService;

    @Override
    /**
     * 获取每日推荐知识点列表
     * @param userId 用户ID，用于获取用户画像信息
     * @param count 推荐的知识点数量
     * @return 推荐的知识点列表
     */
    public List<KnowledgePointVO> getDailyRecommendations(Long userId, Integer count) {
        // 首先尝试从缓存获取
        List<KnowledgePointVO> cachedRecommendations = getCachedRecommendations(userId, count);
        if (cachedRecommendations != null) {
            log.info("从缓存获取用户{}的推荐知识点，数量：{}", userId, cachedRecommendations.size());
            return cachedRecommendations;
        }

        // 缓存未命中，从数据库获取
        // 获取用户画像
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        List<KnowledgePointVO> result;
        if (profile == null) {
            // 新用户，返回热门知识点
            result = getPopularKnowledgePoints(count);
        } else {
            // 基于用户偏好推荐
            result = getPersonalizedRecommendations(profile, count);
        }
        
        // 如果获取到了结果，则缓存
        if (result != null && !result.isEmpty()) {
            // 缓存结果，设置30分钟过期时间
            cacheRecommendations(userId, result, 1800); // 30分钟 = 1800秒
        } else {
            log.warn("用户{}未获取到推荐知识点", userId);
        }
        
        // 批量设置用户相关属性（收藏状态、评分等）
        knowledgePointService.batchSetUserAttributes(result, userId);
        
        return result;
    }

/**
 * 生成推荐知识点的方法
 * 该方法会根据用户画像和指定分类，通过AI生成个性化的知识点
 *
 * @param userId 用户ID，用于获取用户画像信息
 * @param category 知识点分类，用于限定生成内容的范围
 * @return 返回生成的知识点ID
 */
    @Override
    @Transactional  // 使用事务注解，确保方法执行的事务性
    public Long generateRecommendedKnowledgePoint(Long userId, String category) {
        // 获取用户画像
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        try {
            // 构建提示词
            String prompt = buildGenerationPrompt(profile, category);
            
            // 调用AI生成知识点
            Map<String, Object> kpData = aiService.generateKnowledgePoint("", prompt);
            
            // 创建知识点
            KnowledgePoint kp = new KnowledgePoint();
            kp.setTitle((String) kpData.get("title"));
            kp.setCategory(category);
            kp.setSubCategory((String) kpData.get("subCategory"));
            kp.setDescription((String) kpData.get("description"));
            kp.setContent((String) kpData.getOrDefault("content", "详细内容待补充"));
            
            // 处理tags
            Object tagsObj = kpData.get("tags");
            if (tagsObj != null) {
                kp.setTags(JSON.toJSONString(tagsObj));
            } else {
                kp.setTags(JSON.toJSONString(new String[]{"每日推荐", category}));
            }
            
            kp.setDifficulty(getDifficultyByLevel(profile));
            kp.setStatus(1); // 直接上架（每日推荐质量可控）
            kp.setCreatorId(0L); // 系统生成
            kp.setViewCount(0);
            kp.setCollectCount(0);
            kp.setAvgRating(0.0);
            kp.setRatingCount(0);
            
            knowledgePointMapper.insert(kp);
            
            log.info("为用户{}生成每日推荐知识点，分类：{}，ID：{}", userId, category, kp.getId());
            return kp.getId();
            
        } catch (Exception e) {
            log.error("生成推荐知识点失败", e);
            throw new RuntimeException("生成推荐知识点失败，请稍后重试");
        }
    }

    /**
     * 构建AI生成提示词
     */
    private String buildGenerationPrompt(UserProfile profile, String category) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的技术讲师，需要为学生生成一个适合他的学习知识点。\n\n");
        
        // 用户画像信息
        prompt.append("学生画像：\n");
        if (profile != null) {
            prompt.append("- 学习水平：").append(profile.getLearningLevel() != null ? profile.getLearningLevel() : "初学者").append("\n");
            prompt.append("- 学习时长：").append(profile.getLearningDuration() != null ? profile.getLearningDuration()/3600.0 : 0).append("小时\n");
            
            if (profile.getFavoriteCategory() != null) {
                prompt.append("- 感兴趣的方向：").append(profile.getFavoriteCategory()).append("\n");
            }
        }
        
        // 生成要求
        prompt.append("\n请生成一个关于'").append(category).append("'的知识点，要求：\n");
        prompt.append("1. 内容要求：\n");
        prompt.append("   - 适合用户当前水平，由浅入深\n");
        prompt.append("   - 实用性强，贴近实际应用\n");
        prompt.append("   - 包含核心概念、应用场景、最佳实践\n");
        prompt.append("   - 字数400-600字，内容充实详细\n\n");
        
        prompt.append("2. 返回JSON格式：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"知识点标题（30-50字，吸引人且准确）\",\n");
        prompt.append("  \"subCategory\": \"具体子领域\",\n");
        prompt.append("  \"description\": \"简要描述（80-120字）\",\n");
        prompt.append("  \"content\": \"详细内容（400-600字，包含：1.核心概念 2.关键特点 3.应用场景 4.代码示例或图解说明 5.注意事项）\",\n");
        prompt.append("  \"tags\": [\"标签1\", \"标签2\", \"标签3\"],\n");
        prompt.append("  \"difficulty\": \"").append(getDifficultyByLevel(profile)).append("\"\n");
        prompt.append("}\n\n");
        
        prompt.append("注意：只返回JSON，不要其他内容。");
        
        return prompt.toString();
    }

    /**
     * 根据用户水平确定难度
     */
    private String getDifficultyByLevel(UserProfile profile) {
        if (profile == null || profile.getLearningLevel() == null) {
            return "初级";
        }
        
        String level = profile.getLearningLevel();
        if (level.contains("初") || level.contains("beginner")) {
            return "初级";
        } else if (level.contains("高") || level.contains("advanced")) {
            return "高级";
        }
        return "中级";
    }

    /**
     * 获取热门知识点（新用户）
     */
    private List<KnowledgePointVO> getPopularKnowledgePoints(Integer count) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getStatus, 1)
                .orderByDesc(KnowledgePoint::getViewCount)
                .last("LIMIT " + count);
        
        List<KnowledgePoint> list = knowledgePointMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 个性化推荐（优化版）
     */
    private List<KnowledgePointVO> getPersonalizedRecommendations(UserProfile profile, Integer count) {
        List<KnowledgePointVO> result = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();
        
        // 1. 优先推荐用户感兴趣分类的高质量知识点（40%）
        if (profile.getFavoriteCategory() != null) {
            int favoriteCount = Math.max(count * 4 / 10, 2);
            LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgePoint::getStatus, 1)
                    .eq(KnowledgePoint::getCategory, profile.getFavoriteCategory())
                    .orderByDesc(KnowledgePoint::getAvgRating)
                    .orderByDesc(KnowledgePoint::getCollectCount)
                    .last("LIMIT " + favoriteCount);
            
            List<KnowledgePoint> favorites = knowledgePointMapper.selectList(wrapper);
            for (KnowledgePoint kp : favorites) {
                result.add(convertToVO(kp));
                addedIds.add(kp.getId());
            }
        }
        
        // 2. 基于用户画像的协同推荐（30%）
        if (result.size() < count && profile.getKnowledgePreferences() != null) {
            int cfCount = Math.max(count * 3 / 10, 2);
            List<KnowledgePointVO> cfResults = getCollaborativeRecommendations(profile, cfCount, addedIds);
            result.addAll(cfResults);
            cfResults.forEach(vo -> addedIds.add(vo.getId()));
        }
        
        // 3. 补充热门知识点（确保多样性）
        if (result.size() < count) {
            int remaining = count - result.size();
            List<KnowledgePointVO> popular = getDiversePopularKnowledgePoints(remaining, addedIds);
            result.addAll(popular);
        }
        
        return result.stream().limit(count).collect(Collectors.toList());
    }
    
    /**
     * 基于用户画像的协同推荐
     */
    private List<KnowledgePointVO> getCollaborativeRecommendations(UserProfile profile, Integer count, Set<Long> excludeIds) {
        try {
            // 解析用户知识点偏好
            Map<String, Double> preferences = JSON.parseObject(profile.getKnowledgePreferences(), Map.class);
            if (preferences == null || preferences.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 按偏好分数推荐不同分类的知识点
            List<KnowledgePointVO> result = new ArrayList<>();
            List<String> topCategories = preferences.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            for (String category : topCategories) {
                LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(KnowledgePoint::getStatus, 1)
                        .eq(KnowledgePoint::getCategory, category)
                        .orderByDesc(KnowledgePoint::getAvgRating)
                        .last("LIMIT 2");
                
                List<KnowledgePoint> kps = knowledgePointMapper.selectList(wrapper);
                for (KnowledgePoint kp : kps) {
                    if (!excludeIds.contains(kp.getId())) {
                        result.add(convertToVO(kp));
                        if (result.size() >= count) break;
                    }
                }
                if (result.size() >= count) break;
            }
            
            return result;
        } catch (Exception e) {
            log.error("协同推荐失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取多样化的热门知识点
     */
    private List<KnowledgePointVO> getDiversePopularKnowledgePoints(Integer count, Set<Long> excludeIds) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getStatus, 1)
                .orderByDesc(KnowledgePoint::getViewCount)
                .orderByDesc(KnowledgePoint::getAvgRating)
                .last("LIMIT " + (count * 3)); // 多取一些用于多样性筛选
        
        List<KnowledgePoint> allPopular = knowledgePointMapper.selectList(wrapper);
        
        // 确保分类多样性
        List<KnowledgePointVO> result = new ArrayList<>();
        Set<String> usedCategories = new HashSet<>();
        
        // 第一轮：每个分类取一个
        for (KnowledgePoint kp : allPopular) {
            if (excludeIds.contains(kp.getId())) continue;
            if (!usedCategories.contains(kp.getCategory())) {
                result.add(convertToVO(kp));
                usedCategories.add(kp.getCategory());
                if (result.size() >= count) return result;
            }
        }
        
        // 第二轮：补充剩余
        for (KnowledgePoint kp : allPopular) {
            if (excludeIds.contains(kp.getId())) continue;
            if (result.stream().noneMatch(vo -> vo.getId().equals(kp.getId()))) {
                result.add(convertToVO(kp));
                if (result.size() >= count) break;
            }
        }
        
        return result;
    }

    /**
     * 转换为VO
     */
    private KnowledgePointVO convertToVO(KnowledgePoint kp) {
        KnowledgePointVO vo = new KnowledgePointVO();
        BeanUtils.copyProperties(kp, vo);
        
        // 解析JSON字段
        try {
            if (kp.getTags() != null) {
                vo.setTags(JSON.parseArray(kp.getTags(), String.class));
            }
        } catch (Exception e) {
            log.error("解析tags失败", e);
        }
        
        return vo;
    }

    @Override
    public List<KnowledgePointVO> getCachedRecommendations(Long userId, Integer count) {
        String cacheKey = "daily:recommendations:user:" + userId;
        
        // 先检查JVM缓存
        List<Long> jvmCachedIds = jvmCache.get(cacheKey);
        if (jvmCachedIds != null) {
            long cacheTime = System.currentTimeMillis() - getCacheTime(cacheKey + ":time");
            if (cacheTime < JVM_CACHE_TTL) {
                log.info("从JVM缓存获取用户{}的推荐知识点ID列表，数量：{}", userId, jvmCachedIds.size());
                // 根据ID列表查询数据库获取完整信息
                if (!jvmCachedIds.isEmpty()) {
                    List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectBatchIds(jvmCachedIds);
                    List<KnowledgePointVO> result = knowledgePoints.stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                    log.info("从数据库查询用户{}的推荐知识点详情，数量：{}", userId, result.size());
                    
                    // 批量设置用户相关属性（收藏状态、评分等）
                    knowledgePointService.batchSetUserAttributes(result, userId);
                    
                    return result.stream().limit(count).collect(Collectors.toList());
                }
            } else {
                // JVM缓存过期，清除它
                jvmCache.remove(cacheKey);
                removeCacheTime(cacheKey + ":time");
            }
        }
        
        // JVM缓存未命中，检查Redis缓存
        Object cachedData = redisUtil.get(cacheKey);
        
        if (cachedData != null) {
            @SuppressWarnings("unchecked")
            List<Long> cachedIds = (List<Long>) cachedData;
            log.info("从Redis缓存获取用户{}的推荐知识点ID列表，数量：{}", userId, cachedIds.size());
            
            // 将结果存储到JVM缓存
            jvmCache.put(cacheKey, cachedIds);
            setCacheTime(cacheKey + ":time", System.currentTimeMillis());
            
            // 根据ID列表查询数据库获取完整信息
            if (!cachedIds.isEmpty()) {
                List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectBatchIds(cachedIds);
                List<KnowledgePointVO> result = knowledgePoints.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                log.info("从数据库查询用户{}的推荐知识点详情，数量：{}", userId, result.size());
                
                // 批量设置用户相关属性（收藏状态、评分等）
                knowledgePointService.batchSetUserAttributes(result, userId);
                
                return result.stream().limit(count).collect(Collectors.toList());
            }
        }
        
        return null; // 缓存未命中
    }

    @Override
    public boolean cacheRecommendations(Long userId, List<KnowledgePointVO> recommendations, long ttl) {
        // 只缓存知识点ID列表，而不是整个对象
        List<Long> ids = recommendations.stream()
            .map(KnowledgePointVO::getId)
            .collect(Collectors.toList());
        
        String cacheKey = "daily:recommendations:user:" + userId;
        boolean result = redisUtil.set(cacheKey, ids, ttl);
        log.info("缓存用户{}的推荐知识点ID列表到Redis，数量：{}，结果：{}", userId, ids.size(), result);
        
        // 同时缓存到JVM
        if (result) {
            jvmCache.put(cacheKey, ids);
            setCacheTime(cacheKey + ":time", System.currentTimeMillis());
        }
        
        return result;
    }

    @Override
    public boolean clearCachedRecommendations(Long userId) {
        String cacheKey = "daily:recommendations:user:" + userId;
        redisUtil.del(cacheKey);
        
        // 同时清除JVM缓存
        jvmCache.remove(cacheKey);
        removeCacheTime(cacheKey + ":time");
        
        return true;
    }

    private long getCacheTime(String key) {
        Long time = jvmCacheTimestamps.get(key);
        return time != null ? time : 0L;
    }

    private void setCacheTime(String key, long time) {
        jvmCacheTimestamps.put(key, time);
    }

    private void removeCacheTime(String key) {
        jvmCacheTimestamps.remove(key);
    }
    
    @Override
    public List<TopicVO> generateTopics(Long userId, Integer count) {
        // 首先尝试从缓存获取
        String cacheKey = "daily:topics:user:" + userId;
        Object cachedData = redisUtil.get(cacheKey);
        
        if (cachedData != null) {
            log.info("从缓存获取用户{}的主题列表", userId);
            @SuppressWarnings("unchecked")
            List<TopicVO> cachedTopics = (List<TopicVO>) cachedData;
            int actualCount = Math.min(count, cachedTopics.size());
            // 创建新的ArrayList以避免subList序列化问题
            return new ArrayList<>(cachedTopics.subList(0, actualCount));
        }
        
        // 缓存未命中，获取用户画像并生成主题
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        
        String userProfileInfo = buildUserProfileString(profile);
        
        // 使用主题生成服务生成主题
        List<TopicVO> topics = topicGenerationService.generateTopics(userProfileInfo, count != null ? count : 6);
        
        // 缓存生成的主题
        redisUtil.set(cacheKey, topics, 1800); // 30分钟缓存
        
        int actualCount = Math.min(count, topics.size());
        // 创建新的ArrayList以避免subList序列化问题
        return new ArrayList<>(topics.subList(0, actualCount));
    }
    
    /**
     * 构建用户画像字符串
     */
    private String buildUserProfileString(UserProfile profile) {
        if (profile == null) {
            return "新用户，学习水平：初级，偏好方向：计算机科学";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("学习水平：").append(profile.getLearningLevel() != null ? profile.getLearningLevel() : "初级").append("\n");
        sb.append("学习时长：").append(profile.getLearningDuration() != null ? profile.getLearningDuration()/3600.0 : 0).append("小时\n");
        if (profile.getFavoriteCategory() != null) {
            sb.append("感兴趣的方向：").append(profile.getFavoriteCategory()).append("\n");
        }
        if (profile.getInterestTags() != null) {
            sb.append("兴趣标签：").append(profile.getInterestTags()).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public void clearUserTopicCache(Long userId) {
        String cacheKey = "daily:topics:user:" + userId;
        redisUtil.del(cacheKey);
        
        // 同时清除TopicGenerationService中基于用户画像的缓存
        // 由于我们无法直接获取用户画像信息来构建完全相同的缓存键，
        // 清除所有主题相关的缓存
        redisUtil.delByPattern("daily:topics:profile:*");
    }
}
