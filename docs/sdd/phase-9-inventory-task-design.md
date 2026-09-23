# Phase 9 盘点任务管理完善 - 技术设计（Design）

## 1. 数据库复用

复用现有 `inventory_task`、`inventory_record`、`asset` 三张表，不新增表、不新增字段、不新增 migration SQL。

字段映射见 spec.md 第 6 节。关键关联：
- `inventory_record.task_id` → `inventory_task.id`
- `inventory_record.asset_id` → `asset.id`
- 创建明细时从 `asset.location`、`asset.keeper` 写入 `inventory_record.expected_location`、`inventory_record.expected_keeper`

## 2. 后端接口设计

### 2.1 GET `/api/inventory/tasks/page` 分页查询任务
- 参数：`pageNum`（默认1）、`pageSize`（默认10）、`status`（可选）、`scopeType`（可选）
- 返回：`PageResult<InventoryTaskVO>`
- InventoryTaskVO 字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 任务ID |
| taskCode | String | 任务编号 |
| taskName | String | 任务名称 |
| scopeType | String | 范围类型 |
| department | String | 部门范围 |
| location | String | 地点范围 |
| status | String | 任务状态 |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| createdBy | Long | 创建人ID |
| createdByName | String | 创建人姓名 |
| totalRecords | Integer | 明细总数 |
| completedRecords | Integer | 已录入结果数 |

### 2.2 POST `/api/inventory/tasks` 创建任务
- 请求体：`InventoryTaskCreateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskName | String | 是 | 任务名称 |
| scopeType | String | 是 | ALL/DEPARTMENT/LOCATION |
| department | String | 否 | scopeType=DEPARTMENT 时必填 |
| location | String | 否 | scopeType=LOCATION 时必填 |

- 逻辑：
  1. 生成 taskCode（格式 `PD` + yyyyMM + 4位序号）
  2. 创建 InventoryTask，status=IN_PROGRESS，startTime=now，createdBy=当前用户
  3. 根据 scopeType 查询资产（deleted=0）
  4. 为每个资产创建 InventoryRecord，写入 expectedLocation/expectedKeeper
  5. 返回任务ID
- 返回：`Result<Long>`

### 2.3 GET `/api/inventory/tasks/{id}` 查看任务详情
- 返回：`Result<InventoryTaskVO>`（含明细统计）

### 2.4 GET `/api/inventory/tasks/{id}/records` 查看盘点明细
- 参数：`pageNum`（默认1）、`pageSize`（默认50，明细一般不分页但保留）
- 返回：`PageResult<InventoryRecordVO>`
- InventoryRecordVO 字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细ID |
| taskId | Long | 任务ID |
| assetId | Long | 资产ID |
| assetCode | String | 资产编号 |
| assetName | String | 资产名称 |
| categoryName | String | 分类名称 |
| expectedLocation | String | 应在地点 |
| actualLocation | String | 实际地点 |
| expectedKeeper | String | 应在保管人 |
| actualKeeper | String | 实际保管人 |
| result | String | 盘点结果 |
| scannedAt | LocalDateTime | 盘点时间 |
| remark | String | 备注 |

### 2.5 PUT `/api/inventory/records/{recordId}` 更新单条盘点结果
- 请求体：`InventoryRecordUpdateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| actualLocation | String | 否 | 实际地点 |
| actualKeeper | String | 否 | 实际保管人 |
| result | String | 是 | NORMAL/LOCATION_MISMATCH/KEEPER_MISMATCH/LOST/EXTRA |
| remark | String | 否 | 备注 |

- 逻辑：更新 record，scannedAt=now
- 返回：`Result<Void>`

### 2.6 PUT `/api/inventory/tasks/{id}/complete` 完成盘点任务
- 逻辑：
  1. 校验任务存在且状态为 IN_PROGRESS
  2. 校验所有明细 result 非空
  3. 更新任务 status=COMPLETED，endTime=now
- 返回：`Result<Void>`

## 3. 后端类设计

### 3.1 新增文件

```
backend/src/main/java/com/example/asset/inventory/
├── controller/InventoryController.java       （修改）
├── service/InventoryService.java             （修改）
├── mapper/
│   ├── InventoryTaskMapper.java              （新增）
│   └── InventoryRecordMapper.java            （新增）
├── entity/
│   ├── InventoryTask.java                    （已有，不改）
│   └── InventoryRecord.java                  （已有，不改）
├── dto/
│   ├── InventoryTaskCreateRequest.java       （新增）
│   ├── InventoryTaskQueryRequest.java        （新增）
│   └── InventoryRecordUpdateRequest.java     （新增）
└── vo/
    ├── InventoryTaskVO.java                  （新增）
    └── InventoryRecordVO.java                （新增）
```

