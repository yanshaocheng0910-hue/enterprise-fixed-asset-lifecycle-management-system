# Phase 15 - 基础数据字典与演示数据时间分布优化 需求规格

## 1. 背景

当前系统已完成资产台账、生命周期、审批中心、资产时间线、盘点闭环、折旧财务、财务模拟同步、预警中心、AI 辅助分析、Excel 导出、演示数据增强和全局审计日志等模块。演示数据数量已经足够（120 条 DEMO 资产 + 关联数据），但存在两个突出问题：

1. **基础信息偏手填**：资产新增/编辑、领用、调拨、盘点等页面的"所属部门""存放地点""保管人"字段仍为 `el-input` 手动输入，缺乏下拉选择，不利于演示真实系统体验，也容易输入不一致的部门/地点名称。
2. **时间分布不够直观**：生命周期单据（领用/调拨/维修/报废/入库）和审批记录的 `created_at` 均使用 `NOW()`，全部集中在执行当天；`finance_sync_record` 仅 6 条、覆盖 6 个月；盘点任务仅 4 条、集中在 2026-06/07。这导致折旧月度趋势、财务同步列表、审计日志时间筛选、盘点记录难以展示明显的时间跨度。

## 2. 目标

- 补齐部门/地点/保管人下拉选择能力，提升日常录入体验和数据一致性
- 优化演示数据在月份上的分布，让折旧趋势、财务同步、审计日志、盘点等月度数据更直观
- 不修改任何核心业务逻辑，仅做只读基础数据查询、前端下拉改造和演示数据时间分布调整

## 3. 用户价值

- 提升报表趋势展示效果（折旧月度趋势至少 12 个月）
- 提升财务折旧分析体验（财务同步覆盖 12 个月，含成功/失败）
- 提升审计追踪体验（审计日志按时间范围筛选能查到不同月份数据）
- 提升日常录入体验（部门/地点/保管人下拉选择，避免手填）

## 4. 数据来源映射

### 4.1 基础数据表（新增）

| 表名 | 用途 | 字段 |
|------|------|------|
| `base_department` | 部门字典 | id, department_code, department_name, manager_name, status, sort_order, created_at, updated_at |
| `base_location` | 地点字典 | id, location_code, location_name, building, floor_no, room_no, status, sort_order, created_at, updated_at |

### 4.2 保管人来源

复用现有 `sys_user` 表，读取 `status=1` 的启用用户的 `real_name` 字段。若用户表数据不足，可补充演示人员。

### 4.3 演示数据时间分布优化

| 数据表 | 当前状态 | 优化目标 |
|--------|----------|----------|
| `asset.purchase_date` | 已分散在 2016-2026 | 保持，无需调整 |
| `asset_inbound_order.created_at` | 全部 `NOW()` | 分散到最近 12 个月 |
| `asset_receive_order.created_at` | 全部 `NOW()` | 分散到最近 12 个月 |
| `asset_transfer_order.created_at` | 全部 `NOW()` | 分散到最近 12 个月 |
| `asset_repair_order.created_at` | 全部 `NOW()` | 分散到最近 12 个月 |
| `asset_scrap_order.created_at` | 全部 `NOW()` | 分散到最近 12 个月 |
| `approval_instance.created_at` | 全部 `NOW()` | 分散到最近 12 个月（参考 started_at） |
| `approval_record.created_at` | 全部 `NOW()` | 分散到最近 12 个月（参考 approved_at） |
| `inventory_task` | 4 条，集中在 2026-06/07 | 扩展到覆盖 2025-Q4、2026-Q1、Q2、Q3 |
| `finance_sync_record` | 6 条，覆盖 6 个月 | 扩展到 12 条，覆盖最近 12 个月，含 10 条 SUCCESS、2 条 FAILED |
| `depreciation_record` | 已覆盖 2025-07~2026-06 | 保持，无需调整 |
| `asset_operation_log.operation_time` | 已分散 | 保持，无需调整 |

## 5. 接口设计

### 5.1 基础数据接口（只读）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/master-data/departments` | GET | 获取启用部门列表 | 登录即可 |
| `/api/master-data/locations` | GET | 获取启用地点列表 | 登录即可 |
| `/api/master-data/keepers` | GET | 获取可选保管人列表 | 登录即可 |

### 5.2 VO 字段

**MasterDataVO**（统一返回结构）：
- `id`: Long
- `code`: String（编码）
- `name`: String（名称）
- `label`: String（显示文本，如 "信息中心"）
- `value`: String（值，同 name，用于提交）
- `extraInfo`: String（附加信息，如部门负责人、地点楼层）

## 6. 前端改造范围

| 页面 | 改造字段 | 当前组件 | 目标组件 |
|------|----------|----------|----------|
| AssetList.vue 搜索表单 | department, keeper, location | el-input | el-select（可输入） |
| AssetList.vue 编辑弹窗 | department, keeper, location | el-input | el-select |
| Receive.vue 编辑弹窗 | receiver, receiverDepartment | el-input | el-select |
| Transfer.vue 编辑弹窗 | toDepartment, toLocation, toKeeper | el-input | el-select |
| InventoryTask.vue 新建弹窗 | department, location | el-input | el-select |
| InventoryTask.vue 编辑明细 | actualLocation, actualKeeper | el-input | el-select |

注意：Repair.vue 无部门/地点/保管人字段，不改造。

## 7. 开发边界

- 不修改登录/JWT
- 不修改审批核心逻辑
- 不修改生命周期状态流转
- 不修改盘点主链路
- 不修改财务同步主链路
- 不修改预警规则主链路
- 不修改 AI 分析核心逻辑
- 不修改 Excel 导出主链路
- 不修改审计主链路
- 不修改资产新增/编辑/删除主流程（仅改前端表单组件类型，提交字段结构不变）
- 本阶段只做基础数据字典、前端下拉选择、演示数据时间分布优化

## 8. 验收标准

1. 新增资产时，部门、保管人、地点均为下拉选择
2. 调拨页面调入部门、地点、接收人能下拉选择
3. 盘点任务按部门/地点创建时能选择部门/地点
4. 折旧报表月度趋势至少显示 12 个月数据，数值有变化
5. 财务同步记录覆盖最近 12 个月，含 SUCCESS 和 FAILED
6. 审计日志按时间范围筛选能查到不同月份数据
7. 资产台账筛选部门/地点时选项丰富
8. 回归资产新增、编辑、领用、调拨、盘点、折旧、财务同步、预警、AI、Excel、审计页面均正常
