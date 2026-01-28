package com.learning.recommend.dto;

import lombok.Data;

/**
 * 问答请求DTO
 */
@Data
public class QAQuestionDTO {
    /**
     * 用户输入的问题或用于AI生成的提示词
     */
    private String question;

    /**
     * 来源标记：USER 用户在问答页面提问；DAILY_TOPIC 每日推荐/AI知识点创作内部调用
     */
    private String source;
}
