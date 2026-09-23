# Phase 11 - 预警中心 Design

## 1. 数据库变更

**无数据库变更**。完全复用现有表：asset、asset_operation_log、asset_repair_order、inventory_record、finance_sync_record。

## 2. 后端模块结构

```
backend/src/main/java/com/example/asset/warning/
├── controller/
│   └── WarningController.java
├── service/
│   └── WarningService.java
├── mapper/
│   └── WarningMapper.java
└── vo/
    ├── WarningSummaryVO.java
    ├── WarningItemVO.java
    └── WarningQueryRequest.java
```

```
backend/src/main/resources/mapper/warning/
└── WarningMapper.xml
```

## 3. 后端 VO 设计

### WarningSummaryVO

| 字段 | 类型 | 说明 |
|------|------|------|
| totalWarningCount | Integer | 预警总数 |
| highWarningCount | Integer | 高风险预警数 |
| mediumWarningCount | Integer | 中风险预警数 |
| lowWarningCount | Integer | 低风险预警数 |
| lowValueCount | Integer | 低净值资产数 |
| nearEndCount | Integer | 接近使用年限资产数 |
| idleLongTimeCount | Integer | 长期闲置资产数 |
| repairOverdueCount | Integer | 维修超期资产数 |
| inventoryAbnormalCount | Integer | 盘点异常数 |
| financeSyncAbnormalCount | Integer | 财务同步异常数 |

### WarningItemVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 预警项唯一 ID |
| warningType | String | 预警类型编码 |
| warningTypeName | String | 预警类型名称 |
| warningLevel | String | 预警等级 |
| title | String | 预警标题 |
| description | String | 预警描述 |
| assetId | Long | 关联资产 ID |
| assetCode | String | 关联资产编号 |
| assetName | String | 关联资产名称 |
| businessId | Long | 关联业务 ID |
| businessType | String | 业务来源类型 |
| source | String | 数据来源 |
| createdAt | String | 预警生成时间 |
| suggestion | String | 处置建议 |

### WarningQueryRequest

| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | 预警类型（可选） |
| level | String | 预警等级（可选） |
| pageNum | Integer | 页码，默认 1 |
| pageSize | Integer | 每页条数，默认 10 |

## 4. 预警常量设计

在 WarningService 中集中定义阈值常量，避免魔法数字散落：

```java
// 预警阈值
private static final BigDecimal LOW_VALUE_NET_VALUE_RATE = new BigDecimal("0.2");
private static final int NEAR_END_REMAINING_MONTHS = 6;
private static final int IDLE_LONG_TIME_DAYS = 90;
private static final int REPAIR_OVERDUE_DAYS = 30;
private static final String INVENTORY_NORMAL_RESULT = "NORMAL";
private static final String FINANCE_SYNC_SUCCESS = "SUCCESS";

// 预警等级
private static final String LEVEL_HIGH = "HIGH";
private static final String LEVEL_MEDIUM = "MEDIUM";
private static final String LEVEL_LOW = "LOW";

// 业务来源
private static final String SOURCE_ASSET = "ASSET";
private static final String SOURCE_REPAIR = "REPAIR";
private static final String SOURCE_INVENTORY = "INVENTORY";
private static final String SOURCE_FINANCE = "FINANCE";
private static final String SOURCE_SYSTEM = "SYSTEM";
```

## 5. 后端 Mapper 设计

### WarningMapper.java

```java
@Mapper
public interface WarningMapper {

    // 低净值资产
    List<WarningItemVO> selectLowValueAssets();

    // 接近使用年限资产
    List<WarningItemVO> selectNearEndAssets();

    // 长期闲置资产（status=IDLE 且最近操作时间或 created_at 距今 > 90 天）
    List<WarningItemVO> selectIdleLongTimeAssets(@Param("thresholdDate") LocalDateTime thresholdDate);

    // 维修超期资产（asset.status=REPAIRING 或维修单 status=DRAFT 且 repair_start_date 距今 > 30 天）
    List<WarningItemVO> selectRepairOverdueAssets(@Param("thresholdDate") LocalDate thresholdDate);

    // 盘点异常记录（result IS NOT NULL AND result != 'NORMAL'）
    List<WarningItemVO> selectInventoryAbnormalRecords();

    // 财务同步异常记录（status != 'SUCCESS'）
    List<WarningItemVO> selectFinanceSyncAbnormalRecords();
}
```

