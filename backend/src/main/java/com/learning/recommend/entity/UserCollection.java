package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏实体
 */
@Data
@TableName("t_user_collection")
public class UserCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long materialId;

    private Long knowledgePointId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
