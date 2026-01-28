package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.dto.MaterialQueryDTO;
import com.learning.recommend.service.MaterialService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.MaterialVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学习资料控制器
 */
@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取资料列表
     */
    @GetMapping("/list")
    public Result<Page<MaterialVO>> getMaterialList(
            MaterialQueryDTO queryDTO,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        Page<MaterialVO> page = materialService.getMaterialList(queryDTO, userId);
        return Result.success(page);
    }

    /**
     * 获取资料详情
     */
    @GetMapping("/{id}")
    public Result<MaterialVO> getMaterialDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        MaterialVO vo = materialService.getMaterialDetail(id, userId);
        return Result.success(vo);
    }

    /**
     * 记录浏览
     */
    @PostMapping("/{id}/view")
    public Result<Void> recordView(
            @PathVariable Long id,
            @RequestParam(required = false) Integer duration,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        materialService.recordView(id, userId, duration);
        return Result.success("记录成功", null);
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{id}/collect")
    public Result<Void> toggleCollect(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        materialService.toggleCollect(id, userId);
        return Result.success("操作成功", null);
    }

    /**
     * 评分
     */
    @PostMapping("/{id}/rate")
    public Result<Void> rateMaterial(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        materialService.rateMaterial(id, userId, rating);
        return Result.success("评分成功", null);
    }

    /**
     * 获取我的收藏
     */
    @GetMapping("/my-collections")
    public Result<Page<MaterialVO>> getMyCollections(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        Page<MaterialVO> page = materialService.getUserCollections(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 学生上传资料（需要审核）
     */
    @PostMapping("/upload")
    public Result<Void> uploadMaterial(
            @RequestBody MaterialVO materialVO,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        materialService.createMaterial(materialVO, 2); // 学生上传需要审核，状态为2
        return Result.success("上传成功，等待审核", null);
    }

    /**
     * 批量获取资料详情
     */
    @GetMapping("/batch/detail")
    public Result<java.util.List<MaterialVO>> getBatchMaterialDetails(
            @RequestParam java.util.List<Long> ids,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            String actualToken = token.replace("Bearer ", "");
            userId = jwtUtil.getUserIdFromToken(actualToken);
        }
        
        java.util.List<MaterialVO> vos = materialService.getBatchMaterialDetails(ids, userId);
        return Result.success(vos);
    }

    /**
     * 批量验证内容
     */
    @PostMapping("/validate/batch")
    public Result<java.util.Map<Long, java.util.List<String>>> batchValidateContent(
            @RequestBody java.util.List<com.learning.recommend.vo.MaterialVO> requests) {
        
        java.util.Map<Long, java.util.List<String>> validationErrors = materialService.batchValidateContent(requests);
        return Result.success(validationErrors);
    }
}
