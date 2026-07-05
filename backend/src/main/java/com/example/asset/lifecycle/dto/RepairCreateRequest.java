package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RepairCreateRequest {

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    @NotBlank(message = "故障描述不能为空")
    private String faultDescription;

    private String repairVendor;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repairStartDate;

    private String remark;
}
