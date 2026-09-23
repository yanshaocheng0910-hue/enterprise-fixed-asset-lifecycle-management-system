package com.example.asset.ai.llm;

/**
 * LLM 调用异常
 */
public class LlmException extends RuntimeException {

    /** HTTP 状态码（网络异常时为 -1） */
    private final int statusCode;

    public LlmException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public LlmException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
