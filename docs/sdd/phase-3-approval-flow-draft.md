# 第三阶段：审批流 · 设计草案

> ⚠️ 本文件仅为设计草案，未实现任何代码。
> 正式开始第三阶段前，需要先按模板编写完整的 Spec 和 Design 文档。

## 1. 背景

第二阶段实现了生命周期单据的自动流转，但所有单据是「提交即完成」模式。

在国企管理场景中，资产领用、调拨、维修、报废等操作需要经过审批流程，不能由操作人直接完成。

第三阶段的目标是为生命周期单据引入审批机制。

## 2. 目标

- 领用、调拨、维修、报废操作不再直接完成
- 改为提交 → 审批 → 完成的流程
- 新增审批模板、审批节点、审批记录
- 我的待办 / 我的已办
- 审批通过 / 审批驳回
- 单据审批历史追溯

## 3. 建议新增表

### approval_flow（审批模板）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| flow_code | VARCHAR | 审批模板编码 |
| flow_name | VARCHAR | 审批模板名称 |
| order_type | VARCHAR | 关联单据类型（RECEIVE/TRANSFER/REPAIR/SCRAP） |

### approval_node（审批节点）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| flow_id | BIGINT | 审批模板 ID |
| node_order | INT | 节点序号 |
| approver_role | VARCHAR | 审批角色 |
| node_name | VARCHAR | 节点名称 |

### approval_record（审批记录）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| order_type | VARCHAR | 单据类型 |
| order_id | BIGINT | 单据 ID |
| node_id | BIGINT | 节点 ID |
| approver_id | BIGINT | 审批人 |
| action | VARCHAR | 审批动作（APPROVED / REJECTED） |
| comment | VARCHAR | 审批意见 |
| created_at | DATETIME | 创建时间 |

## 4. 建议新增状态

| 状态 | 说明 |
|---|---|
| DRAFT | 草稿（创建后未提交） |
| SUBMITTED | 已提交待审批 |
| APPROVING | 审批中（多节点时） |
| APPROVED | 审批通过 |
| REJECTED | 审批驳回 |
| COMPLETED | 已完成（单据生效） |
| CANCELLED | 已取消 |

## 5. 建议状态流转

```
DRAFT ──提交──> SUBMITTED ──审批中──> APPROVING ──通过──> APPROVED ──执行──> COMPLETED
                                   └──驳回──> REJECTED ──重新提交──> SUBMITTED
DRAFT ──取消──> CANCELLED
```

## 6. 建议页面

- 审批模板管理
- 审批节点配置
- 我的待办
- 我的已办
- 单据审批历史

## 7. 注意事项

1. 审批流上线后，第二阶段的所有「提交即完成」逻辑需要改造为「提交→审批→完成」。
2. 新增的审批记录表需要与现有生命周期单据表通过 `order_type + order_id` 关联。
3. 审批被驳回后，前端应允许编辑单据后重新提交。
4. 现有的 `asset_operation_log` 操作日志继续保留，同时新增审批记录。

## 8. 暂不实现

- 会签
- 转审
- 加签
- 条件分支
- 超时自动审批
