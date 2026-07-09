package com.example.asset.depreciation.service;

import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.depreciation.mapper.DepreciationRecordMapper;
import com.example.asset.depreciation.mapper.DepreciationReportMapper;
import com.example.asset.depreciation.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepreciationReportService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DepreciationReportMapper depreciationReportMapper;
    private final DepreciationRecordMapper depreciationRecordMapper;
    private final AssetMapper assetMapper;

    public DepreciationReportService(DepreciationReportMapper depreciationReportMapper,
                                     DepreciationRecordMapper depreciationRecordMapper,
                                     AssetMapper assetMapper) {
        this.depreciationReportMapper = depreciationReportMapper;
        this.depreciationRecordMapper = depreciationRecordMapper;
        this.assetMapper = assetMapper;
    }

    public MonthlyReportSummaryVO monthlySummary(String month) {
        MonthlyReportSummaryVO summary = new MonthlyReportSummaryVO();
        summary.setMonth(month);

        // 从资产表获取当前有效资产的汇总数据
        List<MonthlyDepreciationItemVO> items = depreciationReportMapper.selectMonthlyDepreciationItems(month);

        BigDecimal monthlyTotal = BigDecimal.ZERO;
        BigDecimal accumulatedTotal = BigDecimal.ZERO;
        BigDecimal originalTotal = BigDecimal.ZERO;
        BigDecimal netTotal = BigDecimal.ZERO;

        for (MonthlyDepreciationItemVO item : items) {
            if (item.getMonthlyDepreciation() != null) {
                monthlyTotal = monthlyTotal.add(item.getMonthlyDepreciation());
            }
            if (item.getAccumulatedDepreciation() != null) {
                accumulatedTotal = accumulatedTotal.add(item.getAccumulatedDepreciation());
            }
            if (item.getOriginalValue() != null) {
                originalTotal = originalTotal.add(item.getOriginalValue());
            }
            if (item.getNetValue() != null) {
                netTotal = netTotal.add(item.getNetValue());
            }
        }

        summary.setMonthlyDepreciationTotal(monthlyTotal);
        summary.setAccumulatedDepreciationTotal(accumulatedTotal);
        summary.setOriginalValueTotal(originalTotal);
        summary.setNetValueTotal(netTotal);
        summary.setTotalAssetCount(items.size());
        summary.setDepreciatingAssetCount((int) items.stream()
                .filter(i -> i.getMonthlyDepreciation() != null && i.getMonthlyDepreciation().compareTo(BigDecimal.ZERO) > 0)
                .count());

        return summary;
    }

    public List<MonthlyDepreciationItemVO> monthlyItems(String month) {
        return depreciationReportMapper.selectMonthlyDepreciationItems(month);
    }

    public List<DepartmentStatVO> departmentStats() {
        return depreciationReportMapper.selectDepartmentStats();
    }

    public List<CategoryStatVO> categoryStats() {
        return depreciationReportMapper.selectCategoryStats();
    }

    public List<DepreciationTrendVO> trend() {
        // 生成最近 12 个月的月份列表
        List<String> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            months.add(current.minusMonths(i).format(MONTH_FMT));
        }

        // 优先从 depreciation_record 表获取真实数据
        List<DepreciationTrendVO> dbTrend = depreciationReportMapper.selectDepreciationTrend(months);
        Map<String, DepreciationTrendVO> dbMap = dbTrend.stream()
                .collect(Collectors.toMap(DepreciationTrendVO::getMonth, v -> v));

        // 构建完整的 12 个月趋势（缺失月份用计算值填充）
        List<MonthlyDepreciationItemVO> items = depreciationReportMapper.selectMonthlyDepreciationItems(months.get(months.size() - 1));
        BigDecimal currentMonthlyTotal = BigDecimal.ZERO;
        for (MonthlyDepreciationItemVO item : items) {
            if (item.getMonthlyDepreciation() != null) {
                currentMonthlyTotal = currentMonthlyTotal.add(item.getMonthlyDepreciation());
            }
        }

        BigDecimal totalOriginal = BigDecimal.ZERO;
        BigDecimal totalAccumulated = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        for (MonthlyDepreciationItemVO item : items) {
            if (item.getOriginalValue() != null) totalOriginal = totalOriginal.add(item.getOriginalValue());
            if (item.getAccumulatedDepreciation() != null) totalAccumulated = totalAccumulated.add(item.getAccumulatedDepreciation());
            if (item.getNetValue() != null) totalNet = totalNet.add(item.getNetValue());
        }

        List<DepreciationTrendVO> result = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            String m = months.get(i);
            DepreciationTrendVO existing = dbMap.get(m);
            if (existing != null) {
                result.add(existing);
            } else {
                // 根据当前数据估算历史趋势
                int monthsAgo = months.size() - 1 - i;
                BigDecimal factor = BigDecimal.valueOf(monthsAgo).multiply(currentMonthlyTotal);
                DepreciationTrendVO point = new DepreciationTrendVO();
                point.setMonth(m);
                point.setMonthlyDepreciation(currentMonthlyTotal.setScale(2, RoundingMode.HALF_UP));
                point.setAccumulatedDepreciation(totalAccumulated.subtract(factor).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
                point.setNetValue(totalNet.add(factor).setScale(2, RoundingMode.HALF_UP));
                result.add(point);
            }
        }

        return result;
    }

    public DepreciationSummaryVO getSummary() {
        DepreciationSummaryVO summary = depreciationReportMapper.selectDepreciationSummary();
        if (summary == null) {
            summary = new DepreciationSummaryVO();
            summary.setAssetCount(0);
            summary.setTotalOriginalValue(BigDecimal.ZERO);
            summary.setTotalNetValue(BigDecimal.ZERO);
            summary.setTotalAccumulatedDepreciation(BigDecimal.ZERO);
        }
        // 计算平均折旧率
        if (summary.getTotalOriginalValue() != null
                && summary.getTotalOriginalValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = summary.getTotalAccumulatedDepreciation()
                    .divide(summary.getTotalOriginalValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            summary.setAverageDepreciationRate(rate);
        } else {
            summary.setAverageDepreciationRate(BigDecimal.ZERO);
        }
        // 低净值资产数
        List<LowValueAssetVO> lowValueAssets = depreciationReportMapper.selectLowValueAssets();
        summary.setLowValueAssetCount(lowValueAssets.size());
        // 接近报废资产数
        List<NearEndAssetVO> nearEndAssets = depreciationReportMapper.selectNearEndAssets();
        summary.setNearEndAssetCount(nearEndAssets.size());
        return summary;
    }

    public List<LowValueAssetVO> getLowValueAssets() {
        return depreciationReportMapper.selectLowValueAssets();
    }

    public List<NearEndAssetVO> getNearEndAssets() {
        return depreciationReportMapper.selectNearEndAssets();
    }
}
