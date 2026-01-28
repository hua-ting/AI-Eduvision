package com.learning.recommend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.dto.LoginDTO;
import com.learning.recommend.dto.RegisterDTO;
import com.learning.recommend.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    Map<String, Object> login(LoginDTO loginDTO);
    
    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserVO userVO);
    
    // ========== 管理员功能 ==========
    
    /**
     * 管理员获取用户列表
     */
    Page<UserVO> getAdminUserList(String keyword, Integer role, Integer pageNum, Integer pageSize);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 管理员更新用户信息（包括角色和状态）
     */
    void updateUserInfoByAdmin(Long userId, UserVO userVO);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取最近浏览记录
     */
    java.util.List<Map<String, Object>> getRecentViews(Long userId, Integer limit);

    /**
     * 获取知识点最近浏览记录
     */
    java.util.List<Map<String, Object>> getRecentKnowledgeViews(Long userId, Integer limit);

    /**
     * 检查用户是否为管理员
     */
    boolean isAdminUser(Long userId);
}