# 接口设计文档

## 1. 接口规范

### 1.1 基础URL

- 开发环境：`http://localhost:8080`
- 前端代理：`/api` → `http://localhost:8080`

### 1.2 统一返回格式

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

分页响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

错误响应：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 1.3 HTTP 状态码说明

| HTTP 状态码 | code | 说明 |
|---|---|---|
| 200 | 200 | 成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未登录或登录已失效 |
| 403 | 403 | 无权访问 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 系统异常 |

### 1.4 鉴权方式

- 类型：JWT Bearer Token
- 请求头格式：`Authorization: Bearer <token>`
- 登录接口 `/api/auth/login` 放行
- 其他 `/api/**` 接口需要有效 token

### 1.5 分页参数

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| pageNum | int | 1 | 页码 |
| pageSize | int | 10 | 每页条数 |

---

## 2. 认证接口

### 2.1 登录

```
POST /api/auth/login
```

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "department": "信息中心",
    "roles": ["ADMIN", "ASSET_MANAGER", "FINANCE", "AUDITOR"]
  }
}
```

### 2.2 获取当前用户

```
GET /api/auth/me
```

请求头：`Authorization: Bearer <token>`

返回示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "department": "信息中心",
    "roles": ["ADMIN", "ASSET_MANAGER", "FINANCE", "AUDITOR"]
  }
}
```

---

## 3. 资产接口

### 3.1 分页查询资产

```
GET /api/assets/page
```

请求头：`Authorization: Bearer <token>`

查询参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| assetCode | string | 资产编号（模糊） |
| assetName | string | 资产名称（模糊） |
| categoryId | long | 资产分类ID |
| department | string | 所属部门（模糊） |
| keeper | string | 使用人（模糊） |
| location | string | 存放地点（模糊） |
| status | string | 资产状态 |
| purchaseDateStart | string | 购置日期开始 yyyy-MM-dd |
| purchaseDateEnd | string | 购置日期结束 yyyy-MM-dd |

返回示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "assetCode": "FA2024030001",
        "assetName": "台式电脑-A01",
        "categoryId": 2,
        "categoryName": "电子设备",
        "brand": "Dell",
        "specification": "OptiPlex 7090",
        "purchaseDate": "2024-03-15",
        "originalValue": 6800.00,
        "netValue": 3031.76,
        "department": "信息中心",
        "keeper": "张伟",
        "location": "A座3层机房",
        "status": "IN_USE",
        "qrCode": "QR20240001"
      }
    ],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 3.2 查询资产详情

```
GET /api/assets/{id}
```

请求头：`Authorization: Bearer <token>`

返回示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "assetCode": "FA2024030001",
    "assetName": "台式电脑-A01",
    "categoryId": 2,
    "categoryName": "电子设备",
    "specification": "OptiPlex 7090",
    "brand": "Dell",
    "purchaseDate": "2024-03-15",
    "originalValue": 6800.00,
    "usefulLife": 4,
    "residualRate": 0.05,
    "depreciationMethod": "straight_line",
    "accumulatedDepreciation": 3768.24,
    "netValue": 3031.76,
    "department": "信息中心",
    "keeper": "张伟",
    "location": "A座3层机房",
    "status": "IN_USE",
    "qrCode": "QR20240001",
    "rfidCode": null,
    "photoUrl": null,
    "remark": "系统初始化资产数据",
    "createdAt": "2024-03-15 00:00:00",
    "updatedAt": "2024-03-15 00:00:00"
  }
}
```

### 3.3 新增资产

```
POST /api/assets
```

请求头：`Authorization: Bearer <token>`

请求体：

```json
{
  "assetName": "测试资产",
  "categoryId": 2,
  "specification": "i7/16G/512G",
  "brand": "Lenovo",
  "purchaseDate": "2026-07-01",
  "originalValue": 6800.00,
  "usefulLife": 4,
  "residualRate": 0.05,
  "department": "信息中心",
  "keeper": "张三",
  "location": "A座3层",
  "qrCode": "",
  "rfidCode": "",
  "photoUrl": "",
  "remark": ""
}
```

说明：

- `assetCode` 由后端自动生成，前端不提交
- `accumulatedDepreciation` 和 `netValue` 由后端自动计算，前端不提交
- 新增后默认状态为 `IDLE`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 22
}
```