### WarningMapper.xml 关键 SQL

```xml
<!-- 低净值资产：net_value / original_value <= 0.2 且未报废 -->
<select id="selectLowValueAssets" resultType="...WarningItemVO">
    SELECT
        a.id AS assetId,
        a.asset_code AS assetCode,
        a.asset_name AS assetName,
        CONCAT('低净值资产：', a.asset_name, '（净值率 ', ROUND(a.net_value/a.original_value*100, 2), '%）') AS title,
        CONCAT('资产编号 ', a.asset_code, '，原值 ', a.original_value, '，净值 ', a.net_value) AS description,
        '建议评估资产是否需要报废或调拨' AS suggestion
    FROM asset a
    WHERE a.deleted = 0
      AND a.status NOT IN ('SCRAPPED')
      AND a.original_value > 0
      AND a.net_value / a.original_value &lt;= 0.2
    ORDER BY a.net_value / a.original_value ASC
</select>

<!-- 接近使用年限：剩余月数 < 6 -->
<select id="selectNearEndAssets" resultType="...WarningItemVO">
    SELECT
        a.id AS assetId,
        a.asset_code AS assetCode,
        a.asset_name AS assetName,
        CONCAT('接近使用年限：', a.asset_name, '（剩余 ', a.useful_life * 12 - PERIOD_DIFF(...), ' 个月）') AS title,
        ...
    FROM asset a
    WHERE a.deleted = 0
      AND a.status NOT IN ('SCRAPPED')
      AND a.useful_life IS NOT NULL AND a.purchase_date IS NOT NULL
      AND a.useful_life * 12 - PERIOD_DIFF(DATE_FORMAT(NOW(), '%Y%m'), DATE_FORMAT(a.purchase_date, '%Y%m')) &lt; 6
    ORDER BY 剩余月数 ASC
</select>

<!-- 长期闲置：status=IDLE 且最近操作时间或 created_at 距今 > 90 天 -->
<select id="selectIdleLongTimeAssets" resultType="...WarningItemVO">
    SELECT * FROM (
        SELECT
            a.id AS assetId, a.asset_code AS assetCode, a.asset_name AS assetName,
            a.created_at AS assetCreatedAt,
            (SELECT MAX(l.operation_time) FROM asset_operation_log l WHERE l.asset_id = a.id) AS lastOperationTime,
            ...
        FROM asset a
        WHERE a.deleted = 0 AND a.status = 'IDLE'
    ) t
    WHERE COALESCE(t.lastOperationTime, t.assetCreatedAt) &lt; #{thresholdDate}
</select>

<!-- 维修超期：asset.status=REPAIRING 或维修单 status=DRAFT 且 repair_start_date 距今 > 30 天 -->
<select id="selectRepairOverdueAssets" resultType="...WarningItemVO">
    SELECT
        r.id AS businessId, r.asset_id AS assetId,
        a.asset_code AS assetCode, a.asset_name AS assetName,
        r.repair_start_date AS repairStartDate, r.status AS repairStatus,
        ...
    FROM asset_repair_order r
    LEFT JOIN asset a ON a.id = r.asset_id
    WHERE r.repair_start_date IS NOT NULL
      AND r.repair_start_date &lt; #{thresholdDate}
      AND (r.status = 'DRAFT' OR a.status = 'REPAIRING')
    ORDER BY r.repair_start_date ASC
</select>

<!-- 盘点异常：result IS NOT NULL AND result != 'NORMAL' -->
<select id="selectInventoryAbnormalRecords" resultType="...WarningItemVO">
    SELECT
        r.id AS businessId, r.task_id AS taskId, r.asset_id AS assetId,
        a.asset_code AS assetCode, a.asset_name AS assetName,
        r.result, r.remark,
        ...
    FROM inventory_record r
    LEFT JOIN asset a ON a.id = r.asset_id
    WHERE r.result IS NOT NULL AND r.result != 'NORMAL'
    ORDER BY r.scanned_at DESC
</select>

<!-- 财务同步异常：status != 'SUCCESS' -->
<select id="selectFinanceSyncAbnormalRecords" resultType="...WarningItemVO">
    SELECT
        f.id AS businessId, f.sync_month AS syncMonth, f.status, f.remark,
        ...
    FROM finance_sync_record f
    WHERE f.status != 'SUCCESS'
    ORDER BY f.created_at DESC
</select>
```

