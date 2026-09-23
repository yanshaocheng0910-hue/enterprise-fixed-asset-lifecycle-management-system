package com.example.asset.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("finance_sync_record")
public class FinanceSyncRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String syncMonth;
    private String syncBatchNo;
    private BigDecimal totalAmount;
    private Integer recordCount;
    private Integer assetCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalNetValue;
    private BigDecimal totalAccumulatedDepreciation;
    private BigDecimal monthlyDepreciation;
    private String status;
    private String operatorName;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}