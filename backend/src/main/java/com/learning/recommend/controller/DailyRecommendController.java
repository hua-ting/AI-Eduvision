package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.service.DailyRecommendService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.KnowledgePointVO;
import com.learning.recommend.vo.TopicVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 每日推荐控制器
 */
@RestController
@RequestMapping("/api/daily")
@Api(tags = "每日推荐")
@Slf4j
public class DailyRecommendController {

    @Autowired
    private DailyRecommendService dailyRecommendService;

    @GetMapping("/recommendations")
    @ApiOperation("获取每日推荐")
    public Result<List<KnowledgePointVO>> getDailyRecommendations(
            @RequestParam(defaultValue = "5") Integer count,
            HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            List<KnowledgePointVO> recommendations = dailyRecommendService.getDailyRecommendations(userId, count);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取每日推荐失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/generate")
    @ApiOperation("生成推荐知识点")
    public Result<Long> generateRecommendedKnowledgePoint(
            @RequestParam String category,
            HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            Long kpId = dailyRecommendService.generateRecommendedKnowledgePoint(userId, category);
            return Result.success(kpId);
        } catch (Exception e) {
            log.error("生成推荐知识点失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/recommendations")
    @ApiOperation("清除用户缓存的推荐知识点（允许重新生成）")
    public Result<String> clearCachedRecommendations(HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            boolean success = dailyRecommendService.clearCachedRecommendations(userId);
            if (success) {
                return Result.success("推荐知识点缓存已清除，下次访问将重新生成");
            } else {
                return Result.error("清除缓存失败");
            }
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/topics")
    @ApiOperation("生成主题列表（用于AI知识点创作）")
    public Result<List<TopicVO>> getTopics(
            @RequestParam(defaultValue = "6") Integer count,
            HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            List<TopicVO> topics = dailyRecommendService.generateTopics(userId, count);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("生成主题失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/topics/clear")
    @ApiOperation("清除用户主题缓存")
    public Result<String> clearTopicCache(HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            dailyRecommendService.clearUserTopicCache(userId);
            return Result.success("主题缓存已清除");
        } catch (Exception e) {
            log.error("清除主题缓存失败", e);
            return Result.error(e.getMessage());
        }
    }
}
