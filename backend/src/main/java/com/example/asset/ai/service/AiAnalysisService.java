package com.example.asset.ai.service;

import com.example.asset.ai.config.AiProperties;
import com.example.asset.ai.llm.LlmClient;
import com.example.asset.ai.llm.LlmException;
import com.example.asset.ai.llm.LlmResponse;
import com.example.asset.ai.mapper.AiAnalysisMapper;
import com.example.asset.ai.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> STATUS_LABELS = new LinkedHashMap<>();

    static {
        STATUS_LABELS.put("IN_USE", "使用中");
        STATUS_LABELS.put("IDLE", "闲置");
        STATUS_LABELS.put("REPAIRING", "维修中");
        STATUS_LABELS.put("WAITING_SCRAP", "待报废");
        STATUS_LABELS.put("SCRAPPED", "已报废");
        STATUS_LABELS.put("INVENTORY_ABNORMAL", "盘点异常");
    }

    private final AiAnalysisMapper aiAnalysisMapper;
    private final LlmClient llmClient;
    private final AiProperties aiProperties;

    public AiAnalysisService(AiAnalysisMapper aiAnalysisMapper, LlmClient llmClient, AiProperties aiProperties) {
        this.aiAnalysisMapper = aiAnalysisMapper;
        this.llmClient = llmClient;
        this.aiProperties = aiProperties;
    }

    // ===== 原有接口（保持不变） =====

    public AiSummaryVO getSummary() {
        AiSummaryVO vo = new AiSummaryVO();
        vo.setTotalCount(aiAnalysisMapper.selectTotalAssetCount());
        vo.setTotalOriginalValue(aiAnalysisMapper.selectTotalOriginalValue());
        vo.setTotalNetValue(aiAnalysisMapper.selectTotalNetValue());

        List<Map<String, Object>> rows = aiAnalysisMapper.selectStatusDistribution();
        List<AiSummaryVO.StatusItem> items = rows.stream().map(r -> {
            AiSummaryVO.StatusItem s = new AiSummaryVO.StatusItem();
            String status = (String) r.get("status");
            s.setStatus(status);
            s.setStatusLabel(STATUS_LABELS.getOrDefault(status, status));
            s.setCount(((Number) r.get("cnt")).longValue());
            s.setTotalValue((BigDecimal) r.get("net_value"));
            return s;
        }).collect(Collectors.toList());
        vo.setStatusDistribution(items);
        return vo;
    }

    public AiAlertVO getAlerts() {
        AiAlertVO vo = new AiAlertVO();
        vo.setIdleAlerts(buildAlerts(aiAnalysisMapper.selectIdleAssets(),
                "长期闲置超过 1 年，建议确认资产状态并处理", "medium"));
        vo.setFrequentRepairAlerts(buildAlerts(aiAnalysisMapper.selectFrequentRepairAssets(),
                null, null));
        vo.setAbnormalStatusAlerts(buildAlerts(aiAnalysisMapper.selectAbnormalAssets(),
                "资产状态为盘点异常，请核实资产实际情况", "high"));
        return vo;
    }

    private List<AiAlertVO.AlertItem> buildAlerts(List<Map<String, Object>> rows, String defaultReason, String defaultSeverity) {
        return rows.stream().map(r -> {
            AiAlertVO.AlertItem a = new AiAlertVO.AlertItem();
            a.setAssetId(((Number) r.get("id")).longValue());
            a.setAssetCode((String) r.get("asset_code"));
            a.setAssetName((String) r.get("asset_name"));
            a.setDepartment((String) r.get("department"));
            a.setKeeper((String) r.get("keeper"));
            if (defaultReason != null) {
                a.setAlertReason(defaultReason);
                a.setSeverity(defaultSeverity);
            } else {
                int count = ((Number) r.get("repair_count")).intValue();
                a.setAlertReason("维修次数已达 " + count + " 次，建议评估是否需报废更换");
                a.setSeverity(count >= 5 ? "high" : "medium");
            }
            return a;
        }).collect(Collectors.toList());
    }

    public AiSuggestionVO getSuggestions() {
        AiSuggestionVO vo = new AiSuggestionVO();

        List<Map<String, Object>> repairRows = aiAnalysisMapper.selectRepairSuggestions();
        vo.setRepairSuggestions(repairRows.stream().map(r -> {
            AiSuggestionVO.SuggestionItem s = new AiSuggestionVO.SuggestionItem();
            s.setAssetId(((Number) r.get("id")).longValue());
            s.setAssetCode((String) r.get("asset_code"));
            s.setAssetName((String) r.get("asset_name"));
            s.setDepartment((String) r.get("department"));
            s.setUsefulLife(r.get("useful_life") != null ? ((Number) r.get("useful_life")).intValue() : null);
            s.setNetValue((BigDecimal) r.get("net_value"));
            s.setRepairCount(((Number) r.get("repair_count")).intValue());
            s.setSuggestion("建议安排维修检测");
            s.setReason("该资产已维修 " + s.getRepairCount() + " 次，继续使用存在故障风险");
            return s;
        }).collect(Collectors.toList()));

        List<Map<String, Object>> scrapRows = aiAnalysisMapper.selectScrapSuggestions();
        vo.setScrapSuggestions(scrapRows.stream().map(r -> {
            AiSuggestionVO.SuggestionItem s = new AiSuggestionVO.SuggestionItem();
            s.setAssetId(((Number) r.get("id")).longValue());
            s.setAssetCode((String) r.get("asset_code"));
            s.setAssetName((String) r.get("asset_name"));
            s.setDepartment((String) r.get("department") != null ? r.get("department").toString() : null);
            s.setUsefulLife(r.get("useful_life") != null ? ((Number) r.get("useful_life")).intValue() : null);
            s.setNetValue((BigDecimal) r.get("net_value"));
            s.setRepairCount(0);
            s.setSuggestion("建议启动报废流程");
            String reason = "该资产净值已低于原值 5%";
            if (s.getUsefulLife() != null && s.getUsefulLife() > 0) {
                reason += "或已超过使用年限 " + s.getUsefulLife() + " 年";
            }
            s.setReason(reason);
            return s;
        }).collect(Collectors.toList()));

        return vo;
    }

    // ===== 报告生成（三层架构） =====

    /**
     * 报告生成入口：优先 DeepSeek，失败 fallback 到规则引擎
     */
    public AiReportVO getReport() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 尝试 DeepSeek 生成
        if (aiProperties.isEnabled() && aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            try {
                AiReportVO report = generateByDeepSeek(now);
                log.info("AI 报告生成成功, mode=DEEPSEEK, model={}", aiProperties.getModel());
                return report;
            } catch (Exception e) {
                log.warn("DeepSeek 生成失败，切换到规则兜底: {}", e.getMessage());
                if (aiProperties.isFallbackEnabled()) {
                    AiReportVO report = generateByRuleFallback(now);
                    report.setFallbackReason("DeepSeek 调用失败: " + e.getMessage());
                    return report;
                }
                throw new RuntimeException("AI 报告生成失败且未启用 fallback", e);
            }
        }

        // 未配置 API Key，直接规则兜底
        AiReportVO report = generateByRuleFallback(now);
        report.setFallbackReason("未配置 DEEPSEEK_API_KEY 或 AI 未启用，系统已切换为规则引擎分析模式");
        return report;
    }

    // ===== 第一层：数据准备 =====

    private String collectAiContext() {
        StringBuilder sb = new StringBuilder();

        // 资产总览
        long totalCount = aiAnalysisMapper.selectTotalAssetCount();
        BigDecimal totalOrig = aiAnalysisMapper.selectTotalOriginalValue();
        BigDecimal totalNet = aiAnalysisMapper.selectTotalNetValue();
        BigDecimal totalAccDep = aiAnalysisMapper.selectTotalAccumulatedDepreciation();

        sb.append("【资产总览】\n");
        sb.append(String.format("资产总数: %d 项\n", totalCount));
        sb.append(String.format("资产原值合计: %s 元\n", totalOrig));
        sb.append(String.format("资产净值合计: %s 元\n", totalNet));
        sb.append(String.format("累计折旧合计: %s 元\n", totalAccDep));
        sb.append("\n");

        // 状态分布
        List<Map<String, Object>> dist = aiAnalysisMapper.selectStatusDistribution();
        sb.append("【资产状态分布】\n");
        for (Map<String, Object> r : dist) {
            String status = (String) r.get("status");
            String label = STATUS_LABELS.getOrDefault(status, status);
            sb.append(String.format("- %s: %s 项, 净值 %s 元\n", label, r.get("cnt"), r.get("net_value")));
        }
        sb.append("\n");

        // 风险统计
        int idleCount = aiAnalysisMapper.selectIdleAssets().size();
        int freqRepairCount = aiAnalysisMapper.selectFrequentRepairAssets().size();
        int abnormalCount = aiAnalysisMapper.selectAbnormalAssets().size();
        int lowValueCount = aiAnalysisMapper.selectLowValueAssetCount();
        int nearEndCount = aiAnalysisMapper.selectNearEndOfLifeCount();
        int repairingCount = aiAnalysisMapper.selectRepairingCount();
        int financeSyncFail = aiAnalysisMapper.selectFinanceSyncFailCount();
        int todayAuditLogs = aiAnalysisMapper.selectTodayAuditLogCount();

        sb.append("【风险统计】\n");
        sb.append(String.format("- 长期闲置资产(>1年): %d 项\n", idleCount));
        sb.append(String.format("- 频繁维修资产(>=3次): %d 项\n", freqRepairCount));
        sb.append(String.format("- 盘点异常资产: %d 项\n", abnormalCount));
        sb.append(String.format("- 低净值资产(净值<=原值5%%): %d 项\n", lowValueCount));
        sb.append(String.format("- 接近使用年限资产: %d 项\n", nearEndCount));
        sb.append(String.format("- 当前维修中资产: %d 项\n", repairingCount));
        sb.append(String.format("- 财务同步失败记录: %d 条\n", financeSyncFail));
        sb.append(String.format("- 今日审计操作日志: %d 条\n", todayAuditLogs));
        sb.append("\n");

        // 折旧趋势
        List<Map<String, Object>> trend = aiAnalysisMapper.selectMonthlyDepreciationTrend();
        if (!trend.isEmpty()) {
            sb.append("【近12个月折旧趋势】\n");
            for (Map<String, Object> t : trend) {
                sb.append(String.format("- %s: 月折旧 %s 元, 累计折旧 %s 元\n",
                        t.get("month"), t.get("monthly_dep"), t.get("acc_dep")));
            }
            sb.append("\n");
        }

        // 典型高风险资产
        List<Map<String, Object>> riskAssets = aiAnalysisMapper.selectTopRiskAssets();
        if (!riskAssets.isEmpty()) {
            sb.append("【典型高风险资产】\n");
            for (Map<String, Object> a : riskAssets) {
                sb.append(String.format("- %s %s | 部门: %s | 状态: %s | 原值: %s 元 | 净值: %s 元 | 使用年限: %s 年 | 已用: %.1f 年\n",
                        a.get("asset_code"), a.get("asset_name"), a.get("department"),
                        a.get("status"), a.get("original_value"), a.get("net_value"),
                        a.get("useful_life"), ((Number) a.get("used_years")).doubleValue()));
            }
            sb.append("\n");
        }

        // 维修/报废建议统计
        int repairSugg = aiAnalysisMapper.selectRepairSuggestions().size();
        int scrapSugg = aiAnalysisMapper.selectScrapSuggestions().size();
        sb.append("【建议统计】\n");
        sb.append(String.format("- 建议维修检测: %d 项\n", repairSugg));
        sb.append(String.format("- 建议启动报废: %d 项\n", scrapSugg));

        return sb.toString();
    }

    // ===== 第二层：DeepSeek 生成 =====

    private AiReportVO generateByDeepSeek(String now) {
        String context = collectAiContext();

        String systemPrompt = "你是国企固定资产管理系统中的AI辅助分析助手。你只能基于用户提供的系统数据进行分析，"
                + "不能编造不存在的资产、金额、部门、月份或业务记录。你的输出用于辅助资产管理员、财务人员和审计人员理解风险，"
                + "不作为最终审批、报废或财务结论。请用中文输出，语言专业、简洁、适合管理系统报告。\n\n"
                + "请严格按照以下JSON格式输出（不要输出JSON以外的内容，不要输出markdown代码块标记）：\n"
                + "{\n"
                + "  \"summary\": \"资产总体状态摘要（2-3句话，包含总数、原值、净值等关键数据）\",\n"
                + "  \"keyRisks\": \"主要风险分析（逐条列出关键风险点和涉及的资产）\",\n"
                + "  \"financialInsight\": \"财务与折旧分析（基于折旧趋势和净值数据）\",\n"
                + "  \"operationInsight\": \"运营管理建议（针对闲置、维修、盘点异常等）\",\n"
                + "  \"auditFocus\": \"审计关注点（基于操作日志和异常数据）\",\n"
                + "  \"recommendations\": [\"建议1\", \"建议2\", \"建议3\", \"建议4\"],\n"
                + "  \"conclusion\": \"管理结论（1-2句话总结）\"\n"
                + "}\n\n"
                + "要求：不得编造输入中没有的数据，金额、数量、月份必须来自输入数据，数据不足时说明\"当前数据不足以判断\"。";

        String userPrompt = "以下是国企固定资产管理系统的当前数据，请基于这些数据生成AI辅助分析报告：\n\n" + context;

        LlmResponse llmResponse = llmClient.chat(systemPrompt, userPrompt);
        String rawText = llmResponse.getContent();

        AiReportVO vo = new AiReportVO();
        vo.setGeneratedAt(now);
        vo.setAnalysisMode("DEEPSEEK");
        vo.setProvider(aiProperties.getProvider());
        vo.setModel(llmResponse.getModel());
        vo.setRawText(rawText);

        // 尝试解析 JSON 响应
        try {
            String jsonStr = rawText.trim();
            // 去除可能的 markdown 代码块标记
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            vo.setSummary(getJsonText(root, "summary"));
            vo.setKeyRisks(getJsonText(root, "keyRisks"));
            vo.setFinancialInsight(getJsonText(root, "financialInsight"));
            vo.setOperationInsight(getJsonText(root, "operationInsight"));
            vo.setAuditFocus(getJsonText(root, "auditFocus"));
            vo.setConclusion(getJsonText(root, "conclusion"));

            JsonNode recs = root.path("recommendations");
            if (recs.isArray()) {
                List<String> recList = new ArrayList<>();
                for (JsonNode rec : recs) {
                    recList.add(rec.asText());
                }
                vo.setRecommendations(recList);
            }

            // 兼容旧字段
            vo.setAnomalyOverview(vo.getKeyRisks());
            vo.setSuggestionOverview(String.join("；", vo.getRecommendations() != null ? vo.getRecommendations() : Collections.emptyList()));

        } catch (Exception e) {
            log.warn("DeepSeek 响应 JSON 解析失败，使用原始文本: {}", e.getMessage());
            // JSON 解析失败，把原始文本作为摘要
            vo.setSummary(rawText);
            vo.setKeyRisks("模型返回非结构化内容，请查看原始报告");
            vo.setFinancialInsight("见原始报告");
            vo.setOperationInsight("见原始报告");
            vo.setAuditFocus("见原始报告");
            vo.setConclusion("见原始报告");
            vo.setRecommendations(Collections.emptyList());
            vo.setAnomalyOverview(vo.getKeyRisks());
            vo.setSuggestionOverview(vo.getConclusion());
        }

        // 生成 Markdown 报告
        vo.setMarkdownReport(buildMarkdownReport(vo));

        return vo;
    }

    // ===== 第三层：规则兜底 =====

    private AiReportVO generateByRuleFallback(String now) {
        AiReportVO vo = new AiReportVO();
        vo.setGeneratedAt(now);
        vo.setAnalysisMode("RULE_FALLBACK");
        vo.setProvider("rule-engine");
        vo.setModel("none");

        long totalCount = aiAnalysisMapper.selectTotalAssetCount();
        BigDecimal totalOrig = aiAnalysisMapper.selectTotalOriginalValue();
        BigDecimal totalNet = aiAnalysisMapper.selectTotalNetValue();
        BigDecimal totalAccDep = aiAnalysisMapper.selectTotalAccumulatedDepreciation();

        List<Map<String, Object>> dist = aiAnalysisMapper.selectStatusDistribution();
        String statusDesc = dist.stream()
                .map(r -> STATUS_LABELS.getOrDefault((String) r.get("status"), (String) r.get("status")) + " " + r.get("cnt") + " 项")
                .collect(Collectors.joining("，"));

        vo.setSummary(String.format("截至 %s，系统共登记固定资产 %d 项，资产原值总计 %s 元，资产净值总计 %s 元。状态分布：%s。",
                now, totalCount, totalOrig, totalNet, statusDesc));

        int idle = aiAnalysisMapper.selectIdleAssets().size();
        int freq = aiAnalysisMapper.selectFrequentRepairAssets().size();
        int abn = aiAnalysisMapper.selectAbnormalAssets().size();
        int lowVal = aiAnalysisMapper.selectLowValueAssetCount();
        int nearEnd = aiAnalysisMapper.selectNearEndOfLifeCount();
        int finFail = aiAnalysisMapper.selectFinanceSyncFailCount();

        vo.setKeyRisks(String.format("当前存在 %d 项长期闲置资产、%d 项频繁维修资产、%d 项盘点异常资产、%d 项低净值资产、%d 项接近使用年限资产。%s",
                idle, freq, abn, lowVal, nearEnd,
                (idle + freq + abn + lowVal + nearEnd > 0) ? "建议管理人员及时核实并处理。" : "整体风险可控。"));

        vo.setFinancialInsight(String.format("累计折旧 %s 元，资产净值率 %.1f%%。%s",
                totalAccDep,
                totalOrig.compareTo(BigDecimal.ZERO) > 0 ? totalNet.divide(totalOrig, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0,
                finFail > 0 ? String.format("财务同步存在 %d 条失败记录，需排查同步异常。", finFail) : "财务同步正常。"));

        List<String> recs = new ArrayList<>();
        if (idle > 0) recs.add(String.format("对 %d 项长期闲置资产进行复核，考虑调拨或处置", idle));
        if (freq > 0) recs.add(String.format("对 %d 项频繁维修资产评估是否报废更换", freq));
        if (abn > 0) recs.add(String.format("对 %d 项盘点异常资产进行实地核查", abn));
        if (lowVal > 0) recs.add(String.format("对 %d 项低净值资产启动报废评估", lowVal));
        if (nearEnd > 0) recs.add(String.format("对 %d 项接近使用年限资产制定更新计划", nearEnd));
        if (finFail > 0) recs.add(String.format("排查 %d 条财务同步失败记录", finFail));
        if (recs.isEmpty()) recs.add("当前资产运营状况良好，建议保持定期巡检和数据同步");
        vo.setRecommendations(recs);

        int repairSugg = aiAnalysisMapper.selectRepairSuggestions().size();
        int scrapSugg = aiAnalysisMapper.selectScrapSuggestions().size();
        vo.setOperationInsight(String.format("建议安排维修检测 %d 项资产，建议启动报废流程 %d 项资产。", repairSugg, scrapSugg));

        int todayLogs = aiAnalysisMapper.selectTodayAuditLogCount();
        vo.setAuditFocus(String.format("今日系统操作日志 %d 条。%s",
                todayLogs,
                (abn > 0 || finFail > 0) ? "建议重点关注盘点异常和财务同步记录。" : "审计无重大异常关注点。"));

        vo.setConclusion(String.format("系统共管理 %d 项固定资产，净值 %s 元。%s",
                totalCount, totalNet,
                (idle + freq + abn + lowVal) > 0 ? "存在需要关注的风险资产，建议按优先级处置。" : "资产整体状况良好。"));

        // 兼容旧字段
        vo.setAnomalyOverview(vo.getKeyRisks());
        vo.setSuggestionOverview(String.join("；", recs));

        vo.setMarkdownReport(buildMarkdownReport(vo));
        vo.setRawText(null);

        return vo;
    }

    // ===== 辅助方法 =====

    private String getJsonText(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return "";
        return n.asText("");
    }

    private String buildMarkdownReport(AiReportVO vo) {
        StringBuilder md = new StringBuilder();
        md.append("# AI 辅助分析报告\n\n");
        md.append(String.format("**分析模式**: %s | **模型**: %s | **生成时间**: %s\n\n", vo.getAnalysisMode(), vo.getModel(), vo.getGeneratedAt()));
        md.append("---\n\n");

        md.append("## 一、资产总体状态摘要\n\n");
        md.append(vo.getSummary()).append("\n\n");

        md.append("## 二、主要风险分析\n\n");
        md.append(vo.getKeyRisks()).append("\n\n");

        md.append("## 三、财务与折旧分析\n\n");
        md.append(vo.getFinancialInsight()).append("\n\n");

        md.append("## 四、运营管理建议\n\n");
        md.append(vo.getOperationInsight()).append("\n\n");

        md.append("## 五、审计关注点\n\n");
        md.append(vo.getAuditFocus()).append("\n\n");

        md.append("## 六、下一步处置建议\n\n");
        if (vo.getRecommendations() != null && !vo.getRecommendations().isEmpty()) {
            for (int i = 0; i < vo.getRecommendations().size(); i++) {
                md.append(String.format("%d. %s\n", i + 1, vo.getRecommendations().get(i)));
            }
        } else {
            md.append("暂无具体建议\n");
        }
        md.append("\n");

        md.append("## 七、管理结论\n\n");
        md.append(vo.getConclusion()).append("\n");

        if (vo.getFallbackReason() != null) {
            md.append("\n---\n\n> ⚠️ ").append(vo.getFallbackReason()).append("\n");
        }

        return md.toString();
    }
}
