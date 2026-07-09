# Phase 7.1：资产生命周期时间线 - 需求规格

> 状态：正式规格
> 基于：`docs/sdd/phase-7-asset-timeline-draft.md`

## 1. 背景

资产详情页目前仅展示资产基础信息（编号、名称、分类、价值、部门、状态等）。资产的流转记录分散在 5 张生命周期单据表（入库、领用、调拨、维修、报废）、审批记录表和操作日志表中，用户无法在资产详情页直观看到资产的完整流转历史。

当前要查看某个资产的某次领用记录，需要手动到「生命周期 → 领用」页面按资产筛选；要查看审批历史，需要到审批页面按业务单据查找；操作日志则在数据库层面记录，前端无入口。

## 2. 目标

在资产详情页底部新增「生命周期时间线」区域，聚合展示该资产从入库、领用、调拨、维修、报废、审批到操作日志的完整流转记录，按时间倒序排列，支持按事件类型筛选。

## 3. 用户价值

- **可追溯**：资产管理员可在一个页面查看资产完整生命周期
- **可审计**：审计人员无需跨页面拼凑流转记录
- **效率提升**：减少跨页面查询操作，降低理解成本

## 4. 本阶段做什么

1. 统一时间线 VO（`AssetTimelineEventVO`）
2. 后端聚合查询接口（`GET /api/assets/{assetId}/timeline`）
3. 前端时间线组件（`AssetTimeline.vue`）
4. 事件类型筛选（全部 / 入库 / 领用 / 调拨 / 维修 / 报废 / 审批 / 操作日志）
5. 集成到资产详情页

## 5. 本阶段不做什么

- 不新增数据库表
- 不新增数据库字段
- 不修改已有生命周期单据创建逻辑
- 不修改审批提交 / 审批通过 / 审批驳回逻辑
- 不修改资产状态流转逻辑
- 不修改登录、JWT、RBAC、资产台账列表、资产新增、资产编辑、生命周期页面、审批页面主流程
- 不做时间线导出
- 不做时间线编辑
- 不做时间线删除
- 不做复杂图谱
- 不做消息通知
- 不做 AI 分析

## 6. 数据来源

| 事件类型 | 数据来源表 | 关联字段 |
|---|---|---|
| 入库 INBOUND | `asset_inbound_order` | `asset_id` |
| 领用 RECEIVE | `asset_receive_order` | `asset_id` |
| 调拨 TRANSFER | `asset_transfer_order` | `asset_id` |
| 维修 REPAIR | `asset_repair_order` | `asset_id` |
| 报废 SCRAP | `asset_scrap_order` | `asset_id` |
| 审批 APPROVAL | `approval_record` → `approval_instance` | 通过 `business_type` + `business_id` 关联生命周期单据 |
| 操作日志 OPERATION_LOG | `asset_operation_log` | `asset_id` |

## 7. 审批事件关联说明

`approval_record` 表没有 `asset_id` 字段，不能直接按资产查询。关联路径：

1. 查询当前资产的 4 类生命周期单据（RECEIVE / TRANSFER / REPAIR / SCRAP），收集每类单据的 `id`
2. 按 `business_type` + `business_id` 查询 `approval_instance`（入库不走审批，不查）
3. 按 `instance_id` 查询 `approval_record`
4. 将审批记录映射为 APPROVAL 事件

## 8. 验收标准

1. 后端 `mvn -DskipTests package` 通过
2. 前端 `npm run build` 通过
3. 登录 admin / 123456 后进入资产详情页，基础信息正常展示
4. 资产详情页下方出现「生命周期时间线」区域
5. 时间线按时间倒序排列
6. 支持按事件类型筛选（全部 / 入库 / 领用 / 调拨 / 维修 / 报废 / 审批 / 操作日志）
7. 无数据的资产显示「暂无生命周期记录」
8. 不影响资产列表、新增、编辑、删除
9. 不影响生命周期页面
10. 不影响审批页面
11. 不新增数据库表
12. 不修改已有业务流转逻辑
