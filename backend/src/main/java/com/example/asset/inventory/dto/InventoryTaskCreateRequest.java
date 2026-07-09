package com.example.asset.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InventoryTaskCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "盘点范围类型不能为空")
    private String scopeType;

    private String department;

    private String location;
}
