# Phase 9 盘点任务管理完善 - 需求规格（Spec）

## 1. 背景

当前固定资产系统中，盘点任务模块仅是骨架页面：
- 后端 `InventoryController` 只有 `GET /api/inventory/tasks/page` 一个接口，且 `InventoryService.page()` 直接返回空列表，未接入数据库
- 前端 `InventoryTask.vue` 显示"盘点管理功能待开发"的空状态
- 数据库已建好 `inventory_task` 和 `inventory_record` 表，字段齐全但从未被使用

固定资产系统需要定期核对资产实际位置、保管人和账面信息，这是国企固定资产管理中的标准流程。当前骨架无法满足实际盘点业务需求。

## 2. 目标

实现盘点业务闭环，包括：
1. **盘点任务列表**：分页查看盘点任务，支持按状态筛选
2. **新建盘点任务**：按范围（ALL 全部资产 / DEPARTMENT 按部门 / LOCATION 按地点）创建任务，自动生成盘点明细
3. **盘点明细核对**：查看任务下的盘点明细，录入实际地点、实际保管人、盘点结果、备注
4. **完成盘点任务**：校验所有明细已录入结果后，标记任务完成
5. **盘点报告查看**：查看任务详情和明细汇总

## 3. 用户价值

补齐国企固定资产管理中的定期盘点流程，让资产管理人员可以在网页端完成"建任务 → 逐条核对 → 完成盘点 → 查看报告"的完整闭环，不再依赖线下表格。

## 4. 做什么

### 4.1 后端
- 新增 `InventoryTaskMapper`、`InventoryRecordMapper`（MyBatis-Plus BaseMapper）
- 新增 DTO：`InventoryTaskCreateRequest`、`InventoryTaskQueryRequest`、`InventoryRecordUpdateRequest`
- 新增 VO：`InventoryTaskVO`（含明细统计）、`InventoryRecordVO`（含资产信息）
- 完善 `InventoryService`：page、create、detail、getRecords、updateRecord、complete
- 完善 `InventoryController`：6 个接口

### 4.2 前端
- 完善 `frontend/src/api/inventory.ts`：补齐 5 个接口函数
- 完善 `frontend/src/views/inventory/InventoryTask.vue`：筛选区、任务列表、新建弹窗、明细弹窗、结果录入、完成按钮

## 5. 不做什么

- 不修改登录 / JWT
- 不修改审批流核心逻辑
- 不修改生命周期状态流转
- 不修改资产新增 / 编辑 / 删除主流程
- 不做扫码硬件接入
- 不做移动端
- 不做 AI 自动识别
- 不做复杂权限收口（多角色演示账号留到后面）
- 不新增数据库表（复用现有 `inventory_task`、`inventory_record`）
- 不新增数据库字段（现有字段足够）
- 不新增 migration SQL
- 不新增路由（已有 `/inventory/tasks`）
- 不新增菜单（已有"盘点管理"菜单项）

## 6. 数据库复用说明

### inventory_task 表（已有，字段足够）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| task_code | VARCHAR(64) | 任务编号 |
| task_name | VARCHAR(128) | 任务名称 |
| scope_type | VARCHAR(32) | 盘点范围类型（ALL/DEPARTMENT/LOCATION）|
| department | VARCHAR(128) | 部门范围 |
| location | VARCHAR(255) | 地点范围 |
| status | VARCHAR(32) | 任务状态（IN_PROGRESS/COMPLETED）|
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| created_by | BIGINT | 创建人 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### inventory_record 表（已有，字段足够）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| task_id | BIGINT | 任务ID |
| asset_id | BIGINT | 资产ID |
| expected_location | VARCHAR(255) | 应在地点 |
| actual_location | VARCHAR(255) | 实际地点 |
| expected_keeper | VARCHAR(64) | 应在保管人 |
| actual_keeper | VARCHAR(64) | 实际保管人 |
| result | VARCHAR(32) | 盘点结果（NORMAL/LOCATION_MISMATCH/KEEPER_MISMATCH/LOST/EXTRA）|
| scanned_at | DATETIME | 盘点时间 |
| remark | VARCHAR(500) | 备注 |

## 7. 权限说明

复用现有权限（`migration-v5-rbac.sql` 已定义）：
- `inventory:view`（id=15）：查看盘点任务与明细
- `inventory:create`（id=16）：创建盘点任务

前端路由 meta.permission 保持 `inventory:view`，菜单项 `v-if` 保持 `inventory:view`。

## 8. 后端接口清单

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 1 | GET | `/api/inventory/tasks/page` | 分页查询任务 | inventory:view |
| 2 | POST | `/api/inventory/tasks` | 创建任务（自动生成明细）| inventory:create |
| 3 | GET | `/api/inventory/tasks/{id}` | 查看任务详情 | inventory:view |
| 4 | GET | `/api/inventory/tasks/{id}/records` | 查看盘点明细 | inventory:view |
| 5 | PUT | `/api/inventory/records/{recordId}` | 更新单条盘点结果 | inventory:create |
| 6 | PUT | `/api/inventory/tasks/{id}/complete` | 完成盘点任务 | inventory:create |

## 9. 业务规则

### 9.1 创建任务时生成明细
- 根据 `scopeType` 生成明细：
  - `ALL`：查询全部未删除资产（`deleted=0`）
  - `DEPARTMENT`：查询指定部门资产
  - `LOCATION`：查询指定地点资产
- 每条明细记录 `expectedLocation`（资产当前 location）和 `expectedKeeper`（资产当前 keeper）
- 任务初始状态 `IN_PROGRESS`，`startTime` 设为当前时间

### 9.2 盘点结果枚举
- `NORMAL`：正常
- `LOCATION_MISMATCH`：地点不符
- `KEEPER_MISMATCH`：保管人不符
- `LOST`：丢失
- `EXTRA`：账外资产

### 9.3 完成任务校验
- 完成前校验所有明细已录入 `result`（非空）
- 完成后任务状态改为 `COMPLETED`，`endTime` 设为当前时间

## 10. 验收标准

1. 后端 `mvn -DskipTests package` 构建成功
2. 前端 `npm run build` 构建成功
3. 登录 admin/123456 后进入盘点任务页面
4. 能看到任务列表（初始可为空状态）
5. 点击"新建盘点任务"弹出弹窗
6. 选择范围类型（ALL/DEPARTMENT/LOCATION）创建任务
7. 创建成功后列表刷新，能看到新任务
8. 任务明细已自动生成（数量与匹配资产数一致）
9. 点击"查看明细"能看到盘点明细列表
10. 编辑一条明细，填写实际地点、实际保管人、盘点结果、备注
11. 保存成功后明细刷新
12. 所有明细录入结果后，点击"完成任务"成功
13. 任务状态变为 COMPLETED
14. 资产台账、审批中心、资产详情时间线仍正常（回归）
