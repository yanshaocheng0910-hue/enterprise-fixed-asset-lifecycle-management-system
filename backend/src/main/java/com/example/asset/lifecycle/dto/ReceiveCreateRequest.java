package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReceiveCreateRequest {

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    @NotBlank(message = "领用人不能为空")
    private String receiver;

    @NotBlank(message = "领用部门不能为空")
    private String receiverDepartment;

    @NotNull(message = "领用日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiveDate;

    private String usagePurpose;

    private String remark;
}
