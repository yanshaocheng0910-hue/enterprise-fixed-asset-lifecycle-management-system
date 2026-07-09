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
}
