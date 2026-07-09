package com.example.asset.inventory.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryRecordVO {
    private Long id;
    private Long taskId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String categoryName;
    private String expectedLocation;
    private String actualLocation;
    private String expectedKeeper;
    private String actualKeeper;
    private String result;
    private LocalDateTime scannedAt;
    private String remark;
}