# Phase 8.1 审批进度可视化增强 - 需求规格

## 1. 背景

当前审批流后端已实现 submit/approve/reject/records/detail 接口，前端审批中心页面也有待办和已办列表。但生命周期单据详情页（领用、调拨、维修、报废）缺少审批进度展示，用户查看单据详情时无法看到：

- 该单据是否已提交审批
- 当前审批到哪个节点
- 审批通过/驳回的历史记录
- 审批意见和审批人信息

## 2. 目标

在领用、调拨、维修、报废单据详情弹窗中展示审批进度和审批历史记录，让用户能清楚看到单据的审批全过程。

## 3. 用户价值

- **申请人**：知道审批走到哪一步，是否被驳回
- **审批人**：追溯之前的审批记录和意见
- **资产管理员**：了解单据审批全过程，便于审计

## 4. 本阶段做

- 新增 `frontend/src/api/approval.ts`，封装现有后端审批查询接口
- 新增 `frontend/src/components/approval/ApprovalProgress.vue` 只读组件
- 修改 `frontend/src/components/LifecycleDetailDialog.vue`，通过 `businessType` prop 控制是否显示审批进度
- 修改 Receive.vue、Transfer.vue、Repair.vue、Scrap.vue，传入对应 businessType

## 5. 本阶段不做

- 不新增数据库表
- 不新增数据库字段
- 不修改 approval_instance、approval_record、approval_flow、approval_node 表结构
- 不修改 ApprovalService 的 submit/approve/reject/executeBusinessFlow 核心逻辑
- 不修改 LifecycleService 状态流转逻辑
- 不改登录、JWT、RBAC 主链路
- 不做消息通知
- 不做审批流配置页面
- 不做驳回重提
- 不做复杂工作流引擎
- 不做审批操作按钮（只读展示）
- 不新增后端接口（复用现有 `GET /api/approval/records`）

## 6. 数据来源

- 接口：`GET /api/approval/records?businessType=&businessId=`
- 返回：`Result<List<ApprovalRecordVO>>`
- ApprovalRecordVO 字段：nodeName、approverName、action、comment、approvedAt
- businessType 取值：RECEIVE、TRANSFER、REPAIR、SCRAP
- businessId 取值：对应生命周期单据的 id

## 7. 验收标准

1. 后端 `mvn -DskipTests package` 通过
2. 前端 `npm run build` 通过
3. 领用单据详情弹窗展示审批进度
4. 调拨单据详情弹窗展示审批进度
5. 维修单据详情弹窗展示审批进度
6. 报废单据详情弹窗展示审批进度
7. 审批记录按时间显示
8. 无审批记录时显示空状态"暂无审批记录"
9. 入库单据详情不展示审批进度（入库不走审批）
10. 不影响提交审批、审批通过、审批驳回入口
11. 不影响生命周期状态流转
12. 不影响资产详情页时间线功能
