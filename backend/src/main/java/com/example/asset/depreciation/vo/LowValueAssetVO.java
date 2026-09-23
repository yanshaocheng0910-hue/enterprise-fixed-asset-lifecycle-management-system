package com.example.asset.depreciation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LowValueAssetVO {

    private Long assetId;
    private String assetCode;
    private String assetName;
    private String department;
    private BigDecimal originalValue;
    private BigDecimal netValue;
    private BigDecimal netValueRate;
    private String status;
}
