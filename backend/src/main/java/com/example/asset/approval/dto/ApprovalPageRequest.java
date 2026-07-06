package com.example.asset.approval.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ApprovalPageRequest {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    private Long pageSize = 10L;

    private String businessType;

    private String status;
}
