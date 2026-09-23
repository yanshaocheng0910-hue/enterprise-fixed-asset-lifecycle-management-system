package com.example.asset.depreciation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MonthlyDepreciationItemVO {

    private Long assetId;
    private String assetCode;
    private String assetName;
    private String categoryName;
    private String department;
    private String keeper;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    private BigDecimal originalValue;
    private BigDecimal residualRate;
    private Integer usefulLife;
    private BigDecimal monthlyDepreciation;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netValue;
    private String status;
}
