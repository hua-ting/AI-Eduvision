package com.learning.recommend.dto;

import lombok.Data;

/**
 * 资料采集搜索DTO
 */
@Data
public class CrawlerSearchDTO {
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 资料源类型（csdn, runoob, juejin等）
     */
    private String sourceType;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
