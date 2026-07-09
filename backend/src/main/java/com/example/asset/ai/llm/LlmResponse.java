package com.example.asset.ai.llm;

import lombok.Data;

/**
 * LLM 响应封装
 */
@Data
public class LlmResponse {

    /** 模型返回的文本内容 */
    private String content;

    /** 调用的模型名称 */
    private String model;

    /** token 用量信息（可选） */
    private Integer promptTokens;
    private Integer completionTokens;

    public LlmResponse() {}

    public LlmResponse(String content, String model) {
        this.content = content;
        this.model = model;
    }
}
