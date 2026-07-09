package com.example.asset.depreciation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepreciationSummaryVO {

    private Integer assetCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalNetValue;
    private BigDecimal totalAccumulatedDepreciation;
    private BigDecimal monthlyDepreciation;
    private BigDecimal averageDepreciationRate;
    private Integer lowValueAssetCount;
    private Integer nearEndAssetCount;
}
