package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.dto.LoginDTO;
import com.learning.recommend.dto.RegisterDTO;
import com.learning.recommend.service.UserService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功", null);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return Result.success(result);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        // 从Token中获取用户ID
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserVO userVO) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        userService.updateUserInfo(userId, userVO);
        return Result.success("更新成功", null);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 前端清除Token即可
        return Result.success("退出成功", null);
    }

    @ApiOperation("获取用户统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        Map<String, Object> stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    @ApiOperation("获取最近浏览记录")
    @GetMapping("/recent-views")
    public Result<java.util.List<Map<String, Object>>> getRecentViews(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") Integer limit) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        java.util.List<Map<String, Object>> recentViews = userService.getRecentViews(userId, limit);
        return Result.success(recentViews);
    }

    @ApiOperation("获取知识点最近浏览记录")
    @GetMapping("/recent-knowledge-views")
    public Result<java.util.List<Map<String, Object>>> getRecentKnowledgeViews(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") Integer limit) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        java.util.List<Map<String, Object>> recentViews = userService.getRecentKnowledgeViews(userId, limit);
        return Result.success(recentViews);
    }
}
