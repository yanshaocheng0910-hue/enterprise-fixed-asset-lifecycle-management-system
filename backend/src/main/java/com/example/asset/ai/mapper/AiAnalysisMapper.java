package com.example.asset.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiAnalysisMapper {

    @Select("SELECT COUNT(*) FROM asset WHERE deleted = 0")
    Long selectTotalAssetCount();

    @Select("SELECT COALESCE(SUM(original_value), 0) FROM asset WHERE deleted = 0")
    BigDecimal selectTotalOriginalValue();

    @Select("SELECT COALESCE(SUM(net_value), 0) FROM asset WHERE deleted = 0")
    BigDecimal selectTotalNetValue();

    @Select("SELECT COALESCE(SUM(accumulated_depreciation), 0) FROM asset WHERE deleted = 0")
    BigDecimal selectTotalAccumulatedDepreciation();

    @Select("SELECT status, COUNT(*) AS cnt, COALESCE(SUM(net_value), 0) AS net_value FROM asset WHERE deleted = 0 GROUP BY status ORDER BY cnt DESC")
    List<Map<String, Object>> selectStatusDistribution();

    @Select("SELECT id, asset_code, asset_name, department, keeper FROM asset WHERE deleted = 0 AND status = 'IDLE' AND updated_at < DATE_SUB(NOW(), INTERVAL 1 YEAR) ORDER BY updated_at ASC LIMIT 20")
    List<Map<String, Object>> selectIdleAssets();

    @Select("SELECT a.id, a.asset_code, a.asset_name, a.department, a.keeper, COUNT(r.id) AS repair_count FROM asset a JOIN asset_repair_order r ON r.asset_id = a.id WHERE a.deleted = 0 GROUP BY a.id HAVING COUNT(r.id) >= 3 ORDER BY repair_count DESC LIMIT 10")
    List<Map<String, Object>> selectFrequentRepairAssets();

    @Select("SELECT id, asset_code, asset_name, department, keeper FROM asset WHERE deleted = 0 AND status = 'INVENTORY_ABNORMAL' ORDER BY updated_at DESC LIMIT 20")
    List<Map<String, Object>> selectAbnormalAssets();

    @Select("SELECT a.id, a.asset_code, a.asset_name, a.department, a.useful_life, a.net_value, COUNT(r.id) AS repair_count FROM asset a JOIN asset_repair_order r ON r.asset_id = a.id WHERE a.deleted = 0 AND a.status = 'IN_USE' GROUP BY a.id HAVING COUNT(r.id) >= 2 ORDER BY repair_count DESC LIMIT 10")
    List<Map<String, Object>> selectRepairSuggestions();

    @Select("SELECT a.id, a.asset_code, a.asset_name, a.department, a.useful_life, a.net_value, 0 AS repair_count FROM asset a WHERE a.deleted = 0 AND a.status IN ('IN_USE', 'IDLE') AND (a.net_value <= a.original_value * 0.05 OR DATEDIFF(NOW(), a.purchase_date) / 365 >= a.useful_life) ORDER BY a.net_value ASC LIMIT 10")
    List<Map<String, Object>> selectScrapSuggestions();

    // ===== 新增：AI 上下文数据收集查询 =====

    @Select("SELECT COUNT(*) FROM asset WHERE deleted = 0 AND net_value <= original_value * 0.05 AND original_value > 0")
    Integer selectLowValueAssetCount();

    @Select("SELECT COUNT(*) FROM asset WHERE deleted = 0 AND useful_life > 0 AND DATEDIFF(NOW(), purchase_date) / 365 >= useful_life * 0.8")
    Integer selectNearEndOfLifeCount();

    @Select("SELECT COUNT(*) FROM asset WHERE deleted = 0 AND status = 'REPAIRING'")
    Integer selectRepairingCount();

    @Select("SELECT COUNT(*) FROM finance_sync_record WHERE status = 'FAILED'")
    Integer selectFinanceSyncFailCount();

    @Select("SELECT COUNT(*) FROM asset_operation_log WHERE DATE(operation_time) = CURDATE()")
    Integer selectTodayAuditLogCount();

    @Select("SELECT a.asset_code, a.asset_name, a.department, a.status, a.net_value, a.original_value, a.useful_life, " +
            "DATEDIFF(NOW(), a.purchase_date) / 365 AS used_years " +
            "FROM asset a WHERE a.deleted = 0 AND " +
            "(a.status = 'INVENTORY_ABNORMAL' OR a.net_value <= a.original_value * 0.05 OR " +
            "a.status = 'REPAIRING' OR (a.useful_life > 0 AND DATEDIFF(NOW(), a.purchase_date) / 365 >= a.useful_life)) " +
            "ORDER BY a.net_value ASC LIMIT 10")
    List<Map<String, Object>> selectTopRiskAssets();

    @Select("SELECT DATE_FORMAT(d.depreciation_date, '%Y-%m') AS month, " +
            "COALESCE(SUM(d.monthly_depreciation), 0) AS monthly_dep, " +
            "COALESCE(SUM(d.accumulated_depreciation), 0) AS acc_dep " +
            "FROM depreciation_record d WHERE d.depreciation_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(d.depreciation_date, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> selectMonthlyDepreciationTrend();
}
