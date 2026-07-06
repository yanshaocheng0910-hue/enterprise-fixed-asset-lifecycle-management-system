package com.example.asset.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalDetailVO {
    private Long instanceId;
    private String businessType;
    private Long businessId;
    private String flowName;
    private String currentNodeName;
    private String status;
    private String applicantName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    private List<ApprovalRecordVO> records;
}
