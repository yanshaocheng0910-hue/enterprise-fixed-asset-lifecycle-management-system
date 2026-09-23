package com.example.asset.lifecycle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScrapOrderPageVO {

    private Long id;
    private String orderCode;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String scrapReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scrapDate;
    private String disposalMethod;
    private BigDecimal residualValue;
    private String beforeStatus;
    private String afterStatus;
    private String status;
    private String remark;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
