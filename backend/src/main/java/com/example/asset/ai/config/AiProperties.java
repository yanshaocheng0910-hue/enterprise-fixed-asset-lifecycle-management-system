package com.example.asset.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用 AI 分析 */
    private boolean enabled = true;

    /** AI 提供方 */
    private String provider = "deepseek";

    /** API 基础地址 */
    private String apiBaseUrl = "https://api.deepseek.com";

    /** API Key（仅从环境变量注入，不硬编码） */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "deepseek-v4-flash";

    /** 调用超时（秒） */
    private int timeoutSeconds = 30;

    /** 是否允许 fallback 到规则引擎 */
    private boolean fallbackEnabled = true;
}
