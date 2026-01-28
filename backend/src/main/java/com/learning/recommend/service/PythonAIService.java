package com.learning.recommend.service;

import java.util.Map;

/**
 * Python AI服务接口（Transformer模型）
 */
public interface PythonAIService {
    
    /**
     * 生成资料摘要
     * @param text 原文本
     * @return 摘要结果（包含summary、keyPoints、keywords）
     */
    Map<String, Object> generateSummary(String text);
}
