# 第三阶段：审批流 · 任务拆分

## 1. 阶段目标

把第二阶段「提交即完成」的生命周期单据，升级为「提交申请 → 审批 → 审批通过后执行业务流转」的审批模式。

本阶段完成后，领用、调拨、维修、报废四个业务类型均需经过审批流程才能生效，审批通过后自动执行第二阶段的资产状态流转逻辑，审批驳回后资产状态保持不变。

## 2. 开发边界

### 本阶段做

- 新增 4 张审批表：approval_flow、approval_node、approval_instance、approval_record
- 审批流程：领用（单级）、调拨（单级）、维修（单级）、报废（两级）
- 提交审批：POST /api/approval/submit
- 我的待办：GET /api/approval/todo/page
- 我的已办：GET /api/approval/done/page
- 审批通过：POST /api/approval/{instanceId}/approve
- 审批驳回：POST /api/approval/{instanceId}/reject
- 审批详情：GET /api/approval/{instanceId}
- 审批记录：GET /api/approval/records
- 生命周期页面增加「审批状态」列和「提交审批」按钮
- 我的待办/已办前端页面
- 审批详情/操作弹窗组件
- 审批写入 asset_operation_log

### 本阶段不做

- BPMN / Activiti / Flowable 等专业工作流引擎
- 流程图可视化编辑器
- 会签 / 或签 / 转审 / 加签
- 消息通知（短信、邮件、站内信）
- 钉钉 / 企业微信 / 飞书集成
- 移动端审批
- 动态复杂审批配置（如「金额>10万自动加签财务」）
- 审批超时自动处理
- 审批委托（他人代审）
- 审批模板的可视化配置界面
- 入库审批（原因见 Spec 文档）

---

## 3. Backend Tasks

### Task B1：数据库迁移 SQL

**文件：** `backend/src/main/resources/sql/migration-v3-approval.sql`

创建 4 张表：

| 表名 | 说明 |
|---|---|
| approval_flow | 审批模板（预定义 4 条：领用/调拨/维修/报废） |
| approval_node | 审批节点（预定义 5 条：领用1级、调拨1级、维修1级、报废2级） |
| approval_instance | 审批实例（每次提交审批创建一个实例） |
| approval_record | 审批记录（每个节点的操作记录） |

同时插入初始化数据：
- 4 条 approval_flow（领用/调拨/维修/报废各一条）
- 5 条 approval_node（领用→部门负责人、调拨→资产管理员、维修→资产管理员、报废→资产管理员+财务）

**验收：** 迁移 SQL 执行成功，4 张表创建完成，初始化数据正确。

---

### Task B2：审批实体和 Mapper

**路径：** `backend/src/main/java/com/example/asset/approval/entity/`

创建 4 个 Entity：

| Entity | 映射表 | 关键字段 |
|---|---|---|
| ApprovalFlow | approval_flow | id, flowCode, flowName, businessType, enabled |
| ApprovalNode | approval_node | id, flowId, nodeCode, nodeName, approverRole, sortOrder |
| ApprovalInstance | approval_instance | id, businessType, businessId, flowId, currentNodeId, status, startedBy |
| ApprovalRecord | approval_record | id, instanceId, nodeId, approverId, approverName, action, comment |

**路径：** `backend/src/main/java/com/example/asset/approval/mapper/`

创建 4 个 Mapper：

| Mapper | 自定义方法 |
|---|---|
| ApprovalFlowMapper | findByBusinessType(businessType) |
| ApprovalNodeMapper | findByFlowIdOrderBySort(flowId) |
| ApprovalInstanceMapper | selectTodoPage(分页参数), selectDonePage(分页参数) |
| ApprovalRecordMapper | findByInstanceIdOrderByCreatedAt(instanceId) |

**验收：** 编译通过，MyBatis-Plus 基础 CRUD 可用，自定义分页查询可运行。

---

### Task B3：审批 DTO / VO

**路径：** `backend/src/main/java/com/example/asset/approval/dto/`

