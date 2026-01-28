package com.learning.recommend.vo;

import lombok.Data;

/**
 * 爬虫结果VO
 */
@Data
public class CrawlerResultVO {
    /**
     * 标题
     */
    private String title;
    
    /**
     * 原文URL
     */
    private String url;
    
    /**
     * 描述/摘要
     */
    private String description;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 来源网站
     */
    private String source;
    
    /**
     * 分类（自动推断）
     */
    private String category;
    
    /**
     * 难度（自动推断）
     */
    private String difficulty;
    
    /**
     * 发布时间
     */
    private String publishTime;
}
