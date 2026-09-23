# 第三阶段：审批流 · 设计文档

## 1. 数据库设计

### 1.1 设计思路

本阶段新增 4 张表，与已有生命周期单据表（asset_receive_order 等）通过 business_type + business_id 关联，不修改现有的生命周期业务表结构。

`approval_flow` 和 `approval_node` 表用于预定义审批规则（四级业务类型各一套节点），`approval_instance` 记录每次审批的实例和当前状态，`approval_record` 记录每一步审批的操作明细。

### 1.2 表结构

#### approval_flow（审批模板）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| flow_code | VARCHAR(64) | NOT NULL UNIQUE | 审批模板编码，如 APPROVE_RECEIVE |
| flow_name | VARCHAR(128) | NOT NULL | 审批模板名称 |
| business_type | VARCHAR(32) | NOT NULL | 业务类型：RECEIVE / TRANSFER / REPAIR / SCRAP |
| enabled | TINYINT | NOT NULL DEFAULT 1 | 是否启用 |
| remark | VARCHAR(500) | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

预计初始化数据：4 条（领用/调拨/维修/报废各一条）。

#### approval_node（审批节点）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| flow_id | BIGINT | NOT NULL | 关联审批模板 ID |
| node_code | VARCHAR(64) | NOT NULL | 节点编码，如 DEPT_HEAD_APPROVE |
| node_name | VARCHAR(128) | NOT NULL | 节点名称 |
| approver_role | VARCHAR(64) | NOT NULL | 审批角色编码：DEPT_HEAD / ASSET_MANAGER / FINANCE |
| sort_order | INT | NOT NULL | 节点排序，从 1 开始 |
| required | TINYINT | NOT NULL DEFAULT 1 | 是否必须审批 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

预计初始化数据：

| 业务类型 | 节点 1 | 节点 2 |
|---|---|---|
| 领用 | DEPT_HEAD (1) | — |
| 调拨 | ASSET_MANAGER (1) | — |
| 维修 | ASSET_MANAGER (1) | — |
| 报废 | ASSET_MANAGER (1) | FINANCE (2) |

#### approval_instance（审批实例）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| business_type | VARCHAR(32) | NOT NULL | 业务类型 |
| business_id | BIGINT | NOT NULL | 业务单据 ID（如 asset_receive_order.id） |
| flow_id | BIGINT | NOT NULL | 审批模板 ID |
| current_node_id | BIGINT | DEFAULT NULL | 当前待处理节点 ID，审批完成时为 NULL |
| status | VARCHAR(32) | NOT NULL | 审批状态：SUBMITTED / APPROVING / APPROVED / REJECTED |
| started_by | BIGINT | NOT NULL | 发起审批的用户 ID |
| started_at | DATETIME | NOT NULL | 发起时间 |
| completed_at | DATETIME | DEFAULT NULL | 全部完成时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

索引：
- INDEX idx_business (business_type, business_id)
- INDEX idx_status (status)
- INDEX idx_approver (current_node_id)

#### approval_record（审批记录）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| instance_id | BIGINT | NOT NULL | 审批实例 ID |
| node_id | BIGINT | NOT NULL | 审批节点 ID |
| approver_id | BIGINT | NOT NULL | 审批人用户 ID |
| approver_name | VARCHAR(64) | NOT NULL | 审批人姓名 |
| action | VARCHAR(32) | NOT NULL | 审批动作：APPROVED / REJECTED |
| comment | VARCHAR(500) | DEFAULT NULL | 审批意见 |
| status | VARCHAR(32) | NOT NULL | 该节点处理后的状态 |
| approved_at | DATETIME | NOT NULL | 审批时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

索引：
- INDEX idx_instance (instance_id)
- INDEX idx_approver (approver_id)

### 1.3 核心关联关系

```
approval_flow 1──N approval_node
approval_instance N──1 approval_flow
approval_instance 1──N approval_record
approval_record N──1 approval_node

approval_instance.business_type + business_id
    ──关联──► asset_receive_order / asset_transfer_order / asset_repair_order / asset_scrap_order
```

---

## 2. 后端包结构