## 6. 后端 Service 设计

### WarningService

```java
@Service
public class WarningService {

    private final WarningMapper warningMapper;

    // 集中定义常量（见第 4 节）

    public WarningSummaryVO getSummary() {
        // 调用 6 个 mapper 方法统计各类预警数
        // 汇总 totalWarningCount、按等级统计
    }

    public PageResult<WarningItemVO> getItems(WarningQueryRequest query) {
        // 1. 根据查询条件决定调用哪些 mapper 方法
        //    - type 为空：调用全部 6 个方法并合并
        //    - type 指定：只调用对应方法
        // 2. 为每条预警补充 warningType、warningTypeName、warningLevel、businessType、source、createdAt、suggestion
        // 3. 财务同步异常额外检查当月是否有 SUCCESS 记录，无则追加"本月未同步"预警
        // 4. 内存分页（预警数据量可控，无需数据库分页）
        // 5. 返回 PageResult
    }

    // 各类型预警补充元信息的私有方法
    private WarningItemVO decorateLowValue(WarningItemVO raw);
    private WarningItemVO decorateNearEnd(WarningItemVO raw);
    // ...
}
```

### 预警 ID 生成策略

为保证预警项在单次查询中唯一，使用类型前缀 + 业务 ID 拼接：

- LOW_VALUE: `LV-{assetId}`
- NEAR_END: `NE-{assetId}`
- IDLE_LONG_TIME: `IL-{assetId}`
- REPAIR_OVERDUE: `RO-{repairOrderId}`
- INVENTORY_ABNORMAL: `IA-{inventoryRecordId}`
- FINANCE_SYNC_ABNORMAL: `FA-{syncRecordId}` 或 `FA-MISSING-{month}`（本月未同步）

由于 ID 在 VO 中为 Long 类型，使用 `businessId` 作为唯一标识，并在分页时按业务 ID 去重。实际实现中 `id` 字段直接使用业务 ID（assetId 或 orderId 等），并通过 `warningType` 区分。

## 7. 后端 Controller 设计

```java
@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private final WarningService warningService;

    @GetMapping("/summary")
    public Result<WarningSummaryVO> summary() {
        return Result.success(warningService.getSummary());
    }

    @GetMapping("/items")
    public Result<PageResult<WarningItemVO>> items(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        WarningQueryRequest query = new WarningQueryRequest();
        query.setType(type);
        query.setLevel(level);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(warningService.getItems(query));
    }
}
```

## 8. 前端 API 设计

### frontend/src/api/warning.ts

```typescript
import request from './request'

export interface WarningSummary {
  totalWarningCount: number
  highWarningCount: number
  mediumWarningCount: number
  lowWarningCount: number
  lowValueCount: number
  nearEndCount: number
  idleLongTimeCount: number
  repairOverdueCount: number
  inventoryAbnormalCount: number
  financeSyncAbnormalCount: number
}

export interface WarningItem {
  id: number
  warningType: string
  warningTypeName: string
  warningLevel: string
  title: string
  description: string
  assetId: number | null
  assetCode: string | null
  assetName: string | null
  businessId: number | null
  businessType: string
  source: string
  createdAt: string
  suggestion: string
}

export function getWarningSummary() {
  return request.get('/warnings/summary')
}

export function getWarningItems(params: {
  type?: string
  level?: string
  pageNum: number
  pageSize: number
}) {
  return request.get('/warnings/items', { params })
}
```

