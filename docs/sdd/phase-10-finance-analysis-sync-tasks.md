# Phase 10 - 财务数据分析与模拟同步增强 Tasks

## T1 SDD 文档

- 新增 spec/design/tasks/acceptance 4 个文档
- 状态：✅ 完成

## T2 后端财务分析增强

- DepreciationSummaryVO 新增 monthlyDepreciation 字段
- DepreciationReportService.getSummary() 计算 monthlyDepreciation
- 状态：待执行

## T3 后端模拟同步增强

- 新增 migration-v6-finance-enhance.sql
- FinanceSyncRecord entity 新增 7 个字段
- 新增 FinanceSyncRecordVO
- FinanceService 增强 syncDepreciation + 新增 getSyncDetail
- FinanceSyncController 规范路径 + 新增详情端点
- 状态：待执行

## T4 前端 API 封装

- finance.ts 重写：syncDepreciationData、getFinanceSyncRecords、getFinanceSyncDetail
- 状态：待执行

## T5 折旧报表页面微调

- DepreciationReport.vue 新增本月折旧额卡片
- 状态：待执行

## T6 财务同步页面增强

- FinanceSync.vue 重写：总览卡片 + 同步按钮 + 记录表格 + 详情弹窗
- 状态：待执行

## T7 构建验收

- 后端：cd backend && mvn -DskipTests package
- 前端：cd frontend && npm run build
- 状态：待执行

## T8 API 验收 + 审计报告 + 提交

- 登录 admin/123456
- 测试 3 个财务同步接口
- 回归：assets/page、approval/todo/page、inventory/tasks/page、assets/1/timeline
- 输出审计报告
- git commit -m "feat(finance): enhance financial analysis and simulated sync"
- git push origin main
- 状态：待执行
