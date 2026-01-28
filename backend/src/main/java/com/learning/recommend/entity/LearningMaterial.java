package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学习资料实体
 */
@Data
@TableName("t_material")
public class LearningMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String subCategory;

    private String description;

    private String coverUrl;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private String tags;  // JSON格式

    private String difficulty;

    private Integer viewCount;

    private Integer collectCount;

    private Integer downloadCount;

    private Double avgRating;

    private Integer ratingCount;

    private Integer status;  // 0下架 1上架 2待审核

    private Long uploaderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