## 9. 前端页面设计

### WarningCenter.vue

```
PageHeader（标题：预警中心，描述：集中展示资产价值、使用年限、闲置、维修、盘点和财务同步风险）
├── 顶部统计卡片（4 列）
│   ├── 总预警数（totalWarningCount）
│   ├── 高风险（highWarningCount，红色）
│   ├── 中风险（mediumWarningCount，橙色）
│   └── 低风险（lowWarningCount，蓝色）
├── 类型数量卡片（6 列）
│   ├── 低净值资产（lowValueCount）
│   ├── 接近使用年限（nearEndCount）
│   ├── 长期闲置（idleLongTimeCount）
│   ├── 维修超期（repairOverdueCount）
│   ├── 盘点异常（inventoryAbnormalCount）
│   └── 财务同步异常（financeSyncAbnormalCount）
├── 筛选区域
│   ├── 预警类型下拉（type）
│   └── 预警等级下拉（level）
├── 预警列表表格
│   ├── 预警类型（warningTypeName，带 tag 颜色）
│   ├── 等级（warningLevel，带 tag 颜色）
│   ├── 标题（title）
│   ├── 描述（description，show-overflow-tooltip）
│   ├── 资产编号（assetCode）
│   ├── 资产名称（assetName）
│   ├── 生成时间（createdAt）
│   ├── 处置建议（suggestion，show-overflow-tooltip）
│   └── 操作（查看资产，assetId 为空时禁用）
├── 分页
└── 空数据：el-empty
```

### 资产跳转逻辑

- 点击"查看资产"按钮，若 `assetId` 存在，`router.push('/assets/' + assetId)`
- 若 `assetId` 为空（如本月未同步预警），按钮禁用

## 10. 路由和菜单设计

### 路由（router/index.ts）

```typescript
{
  path: 'warning-center',
  name: 'WarningCenter',
  component: () => import('@/views/warning/WarningCenter.vue'),
  meta: { title: '预警中心' }
  // 本阶段不加 permission，Phase 12 多角色权限收敛时补 warning:view
}
```

### 菜单（MainLayout.vue）

在"财务数据"菜单项后新增：

```vue
<el-menu-item index="/warning-center">
  <el-icon><Warning /></el-icon>
  <span>预警中心</span>
</el-menu-item>
```

不使用权限码控制，所有登录用户可见。

## 11. 文件清单

### 新增文件（9）

- docs/sdd/phase-11-warning-center-spec.md
- docs/sdd/phase-11-warning-center-design.md
- docs/sdd/phase-11-warning-center-tasks.md
- docs/sdd/phase-11-warning-center-acceptance.md
- backend/src/main/java/com/example/asset/warning/controller/WarningController.java
- backend/src/main/java/com/example/asset/warning/service/WarningService.java
- backend/src/main/java/com/example/asset/warning/mapper/WarningMapper.java
- backend/src/main/java/com/example/asset/warning/vo/WarningSummaryVO.java
- backend/src/main/java/com/example/asset/warning/vo/WarningItemVO.java

### 修改文件（4）

- backend/src/main/resources/mapper/warning/WarningMapper.xml（新增）
- frontend/src/api/warning.ts（新增）
- frontend/src/views/warning/WarningCenter.vue（新增）
- frontend/src/router/index.ts（新增路由）
- frontend/src/layouts/MainLayout.vue（新增菜单项）

> 注：WarningQueryRequest 作为查询参数对象，直接在 WarningService.getItems 方法签名中使用 @RequestParam 接收，不单独建 DTO 类，减少文件数量。
