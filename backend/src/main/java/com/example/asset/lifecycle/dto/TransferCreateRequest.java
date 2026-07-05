package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransferCreateRequest {

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    private String fromDepartment;

    @NotBlank(message = "调入部门不能为空")
    private String toDepartment;

    private String fromLocation;

    private String toLocation;

    private String fromKeeper;

    @NotBlank(message = "调入保管人不能为空")
    private String toKeeper;

    @NotNull(message = "调拨日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    private String remark;
}