```
backend/src/main/java/com/example/asset/approval/
├── controller/
│   └── ApprovalController.java
├── service/
│   ├── ApprovalService.java          # 审批流程核心业务
│   └── ApprovalFlowService.java      # 审批模板配置
├── mapper/
│   ├── ApprovalFlowMapper.java       # BaseMapper
│   ├── ApprovalNodeMapper.java       # BaseMapper
│   ├── ApprovalInstanceMapper.java   # BaseMapper + 分页查询
│   └── ApprovalRecordMapper.java     # BaseMapper
├── entity/
│   ├── ApprovalFlow.java
│   ├── ApprovalNode.java
│   ├── ApprovalInstance.java
│   └── ApprovalRecord.java
├── dto/
│   ├── ApprovalSubmitRequest.java    # businessType, businessId, remark
│   ├── ApprovalActionRequest.java    # action(APPROVED/REJECTED), comment
│   └── ApprovalPageRequest.java      # pageNum, pageSize, status 等筛选条件
└── vo/
    ├── TodoPageVO.java               # 待办分页 VO（含业务单据摘要信息）
    ├── DonePageVO.java               # 已办分页 VO
    ├── ApprovalRecordVO.java         # 审批记录 VO
    └── ApprovalDetailVO.java         # 审批实例详情 VO
```

---

## 3. Entity 设计

四个 Entity 按 1.2 节表结构设计，使用 `@Data`、`@TableName`、`@TableId(type = IdType.AUTO)` 注解，日期字段使用 `@JsonFormat`。

无特殊业务逻辑，均为纯数据映射。

---

## 4. DTO 设计

### ApprovalSubmitRequest

```
businessType: String @NotBlank  # RECEIVE / TRANSFER / REPAIR / SCRAP
businessId: Long @NotNull       # 业务单据 ID
remark: String                  # 提交时的备注
```

### ApprovalActionRequest

```
action: String @NotBlank  # APPROVED / REJECTED
comment: String           # 审批意见（驳回时建议必填）
```

### ApprovalPageRequest

```
pageNum: Long @Min(1)     # 默认 1
pageSize: Long @Min(1)    # 默认 10
status: String            # 筛选状态
businessType: String      # 筛选业务类型
```

---

## 5. VO 设计

TodoPageVO / DonePageVO 需要包含：

```
instanceId, businessType, businessId
orderCode, assetCode, assetName    # 从业务单据关联查询
nodeName                           # 当前待审批节点名称
status                             # 审批状态
startedAt                          # 发起时间
approverName                       # 审批人（已办）
action                             # 审批动作（已办）
comment                            # 审批意见（已办）
```

核心设计考虑：VO 需要联表查询业务单据和资产信息，应该在 Mapper 中通过自定义 SQL 或 MyBatis-Plus 多表关联实现，而不是在 Service 中逐条循环查询。

---

## 6. Mapper 设计

| Mapper | 基础方法 | 自定义查询 |
|---|---|---|
| ApprovalFlowMapper | CRUD | findByBusinessType |
| ApprovalNodeMapper | CRUD | findByFlowIdOrderBySort |
| ApprovalInstanceMapper | CRUD + 分页 | todoPage（联表查询待办）、donePage（联表查询已办） |
| ApprovalRecordMapper | CRUD | findByInstanceIdOrderByCreatedAt |

**todoPage 查询逻辑：**

```
SELECT ai.*, an.node_name, 
       (CASE ai.business_type 
        WHEN 'RECEIVE' THEN (SELECT order_code FROM asset_receive_order WHERE id = ai.business_id)
        ...) AS order_code
FROM approval_instance ai
JOIN approval_node an ON ai.current_node_id = an.id
WHERE ai.status IN ('SUBMITTED', 'APPROVING')
  AND an.approver_role = (当前用户角色)
ORDER BY ai.started_at DESC
```

---

## 7. Service 设计

### ApprovalService

| 方法 | 说明 |
|---|---|
| submit(businessType, businessId) | 提交审批：创建审批实例，更新单据状态为 SUBMITTED，设置第一个审批节点 |
| approve(instanceId, userId, comment) | 审批通过：写入审批记录；若还有下一节点，更新 current_node_id；若全部节点通过，执行状态流转 |
| reject(instanceId, userId, comment) | 审批驳回：写入审批记录，更新 instance.status = REJECTED，单据状态回到 DRAFT |
| todoPage(query) | 查询当前用户的待办列表 |
| donePage(query) | 查询当前用户已处理的审批记录 |
| getRecords(businessType, businessId) | 查询指定单据的审批记录 |
| getDetail(instanceId) | 查询审批实例详情 |

