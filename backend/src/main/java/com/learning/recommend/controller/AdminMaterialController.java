package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.dto.MaterialQueryDTO;
import com.learning.recommend.service.MaterialService;
import com.learning.recommend.vo.MaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员资料管理控制器
 */
@Api(tags = "管理员-资料管理")
@RestController
@RequestMapping("/admin/material")
public class AdminMaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 获取所有资料列表（包括待审核、已下架）
     */
    @ApiOperation("获取资料列表")
    @GetMapping("/list")
    public Result<Page<MaterialVO>> getMaterialList(MaterialQueryDTO queryDTO) {
        Page<MaterialVO> page = materialService.getAdminMaterialList(queryDTO);
        return Result.success(page);
    }

    /**
     * 审核资料（通过/拒绝）
     */
    @ApiOperation("审核资料")
    @PutMapping("/{id}/review")
    public Result<Void> reviewMaterial(
            @ApiParam("资料ID") @PathVariable Long id,
            @ApiParam("状态:1上架 0下架") @RequestParam Integer status) {
        materialService.updateMaterialStatus(id, status);
        return Result.success("审核成功", null);
    }

    /**
     * 上架/下架资料
     */
    @ApiOperation("上下架资料")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @ApiParam("资料ID") @PathVariable Long id,
            @ApiParam("状态:1上架 0下架") @RequestParam Integer status) {
        materialService.updateMaterialStatus(id, status);
        return Result.success("操作成功", null);
    }

    /**
     * 删除资料
     */
    @ApiOperation("删除资料")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMaterial(@ApiParam("资料ID") @PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除资料
     */
    @ApiOperation("批量删除资料")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@ApiParam("资料ID列表") @RequestBody Long[] ids) {
        materialService.batchDeleteMaterial(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 批量上下架
     */
    @ApiOperation("批量上下架")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(
            @ApiParam("资料ID列表") @RequestBody Long[] ids,
            @ApiParam("状态:1上架 0下架") @RequestParam Integer status) {
        materialService.batchUpdateStatus(ids, status);
        return Result.success("批量操作成功", null);
    }

    /**
     * 获取资料详情
     */
    @ApiOperation("获取资料详情")
    @GetMapping("/{id}")
    public Result<MaterialVO> getMaterialDetail(@ApiParam("资料ID") @PathVariable Long id) {
        MaterialVO material = materialService.getMaterialDetail(id);
        return Result.success(material);
    }

    /**
     * 更新资料信息
     */
    @ApiOperation("更新资料信息")
    @PutMapping("/{id}")
    public Result<Void> updateMaterial(
            @ApiParam("资料ID") @PathVariable Long id,
            @RequestBody MaterialVO materialVO) {
        materialService.updateMaterialInfo(id, materialVO);
        return Result.success("更新成功", null);
    }

    /**
     * 获取资料统计信息
     */
    @ApiOperation("获取资料统计")
    @GetMapping("/stats")
    public Result<Object> getMaterialStats() {
        return Result.success(materialService.getMaterialStats());
    }

    /**
     * 上传资料（管理员直接上架）
     */
    @ApiOperation("上传资料")
    @PostMapping("/upload")
    public Result<Void> uploadMaterial(@RequestBody MaterialVO materialVO) {
        materialService.createMaterial(materialVO, 1); // 管理员上传直接上架
        return Result.success("上传成功", null);
    }
}
