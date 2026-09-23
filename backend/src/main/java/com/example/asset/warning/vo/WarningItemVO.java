package com.example.asset.warning.vo;

import lombok.Data;

@Data
public class WarningItemVO {

    private Long id;
    private String warningType;
    private String warningTypeName;
    private String warningLevel;
    private String title;
    private String description;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long businessId;
    private String businessType;
    private String source;
    private String createdAt;
    private String suggestion;
}
