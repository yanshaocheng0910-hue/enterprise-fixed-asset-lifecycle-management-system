package com.example.asset.ai.vo;

import lombok.Data;
import java.util.Map;

@Data
public class AiReportVO {
    private String generatedAt;
    private String summary;
    private String anomalyOverview;
    private String suggestionOverview;
}
