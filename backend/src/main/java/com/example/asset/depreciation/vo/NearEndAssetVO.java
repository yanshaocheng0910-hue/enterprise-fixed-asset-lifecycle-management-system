package com.example.asset.depreciation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NearEndAssetVO {

    private Long assetId;
    private String assetCode;
    private String assetName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    private Integer usefulLife;
    private Integer usedMonths;
    private Integer remainingMonths;
    private String status;
}
