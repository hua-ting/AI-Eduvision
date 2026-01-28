package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.dto.KnowledgePointQueryDTO;
import com.learning.recommend.service.KnowledgePointService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.KnowledgePointVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识点控制器
 */
@Api(tags = "知识点管理")
@RestController
@RequestMapping("/knowledge")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取知识点列表
     */
    @ApiOperation("获取知识点列表")
    @GetMapping("/list")
    public Result<Page<KnowledgePointVO>> getKnowledgePointList(
            KnowledgePointQueryDTO queryDTO,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        Page<KnowledgePointVO> page = knowledgePointService.getKnowledgePointList(queryDTO, userId);
        return Result.success(page);
    }

    /**
     * 获取知识点详情
     */
    @ApiOperation("获取知识点详情")
    @GetMapping("/{id}")
    public Result<KnowledgePointVO> getKnowledgePointDetail(
            @ApiParam("知识点ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        KnowledgePointVO vo = knowledgePointService.getKnowledgePointDetail(id, userId);
        return Result.success(vo);
    }

    /**
     * 记录浏览
     */
    @ApiOperation("记录浏览行为")
    @PostMapping("/{id}/view")
    public Result<Void> recordView(
            @ApiParam("知识点ID") @PathVariable Long id,
            @ApiParam("浏览时长(秒)") @RequestParam(required = false) Integer duration,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        knowledgePointService.recordView(id, userId, duration);
        return Result.success("记录成功", null);
    }

    /**
     * 收藏/取消收藏
     */
    @ApiOperation("收藏/取消收藏")
    @PostMapping("/{id}/collect")
    public Result<Void> toggleCollect(
            @ApiParam("知识点ID") @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        knowledgePointService.toggleCollect(id, userId);
        return Result.success("操作成功", null);
    }

    /**
     * 评分
     */
    @ApiOperation("对知识点评分")
    @PostMapping("/{id}/rate")
    public Result<Void> rateKnowledgePoint(
            @ApiParam("知识点ID") @PathVariable Long id,
            @ApiParam("评分(1-5)") @RequestParam Integer rating,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        knowledgePointService.rateKnowledgePoint(id, userId, rating);
        return Result.success("评分成功", null);
    }

    /**
     * 获取我的收藏
     */
    @ApiOperation("获取我的收藏列表")
    @GetMapping("/my-collections")
    public Result<Page<KnowledgePointVO>> getMyCollections(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        Page<KnowledgePointVO> page = knowledgePointService.getUserCollections(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 用户贡献知识点（需要审核）
     */
    @ApiOperation("贡献知识点")
    @PostMapping("/contribute")
    public Result<Void> contributeKnowledgePoint(
            @RequestBody KnowledgePointVO knowledgePointVO,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        knowledgePointService.createKnowledgePoint(knowledgePointVO, userId);
        return Result.success("提交成功，等待审核", null);
    }

    /**
     * 批量获取知识点详情
     */
    @ApiOperation("批量获取知识点详情")
    @GetMapping("/batch/detail")
    public Result<java.util.List<KnowledgePointVO>> getBatchKnowledgePointDetails(
            @ApiParam("知识点ID列表") @RequestParam java.util.List<Long> ids,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        java.util.List<KnowledgePointVO> vos = knowledgePointService.getBatchKnowledgePointDetails(ids, userId);
        return Result.success(vos);
    }

    /**
     * 批量更新知识点
     */
    @ApiOperation("批量更新知识点")
    @PostMapping("/batch/update")
    public Result<Void> batchUpdateKnowledgePoints(
            @RequestBody java.util.List<KnowledgePointVO> knowledgePoints,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        knowledgePointService.batchUpdateKnowledgePoints(knowledgePoints, userId);
        return Result.success();
    }

    /**
     * 批量验证内容
     */
    @ApiOperation("批量验证内容")
    @PostMapping("/validate/batch")
    public Result<java.util.Map<Long, java.util.List<String>>> batchValidateContent(
            @RequestBody java.util.List<com.learning.recommend.vo.KnowledgePointVO> requests) {
        
        java.util.Map<Long, java.util.List<String>> validationErrors = knowledgePointService.batchValidateContent(requests);
        return Result.success(validationErrors);
    }
}
