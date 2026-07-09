package com.example.asset.export.controller;

import com.example.asset.ai.service.AiAnalysisService;
import com.example.asset.ai.vo.AiReportVO;
import com.example.asset.approval.dto.ApprovalPageRequest;
import com.example.asset.approval.service.ApprovalService;
import com.example.asset.approval.vo.ApprovalDoneVO;
import com.example.asset.asset.dto.AssetQueryRequest;
import com.example.asset.asset.dto.AssetTimelineQueryRequest;
import com.example.asset.asset.service.AssetService;
import com.example.asset.asset.service.AssetTimelineService;
import com.example.asset.asset.vo.AssetPageVO;
import com.example.asset.asset.vo.AssetTimelineEventVO;
import com.example.asset.common.PageResult;
import com.example.asset.common.excel.ExcelExportUtil;
import com.example.asset.depreciation.service.DepreciationReportService;
import com.example.asset.depreciation.vo.DepreciationSummaryVO;
import com.example.asset.depreciation.vo.MonthlyDepreciationItemVO;
import com.example.asset.finance.service.FinanceService;
import com.example.asset.finance.vo.FinanceSyncRecordVO;
import com.example.asset.inventory.dto.InventoryTaskQueryRequest;
import com.example.asset.inventory.service.InventoryService;
import com.example.asset.inventory.vo.InventoryRecordVO;
import com.example.asset.inventory.vo.InventoryTaskVO;
import com.example.asset.warning.service.WarningService;
import com.example.asset.warning.vo.WarningItemVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final AssetService assetService;
    private final AssetTimelineService assetTimelineService;
    private final ApprovalService approvalService;
    private final InventoryService inventoryService;
    private final DepreciationReportService depreciationReportService;
    private final FinanceService financeService;
    private final WarningService warningService;
    private final AiAnalysisService aiAnalysisService;

    public ExportController(AssetService assetService,
                            AssetTimelineService assetTimelineService,
                            ApprovalService approvalService,
                            InventoryService inventoryService,
                            DepreciationReportService depreciationReportService,
                            FinanceService financeService,
                            WarningService warningService,
                            AiAnalysisService aiAnalysisService) {
        this.assetService = assetService;
        this.assetTimelineService = assetTimelineService;
        this.approvalService = approvalService;
        this.inventoryService = inventoryService;
        this.depreciationReportService = depreciationReportService;
        this.financeService = financeService;
        this.warningService = warningService;
        this.aiAnalysisService = aiAnalysisService;
    }

    // ===== 1. 导出资产台账 =====
    @GetMapping("/assets")
    public void exportAssets(HttpServletResponse response,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String department,
                             @RequestParam(required = false) String keyword) {
        AssetQueryRequest query = new AssetQueryRequest();
        query.setStatus(status);
        query.setDepartment(department);
        query.setAssetName(keyword);
        query.setPageNum(1L);
        query.setPageSize(10000L);

        PageResult<AssetPageVO> pageResult = assetService.page(query);
        List<AssetPageVO> list = pageResult.getRecords();

        String[] headers = {"资产编号", "资产名称", "分类", "品牌", "规格", "部门", "保管人", "存放地点", "原值", "净值", "状态", "购置日期"};
        int[] widths = {18, 22, 14, 14, 18, 14, 12, 18, 14, 14, 12, 14};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("资产台账");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "资产台账", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            AssetPageVO a = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    a.getAssetCode(), a.getAssetName(), a.getCategoryName(), a.getBrand(),
                    a.getSpecification(), a.getDepartment(), a.getKeeper(), a.getLocation(),
                    a.getOriginalValue(), a.getNetValue(), a.getStatus(), a.getPurchaseDate()
            });
        }
        ExcelExportUtil.writeToResponse(response, "资产台账.xlsx", wb);
    }

    // ===== 2. 导出资产时间线 =====
    @GetMapping("/assets/{assetId}/timeline")
    public void exportAssetTimeline(HttpServletResponse response, @PathVariable Long assetId) {
        List<AssetTimelineEventVO> list = assetTimelineService.getTimeline(assetId, new AssetTimelineQueryRequest());

        String[] headers = {"事件类型", "标题", "单据编号", "业务类型", "变更前状态", "变更后状态", "事件时间", "来源", "备注"};
        int[] widths = {14, 28, 18, 12, 14, 14, 20, 10, 24};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("资产时间线");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "资产时间线（资产ID：" + assetId + "）", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            AssetTimelineEventVO e = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    e.getEventTypeName(), e.getTitle(), e.getOrderCode(), e.getBusinessType(),
                    e.getBeforeStatus(), e.getAfterStatus(), e.getEventTime(), e.getSource(), e.getRemark()
            });
        }
        ExcelExportUtil.writeToResponse(response, "资产时间线.xlsx", wb);
    }

    // ===== 3. 导出审批记录 =====
    @GetMapping("/approval/records")
    public void exportApprovalRecords(HttpServletResponse response) {
        ApprovalPageRequest req = new ApprovalPageRequest();
        req.setPageNum(1L);
        req.setPageSize(10000L);

        PageResult<ApprovalDoneVO> pageResult = approvalService.donePage(req);
        List<ApprovalDoneVO> list = pageResult.getRecords();

        String[] headers = {"实例ID", "业务类型", "单据编号", "资产编号", "资产名称", "审批动作", "审批意见", "状态", "审批人", "审批时间"};
        int[] widths = {12, 14, 18, 18, 22, 10, 28, 12, 12, 20};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("审批记录");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "审批记录", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            ApprovalDoneVO d = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    d.getInstanceId(), d.getBusinessType(), d.getOrderCode(), d.getAssetCode(),
                    d.getAssetName(), d.getAction(), d.getComment(), d.getStatus(),
                    d.getApproverName(), d.getApprovedAt()
            });
        }
        ExcelExportUtil.writeToResponse(response, "审批记录.xlsx", wb);
    }

    // ===== 4. 导出盘点任务 =====
    @GetMapping("/inventory/tasks")
    public void exportInventoryTasks(HttpServletResponse response) {
        InventoryTaskQueryRequest query = new InventoryTaskQueryRequest();
        query.setPageNum(1L);
        query.setPageSize(10000L);

        PageResult<InventoryTaskVO> pageResult = inventoryService.page(query);
        List<InventoryTaskVO> list = pageResult.getRecords();

        String[] headers = {"任务编号", "任务名称", "范围类型", "部门", "地点", "状态", "开始时间", "结束时间", "明细总数", "已完成数"};
        int[] widths = {18, 24, 12, 14, 18, 12, 20, 20, 10, 10};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("盘点任务");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "盘点任务列表", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            InventoryTaskVO t = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    t.getTaskCode(), t.getTaskName(), t.getScopeType(), t.getDepartment(),
                    t.getLocation(), t.getStatus(), t.getStartTime(), t.getEndTime(),
                    t.getTotalRecords(), t.getCompletedRecords()
            });
        }
        ExcelExportUtil.writeToResponse(response, "盘点任务.xlsx", wb);
    }

    // ===== 5. 导出盘点明细 =====
    @GetMapping("/inventory/tasks/{taskId}/records")
    public void exportInventoryTaskRecords(HttpServletResponse response, @PathVariable Long taskId) {
        List<InventoryRecordVO> list = inventoryService.getRecords(taskId);

        String[] headers = {"资产编号", "资产名称", "分类", "应在地点", "实际地点", "应在保管人", "实际保管人", "盘点结果", "扫码时间", "备注"};
        int[] widths = {18, 22, 14, 18, 18, 14, 14, 12, 20, 24};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("盘点明细");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "盘点明细（任务ID：" + taskId + "）", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            InventoryRecordVO r = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    r.getAssetCode(), r.getAssetName(), r.getCategoryName(),
                    r.getExpectedLocation(), r.getActualLocation(),
                    r.getExpectedKeeper(), r.getActualKeeper(),
                    r.getResult(), r.getScannedAt(), r.getRemark()
            });
        }
        ExcelExportUtil.writeToResponse(response, "盘点明细.xlsx", wb);
    }

    // ===== 6. 导出折旧报表 =====
    @GetMapping("/depreciation/report")
    public void exportDepreciationReport(HttpServletResponse response,
                                         @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        DepreciationSummaryVO summary = depreciationReportService.getSummary();
        List<MonthlyDepreciationItemVO> items = depreciationReportService.monthlyItems(month);

        String[] headers = {"资产编号", "资产名称", "分类", "部门", "保管人", "购置日期", "原值", "月折旧额", "累计折旧", "净值", "状态"};
        int[] widths = {18, 22, 14, 14, 12, 14, 14, 14, 14, 14, 12};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("折旧报表");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "折旧报表（月份：" + month + "）", headers.length);

        // 总览区域（第2-4行）
        ExcelExportUtil.writeRow(sheet, 1, new Object[]{"资产总数", summary.getAssetCount()});
        ExcelExportUtil.writeRow(sheet, 2, new Object[]{"原值总额（元）", summary.getTotalOriginalValue()});
        ExcelExportUtil.writeRow(sheet, 3, new Object[]{"净值总额（元）", summary.getTotalNetValue()});
        ExcelExportUtil.writeRow(sheet, 4, new Object[]{"累计折旧（元）", summary.getTotalAccumulatedDepreciation()});
        ExcelExportUtil.writeRow(sheet, 5, new Object[]{"本月折旧额（元）", summary.getMonthlyDepreciation()});
        ExcelExportUtil.writeRow(sheet, 6, new Object[]{"平均折旧率", summary.getAverageDepreciationRate()});

        // 明细表头从第8行开始
        int headerRow = 8;
        ExcelExportUtil.writeHeader(sheet, headers, headerRow);
        ExcelExportUtil.freezeHeader(sheet, headerRow);

        for (int i = 0; i < items.size(); i++) {
            MonthlyDepreciationItemVO item = items.get(i);
            ExcelExportUtil.writeRow(sheet, headerRow + 1 + i, new Object[]{
                    item.getAssetCode(), item.getAssetName(), item.getCategoryName(),
                    item.getDepartment(), item.getKeeper(), item.getPurchaseDate(),
                    item.getOriginalValue(), item.getMonthlyDepreciation(),
                    item.getAccumulatedDepreciation(), item.getNetValue(), item.getStatus()
            });
        }
        ExcelExportUtil.writeToResponse(response, "折旧报表_" + month + ".xlsx", wb);
    }

    // ===== 7. 导出财务同步记录 =====
    @GetMapping("/finance/sync/records")
    public void exportFinanceSyncRecords(HttpServletResponse response) {
        PageResult<FinanceSyncRecordVO> pageResult = financeService.syncRecords(1L, 10000L);
        List<FinanceSyncRecordVO> list = pageResult.getRecords();

        String[] headers = {"批次号", "同步月份", "资产数量", "原值总额", "净值总额", "累计折旧", "本月折旧额", "状态", "操作人", "同步时间"};
        int[] widths = {20, 12, 10, 16, 16, 16, 16, 10, 12, 20};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("财务同步记录");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "财务同步记录", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            FinanceSyncRecordVO r = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    r.getSyncBatchNo(), r.getSyncMonth(), r.getAssetCount(),
                    r.getTotalOriginalValue(), r.getTotalNetValue(),
                    r.getTotalAccumulatedDepreciation(), r.getMonthlyDepreciation(),
                    r.getStatus(), r.getOperatorName(), r.getSyncTime()
            });
        }
        ExcelExportUtil.writeToResponse(response, "财务同步记录.xlsx", wb);
    }

    // ===== 8. 导出预警列表 =====
    @GetMapping("/warnings")
    public void exportWarnings(HttpServletResponse response,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String level) {
        PageResult<WarningItemVO> pageResult = warningService.getItems(type, level, 1, 10000);
        List<WarningItemVO> list = pageResult.getRecords();

        String[] headers = {"预警类型", "等级", "标题", "描述", "资产编号", "资产名称", "生成时间", "处置建议"};
        int[] widths = {14, 8, 28, 36, 18, 22, 20, 28};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("预警列表");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "预警列表", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        for (int i = 0; i < list.size(); i++) {
            WarningItemVO w = list.get(i);
            ExcelExportUtil.writeRow(sheet, i + 2, new Object[]{
                    w.getWarningTypeName(), w.getWarningLevel(), w.getTitle(),
                    w.getDescription(), w.getAssetCode(), w.getAssetName(),
                    w.getCreatedAt(), w.getSuggestion()
            });
        }
        ExcelExportUtil.writeToResponse(response, "预警列表.xlsx", wb);
    }

    // ===== 9. 导出 AI 分析报告 =====
    @GetMapping("/ai/report")
    public void exportAiReport(HttpServletResponse response) {
        AiReportVO report = aiAnalysisService.getReport();

        String[] headers = {"项目", "内容"};
        int[] widths = {20, 80};

        SXSSFWorkbook wb = ExcelExportUtil.createWorkbook();
        Sheet sheet = wb.createSheet("AI分析报告");
        ExcelExportUtil.setColumnWidths(sheet, widths);
        ExcelExportUtil.writeTitle(sheet, "AI 辅助分析报告", headers.length);
        ExcelExportUtil.writeHeader(sheet, headers, 1);
        ExcelExportUtil.freezeHeader(sheet, 1);

        int row = 2;
        ExcelExportUtil.writeRow(sheet, row++, new Object[]{"生成时间", report.getGeneratedAt()});
        ExcelExportUtil.writeRow(sheet, row++, new Object[]{"总览摘要", report.getSummary()});
        ExcelExportUtil.writeRow(sheet, row++, new Object[]{"异常概览", report.getAnomalyOverview()});
        ExcelExportUtil.writeRow(sheet, row++, new Object[]{"建议概览", report.getSuggestionOverview()});

        ExcelExportUtil.writeToResponse(response, "AI分析报告.xlsx", wb);
    }
}
