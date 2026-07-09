# Phase 12 - Excel 导出中心与核心报表导出 Design

## 1. 依赖变更

### pom.xml 新增 Apache POI

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## 2. 后端模块结构

```
backend/src/main/java/com/example/asset/
├── common/
│   └── excel/
│       └── ExcelExportUtil.java          # 通用 Excel 工具
└── export/
    └── controller/
        └── ExportController.java          # 导出控制器
```

## 3. ExcelExportUtil 设计

通用工具类，提供静态方法：

```java
public class ExcelExportUtil {

    // 创建 workbook
    public static SXSSFWorkbook createWorkbook();

    // 写入标题行（合并单元格）
    public static void writeTitle(Sheet sheet, String title, int columnCount);

    // 写入表头行
    public static void writeHeader(Sheet sheet, String[] headers, int headerRowIndex);

    // 写入数据行（支持 String、BigDecimal、Date、LocalDate、LocalDateTime）
    public static void writeRow(Sheet sheet, int rowIndex, Object[] values);

    // 设置列宽
    public static void autoSizeColumns(Sheet sheet, int columnCount);

    // 写入 HttpServletResponse
    public static void writeToResponse(HttpServletResponse response, String filename, Workbook workbook);

    // 创建单元格样式
    public static CellStyle createTitleStyle(Workbook wb);
    public static CellStyle createHeaderStyle(Workbook wb);
    public static CellStyle createDateStyle(Workbook wb);
    public static CellStyle createMoneyStyle(Workbook wb);
}
```

### 样式规范

- 标题样式：字体 Microsoft YaHei 14pt 加粗，居中，背景色 #4472C4，白色字体
- 表头样式：字体 11pt 加粗，居中，背景色 #D9E2F3，带边框
- 数据样式：字体 11pt，左对齐，带边框
- 金额样式：字体 11pt，右对齐，格式 #,##0.00
- 日期样式：字体 11pt，居中，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss

## 4. ExportController 设计

```java
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

    @GetMapping("/assets")
    public void exportAssets(HttpServletResponse response, 
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String keyword);

    @GetMapping("/assets/{assetId}/timeline")
    public void exportAssetTimeline(HttpServletResponse response, @PathVariable Long assetId);

    @GetMapping("/approval/records")
    public void exportApprovalRecords(HttpServletResponse response);

    @GetMapping("/inventory/tasks")
    public void exportInventoryTasks(HttpServletResponse response);

    @GetMapping("/inventory/tasks/{taskId}/records")
    public void exportInventoryTaskRecords(HttpServletResponse response, @PathVariable Long taskId);

    @GetMapping("/depreciation/report")
    public void exportDepreciationReport(HttpServletResponse response,
            @RequestParam(defaultValue = "当前月") String month);

    @GetMapping("/finance/sync/records")
    public void exportFinanceSyncRecords(HttpServletResponse response);

    @GetMapping("/warnings")
    public void exportWarnings(HttpServletResponse response,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level);

    @GetMapping("/ai/report")
    public void exportAiReport(HttpServletResponse response);
}
```

## 5. 各导出接口数据映射

### 5.1 资产台账导出

复用 `AssetService.page(query)`，设 pageSize=10000。

| Excel 列 | VO 字段 |
|-----------|---------|
| 资产编号 | assetCode |
| 资产名称 | assetName |
| 分类 | categoryName |
| 品牌 | brand |
| 规格 | specification |
| 部门 | department |
| 保管人 | keeper |
| 存放地点 | location |
| 原值 | originalValue |
| 净值 | netValue |
| 状态 | status |
| 购置日期 | purchaseDate |

### 5.2 资产时间线导出

复用 `AssetTimelineService.getTimeline(assetId, query)`。

| Excel 列 | VO 字段 |
|-----------|---------|
| 事件类型 | eventTypeName |
| 标题 | title |
| 单据编号 | orderCode |
| 业务类型 | businessType |
| 变更前状态 | beforeStatus |
| 变更后状态 | afterStatus |
| 事件时间 | eventTime |
| 来源 | source |
| 备注 | remark |

