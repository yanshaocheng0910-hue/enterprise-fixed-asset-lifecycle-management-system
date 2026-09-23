package com.example.asset.depreciation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("depreciation_record")
public class DepreciationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private String depreciationMonth;
    private BigDecimal originalValue;
    private BigDecimal monthlyDepreciation;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netValue;
    private LocalDateTime createdAt;
}
