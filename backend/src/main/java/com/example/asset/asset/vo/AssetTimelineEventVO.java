package com.example.asset.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AssetTimelineEventVO {

    private String id;
    private Long assetId;
    private String eventType;
    private String eventTypeName;
    private String title;
    private String description;
    private String orderCode;
    private String businessType;
    private Long businessId;
    private String status;
    private String beforeStatus;
    private String afterStatus;
    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    private String source;
    private String remark;
}
