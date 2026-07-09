# 核心领域模型

## 1. 领域模型总览

```
Asset ──belongs to──> AssetCategory
Asset ──has──> AssetOperationLog (多条)
Asset ──has──> InboundOrder
Asset ──has──> ReceiveOrder
Asset ──has──> TransferOrder
Asset ──has──> RepairOrder
Asset ──has──> ScrapOrder
User  ──creates──> InboundOrder / ReceiveOrder / TransferOrder / RepairOrder / ScrapOrder
User  ──creates──> AssetOperationLog
User  ──has──> Role (多对多)
```

## 2. 模型清单

### 2.1 Asset（固定资产）

| 属性 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| assetCode | String | 资产编号（FA+yyyyMM+4位流水） |
| assetName | String | 资产名称 |
| categoryId | Long | 资产分类 ID |
| specification | String | 规格型号 |
| brand | String | 品牌 |
| purchaseDate | LocalDate | 购置日期 |
| originalValue | BigDecimal | 原值 |
| usefulLife | Integer | 使用年限 |
| residualRate | BigDecimal | 残值率 |
| depreciationMethod | String | 折旧方法 |
| accumulatedDepreciation | BigDecimal | 累计折旧 |
| netValue | BigDecimal | 净值 |
| department | String | 所属部门 |
| keeper | String | 保管人/使用人 |
| location | String | 存放地点 |
| status | String | 资产状态（IDLE/IN_USE/REPAIRING/WAITING_SCRAP/SCRAPPED 等） |
| deleted | Boolean | 逻辑删除标记 |

### 2.2 AssetCategory（资产分类）

| 属性 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| categoryCode | String | 分类编码 |
| categoryName | String | 分类名称 |
| parentId | Long | 父级 ID |
| depreciationYears | Integer | 默认折旧年限 |

### 2.3 AssetOperationLog（操作日志）

| 属性 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| assetId | Long | 资产 ID |
| operationType | String | 操作类型 |
| operationName | String | 操作名称 |
| beforeStatus | String | 变更前状态 |
| afterStatus | String | 变更后状态 |
| operatorId | Long | 操作人 ID |
| operatorName | String | 操作人姓名 |
| operationTime | LocalDateTime | 操作时间 |

### 2.4 InboundOrder（入库单）

| 属性 | 类型 | 说明 |
|---|---|---|
| orderCode | String | 单据编号（IN+yyyyMMdd+4位流水） |
| assetId | Long | 资产 ID |
| inboundType | String | 入库类型 |
| supplier | String | 供应商 |
| inboundDate | LocalDate | 入库日期 |
| handler | String | 经办人 |
| status | String | 单据状态（COMPLETED） |

### 2.5 ReceiveOrder（领用单）

| 属性 | 类型 | 说明 |
|---|---|---|
| orderCode | String | 单据编号（RE+yyyyMMdd+4位流水） |
| assetId | Long | 资产 ID |
| receiver | String | 领用人 |
| receiverDepartment | String | 领用部门 |
| receiveDate | LocalDate | 领用日期 |
| usagePurpose | String | 使用用途 |
| status | String | 单据状态（COMPLETED） |

### 2.6 TransferOrder（调拨单）

| 属性 | 类型 | 说明 |
|---|---|---|
| orderCode | String | 单据编号（TF+yyyyMMdd+4位流水） |
| assetId | Long | 资产 ID |
| fromDepartment | String | 调出部门 |
| toDepartment | String | 调入部门 |
| fromLocation | String | 调出地点 |
| toLocation | String | 调入地点 |
| fromKeeper | String | 调出保管人 |
| toKeeper | String | 调入保管人 |
| transferDate | LocalDate | 调拨日期 |
| status | String | 单据状态（COMPLETED） |

### 2.7 RepairOrder（维修单）

| 属性 | 类型 | 说明 |
|---|---|---|
| orderCode | String | 单据编号（RP+yyyyMMdd+4位流水） |
| assetId | Long | 资产 ID |
| faultDescription | String | 故障描述 |
| repairVendor | String | 维修厂商 |
| repairCost | BigDecimal | 维修费用 |
| repairStartDate | LocalDate | 开始日期 |
| repairEndDate | LocalDate | 完成日期 |
| repairResult | String | 维修结果（REPAIRED / SCRAP_SUGGESTED） |
| status | String | 单据状态（DRAFT → COMPLETED） |

### 2.8 ScrapOrder（报废单）

| 属性 | 类型 | 说明 |
|---|---|---|
| orderCode | String | 单据编号（SC+yyyyMMdd+4位流水） |
| assetId | Long | 资产 ID |
| scrapReason | String | 报废原因 |
| scrapDate | LocalDate | 报废日期 |
| disposalMethod | String | 处置方式 |
| residualValue | BigDecimal | 残值 |
| status | String | 单据状态（COMPLETED） |

### 2.9 User（系统用户）

| 属性 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| username | String | 用户名 |
| password | String | 密码（BCrypt 加密） |
| realName | String | 真实姓名 |
| department | String | 所属部门 |
| status | Integer | 状态（1 启用 / 0 禁用） |

### 2.10 Role（系统角色）

| 属性 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| roleCode | String | 角色编码（ADMIN / ASSET_MANAGER / FINANCE / AUDITOR） |
| roleName | String | 角色名称 |
| description | String | 描述 |

## 3. 关系说明

| 关系 | 说明 |
|---|---|
| Asset → AssetCategory | 多对一，一个资产属于一个分类 |
| Asset → Lifecycle Orders | 一对多，一个资产可产生入库/领用/调拨/维修/报废单据 |
| Asset → AssetOperationLog | 一对多，一个资产可产生多条操作日志 |
| User → Lifecycle Orders | 一对多，用户可以通过 created_by 创建多条单据 |
| User → AssetOperationLog | 一对多，用户可以通过 operator 产生多条操作日志 |
| User ↔ Role | 多对多，通过 sys_user_role 关联表 |
