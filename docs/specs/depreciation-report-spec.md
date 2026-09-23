# 折旧报表与价值分析规格草案

> 状态：规划中
> 对应 SDD 草案：`docs/sdd/phase-5-depreciation-report-draft.md`

## 1. 功能定义

基于系统内部资产数据计算和展示资产原值、累计折旧、净值、月度折旧、部门统计和分类统计。

## 2. 做什么

- 月度折旧报表生成
- 按部门统计资产价值
- 按分类统计资产价值
- 资产净值趋势图展示
- 折旧计算结果展示（平均年限法）

## 3. 不做什么

- 不做多折旧方法切换（当前只用平均年限法）
- 不做折旧凭证导出到财务软件
- 不做资产减值测试
- 不做税务折旧调整

## 4. 数据来源

| 数据 | 来源 |
|---|---|
| 资产原值 | `asset.original_value` |
| 残值率 | `asset.residual_rate` |
| 使用年限 | `asset.useful_life` |
| 入库日期 | `asset.purchase_date` |
| 折旧记录 | `asset_depreciation` 表 |

## 5. 接口规划

- `GET /api/depreciation/report/monthly` - 月度折旧报表
- `GET /api/depreciation/statistics/department` - 部门资产价值统计
- `GET /api/depreciation/statistics/category` - 分类资产价值统计
- `GET /api/depreciation/trend` - 资产净值趋势

## 6. 前端页面

- 折旧报表页（已有入口，待完善）
- 部门资产价值统计页
- 分类资产价值统计页
- 资产净值趋势页（ECharts 图表）

## 7. 验收标准

- 可按月份生成折旧报表
- 可按部门查看资产价值统计
- 可按分类查看资产价值统计
- 折旧计算与平均年限法公式一致
- 前端可展示趋势图和统计表
