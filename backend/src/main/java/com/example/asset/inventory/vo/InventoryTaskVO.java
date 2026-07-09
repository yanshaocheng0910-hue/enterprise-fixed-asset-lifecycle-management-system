package com.example.asset.inventory.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryTaskVO {
    private Long id;
    private String taskCode;
    private String taskName;
    private String scopeType;
    private String department;
    private String location;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdByName;
    private LocalDateTime createdAt;

    private long totalCount;
    private long scannedCount;
    private long normalCount;
    private long abnormalCount;
}