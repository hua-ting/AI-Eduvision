package com.learning.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.entity.QARecord;
import com.learning.recommend.mapper.KnowledgePointMapper;
import com.learning.recommend.mapper.QARecordMapper;
import com.learning.recommend.service.AIService;
import com.learning.recommend.service.QAService;
import com.learning.recommend.service.UserProfileService;
import com.learning.recommend.vo.QARecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问答服务实现
 */
@Service
@Slf4j
public class QAServiceImpl implements QAService {

    @Autowired
    private QARecordMapper qaRecordMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private AIService aiService;
    
    @Autowired(required = false)
    private UserProfileService userProfileService;

    @Override
    public Map<String, Object> askQuestion(Long userId, String question, String source) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 调用AI服务回答问题
            String answer = aiService.answerQuestion(question);
            
            boolean isInternal = source != null && "DAILY_TOPIC".equalsIgnoreCase(source);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("question", question);
            result.put("answer", answer);
            result.put("modelName", "qwen-turbo");
            result.put("duration", (int)(System.currentTimeMillis() - startTime));
            
            if (isInternal) {
                // 每日推荐/AI知识点创作等内部调用：不记录问答历史、不更新画像
                return result;
            }
            
            // 保存问答记录（用户主动提问）
            QARecord record = new QARecord();
            record.setUserId(userId);
            record.setQuestion(question);
            record.setAnswer(answer);
            record.setModelName("qwen-turbo");
            record.setDuration((int)(System.currentTimeMillis() - startTime));
            // 用户主动提问的记录，generatedKpId保持为null
            qaRecordMapper.insert(record);
            
            // 填充记录ID
            result.put("id", record.getId());
            
            // 异步更新用户画像 - 记录问答行为
            if (userProfileService != null) {
                // 简单判断主题，实际应该用NLP分类
                String category = extractCategoryFromQuestion(question);
                userProfileService.recordQABehavior(userId, question, category);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("AI问答失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI问答服务暂时不可用，请稍后重试");
        }
    }

/**
 * 根据问答记录生成知识点
 * @param qaId 问答记录ID
 * @param userId 用户ID
 * @return 生成知识点的ID
 * @throws RuntimeException 当问答记录不存在或生成知识点失败时抛出
 */
    @Override
    public Long generateKnowledgePoint(Long qaId, Long userId) {
        // 查询问答记录
        QARecord qaRecord = qaRecordMapper.selectById(qaId);
    // 验证问答记录是否存在且属于当前用户
        if (qaRecord == null || !qaRecord.getUserId().equals(userId)) {
            throw new RuntimeException("问答记录不存在");
        }
        
        try {
            // 调用AI服务生成知识点
            Map<String, Object> kpData = aiService.generateKnowledgePoint(
                qaRecord.getQuestion(),   // 传入问题内容
                qaRecord.getAnswer()     // 传入回答内容
            );
            
            // 创建知识点（待审核状态）
            KnowledgePoint kp = new KnowledgePoint();
            kp.setTitle((String) kpData.get("title"));
            kp.setCategory((String) kpData.getOrDefault("category", "其他"));
            kp.setSubCategory((String) kpData.get("subCategory"));
            kp.setDescription((String) kpData.get("description"));
            kp.setContent(qaRecord.getAnswer());  // 使用AI回答作为内容
            
            // 正确转换tags为JSON字符串
            Object tagsObj = kpData.get("tags");
            if (tagsObj != null) {
                kp.setTags(JSON.toJSONString(tagsObj));
            } else {
                kp.setTags(JSON.toJSONString(new String[]{"AI生成"}));
            }
            
            kp.setDifficulty((String) kpData.getOrDefault("difficulty", "中级"));
            kp.setStatus(2);  // 待审核
            kp.setCreatorId(userId);
            kp.setViewCount(0);
            kp.setCollectCount(0);
            kp.setAvgRating(0.0);
            kp.setRatingCount(0);
            
            knowledgePointMapper.insert(kp);
            
            // 更新问答记录关联，防止用户看到自己生成知识点的记录直接不插入
//            qaRecord.setGeneratedKpId(kp.getId());
//            qaRecordMapper.updateById(qaRecord);
            
            return kp.getId();
            
        } catch (Exception e) {
            log.error("生成知识点失败: {}", e.getMessage(), e);
            throw new RuntimeException("知识点生成失败，请稍后重试");
        }
    }

    @Override
    public Page<QARecordVO> getUserQAHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<QARecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<QARecord> wrapper = new QueryWrapper<>();
        // 只查询用户主动提问的记录，不包含系统内部调用的记录
        // 通过generatedKpId是否为null来区分，用户提问的记录该字段为null
        wrapper.eq("user_id", userId)
                .isNull("generated_kp_id")  // 用户主动提问的记录generatedKpId为null
                .orderByDesc("create_time");
        
        Page<QARecord> recordPage = qaRecordMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<QARecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<QARecordVO> voList = recordPage.getRecords().stream().map(record -> {
            QARecordVO vo = new QARecordVO();
            BeanUtils.copyProperties(record, vo);
            
            // 如果关联了知识点，查询标题
            if (record.getGeneratedKpId() != null) {
                KnowledgePoint kp = knowledgePointMapper.selectById(record.getGeneratedKpId());
                if (kp != null) {
                    vo.setGeneratedKpTitle(kp.getTitle());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    /**
     * 从问题中提取分类(简单关键词匹配)
     */
    private String extractCategoryFromQuestion(String question) {
        if (question == null) return "其他";
        
        String lowerQuestion = question.toLowerCase();
        if (lowerQuestion.contains("算法") || lowerQuestion.contains("排序") || lowerQuestion.contains("algorithm")) {
            return "算法";
        } else if (lowerQuestion.contains("数据库") || lowerQuestion.contains("sql") || lowerQuestion.contains("mysql")) {
            return "数据库";
        } else if (lowerQuestion.contains("java") || lowerQuestion.contains("python") || lowerQuestion.contains("编程")) {
            return "编程语言";
        } else if (lowerQuestion.contains("网络") || lowerQuestion.contains("http") || lowerQuestion.contains("tcp")) {
            return "计算机网络";
        } else if (lowerQuestion.contains("ai") || lowerQuestion.contains("人工智能") || lowerQuestion.contains("机器学习")) {
            return "人工智能";
        }
        return "其他";
    }
}
