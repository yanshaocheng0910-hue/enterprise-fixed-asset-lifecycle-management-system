package com.example.asset.inventory.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryTaskQueryRequest {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    private Long pageSize = 10L;

    private String status;

    private String scopeType;
}
