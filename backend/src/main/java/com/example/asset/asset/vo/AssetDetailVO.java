package com.example.asset.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetDetailVO {

    private Long id;
    private String assetCode;
    private String assetName;
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private String specification;
    private String brand;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    private BigDecimal originalValue;
    private Integer usefulLife;
    private BigDecimal residualRate;
    private String depreciationMethod;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netValue;
    private String department;
    private String keeper;
    private String location;
    private String status;
    private String qrCode;
    private String rfidCode;
    private String photoUrl;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
