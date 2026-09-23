# Phase 7.1：资产生命周期时间线 - 技术设计

> 状态：正式设计
> 对应规格：`docs/sdd/phase-7-asset-timeline-spec.md`

## 1. 接口设计

### 接口

```
GET /api/assets/{assetId}/timeline?eventType=INBOUND
```

- 路径参数：`assetId`（Long，必填）
- 查询参数：`eventType`（String，可选，为空返回全部）
- 权限：`@RequirePermission("asset:view")`
- 返回：`Result<List<AssetTimelineEventVO>>`

### eventType 枚举值

| 值 | 含义 |
|---|---|
| INBOUND | 入库 |
| RECEIVE | 领用 |
| TRANSFER | 调拨 |
| REPAIR | 维修 |
| SCRAP | 报废 |
| APPROVAL | 审批 |
| OPERATION_LOG | 操作日志 |

### 排序规则

所有事件按 `eventTime` 降序排列（最新事件在前）。

### 筛选规则

- `eventType` 为空：返回全部事件
- `eventType` 不为空：只返回指定类型事件

## 2. VO 结构

### AssetTimelineQueryRequest

```java
// 位于：com.example.asset.asset.dto
private String eventType;  // 可选筛选参数
```

### AssetTimelineEventVO

```java
// 位于：com.example.asset.asset.vo
private String id;              // 事件唯一标识（eventType + "-" + 业务id）
private Long assetId;           // 资产ID
private String eventType;       // 事件类型
private String eventTypeName;   // 事件类型中文名
private String title;           // 事件标题
private String description;     // 事件描述
private String orderCode;       // 单据编号（操作日志为空）
private String businessType;    // 业务类型（操作日志为空）
private Long businessId;        // 业务单据ID（操作日志为空）
private String status;          // 单据/审批状态
private String beforeStatus;    // 变更前状态
private String afterStatus;     // 变更后状态
private String operatorName;    // 操作人
private LocalDateTime eventTime;// 事件时间
private String source;          // 数据来源表名
private String remark;          // 备注
```

## 3. 后端类设计

### 文件位置

所有新增文件位于 `backend/src/main/java/com/example/asset/asset/` 下：

```
asset/asset/
├── controller/
│   └── AssetTimelineController.java   # 新增
├── dto/
│   └── AssetTimelineQueryRequest.java # 新增
├── service/
│   └── AssetTimelineService.java      # 新增
└── vo/
    └── AssetTimelineEventVO.java      # 新增
```

### AssetTimelineController

```java
@RestController
@RequestMapping("/api/assets")
public class AssetTimelineController {
    private final AssetTimelineService assetTimelineService;

    @GetMapping("/{assetId}/timeline")
    @RequirePermission("asset:view")
    public Result<List<AssetTimelineEventVO>> getTimeline(
            @PathVariable Long assetId,
            AssetTimelineQueryRequest query) {
        return Result.success(assetTimelineService.getTimeline(assetId, query));
    }
}
```

注意：`@RequestMapping("/api/assets")` 与 `AssetController` 相同，但方法路径 `/{assetId}/timeline` 不与 `AssetController` 的 `/{id}` 冲突（Spring 按路径段数量区分）。

### AssetTimelineService

注入 9 个 Mapper：

| Mapper | 所在包 | 用途 |
|---|---|---|
| AssetMapper | asset.asset.mapper | 校验资产存在 |
| AssetOperationLogMapper | asset.asset.mapper | 查操作日志 |
| InboundOrderMapper | asset.lifecycle.mapper | 查入库单 |
| ReceiveOrderMapper | asset.lifecycle.mapper | 查领用单 |
| TransferOrderMapper | asset.lifecycle.mapper | 查调拨单 |
| RepairOrderMapper | asset.lifecycle.mapper | 查维修单 |
| ScrapOrderMapper | asset.lifecycle.mapper | 查报废单 |
| ApprovalInstanceMapper | asset.approval.mapper | 查审批实例 |
| ApprovalRecordMapper | asset.approval.mapper | 查审批记录 |

### 查询逻辑

