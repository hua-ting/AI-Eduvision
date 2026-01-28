package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.service.KnowledgePointService;
import com.learning.recommend.service.UserService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.KnowledgePointVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员知识点审核控制器
 */
@Api(tags = "管理员-知识点审核管理")
@RestController
@RequestMapping("/api/admin/knowledge-review")
public class AdminKnowledgeReviewController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;

    /**
     * 获取待审核的知识点列表
     */
    @ApiOperation("获取待审核的知识点列表")
    @GetMapping("/pending")
    public Result<Object> getPendingReviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 检查用户是否为管理员
            if (!userService.isAdminUser(userId)) {
                return Result.error("权限不足，只有管理员可以执行此操作");
            }
            
            // 获取待审核的知识点列表
            var result = knowledgePointService.getPendingReviewKnowledgePoints(pageNum, pageSize);
            return Result.success("获取待审核列表成功", result);
        } catch (Exception e) {
            return Result.error("获取待审核列表失败：" + e.getMessage());
        }
    }

    /**
     * 审核知识点修改申请
     */
    @ApiOperation("审核知识点修改申请")
    @PostMapping("/review/{id}")
    public Result reviewKnowledgePoint(
            @ApiParam("知识点ID") @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> requestData,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 检查用户是否为管理员
            if (!userService.isAdminUser(userId)) {
                return Result.error("权限不足，只有管理员可以执行此操作");
            }
            
            // 从请求体中提取参数
            Integer status = Integer.valueOf(requestData.get("status").toString());
            String reason = requestData.containsKey("reason") ? requestData.get("reason").toString() : null;

            // 调用服务进行审核
            knowledgePointService.reviewKnowledgePointContent(id, status, reason, userId);

            return Result.success("审核操作成功");
        } catch (Exception e) {
            return Result.error("审核操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核历史
     */
    @ApiOperation("获取审核历史")
    @GetMapping("/history")
    public Result<Object> getReviewHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 检查用户是否为管理员
            if (!userService.isAdminUser(userId)) {
                return Result.error("权限不足，只有管理员可以执行此操作");
            }
            
            // TODO: 实现审核历史查询功能
            // 这需要创建一个专门的审核记录表来跟踪审核历史
            return Result.success("获取审核历史成功", null);
        } catch (Exception e) {
            return Result.error("获取审核历史失败：" + e.getMessage());
        }
    }
}