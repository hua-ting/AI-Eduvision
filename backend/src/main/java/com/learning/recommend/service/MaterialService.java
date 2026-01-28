package com.learning.recommend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.dto.MaterialQueryDTO;
import com.learning.recommend.vo.MaterialVO;

import java.util.Map;

/**
 * 学习资料服务接口
 */
public interface MaterialService {
    
    /**
     * 分页查询资料列表
     */
    Page<MaterialVO> getMaterialList(MaterialQueryDTO queryDTO, Long userId);
    
    /**
     * 获取资料详情
     */
    MaterialVO getMaterialDetail(Long materialId, Long userId);
    
    /**
     * 获取资料详情（管理员）
     */
    MaterialVO getMaterialDetail(Long materialId);
    
    /**
     * 记录浏览行为
     */
    void recordView(Long materialId, Long userId, Integer duration);
    
    /**
     * 收藏/取消收藏
     */
    void toggleCollect(Long materialId, Long userId);
    
    /**
     * 评分
     */
    void rateMaterial(Long materialId, Long userId, Integer rating);
    
    /**
     * 获取用户收藏列表
     */
    Page<MaterialVO> getUserCollections(Long userId, Integer pageNum, Integer pageSize);
    
    // ========== 管理员功能 ==========
    
    /**
     * 管理员获取资料列表（包含所有状态）
     */
    Page<MaterialVO> getAdminMaterialList(MaterialQueryDTO queryDTO);
    
    /**
     * 更新资料状态
     */
    void updateMaterialStatus(Long materialId, Integer status);
    
    /**
     * 更新资料信息
     */
    void updateMaterialInfo(Long materialId, MaterialVO materialVO);
    
    /**
     * 删除资料
     */
    void deleteMaterial(Long materialId);
    
    /**
     * 批量删除资料
     */
    void batchDeleteMaterial(Long[] ids);
    
    /**
     * 批量更新状态
     */
    void batchUpdateStatus(Long[] ids, Integer status);
    
    /**
     * 获取资料统计信息
     */
    Map<String, Object> getMaterialStats();

    /**
     * 创建资料
     * @param materialVO 资料信息
     * @param status 状态（1-直接上架，2-待审核）
     */
    void createMaterial(MaterialVO materialVO, Integer status);
    
    /**
     * 批量获取资料详情
     */
    java.util.List<MaterialVO> getBatchMaterialDetails(java.util.List<Long> ids, Long userId);
    
    /**
     * 批量验证内容
     */
    java.util.Map<Long, java.util.List<String>> batchValidateContent(java.util.List<MaterialVO> requests);
}
