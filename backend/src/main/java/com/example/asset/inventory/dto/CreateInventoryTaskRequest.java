package com.example.asset.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInventoryTaskRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String scopeType;

    private String department;

    private String location;
}