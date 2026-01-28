package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.service.UserService;
import com.learning.recommend.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 */
@Api(tags = "管理员-用户管理")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    public Result<Page<UserVO>> getUserList(
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("角色:0学生 1管理员") @RequestParam(required = false) Integer role,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserVO> page = userService.getAdminUserList(keyword, role, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 禁用/启用用户
     */
    @ApiOperation("禁用/启用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @ApiParam("用户ID") @PathVariable Long id,
            @ApiParam("状态:0禁用 1正常") @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success("操作成功", null);
    }

    /**
     * 删除用户
     */
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@ApiParam("用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取用户详情
     */
    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@ApiParam("用户ID") @PathVariable Long id) {
        UserVO userVO = userService.getUserInfo(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息（管理员权限）
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @ApiParam("用户ID") @PathVariable Long id,
            @RequestBody UserVO userVO) {
        userService.updateUserInfoByAdmin(id, userVO);
        return Result.success("更新成功", null);
    }
}
