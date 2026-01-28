package com.learning.recommend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.dto.KnowledgePointQueryDTO;
import com.learning.recommend.vo.KnowledgePointVO;

import java.util.List;
import java.util.Map;

/**
 * 知识点服务接口
 */
public interface KnowledgePointService {
    
    /**
     * 分页查询知识点列表
     */
    Page<KnowledgePointVO> getKnowledgePointList(KnowledgePointQueryDTO queryDTO, Long userId);
    
    /**
     * 获取知识点详情
     */
    KnowledgePointVO getKnowledgePointDetail(Long id, Long userId);
    
    /**
     * 获取知识点详情（管理员）
     */
    KnowledgePointVO getKnowledgePointDetail(Long id);
    
    /**
     * 记录浏览行为
     */
    void recordView(Long id, Long userId, Integer duration);
    
    /**
     * 收藏/取消收藏
     */
    void toggleCollect(Long id, Long userId);
    
    /**
     * 评分
     */
    void rateKnowledgePoint(Long id, Long userId, Integer rating);
    
    /**
     * 获取用户收藏列表
     */
    Page<KnowledgePointVO> getUserCollections(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 创建知识点
     */
    void createKnowledgePoint(KnowledgePointVO knowledgePointVO, Long userId);
    
    // ========== 管理员功能 ==========
    
    /**
     * 管理员获取知识点列表（包含所有状态）
     */
    Page<KnowledgePointVO> getAdminKnowledgePointList(KnowledgePointQueryDTO queryDTO);
    
    /**
     * 审核知识点
     */
    void auditKnowledgePoint(Long id, Integer status, String reason);
    
    /**
     * 更新知识点状态
     */
    void updateKnowledgePointStatus(Long id, Integer status);
    
    /**
     * 删除知识点
     */
    void deleteKnowledgePoint(Long id);
    
    /**
     * 批量删除知识点
     */
    void batchDeleteKnowledgePoint(Long[] ids);
    
    /**
     * 批量更新状态
     */
    void batchUpdateStatus(Long[] ids, Integer status);
    
    /**
     * 更新知识点信息
     */
    void updateKnowledgePointInfo(Long id, KnowledgePointVO knowledgePointVO);
    
    /**
     * 获取知识点统计信息
     */
    Map<String, Object> getKnowledgePointStats();
    
    /**
     * 批量设置用户相关属性（收藏状态、评分等）
     */
    void batchSetUserAttributes(List<KnowledgePointVO> vos, Long userId);
    
    /**
     * 批量获取知识点详情
     */
    List<KnowledgePointVO> getBatchKnowledgePointDetails(List<Long> ids, Long userId);
    
    /**
     * 批量更新知识点
     */
    void batchUpdateKnowledgePoints(List<KnowledgePointVO> knowledgePoints, Long userId);
    
    /**
     * 批量验证内容
     */
    Map<Long, List<String>> batchValidateContent(List<KnowledgePointVO> requests);

    /**
     * 更新知识点内容
     */
    void updateKnowledgePointContent(Long id, String content, Long userId);

    /**
     * 提交内容审核
     */
    void submitContentReview(Long contentId, String content, Long userId);
    
    /**
     * 获取待审核的知识点列表
     */
    Page<KnowledgePointVO> getPendingReviewKnowledgePoints(Integer pageNum, Integer pageSize);
    
    /**
     * 审核知识点修改申请
     */
    void reviewKnowledgePointContent(Long id, Integer status, String reason, Long reviewerId);
}