### 5.3 审批记录导出

复用 `ApprovalService.donePage(req)`，设 pageSize=10000。

| Excel 列 | VO 字段 |
|-----------|---------|
| 实例ID | instanceId |
| 业务类型 | businessType |
| 单据编号 | orderCode |
| 资产编号 | assetCode |
| 资产名称 | assetName |
| 审批动作 | action |
| 审批意见 | comment |
| 状态 | status |
| 审批人 | approverName |
| 审批时间 | approvedAt |

### 5.4 盘点任务导出

复用 `InventoryService.page(query)`，设 pageSize=10000。

| Excel 列 | VO 字段 |
|-----------|---------|
| 任务编号 | taskCode |
| 任务名称 | taskName |
| 范围类型 | scopeType |
| 部门 | department |
| 地点 | location |
| 状态 | status |
| 开始时间 | startTime |
| 结束时间 | endTime |
| 明细总数 | totalRecords |
| 已完成数 | completedRecords |

### 5.5 盘点明细导出

复用 `InventoryService.getRecords(taskId)`。

| Excel 列 | VO 字段 |
|-----------|---------|
| 资产编号 | assetCode |
| 资产名称 | assetName |
| 分类 | categoryName |
| 应在地点 | expectedLocation |
| 实际地点 | actualLocation |
| 应在保管人 | expectedKeeper |
| 实际保管人 | actualKeeper |
| 盘点结果 | result |
| 扫码时间 | scannedAt |
| 备注 | remark |

### 5.6 折旧报表导出

复用 `DepreciationReportService.getSummary()` + `monthlyItems(month)`。

Sheet 结构：
- 标题行：折旧报表（月份：yyyy-MM）
- 总览区域：资产总数、原值总额、净值总额、累计折旧、本月折旧额、平均折旧率
- 空行
- 明细表头 + 数据行

| 明细列 | VO 字段 |
|--------|---------|
| 资产编号 | assetCode |
| 资产名称 | assetName |
| 分类 | categoryName |
| 部门 | department |
| 保管人 | keeper |
| 购置日期 | purchaseDate |
| 原值 | originalValue |
| 月折旧额 | monthlyDepreciation |
| 累计折旧 | accumulatedDepreciation |
| 净值 | netValue |
| 状态 | status |

### 5.7 财务同步记录导出

复用 `FinanceService.syncRecords(1, 10000)`。

| Excel 列 | VO 字段 |
|-----------|---------|
| 批次号 | syncBatchNo |
| 同步月份 | syncMonth |
| 资产数量 | assetCount |
| 原值总额 | totalOriginalValue |
| 净值总额 | totalNetValue |
| 累计折旧 | totalAccumulatedDepreciation |
| 本月折旧额 | monthlyDepreciation |
| 状态 | status |
| 操作人 | operatorName |
| 同步时间 | syncTime |

### 5.8 预警列表导出

复用 `WarningService.getItems(type, level, 1, 10000)`。

| Excel 列 | VO 字段 |
|-----------|---------|
| 预警类型 | warningTypeName |
| 等级 | warningLevel |
| 标题 | title |
| 描述 | description |
| 资产编号 | assetCode |
| 资产名称 | assetName |
| 生成时间 | createdAt |
| 处置建议 | suggestion |

### 5.9 AI 分析报告导出

复用 `AiAnalysisService.getReport()`。

Sheet 结构：
- 标题行：AI 辅助分析报告
- 生成时间
- 总览摘要（totalCount、totalOriginalValue、totalNetValue）
- 异常概览文本
- 建议概览文本

由于 AI 报告结构为文本型概览，导出为单 sheet 文本格式。

## 6. 前端 API 设计

### frontend/src/utils/download.ts

