package com.example.asset.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_transfer_order")
public class TransferOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderCode;
    private Long assetId;
    private String fromDepartment;
    private String toDepartment;
    private String fromLocation;
    private String toLocation;
    private String fromKeeper;
    private String toKeeper;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;
    private String beforeStatus;
    private String afterStatus;
    private String status;
    private String remark;
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
