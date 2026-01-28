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
 * 管理员知识点管理控制器
 */
@Api(tags = "管理员-知识点管理")
@RestController
@RequestMapping("/api/admin/knowledge")
public class AdminKnowledgeController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 更新知识点内容
     */
    @ApiOperation("更新知识点内容")
    @PutMapping("/{id}/content")
    public Result<Void> updateKnowledgePointContent(
            @ApiParam("知识点ID") @PathVariable Long id,
            @RequestBody KnowledgePointVO knowledgePointVO,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 验证用户权限（需要管理员权限）
            if (!userService.isAdminUser(userId)) {
                return Result.error("权限不足，只有管理员可以执行此操作");
            }
            
            // 管理员直接更新知识点内容（包括标题、描述和内容），无需审核
            knowledgePointService.updateKnowledgePointInfo(id, knowledgePointVO);
            
            return Result.success("知识点内容更新成功", null);
        } catch (Exception e) {
            return Result.error("知识点内容更新失败：" + e.getMessage());
        }
    }
}