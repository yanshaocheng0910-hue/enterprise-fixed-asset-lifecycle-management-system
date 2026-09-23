package com.example.asset.warning.service;

import com.example.asset.common.PageResult;
import com.example.asset.warning.mapper.WarningMapper;
import com.example.asset.warning.vo.WarningItemVO;
import com.example.asset.warning.vo.WarningSummaryVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarningService {

    // ===== 预警阈值常量（集中管理，避免魔法数字散落） =====
    // 低净值资产阈值 net_value/original_value <= 0.2、接近使用年限剩余月数 < 6、
    // 盘点正常结果 NORMAL、财务同步成功状态 SUCCESS 已在 WarningMapper.xml SQL 中固化
    private static final int IDLE_LONG_TIME_DAYS = 90;
    private static final int REPAIR_OVERDUE_DAYS = 30;

    // ===== 预警等级 =====
    private static final String LEVEL_HIGH = "HIGH";
    private static final String LEVEL_MEDIUM = "MEDIUM";
    private static final String LEVEL_LOW = "LOW";

    // ===== 预警类型 =====
    public static final String TYPE_LOW_VALUE = "LOW_VALUE";
    public static final String TYPE_NEAR_END = "NEAR_END";
    public static final String TYPE_IDLE_LONG_TIME = "IDLE_LONG_TIME";
    public static final String TYPE_REPAIR_OVERDUE = "REPAIR_OVERDUE";
    public static final String TYPE_INVENTORY_ABNORMAL = "INVENTORY_ABNORMAL";
    public static final String TYPE_FINANCE_SYNC_ABNORMAL = "FINANCE_SYNC_ABNORMAL";

    // ===== 业务来源 =====
    private static final String SOURCE_ASSET = "ASSET";
    private static final String SOURCE_REPAIR = "REPAIR";
    private static final String SOURCE_INVENTORY = "INVENTORY";
    private static final String SOURCE_FINANCE = "FINANCE";
    private static final String SOURCE_SYSTEM = "SYSTEM";

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WarningMapper warningMapper;

    public WarningService(WarningMapper warningMapper) {
        this.warningMapper = warningMapper;
    }

    /**
     * 获取预警总览
     */
    public WarningSummaryVO getSummary() {
        WarningSummaryVO vo = new WarningSummaryVO();
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DATETIME_FMT);

        List<WarningItemVO> allItems = collectAllWarnings(now, nowStr);

        int lowValueCount = countByType(allItems, TYPE_LOW_VALUE);
        int nearEndCount = countByType(allItems, TYPE_NEAR_END);
        int idleLongTimeCount = countByType(allItems, TYPE_IDLE_LONG_TIME);
        int repairOverdueCount = countByType(allItems, TYPE_REPAIR_OVERDUE);
        int inventoryAbnormalCount = countByType(allItems, TYPE_INVENTORY_ABNORMAL);
        int financeSyncAbnormalCount = countByType(allItems, TYPE_FINANCE_SYNC_ABNORMAL);

        vo.setLowValueCount(lowValueCount);
        vo.setNearEndCount(nearEndCount);
        vo.setIdleLongTimeCount(idleLongTimeCount);
        vo.setRepairOverdueCount(repairOverdueCount);
        vo.setInventoryAbnormalCount(inventoryAbnormalCount);
        vo.setFinanceSyncAbnormalCount(financeSyncAbnormalCount);

        vo.setTotalWarningCount(allItems.size());
        vo.setHighWarningCount(repairOverdueCount + inventoryAbnormalCount + financeSyncAbnormalCount);
        vo.setMediumWarningCount(nearEndCount + idleLongTimeCount);
        vo.setLowWarningCount(lowValueCount);

        return vo;
    }

    /**
     * 获取预警列表（支持按类型和等级筛选，内存分页）
     */
    public PageResult<WarningItemVO> getItems(String type, String level, Integer pageNum, Integer pageSize) {
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DATETIME_FMT);

        List<WarningItemVO> allItems = collectAllWarnings(now, nowStr);

        // 按类型筛选
        List<WarningItemVO> filtered = allItems.stream()
                .filter(item -> type == null || type.isEmpty() || type.equals(item.getWarningType()))
                .filter(item -> level == null || level.isEmpty() || level.equals(item.getWarningLevel()))
                .collect(Collectors.toList());

        // 内存分页
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int total = filtered.size();
        int fromIndex = Math.min((current - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<WarningItemVO> pageRecords = filtered.subList(fromIndex, toIndex);

        return new PageResult<>(pageRecords, (long) total, (long) current, (long) size);
    }

    /**
     * 收集全部预警（6 类）
     */
    private List<WarningItemVO> collectAllWarnings(LocalDateTime now, String nowStr) {
        List<WarningItemVO> all = new ArrayList<>();

        // 1. 低净值资产
        List<WarningItemVO> lowValue = warningMapper.selectLowValueAssets();
        decorate(lowValue, TYPE_LOW_VALUE, "低净值资产", LEVEL_LOW, null, SOURCE_ASSET, nowStr);
        all.addAll(lowValue);

        // 2. 接近使用年限
        List<WarningItemVO> nearEnd = warningMapper.selectNearEndAssets();
        decorate(nearEnd, TYPE_NEAR_END, "接近使用年限", LEVEL_MEDIUM, null, SOURCE_ASSET, nowStr);
        all.addAll(nearEnd);

        // 3. 长期闲置
        LocalDateTime idleThreshold = now.minusDays(IDLE_LONG_TIME_DAYS);
        List<WarningItemVO> idleLongTime = warningMapper.selectIdleLongTimeAssets(idleThreshold);
        decorate(idleLongTime, TYPE_IDLE_LONG_TIME, "长期闲置", LEVEL_MEDIUM, null, SOURCE_ASSET, nowStr);
        all.addAll(idleLongTime);

        // 4. 维修超期
        LocalDate repairThreshold = now.toLocalDate().minusDays(REPAIR_OVERDUE_DAYS);
        List<WarningItemVO> repairOverdue = warningMapper.selectRepairOverdueAssets(repairThreshold);
        decorate(repairOverdue, TYPE_REPAIR_OVERDUE, "维修超期", LEVEL_HIGH, "REPAIR", SOURCE_REPAIR, nowStr);
        all.addAll(repairOverdue);

        // 5. 盘点异常
        List<WarningItemVO> inventoryAbnormal = warningMapper.selectInventoryAbnormalRecords();
        decorate(inventoryAbnormal, TYPE_INVENTORY_ABNORMAL, "盘点异常", LEVEL_HIGH, "INVENTORY", SOURCE_INVENTORY, nowStr);
        all.addAll(inventoryAbnormal);

        // 6. 财务同步异常
        List<WarningItemVO> financeAbnormal = warningMapper.selectFinanceSyncAbnormalRecords();
        decorate(financeAbnormal, TYPE_FINANCE_SYNC_ABNORMAL, "财务同步异常", LEVEL_HIGH, "FINANCE", SOURCE_FINANCE, nowStr);
        all.addAll(financeAbnormal);

        // 6.1 当月无 SUCCESS 同步记录时追加"本月未同步"预警
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int monthSuccessCount = warningMapper.countCurrentMonthSuccessSync(currentMonth);
        if (monthSuccessCount == 0) {
            WarningItemVO missingSync = new WarningItemVO();
            missingSync.setId(0L);
            missingSync.setWarningType(TYPE_FINANCE_SYNC_ABNORMAL);
            missingSync.setWarningTypeName("财务同步异常");
            missingSync.setWarningLevel(LEVEL_HIGH);
            missingSync.setTitle("本月未同步：" + currentMonth);
            missingSync.setDescription("当前月份 " + currentMonth + " 尚无成功的财务同步记录");
            missingSync.setAssetId(null);
            missingSync.setAssetCode(null);
            missingSync.setAssetName(null);
            missingSync.setBusinessId(null);
            missingSync.setBusinessType("FINANCE");
            missingSync.setSource(SOURCE_SYSTEM);
            missingSync.setCreatedAt(nowStr);
            missingSync.setSuggestion("建议尽快执行本月财务模拟同步");
            all.add(missingSync);
        }

        return all;
    }

    /**
     * 批量补充预警元信息
     */
    private void decorate(List<WarningItemVO> items, String type, String typeName,
                          String level, String businessType, String source, String createdAt) {
        for (WarningItemVO item : items) {
            item.setWarningType(type);
            item.setWarningTypeName(typeName);
            item.setWarningLevel(level);
            if (businessType != null) {
                item.setBusinessType(businessType);
            } else {
                item.setBusinessType("ASSET");
            }
            item.setSource(source);
            item.setCreatedAt(createdAt);
        }
    }

    private int countByType(List<WarningItemVO> items, String type) {
        return (int) items.stream().filter(i -> type.equals(i.getWarningType())).count();
    }
}