### 审批通过后的状态流转

核心设计：`approve()` 方法在最后一个节点审批通过后，根据 `businessType` 调用对应的生命周期流转方法。具体来说：

- 审批通过 → 将单据状态从 APPROVED 更新为 COMPLETED
- 调用 LifecycleService 中对应的方法（如 receive()、transfer() 等）
- 这些方法是第二阶段已实现并被验证过的，第三阶段只需在审批通过后触发即可

**关键设计决策：** 第二阶段的生命周期创建方法（如 `createReceive()`、`createTransfer()` 等）在审批模式下不应再直接执行流转。需要做以下改造：

- 第二阶段的方法保持不动（向后兼容）
- 审批模式下，单据创建时只写入业务表，不修改 asset 表状态
- 审批通过后，由 `ApprovalService` 调用一个轻量的「执行流转」方法，该方法只做 asset 状态更新和日志写入

### 角色与审批人的匹配

当前系统的用户表有 `sys_user_role` 关联角色。但当前版本中「部门负责人」这个角色需要根据用户的 `department` 字段判断——审批人属于同一个部门的用户才能审批本部门的申请。

**简化方案：** 第一阶段使用固定的角色判断，不实现部门级数据权限。即：

- 拥有 ASSET_MANAGER 角色的用户可以审批调拨/维修/报废
- 拥有 FINANCE 角色的用户可以审批报废的第二级
- DEPT_HEAD 角色的判断暂用 ADMIN 替代（或后续新增角色）

---

## 8. Controller 设计

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
    public Result<PageResult<TodoPageVO>> todoPage(@Valid ApprovalPageRequest req)

    @GetMapping("/done/page")
    public Result<PageResult<DonePageVO>> donePage(@Valid ApprovalPageRequest req)

    @GetMapping("/records")
    public Result<List<ApprovalRecordVO>> records(
            @RequestParam String businessType,
            @RequestParam Long businessId)

    @GetMapping("/{instanceId}")
    public Result<ApprovalDetailVO> detail(@PathVariable Long instanceId)
}
```

---

## 9. 生命周期接口改造方案

### 方案比较

| 维度 | 方案 A：保留原有接口+参数 | 方案 B：创建后生成 DRAFT |
|---|---|---|
| 改动量 | 小，现有接口加一个 submitForApproval 参数 | 大，现有创建接口需要改为不直接完成 |
| 向后兼容 | 第二阶段已有功能不受影响 | 第二阶段已有的「提交即完成」逻辑需要改造 |
| 客户端适配 | 前端加一个参数判断是否提交审批 | 所有生命周期页面都需要调整创建逻辑 |
| 数据一致性 | 创建时如果已执行流转，审批通过后不能再流转 | 更清晰：创建→审批→流转，三步独立 |

**推荐方案 B：创建单据后只生成 DRAFT，不直接完成。**

理由：
1. 审批模式下的正确流程是「创建草稿 → 提交审批 → 审批通过 → 执行流转」，方案 A 容易出现「创建时已流转、审批通过后不知该如何处理」的矛盾。
2. 维修单在第二阶段已经是 DRAFT→COMPLETED 模式，已有先例，方案 B 是将其推广到所有单据类型。
3. 方案 A 的 submitForApproval 参数增加了接口的复杂度，方案 B 的流程更清晰、更容易理解和维护。

### 具体改造方式

**当前 createReceive 的行为（第二阶段）：**
1. 写入 asset_receive_order
2. 修改 asset.status = IN_USE
3. 写入 operation_log

**审批模式下的行为（第三阶段）：**
1. 写入 asset_receive_order（status = DRAFT）
2. 不修改 asset 表
3. 不写入 operation_log
4. 申请人点击「提交审批」→ 进入审批流程
5. 审批通过后 → 执行流转（修改 asset 表 + 写入 operation_log + 单据置为 COMPLETED）

### 旧接口兼容

为保持第二阶段已有功能的完整性（如测试时可能还需要「直接完成」模式），建议保留第二阶段的接口不变。

第三阶段新增的审批模式下，前端调用的是相同的 createReceive 等接口，但新增一个参数或通过配置开关来控制是否启用审批。

**最简实现方案：直接将 create 接口的行为改为先创建 DRAFT。** 第二阶段已验收通过，第三阶段是在其基础上的升级，审批模式是唯一的业务流程——不需要同时保留两种模式。

---

## 10. 前端页面设计

### 10.1 新增页面

#### 我的待办（/approval/todo）

```
┌─────────────────────────────────────────────────────────┐
│  我的待办                             查询 │ 重置 │    │
├────────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┤
│ 单据编号 │ 类型  │ 资产  │ 申请人 │ 提交时间 │ 当前节点 │ 操作  │
├────────┼──────┼──────┼──────┼──────┼──────┼──────┤
│ RE...  │ 领用  │ 电脑  │ 张三  │ 2026-07 │ 部门负责人│ 审批  │
├────────┼──────┼──────┼──────┼──────┼──────┼──────┤
│ SC...  │ 报废  │ 车辆  │ 李四  │ 2026-07 │ 财务审批 │ 审批  │
└────────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┘
                                   [上一页] 1 2 3 [下一页]
