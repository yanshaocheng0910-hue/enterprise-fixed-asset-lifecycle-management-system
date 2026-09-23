# Phase 8.1 审批进度可视化增强 - 技术设计

## 1. 后端接口（复用，不新增）

现有接口已满足需求，无需新增后端代码：

```
GET /api/approval/records?businessType={type}&businessId={id}
```

- 返回类型：`Result<List<ApprovalRecordVO>>`
- ApprovalRecordVO 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| nodeName | String | 审批节点名称 |
| approverName | String | 审批人姓名 |
| action | String | 审批动作：SUBMIT/APPROVED/REJECTED |
| comment | String | 审批意见 |
| approvedAt | LocalDateTime | 审批时间（已格式化） |

## 2. 前端 API 设计

### 文件：`frontend/src/api/approval.ts`（新增）

```typescript
export interface ApprovalRecordItem {
  nodeName: string | null
  approverName: string | null
  action: string
  comment: string | null
  approvedAt: string | null
}

export function getApprovalRecords(params: { businessType: string; businessId: number }) {
  return request.get<any, { code: number; message: string; data: ApprovalRecordItem[] }>(
    '/approval/records', { params }
  )
}
```

## 3. 前端组件设计

### 文件：`frontend/src/components/approval/ApprovalProgress.vue`（新增）

**Props：**

| 名称 | 类型 | 必填 | 说明 |
|---|---|---|---|
| businessType | String | 是 | 业务类型：RECEIVE/TRANSFER/REPAIR/SCRAP |
| businessId | Number | 是 | 单据 id |
| status | String | 否 | 单据状态，用于显示当前审批状态 |

**行为：**

- onMounted 自动加载审批记录
- watch businessId 变化后重新加载
- 请求失败只影响组件内部，不影响详情弹窗主体

**UI 结构：**

```
审批进度
├── 当前单据状态标签（如传入 status）
└── el-timeline 审批记录列表
    ├── 每条记录：动作标题 + 审批人 + 时间 + 意见
    └── 空状态：el-empty "暂无审批记录"
```

**动作映射：**

| action | 显示 | 标签颜色 |
|---|---|---|
| SUBMIT | 提交申请 | info |
| APPROVED | 审批通过 | success |
| REJECTED | 审批驳回 | danger |
| 其他 | 审批记录 | info |

## 4. 公共组件集成设计

### 文件：`frontend/src/components/LifecycleDetailDialog.vue`（修改）

**修改内容：**

- 新增可选 prop：`businessType?: string`
- 在 el-descriptions 下方新增审批进度区域
- 当 `businessType` 存在且 `data.id` 存在时渲染 `<ApprovalProgress>`
- Inbound 页面不传 businessType，自动不显示审批进度

**设计优势：**

- 只修改一个公共组件，四个页面只需传入 businessType prop
- 不破坏原有详情弹窗布局
- 入库详情自动不显示审批进度（不传 businessType）

## 5. 四个生命周期页面修改

| 页面 | 修改内容 | businessType |
|---|---|---|
| Receive.vue | LifecycleDetailDialog 添加 `business-type="RECEIVE"` | RECEIVE |
| Transfer.vue | LifecycleDetailDialog 添加 `business-type="TRANSFER"` | TRANSFER |
| Repair.vue | LifecycleDetailDialog 添加 `business-type="REPAIR"` | REPAIR |
| Scrap.vue | LifecycleDetailDialog 添加 `business-type="SCRAP"` | SCRAP |
| Inbound.vue | 不修改 | 不传 |

## 6. 不新增后端代码声明

现有 `GET /api/approval/records` 接口已完全满足前端需求，本阶段不新增任何后端 Java 文件、不新增 XML、不修改任何后端业务逻辑。

## 7. UI 风格

- 保持现有 Element Plus 风格：白底、细边框、圆角
- el-timeline 展示审批记录
- 审批人空显示"系统"
- 审批意见空显示"-"
- 时间使用 approvedAt
- 不使用渐变、玻璃拟态、复杂动画
