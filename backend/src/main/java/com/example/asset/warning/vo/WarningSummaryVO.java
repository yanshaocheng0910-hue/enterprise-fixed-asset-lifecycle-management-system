package com.example.asset.warning.vo;

import lombok.Data;

@Data
public class WarningSummaryVO {

    private Integer totalWarningCount;
    private Integer highWarningCount;
    private Integer mediumWarningCount;
    private Integer lowWarningCount;
    private Integer lowValueCount;
    private Integer nearEndCount;
    private Integer idleLongTimeCount;
    private Integer repairOverdueCount;
    private Integer inventoryAbnormalCount;
    private Integer financeSyncAbnormalCount;
}
