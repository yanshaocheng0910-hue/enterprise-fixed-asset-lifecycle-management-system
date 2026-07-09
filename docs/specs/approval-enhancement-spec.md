# 审批流增强规格草案

> 状态：规划中
> 对应 SDD 草案：`docs/sdd/phase-6-approval-enhancement-draft.md`

## 1. 功能定义

完善我的待办、我的已办、审批详情、审批记录、审批进度展示和驳回重提，提升流程管理完整度。

## 2. 做什么

- 我的待办列表（当前用户待审批的实例）
- 我的已办列表（当前用户已审批的实例）
- 审批进度组件（可视化展示节点和当前进度）
- 驳回重提逻辑（驳回后可修改并重新提交）
- 审批详情完善（完整审批链路和记录）

## 3. 不做什么

- 不做审批流的流程引擎重构
- 不做多人会签和并行审批
- 不做审批催办和邮件通知
- 不做审批委托和代理

## 4. 数据来源

复用已有表：

| 表名 | 说明 |
|---|---|
| `approval_template` | 审批模板 |
| `approval_instance` | 审批实例 |
| `approval_node` | 审批节点 |
| `approval_record` | 审批记录 |

如需新增字段（驳回原因、重提标记），通过 migration SQL 实现。

## 5. 接口规划

- `GET /api/approval/todo/page` - 我的待办分页查询
- `GET /api/approval/done/page` - 我的已办分页查询
- `GET /api/approval/instance/{id}/progress` - 审批进度
- `POST /api/approval/instance/{id}/reject` - 驳回
- `POST /api/approval/instance/{id}/resubmit` - 重新提交

## 6. 前端页面

- 我的待办页（新增）
- 我的已办页（新增）
- 审批详情页（完善）
- 审批进度组件（新增通用组件）

## 7. 验收标准

- 当前用户可查看待办审批列表
- 当前用户可查看已办审批列表
- 审批详情页可展示审批进度
- 驳回后原提交人可修改并重新提交
- 所有操作符合 RBAC 权限控制
