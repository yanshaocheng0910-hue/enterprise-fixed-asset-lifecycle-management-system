# Phase 8.2 审批中心页面完善 - 技术设计

## 1. 后端接口（复用，不新增）

### 1.1 待办分页

```
GET /api/approval/todo/page?pageNum=1&pageSize=10&businessType=&status=
```

返回 `Result<PageResult<ApprovalTodoVO>>`

ApprovalTodoVO 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| instanceId | Long | 审批实例 ID |
| businessType | String | 业务类型 |
| businessId | Long | 单据 ID |
| flowName | String | 流程名称 |
| nodeName | String | 当前节点 |
| status | String | 状态 |
| startedBy | Long | 申请人 ID |
| applicantName | String | 申请人姓名 |
| startedAt | LocalDateTime | 提交时间 |

### 1.2 已办分页

```
GET /api/approval/done/page?pageNum=1&pageSize=10&businessType=&status=
```

返回 `Result<PageResult<ApprovalDoneVO>>`

ApprovalDoneVO 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| instanceId | Long | 审批实例 ID |
| businessType | String | 业务类型 |
| businessId | Long | 单据 ID |
| orderCode | String | 单据编号 |
| assetCode | String | 资产编号 |
| assetName | String | 资产名称 |
| action | String | 审批动作 |
| comment | String | 审批意见 |
| status | String | 状态 |
| approverName | String | 审批人 |
| approvedAt | LocalDateTime | 审批时间 |

### 1.3 审批详情

```
GET /api/approval/{instanceId}
```

返回 `Result<ApprovalDetailVO>`

ApprovalDetailVO 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| instanceId | Long | 审批实例 ID |
| businessType | String | 业务类型 |
| businessId | Long | 单据 ID |
| flowName | String | 流程名称 |
| currentNodeName | String | 当前节点 |
| status | String | 状态 |
| applicantName | String | 申请人 |
| startedAt | LocalDateTime | 开始时间 |
| completedAt | LocalDateTime | 完成时间 |
| records | List<ApprovalRecordVO> | 审批记录列表 |

### 1.4 审批通过/驳回

```
POST /api/approval/{instanceId}/approve  body: { action: "APPROVED", comment: "..." }
POST /api/approval/{instanceId}/reject   body: { action: "REJECTED", comment: "..." }
```

返回 `Result<Void>`

## 2. 前端 API 设计

### 文件：`frontend/src/api/approval.ts`（修改，追加）

保留已有 `getApprovalRecords`，新增：

```typescript
export interface ApprovalTodoItem { ... }
export interface ApprovalDoneItem { ... }
export interface ApprovalDetail { ... }

export function getApprovalTodoPage(params: Record<string, any>)
export function getApprovalDonePage(params: Record<string, any>)
export function getApprovalDetail(instanceId: number)
export function approveApproval(instanceId: number, data: { action: string; comment?: string })
export function rejectApproval(instanceId: number, data: { action: string; comment?: string })
```

## 3. 页面设计

### 3.1 ApprovalTodo.vue

- PageHeader 标题"我的待办"
- el-table 展示：业务类型、单据ID、流程名称、当前节点、申请人、提交时间、状态
- 操作列：查看详情、通过、驳回
- 通过/驳回弹出 el-dialog，填写 comment
- 分页 el-pagination
- 空数据 el-empty
- 加载中 v-loading

### 3.2 ApprovalDone.vue

- PageHeader 标题"我的已办"
- el-table 展示：业务类型、单据编号、资产名称、审批动作、审批意见、审批时间、状态
- 操作列：查看详情
- 分页 el-pagination
- 空数据 el-empty

### 3.3 ApprovalDetailDialog.vue

- el-dialog 弹窗
- el-descriptions 展示：流程名称、业务类型、业务ID、申请人、当前节点、状态、开始时间、完成时间
- 下方 el-timeline 展示审批记录
- 动作映射：SUBMIT→提交申请, APPROVED→审批通过, REJECTED→审批驳回

## 4. 路由设计

| 路径 | 名称 | 组件 | 权限 |
|---|---|---|---|
| /approval/todo | ApprovalTodo | ApprovalTodo.vue | approval:todo |
| /approval/done | ApprovalDone | ApprovalDone.vue | approval:done |

## 5. 菜单设计

在 MainLayout.vue 中新增"审批中心"子菜单：

```html
<el-sub-menu index="approval" v-if="authStore.hasPermission('approval:todo') || authStore.hasPermission('approval:done')">
  <template #title>
    <el-icon><Checked /></el-icon>
    <span>审批中心</span>
  </template>
  <el-menu-item index="/approval/todo" v-if="authStore.hasPermission('approval:todo')">我的待办</el-menu-item>
  <el-menu-item index="/approval/done" v-if="authStore.hasPermission('approval:done')">我的已办</el-menu-item>
</el-sub-menu>
```

## 6. 不新增后端代码声明

现有 5 个审批接口完全满足需求，本阶段不新增任何后端 Java 文件、不修改任何后端业务逻辑。
