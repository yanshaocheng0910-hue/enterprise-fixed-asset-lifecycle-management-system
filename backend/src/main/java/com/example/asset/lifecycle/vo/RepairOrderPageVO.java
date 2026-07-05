package com.example.asset.lifecycle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RepairOrderPageVO {

    private Long id;
    private String orderCode;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String faultDescription;
    private String repairVendor;
    private BigDecimal repairCost;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repairStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repairEndDate;
    private String repairResult;
    private String beforeStatus;
    private String afterStatus;
    private String status;
    private String remark;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
