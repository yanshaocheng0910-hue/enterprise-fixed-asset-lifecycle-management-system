# Phase 11 - 预警中心 Tasks

## T1 SDD 文档

- 新增 spec/design/tasks/acceptance 4 个文档
- 状态：✅ 完成

## T2 后端 VO 设计

- 新增 WarningSummaryVO（10 个字段）
- 新增 WarningItemVO（14 个字段）
- 状态：待执行

## T3 后端 Mapper 设计

- 新增 WarningMapper 接口（6 个查询方法）
- 新增 WarningMapper.xml（6 条 SQL）
- 状态：待执行

## T4 后端 Service/Controller 实现

- 新增 WarningService（getSummary、getItems）
- 集中定义阈值常量
- 新增 WarningController（2 个端点）
- 状态：待执行

## T5 前端 API 封装

- 新增 frontend/src/api/warning.ts
- 封装 getWarningSummary、getWarningItems
- 状态：待执行

## T6 前端预警中心页面

- 新增 frontend/src/views/warning/WarningCenter.vue
- 顶部 4 个统计卡片
- 中间 6 个类型数量卡片
- 筛选区域（类型 + 等级）
- 预警列表表格 + 分页
- 查看资产跳转
- 空数据 el-empty
- 状态：待执行

## T7 路由和菜单

- 修改 frontend/src/router/index.ts，新增 /warning-center 路由
- 修改 frontend/src/layouts/MainLayout.vue，新增"预警中心"菜单项
- 状态：待执行

## T8 构建验收

- 后端：cd backend && mvn -DskipTests package
- 前端：cd frontend && npm run build
- 状态：待执行

## T9 API 验收 + 审计报告 + 提交

- 登录 admin/123456
- 测试 GET /api/warnings/summary
- 测试 GET /api/warnings/items（无筛选、按 type、按 level）
- 回归：assets/page、approval/todo/page、inventory/tasks/page、finance/sync/records、assets/1/timeline
- 输出审计报告
- git commit -m "feat(warning): add asset warning center"
- git push origin main
- 状态：待执行
