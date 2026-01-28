package com.learning.recommend.service.impl;

import com.learning.recommend.service.PythonAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Python AI服务实现，调用 FastAPI 摘要接口
 */
@Service
@Slf4j
public class PythonAIServiceImpl implements PythonAIService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${ai.python.enabled:false}")
    private boolean enabled;

    @Value("${ai.python.service-url:http://localhost:5000}")
    private String serviceUrl;

    @Value("${ai.python.timeout:30000}")
    private int timeout;

    @Override
    public Map<String, Object> generateSummary(String text) {
        Map<String, Object> result = new HashMap<>();
        if (!enabled || text == null || text.length() < 50) {
            return result;
        }
        try {
            String url = serviceUrl + "/api/summarize";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("text", text);
            body.put("max_length", 200);
            body.put("min_length", 80);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                result.put("summary", data.get("summary"));
                result.put("keyPoints", data.get("key_points"));
                result.put("keywords", data.get("keywords"));
            }
        } catch (Exception e) {
            log.error("调用Python AI摘要服务失败", e);
        }
        return result;
    }
}
