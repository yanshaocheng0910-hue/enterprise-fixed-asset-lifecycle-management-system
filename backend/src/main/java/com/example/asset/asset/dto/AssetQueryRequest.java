package com.example.asset.asset.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AssetQueryRequest {

    private String assetCode;
    private String assetName;
    private Long categoryId;
    private String department;
    private String keeper;
    private String location;
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDateEnd;

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    private Long pageSize = 10L;
}
