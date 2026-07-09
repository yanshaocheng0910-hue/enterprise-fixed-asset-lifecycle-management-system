# Phase 10 - 财务数据分析与模拟同步增强 Design

## 1. 数据库变更

### migration-v6-finance-enhance.sql

```sql
ALTER TABLE finance_sync_record
    ADD COLUMN sync_batch_no VARCHAR(64) DEFAULT NULL COMMENT '同步批次号' AFTER sync_month,
    ADD COLUMN asset_count INT NOT NULL DEFAULT 0 COMMENT '资产数量' AFTER record_count,
    ADD COLUMN total_original_value DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '原值总额' AFTER total_amount,
    ADD COLUMN total_net_value DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '净值总额' AFTER total_original_value,
    ADD COLUMN total_accumulated_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '累计折旧总额' AFTER total_net_value,
    ADD COLUMN monthly_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '本月折旧额' AFTER total_accumulated_depreciation,
    ADD COLUMN operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人' AFTER status;
```

保留原有 total_amount、record_count 字段不变。

## 2. 后端 VO 设计

### DepreciationSummaryVO（修改 - 新增字段）

| 字段 | 类型 | 说明 |
|------|------|------|
| assetCount | Integer | 有效资产总数 |
| totalOriginalValue | BigDecimal | 原值总额 |
| totalNetValue | BigDecimal | 净值总额 |
| totalAccumulatedDepreciation | BigDecimal | 累计折旧总额 |
| monthlyDepreciation | BigDecimal | **本月折旧额（新增）** |
| averageDepreciationRate | BigDecimal | 平均折旧率（%） |
| lowValueAssetCount | Integer | 低净值资产数 |
| nearEndAssetCount | Integer | 接近报废资产数 |

### FinanceSyncRecordVO（新增）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录 ID |
| syncBatchNo | String | 同步批次号 |
| syncMonth | String | 同步月份 |
| assetCount | Integer | 资产数量 |
| totalOriginalValue | BigDecimal | 原值总额 |
| totalNetValue | BigDecimal | 净值总额 |
| totalAccumulatedDepreciation | BigDecimal | 累计折旧总额 |
| monthlyDepreciation | BigDecimal | 本月折旧额 |
| status | String | 状态 |
| operatorName | String | 操作人 |
| syncTime | String | 同步时间 |
| remark | String | 备注 |

## 3. 后端 Entity 修改

FinanceSyncRecord 新增字段：syncBatchNo、assetCount、totalOriginalValue、totalNetValue、totalAccumulatedDepreciation、monthlyDepreciation、operatorName

## 4. 后端 Service 设计

### DepreciationReportService.getSummary() 增强

- 调用 DepreciationRecordMapper 查询当前月的 monthlyDepreciation
- 如果当月无 depreciation_record，则基于 asset 表计算：SUM(original_value * (1 - residual_rate) / (useful_life * 12))

### FinanceService 增强

- syncDepreciation(month)：增强为计算完整价值快照
  - 查询 asset 表统计：assetCount、totalOriginalValue、totalNetValue、totalAccumulatedDepreciation
  - 查询 depreciation_record 获取 monthlyDepreciation（当月）
  - 生成 syncBatchNo = "FS" + yyyyMMddHHmmss
  - 从 UserContext 获取 operatorName
  - 写入 finance_sync_record
  - 返回 FinanceSyncRecordVO
- syncRecords(pageNum, pageSize)：分页查询，返回 FinanceSyncRecordVO 列表
- getSyncDetail(id)：按 ID 查询，返回 FinanceSyncRecordVO

## 5. 后端 Controller 设计

```java
@RestController
@RequestMapping("/api/finance")
public class FinanceSyncController {

    @PostMapping("/sync/depreciation")
    public Result<FinanceSyncRecordVO> syncDepreciation(@RequestParam String month)

    @GetMapping("/sync/records")
    public Result<PageResult<FinanceSyncRecordVO>> syncRecords(
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "10") Long pageSize)

    @GetMapping("/sync/records/{id}")
    public Result<FinanceSyncRecordVO> getSyncDetail(@PathVariable Long id)
}
```

## 6. 前端 API 设计

### depreciation.ts（不变，已有 6 个函数）

### finance.ts（重写）

```typescript
export function syncDepreciationData(month: string)
export function getFinanceSyncRecords(params: { pageNum: number; pageSize: number })
export function getFinanceSyncDetail(id: number)
```

## 7. 前端页面设计

### DepreciationReport.vue（微调）

- 指标卡区域从 6 列改为 7 列（或调整布局），新增"本月折旧额"卡片
- 其他不变

### FinanceSync.vue（重写）

```
PageHeader
├── 总览卡片区域（3-4 个 DataCard）
│   ├── 同步记录总数
│   ├── 最近同步月份
│   ├── 累计同步折旧额
│   └── 最近同步状态
├── 操作区域
│   ├── 月份选择器
│   └── "模拟同步折旧数据"按钮
├── 同步记录表格
│   ├── 批次号、同步月份、资产数量、原值、净值、累计折旧、本月折旧、状态、同步时间、操作人、操作（查看详情）
│   └── 分页
└── 详情弹窗
    └── el-descriptions 展示完整字段
```

## 8. 文件清单

### 新增文件（6）

- docs/sdd/phase-10-finance-analysis-sync-spec.md
- docs/sdd/phase-10-finance-analysis-sync-design.md
- docs/sdd/phase-10-finance-analysis-sync-tasks.md
- docs/sdd/phase-10-finance-analysis-sync-acceptance.md
- backend/src/main/resources/sql/migration-v6-finance-enhance.sql
- backend/src/main/java/com/example/asset/finance/vo/FinanceSyncRecordVO.java

### 修改文件（8）

- backend/.../depreciation/vo/DepreciationSummaryVO.java（+monthlyDepreciation）
- backend/.../depreciation/service/DepreciationReportService.java（计算 monthlyDepreciation）
- backend/.../finance/entity/FinanceSyncRecord.java（+7 字段）
- backend/.../finance/service/FinanceService.java（增强同步逻辑+详情查询）
- backend/.../finance/controller/FinanceSyncController.java（规范路径+详情端点）
- frontend/src/api/finance.ts（重写 API）
- frontend/src/views/finance/FinanceSync.vue（重写页面）
- frontend/src/views/depreciation/DepreciationReport.vue（+本月折旧卡）
