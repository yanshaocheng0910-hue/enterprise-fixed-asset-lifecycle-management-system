package com.example.asset.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalDoneVO {
    private Long instanceId;
    private String businessType;
    private Long businessId;
    private String orderCode;
    private String assetCode;
    private String assetName;
    private String action;
    private String comment;
    private String status;
    private String approverName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
}
