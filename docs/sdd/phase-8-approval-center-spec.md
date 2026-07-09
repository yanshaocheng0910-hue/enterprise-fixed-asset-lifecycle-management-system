# Phase 8.2 审批中心页面完善 - 需求规格

## 1. 背景

后端已实现完整的审批流接口：待办分页（todo/page）、已办分页（done/page）、审批详情（detail）、审批通过（approve）、审批驳回（reject）。Phase 8.1 已在生命周期单据详情中展示了审批进度，但前端缺少独立的审批中心页面，用户无法集中查看和处理待审批事项。

## 2. 目标

补齐前端审批中心页面，让用户可以：
- 查看我的待办列表
- 对待办事项执行通过/驳回操作
- 查看我的已办列表
- 查看审批详情（含审批记录时间线）

## 3. 用户价值

- **审批人**：集中处理待审批事项，无需到各单据页面逐一查看
- **申请人**：查看已办记录，了解审批结果
- **资产管理员**：追溯审批历史

## 4. 本阶段做

- 补全 `frontend/src/api/approval.ts`，新增待办、已办、详情、通过、驳回 API
- 新增 `frontend/src/views/approval/ApprovalTodo.vue` 待办页面
- 新增 `frontend/src/views/approval/ApprovalDone.vue` 已办页面
- 新增 `frontend/src/components/approval/ApprovalDetailDialog.vue` 审批详情弹窗
- 修改 `frontend/src/router/index.ts`，新增 /approval/todo 和 /approval/done 路由
- 修改 `frontend/src/layouts/MainLayout.vue`，新增"审批中心"菜单

## 5. 本阶段不做

- 不新增数据库表
- 不新增字段
- 不修改 ApprovalService 的 submit/approve/reject/executeBusinessFlow 核心逻辑
- 不修改 LifecycleService 状态流转
- 不修改 approval_instance、approval_record、approval_flow、approval_node 表结构
- 不重构登录、JWT、RBAC、资产台账、生命周期主链路
- 不做消息通知
- 不做审批委托
- 不做加签/会签
- 不做流程配置页面
- 不新增后端接口

## 6. 后端接口（复用现有）

| 接口 | 方法 | 说明 |
|---|---|---|
| /api/approval/todo/page | GET | 待办分页 |
| /api/approval/done/page | GET | 已办分页 |
| /api/approval/{instanceId} | GET | 审批详情 |
| /api/approval/{instanceId}/approve | POST | 审批通过 |
| /api/approval/{instanceId}/reject | POST | 审批驳回 |

## 7. 权限

- `approval:todo`（id=31）- 我的待办
- `approval:done`（id=32）- 我的已办
- 已在 migration-v5-rbac.sql 中定义，无需新增

## 8. 验收标准

1. 后端 `mvn -DskipTests package` 通过
2. 前端 `npm run build` 通过
3. 我的待办页面能展示待审批列表
4. 待办列表支持分页
5. 点击"通过"弹出确认弹窗，填写 comment 后调用接口
6. 点击"驳回"弹出确认弹窗，填写 comment 后调用接口
7. 操作成功后列表刷新
8. 我的已办页面能展示已处理记录
9. 审批详情弹窗展示流程信息和审批记录时间线
10. 空数据用 el-empty
11. 不影响 Phase 8.1 审批进度组件
12. 不影响资产详情时间线
13. 不影响生命周期单据页面
14. 菜单显示"审批中心"子菜单
