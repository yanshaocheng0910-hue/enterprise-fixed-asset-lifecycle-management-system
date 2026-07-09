# Phase 14 - 全局操作日志与审计页面 技术设计 (Design)

## 1. 整体架构

```
前端 AuditLog.vue
  ├─ 审计统计卡片 (AuditSummaryVO)
  ├─ 筛选区 (logType/assetCode/assetName/operatorName/dateRange)
  ├─ 日志表格 (AuditLogVO 分页)
  ├─ 日志详情弹窗
  └─ 导出 Excel 按钮
        │
        ▼ axios
frontend/src/api/audit.ts
  ├─ getAuditLogPage
  ├─ getAuditSummary
  ├─ getAuditLogDetail
  └─ exportAuditLogs
        │
        ▼ HTTP
后端 AuditController (/api/audit/logs/*)
  └─ AuditService
       ├─ AuditMapper (XML: 4 个来源查询)
       │   ├─ selectAssetOperationLogs   (asset_operation_log JOIN asset)
       │   ├─ selectApprovalAuditLogs     (approval_record JOIN approval_instance JOIN lifecycle UNION)
       │   ├─ selectInventoryAbnormalLogs (inventory_record JOIN asset JOIN inventory_task WHERE result != 'NORMAL')
       │   └─ selectFinanceSyncLogs       (finance_sync_record)
       ├─ 多源合并 → 统一 AuditLogVO
       ├─ 内存筛选 (logType/assetCode/assetName/operatorName/dateRange)
       ├─ 按 operationTime 倒序排序
       └─ 内存分页
ExportController 新增 /api/export/audit/logs
  └─ 复用 AuditService + ExcelExportUtil
```

## 2. 数据来源映射

### 2.1 资产操作 (ASSET_OPERATION)
- 来源表：`asset_operation_log` a JOIN `asset` b ON a.asset_id = b.id
- 字段映射：
  - id = `'ASSET-' || a.id`
  - logType = `ASSET_OPERATION`，logTypeName = `资产操作`
  - assetId = a.asset_id，assetCode = b.asset_code，assetName = b.asset_name
  - businessType = a.operation_type，operation = a.operation_name
  - beforeStatus = a.before_status，afterStatus = a.after_status
  - operatorName = a.operator_name，operationTime = a.operation_time
  - remark = a.remark，source = `ASSET`

### 2.2 审批操作 (APPROVAL)
- 来源表：`approval_record` ar JOIN `approval_instance` ai ON ar.instance_id = ai.id
  - LEFT JOIN 生命周期单据取资产（UNION ALL 子查询映射 business_type+business_id → asset_id）
  - LEFT JOIN `asset` b 取资产编号/名称
- 字段映射：
  - id = `'APPROVAL-' || ar.id`
  - logType = `APPROVAL`，logTypeName = `审批操作`
  - assetId = 子查询资产 ID，assetCode/assetName = b 字段
  - businessType = ai.business_type，businessId = ai.business_id
  - operation = ar.action（SUBMIT/APPROVED/REJECTED）
  - beforeStatus/afterStatus = ar.status
  - operatorName = ar.approver_name（SUBMIT 时为空，由 Service 补充发起人）
  - operationTime = ar.approved_at（SUBMIT 时用 ar.created_at）
  - remark = ar.comment，source = `APPROVAL`

**资产关联子查询**（UNION ALL 映射 4 种业务单据 → asset_id）：
```sql
SELECT 'RECEIVE' AS bt, id AS oid, asset_id FROM asset_receive_order
UNION ALL SELECT 'TRANSFER', id, asset_id FROM asset_transfer_order
UNION ALL SELECT 'REPAIR', id, asset_id FROM asset_repair_order
UNION ALL SELECT 'SCRAP', id, asset_id FROM asset_scrap_order
```
通过 `ai.business_type = sub.bt AND ai.business_id = sub.oid` 关联。

