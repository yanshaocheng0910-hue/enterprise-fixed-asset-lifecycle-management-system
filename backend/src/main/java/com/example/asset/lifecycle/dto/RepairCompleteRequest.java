package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RepairCompleteRequest {

    @NotBlank(message = "维修结果不能为空")
    private String repairResult;

    private String repairVendor;

    private BigDecimal repairCost;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repairEndDate;

    private String remark;
}
