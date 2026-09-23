package com.example.asset.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InventoryRecordUpdateRequest {

    private String actualLocation;

    private String actualKeeper;

    @NotBlank(message = "盘点结果不能为空")
    private String result;

    private String remark;
}
