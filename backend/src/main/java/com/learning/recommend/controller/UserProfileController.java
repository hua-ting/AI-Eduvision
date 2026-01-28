package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.entity.UserProfile;
import com.learning.recommend.service.UserProfileService;
import com.learning.recommend.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户画像控制器
 */
@Api(tags = "用户画像")
@RestController
@RequestMapping("/api/user-profile")
public class UserProfileController {

    @Resource
    private UserProfileService userProfileService;

    @ApiOperation("获取用户画像")
    @GetMapping
    public Result<UserProfile> getUserProfile(HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        UserProfile profile = userProfileService.getUserProfile(userId);
        return Result.success(profile);
    }

    @ApiOperation("获取学习统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getLearningStats(HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        Map<String, Object> stats = userProfileService.getUserLearningStats(userId);
        return Result.success(stats);
    }

    @ApiOperation("获取用户偏好")
    @GetMapping("/preferences")
    public Result<Map<String, Object>> getUserPreferences(HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        Map<String, Object> preferences = userProfileService.getUserPreferences(userId);
        return Result.success(preferences);
    }

    @ApiOperation("手动更新画像")
    @GetMapping("/refresh")
    public Result<String> refreshProfile(HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        userProfileService.updateUserProfile(userId);
        return Result.success("画像更新中...");
    }
}
