package com.example.asset.audit.vo;

import lombok.Data;

/**
 * 审计统计 VO
 */
@Data
public class AuditSummaryVO {

    /** 今日操作总数 */
    private Integer todayOperationCount;
    /** 资产操作日志总数 */
    private Integer assetChangeCount;
    /** 审批记录总数 */
    private Integer approvalOperationCount;
    /** 盘点异常总数（result != NORMAL） */
    private Integer inventoryAbnormalCount;
    /** 财务同步记录总数 */
    private Integer financeSyncCount;
}
