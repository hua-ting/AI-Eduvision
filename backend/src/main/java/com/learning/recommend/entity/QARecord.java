package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问答记录实体
 */
@Data
@TableName("t_qa_record")
public class QARecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String answer;

    private Long generatedKpId;  // 生成的知识点ID

    private String modelName;

    private Integer duration;  // 耗时(毫秒)

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