### 2.3 盘点异常 (INVENTORY_ABNORMAL)
- 来源表：`inventory_record` ir JOIN `asset` b ON ir.asset_id = b.id JOIN `inventory_task` t ON ir.task_id = t.id
- 筛选：`ir.result != 'NORMAL'`
- 字段映射：
  - id = `'INVENTORY-' || ir.id`
  - logType = `INVENTORY_ABNORMAL`，logTypeName = `盘点异常`
  - assetId/assetCode/assetName 来自 asset
  - businessType = `INVENTORY`，businessId = ir.task_id
  - operation = ir.result（LOCATION_MISMATCH/KEEPER_MISMATCH/LOST/EXTRA）
  - operatorName = ir.actual_keeper（实际保管人）
  - operationTime = ir.scanned_at
  - remark = ir.remark，source = `INVENTORY`

### 2.4 财务同步 (FINANCE_SYNC)
- 来源表：`finance_sync_record` f
- 字段映射：
  - id = `'FINANCE-' || f.id`
  - logType = `FINANCE_SYNC`，logTypeName = `财务同步`
  - assetId = null，assetCode/assetName = null（批量同步无单一资产）
  - businessType = `FINANCE`，businessId = f.id
  - operation = f.status（SUCCESS/FAILED）
  - beforeStatus/afterStatus = null
  - operatorName = f.operator_name
  - operationTime = f.created_at
  - remark = f.remark，source = `FINANCE`

## 3. 后端文件清单

### 3.1 新增文件
| 路径 | 说明 |
|---|---|
| `backend/src/main/java/com/example/asset/audit/vo/AuditLogVO.java` | 审计日志 VO |
| `backend/src/main/java/com/example/asset/audit/vo/AuditSummaryVO.java` | 审计统计 VO |
| `backend/src/main/java/com/example/asset/audit/dto/AuditLogQueryRequest.java` | 查询请求 DTO |
| `backend/src/main/java/com/example/asset/audit/mapper/AuditMapper.java` | 审计 Mapper 接口 |
| `backend/src/main/resources/mapper/audit/AuditMapper.xml` | 审计 Mapper XML（4 个来源查询） |
| `backend/src/main/java/com/example/asset/audit/service/AuditService.java` | 审计 Service（多源合并+筛选+分页） |
| `backend/src/main/java/com/example/asset/audit/controller/AuditController.java` | 审计 Controller |

### 3.2 修改文件
| 路径 | 修改内容 |
|---|---|
| `backend/.../export/controller/ExportController.java` | 新增 `/api/export/audit/logs` 端点，注入 AuditService |

## 4. 后端接口设计

### 4.1 AuditController
```
@RestController
@RequestMapping("/api/audit/logs")
- GET /summary       → Result<AuditSummaryVO>         @RequirePermission("approval:audit")
- GET /page          → Result<PageResult<AuditLogVO>> @RequirePermission("approval:audit")
- GET /{id}          → Result<AuditLogVO>             @RequirePermission("approval:audit")
```

### 4.2 ExportController 新增
```
- GET /api/export/audit/logs → void (Excel)  @RequirePermission("approval:audit")
```

### 4.3 AuditLogQueryRequest 字段
- logType: String
- assetCode: String
- assetName: String
- operatorName: String
- startDate: String (yyyy-MM-dd)
- endDate: String (yyyy-MM-dd)
- pageNum: Long (默认 1)
- pageSize: Long (默认 10)

## 5. AuditService 核心逻辑

参考 `WarningService` 的「多源收集 + 内存分页」模式：

```java
public PageResult<AuditLogVO> page(AuditLogQueryRequest req) {
    List<AuditLogVO> all = collectAll();
    // 内存筛选
    List<AuditLogVO> filtered = all.stream()
        .filter(by logType)
        .filter(by assetCode contains)
        .filter(by assetName contains)
        .filter(by operatorName contains)
        .filter(by dateRange)
        .sorted(by operationTime desc)
        .collect();
    // 内存分页
    int from = (pageNum-1) * pageSize;
    return new PageResult<>(subList, total, pageNum, pageSize);
}
```

