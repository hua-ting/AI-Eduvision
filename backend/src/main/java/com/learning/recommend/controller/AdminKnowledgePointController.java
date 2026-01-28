package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.dto.KnowledgePointQueryDTO;
import com.learning.recommend.service.AIService;
import com.learning.recommend.service.KnowledgePointService;
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
@RequestMapping("/admin/knowledge")
public class AdminKnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private AIService aiService;

    /**
     * 获取所有知识点列表（包括待审核、已下架）
     */
    @ApiOperation("获取知识点列表")
    @GetMapping("/list")
    public Result<Page<KnowledgePointVO>> getKnowledgePointList(KnowledgePointQueryDTO queryDTO) {
        Page<KnowledgePointVO> page = knowledgePointService.getAdminKnowledgePointList(queryDTO);
        return Result.success(page);
    }

    /**
     * 审核知识点（通过/拒绝）
     */
    @ApiOperation("审核知识点")
    @PutMapping("/{id}/audit")
    public Result<Void> auditKnowledgePoint(
            @ApiParam("知识点ID") @PathVariable Long id,
            @ApiParam("状态:1通过 3拒绝") @RequestParam Integer status,
            @ApiParam("拒绝原因") @RequestParam(required = false) String reason) {
        knowledgePointService.auditKnowledgePoint(id, status, reason);
        return Result.success("审核成功", null);
    }

    /**
     * 上架/下架知识点
     */
    @ApiOperation("上下架知识点")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @ApiParam("知识点ID") @PathVariable Long id,
            @ApiParam("状态:1上架 0下架") @RequestParam Integer status) {
        knowledgePointService.updateKnowledgePointStatus(id, status);
        return Result.success("操作成功", null);
    }

    /**
     * 删除知识点
     */
    @ApiOperation("删除知识点")
    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgePoint(@ApiParam("知识点ID") @PathVariable Long id) {
        knowledgePointService.deleteKnowledgePoint(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除知识点
     */
    @ApiOperation("批量删除知识点")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@ApiParam("知识点ID列表") @RequestBody Long[] ids) {
        knowledgePointService.batchDeleteKnowledgePoint(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 批量上下架
     */
    @ApiOperation("批量上下架")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(
            @ApiParam("知识点ID列表") @RequestBody Long[] ids,
            @ApiParam("状态:1上架 0下架") @RequestParam Integer status) {
        knowledgePointService.batchUpdateStatus(ids, status);
        return Result.success("批量操作成功", null);
    }

    /**
     * 获取知识点详情
     */
    @ApiOperation("获取知识点详情")
    @GetMapping("/{id}")
    public Result<KnowledgePointVO> getKnowledgePointDetail(@ApiParam("知识点ID") @PathVariable Long id) {
        KnowledgePointVO knowledgePoint = knowledgePointService.getKnowledgePointDetail(id);
        return Result.success(knowledgePoint);
    }

    /**
     * 更新知识点信息
     */
    @ApiOperation("更新知识点信息")
    @PutMapping("/{id}")
    public Result<Void> updateKnowledgePoint(
            @ApiParam("知识点ID") @PathVariable Long id,
            @RequestBody KnowledgePointVO knowledgePointVO) {
        knowledgePointService.updateKnowledgePointInfo(id, knowledgePointVO);
        return Result.success("更新成功", null);
    }

    /**
     * 获取知识点统计信息
     */
    @ApiOperation("获取知识点统计")
    @GetMapping("/stats")
    public Result<Object> getKnowledgePointStats() {
        return Result.success(knowledgePointService.getKnowledgePointStats());
    }

    /**
     * AI一键填充知识点
     */
    @ApiOperation("AI一键填充知识点")
    @PostMapping("/generate")
    public Result<Object> generateKnowledgePoints(
            @ApiParam("分类") @RequestParam String category,
            @ApiParam("生成数量") @RequestParam(defaultValue = "10") Integer count) {
        return Result.success(aiService.batchGenerateKnowledgePoints(category, count));
    }
}