`data` 为新资产的 `id`。

### 3.4 编辑资产

```
PUT /api/assets/{id}
```

请求头：`Authorization: Bearer <token>`

请求体同新增，增加 `status` 字段：

```json
{
  "assetName": "测试资产-已编辑",
  "categoryId": 2,
  "specification": "i7/16G/512G",
  "brand": "Lenovo",
  "purchaseDate": "2026-07-01",
  "originalValue": 6800.00,
  "usefulLife": 4,
  "residualRate": 0.05,
  "department": "信息中心",
  "keeper": "张三",
  "location": "A座3层",
  "status": "IN_USE",
  "qrCode": "",
  "rfidCode": "",
  "photoUrl": "",
  "remark": ""
}
```

说明：

- 编辑时如果 `status` 发生变化，自动写入 `asset_operation_log`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 3.5 删除资产

```
DELETE /api/assets/{id}
```

请求头：`Authorization: Bearer <token>`

说明：

- 使用逻辑删除，`deleted` 标记为 1
- 分页接口不再返回已删除资产

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 3.6 查询资产状态字典

```
GET /api/assets/status-options
```

请求头：`Authorization: Bearer <token>`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "code": "IDLE", "label": "闲置" },
    { "code": "IN_USE", "label": "使用中" },
    { "code": "TRANSFERRING", "label": "调拨中" },
    { "code": "REPAIRING", "label": "维修中" },
    { "code": "WAITING_SCRAP", "label": "待报废" },
    { "code": "SCRAPPED", "label": "已报废" },
    { "code": "INVENTORY_ABNORMAL", "label": "盘点异常" }
  ]
}
```

---

## 4. 资产分类接口

### 4.1 分类列表

```
GET /api/asset-categories/list
```

请求头：`Authorization: Bearer <token>`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryCode": "OFFICE",
      "categoryName": "办公设备",
      "parentId": 0,
      "depreciationYears": 5,
      "remark": "办公类固定资产"
    }
  ]
}
```

### 4.2 分类树

```
GET /api/asset-categories/tree
```

请求头：`Authorization: Bearer <token>`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryCode": "OFFICE",
      "categoryName": "办公设备",
      "parentId": 0,
      "depreciationYears": 5,
      "remark": "办公类固定资产",
      "children": []
    }
  ]
}
```

---

## 5. 首页驾驶舱接口

### 5.1 核心统计

```
GET /api/dashboard/stats
```

请求头：`Authorization: Bearer <token>`

说明：数据来自真实 `asset` 表实时统计。

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "assetCount": 20,
    "totalOriginalValue": 9596600.00,
    "totalAccumulatedDepreciation": 3778142.05,
    "totalNetValue": 5818457.95,
    "inUseCount": 11,
    "idleCount": 4,
    "repairingCount": 3,
    "waitingScrapCount": 2
  }
}
```

### 5.2 分类分布

```
GET /api/dashboard/category-distribution
```

请求头：`Authorization: Bearer <token>`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "name": "办公设备", "value": 8 },
    { "name": "电子设备", "value": 6 }
  ]
}
```

### 5.3 部门金额排行

```
GET /api/dashboard/department-ranking
```

请求头：`Authorization: Bearer <token>`

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "department": "综合办公室", "amount": 8800000.00 },
    { "department": "资产管理部", "amount": 3109500.00 }
  ]
}
```

### 5.4 月度折旧趋势

```
GET /api/dashboard/depreciation-trend
```

请求头：`Authorization: Bearer <token>`

说明：第一阶段为 mock 数据。

