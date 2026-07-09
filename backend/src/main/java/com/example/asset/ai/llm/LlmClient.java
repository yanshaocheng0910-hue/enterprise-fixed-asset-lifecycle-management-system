package com.example.asset.ai.llm;

/**
 * LLM 客户端接口
 */
public interface LlmClient {

    /**
     * 调用大模型对话接口
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 模型响应
     * @throws LlmException 调用失败时抛出
     */
    LlmResponse chat(String systemPrompt, String userPrompt);
}
