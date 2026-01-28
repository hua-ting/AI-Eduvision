package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点实体
 */
@Data
@TableName("t_knowledge_point")
public class KnowledgePoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String subCategory;

    private String description;

    private String content;

    private String tags;  // JSON格式

    private String difficulty;

    private Integer viewCount;

    private Integer collectCount;

    private Double avgRating;

    private Integer ratingCount;

    private Integer status;  // 0下架 1上架 2待审核

    private Long creatorId;

    private String relatedPoints;  // JSON格式，相关知识点ID数组

    private String prerequisites;  // JSON格式，前置知识点ID数组

    private String auditReason;  // 审核原因

    private Integer auditStatus;  // 审核状态：0-待审核，1-通过，2-拒绝

    private String pendingContent;  // 待审核的内容

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField
    private Long updatedBy;  // 更新者ID
}
