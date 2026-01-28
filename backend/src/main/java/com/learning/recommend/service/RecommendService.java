package com.learning.recommend.service;

import com.learning.recommend.vo.MaterialVO;

import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendService {
    
    /**
     * 获取个性化推荐资料
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐资料列表
     */
    List<MaterialVO> getPersonalizedRecommendations(Long userId, Integer limit);
    
    /**
     * 获取热门推荐
     * @param limit 推荐数量
     * @return 热门资料列表
     */
    List<MaterialVO> getHotRecommendations(Integer limit);
    
    /**
     * 获取相似资料推荐
     * @param materialId 资料ID
     * @param limit 推荐数量
     * @return 相似资料列表
     */
    List<MaterialVO> getSimilarMaterials(Long materialId, Integer limit);
}
