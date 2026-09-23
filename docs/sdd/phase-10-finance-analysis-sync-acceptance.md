# Phase 10 - 财务数据分析与模拟同步增强 Acceptance

## 1. 构建验收

- [ ] 后端 `mvn -DskipTests package` 构建成功
- [ ] 前端 `npm run build` 构建成功

## 2. 功能验收

- [ ] GET /api/depreciation/summary 返回 200，含 monthlyDepreciation 字段
- [ ] POST /api/finance/sync/depreciation?month=2026-07 返回 200，生成完整同步记录
- [ ] GET /api/finance/sync/records 返回 200，分页列表含 VO 字段
- [ ] GET /api/finance/sync/records/{id} 返回 200，详情完整
- [ ] 前端折旧报表页面显示本月折旧额卡片
- [ ] 前端财务同步页面显示总览卡片
- [ ] 前端财务同步页面显示同步按钮
- [ ] 前端财务同步页面显示记录表格
- [ ] 前端财务同步页面显示详情弹窗

## 3. 回归验收

- [ ] GET /api/assets/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/approval/todo/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/inventory/tasks/page?pageNum=1&pageSize=10 返回 200
- [ ] GET /api/assets/1/timeline 返回 200

## 4. 数据库验收

- [ ] 未新增数据库表（仅 ALTER TABLE 补字段）
- [ ] migration SQL 有明确原因说明
- [ ] 未修改审批核心逻辑
- [ ] 未修改生命周期状态流转
- [ ] 未修改盘点主链路
- [ ] 未真实调用外部财务系统

## 5. 审计报告清单

- 是否新增数据库表
- 是否新增字段
- 是否修改审批核心逻辑（必须为否）
- 是否修改生命周期状态流转（必须为否）
- 是否修改盘点主链路（必须为否）
- 是否真实调用外部财务系统（必须为否）
- 新增文件列表
- 修改文件列表
- 后端构建结果
- 前端构建结果
- API 验收结果
- git diff --stat
- git status --short
