package com.example.asset.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_task")
public class InventoryTask {

    @TableId(type = IdType.AUTO)
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
}