返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "month": "2026-02", "value": 125000.00 },
    { "month": "2026-03", "value": 128000.00 }
  ]
}
```
---

## 6. 生命周期单据接口

### 6.1 资产选择列表

`
GET /api/lifecycle/asset-select-options?status=IDLE
`

请求头：Authorization: Bearer <token>

查询参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| status | string | 可选，按状态筛选（IDLE/IN_USE 等） |

返回：

`json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "assetCode": "FA2024030001", "assetName": "台式电脑-A01", "department": "信息中心", "keeper": "张伟", "location": "A座3层机房", "status": "IDLE" }
  ]
}
`

### 6.2 入库单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/lifecycle/inbound | 创建入库单 |
| GET | /api/lifecycle/inbound/{id} | 入库单详情 |
| GET | /api/lifecycle/inbound/page | 入库单分页 |

创建请求体：

`json
{
  "assetId": 1,
  "inboundType": "采购",
  "supplier": "供应商名称",
  "purchaseOrderNo": "PO202607001",
  "inboundDate": "2026-07-05",
  "handler": "经办人",
  "remark": ""
}
`

说明：

- 入库后资产状态为 IDLE
- 单据编号格式：IN + yyyyMMdd + 4位流水号

### 6.3 领用单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/lifecycle/receive | 创建领用单 |
| GET | /api/lifecycle/receive/{id} | 领用单详情 |
| GET | /api/lifecycle/receive/page | 领用单分页 |

创建请求体：

`json
{
  "assetId": 1,
  "receiver": "领用人姓名",
  "receiverDepartment": "领用部门",
  "receiveDate": "2026-07-05",
  "usagePurpose": "使用用途",
  "remark": ""
}
`

说明：

- 仅允许 IDLE 资产领用
- 领用后 asset.status = IN_USE, department/keeper 更新
- 单据编号格式：RE + yyyyMMdd + 4位流水号

### 6.4 调拨单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/lifecycle/transfer | 创建调拨单 |
| GET | /api/lifecycle/transfer/{id} | 调拨单详情 |
| GET | /api/lifecycle/transfer/page | 调拨单分页 |

创建请求体：

`json
{
  "assetId": 1,
  "toDepartment": "调入部门",
  "toLocation": "调入地点",
  "toKeeper": "调入保管人",
  "transferDate": "2026-07-05",
  "remark": ""
}
`

说明：

- 仅允许 IDLE 或 IN_USE 资产调拨
- 调拨后更新 department/location/keeper
- 单据编号格式：TF + yyyyMMdd + 4位流水号

### 6.5 维修单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/lifecycle/repair | 创建维修单 |
| GET | /api/lifecycle/repair/{id} | 维修单详情 |
| GET | /api/lifecycle/repair/page | 维修单分页 |
| PUT | /api/lifecycle/repair/{id}/complete | 完成维修 |

创建请求体：

`json
{
  "assetId": 1,
  "faultDescription": "故障描述",
  "repairVendor": "维修厂商",
  "repairCost": 1200.00,
  "repairStartDate": "2026-07-01",
  "remark": ""
}
`

完成维修请求体：

`json
{
  "repairResult": "REPAIRED",
  "repairVendor": "维修厂商",
  "repairCost": 1200.00,
  "repairEndDate": "2026-07-05",
  "remark": "维修备注"
}
`

说明：

- 仅允许 IDLE 或 IN_USE 资产发起维修
- 创建后 asset.status = REPAIRING，维修单 status = DRAFT
- repairResult 可选值：REPAIRED（→ IN_USE）、SCRAP_SUGGESTED（→ WAITING_SCRAP）
- 单据编号格式：RP + yyyyMMdd + 4位流水号

### 6.6 报废单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/lifecycle/scrap | 创建报废单 |
| GET | /api/lifecycle/scrap/{id} | 报废单详情 |
| GET | /api/lifecycle/scrap/page | 报废单分页 |

创建请求体：

`json
{
  "assetId": 1,
  "scrapReason": "报废原因",
  "scrapDate": "2026-07-05",
  "disposalMethod": "回收",
  "residualValue": 500.00,
  "remark": ""
}
`

说明：

- 允许 IDLE、IN_USE、REPAIRING、WAITING_SCRAP 报废
- 报废后 asset.status = SCRAPPED
- 单据编号格式：SC + yyyyMMdd + 4位流水号

### 6.7 单据状态

| 状态 | 说明 |
|---|---|
| DRAFT | 草稿（维修单创建后默认） |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
