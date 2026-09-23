# Phase 10 - 折旧报表与资产价值分析 Acceptance

## 1. 构建验收

- [ ] 后端 `mvn -DskipTests package` 构建成功
- [ ] 前端 `npm run build` 构建成功

## 2. 功能验收

- [ ] GET /api/depreciation/summary 返回 200，含 assetCount、totalOriginalValue、totalNetValue、totalAccumulatedDepreciation、averageDepreciationRate、lowValueAssetCount、nearEndAssetCount
- [ ] GET /api/depreciation/low-value-assets 返回 200，列表数据净值率均 < 10%
- [ ] GET /api/depreciation/near-end-assets 返回 200，列表数据剩余月数均 < 12
- [ ] 前端折旧报表页面显示指标卡
- [ ] 前端显示部门价值柱状图
- [ ] 前端显示分类价值柱状图
- [ ] 前端显示月度折旧趋势图
- [ ] 前端显示低净值资产表格
- [ ] 前端显示接近使用年限资产表格

## 3. 回归验收

- [ ] GET /api/assets/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/approval/todo/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/inventory/tasks/page?pageNum=1&pageSize=10 返回 200
- [ ] GET /api/assets/1/timeline 返回 200

## 4. 数据库验收

- [ ] 未新增数据库表
- [ ] 未新增字段
- [ ] 未新增 migration SQL

## 5. 代码验收

- [ ] 未修改审批核心逻辑
- [ ] 未修改生命周期状态流转
- [ ] 未修改盘点主链路
- [ ] 未修改登录/JWT

## 6. 审计报告清单

- 是否新增数据库表
- 是否新增字段
- 是否修改审批核心逻辑（必须为否）
- 是否修改生命周期状态流转（必须为否）
- 是否修改盘点主链路（必须为否）
- 新增文件列表
- 修改文件列表
- 后端构建结果
- 前端构建结果
- API 验收结果
- git diff --stat
- git status --short
