package com.example.asset.inventory.vo;

import lombok.Data;
import java.util.List;

@Data
public class InventoryReportVO {
    private InventoryTaskVO task;
    private long totalCount;
    private long scannedCount;
    private long normalCount;
    private long locationMismatchCount;
    private long keeperMismatchCount;
    private long missingCount;
    private List<InventoryRecordVO> details;
}