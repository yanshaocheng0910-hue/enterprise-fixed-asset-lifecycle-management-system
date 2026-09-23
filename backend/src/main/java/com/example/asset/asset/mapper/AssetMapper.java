package com.example.asset.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.asset.dto.AssetQueryRequest;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.vo.AssetDetailVO;
import com.example.asset.asset.vo.AssetPageVO;
import com.example.asset.dashboard.vo.DashboardStatsVO;
import com.example.asset.dashboard.vo.DepartmentRankingVO;
import com.example.asset.dashboard.vo.NameValueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    IPage<AssetPageVO> selectAssetPage(Page<AssetPageVO> page, @Param("query") AssetQueryRequest query);

    AssetDetailVO selectAssetDetail(@Param("id") Long id);

    @Select("SELECT MAX(asset_code) FROM asset WHERE asset_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxAssetCodeByPrefix(@Param("prefix") String prefix);

    @Select("""
            SELECT
                COUNT(*) AS assetCount,
                COALESCE(SUM(original_value), 0) AS totalOriginalValue,
                COALESCE(SUM(accumulated_depreciation), 0) AS totalAccumulatedDepreciation,
                COALESCE(SUM(net_value), 0) AS totalNetValue,
                COALESCE(SUM(CASE WHEN status = 'IN_USE' THEN 1 ELSE 0 END), 0) AS inUseCount,
                COALESCE(SUM(CASE WHEN status = 'IDLE' THEN 1 ELSE 0 END), 0) AS idleCount,
                COALESCE(SUM(CASE WHEN status = 'REPAIRING' THEN 1 ELSE 0 END), 0) AS repairingCount,
                COALESCE(SUM(CASE WHEN status = 'WAITING_SCRAP' THEN 1 ELSE 0 END), 0) AS waitingScrapCount
            FROM asset
            WHERE deleted = 0
            """)
    DashboardStatsVO selectDashboardStats();

    @Select("""
            SELECT c.category_name AS name, COUNT(a.id) AS value
            FROM asset_category c
            LEFT JOIN asset a ON a.category_id = c.id AND a.deleted = 0
            GROUP BY c.id, c.category_name
            ORDER BY value DESC, c.id ASC
            """)
    List<NameValueVO> selectCategoryDistribution();

    @Select("""
            SELECT department, COALESCE(SUM(original_value), 0) AS amount
            FROM asset
            WHERE deleted = 0
            GROUP BY department
            ORDER BY amount DESC, department ASC
            """)
    List<DepartmentRankingVO> selectDepartmentRanking();
}