```
1. 校验资产存在（deleted=0），不存在抛 BusinessException(NOT_FOUND, "资产不存在")

2. 查操作日志：asset_operation_log WHERE asset_id = ? → 映射 OPERATION_LOG
   eventTime = operationTime
   title = operationName（空则"操作日志"）
   operatorName = operatorName
   beforeStatus/afterStatus = 日志字段
   description = 操作名称 + 状态变化 + 备注

3. 查5类生命周期单据（均 WHERE asset_id = ?）：
   - asset_inbound_order → INBOUND
   - asset_receive_order → RECEIVE
   - asset_transfer_order → TRANSFER
   - asset_repair_order → REPAIR
   - asset_scrap_order → SCRAP
   eventTime = createdAt
   orderCode = 单据编号
   businessType = 对应类型
   businessId = 单据id
   status = 单据状态
   beforeStatus/afterStatus = 单据字段
   description = 单据编号 + 业务动作 + 状态 + 备注

4. 审批事件聚合：
   4.1 收集当前资产的 RECEIVE/TRANSFER/REPAIR/SCRAP 四类单据 id
   4.2 对每个 (businessType, businessId) 查 approval_instance
   4.3 对每个 instance 查 approval_record（按 created_at ASC）
   4.4 映射 APPROVAL 事件：
       eventTime = approvedAt（空则 createdAt）
       title = 按 action 映射（SUBMIT→提交审批, APPROVED→审批通过, REJECTED→审批驳回）
       operatorName = approverName（空则"系统"）
       description = 审批动作 + 审批意见(comment) + 审批后状态

5. 合并所有事件，按 eventType 过滤，按 eventTime 降序排序
```

### 关键实体字段参考

**AssetOperationLog**：id, assetId, operationType, operationName, beforeStatus, afterStatus, operatorId, operatorName, operationTime, remark

**InboundOrder**：id, orderCode, assetId, inboundType, supplier, purchaseOrderNo, inboundDate, handler, beforeStatus, afterStatus, status, remark, createdBy, createdAt, updatedAt

**ReceiveOrder**：id, orderCode, assetId, receiver, receiverDepartment, receiveDate, usagePurpose, beforeStatus, afterStatus, status, remark, createdBy, createdAt, updatedAt

**TransferOrder**：id, orderCode, assetId, fromDepartment, toDepartment, fromLocation, toLocation, fromKeeper, toKeeper, transferDate, beforeStatus, afterStatus, status, remark, createdBy, createdAt, updatedAt

**RepairOrder**：id, orderCode, assetId, faultDescription, repairVendor, repairCost, repairStartDate, repairEndDate, repairResult, beforeStatus, afterStatus, status, remark, createdBy, createdAt, updatedAt

**ScrapOrder**：id, orderCode, assetId, scrapReason, scrapDate, disposalMethod, residualValue, beforeStatus, afterStatus, status, remark, createdBy, createdAt, updatedAt

**ApprovalInstance**：id, businessType, businessId, flowId, currentNodeId, status, startedBy, startedAt, completedAt, createdAt, updatedAt

**ApprovalRecord**：id, instanceId, nodeId, approverId, approverName, action, comment, status, approvedAt, createdAt

## 4. 前端设计

### API 层（asset.ts）

新增类型和函数，不修改已有函数：

```typescript
export interface AssetTimelineEvent {
  id: string
  assetId: number
  eventType: string
  eventTypeName: string
  title: string
  description: string
  orderCode: string | null
  businessType: string | null
  businessId: number | null
  status: string | null
  beforeStatus: string | null
  afterStatus: string | null
  operatorName: string | null
  eventTime: string
  source: string
  remark: string | null
}

export function getAssetTimeline(assetId: number, params?: { eventType?: string }) {
  return request.get<any, { code: number; message: string; data: AssetTimelineEvent[] }>(
    `/assets/${assetId}/timeline`, { params }
  )
}
```

### 组件（AssetTimeline.vue）

路径：`frontend/src/views/asset/components/AssetTimeline.vue`

- Props：`assetId: number`
- onMounted 自动加载
- watch eventType 变化重新加载
- 顶部：左侧标题「生命周期时间线」，右侧 el-select 筛选
- 使用 el-timeline / el-timeline-item
- 每条事件展示：title、eventTypeName 标签、eventTime、operatorName、description、状态变化、单据编号、备注
- 加载中：loading
- 空数据：el-empty「暂无生命周期记录」
- 请求失败：只影响组件内部，不影响资产详情主体

### 集成（AssetDetail.vue）

- 保留原有 PageHeader、el-descriptions、返回按钮、getAssetDetail 逻辑
- 基础信息卡下方新增时间线区域
- 仅 asset 存在时渲染 `<AssetTimeline :asset-id="Number(route.params.id)" />`
- 最大宽度从 900px 调整到 1100px

## 5. 不新增 XML Mapper

所有查询使用 `BaseMapper.selectList(LambdaQueryWrapper)` 完成，不新增 XML。

## 6. 路由不冲突说明

- `AssetController`：`@GetMapping("/{id}")` → 匹配 `/api/assets/123`（1 个路径段）
- `AssetTimelineController`：`@GetMapping("/{assetId}/timeline")` → 匹配 `/api/assets/123/timeline`（2 个路径段）

Spring MVC 按路径段数量区分，不存在冲突。
