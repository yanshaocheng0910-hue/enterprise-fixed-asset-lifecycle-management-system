package com.example.asset.dashboard.service;

import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.dashboard.vo.DashboardStatsVO;
import com.example.asset.dashboard.vo.DepartmentRankingVO;
import com.example.asset.dashboard.vo.NameValueVO;
import com.example.asset.dashboard.vo.TrendPointVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AssetMapper assetMapper;

    public DashboardService(AssetMapper assetMapper) {
        this.assetMapper = assetMapper;
    }

    public DashboardStatsVO stats() {
        DashboardStatsVO statsVO = assetMapper.selectDashboardStats();
        if (statsVO.getTotalOriginalValue() == null) {
            statsVO.setTotalOriginalValue(BigDecimal.ZERO);
        }
        if (statsVO.getTotalAccumulatedDepreciation() == null) {
            statsVO.setTotalAccumulatedDepreciation(BigDecimal.ZERO);
        }
        if (statsVO.getTotalNetValue() == null) {
            statsVO.setTotalNetValue(BigDecimal.ZERO);
        }
        return statsVO;
    }

    public List<NameValueVO> categoryDistribution() {
        return assetMapper.selectCategoryDistribution();
    }

    public List<DepartmentRankingVO> departmentRanking() {
        return assetMapper.selectDepartmentRanking();
    }

    public List<TrendPointVO> depreciationTrend() {
        List<TrendPointVO> points = new ArrayList<>();
        BigDecimal base = BigDecimal.valueOf(12.5);
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            BigDecimal value = base.add(BigDecimal.valueOf((5 - i) * 1.8)).multiply(BigDecimal.valueOf(1000));
            points.add(new TrendPointVO(month.format(MONTH_FORMATTER), value));
        }
        return points;
    }
}
