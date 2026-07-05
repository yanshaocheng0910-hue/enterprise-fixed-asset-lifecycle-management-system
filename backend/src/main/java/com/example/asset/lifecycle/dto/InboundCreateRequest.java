package com.example.asset.lifecycle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InboundCreateRequest {

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    private String inboundType;

    private String supplier;

    private String purchaseOrderNo;

    @NotNull(message = "入库日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inboundDate;

    private String handler;

    private String remark;
}