| DTO | 字段 | 用途 |
|---|---|---|
| ApprovalSubmitRequest | businessType, businessId, remark | 提交审批请求 |
| ApprovalActionRequest | action(APPROVED/REJECTED), comment | 审批操作请求 |
| ApprovalPageRequest | pageNum, pageSize, businessType, status | 分页查询条件 |

**路径：** `backend/src/main/java/com/example/asset/approval/vo/`

| VO | 包含字段 | 用途 |
|---|---|---|
| ApprovalTodoVO | instanceId, businessType, businessId, orderCode, assetCode, assetName, nodeName, status, startedAt, applicantName | 待办列表展示 |
| ApprovalDoneVO | 同上 + approverName, action, comment, approvedAt | 已办列表展示 |
| ApprovalRecordVO | nodeName, approverName, action, comment, approvedAt | 审批历史展示 |
| ApprovalDetailVO | 审批实例信息 + 业务单据信息 + 审批记录列表 | 审批详情展示 |

**验收：** 编译通过，字段定义正确。

---

### Task B4：ApprovalService

**路径：** `backend/src/main/java/com/example/asset/approval/service/ApprovalService.java`

| 方法 | 核心逻辑 |
|---|---|
| submit(businessType, businessId) | 1. 校验单据状态为 DRAFT/REJECTED；2. 查询审批模板 flow；3. 创建审批实例，状态 SUBMITTED，currentNodeId = 第一个节点；4. 更新业务单据 status = SUBMITTED |
| approve(instanceId, comment) | 1. 校验 instance 状态为 SUBMITTED/APPROVING；2. 校验当前用户为当前节点的审批角色；3. 写入 approval_record；4. 若有下一节点，currentNodeId 指向下一节点，status = APPROVING；5. 若为最后一个节点，status = APPROVED，调用 executeBusinessFlow(businessType, businessId)；6. 调用 recordOperationLog |
| reject(instanceId, comment) | 1. 校验 instance 状态；2. 写入 approval_record；3. instance.status = REJECTED；4. 业务单据 status = REJECTED；5. 调用 recordOperationLog |
| getTodoPage(query) | 根据当前用户角色查询待办列表（联表查询业务单据信息） |
| getDonePage(query) | 根据当前用户 ID 查询已处理记录 |
| getRecords(businessType, businessId) | 查询指定单据的审批记录 |
| getDetail(instanceId) | 查询审批实例详情（含业务单据信息） |

**事务边界：** approve() 和 reject() 方法必须使用 `@Transactional(rollbackFor = Exception.class)`。

**验收：** 单测覆盖 submit/approve/reject 三个核心方法。

---

### Task B5：生命周期 Service 改造

**路径：** `backend/src/main/java/com/example/asset/lifecycle/service/LifecycleService.java`

**改造原则：不删除第二阶段已有的完整方法，不破坏现有接口签名。**

需要新增的方法：

```java
// 审批通过后执行领用流转
@Transactional
public void executeReceiveFlow(Long orderId) {
    ReceiveOrder order = receiveOrderMapper.selectById(orderId);
    Asset asset = requireAsset(order.getAssetId());
    // 复用第二阶段的流转逻辑：改状态、更新部门/保管人、写日志
    asset.setStatus(IN_USE);
    asset.setDepartment(order.getReceiverDepartment());
    asset.setKeeper(order.getReceiver());
    assetMapper.updateById(asset);
    order.setStatus("COMPLETED");
    order.setUpdatedAt(LocalDateTime.now());
    receiveOrderMapper.updateById(order);
    recordLog(asset.getId(), "RECEIVE", "资产领用（审批通过）", ...);
}
```

同理解析出：
- `executeTransferFlow(Long orderId)`
- `executeRepairFlow(Long orderId)` — 注意维修有 create 和 complete 两个阶段
- `executeScrapFlow(Long orderId)`

**注意：** `executeReceiveFlow` 等新方法与原有的 `createReceive` 不同：
- `createReceive` 是「创建单据 + 状态流转」的完整操作（第二阶段模式）
- `executeReceiveFlow` 是「审批通过后只执行流转」（第三阶段模式）

两者共存，互不影响。

**验收：** 新方法编译通过，审批通过后能正确执行流转。

---