```typescript
import axios from 'axios'
import { getToken } from '@/utils/token'

const downloadRequest = axios.create({
  baseURL: '/api',
  timeout: 60000,
  responseType: 'blob'
})

downloadRequest.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

downloadRequest.interceptors.response.use(
  (response) => {
    // 从 Content-Disposition 提取文件名
    const disposition = response.headers['content-disposition'] || ''
    let filename = 'export.xlsx'
    const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/i)
    if (match) filename = decodeURIComponent(match[1].replace(/["']/g, ''))
    // 创建下载链接
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    return response
  },
  (error) => Promise.reject(error)
)

export function download(url: string, params?: Record<string, any>) {
  return downloadRequest.get(url, { params })
}
```

### frontend/src/api/export.ts

```typescript
import { download } from '@/utils/download'

export function exportAssets(params?: { status?: string; department?: string; keyword?: string }) {
  return download('/export/assets', params)
}
export function exportAssetTimeline(assetId: number) {
  return download(`/export/assets/${assetId}/timeline`)
}
export function exportApprovalRecords() {
  return download('/export/approval/records')
}
export function exportInventoryTasks() {
  return download('/export/inventory/tasks')
}
export function exportInventoryTaskRecords(taskId: number) {
  return download(`/export/inventory/tasks/${taskId}/records`)
}
export function exportDepreciationReport(month?: string) {
  return download('/export/depreciation/report', { month })
}
export function exportFinanceSyncRecords() {
  return download('/export/finance/sync/records')
}
export function exportWarnings(params?: { type?: string; level?: string }) {
  return download('/export/warnings', params)
}
export function exportAiReport() {
  return download('/export/ai/report')
}
```

## 7. 前端页面按钮接入

| 页面 | 按钮位置 | 按钮文案 | 调用函数 |
|------|----------|----------|----------|
| AssetList.vue | 工具栏 | 导出 Excel | exportAssets(当前筛选) |
| AssetDetail.vue | 时间线区域标题旁 | 导出时间线 | exportAssetTimeline(id) |
| ApprovalTodo.vue | 工具栏 | 导出审批记录 | exportApprovalRecords() |
| ApprovalDone.vue | 工具栏 | 导出审批记录 | exportApprovalRecords() |
| InventoryTask.vue | 工具栏 | 导出任务列表 | exportInventoryTasks() |
| InventoryTask.vue | 明细弹窗 | 导出明细 | exportInventoryTaskRecords(taskId) |
| DepreciationReport.vue | 月份选择器旁 | 导出报表 | exportDepreciationReport(month) |
| FinanceSync.vue | 操作区域 | 导出记录 | exportFinanceSyncRecords() |
| WarningCenter.vue | 筛选区域 | 导出预警 | exportWarnings(当前筛选) |
| AiAnalysis.vue | 报告区域 | 导出报告 | exportAiReport() |

## 8. 文件清单

### 新增文件（7）

- docs/sdd/phase-12-excel-export-spec.md
- docs/sdd/phase-12-excel-export-design.md
- docs/sdd/phase-12-excel-export-tasks.md
- docs/sdd/phase-12-excel-export-acceptance.md
- backend/src/main/java/com/example/asset/common/excel/ExcelExportUtil.java
- backend/src/main/java/com/example/asset/export/controller/ExportController.java
- frontend/src/utils/download.ts
- frontend/src/api/export.ts

### 修改文件（8）

- backend/pom.xml（+poi-ooxml 依赖）
- frontend/src/views/asset/AssetList.vue（+导出按钮）
- frontend/src/views/asset/AssetDetail.vue（+导出时间线按钮）
- frontend/src/views/approval/ApprovalTodo.vue（+导出按钮）
- frontend/src/views/approval/ApprovalDone.vue（+导出按钮）
- frontend/src/views/inventory/InventoryTask.vue（+导出任务/明细按钮）
- frontend/src/views/depreciation/DepreciationReport.vue（+导出报表按钮）
- frontend/src/views/finance/FinanceSync.vue（+导出记录按钮）
- frontend/src/views/warning/WarningCenter.vue（+导出预警按钮）
- frontend/src/views/ai/AiAnalysis.vue（+导出报告按钮）
