package com.example.asset.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderCode;
    private Long assetId;
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
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
