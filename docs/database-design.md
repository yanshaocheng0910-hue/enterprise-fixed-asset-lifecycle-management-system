# 数据库设计文档

## 1. 数据库概览

- **数据库名**: `fixed_asset_lifecycle_system`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **数据库类型**: MySQL 8

## 2. 表结构总览

| 表名 | 说明 | 阶段 |
|---|---|---|
| sys_user | 系统用户表 | 第一阶段 |
| sys_role | 系统角色表 | 第一阶段 |
| sys_user_role | 用户角色关联表 | 第一阶段 |
| asset_category | 资产分类表 | 第一阶段 |
| asset | 固定资产表 | 第一阶段 |
| asset_operation_log | 资产操作日志表 | 第一阶段 |
| asset_inbound_order | 资产入库单 | 第二阶段 |
| asset_receive_order | 资产领用单 | 第二阶段 |
| asset_transfer_order | 资产调拨单 | 第二阶段 |
| asset_repair_order | 资产维修单 | 第二阶段 |
| asset_scrap_order | 资产报废单 | 第二阶段 |
| depreciation_record | 折旧记录表 | 第一阶段（结构预留） |
| inventory_task | 盘点任务表 | 第一阶段（结构预留） |
| inventory_record | 盘点明细表 | 第一阶段（结构预留） |

## 3. 表详细设计

### 3.1 sys_user - 系统用户表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| username | VARCHAR(64) | NOT NULL UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt 加密） |
| real_name | VARCHAR(64) | NOT NULL | 真实姓名 |
| department | VARCHAR(128) | DEFAULT NULL | 所属部门 |
| phone | VARCHAR(32) | DEFAULT NULL | 手机号 |
| email | VARCHAR(128) | DEFAULT NULL | 邮箱 |
| status | TINYINT | NOT NULL DEFAULT 1 | 状态：1启用 0禁用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 3.2 sys_role - 系统角色表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| role_code | VARCHAR(64) | NOT NULL UNIQUE | 角色编码 |
| role_name | VARCHAR(64) | NOT NULL | 角色名称 |
| description | VARCHAR(255) | DEFAULT NULL | 描述 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

默认数据：

| role_code | role_name | 说明 |
|---|---|---|
| ADMIN | 系统管理员 | 系统超级管理员 |
| ASSET_MANAGER | 资产管理员 | 负责资产台账和盘点 |
| FINANCE | 财务人员 | 负责折旧与财务对接 |
| AUDITOR | 审计人员 | 只读审计角色 |

### 3.3 sys_user_role - 用户角色关联表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户ID |
| role_id | BIGINT | NOT NULL | 角色ID |
| | | UNIQUE KEY (user_id, role_id) | 联合唯一 |

### 3.4 asset_category - 资产分类表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| category_code | VARCHAR(64) | NOT NULL UNIQUE | 分类编码 |
| category_name | VARCHAR(128) | NOT NULL | 分类名称 |
| parent_id | BIGINT | NOT NULL DEFAULT 0 | 父级ID（0为顶级） |
| depreciation_years | INT | NOT NULL DEFAULT 5 | 折旧年限 |
| remark | VARCHAR(255) | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

默认数据：

| category_code | category_name | depreciation_years |
|---|---|---|
| OFFICE | 办公设备 | 5 |
| ELECTRONIC | 电子设备 | 4 |
| VEHICLE | 运输设备 | 8 |
| PRODUCTION | 生产设备 | 10 |
| BUILDING | 房屋及建筑物 | 20 |

### 3.5 asset - 固定资产表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| asset_code | VARCHAR(64) | NOT NULL UNIQUE | 资产编号（FA+年月+4位流水） |
| asset_name | VARCHAR(128) | NOT NULL | 资产名称 |
| category_id | BIGINT | NOT NULL | 分类ID |
| specification | VARCHAR(255) | DEFAULT NULL | 规格型号 |
| brand | VARCHAR(128) | DEFAULT NULL | 品牌 |
| purchase_date | DATE | NOT NULL | 购置日期 |
| original_value | DECIMAL(18,2) | NOT NULL | 原值 |
| useful_life | INT | NOT NULL | 使用年限 |
| residual_rate | DECIMAL(5,4) | NOT NULL | 残值率 |
| depreciation_method | VARCHAR(32) | NOT NULL DEFAULT 'straight_line' | 折旧方法 |
| accumulated_depreciation | DECIMAL(18,2) | NOT NULL DEFAULT 0 | 累计折旧 |
| net_value | DECIMAL(18,2) | NOT NULL DEFAULT 0 | 净值 |
| department | VARCHAR(128) | DEFAULT NULL | 所属部门 |
| keeper | VARCHAR(64) | DEFAULT NULL | 保管人/使用人 |
| location | VARCHAR(255) | DEFAULT NULL | 存放地点 |
| status | VARCHAR(32) | NOT NULL | 资产状态 |
| photo_url | VARCHAR(255) | DEFAULT NULL | 图片地址 |
| remark | VARCHAR(500) | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| deleted | TINYINT | NOT NULL DEFAULT 0 | 逻辑删除标记（0未删 1已删） |

索引：

| 索引名 | 字段 |
|---|---|
| idx_asset_category_id | category_id |
| idx_asset_status | status |
| idx_asset_department | department |

资产状态枚举：

