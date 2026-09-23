package com.example.asset.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_inbound_order")
public class InboundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderCode;
    private Long assetId;
    private String inboundType;
    private String supplier;
    private String purchaseOrderNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inboundDate;
    private String handler;
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
