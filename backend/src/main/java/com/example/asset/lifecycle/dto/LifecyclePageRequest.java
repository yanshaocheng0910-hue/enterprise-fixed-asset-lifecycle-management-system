package com.example.asset.lifecycle.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LifecyclePageRequest {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    private Long pageSize = 10L;

    private String orderCode;

    private String assetName;

    private String status;
}
