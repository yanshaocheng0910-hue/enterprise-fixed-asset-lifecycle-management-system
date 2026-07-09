package com.example.asset.depreciation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepartmentStatVO {

    private String department;
    private Integer assetCount;
    private BigDecimal originalValueTotal;
    private BigDecimal accumulatedDepreciationTotal;
    private BigDecimal netValueTotal;
    private BigDecimal monthlyDepreciation;
}