| 状态码 | 说明 |
|---|---|
| IDLE | 闲置 |
| IN_USE | 使用中 |
| TRANSFERRING | 调拨中 |
| REPAIRING | 维修中 |
| WAITING_SCRAP | 待报废 |
| SCRAPPED | 已报废 |
| INVENTORY_ABNORMAL | 盘点异常 |

### 3.6 asset_operation_log - 资产操作日志表

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| asset_id | BIGINT | NOT NULL | 资产ID |
| operation_type | VARCHAR(64) | NOT NULL | 操作类型（CREATE/UPDATE/DELETE/INIT） |
| operation_name | VARCHAR(128) | NOT NULL | 操作名称 |
| before_status | VARCHAR(32) | DEFAULT NULL | 变更前状态 |
| after_status | VARCHAR(32) | DEFAULT NULL | 变更后状态 |
| operator_id | BIGINT | DEFAULT NULL | 操作人ID |
| operator_name | VARCHAR(64) | DEFAULT NULL | 操作人姓名 |
| operation_time | DATETIME | NOT NULL | 操作时间 |
| remark | VARCHAR(500) | DEFAULT NULL | 备注 |

### 3.7 depreciation_record - 折旧记录表（结构预留）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| asset_id | BIGINT | NOT NULL | 资产ID |
| depreciation_month | VARCHAR(7) | NOT NULL | 折旧月份 |
| original_value | DECIMAL(18,2) | NOT NULL | 原值 |
| monthly_depreciation | DECIMAL(18,2) | NOT NULL | 月折旧额 |
| accumulated_depreciation | DECIMAL(18,2) | NOT NULL | 累计折旧 |
| net_value | DECIMAL(18,2) | NOT NULL | 净值 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.8 inventory_task - 盘点任务表（结构预留）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| task_code | VARCHAR(64) | NOT NULL UNIQUE | 任务编号 |
| task_name | VARCHAR(128) | NOT NULL | 任务名称 |
| scope_type | VARCHAR(32) | DEFAULT NULL | 盘点范围类型 |
| department | VARCHAR(128) | DEFAULT NULL | 部门范围 |
| location | VARCHAR(255) | DEFAULT NULL | 地点范围 |
| status | VARCHAR(32) | DEFAULT NULL | 任务状态 |
| start_time | DATETIME | DEFAULT NULL | 开始时间 |
| end_time | DATETIME | DEFAULT NULL | 结束时间 |
| created_by | BIGINT | DEFAULT NULL | 创建人 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 3.9 inventory_record - 盘点明细表（结构预留）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| task_id | BIGINT | NOT NULL | 任务ID |
| asset_id | BIGINT | NOT NULL | 资产ID |
| expected_location | VARCHAR(255) | DEFAULT NULL | 应在地点 |
| actual_location | VARCHAR(255) | DEFAULT NULL | 实际地点 |
| expected_keeper | VARCHAR(64) | DEFAULT NULL | 应在保管人 |
| actual_keeper | VARCHAR(64) | DEFAULT NULL | 实际保管人 |
| result | VARCHAR(32) | DEFAULT NULL | 盘点结果 |
| remark | VARCHAR(500) | DEFAULT NULL | 备注 |

## 4. 核心业务规则

### 4.1 资产编号规则

格式：`FA` + `yyyyMM` + `4位流水号`

示例：FA2026070001

规则：

- 由后端在新增资产时自动生成
- 前端无需填写
- 每月流水号从 0001 开始

### 4.2 折旧计算规则

方法：平均年限法

年折旧额 = 原值 × (1 - 残值率) / 使用年限

月折旧额 = 年折旧额 / 12

累计折旧 = 已使用月份 × 月折旧额

资产净值 = 原值 - 累计折旧

约束：净值不能小于残值

### 4.3 生命周期单据编号规则

格式：`前缀` + `yyyyMMdd` + `4位流水号`

| 单据类型 | 前缀 | 示例 |
|---|---|---|
| 入库单 | IN | IN202607050001 |
| 领用单 | RE | RE202607050001 |
| 调拨单 | TF | TF202607050001 |
| 维修单 | RP | RP202607050001 |
| 报废单 | SC | SC202607050001 |

规则：

- 前缀 + 当天日期（yyyyMMdd）+ 4位流水号
- 跨5张表查询最大编号保证唯一性
- 每天流水号从 0001 开始

### 4.4 资产状态流转规则

| 操作 | 变更 |
|---|---|
| 新增入库 | 默认 IDLE |
| 领用 | 状态 → IN_USE |
| 调拨 | 状态 → TRANSFERRING |
| 维修 | 状态 → REPAIRING |
| 申请报废 | 状态 → WAITING_SCRAP |
| 报废完成 | 状态 → SCRAPPED |
| 盘点异常 | 状态 → INVENTORY_ABNORMAL |

### 4.5 逻辑删除规则

- asset 表使用 deleted 字段标记
- deleted = 0：有效数据
- deleted = 1：已删除数据
- MyBatis-Plus 全局配置自动过滤

## 5. ER 关系

```
sys_user --< sys_user_role >-- sys_role
asset_category --< asset
asset --< asset_operation_log
asset --< depreciation_record
inventory_task --< inventory_record
inventory_record >-- asset
asset --< asset_inbound_order
asset --< asset_receive_order
asset --< asset_transfer_order
asset --< asset_repair_order
asset --< asset_scrap_order
```
