package com.example.asset.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsVO {

    private Long assetCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalAccumulatedDepreciation;
    private BigDecimal totalNetValue;
    private Long inUseCount;
    private Long idleCount;
    private Long repairingCount;
    private Long waitingScrapCount;
}
