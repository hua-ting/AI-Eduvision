package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.dto.ContentUpdateDTO;
import com.learning.recommend.dto.ReviewRequestDTO;
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
 * 内容管理控制器 - 处理权限化编辑功能
 */
@Api(tags = "内容管理")
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 保存内容（管理员权限）
     */
    @ApiOperation("保存内容（管理员权限）")
    @PostMapping("/save")
    public Result<Void> saveContent(
            @RequestBody ContentUpdateDTO contentUpdateDTO,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        // 验证用户权限（需要管理员权限）
        if (!userService.isAdminUser(userId)) {
            return Result.error("权限不足，只有管理员可以执行此操作");
        }
        
        knowledgePointService.updateKnowledgePointContent(contentUpdateDTO.getId(), contentUpdateDTO.getContent(), userId);
        return Result.success("内容保存成功", null);
    }

    /**
     * 提交内容审核申请（普通用户权限）
     */
    @ApiOperation("提交内容审核申请（普通用户权限）")
    @PostMapping("/submit-review")
    public Result<Void> submitForReview(
            @RequestBody ReviewRequestDTO reviewRequestDTO,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        // 验证用户权限（普通用户也可以提交审核申请）
        if (userService.isAdminUser(userId)) {
            // 如果是管理员，可以直接更新内容
            knowledgePointService.updateKnowledgePointContent(reviewRequestDTO.getContentId(), reviewRequestDTO.getContent(), userId);
            return Result.success("管理员直接更新内容成功", null);
        } else {
            // 普通用户提交审核申请
            knowledgePointService.submitContentReview(reviewRequestDTO.getContentId(), reviewRequestDTO.getContent(), userId);
            return Result.success("审核申请已提交，等待管理员审核", null);
        }
    }

    /**
     * 获取内容详情
     */
    @ApiOperation("获取内容详情")
    @GetMapping("/{id}")
    public Result<KnowledgePointVO> getContentById(
            @ApiParam("内容ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        KnowledgePointVO vo = knowledgePointService.getKnowledgePointDetail(id, userId);
        if (vo == null) {
            return Result.error("内容不存在");
        }
        return Result.success(vo);
    }
}