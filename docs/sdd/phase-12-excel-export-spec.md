# Phase 12 - Excel 导出中心与核心报表导出 Spec

## 1. 背景

系统已完成资产台账、生命周期、审批中心、盘点闭环、折旧报表、财务模拟同步、预警中心和 AI 辅助分析模块，但缺少 Excel 导出能力。在真实业务场景中，资产管理员、财务人员、审计人员需要将报表下载为 Excel 文件用于留档、线下流转和数据交付。

## 2. 目标

1. 补齐核心业务数据的 Excel 导出能力
2. 覆盖 9 类导出场景：资产台账、资产时间线、审批记录、盘点任务、盘点明细、折旧报表、财务同步记录、预警列表、AI 分析报告
3. 提供通用 Excel 工具，统一样式和格式
4. 前端各页面接入导出按钮，一键下载

## 3. 用户价值

- **资产管理员**：导出资产台账、盘点任务/明细，便于线下核对和留档
- **财务人员**：导出折旧报表、财务同步记录，便于财务对账和审计
- **审计人员**：导出审批记录、预警列表、AI 分析报告，便于审计追踪

## 4. 做什么

- 后端添加 Apache POI 依赖（poi-ooxml 5.2.5）
- 后端新增通用 Excel 工具 ExcelExportUtil
- 后端新增 ExportController（9 个导出端点），复用现有 Service 查询方法
- 前端新增 download.ts（blob 下载工具）
- 前端新增 export.ts（导出 API 封装）
- 前端 8 个页面接入导出按钮

## 5. 不做什么

- 不修改登录/JWT
- 不修改审批核心逻辑
- 不修改生命周期状态流转
- 不修改盘点主链路
- 不修改财务同步主链路
- 不修改预警规则主链路
- 不修改 AI 分析核心逻辑
- 不修改资产新增/编辑/删除主流程
- 不做 PDF 导出
- 不做 Word 导出
- 不做 Excel 导入
- 不做复杂报表设计器
- 不新增数据库表
- 不新增数据库字段

## 6. 数据库复用说明

本阶段完全复用现有表和查询逻辑，不新增表、不新增字段。所有导出接口直接调用现有 Service 方法获取数据。

## 7. 接口清单

| 方法 | 路径 | 说明 | 复用 Service |
|------|------|------|-------------|
| GET | /api/export/assets | 导出资产台账 | AssetService.page |
| GET | /api/export/assets/{assetId}/timeline | 导出资产时间线 | AssetTimelineService.getTimeline |
| GET | /api/export/approval/records | 导出审批记录 | ApprovalService.donePage |
| GET | /api/export/inventory/tasks | 导出盘点任务 | InventoryService.page |
| GET | /api/export/inventory/tasks/{taskId}/records | 导出盘点明细 | InventoryService.getRecords |
| GET | /api/export/depreciation/report | 导出折旧报表 | DepreciationReportService.getSummary + monthlyItems |
| GET | /api/export/finance/sync/records | 导出财务同步记录 | FinanceService.syncRecords |
| GET | /api/export/warnings | 导出预警列表 | WarningService.getItems |
| GET | /api/export/ai/report | 导出 AI 分析报告 | AiAnalysisService.getReport |

## 8. 业务规则

1. 所有导出接口均为 GET 请求，返回 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 类型
2. 导出文件名使用 `Content-Disposition: attachment; filename=xxx.xlsx`
3. 资产台账导出：导出全部资产（pageSize=10000），支持按 status、department、keyword 筛选
4. 资产时间线导出：按 assetId 查询全部时间线事件
5. 审批记录导出：导出已办记录（donePage），pageSize=10000
6. 盘点任务导出：导出全部任务
7. 盘点明细导出：按 taskId 导出该任务下所有明细
8. 折旧报表导出：含总览汇总行 + 月度明细列表，month 参数默认当前月
9. 财务同步记录导出：导出全部同步记录
10. 预警列表导出：导出全部预警（无分页限制）
11. AI 报告导出：导出 AI 分析报告（summary + anomalyOverview + suggestionOverview）

## 9. Excel 格式规范

1. 标题行：合并单元格，字体加粗，居中，背景色浅蓝
2. 表头行：字体加粗，居中，背景色浅灰，带边框
3. 数据行：左对齐，金额字段右对齐并保留 2 位小数，日期字段格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
4. 列宽：自动适配内容宽度（或固定合理宽度）
5. 冻结表头：首行冻结

## 10. 验收标准

1. 后端 `mvn -DskipTests package` 构建成功
2. 前端 `npm run build` 构建成功
3. GET /api/export/assets 返回 xlsx 文件
4. GET /api/export/assets/1/timeline 返回 xlsx 文件
5. GET /api/export/approval/records 返回 xlsx 文件
6. GET /api/export/inventory/tasks 返回 xlsx 文件
7. GET /api/export/inventory/tasks/{taskId}/records 返回 xlsx 文件
8. GET /api/export/depreciation/report 返回 xlsx 文件
9. GET /api/export/finance/sync/records 返回 xlsx 文件
10. GET /api/export/warnings 返回 xlsx 文件
11. GET /api/export/ai/report 返回 xlsx 文件
12. 前端各页面显示导出按钮
13. 点击导出按钮能下载 xlsx 文件
14. 回归核心页面无白屏、无接口报错