- `collectAll()`：并行调用 4 个 Mapper 方法，合并结果
- 统计接口直接调用各 Mapper 的 count 方法（或复用 collectAll 计算）

## 6. 前端文件清单

### 6.1 新增文件
| 路径 | 说明 |
|---|---|
| `frontend/src/api/audit.ts` | 审计 API 封装（4 个方法） |
| `frontend/src/views/audit/AuditLog.vue` | 审计日志页面 |

### 6.2 修改文件
| 路径 | 修改内容 |
|---|---|
| `frontend/src/router/index.ts` | 新增 `/audit/logs` 路由，meta.permission = `approval:audit` |
| `frontend/src/layouts/MainLayout.vue` | 新增「审计追踪」菜单项 |

## 7. 前端页面结构 (AuditLog.vue)

```
┌─────────────────────────────────────────────────┐
│ 审计统计卡片区（5 个 el-card）                    │
│ [今日操作] [资产变更] [审批操作] [盘点异常] [财务同步] │
├─────────────────────────────────────────────────┤
│ 筛选区（el-form inline）                          │
│ [日志类型▼] [资产编号] [资产名称] [操作人]         │
│ [日期范围] [查询] [重置]              [导出Excel] │
├─────────────────────────────────────────────────┤
│ 日志表格（el-table）                              │
│ 日志类型|资产编号|资产名称|业务类型|操作|          │
│ 操作人|操作时间|来源|操作(查看资产/查看详情)        │
├─────────────────────────────────────────────────┤
│ 分页（el-pagination）                             │
└─────────────────────────────────────────────────┘

日志详情弹窗（el-dialog）：展示完整 AuditLogVO 字段
```

## 8. 前端 API 封装 (audit.ts)

```typescript
import request from '@/utils/request'  // 或现有 axios 实例
import { download } from '@/utils/download'

export function getAuditSummary() { return request.get('/audit/logs/summary') }
export function getAuditLogPage(params) { return request.get('/audit/logs/page', { params }) }
export function getAuditLogDetail(id) { return request.get(`/audit/logs/${id}`) }
export function exportAuditLogs(params) { return download('/export/audit/logs', params) }
```

## 9. 路由与菜单

### 9.1 路由
```typescript
{
  path: 'audit/logs',
  name: 'AuditLog',
  component: () => import('@/views/audit/AuditLog.vue'),
  meta: { title: '审计日志', permission: 'approval:audit' }
}
```

### 9.2 菜单（MainLayout.vue）
在「审批中心」菜单之后新增独立菜单项：
```html
<el-menu-item index="/audit/logs" v-if="authStore.hasPermission('approval:audit')">
  <el-icon><Document /></el-icon>
  <span>审计追踪</span>
</el-menu-item>
```

## 10. Excel 导出设计

导出端点 `GET /api/export/audit/logs`：
- 接收与分页查询相同的筛选参数
- 调用 `AuditService.page()` 并设置 pageSize=10000 取全量
- 表头：`["日志类型","资产编号","资产名称","业务类型","操作","变更前状态","变更后状态","操作人","操作时间","来源","备注"]`
- 文件名：`审计日志.xlsx`
- 复用 `ExcelExportUtil` 工具方法

## 11. 复合 ID 解析（详情接口）

`GET /api/audit/logs/{id}` 中 id 格式为 `{SOURCE}-{原ID}`：
- 解析前缀确定来源表
- 回查原表并组装成 AuditLogVO 返回
- 找不到时抛 BusinessException(NOT_FOUND)

## 12. 不新增表/字段说明

- 4 个数据源表均已存在且字段足够
- 资产关联通过 JOIN 实现，无需新增外键字段
- 权限复用 `approval:audit`，无需新增权限 SQL
- 完全符合开发边界
