package com.example.asset.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.common.PageResult;
import com.example.asset.depreciation.mapper.DepreciationRecordMapper;
import com.example.asset.finance.entity.FinanceSyncRecord;
import com.example.asset.finance.mapper.FinanceSyncRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FinanceService {

    private final DepreciationRecordMapper depreciationRecordMapper;
    private final FinanceSyncRecordMapper financeSyncRecordMapper;

    public FinanceService(DepreciationRecordMapper depreciationRecordMapper,
                          FinanceSyncRecordMapper financeSyncRecordMapper) {
        this.depreciationRecordMapper = depreciationRecordMapper;
        this.financeSyncRecordMapper = financeSyncRecordMapper;
    }

    @Transactional
    public Map<String, Object> syncDepreciation(String month) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 校验月份格式
        try {
            YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "月份格式错误，请使用 YYYY-MM 格式");
            return result;
        }

        // 检查是否已同步
        FinanceSyncRecord existing = financeSyncRecordMapper.selectByMonth(month);
        if (existing != null) {
            result.put("success", true);
            result.put("message", "已是最新数据");
            result.put("syncMonth", existing.getSyncMonth());
            result.put("totalAmount", existing.getTotalAmount());
            result.put("recordCount", existing.getRecordCount());
            result.put("syncTime", existing.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return result;
        }

        // 查询该月折旧数据
        BigDecimal totalAmount = depreciationRecordMapper.sumMonthlyDepreciationByMonth(month);
        Integer recordCount = depreciationRecordMapper.countByMonth(month);

        if (recordCount == null || recordCount == 0) {
            result.put("success", false);
            result.put("message", "该月份暂无折旧数据，请先计提折旧");
            return result;
        }

        // 写入同步记录
        FinanceSyncRecord syncRecord = new FinanceSyncRecord();
        syncRecord.setSyncMonth(month);
        syncRecord.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        syncRecord.setRecordCount(recordCount);
        syncRecord.setStatus("SUCCESS");
        syncRecord.setRemark("本月折旧数据已同步至财务系统");
        syncRecord.setCreatedAt(LocalDateTime.now());
        financeSyncRecordMapper.insert(syncRecord);

        result.put("success", true);
        result.put("message", "同步成功");
        result.put("syncMonth", month);
        result.put("totalAmount", syncRecord.getTotalAmount());
        result.put("recordCount", syncRecord.getRecordCount());
        result.put("syncTime", syncRecord.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    public PageResult<FinanceSyncRecord> syncRecords(Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        LambdaQueryWrapper<FinanceSyncRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FinanceSyncRecord::getCreatedAt);
        Page<FinanceSyncRecord> page = new Page<>(current, size);
        Page<FinanceSyncRecord> result = financeSyncRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }
}