package com.example.asset.depreciation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyReportSummaryVO {

    private String month;
    private BigDecimal monthlyDepreciationTotal;
    private BigDecimal accumulatedDepreciationTotal;
    private BigDecimal originalValueTotal;
    private BigDecimal netValueTotal;
    private Integer totalAssetCount;
    private Integer depreciatingAssetCount;
}
