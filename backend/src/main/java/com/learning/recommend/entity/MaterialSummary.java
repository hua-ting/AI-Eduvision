package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资料摘要实体
 */
@Data
@TableName("t_material_summary")
public class MaterialSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private String summaryText;

    private String summaryType;

    private String keyPoints;  // JSON格式

    private String keywords;   // JSON格式

    private String modelName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime generateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
