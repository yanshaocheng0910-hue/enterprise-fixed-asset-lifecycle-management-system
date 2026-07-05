package com.example.asset.asset.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetUpdateRequest {

    @NotBlank(message = "资产名称不能为空")
    private String assetName;

    @NotNull(message = "资产分类不能为空")
    private Long categoryId;

    private String specification;
    private String brand;

    @NotNull(message = "购置日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    @NotNull(message = "资产原值不能为空")
    @Positive(message = "资产原值必须大于0")
    private BigDecimal originalValue;

    @NotNull(message = "使用年限不能为空")
    @Positive(message = "使用年限必须大于0")
    private Integer usefulLife;

    @NotNull(message = "残值率不能为空")
    @DecimalMin(value = "0.00", message = "残值率不能小于0")
    @DecimalMax(value = "1.00", message = "残值率不能大于1")
    private BigDecimal residualRate;

    @NotBlank(message = "资产状态不能为空")
    private String status;

    private String department;
    private String keeper;
    private String location;
    private String qrCode;
    private String rfidCode;
    private String photoUrl;
    private String remark;
}
