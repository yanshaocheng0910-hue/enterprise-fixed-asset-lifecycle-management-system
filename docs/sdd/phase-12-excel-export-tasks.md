# Phase 12 - Excel 导出中心与核心报表导出 Tasks

## T1 SDD 文档

- 新增 spec/design/tasks/acceptance 4 个文档
- 状态：✅ 完成

## T2 添加 Apache POI 依赖

- 修改 backend/pom.xml，新增 poi-ooxml 5.2.5
- 状态：待执行

## T3 后端 ExcelExportUtil 通用工具

- 新增 backend/src/main/java/com/example/asset/common/excel/ExcelExportUtil.java
- 实现 createWorkbook、writeTitle、writeHeader、writeRow、autoSizeColumns、writeToResponse
- 状态：待执行

## T4 后端 ExportController

- 新增 backend/src/main/java/com/example/asset/export/controller/ExportController.java
- 实现 9 个导出端点
- 复用现有 Service 查询方法
- 状态：待执行

## T5 前端下载工具和 API

- 新增 frontend/src/utils/download.ts（blob 下载工具）
- 新增 frontend/src/api/export.ts（9 个导出函数）
- 状态：待执行

## T6 前端页面接入导出按钮

- AssetList.vue、AssetDetail.vue、ApprovalTodo.vue、ApprovalDone.vue
- InventoryTask.vue、DepreciationReport.vue、FinanceSync.vue
- WarningCenter.vue、AiAnalysis.vue
- 状态：待执行

## T7 构建验收

- 后端：cd backend && mvn -DskipTests package
- 前端：cd frontend && npm run build
- 状态：待执行

## T8 API 验收 + 审计报告 + 提交

- 登录 admin/123456
- 测试 9 个导出接口
- 回归核心页面
- 输出审计报告
- git commit -m "feat(export): add excel export for core reports"
- git push origin main
- 状态：待执行
