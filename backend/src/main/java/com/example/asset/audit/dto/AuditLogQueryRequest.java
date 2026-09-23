package com.example.asset.audit.dto;

import lombok.Data;

/**
 * 审计日志查询请求
 */
@Data
public class AuditLogQueryRequest {

    /** 日志类型：ASSET_OPERATION/APPROVAL/INVENTORY_ABNORMAL/FINANCE_SYNC */
    private String logType;
    /** 资产编号（模糊匹配） */
    private String assetCode;
    /** 资产名称（模糊匹配） */
    private String assetName;
    /** 操作人（模糊匹配） */
    private String operatorName;
    /** 开始日期 yyyy-MM-dd */
    private String startDate;
    /** 结束日期 yyyy-MM-dd */
    private String endDate;
    /** 页码 */
    private Long pageNum = 1L;
    /** 每页条数 */
    private Long pageSize = 10L;
}
