package com.learning.recommend.service;

import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 回答问题
     * @param question 问题
     * @return 回答内容
     */
    String answerQuestion(String question);
    
    /**
     * 从问答生成知识点
     * @param question 问题
     * @param answer 回答
     * @return 知识点信息（title, category, tags等）
     */
    Map<String, Object> generateKnowledgePoint(String question, String answer);
    
    /**
     * 批量生成知识点（用于一键填充）
     * @param category 分类
     * @param count 生成数量
     * @return 知识点列表
     */
    Map<String, Object> batchGenerateKnowledgePoints(String category, int count);
}
