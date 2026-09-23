# Phase 15 - 基础数据字典与演示数据时间分布优化 技术设计

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────┐
│  前端 Vue3 + Element Plus                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ AssetList    │  │ Receive      │  │ Transfer     │  │
│  │ .vue         │  │ .vue         │  │ .vue         │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                 │                 │           │
│  ┌──────┴─────────────────┴─────────────────┴───────┐  │
│  │  useMasterDataOptions composable                 │  │
│  │  (getDepartments / getLocations / getKeepers)    │  │
│  └──────────────────────┬───────────────────────────┘  │
│                         │                              │
│  ┌──────────────────────┴───────────────────────────┐  │
│  │  api/masterData.ts                               │  │
│  └──────────────────────┬───────────────────────────┘  │
└─────────────────────────┼───────────────────────────────┘
                          │ HTTP /api/master-data/*
┌─────────────────────────┼───────────────────────────────┐
│  后端 Spring Boot        │                               │
│  ┌──────────────────────┴───────────────────────────┐  │
│  │  MasterDataController                            │  │
│  │  GET /departments  GET /locations  GET /keepers  │  │
│  └──────────────────────┬───────────────────────────┘  │
│  ┌──────────────────────┴───────────────────────────┐  │
│  │  MasterDataService                               │  │
│  └──────────────────────┬───────────────────────────┘  │
│  ┌──────────────────────┴───────────────────────────┐  │
│  │  MasterDataMapper (XML)                          │  │
│  └──────────────────────┬───────────────────────────┘  │
│  ┌──────────────────────┴───────────────────────────┐  │
│  │  base_department / base_location / sys_user      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 2. 数据库设计

### 2.1 新增表

**base_department**（部门字典表）：
```sql
CREATE TABLE base_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_code VARCHAR(64) NOT NULL UNIQUE COMMENT '部门编码',
    department_name VARCHAR(128) NOT NULL COMMENT '部门名称',
    manager_name VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='基础部门字典表';
```

**base_location**（地点字典表）：
```sql
CREATE TABLE base_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location_code VARCHAR(64) NOT NULL UNIQUE COMMENT '地点编码',
    location_name VARCHAR(128) NOT NULL COMMENT '地点名称',
    building VARCHAR(64) DEFAULT NULL COMMENT '楼栋',
    floor_no VARCHAR(32) DEFAULT NULL COMMENT '楼层',
    room_no VARCHAR(64) DEFAULT NULL COMMENT '房间号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='基础地点字典表';
```

### 2.2 种子数据

**部门（10 个）**：信息中心、资产管理部、财务部、审计部、综合办公室、教务部、实验实训中心、后勤保障部、图书馆、保卫处

**地点（14 个）**：信息中心机房、综合楼 301、综合楼 502、实验楼 A201、实验楼 B305、图书馆一楼、图书馆三楼、行政楼会议室、后勤仓库、保卫处监控室、财务档案室、资产仓库、教学楼多媒体教室、实训中心设备间

### 2.3 设计取舍说明

选择新增 `base_department` 和 `base_location` 两张轻量字典表，而非使用通用 `sys_dict_type/sys_dict_item`，原因：
1. 部门和地点有独立属性（负责人、楼栋/楼层/房间号），通用字典表无法承载
2. 两张表结构简单，字段明确，查询效率高
3. 未来可扩展为完整的主数据管理模块

## 3. 后端文件清单

### 3.1 新增文件

| 文件 | 说明 |
|------|------|
| `backend/src/main/java/com/example/asset/masterdata/controller/MasterDataController.java` | 基础数据 Controller，3 个 GET 接口 |
| `backend/src/main/java/com/example/asset/masterdata/service/MasterDataService.java` | 基础数据 Service |
| `backend/src/main/java/com/example/asset/masterdata/mapper/MasterDataMapper.java` | Mapper 接口 |
| `backend/src/main/java/com/example/asset/masterdata/vo/MasterDataVO.java` | 统一返回 VO |
| `backend/src/main/resources/mapper/masterdata/MasterDataMapper.xml` | Mapper XML |
| `backend/src/main/resources/sql/migration-v15-master-data-demo-time.sql` | 建表 + 种子 + 时间分布优化 |

### 3.2 接口设计

```
GET /api/master-data/departments
  → Result<List<MasterDataVO>>
  → 查询 base_department WHERE status=1 ORDER BY sort_order

GET /api/master-data/locations
  → Result<List<MasterDataVO>>
  → 查询 base_location WHERE status=1 ORDER BY sort_order

GET /api/master-data/keepers
  → Result<List<MasterDataVO>>
  → 查询 sys_user WHERE status=1，返回 real_name
  → 若不足，补充演示人员（从 asset.keeper 去重）
```

### 3.3 MasterDataVO

```java
@Data
public class MasterDataVO {
    private Long id;
    private String code;      // 编码
    private String name;      // 名称
    private String label;     // 显示文本
    private String value;     // 值（同 name，用于提交）
    private String extraInfo; // 附加信息
}
```

## 4. 前端文件清单

### 4.1 新增文件

| 文件 | 说明 |
|------|------|
| `frontend/src/api/masterData.ts` | 基础数据 API 封装 |
| `frontend/src/composables/useMasterDataOptions.ts` | 可复用 composable |

### 4.2 修改文件

| 文件 | 改造内容 |
|------|----------|
| `frontend/src/views/asset/AssetList.vue` | 搜索表单 + 编辑弹窗的 department/keeper/location 改为 el-select |
| `frontend/src/views/lifecycle/Receive.vue` | 编辑弹窗的 receiver/receiverDepartment 改为 el-select |
| `frontend/src/views/lifecycle/Transfer.vue` | 编辑弹窗的 toDepartment/toLocation/toKeeper 改为 el-select |
| `frontend/src/views/inventory/InventoryTask.vue` | 新建弹窗 department/location + 编辑明细 actualLocation/actualKeeper 改为 el-select |

### 4.3 useMasterDataOptions composable 设计

```typescript
export function useMasterDataOptions() {
  const departmentOptions = ref<MasterDataItem[]>([])
  const locationOptions = ref<MasterDataItem[]>([])
  const keeperOptions = ref<MasterDataItem[]>([])

  async function loadDepartments() { ... }
  async function loadLocations() { ... }
  async function loadKeepers() { ... }
  async function loadAll() { Promise.all([loadDepartments(), loadLocations(), loadKeepers()]) }

  return { departmentOptions, locationOptions, keeperOptions, loadAll, loadDepartments, loadLocations, loadKeepers }
}
```

### 4.4 前端改造原则

- el-select 设置 `filterable` 允许搜索
- el-select 设置 `allow-create` 允许输入不在选项中的值（兼容历史数据）
- 提交字段结构不变，仍然传字符串值（如 department 传 "信息中心"）
- 加载失败时降级为空数组并提示

## 5. 演示数据时间分布优化设计

### 5.1 生命周期单据 created_at 优化

对 DEMO 前缀的生命周期单据按 `order_code` 中的年月线索或按 id 顺序，UPDATE `created_at` 为最近 12 个月的不同时间点：

```sql
-- 按单据类型批量更新 created_at 为不同月份
UPDATE asset_inbound_order SET created_at = '2025-08-15 10:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1009;
UPDATE asset_inbound_order SET created_at = '2025-09-15 10:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1010 AND 1019;
-- ...以此类推
```

### 5.2 审批实例/记录 created_at 优化

```sql
-- 审批实例 created_at 参考 started_at
UPDATE approval_instance SET created_at = started_at WHERE id >= 1000;
-- 审批记录 created_at 参考 approved_at 或实例 started_at
UPDATE approval_record ar JOIN approval_instance ai ON ar.instance_id = ai.id 
SET ar.created_at = COALESCE(ar.approved_at, ai.started_at) WHERE ar.id >= 1000;
```

### 5.3 盘点任务扩展

新增 2-3 条盘点任务，覆盖 2025-Q4 和 2026-Q1：
```sql
INSERT INTO inventory_task ... -- 2025年11月盘点、2026年2月盘点
```

### 5.4 finance_sync_record 扩展

新增 6 条财务同步记录，补齐最近 12 个月（2025-08 ~ 2026-07），其中 1 条 FAILED：
```sql
INSERT INTO finance_sync_record ... -- 覆盖 2025-08 到 2026-01
```

## 6. 权限设计

基础数据接口为只读查询，登录用户均可访问，无需新增权限。不修改 `sys_permission` 表，不新增权限项。

## 7. 幂等性保证

- `base_department` 和 `base_location` 使用 `INSERT IGNORE` 或 `ON DUPLICATE KEY UPDATE`
- 种子数据使用固定 ID，重复执行不会产生重复记录
- 时间分布优化使用 `UPDATE ... WHERE id >= 1000 AND order_code LIKE 'DEMO-%'`，仅影响 DEMO 数据
- 新增的盘点任务和财务同步记录使用 `INSERT IGNORE` + 固定 ID
