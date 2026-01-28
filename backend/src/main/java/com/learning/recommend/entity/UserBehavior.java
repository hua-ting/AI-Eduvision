package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为实体
 */
@Data
@TableName("t_user_behavior")
public class UserBehavior implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long materialId;

    private Long knowledgePointId;

    private String behaviorType;

    private Integer duration;

    private Integer rating;

    private String device;

    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
