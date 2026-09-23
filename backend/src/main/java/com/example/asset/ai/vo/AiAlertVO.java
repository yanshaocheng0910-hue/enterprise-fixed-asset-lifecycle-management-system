package com.example.asset.ai.vo;

import lombok.Data;
import java.util.List;

@Data
public class AiAlertVO {
    private List<AlertItem> idleAlerts;
    private List<AlertItem> frequentRepairAlerts;
    private List<AlertItem> abnormalStatusAlerts;

    @Data
    public static class AlertItem {
        private Long assetId;
        private String assetCode;
        private String assetName;
        private String department;
        private String keeper;
        private String alertReason;
        private String severity;
    }
}
