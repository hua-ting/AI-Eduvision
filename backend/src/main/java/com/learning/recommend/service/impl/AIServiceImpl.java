package com.learning.recommend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.learning.recommend.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

/**
 * AI服务实现（通义千问）
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${ai.qwen.api-key:}")
    private String apiKey;

    @Value("${ai.qwen.api-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String apiUrl;

    @Value("${ai.qwen.model:qwen-turbo}")
    private String model;

    @Value("${ai.qwen.enabled:false}")
    private boolean enabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String answerQuestion(String question) {
        // 兜底：空问题直接返回
        if (question == null || question.trim().isEmpty()) {
            return "请提供一个具体的学习问题，我会按要求为你生成条理清晰的知识点讲解。";
        }
        String q = question.trim();

        if (!enabled) {
            return "AI服务未启用，请在配置文件中启用AI服务";
        }

        if (apiKey == null || apiKey.isEmpty()) {
            return "AI服务API密钥未配置，请在配置文件中设置api-key";
        }

        if (apiKey.equals("your-qwen-api-key-here")) {
            return "AI服务使用的是默认API密钥，请配置真实的API密钥以使用AI功能";
        }

        try {
            // 简化提示词：正常人视角讲解知识点
            String prompt = String.format(
                    "请讲解：%s\n\n" +
                    "要求：\n" +
                    "1. 用 Markdown 格式，包含：核心概念、应用场景、常见问题\n" +
                    "2. 关键词用 ** 加粗，你自己判断是否需要代码示例\n" +
                    "3. 字数 300-800 字，不要过度修饰",
                    q
            );

            return callQwenAPI(prompt);
        } catch (Exception e) {
            log.error("AI问答失败", e);
            return "AI服务暂时不可用，请稍后重试";
        }
    }

    @Override
    public Map<String, Object> generateKnowledgePoint(String question, String answer) {
        if (!enabled || apiKey == null || apiKey.isEmpty() || apiKey.equals("your-qwen-api-key-here")) {
            // 临时实现：简单生成
            return createSimpleKnowledgePoint(question, answer);
        }

        try {
            String prompt = String.format(
                "基于以下问答内容，生成一个结构化的知识点，与上一次生成的不一样：\n" +
                "问题：%s\n" +
                "回答：%s\n\n" +
                "请以下面的JSON格式返回（只返回JSON，不要其他内容）：\n" +
                "{\n" +
                "  \"title\": \"知识点标题（30-50字，简明概括核心内容）\",\n" +
                "  \"category\": \"分类（从以下选择：算法、数据库、人工智能、前端开发、后端开发、计算机网络、操作系统、其他）\",\n" +
                "  \"subCategory\": \"子分类（更具体的领域，可选）\",\n" +
                "  \"description\": \"简要描述（80-120字，总结核心要点）\",\n" +
                "  \"tags\": [\"标签1\", \"标签2\", \"标签3\"],\n" +
                "  \"difficulty\": \"难度（从以下选择：初级、中级、高级）\"\n" +
                "}\n\n" +
                "注意事项：\n" +
                "1. 标题要精炼且具有吸引力\n" +
                "2. 分类要准确，不确定时选择'其他'\n" +
                "3. 描述要涵盖问题的核心要点\n" +
                "4. 标签选择最相关的3-5个关键词\n" +
                "5. 难度评估要符合实际技术水平",
                question, answer
            );
            
            String response = callQwenAPI(prompt);
            return parseKnowledgePointJSON(response);
        } catch (Exception e) {
            log.error("生成知识点失败", e);
            return createSimpleKnowledgePoint(question, answer);
        }
    }

    @Override
    public Map<String, Object> batchGenerateKnowledgePoints(String category, int count) {
        List<Map<String, Object>> knowledgePoints = new ArrayList<>();
        
        if (!enabled || apiKey == null || apiKey.isEmpty() || apiKey.equals("your-qwen-api-key-here")) {
            // 临时实现：返回示例数据
            for (int i = 0; i < Math.min(count, 5); i++) {
                knowledgePoints.add(createSampleKnowledgePoint(category, i + 1));
            }
        } else {
            try {
                String prompt = String.format(
                    "请生成%d个关于'%s'的面试题的知识点，返回JSON数组格式：\n" +
                    "[{\"title\":\"标题\",\"category\":\"%s\",\"subCategory\":\"子分类\",\"description\":\"描述\",\"content\":\"详细内容\",\"tags\":[\"标签\"],\"difficulty\":\"难度\"}]\n" +
                    "要求：1.内容专业准确 2.难度分布合理 3.覆盖不同子主题",
                    count, category, category
                );
                
                String response = callQwenAPI(prompt);
                knowledgePoints = parseKnowledgePointArray(response);
            } catch (Exception e) {
                log.error("批量生成知识点失败", e);
                for (int i = 0; i < Math.min(count, 5); i++) {
                    knowledgePoints.add(createSampleKnowledgePoint(category, i + 1));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("knowledgePoints", knowledgePoints);
        result.put("count", knowledgePoints.size());
        return result;
    }

    /**
     * 调用通义千问API
     */
    private String callQwenAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 使用DashScope标准格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.7);
        parameters.put("top_p", 0.95);
        parameters.put("max_tokens", 1000);
        requestBody.put("parameters", parameters);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                String responseBody = response.getBody();
                log.info("API响应内容: {}", responseBody);
                
                JsonNode root = objectMapper.readTree(responseBody);
                
                // 尝试多种响应格式
                // 格式1: DashScope标准格式 - output.text
                JsonNode outputNode = root.path("output");
                if (outputNode.isObject()) {
                    JsonNode textNode = outputNode.path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText();
                    }
                    
                    // 格式1b: DashScope格式 - output.choices
                    JsonNode choicesNode = outputNode.path("choices");
                    if (choicesNode.isArray() && choicesNode.size() > 0) {
                        JsonNode firstChoice = choicesNode.get(0);
                        JsonNode messageNode = firstChoice.path("message");
                        JsonNode contentNode = messageNode.path("content");
                        if (!contentNode.isMissingNode()) {
                            return contentNode.asText();
                        }
                    }
                }
                
                // 格式2: OpenAI兼容格式
                JsonNode choicesNode2 = root.path("choices");
                if (choicesNode2.isArray() && choicesNode2.size() > 0) {
                    JsonNode firstChoice2 = choicesNode2.get(0);
                    JsonNode messageNode2 = firstChoice2.path("message");
                    JsonNode contentNode2 = messageNode2.path("content");
                    if (!contentNode2.isMissingNode()) {
                        return contentNode2.asText();
                    }
                }
                
                // 格式3: 直接返回内容
                JsonNode contentNode3 = root.path("content");
                if (!contentNode3.isMissingNode()) {
                    return contentNode3.asText();
                }
                
                throw new RuntimeException("无法解析API响应格式，请检查响应内容: " + responseBody);
            } catch (Exception e) {
                log.error("解析API响应失败", e);
                log.error("响应内容: {}", response.getBody());
                throw new RuntimeException("AI响应解析失败: " + e.getMessage());
            }
        } else {
            log.error("API调用失败，状态码: {}", response.getStatusCode());
            log.error("响应内容: {}", response.getBody());
            throw new RuntimeException("AI调用失败，状态码: " + response.getStatusCode());
        }
    }

    /**
     * 简单生成知识点（临时实现）
     */
    private Map<String, Object> createSimpleKnowledgePoint(String question, String answer) {
        Map<String, Object> kp = new HashMap<>();
        kp.put("title", question.length() > 50 ? question.substring(0, 47) + "..." : question);
        kp.put("category", "计算机");
        kp.put("subCategory", "其他");
        kp.put("description", answer.length() > 100 ? answer.substring(0, 97) + "..." : answer);
        kp.put("tags", Arrays.asList("学习", "知识点"));
        kp.put("difficulty", "中级");
        return kp;
    }

    /**
     * 创建示例知识点
     */
    private Map<String, Object> createSampleKnowledgePoint(String category, int index) {
        Map<String, Object> kp = new HashMap<>();
        kp.put("title", category + "知识点示例" + index);
        kp.put("category", category);
        kp.put("subCategory", "基础");
        kp.put("description", "这是一个关于" + category + "的知识点示例");
        kp.put("content", "详细内容：包含核心概念、应用场景、示例代码等");
        kp.put("tags", Arrays.asList(category, "基础", "示例"));
        kp.put("difficulty", index % 3 == 0 ? "初级" : index % 3 == 1 ? "中级" : "高级");
        return kp;
    }

    /**
     * 解析知识点JSON
     */
    private Map<String, Object> parseKnowledgePointJSON(String jsonStr) {
        try {
            // 提取JSON部分
            int start = jsonStr.indexOf("{");
            int end = jsonStr.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end);
            }
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (Exception e) {
            log.error("解析JSON失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 解析知识点数组
     */
    private List<Map<String, Object>> parseKnowledgePointArray(String jsonStr) {
        try {
            int start = jsonStr.indexOf("[");
            int end = jsonStr.lastIndexOf("]") + 1;
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end);
            }
            return objectMapper.readValue(jsonStr, List.class);
        } catch (Exception e) {
            log.error("解析JSON数组失败", e);
            return new ArrayList<>();
        }
    }
}
