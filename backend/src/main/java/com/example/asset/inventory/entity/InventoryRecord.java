package com.example.asset.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long assetId;
    private String expectedLocation;
    private String actualLocation;
    private String expectedKeeper;
    private String actualKeeper;
    private String result;
    private LocalDateTime scannedAt;
    private String remark;
}
