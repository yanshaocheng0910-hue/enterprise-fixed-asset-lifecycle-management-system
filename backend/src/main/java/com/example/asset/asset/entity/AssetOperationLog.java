package com.example.asset.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("asset_operation_log")
public class AssetOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private String operationType;
    private String operationName;
    private String beforeStatus;
    private String afterStatus;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime operationTime;
    private String remark;
}
