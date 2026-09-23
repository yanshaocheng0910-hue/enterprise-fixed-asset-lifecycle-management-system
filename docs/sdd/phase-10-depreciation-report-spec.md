# Phase 10 - 折旧报表与资产价值分析 Spec

## 1. 背景

当前系统已有资产台账（asset 表含 original_value、net_value、accumulated_depreciation、useful_life、residual_rate、purchase_date、department、category_id）和折旧记录（depreciation_record 表），折旧报表模块（Phase 6）已实现月度报表汇总、月度折旧明细、部门统计、分类统计和 12 个月趋势。

但折旧报表仍偏骨架，缺少财务分析视角：
- 缺少全局折旧总览（不分月的整体资产价值概览）
- 缺少低净值资产识别（净值率极低、即将报废的资产）
- 缺少接近使用年限资产识别（剩余使用时间不足的资产）
- 前端缺少可视化图表（部门/分类价值对比图）和风险资产列表

## 2. 目标

1. **折旧总览**：提供不分月的全局资产价值概览，包括资产总数、原值总额、净值总额、累计折旧总额、平均折旧率、低净值资产数、接近报废资产数
2. **低净值资产列表**：识别净值率低于 10% 的资产（已折旧超过 90%）
3. **接近使用年限资产列表**：识别剩余使用月数不足 12 个月的资产
4. **前端报表增强**：指标卡 + 部门/分类价值柱状图 + 月度趋势图 + 低净值/接近报废资产表格

## 3. 用户价值

- 帮助财务人员掌握资产整体价值分布和折旧压力
- 帮助资产管理员识别需要关注的风险资产（低净值、接近报废）
- 为资产报废决策提供数据支撑

## 4. 做什么

- 后端新增 3 个接口：`/api/depreciation/summary`、`/api/depreciation/low-value-assets`、`/api/depreciation/near-end-assets`
- 后端新增 3 个 VO：`DepreciationSummaryVO`、`LowValueAssetVO`、`NearEndAssetVO`
- 后端在 `DepreciationReportMapper` 新增 3 个查询方法
- 后端在 `DepreciationReportService` 新增 3 个业务方法
- 前端新增 3 个 API 函数
- 前端增强 `DepreciationReport.vue`：指标卡、柱状图、趋势图、风险资产表格

## 5. 不做什么

- 不修改登录/JWT
- 不修改审批核心逻辑
- 不修改生命周期状态流转
- 不修改盘点任务主链路
- 不修改资产新增/编辑/删除主流程
- 不做 AI 分析
- 不做财务系统真实对接
- 不做导出 Excel/PDF
- 不新增数据库表/字段
- 不新增可视化依赖（复用已有 ECharts / vue-echarts）

## 6. 数据库复用

复用现有表，不新增表/字段/migration：

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| asset | 资产台账 | original_value, net_value, accumulated_depreciation, useful_life, residual_rate, purchase_date, department, category_id, status, deleted |
| asset_category | 资产分类 | id, category_name |
| depreciation_record | 折旧记录 | asset_id, depreciation_month, monthly_depreciation, accumulated_depreciation, net_value |

## 7. 接口清单

### 新增接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/depreciation/summary | 折旧总览（全局资产价值概览） |
| GET | /api/depreciation/low-value-assets | 低净值资产列表（净值率 < 10%） |
| GET | /api/depreciation/near-end-assets | 接近使用年限资产列表（剩余月数 < 12） |

### 已有接口（保留不变）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/depreciation/report/summary?month= | 月度报表汇总 |
| GET | /api/depreciation/report/monthly?month= | 月度折旧明细 |
| GET | /api/depreciation/statistics/department | 部门统计 |
| GET | /api/depreciation/statistics/category | 分类统计 |
| GET | /api/depreciation/trend | 12 个月趋势 |

## 8. 业务规则

1. **低净值资产定义**：`net_value / original_value < 0.1`（净值率低于 10%），且 original_value > 0，且 status != 'SCRAPPED'
2. **接近使用年限资产定义**：剩余使用月数 < 12，即 `(useful_life * 12) - 已使用月数 < 12`，其中已使用月数 = `PERIOD_DIFF(DATE_FORMAT(NOW(), '%Y%m'), DATE_FORMAT(purchase_date, '%Y%m'))`，且 status != 'SCRAPPED'
3. **平均折旧率**：`SUM(accumulated_depreciation) / SUM(original_value) * 100`，结果保留 2 位小数
4. **统计范围**：仅统计 `deleted = 0` 且 `status != 'SCRAPPED'` 的资产
5. **净值率**：`net_value / original_value`，结果保留 4 位小数

## 9. 验收标准

1. 后端 `mvn -DskipTests package` 构建成功
2. 前端 `npm run build` 构建成功
3. GET /api/depreciation/summary 返回 200，数据含 7 个字段
4. GET /api/depreciation/low-value-assets 返回 200，列表数据正确
5. GET /api/depreciation/near-end-assets 返回 200，列表数据正确
6. 前端折旧报表页面显示指标卡、柱状图、趋势图、风险资产表格
7. 回归：资产台账 /api/assets/page 正常
8. 回归：审批中心 /api/approval/todo/page 正常
9. 回归：盘点任务 /api/inventory/tasks/page 正常
10. 回归：资产时间线 /api/assets/1/timeline 正常