### Task B6：ApprovalController

**路径：** `backend/src/main/java/com/example/asset/approval/controller/ApprovalController.java`

```java
@Validated
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    @PostMapping("/submit")
    public Result<Long> submit(@Valid @RequestBody ApprovalSubmitRequest req)

    @PostMapping("/{instanceId}/approve")
    public Result<Void> approve(@PathVariable Long instanceId,
                                @Valid @RequestBody ApprovalActionRequest req)

    @PostMapping("/{instanceId}/reject")
    public Result<Void> reject(@PathVariable Long instanceId,
                               @Valid @RequestBody ApprovalActionRequest req)

    @GetMapping("/todo/page")
    public Result<PageResult<ApprovalTodoVO>> todoPage(@Valid ApprovalPageRequest req)

    @GetMapping("/done/page")
    public Result<PageResult<ApprovalDoneVO>> donePage(@Valid ApprovalPageRequest req)

    @GetMapping("/records")
    public Result<List<ApprovalRecordVO>> records(
            @RequestParam String businessType,
            @RequestParam Long businessId)

    @GetMapping("/{instanceId}")
    public Result<ApprovalDetailVO> detail(@PathVariable Long instanceId)
}
```

所有接口使用 `Result<T>` 和 `PageResult<T>` 统一返回格式。

**验收：** 后端 package 通过，所有端点可访问。

---

### Task B7：权限与用户上下文

当前用户系统中的角色：

| 角色编码 | 角色名称 | 审批职责 |
|---|---|---|
| ADMIN | 系统管理员 | 管理审批配置 |
| ASSET_MANAGER | 资产管理员 | 审批调拨/维修/报废（一级） |
| FINANCE | 财务人员 | 审批报废（二级） |

**当前没有「部门负责人」角色的处理方案：**
第一阶段将「部门负责人」的审批职能赋给 ASSET_MANAGER 角色代为处理，待第六阶段用户管理完善后再细化。

**权限判断：**
- `ApprovalService` 中从 `UserContext` 获取当前用户角色
- 审批操作前校验当前用户角色是否匹配当前节点的 `approver_role`
- 审计人员（AUDITOR）只能查看审批记录，不能操作

**验收：** ASSET_MANAGER 可以审批领用/调拨/维修/报废；FINANCE 可以审批报废二级；AUDITOR 只能查看。

---

### Task B8：后端测试

| 编号 | 测试用例 | 方法 |
|---|---|---|
| T-01 | 提交审批：DRAFT 单据提交后状态变 SUBMITTED | POST /api/approval/submit |
| T-02 | 待办列表：审批人可以看到待办 | GET /api/approval/todo/page |
| T-03 | 审批通过：审批后执行业务流转 | POST /api/approval/{id}/approve |
| T-04 | 审批驳回：驳回后资产状态不变 | POST /api/approval/{id}/reject |
| T-05 | 重复审批：已审批单据不能再次审批 | 返回 400 |
| T-06 | 非法状态：DRAFT 单据不能 approve | 返回 400 |
| T-07 | 审批记录可查询 | GET /api/approval/records |
| T-08 | 第一阶段回归：登录正常 | POST /api/auth/login |
| T-09 | 第一阶段回归：驾驶舱正常 | GET /api/dashboard/stats |
| T-10 | 第一阶段回归：资产台账正常 | GET /api/assets/page |
| T-11 | 第二阶段回归：生命周期创建正常 | 各 create 接口 |
| T-12 | 构建验证 | mvn -DskipTests package |

---

## 4. Frontend Tasks

### Task F1：审批 API

**文件：** `frontend/src/api/approval.ts`

```typescript
import request from './request'

export function submitApproval(data: Record<string, any>) {
  return request.post<any, any>('/approval/submit', data)
}
export function getTodoPage(params: Record<string, any>) {
  return request.get<any, any>('/approval/todo/page', { params })
}
export function getDonePage(params: Record<string, any>) {
  return request.get<any, any>('/approval/done/page', { params })
}
export function approveInstance(id: number, data: Record<string, any>) {
  return request.post<any, any>(`/approval/${id}/approve`, data)
}
export function rejectInstance(id: number, data: Record<string, any>) {
  return request.post<any, any>(`/approval/${id}/reject`, data)
}
export function getApprovalRecords(businessType: string, businessId: number) {
  return request.get<any, any>('/approval/records', { params: { businessType, businessId } })
}
export function getApprovalDetail(id: number) {
  return request.get<any, any>(`/approval/${id}`)
}
```

