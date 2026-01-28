package com.learning.recommend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资料VO
 */
@Data
public class MaterialVO {
    private Long id;
    private String title;
    private String category;
    private String subCategory;
    private String description;
    private String coverUrl;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private List<String> tags;
    private String difficulty;
    private Integer viewCount;
    private Integer collectCount;
    private Integer downloadCount;
    private Double avgRating;
    private Integer ratingCount;
    private LocalDateTime createTime;
    
    // 摘要信息
    private String summaryText;
    private List<String> keyPoints;
    private List<String> keywords;
    
    // 用户相关
    private Boolean isCollected;  // 当前用户是否收藏
    private Integer userRating;   // 当前用户评分
}
