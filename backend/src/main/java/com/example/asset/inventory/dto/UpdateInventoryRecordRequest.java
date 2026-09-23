package com.example.asset.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInventoryRecordRequest {

    @NotNull(message = "盘点记录ID不能为空")
    private Long recordId;

    private String actualLocation;

    private String actualKeeper;

    private String result;

    private String remark;
}