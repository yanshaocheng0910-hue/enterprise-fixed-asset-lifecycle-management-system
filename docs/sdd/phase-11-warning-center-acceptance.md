# Phase 11 - 预警中心 Acceptance

## 1. 构建验收

- [ ] 后端 `mvn -DskipTests package` 构建成功
- [ ] 前端 `npm run build` 构建成功

## 2. 后端 API 验收

- [ ] GET /api/warnings/summary 返回 200，含 totalWarningCount、highWarningCount、mediumWarningCount、lowWarningCount
- [ ] GET /api/warnings/summary 返回 6 个类型计数字段
- [ ] GET /api/warnings/items 返回 200，分页结构正确（records、total、pageNum、pageSize）
- [ ] GET /api/warnings/items?type=LOW_VALUE 返回 200，仅返回低净值预警
- [ ] GET /api/warnings/items?level=HIGH 返回 200，仅返回高风险预警
- [ ] GET /api/warnings/items?type=REPAIR_OVERDUE 返回 200，仅返回维修超期预警

## 3. 前端页面验收

- [ ] 访问 /warning-center 页面正常加载
- [ ] 顶部显示 4 个统计卡片（总预警、高风险、中风险、低风险）
- [ ] 中间显示 6 个类型数量卡片
- [ ] 显示预警列表表格
- [ ] 筛选 warningType 下拉正常
- [ ] 筛选 warningLevel 下拉正常
- [ ] 预警类型列显示带颜色 tag
- [ ] 预警等级列显示带颜色 tag
- [ ] 点击"查看资产"按钮跳转到资产详情页
- [ ] assetId 为空的预警"查看资产"按钮禁用
- [ ] 空数据显示 el-empty
- [ ] 加载中显示 loading

## 4. 回归验收

- [ ] GET /api/assets/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/approval/todo/page?pageNum=1&pageSize=2 返回 200
- [ ] GET /api/inventory/tasks/page?pageNum=1&pageSize=10 返回 200
- [ ] GET /api/finance/sync/records?pageNum=1&pageSize=10 返回 200
- [ ] GET /api/assets/1/timeline 返回 200

## 5. 数据库验收

- [ ] 未新增数据库表
- [ ] 未新增数据库字段
- [ ] 未修改审批核心逻辑
- [ ] 未修改生命周期状态流转
- [ ] 未修改盘点主链路
- [ ] 未修改财务同步主链路
- [ ] 未涉及任何 AI 功能

## 6. 审计报告清单

- 是否新增数据库表
- 是否新增字段
- 是否修改审批核心逻辑（必须为否）
- 是否修改生命周期状态流转（必须为否）
- 是否修改盘点主链路（必须为否）
- 是否修改财务同步主链路（必须为否）
- 是否涉及 AI 功能（必须为否）
- 新增文件列表
- 修改文件列表
- 后端构建结果
- 前端构建结果
- 浏览器/API 验收结果
- git diff --stat
- git status --short
