# Phase 10 - 折旧报表与资产价值分析 Design

## 1. 数据库复用

不新增表/字段/migration，复用 asset、asset_category、depreciation_record 表。

## 2. 后端新增 VO

### DepreciationSummaryVO

| 字段 | 类型 | 说明 |
|------|------|------|
| assetCount | Integer | 有效资产总数 |
| totalOriginalValue | BigDecimal | 原值总额 |
| totalNetValue | BigDecimal | 净值总额 |
| totalAccumulatedDepreciation | BigDecimal | 累计折旧总额 |
| averageDepreciationRate | BigDecimal | 平均折旧率（%） |
| lowValueAssetCount | Integer | 低净值资产数 |
| nearEndAssetCount | Integer | 接近报废资产数 |

### LowValueAssetVO

| 字段 | 类型 | 说明 |
|------|------|------|
| assetId | Long | 资产 ID |
| assetCode | String | 资产编号 |
| assetName | String | 资产名称 |
| department | String | 部门 |
| originalValue | BigDecimal | 原值 |
| netValue | BigDecimal | 净值 |
| netValueRate | BigDecimal | 净值率（0-1） |
| status | String | 资产状态 |

### NearEndAssetVO

| 字段 | 类型 | 说明 |
|------|------|------|
| assetId | Long | 资产 ID |
| assetCode | String | 资产编号 |
| assetName | String | 资产名称 |
| purchaseDate | LocalDate | 购置日期 |
| usefulLife | Integer | 使用年限 |
| usedMonths | Integer | 已使用月数 |
| remainingMonths | Integer | 剩余月数 |
| status | String | 资产状态 |

## 3. 后端 Mapper 新增方法

在 `DepreciationReportMapper` 接口新增 3 个方法，对应 XML 新增 3 条 SQL：

### selectDepreciationSummary

```sql
SELECT
    COUNT(*) AS assetCount,
    COALESCE(SUM(original_value), 0) AS totalOriginalValue,
    COALESCE(SUM(net_value), 0) AS totalNetValue,
    COALESCE(SUM(accumulated_depreciation), 0) AS totalAccumulatedDepreciation
FROM asset
WHERE deleted = 0 AND status NOT IN ('SCRAPPED')
```

低净值/接近报废计数用 Java 代码基于查询结果计算，避免重复扫描。

### selectLowValueAssets

```sql
SELECT
    id AS assetId, asset_code, asset_name, department,
    original_value, net_value,
    ROUND(net_value / original_value, 4) AS netValueRate,
    status
FROM asset
WHERE deleted = 0 AND status NOT IN ('SCRAPPED')
  AND original_value > 0
  AND net_value / original_value < 0.1
ORDER BY netValueRate ASC
```

### selectNearEndAssets

```sql
SELECT
    id AS assetId, asset_code, asset_name, purchase_date,
    useful_life,
    PERIOD_DIFF(DATE_FORMAT(NOW(), '%Y%m'), DATE_FORMAT(purchase_date, '%Y%m')) AS usedMonths,
    useful_life * 12 - PERIOD_DIFF(DATE_FORMAT(NOW(), '%Y%m'), DATE_FORMAT(purchase_date, '%Y%m')) AS remainingMonths,
    status
FROM asset
WHERE deleted = 0 AND status NOT IN ('SCRAPPED')
  AND useful_life IS NOT NULL
  AND purchase_date IS NOT NULL
  AND useful_life * 12 - PERIOD_DIFF(DATE_FORMAT(NOW(), '%Y%m'), DATE_FORMAT(purchase_date, '%Y%m')) < 12
ORDER BY remainingMonths ASC
```

## 4. 后端 Service 新增方法

在 `DepreciationReportService` 新增：

- `getSummary()`：调用 selectDepreciationSummary 获取基础汇总，再调用 selectLowValueAssets 和 selectNearEndAssets 的 size() 填充 lowValueAssetCount 和 nearEndAssetCount，计算 averageDepreciationRate = totalAccumulatedDepreciation / totalOriginalValue * 100
- `getLowValueAssets()`：直接调用 mapper
- `getNearEndAssets()`：直接调用 mapper

## 5. 后端 Controller 新增端点

在 `DepreciationReportController` 新增：

```java
@GetMapping("/summary")
public Result<DepreciationSummaryVO> summary()

@GetMapping("/low-value-assets")
public Result<List<LowValueAssetVO>> lowValueAssets()

@GetMapping("/near-end-assets")
public Result<List<NearEndAssetVO>> nearEndAssets()
```

## 6. 前端 API 设计

在 `frontend/src/api/depreciation.ts` 新增 3 个函数：

```typescript
export function getDepreciationSummary()
export function getLowValueAssets()
export function getNearEndAssets()
```

保留已有 5 个函数不变。

## 7. 前端页面设计

增强 `DepreciationReport.vue`：

### 页面结构

```
PageHeader
├── 指标卡区域（6 个 DataCard）
│   ├── 资产总数
│   ├── 原值总额
│   ├── 净值总额
│   ├── 累计折旧
│   ├── 平均折旧率
│   └── 低净值/接近报废资产数
├── 图表区域（grid 2 列）
│   ├── 部门资产价值统计（柱状图）
│   └── 分类资产价值统计（柱状图）
├── 月度折旧趋势（折线+柱状图）
└── Tabs
    ├── 月度折旧明细（已有表格）
    ├── 部门统计（已有表格）
    ├── 分类统计（已有表格）
    ├── 低净值资产（新增表格）
    └── 接近使用年限资产（新增表格）
```

### 图表设计

- 部门价值柱状图：X 轴部门名，Y 轴金额（万元），双柱（原值/净值）
- 分类价值柱状图：X 轴分类名，Y 轴金额（万元），双柱（原值/净值）
- 月度趋势图：保留现有设计（柱状+折线）

### 风险资产表格

- 低净值资产表：资产编号、资产名称、部门、原值、净值、净值率、状态
- 接近使用年限表：资产编号、资产名称、购置日期、使用年限、已用月数、剩余月数、状态

## 8. 文件清单

### 新增文件（3 个后端 VO + 4 个 SDD = 7）

- backend/.../depreciation/vo/DepreciationSummaryVO.java
- backend/.../depreciation/vo/LowValueAssetVO.java
- backend/.../depreciation/vo/NearEndAssetVO.java
- docs/sdd/phase-10-depreciation-report-spec.md
- docs/sdd/phase-10-depreciation-report-design.md
- docs/sdd/phase-10-depreciation-report-tasks.md
- docs/sdd/phase-10-depreciation-report-acceptance.md

### 修改文件（4）

- backend/.../depreciation/mapper/DepreciationReportMapper.java（新增 3 个方法）
- backend/src/main/resources/mapper/depreciation/DepreciationReportMapper.xml（新增 3 条 SQL）
- backend/.../depreciation/service/DepreciationReportService.java（新增 3 个方法）
- backend/.../depreciation/controller/DepreciationReportController.java（新增 3 个端点）
- frontend/src/api/depreciation.ts（新增 3 个函数）
- frontend/src/views/depreciation/DepreciationReport.vue（增强页面）