```

点击「审批」打开审批详情弹窗，包含：
- 业务单据详情（复用 LifecycleDetailDialog，显示该单据的完整信息）
- 审批历史记录（按时间倒序展示）
- 审批操作区（通过 / 驳回 + 审批意见输入框）

#### 我的已办（/approval/done）

与待办类似，增加「审批结果」列（通过/驳回）和「审批时间」列。

### 10.2 改造页面

#### 生命周期页面（Inbound/Receive/Transfer/Repair/Scrap.vue）

每个页面的修改内容：

1. **列表新增列**：在表格中增加「审批状态」列（颜色标签），展示 DRAFT / SUBMITTED / APPROVING / APPROVED / REJECTED / COMPLETED / CANCELLED。

2. **新增按钮**：当单据状态为 DRAFT 时，操作列增加「提交审批」按钮（使用绿色主题，区别于普通的编辑/删除按钮）。

3. **创建弹窗行为修改**：创建单据后，不再自动执行业务流转。弹窗关闭后，列表中出现一条 DRAFT 状态的单据。

4. **详情弹窗改造**：在现有 LifecycleDetailDialog 中新增「审批记录」区域，调用 `/api/approval/records` 接口展示审批历史。

5. **状态标签改造**：为审批状态设计不同的标签颜色：
   - DRAFT：灰色
   - SUBMITTED：蓝色
   - APPROVING：橙色
   - APPROVED：绿色
   - REJECTED：红色
   - COMPLETED：深绿
   - CANCELLED：灰色

---

## 11. 权限设计

| 操作 | 普通员工 | 部门负责人 | 资产管理员 | 财务人员 | 审计人员 | 系统管理员 |
|---|---|---|---|---|---|---|
| 创建领用/维修单据 | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| 提交审批 | ✅（自己的单据） | ✅ | ✅ | ❌ | ❌ | ✅ |
| 审批领用 | ❌ | ✅（本部门） | ❌ | ❌ | ❌ | ❌ |
| 审批调拨 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 审批维修 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 审批报废（一级） | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 审批报废（二级） | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| 查看待办/已办 | ❌ | ✅（本部门） | ✅ | ✅ | ❌ | ✅ |
| 查看审批记录 | ✅（自己的） | ✅（本部门） | ✅ | ✅ | ✅ | ✅ |
| 管理审批配置 | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

> **注意：** 角色权限的第一阶段实现采用简化的角色编码判断，部门级数据权限暂不实现。例如「普通员工」和「部门负责人」在当前用户表中用角色编码区分，如果当前没有这两个角色，可暂不引入，由系统管理员统一代为完成审批操作。

---

## 12. 与第二阶段的边界

### 本阶段保留的

- 第二阶段的所有 lifecycle 接口保持不动（不做删除、不做重命名）
- `asset` 表和业务单据表的结构不变
- `asset_operation_log` 的使用方式不变
- 驾驶舱、资产台账、资产详情、资产分类等第一阶段功能不变
- 已有的 CRUD 操作（编辑单据、删除单据）仍然可用

### 本阶段改造的

- 单据创建后的状态流转不再立即执行，改为审批通过后执行
- 生命周期页面列表增加「审批状态」列和「提交审批」按钮
- 已有单据的详情弹窗增加审批记录区域

### 审批通过后如何执行状态流转

核心设计原则：**复用，不重写。**

ApprovalService 在最后一个节点审批通过后，根据 businessType 调用 LifecycleService 中已有的流转逻辑。

不过需要将第二阶段中创建方法（createReceive 等）内的「状态变更+日志写入」逻辑提取为一个独立方法，供审批模式调用。例如：

```java
// 第二阶段已有的完整方法（保留不动）
@Transactional
public Long createReceive(ReceiveCreateRequest req) {
    // 校验 + 写入 receive_order + 改 asset 状态 + 写日志
}

