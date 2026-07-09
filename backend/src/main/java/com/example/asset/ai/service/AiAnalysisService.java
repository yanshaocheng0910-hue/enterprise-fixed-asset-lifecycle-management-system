package com.example.asset.ai.service;

import com.example.asset.ai.mapper.AiAnalysisMapper;
import com.example.asset.ai.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

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

    public AiAnalysisService(AiAnalysisMapper aiAnalysisMapper) {
        this.aiAnalysisMapper = aiAnalysisMapper;
    }

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
            s.setDepartment((String) r.get("department"));
            s.setUsefulLife(r.get("useful_life") != null ? ((Number) r.get("useful_life")).intValue() : null);
            s.setNetValue((BigDecimal) r.get("net_value"));
            s.setRepairCount(0);
            BigDecimal netValue = s.getNetValue();
            if (netValue != null && netValue.compareTo(BigDecimal.ZERO) <= 0) {
                s.setSuggestion("建议启动报废流程");
                s.setReason("资产净值已归零，建议报废处理");
            } else {
                s.setSuggestion("建议评估报废");
                s.setReason("资产已接近或超过使用年限（" + s.getUsefulLife() + " 年），当前净值 " + netValue + " 元");
            }
            return s;
        }).collect(Collectors.toList()));

        return vo;
    }

    public AiReportVO getReport() {
        AiReportVO vo = new AiReportVO();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        vo.setGeneratedAt(now);

        long totalCount = aiAnalysisMapper.selectTotalAssetCount();
        BigDecimal totalOrig = aiAnalysisMapper.selectTotalOriginalValue();
        BigDecimal totalNet = aiAnalysisMapper.selectTotalNetValue();

        List<Map<String, Object>> dist = aiAnalysisMapper.selectStatusDistribution();
        String statusDesc = dist.stream()
                .map(r -> STATUS_LABELS.getOrDefault((String) r.get("status"), (String) r.get("status")) + " " + r.get("cnt") + " 项")
                .collect(Collectors.joining("，"));
        vo.setSummary(String.format("截至 %s，系统共登记固定资产 %d 项，资产原值总计 %s 元，资产净值总计 %s 元。状态分布：%s。",
                now, totalCount, totalOrig, totalNet, statusDesc));

        int idle = aiAnalysisMapper.selectIdleAssets().size();
        int freq = aiAnalysisMapper.selectFrequentRepairAssets().size();
        int abn = aiAnalysisMapper.selectAbnormalAssets().size();
        String anomaly = String.format("当前存在 %d 项长期闲置资产、%d 项频繁维修资产、%d 项盘点异常资产。", idle, freq, abn);
        if (idle + freq + abn > 0) anomaly += "建议管理人员及时核实并处理。";
        vo.setAnomalyOverview(anomaly);

        int repairSugg = aiAnalysisMapper.selectRepairSuggestions().size();
        int scrapSugg = aiAnalysisMapper.selectScrapSuggestions().size();
        vo.setSuggestionOverview(String.format("建议安排维修检测 %d 项资产，建议启动报废流程 %d 项资产。", repairSugg, scrapSugg));

        return vo;
    }
}
