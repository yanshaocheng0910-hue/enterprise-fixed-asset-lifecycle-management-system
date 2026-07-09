# Phase 14 - 全局操作日志与审计页面 需求规格 (Spec)

## 1. 背景

固定资产生命周期管理系统已完成资产台账、生命周期（入库/领用/调拨/维修/报废）、审批中心、资产时间线、盘点闭环、折旧财务、财务模拟同步、预警中心、AI 辅助分析、Excel 导出和演示数据增强（Phase 1-13）。系统已具备完整业务闭环和丰富的演示数据（140 资产、84 生命周期单据、35 审批记录、105 盘点明细、6 财务同步、169 预警）。

**当前痛点**：系统关键操作记录分散在 `asset_operation_log`、`approval_record`、`inventory_record`、`finance_sync_record` 等多张表中，缺少统一审计入口。审计人员和管理员需要分别进入资产详情、审批中心、盘点任务、财务同步等多个页面才能拼凑出完整的操作轨迹，难以快速定位异常操作和进行整体追溯。

## 2. 目标

新增一个「全局操作日志与审计页面」，集中展示系统关键操作记录，提升系统的审计追溯能力：

- 集中展示资产操作、审批操作、盘点异常、财务同步等关键行为
- 支持按日志类型、资产编号、资产名称、操作人、时间范围多维度筛选
- 提供审计统计卡片（今日操作数、资产变更数、审批操作数、盘点异常数、财务同步数）
- 支持查看日志详情和跳转资产详情
- 支持导出审计日志 Excel

## 3. 用户价值

- **审计人员**：在一个页面即可查看全系统关键操作，快速追踪资产变更、定位异常操作、导出审计记录用于合规检查
- **系统管理员**：实时掌握系统操作活跃度，识别高风险操作和异常行为
- **业务追溯**：任一资产变更都能在审计日志中找到操作人、操作时间、变更前后状态和来源

## 4. 数据来源（只读，复用现有表）

| 日志类型 | 数据源表 | 筛选/转换规则 |
|---|---|---|
| 资产操作 (ASSET_OPERATION) | `asset_operation_log` | 全量，JOIN `asset` 取资产编号/名称 |
| 审批操作 (APPROVAL) | `approval_record` JOIN `approval_instance` | 全量审批动作（SUBMIT/APPROVED/REJECTED），通过 instance.business_type/business_id 关联生命周期单据取资产 |
| 盘点异常 (INVENTORY_ABNORMAL) | `inventory_record` | `result != 'NORMAL'`，JOIN `asset` 和 `inventory_task` |
| 财务同步 (FINANCE_SYNC) | `finance_sync_record` | 全量同步记录 |

**约束**：不新增日志表，不写死 mock 数据，不同来源统一转换成 `AuditLogVO` 返回。

## 5. 功能需求

### 5.1 审计统计卡片
- 今日操作数（今日 00:00 至现在的操作总数）
- 资产变更数（asset_operation_log 总数）
- 审批操作数（approval_record 总数）
- 盘点异常数（inventory_record 中 result != 'NORMAL' 总数）
- 财务同步数（finance_sync_record 总数）

### 5.2 审计日志分页查询
- 接口：`GET /api/audit/logs/page`
- 筛选条件：logType、assetCode、assetName、operatorName、dateRange（开始/结束日期）
- 排序：按操作时间倒序
- 分页：pageNum / pageSize

### 5.3 审计统计
- 接口：`GET /api/audit/logs/summary`
- 返回 `AuditSummaryVO`

### 5.4 日志详情
- 接口：`GET /api/audit/logs/{id}`
- 注：由于日志来源于多张表，id 采用「来源前缀+原ID」复合编码（如 `ASSET-123`、`APPROVAL-456`、`INVENTORY-789`、`FINANCE-101`）。详情接口解析前缀后回查原表返回完整 `AuditLogVO`。

### 5.5 导出审计日志 Excel
- 接口：`GET /api/export/audit/logs`
- 导出当前筛选条件下的全量审计日志（最多 10000 条）

## 6. AuditLogVO 字段

| 字段 | 类型 | 说明 |
|---|---|---|
| id | String | 复合 ID（来源前缀+原ID） |
| logType | String | 日志类型：ASSET_OPERATION/APPROVAL/INVENTORY_ABNORMAL/FINANCE_SYNC |
| logTypeName | String | 类型中文名 |
| assetId | Long | 资产 ID（财务同步可为空） |
| assetCode | String | 资产编号 |
| assetName | String | 资产名称 |
| businessType | String | 业务类型（如 RECEIVE/TRANSFER/REPAIR/SCRAP/FINANCE） |
| businessId | Long | 业务单据 ID |
| operation | String | 操作描述（如"资产入库"/"APPROVED"/"LOCATION_MISMATCH"/"SUCCESS"） |
| beforeStatus | String | 变更前状态 |
| afterStatus | String | 变更后状态 |
| operatorName | String | 操作人 |
| operationTime | String | 操作时间（yyyy-MM-dd HH:mm:ss） |
| remark | String | 备注 |
| source | String | 来源（ASSET/APPROVAL/INVENTORY/FINANCE） |

## 7. AuditSummaryVO 字段

| 字段 | 类型 | 说明 |
|---|---|---|
| todayOperationCount | Integer | 今日操作总数 |
| assetChangeCount | Integer | 资产操作日志总数 |
| approvalOperationCount | Integer | 审批记录总数 |
| inventoryAbnormalCount | Integer | 盘点异常总数 |
| financeSyncCount | Integer | 财务同步记录总数 |

## 8. 权限设计

复用现有权限 `approval:audit`（id=34，"审批审计 - 查看全部审批记录"），该权限已分配给 ADMIN（role_id=1）和 AUDITOR（role_id=4）角色。

**说明**：Phase 16 多角色权限收敛时，将新增独立权限 `audit:view` 并细分审计范围。本阶段复用 `approval:audit` 避免新增权限 SQL，符合「不新增表、不新增字段」约束。

## 9. 开发边界（必须遵守）

- ✅ 不修改登录/JWT
- ✅ 不修改审批核心逻辑（仅只读查询 approval_record/approval_instance）
- ✅ 不修改生命周期状态流转
- ✅ 不修改盘点主链路（仅只读查询 inventory_record 异常记录）
- ✅ 不修改财务同步主链路（仅只读查询 finance_sync_record）
- ✅ 不修改预警规则主链路
- ✅ 不修改 AI 分析核心逻辑
- ✅ 不修改 Excel 导出主链路（新增审计导出端点，复用 ExcelExportUtil）
- ✅ 不修改资产新增/编辑/删除主流程
- ✅ 本阶段只做只读审计查询、前端审计页面和必要的 Excel 导出接口
- ✅ 不新增数据库表，不新增字段（现有字段足够）
- ✅ 不新增权限 SQL（复用 approval:audit）

## 10. 验收标准

1. 后端 `mvn -DskipTests package` 成功
2. 前端 `npm run build` 成功
3. 登录 admin / 123456 后可见「审计追踪」菜单
4. 审计统计卡片有数据（今日操作数、资产变更数、审批操作数、盘点异常数、财务同步数均 ≥0 且与数据库一致）
5. 资产操作日志可查询
6. 审批操作记录可查询
7. 盘点异常记录可查询
8. 财务同步记录可查询
9. 按资产编号、操作类型、操作人、时间范围筛选正常
10. 点击「查看资产」能跳转资产详情
11. 导出审计日志 Excel 成功（生成 .xlsx 文件）
12. 回归资产台账、审批中心、盘点任务、财务同步、预警中心、AI 分析、Excel 导出正常
