package com.learning.recommend.vo;

/**
 * 主题数据传输对象
 */
public class TopicVO {
    
    private String title;
    private String description;
    private String category;
    private String difficulty;
    
    public TopicVO() {}
    
    public TopicVO(String title, String description, String category, String difficulty) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
    }
    
    // getter和setter方法
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}