package com.learning.recommend.vo;

import com.learning.recommend.entity.KnowledgePoint;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识点VO
 */
@Data
public class KnowledgePointVO {
    private Long id;
    private String title;
    private String category;
    private String subCategory;
    private String description;
    private String content;
    private List<String> tags;
    private String difficulty;
    private Integer viewCount;
    private Integer collectCount;
    private Double avgRating;
    private Integer ratingCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long updatedBy;
    private Integer status;
    private Integer auditStatus;  // 审核状态：0-待审核，1-通过，2-拒绝
    private String pendingContent;  // 待审核的内容
    private String auditReason;  // 审核意见/拒绝理由
    
    // 用户相关
    private Boolean isCollected;  // 当前用户是否收藏
    private Integer userRating;   // 当前用户评分
    
    // 关联信息
    private List<String> relatedPoints;  // 相关知识点
    private List<String> prerequisites;  // 前置知识
    
    /**
     * 从实体类转换为VO
     */
    public static KnowledgePointVO fromEntity(KnowledgePoint entity) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setCategory(entity.getCategory());
        vo.setSubCategory(entity.getSubCategory());
        vo.setDescription(entity.getDescription());
        vo.setContent(entity.getContent());
        vo.setDifficulty(entity.getDifficulty());
        vo.setViewCount(entity.getViewCount());
        vo.setCollectCount(entity.getCollectCount());
        vo.setAvgRating(entity.getAvgRating());
        vo.setRatingCount(entity.getRatingCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setStatus(entity.getStatus());
        vo.setAuditStatus(entity.getAuditStatus());
        vo.setPendingContent(entity.getPendingContent());
        vo.setAuditReason(entity.getAuditReason());
        return vo;
    }
    
    /**
     * 转换为实体类
     */
    public KnowledgePoint toEntity() {
        KnowledgePoint entity = new KnowledgePoint();
        entity.setId(this.getId());
        entity.setTitle(this.getTitle());
        entity.setCategory(this.getCategory());
        entity.setSubCategory(this.getSubCategory());
        entity.setDescription(this.getDescription());
        entity.setContent(this.getContent());
        entity.setDifficulty(this.getDifficulty());
        entity.setViewCount(this.getViewCount());
        entity.setCollectCount(this.getCollectCount());
        entity.setAvgRating(this.getAvgRating());
        entity.setRatingCount(this.getRatingCount());
        entity.setCreateTime(this.getCreateTime());
        entity.setUpdateTime(this.getUpdateTime());
        entity.setUpdatedBy(this.getUpdatedBy());
        entity.setStatus(this.getStatus());
        entity.setAuditStatus(this.getAuditStatus());
        entity.setPendingContent(this.getPendingContent());
        entity.setAuditReason(this.getAuditReason());
        return entity;
    }
}
