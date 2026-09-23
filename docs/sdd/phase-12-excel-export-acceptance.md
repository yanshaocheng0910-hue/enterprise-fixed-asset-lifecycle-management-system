# Phase 12 - Excel 导出中心与核心报表导出 Acceptance

## 1. 构建验收

- [ ] 后端 `mvn -DskipTests package` 构建成功
- [ ] 前端 `npm run build` 构建成功

## 2. 后端导出接口验收

- [ ] GET /api/export/assets 返回 200，Content-Type 为 xlsx
- [ ] GET /api/export/assets/1/timeline 返回 200
- [ ] GET /api/export/approval/records 返回 200
- [ ] GET /api/export/inventory/tasks 返回 200
- [ ] GET /api/export/inventory/tasks/1/records 返回 200
- [ ] GET /api/export/depreciation/report 返回 200
- [ ] GET /api/export/finance/sync/records 返回 200
- [ ] GET /api/export/warnings 返回 200
- [ ] GET /api/export/ai/report 返回 200

## 3. 前端页面验收

- [ ] AssetList.vue 显示"导出 Excel"按钮
- [ ] AssetDetail.vue 时间线区域显示"导出时间线"按钮
- [ ] ApprovalTodo.vue 显示"导出审批记录"按钮
- [ ] ApprovalDone.vue 显示"导出审批记录"按钮
- [ ] InventoryTask.vue 显示"导出任务列表"和"导出明细"按钮
- [ ] DepreciationReport.vue 显示"导出报表"按钮
- [ ] FinanceSync.vue 显示"导出记录"按钮
- [ ] WarningCenter.vue 显示"导出预警"按钮
- [ ] AiAnalysis.vue 显示"导出报告"按钮

## 4. 回归验收

- [ ] 资产台账页面正常
- [ ] 审批中心页面正常
- [ ] 盘点任务页面正常
- [ ] 折旧报表页面正常
- [ ] 财务同步页面正常
- [ ] 预警中心页面正常
- [ ] AI 分析页面正常

## 5. 数据库验收

- [ ] 未新增数据库表
- [ ] 未新增数据库字段
- [ ] 未修改核心业务逻辑

## 6. 审计报告清单

- 是否新增数据库表（必须为否）
- 是否新增字段（必须为否）
- 是否修改核心业务逻辑（必须为否）
- 新增文件列表
- 修改文件列表
- 后端构建结果
- 前端构建结果
- Excel 导出验收结果
- git diff --stat
- git status --short
