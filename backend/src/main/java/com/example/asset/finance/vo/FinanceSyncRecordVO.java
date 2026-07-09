package com.example.asset.finance.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceSyncRecordVO {

    private Long id;
    private String syncBatchNo;
    private String syncMonth;
    private Integer assetCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalNetValue;
    private BigDecimal totalAccumulatedDepreciation;
    private BigDecimal monthlyDepreciation;
    private String status;
    private String operatorName;
    private String syncTime;
    private String remark;
}
