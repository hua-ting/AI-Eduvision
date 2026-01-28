package com.learning.recommend.dto;

import lombok.Data;

import java.util.List;

/**
 * 知识点查询DTO
 */
@Data
public class KnowledgePointQueryDTO {
    
    private String keyword;  // 关键词搜索
    
    private String category;  // 分类筛选
    
    private String subCategory;  // 子分类
    
    private String difficulty;  // 难度
    
    private List<String> tags;  // 标签筛选
    
    private Integer status;  // 状态：0下架 1上架 2待审核
    
    private String orderBy;  // 排序字段: createTime/viewCount/avgRating
    
    private String orderType;  // 排序方式: asc/desc
    
    private Integer pageNum = 1;  // 当前页
    
    private Integer pageSize = 20;  // 每页大小
}
