package com.learning.recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Result;
import com.learning.recommend.dto.QAQuestionDTO;
import com.learning.recommend.service.QAService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.QARecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 问答控制器
 */
@RestController
@RequestMapping("/api/qa")
@Api(tags = "问答模块")
@Slf4j
public class QAController {

    @Autowired
    private QAService qaService;

    @PostMapping("/ask")
    @ApiOperation("提问")
    public Result<Map<String, Object>> askQuestion(@RequestBody QAQuestionDTO dto, HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            Map<String, Object> result = qaService.askQuestion(userId, dto.getQuestion(), dto.getSource());
            return Result.success(result);
        } catch (Exception e) {
            log.error("提问失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/generate/{qaId}")
    @ApiOperation("从问答生成知识点")
    public Result<Long> generateKnowledgePoint(@PathVariable Long qaId, HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            Long kpId = qaService.generateKnowledgePoint(qaId, userId);
            return Result.success(kpId);
        } catch (Exception e) {
            log.error("生成知识点失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/history")
    @ApiOperation("获取问答历史")
    public Result<Page<QARecordVO>> getHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Long userId = JwtUtil.getUserId(request);
            Page<QARecordVO> page = qaService.getUserQAHistory(userId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取历史失败", e);
            return Result.error(e.getMessage());
        }
    }
}
