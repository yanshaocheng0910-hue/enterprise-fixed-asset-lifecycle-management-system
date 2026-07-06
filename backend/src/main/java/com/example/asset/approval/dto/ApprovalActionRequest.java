package com.example.asset.approval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalActionRequest {

    @NotBlank(message = "审批动作不能为空")
    private String action;

    private String comment;
}
