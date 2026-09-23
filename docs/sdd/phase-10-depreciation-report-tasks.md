# Phase 10 - 折旧报表与资产价值分析 Tasks

## T1 SDD 文档

- 新增 spec/design/tasks/acceptance 4 个文档
- 状态：✅ 完成

## T2 后端统计接口

- 新增 VO：DepreciationSummaryVO、LowValueAssetVO、NearEndAssetVO
- 修改 DepreciationReportMapper.java 新增 3 个方法声明
- 修改 DepreciationReportMapper.xml 新增 3 条 SQL
- 修改 DepreciationReportService.java 新增 getSummary/getLowValueAssets/getNearEndAssets
- 修改 DepreciationReportController.java 新增 3 个端点
- 状态：待执行

## T3 前端 API 封装

- 修改 frontend/src/api/depreciation.ts 新增 getDepreciationSummary/getLowValueAssets/getNearEndAssets
- 状态：待执行

## T4 折旧报表页面增强

- 修改 frontend/src/views/depreciation/DepreciationReport.vue
- 顶部 6 个指标卡（使用新 /summary 接口）
- 中部 2 个柱状图（部门/分类价值统计）
- 月度折旧趋势图（保留现有）
- 底部新增 2 个 Tab：低净值资产、接近使用年限资产
- 状态：待执行

## T5 构建验收

- 后端：cd backend && mvn -DskipTests package
- 前端：cd frontend && npm run build
- 状态：待执行

## T6 API 端到端验收

- 登录 admin/123456
- GET /api/depreciation/summary
- GET /api/depreciation/low-value-assets
- GET /api/depreciation/near-end-assets
- 回归：assets/page、approval/todo/page、inventory/tasks/page、assets/1/timeline
- 状态：待执行

## T7 审计报告 + 提交

- 输出审计报告
- git add 相关文件
- git commit -m "feat(depreciation): enhance depreciation report analytics"
- git push origin main
- 状态：待执行
