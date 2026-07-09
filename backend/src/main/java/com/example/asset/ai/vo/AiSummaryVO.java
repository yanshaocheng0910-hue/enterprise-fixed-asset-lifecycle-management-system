package com.example.asset.ai.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AiSummaryVO {
    private Long totalCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalNetValue;
    private List<StatusItem> statusDistribution;

    @Data
    public static class StatusItem {
        private String status;
        private String statusLabel;
        private Long count;
        private BigDecimal totalValue;
    }
}
