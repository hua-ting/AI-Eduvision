package com.learning.recommend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.vo.QARecordVO;
import java.util.Map;

/**
 * 问答服务接口
 */
public interface QAService {
    
    /**
     * 提问并获取AI回答
     * @param userId 用户ID
     * @param question 问题
     * @param source 来源标记：USER 用户提问；DAILY_TOPIC 每日推荐/AI知识点创作
     * @return 回答信息
     */
    Map<String, Object> askQuestion(Long userId, String question, String source);
    
    /**
     * 从问答记录一键生成知识点
     * @param qaId 问答记录ID
     * @param userId 用户ID
     * @return 生成的知识点ID
     */
    Long generateKnowledgePoint(Long qaId, Long userId);
    
    /**
     * 获取用户问答历史
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页问答记录
     */
    Page<QARecordVO> getUserQAHistory(Long userId, Integer pageNum, Integer pageSize);
}
