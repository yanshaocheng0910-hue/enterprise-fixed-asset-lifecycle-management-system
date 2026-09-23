package com.example.asset.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetPageVO {

    private Long id;
    private String assetCode;
    private String assetName;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String specification;
    private String department;
    private String keeper;
    private String location;
    private BigDecimal originalValue;
    private BigDecimal netValue;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
}
