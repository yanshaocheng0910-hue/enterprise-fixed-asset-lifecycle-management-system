package com.example.asset.audit.service;

import com.example.asset.audit.dto.AuditLogQueryRequest;
import com.example.asset.audit.mapper.AuditMapper;
import com.example.asset.audit.vo.AuditLogVO;
import com.example.asset.audit.vo.AuditSummaryVO;
import com.example.asset.common.BusinessException;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计日志 Service
 * 参考 WarningService 的「多源收集 + 内存筛选 + 内存分页」模式
 */
@Service
public class AuditService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 日志类型常量
    public static final String TYPE_ASSET_OPERATION = "ASSET_OPERATION";
    public static final String TYPE_APPROVAL = "APPROVAL";
    public static final String TYPE_INVENTORY_ABNORMAL = "INVENTORY_ABNORMAL";
    public static final String TYPE_FINANCE_SYNC = "FINANCE_SYNC";

    private final AuditMapper auditMapper;

    public AuditService(AuditMapper auditMapper) {
        this.auditMapper = auditMapper;
    }

    /**
     * 分页查询审计日志（多源合并 + 内存筛选 + 内存分页）
     */
    public PageResult<AuditLogVO> page(AuditLogQueryRequest req) {
        LocalDate startDate = parseDate(req.getStartDate());
        LocalDate endDate = parseDate(req.getEndDate());

        List<AuditLogVO> all = collectAll(startDate, endDate);

        // 内存筛选
        List<AuditLogVO> filtered = all.stream()
                .filter(item -> isEmpty(req.getLogType()) || req.getLogType().equals(item.getLogType()))
                .filter(item -> isEmpty(req.getAssetCode())
                        || (item.getAssetCode() != null && item.getAssetCode().toLowerCase().contains(req.getAssetCode().toLowerCase())))
                .filter(item -> isEmpty(req.getAssetName())
                        || (item.getAssetName() != null && item.getAssetName().toLowerCase().contains(req.getAssetName().toLowerCase())))
                .filter(item -> isEmpty(req.getOperatorName())
                        || (item.getOperatorName() != null && item.getOperatorName().toLowerCase().contains(req.getOperatorName().toLowerCase())))
                .sorted(Comparator.comparing(AuditLogVO::getOperationTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 内存分页
        long pageNum = req.getPageNum() == null ? 1L : req.getPageNum();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();
        int total = filtered.size();
        int fromIndex = (int) Math.min((pageNum - 1) * pageSize, total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        List<AuditLogVO> pageRecords = filtered.subList(fromIndex, toIndex);

        return new PageResult<>(pageRecords, (long) total, pageNum, pageSize);
    }

    /**
     * 审计统计
     */
    public AuditSummaryVO summary() {
        AuditSummaryVO vo = new AuditSummaryVO();
        vo.setAssetChangeCount(auditMapper.countAssetOperationLogs());
        vo.setApprovalOperationCount(auditMapper.countApprovalRecords());
        vo.setInventoryAbnormalCount(auditMapper.countInventoryAbnormal());
        vo.setFinanceSyncCount(auditMapper.countFinanceSyncRecords());
        vo.setTodayOperationCount(auditMapper.countTodayOperations(LocalDate.now()));
        return vo;
    }

    /**
     * 日志详情（复合 ID 解析）
     */
    public AuditLogVO getDetail(String id) {
        if (id == null || id.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "日志ID不能为空");
        }

        int dashIndex = id.indexOf('-');
        if (dashIndex <= 0 || dashIndex >= id.length() - 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "日志ID格式错误：" + id);
        }

        String prefix = id.substring(0, dashIndex);
        LocalDate todayStart = LocalDate.now();

        List<AuditLogVO> list;
        switch (prefix) {
            case "ASSET":
                list = auditMapper.selectAssetOperationLogs(null, null);
                break;
            case "APPROVAL":
                list = auditMapper.selectApprovalAuditLogs(null, null);
                break;
            case "INVENTORY":
                list = auditMapper.selectInventoryAbnormalLogs(null, null);
                break;
            case "FINANCE":
                list = auditMapper.selectFinanceSyncLogs(null, null);
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的日志来源：" + prefix);
        }

        return list.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "日志不存在：" + id));
    }

    /**
     * 获取全量审计日志（用于导出，最多 10000 条）
     */
    public List<AuditLogVO> listForExport(AuditLogQueryRequest req) {
        Long originalPageSize = req.getPageSize();
        req.setPageSize(10000L);
        PageResult<AuditLogVO> pageResult = page(req);
        req.setPageSize(originalPageSize);
        return pageResult.getRecords();
    }

    // ======================== Private ========================

    private List<AuditLogVO> collectAll(LocalDate startDate, LocalDate endDate) {
        List<AuditLogVO> all = new ArrayList<>();
        all.addAll(auditMapper.selectAssetOperationLogs(startDate, endDate));
        all.addAll(auditMapper.selectApprovalAuditLogs(startDate, endDate));
        all.addAll(auditMapper.selectInventoryAbnormalLogs(startDate, endDate));
        all.addAll(auditMapper.selectFinanceSyncLogs(startDate, endDate));
        return all;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
