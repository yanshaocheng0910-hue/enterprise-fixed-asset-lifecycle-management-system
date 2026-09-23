package com.example.asset.lifecycle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InboundOrderPageVO {

    private Long id;
    private String orderCode;
    private Long assetId;
    private String assetName;
    private String assetCode;
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
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
