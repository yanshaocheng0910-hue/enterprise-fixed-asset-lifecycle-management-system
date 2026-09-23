package com.example.asset.ai.vo;

import lombok.Data;
import java.util.List;

/**
 * AI 分析报告 VO（增强版，兼容旧字段）
 */
@Data
public class AiReportVO {

    // ===== 旧字段（兼容前端原有读取） =====
    private String generatedAt;
    private String summary;
    private String anomalyOverview;
    private String suggestionOverview;

    // ===== 新增字段 =====

    /** 分析模式：DEEPSEEK / RULE_FALLBACK */
    private String analysisMode;

    /** AI 提供方 */
    private String provider;

    /** 调用的模型名称 */
    private String model;

    /** 主要风险分析 */
    private String keyRisks;

    /** 财务与折旧分析 */
    private String financialInsight;

    /** 运营管理建议 */
    private String operationInsight;

    /** 审计关注点 */
    private String auditFocus;

    /** 下一步处置建议 */
    private List<String> recommendations;

    /** 管理结论 */
    private String conclusion;

    /** Markdown 格式完整报告 */
    private String markdownReport;

    /** fallback 原因（仅 RULE_FALLBACK 模式有值） */
    private String fallbackReason;

    /** 模型原始输出文本 */
    private String rawText;
}
