package com.example.asset.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.common.PageResult;
import com.example.asset.context.UserContext;
import com.example.asset.depreciation.mapper.DepreciationReportMapper;
import com.example.asset.depreciation.vo.DepreciationSummaryVO;
import com.example.asset.finance.entity.FinanceSyncRecord;
import com.example.asset.finance.mapper.FinanceSyncRecordMapper;
import com.example.asset.finance.vo.FinanceSyncRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private final DepreciationReportMapper depreciationReportMapper;
    private final FinanceSyncRecordMapper financeSyncRecordMapper;

    public FinanceService(DepreciationReportMapper depreciationReportMapper,
                          FinanceSyncRecordMapper financeSyncRecordMapper) {
        this.depreciationReportMapper = depreciationReportMapper;
        this.financeSyncRecordMapper = financeSyncRecordMapper;
    }

    @Transactional
    public FinanceSyncRecordVO syncDepreciation(String month) {
        // 校验月份格式
        YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));

        // 检查是否已同步
        FinanceSyncRecord existing = financeSyncRecordMapper.selectByMonth(month);
        if (existing != null) {
            return toVO(existing);
        }

        // 查询资产价值快照
        DepreciationSummaryVO summary = depreciationReportMapper.selectDepreciationSummary();
        BigDecimal monthlyDep = depreciationReportMapper.selectComputedMonthlyDepreciation();
        if (monthlyDep == null) {
            monthlyDep = BigDecimal.ZERO;
        }
        monthlyDep = monthlyDep.setScale(2, RoundingMode.HALF_UP);

        // 生成同步记录
        FinanceSyncRecord record = new FinanceSyncRecord();
        record.setSyncMonth(month);
        record.setSyncBatchNo("FS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setTotalAmount(monthlyDep); // 兼容原有字段
        record.setRecordCount(summary != null ? summary.getAssetCount() : 0);
        record.setAssetCount(summary != null ? summary.getAssetCount() : 0);
        record.setTotalOriginalValue(summary != null ? summary.getTotalOriginalValue() : BigDecimal.ZERO);
        record.setTotalNetValue(summary != null ? summary.getTotalNetValue() : BigDecimal.ZERO);
        record.setTotalAccumulatedDepreciation(summary != null ? summary.getTotalAccumulatedDepreciation() : BigDecimal.ZERO);
        record.setMonthlyDepreciation(monthlyDep);
        record.setStatus("SUCCESS");
        record.setOperatorName(UserContext.getUsername() != null ? UserContext.getUsername() : "system");
        record.setRemark("模拟同步成功，未调用外部财务系统");
        record.setCreatedAt(LocalDateTime.now());

        financeSyncRecordMapper.insert(record);
        return toVO(record);
    }

    public PageResult<FinanceSyncRecordVO> syncRecords(Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        LambdaQueryWrapper<FinanceSyncRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FinanceSyncRecord::getCreatedAt);
        Page<FinanceSyncRecord> page = new Page<>(current, size);
        Page<FinanceSyncRecord> result = financeSyncRecordMapper.selectPage(page, wrapper);
        List<FinanceSyncRecordVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public FinanceSyncRecordVO getSyncDetail(Long id) {
        FinanceSyncRecord record = financeSyncRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        return toVO(record);
    }

    private FinanceSyncRecordVO toVO(FinanceSyncRecord record) {
        FinanceSyncRecordVO vo = new FinanceSyncRecordVO();
        vo.setId(record.getId());
        vo.setSyncBatchNo(record.getSyncBatchNo());
        vo.setSyncMonth(record.getSyncMonth());
        vo.setAssetCount(record.getAssetCount());
        vo.setTotalOriginalValue(record.getTotalOriginalValue());
        vo.setTotalNetValue(record.getTotalNetValue());
        vo.setTotalAccumulatedDepreciation(record.getTotalAccumulatedDepreciation());
        vo.setMonthlyDepreciation(record.getMonthlyDepreciation());
        vo.setStatus(record.getStatus());
        vo.setOperatorName(record.getOperatorName());
        vo.setSyncTime(record.getCreatedAt() != null
                ? record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null);
        vo.setRemark(record.getRemark());
        return vo;
    }
}
