package com.example.asset.ai.llm;

import com.example.asset.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 大模型客户端（OpenAI 兼容 API）
 */
@Component
public class DeepSeekLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekLlmClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AiProperties aiProperties;

    public DeepSeekLlmClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public LlmResponse chat(String systemPrompt, String userPrompt) {
        if (!aiProperties.isEnabled() || aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new LlmException("AI 未启用或未配置 API Key");
        }

        String url = aiProperties.getApiBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        log.info("调用 DeepSeek API, provider={}, model={}", aiProperties.getProvider(), aiProperties.getModel());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiProperties.getModel());
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
                    .build();

            ResponseEntity<String> responseEntity = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);

            String responseBody = responseEntity.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                throw new LlmException("DeepSeek 返回空响应", responseEntity.getStatusCode().value());
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new LlmException("DeepSeek 返回无 choices", responseEntity.getStatusCode().value());
            }

            String content = choices.get(0).path("message").path("content").asText("");
            String model = root.path("model").asText(aiProperties.getModel());

            LlmResponse llmResponse = new LlmResponse(content, model);
            JsonNode usage = root.path("usage");
            if (!usage.isMissingNode()) {
                llmResponse.setPromptTokens(usage.path("prompt_tokens").asInt(0));
                llmResponse.setCompletionTokens(usage.path("completion_tokens").asInt(0));
            }

            log.info("DeepSeek 调用成功, model={}, completionTokens={}", model, llmResponse.getCompletionTokens());
            return llmResponse;

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("401")) {
                throw new LlmException("DeepSeek API Key 无效（401）", 401);
            }
            if (msg != null && msg.contains("429")) {
                throw new LlmException("DeepSeek API 请求频率超限（429）", 429);
            }
            if (msg != null && msg.contains("500")) {
                throw new LlmException("DeepSeek 服务端异常（500）", 500);
            }
            log.warn("DeepSeek 调用失败: {}", msg);
            throw new LlmException("DeepSeek 调用失败: " + (msg != null ? msg : "未知错误"), e);
        }
    }
}