// 第三阶段新增：只执行流转（供审批模式调用）
@Transactional
public void executeReceiveFlow(Long orderId) {
    // 从 receive_order 读取数据
    // 改 asset 状态 + 写日志
}
```

这种做法不影响第二阶段验收通过的代码，同时为审批模式提供了可复用的流转能力。

---

## 13. 日志设计

| 操作 | 日志类型 | 日志内容 |
|---|---|---|
| 提交审批 | SUBMIT_APPROVAL | 创建审批实例 |
| 审批通过（每个节点） | APPROVE | 审批节点名称 + 审批意见 |
| 审批驳回 | REJECT | 驳回原因 |
| 审批流转完成 | APPROVAL_COMPLETE | 审批通过，执行业务流转 |

其中，`APPROVE`、`REJECT`、`APPROVAL_COMPLETE` 写入 `asset_operation_log`，`SUBMIT_APPROVAL` 建议也写入（涉及资产操作审计）。

---

## 14. 任务拆分建议

### Backend

| 编号 | 任务 | 优先级 | 依赖 |
|---|---|---|---|
| B-01 | 创建 migration-v3-approval.sql（4 张表） | P0 | 无 |
| B-02 | 创建 ApprovalFlow / ApprovalNode / ApprovalInstance / ApprovalRecord Entity | P0 | B-01 |
| B-03 | 创建 4 个 Mapper | P0 | B-02 |
| B-04 | 创建 DTO 和 VO | P0 | 无 |
| B-05 | 实现 ApprovalService（submit / approve / reject） | P0 | B-03 |
| B-06 | 实现 ApprovalService（todoPage / donePage / records） | P0 | B-03 |
| B-07 | 从 LifecycleService 提取 executeReceiveFlow / executeTransferFlow 等方法 | P0 | B-05 |
| B-08 | 创建 ApprovalController | P0 | B-05 |
| B-09 | 初始化审批模板数据（4 条 flow + 5 条 node） | P0 | B-01 |
| B-10 | 初始化审批配置 SQL | P0 | B-09 |
| B-11 | 后端测试 | P1 | B-08 |

### Frontend

| 编号 | 任务 | 优先级 |
|---|---|---|
| F-01 | 创建 api/approval.ts | P0 |
| F-02 | 创建我的待办页面 | P0 |
| F-03 | 创建我的已办页面 | P0 |
| F-04 | 创建审批详情弹窗（复用 LifecycleDetailDialog） | P0 |
| F-05 | 改造生命周期页面：增加审批状态列 | P0 |
| F-06 | 改造生命周期页面：增加提交审批按钮 | P0 |
| F-07 | 改造生命周期页面：创建后不自动完成 | P0 |
| F-08 | 状态标签组件增加审批状态支持 | P0 |
| F-09 | 路由配置新增待办/已办路由 | P0 |
| F-10 | 联调测试 | P1 |

### Docs

| 编号 | 任务 |
|---|---|
| D-01 | 更新 README.md |
| D-02 | 更新 docs/database-design.md |
| D-03 | 更新 docs/api-design.md |
| D-04 | 输出测试报告 |

---

## 15. 核心风险与缓解措施

| 风险 | 影响 | 缓解 |
|---|---|---|
| 审批改造破坏第二阶段已有流程 | 已有功能不可用 | 保留第二阶段所有接口不动；在 ApprovalService 中新增方法，不修改 LifecycleService 已有方法签名 |
| 部门负责人角色在用户表中不存在 | 审批人无法匹配 | 第一阶段用角色编码判断。如果现有的 ADMIN 角色承担审批职能，可以先由 ADMIN 代为审批 |
| 界面改动影响已有用户体验 | 用户不适应 | 仅在列表增加一列和一个按钮，不影响已有的查询、查看详情操作 |