### Task F2：审批类型定义

**文件：** `frontend/src/types/approval.ts`

定义 TypeScript 接口：

- `ApprovalTodoItem`
- `ApprovalDoneItem`
- `ApprovalRecord`
- `ApprovalDetail`
- `BusinessType` 联合类型

### Task F3：我的待办页面

**文件：** `frontend/src/views/approval/Todo.vue`
**路由：** `/approval/todo`

页面结构：

```
PageHeader("我的待办", "待我审批的单据列表")

查询表单：业务类型下拉 + 单据编号输入 + 查询/重置

表格列：单据编号 | 业务类型 | 资产编号 | 资产名称 | 申请人 | 提交时间 | 当前节点 | 操作(审批)

审批弹窗(复用 ApprovalDetailDialog + ApprovalActionDialog)

分页
```

点击「审批」打开审批操作弹窗，包含：
- 业务单据详情（复用 LifecycleDetailDialog）
- 审批历史列表
- 通过 / 驳回按钮 + 审批意见输入

### Task F4：我的已办页面

**文件：** `frontend/src/views/approval/Done.vue`
**路由：** `/approval/done`

与待办类似但表格增加：审批结果(通过/驳回)、审批时间、审批意见。

### Task F5：审批详情弹窗

**文件：** `frontend/src/components/ApprovalDetailDialog.vue`

展示内容：
- 申请人信息
- 审批流程图（简化文字版）或节点状态列表
- 审批记录时间线
- 关联的业务单据信息（复用 LifecycleDetailDialog）

### Task F6：审批操作弹窗

**文件：** `frontend/src/components/ApprovalActionDialog.vue`

包含：
- 业务单据摘要
- 审批意见输入框（el-input type="textarea"）
- 通过按钮（el-button type="primary"）
- 驳回按钮（el-button type="danger"）

### Task F7：生命周期页面改造

**文件：** Inbound.vue / Receive.vue / Transfer.vue / Repair.vue / Scrap.vue

| 改动项 | 说明 |
|---|---|
| 列表增加「审批状态」列 | 展示 DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED/COMPLETED/CANCELLED 对应的颜色标签 |
| 操作列增加「提交审批」按钮 | DRAFT 状态显示绿色「提交审批」按钮 |
| 创建弹窗行为修改 | 创建后单据状态为 DRAFT，列表刷新后可见 |
| 详情弹窗增加审批记录 | 调用 GET /api/approval/records 展示 |
| 增加「审批状态」筛选 | 查询表单增加审批状态下拉框 |

**注意：** 每个页面的改动完全相同，建议先改造一个页面作为模板，再复制到其他页面。

### Task F8：主导航增加审批菜单

**文件：** `frontend/src/router/index.ts`

新增路由：

```typescript
{
  path: '/approval/todo',
  name: 'Todo',
  component: () => import('@/views/approval/Todo.vue'),
  meta: { title: '我的待办' }
},
{
  path: '/approval/done',
  name: 'Done',
  component: () => import('@/views/approval/Done.vue'),
  meta: { title: '我的已办' }
}
```

**文件：** `frontend/src/layouts/MainLayout.vue`

在主导航中增加「审批管理」菜单分组：

```
审批管理
├── 我的待办
└── 我的已办
```

### Task F9：前端联调

| 编号 | 验证项 |
|---|---|
| F-01 | 我的待办列表加载正常，数据显示正确 |
| F-02 | 点击审批打开操作弹窗 |
| F-03 | 审批通过后自动刷新待办 |
| F-04 | 审批驳回后自动刷新待办 |
| F-05 | 我已办列表显示正确 |
| F-06 | 生命周期页面新增审批状态列 |
| F-07 | DRAFT 单据可提交审批 |
| F-08 | 生命周期页面不报错、不白屏 |
| F-09 | 前端 npm run build 通过 |

