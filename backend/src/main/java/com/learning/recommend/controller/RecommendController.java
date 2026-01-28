package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.service.RecommendService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.MaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐控制器
 */
@Api(tags = "智能推荐")
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取个性化推荐
     */
    @ApiOperation("获取个性化推荐")
    @GetMapping("/personalized")
    public Result<List<MaterialVO>> getPersonalizedRecommendations(
            @ApiParam("推荐数量") @RequestParam(defaultValue = "10") Integer limit,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        List<MaterialVO> recommendations = recommendService.getPersonalizedRecommendations(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 获取热门推荐
     */
    @ApiOperation("获取热门推荐")
    @GetMapping("/hot")
    public Result<List<MaterialVO>> getHotRecommendations(
            @ApiParam("推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<MaterialVO> recommendations = recommendService.getHotRecommendations(limit);
        return Result.success(recommendations);
    }

    /**
     * 获取相似资料推荐
     */
    @ApiOperation("获取相似资料")
    @GetMapping("/similar/{materialId}")
    public Result<List<MaterialVO>> getSimilarMaterials(
            @ApiParam("资料ID") @PathVariable Long materialId,
            @ApiParam("推荐数量") @RequestParam(defaultValue = "6") Integer limit) {
        List<MaterialVO> recommendations = recommendService.getSimilarMaterials(materialId, limit);
        return Result.success(recommendations);
    }
}
