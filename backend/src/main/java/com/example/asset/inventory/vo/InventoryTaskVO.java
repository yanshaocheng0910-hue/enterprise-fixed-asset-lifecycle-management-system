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
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalRecords;
    private Integer completedRecords;
}
