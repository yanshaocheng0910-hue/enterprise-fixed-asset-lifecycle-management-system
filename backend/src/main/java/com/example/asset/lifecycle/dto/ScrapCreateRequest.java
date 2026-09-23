package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScrapCreateRequest {

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    @NotBlank(message = "报废原因不能为空")
    private String scrapReason;

    @NotNull(message = "报废日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scrapDate;

    private String disposalMethod;

    private BigDecimal residualValue;

    private String remark;
}
