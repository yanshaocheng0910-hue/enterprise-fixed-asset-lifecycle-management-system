package com.example.asset.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalTodoVO {
    private Long instanceId;
    private String businessType;
    private Long businessId;
    private String flowName;
    private String nodeName;
    private String status;
    private Long startedBy;
    private String applicantName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    // 资产信息（填充字段）
    private String orderCode;
    private String assetCode;
    private String assetName;
    private String summary;
}