---

## 5. Docs Tasks

| 编号 | 任务 | 文件 |
|---|---|---|
| D-01 | 更新 README — 第三阶段标记进行中 | README.md |
| D-02 | 更新数据库设计 — 添加 4 张审批表 | docs/database-design.md |
| D-03 | 更新接口设计 — 添加 7 个审批接口 | docs/api-design.md |
| D-04 | 更新阶段计划 — 第三阶段加入计划路线 | docs/phase-plan.md |
| D-05 | 创建第三阶段验收报告 | docs/test-report-phase-3.md |

---

## 6. Acceptance Tasks

### 验收用例

| 编号 | 用例 | 前置条件 | 期望 |
|---|---|---|---|
| AC-01 | 创建领用单为 DRAFT | 用户登录，存在 IDLE 资产 | 创建后 status=DRAFT，资产状态不变 |
| AC-02 | 提交领用审批 | DRAFT 领用单 | status=SUBMITTED/APPROVING |
| AC-03 | 审批人待办出现该单据 | 审批人登录 | 待办列表包含该单据 |
| AC-04 | 审批通过后资产变 IN_USE | APPROVING 状态 | asset.status=IN_USE，领用单 COMPLETED |
| AC-05 | 审批驳回后资产不变 | APPROVING 状态 | asset.status 不变，单据 REJECTED |
| AC-06 | REJECTED 可重新提交 | REJECTED 单据 | 修改后可重新 submit |
| AC-07 | COMPLETED 不能再次审批 | COMPLETED 单据 | 返回 400 |
| AC-08 | 报废走两级审批 | DRAFT 报废单 | 需资产管理员+财务两人审批 |
| AC-09 | 审批记录可查看 | 有审批历史 | 返回完整审批记录列表 |
| AC-10 | 审批写入操作日志 | 审批操作完成 | asset_operation_log 有对应记录 |
| AC-11 | 第一阶段回归 | 系统正常工作 | 登录/驾驶舱/资产台账正常 |
| AC-12 | 第二阶段回归 | 系统正常工作 | 生命周期页面不白屏，列表可加载 |
| AC-13 | 后端构建 | 代码完整 | mvn -DskipTests package 通过 |
| AC-14 | 前端构建 | 代码完整 | npm run build 通过 |

---

## 7. 推荐实现顺序

```
Step 1:  B1 数据库 migration SQL
Step 2:  B2 实体 + Mapper
Step 3:  B3 DTO / VO
Step 4:  B4 ApprovalService（核心业务逻辑）
Step 5:  B5 LifecycleService 改造
Step 6:  B6 ApprovalController
Step 7:  B7 权限与用户上下文
Step 8:  B8 后端测试（接口测试 + 回归）
Step 9:  F1 审批 API
Step 10: F2 审批类型定义
Step 11: F3 我的待办页面
Step 12: F4 我的已办页面
Step 13: F5 + F6 审批弹窗组件
Step 14: F7 生命周期页面改造
Step 15: F8 主导航增加菜单
Step 16: F9 前端联调
Step 17: D1-D5 文档更新
Step 18: 验收测试（AC-01 到 AC-14）
```

---

## 8. 禁止事项

1. **不能跳过 Tasks 直接开发。** 每个 Task 应对应一个 commit，确保可追溯。
2. **不能直接改坏第二阶段生命周期主链路。** 所有修改应在新增方法中完成，保留第二阶段已有接口。
3. **不能删除原有接口。** 第二阶段 `POST /api/lifecycle/receive`、`POST /api/lifecycle/transfer` 等接口保持不动。
4. **不能跳过验收。** 每个 AC 用例必须逐一验证。
5. **不能只 build 不联调。** 审批流涉及前端交互复杂，必须启动前后端用浏览器验证。
6. **不能跳过回归测试。** 第一阶段和第二阶段的已有功能必须重新验证。
7. **不能在未提交 Spec 和 Design 的情况下修改代码。** 第三阶段开发前必须先确认 Spec 和 Design 已定稿。
