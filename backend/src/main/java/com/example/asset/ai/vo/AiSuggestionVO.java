package com.example.asset.ai.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AiSuggestionVO {
    private List<SuggestionItem> repairSuggestions;
    private List<SuggestionItem> scrapSuggestions;

    @Data
    public static class SuggestionItem {
        private Long assetId;
        private String assetCode;
        private String assetName;
        private String department;
        private Integer usefulLife;
        private BigDecimal netValue;
        private Integer repairCount;
        private String suggestion;
        private String reason;
    }
}
