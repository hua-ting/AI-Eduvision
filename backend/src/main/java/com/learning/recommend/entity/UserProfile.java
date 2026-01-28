package com.learning.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户画像实体
 */
@Data
@TableName("t_user_profile")
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String learningLevel;  // 初级/中级/高级

    private String interestTags;  // JSON格式: ["算法", "数据库"]

    private String activeTime;  // JSON格式: 活跃时段统计

    private Integer learningDuration;  // 累计学习时长(分钟)

    private String favoriteCategory;  // 最喜欢的分类

    private String profileVector;  // JSON格式: 用户画像向量
    
    // 新增：问答相关
    private String qaTopics;  // JSON格式: 问答主题分布 {"algorithm": 5, "database": 3}
    
    // 新增：知识点偏好
    private String knowledgePreferences;  // JSON格式: 知识点分类偏好 {"algorithm": 0.8, "database": 0.6}
    
    // 新增：统计信息
    private Integer totalQaCount;  // 总问答次数
    
    private Integer totalKnowledgeViews;  // 知识点浏览次数
    
    private Integer totalKnowledgeCollects;  // 知识点收藏次数

    private LocalDateTime lastActiveTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
