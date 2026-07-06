# 第二阶段：生命周期单据流 · 规格说明

## 1. 背景

第一阶段实现了资产台账管理，但资产状态需要人工手动编辑，无法真实反映资产业务流转过程。

第二阶段的目标是引入生命周期单据概念，让资产状态通过业务单据自动流转。

## 2. 目标

- 实现资产入库、领用、调拨、维修、报废五个核心生命周期模块
- 资产状态通过单据自动变更
- 所有状态变化写入操作日志
- 非法状态操作被拦截

## 3. 用户角色

| 角色 | 说明 |
|---|---|
| 系统管理员 | 全部操作权限 |
| 资产管理员 | 可以创建和查看所有单据 |

## 4. 功能范围

- 入库：创建入库单，资产状态变为 IDLE
- 领用：仅限 IDLE 资产，创建领用单，资产变为 IN_USE
- 调拨：IDLE 或 IN_USE 资产可调拨，更新部门/地点/保管人
- 维修：IDLE 或 IN_USE 资产可创建维修单，资产变为 REPAIRING
- 维修完成：REPAIRED → IN_USE / SCRAP_SUGGESTED → WAITING_SCRAP
- 报废：IDLE/IN_USE/REPAIRING/WAITING_SCRAP 可创建报废单，资产变为 SCRAPPED

## 5. 不做什么

- 审批流（Phase 3 做）
- 盘点管理（Phase 4 做）
- 上传附件
- 消息通知
- 邮件提醒

## 6. 新增表

- asset_inbound_order
- asset_receive_order
- asset_transfer_order
- asset_repair_order
- asset_scrap_order

## 7. 单据编号规则

前缀 + yyyyMMdd + 4 位流水号。

| 单据 | 前缀 | 示例 |
|---|---|---|
| 入库单 | IN | IN202607050001 |
| 领用单 | RE | RE202607050001 |
| 调拨单 | TF | TF202607050001 |
| 维修单 | RP | RP202607050001 |
| 报废单 | SC | SC202607050001 |

## 8. 状态流转

| 流程 | 操作 | 资产状态变化 |
|---|---|---|
| 入库 | 创建入库单 | 任意 → IDLE |
| 领用 | 创建领用单 | IDLE → IN_USE |
| 调拨 | 创建调拨单 | IDLE / IN_USE → IN_USE |
| 创建维修 | 创建维修单 | IDLE / IN_USE → REPAIRING |
| 维修完成 | REPAIRED | REPAIRING → IN_USE |
| 维修完成 | SCRAP_SUGGESTED | REPAIRING → WAITING_SCRAP |
| 报废 | 创建报废单 | IDLE/IN_USE/REPAIRING/WAITING_SCRAP → SCRAPPED |

## 9. 异常规则

| 场景 | 返回 |
|---|---|
| 非 IDLE 资产领用 | 400：仅闲置资产可领用 |
| SCRAPPED 资产调拨 | 400：不允许 |
| SCRAPPED 资产维修 | 400：不允许 |
| 再次报废已报废资产 | 400：不允许 |
| 非 DRAFT 维修单完成 | 400：不允许 |

## 10. 新增接口

共 17 个：

| 方法 | 路径 |
|---|---|
| GET | /api/lifecycle/asset-select-options?status= |
| 3 | /api/lifecycle/inbound/* |
| 3 | /api/lifecycle/receive/* |
| 3 | /api/lifecycle/transfer/* |
| 4 | /api/lifecycle/repair/* |
| 3 | /api/lifecycle/scrap/* |

## 11. 验收标准

- 干净数据库复测通过
- migration-v2-lifecycle.sql 执行成功
- 后端 mvn -DskipTests package 通过
- 前端 npm run build 通过
- 入库/领用/调拨/维修/报废全流程通过
- 非法状态全部返回 400
- 操作日志写入正常
- 第一阶段回归测试通过
