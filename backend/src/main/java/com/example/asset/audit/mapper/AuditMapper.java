package com.example.asset.audit.mapper;

import com.example.asset.audit.vo.AuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 审计日志 Mapper（多源只读查询，统一转换成 AuditLogVO）
 */
@Mapper
public interface AuditMapper {

    /**
     * 1. 资产操作日志（asset_operation_log JOIN asset）
     */
    List<AuditLogVO> selectAssetOperationLogs(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 2. 审批操作日志（approval_record JOIN approval_instance JOIN lifecycle UNION JOIN asset）
     */
    List<AuditLogVO> selectApprovalAuditLogs(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 3. 盘点异常日志（inventory_record WHERE result != 'NORMAL' JOIN asset JOIN inventory_task）
     */
    List<AuditLogVO> selectInventoryAbnormalLogs(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 4. 财务同步日志（finance_sync_record）
     */
    List<AuditLogVO> selectFinanceSyncLogs(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 统计：资产操作日志总数
     */
    int countAssetOperationLogs();

    /**
     * 统计：审批记录总数
     */
    int countApprovalRecords();

    /**
     * 统计：盘点异常总数
     */
    int countInventoryAbnormal();

    /**
     * 统计：财务同步记录总数
     */
    int countFinanceSyncRecords();

    /**
     * 统计：今日操作总数（asset_operation_log.operation_time >= 今日 00:00）
     */
    int countTodayOperations(@Param("todayStart") LocalDate todayStart);
}
