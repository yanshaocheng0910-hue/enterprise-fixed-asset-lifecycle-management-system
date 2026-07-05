package com.example.asset.lifecycle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReceiveOrderPageVO {

    private Long id;
    private String orderCode;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String receiver;
    private String receiverDepartment;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiveDate;
    private String usagePurpose;
    private String beforeStatus;
    private String afterStatus;
    private String status;
    private String remark;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
