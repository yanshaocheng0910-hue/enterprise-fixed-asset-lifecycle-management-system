# Phase 10 - 财务数据分析与模拟同步增强 Spec

## 1. 背景

系统已有资产台账（asset 表含 original_value、net_value、accumulated_depreciation、useful_life、residual_rate、purchase_date）、折旧记录（depreciation_record 表）、折旧报表模块（Phase 10 前半部分已实现 6 个分析接口）和财务同步基础模块（Phase 4 实现了 finance_sync_record 表和基础同步接口）。

但财务分析与模拟同步能力还不完整：
- DepreciationSummaryVO 缺少 monthlyDepreciation 字段（本月折旧额）
- 财务同步记录字段不足（缺少批次号、资产数、原值、净值、累计折旧、操作人）
- 同步接口路径不规范（/sync-depreciation 应为 /sync/depreciation）
- 缺少同步详情查询接口
- 前端财务同步页面功能简陋（缺少总览卡片、详情弹窗）

## 2. 目标

1. **折旧总览增强**：DepreciationSummaryVO 新增 monthlyDepreciation 字段
2. **财务模拟同步增强**：同步时记录完整的资产价值快照（资产数、原值、净值、累计折旧、本月折旧额、批次号、操作人）
3. **同步记录管理**：支持分页查询同步记录列表和查看同步详情
4. **前端财务同步页面增强**：总览卡片 + 同步按钮 + 记录表格 + 详情弹窗

## 3. 用户价值

- 帮助财务人员掌握资产价值变化和本月折旧压力
- 模拟与财务系统对接的数据流转过程
- 追踪历史同步记录，便于审计

## 4. 做什么

- 后端 DepreciationSummaryVO 新增 monthlyDepreciation 字段
- 后端新增 migration SQL 为 finance_sync_record 表补充字段
- 后端新增 FinanceSyncRecordVO
- 后端增强 FinanceService（同步时计算完整价值快照）
- 后端增强 FinanceSyncController（规范路径 + 新增详情接口）
- 前端增强 finance.ts API
- 前端增强 FinanceSync.vue 页面
- 前端微调 DepreciationReport.vue（显示本月折旧额）

## 5. 不做什么

- 不修改登录/JWT
- 不修改审批核心逻辑
- 不修改生命周期状态流转
- 不修改盘点任务主链路
- 不修改资产新增/编辑/删除主流程
- 不做真实外部财务系统对接
- 不调用第三方 API
- 不做 Excel/PDF 导出
- 不做 AI 分析
- 不新增数据库表（仅补充字段）

## 6. 数据库变更说明

### migration-v6-finance-enhance.sql

为 finance_sync_record 表补充字段：

| 新增字段 | 类型 | 说明 | 原因 |
|----------|------|------|------|
| sync_batch_no | VARCHAR(64) | 同步批次号 | 便于追踪每次同步操作 |
| asset_count | INT DEFAULT 0 | 资产数量 | 区别于 record_count（折旧记录数），记录实际资产数 |
| total_original_value | DECIMAL(18,2) DEFAULT 0 | 原值总额 | 同步时的资产原值快照 |
| total_net_value | DECIMAL(18,2) DEFAULT 0 | 净值总额 | 同步时的资产净值快照 |
| total_accumulated_depreciation | DECIMAL(18,2) DEFAULT 0 | 累计折旧总额 | 同步时的累计折旧快照 |
| monthly_depreciation | DECIMAL(18,2) DEFAULT 0 | 本月折旧额 | 区别于 total_amount，明确语义 |
| operator_name | VARCHAR(64) | 操作人 | 记录谁执行了同步 |

保留原有字段 total_amount、record_count 不变，保持向后兼容。

## 7. 接口清单

### 折旧分析（已有，仅增强 summary）

| 方法 | 路径 | 说明 | 变更 |
|------|------|------|------|
| GET | /api/depreciation/summary | 折旧总览 | 新增 monthlyDepreciation 字段 |
| GET | /api/depreciation/low-value-assets | 低净值资产 | 不变 |
| GET | /api/depreciation/near-end-assets | 接近报废资产 | 不变 |
| GET | /api/depreciation/statistics/department | 部门统计 | 不变 |
| GET | /api/depreciation/statistics/category | 分类统计 | 不变 |
| GET | /api/depreciation/trend | 月度趋势 | 不变 |

### 财务模拟同步

| 方法 | 路径 | 说明 | 变更 |
|------|------|------|------|
| POST | /api/finance/sync/depreciation | 模拟同步折旧数据 | 路径变更，增强返回数据 |
| GET | /api/finance/sync/records | 同步记录列表 | 路径变更 |
| GET | /api/finance/sync/records/{id} | 同步详情 | 新增 |

## 8. 业务规则

1. **模拟同步逻辑**：根据当前资产数据统计 syncMonth、assetCount、totalOriginalValue、totalNetValue、totalAccumulatedDepreciation、monthlyDepreciation，生成 syncBatchNo（格式：FS+yyyyMMddHHmmss），状态为 SUCCESS，备注写"模拟同步成功，未调用外部财务系统"
2. **幂等性**：同一月份重复同步时返回已有记录，不重复创建
3. **monthlyDepreciation 计算**：优先从 depreciation_record 表查询该月汇总，如果该月无记录则基于 asset 表计算（SUM(original_value * (1 - residual_rate) / (useful_life * 12))）
4. **操作人**：从 UserContext 获取

## 9. 验收标准

1. 后端 `mvn -DskipTests package` 构建成功
2. 前端 `npm run build` 构建成功
3. GET /api/depreciation/summary 返回 200，含 monthlyDepreciation 字段
4. POST /api/finance/sync/depreciation 返回 200，生成完整同步记录
5. GET /api/finance/sync/records 返回 200，分页列表正常
6. GET /api/finance/sync/records/{id} 返回 200，详情完整
7. 前端财务同步页面显示总览卡片、同步按钮、记录表格、详情弹窗
8. 回归：资产台账、审批中心、盘点任务、资产时间线正常