### 3.2 InventoryTaskMapper
```java
@Mapper
public interface InventoryTaskMapper extends BaseMapper<InventoryTask> {
}
```

### 3.3 InventoryRecordMapper
```java
@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {
    @Select("SELECT r.*, a.asset_code, a.asset_name, c.category_name " +
            "FROM inventory_record r " +
            "LEFT JOIN asset a ON a.id = r.asset_id " +
            "LEFT JOIN asset_category c ON c.id = a.category_id " +
            "WHERE r.task_id = #{taskId} ORDER BY r.id")
    List<InventoryRecordVO> selectRecordsByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result IS NULL")
    int countUnrecorded(@Param("taskId") Long taskId);
}
```

### 3.4 InventoryService 关键方法
- `page(query)` → 分页查询任务，附 totalRecords/completedRecords
- `create(request, userId, username)` → 创建任务+生成明细
- `detail(id)` → 任务详情
- `getRecords(taskId)` → 明细列表（含资产信息）
- `updateRecord(recordId, request)` → 更新单条明细
- `complete(taskId)` → 完成任务（含校验）

## 4. 前端 API 设计

`frontend/src/api/inventory.ts`：

```typescript
// 已有：getInventoryTaskPage（保留）

// 新增
export function createInventoryTask(data: InventoryTaskCreateRequest)
export function getInventoryTaskDetail(id: number)
export function getInventoryRecords(taskId: number, params?)
export function updateInventoryRecord(recordId: number, data: InventoryRecordUpdateRequest)
export function completeInventoryTask(taskId: number)
```

## 5. 前端页面设计

### 5.1 InventoryTask.vue 完善方案

**布局**：
1. PageHeader（标题"盘点任务"）
2. 筛选区：状态筛选下拉 + "新建盘点任务"按钮
3. 任务列表表格：任务编号、任务名称、范围类型、范围描述、状态、开始时间、结束时间、明细进度（已录入/总数）、创建人、操作列
4. 操作列：查看明细、完成任务（仅 IN_PROGRESS 状态）
5. 分页

**新建任务弹窗**：
- 任务名称（必填）
- 范围类型（必填，单选：全部资产/按部门/按地点）
- 部门（scopeType=DEPARTMENT 时显示，必填）
- 地点（scopeType=LOCATION 时显示，必填）

**明细弹窗**：
- 顶部显示任务基本信息
- 明细表格：资产编号、资产名称、分类、应在地点、实际地点、应在保管人、实际保管人、盘点结果、盘点时间、备注、操作
- 操作列：编辑（弹出编辑弹窗）
- 底部"完成任务"按钮

**编辑明细弹窗**：
- 实际地点
- 实际保管人
- 盘点结果（下拉：正常/地点不符/保管人不符/丢失/账外资产）
- 备注

### 5.2 范围类型映射
- ALL → "全部资产"
- DEPARTMENT → "按部门"
- LOCATION → "按地点"

### 5.3 状态映射
- IN_PROGRESS → "进行中"（el-tag type="warning"）
- COMPLETED → "已完成"（el-tag type="success"）

### 5.4 盘点结果映射
- NORMAL → "正常"（el-tag type="success"）
- LOCATION_MISMATCH → "地点不符"（el-tag type="warning"）
- KEEPER_MISMATCH → "保管人不符"（el-tag type="warning"）
- LOST → "丢失"（el-tag type="danger"）
- EXTRA → "账外资产"（el-tag type="info"）

## 6. 路由与菜单复用

- 路由 `/inventory/tasks` 已存在于 `frontend/src/router/index.ts`，meta.permission=`inventory:view`，不改
- 菜单项"盘点管理"已存在于 `MainLayout.vue`，`v-if="authStore.hasPermission('inventory:view')"`，不改

## 7. 不新增

- 不新增路由
- 不新增菜单
- 不新增数据库表
- 不新增数据库字段
- 不新增 migration SQL
- 不修改 InventoryTask/InventoryRecord entity
- 不修改 AssetService
- 不修改 ApprovalService
- 不修改 LifecycleService
