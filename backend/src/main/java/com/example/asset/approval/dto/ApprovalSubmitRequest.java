package com.example.asset.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalSubmitRequest {

    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotNull(message = "业务单据ID不能为空")
    private Long businessId;

    private String remark;
}
