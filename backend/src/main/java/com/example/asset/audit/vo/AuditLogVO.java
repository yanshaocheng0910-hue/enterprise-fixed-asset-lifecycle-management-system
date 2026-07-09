package com.example.asset.audit.vo;

import lombok.Data;

/**
 * 审计日志 VO（统一展示资产操作、审批操作、盘点异常、财务同步）
 */
@Data
public class AuditLogVO {

    /** 复合 ID（来源前缀+原ID，如 ASSET-123、APPROVAL-456） */
    private String id;
    /** 日志类型：ASSET_OPERATION/APPROVAL/INVENTORY_ABNORMAL/FINANCE_SYNC */
    private String logType;
    /** 类型中文名 */
    private String logTypeName;
    /** 资产 ID（财务同步可为空） */
    private Long assetId;
    /** 资产编号 */
    private String assetCode;
    /** 资产名称 */
    private String assetName;
    /** 业务类型（RECEIVE/TRANSFER/REPAIR/SCRAP/FINANCE/INVENTORY 等） */
    private String businessType;
    /** 业务单据 ID */
    private Long businessId;
    /** 操作描述 */
    private String operation;
    /** 变更前状态 */
    private String beforeStatus;
    /** 变更后状态 */
    private String afterStatus;
    /** 操作人 */
    private String operatorName;
    /** 操作时间（yyyy-MM-dd HH:mm:ss） */
    private String operationTime;
    /** 备注 */
    private String remark;
    /** 来源（ASSET/APPROVAL/INVENTORY/FINANCE） */
    private String source;
}
