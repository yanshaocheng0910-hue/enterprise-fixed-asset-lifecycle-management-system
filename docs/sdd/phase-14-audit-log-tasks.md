# Phase 14 - 全局操作日志与审计页面 任务清单 (Tasks)

## T1. SDD 文档（已完成）
- [x] docs/sdd/phase-14-audit-log-spec.md
- [x] docs/sdd/phase-14-audit-log-design.md
- [x] docs/sdd/phase-14-audit-log-tasks.md
- [x] docs/sdd/phase-14-audit-log-acceptance.md

## T2. 后端 VO/DTO
- [ ] AuditLogVO.java（15 字段）
- [ ] AuditSummaryVO.java（5 字段）
- [ ] AuditLogQueryRequest.java（8 字段：logType/assetCode/assetName/operatorName/startDate/endDate/pageNum/pageSize）

## T3. 后端 Mapper
- [ ] AuditMapper.java 接口（4 个查询方法 + 4 个 count 方法）
- [ ] AuditMapper.xml（4 个来源查询 SQL，含 JOIN 和 UNION 子查询）

## T4. 后端 Service
- [ ] AuditService.java
  - collectAll()：调用 4 个 Mapper 方法合并
  - page(req)：内存筛选 + 排序 + 分页
  - summary()：5 个统计指标
  - getDetail(id)：复合 ID 解析 + 回查

## T5. 后端 Controller
- [ ] AuditController.java
  - GET /api/audit/logs/summary
  - GET /api/audit/logs/page
  - GET /api/audit/logs/{id}
  - 3 个端点均加 @RequirePermission("approval:audit")

## T6. 导出接口
- [ ] 修改 ExportController.java
  - 注入 AuditService
  - 新增 GET /api/export/audit/logs
  - 加 @RequirePermission("approval:audit")

## T7. 前端 API
- [ ] frontend/src/api/audit.ts
  - getAuditSummary / getAuditLogPage / getAuditLogDetail / exportAuditLogs

## T8. 前端页面
- [ ] frontend/src/views/audit/AuditLog.vue
  - 统计卡片区（5 个）
  - 筛选区（日志类型/资产编号/资产名称/操作人/日期范围）
  - 日志表格 + 操作列（查看资产/查看详情）
  - 详情弹窗
  - 分页
  - 导出按钮

## T9. 路由与菜单
- [ ] 修改 router/index.ts 新增 /audit/logs 路由
- [ ] 修改 MainLayout.vue 新增「审计追踪」菜单

## T10. 构建验收
- [ ] cd backend && mvn -DskipTests package
- [ ] cd frontend && npm run build

## T11. API/浏览器验收
- [ ] 登录 admin/123456
- [ ] 进入审计日志页面
- [ ] 验证统计卡片数据
- [ ] 验证 4 类日志可查询
- [ ] 验证筛选功能
- [ ] 验证查看资产跳转
- [ ] 验证导出 Excel
- [ ] 回归其他模块

## T12. 审计报告 + 提交
- [ ] 输出审计报告
- [ ] git add（排除 IDE 产物/custom-settings.xml/.trae-html-share-packages/docs/.claude）
- [ ] git commit -m "feat(audit): add global audit log page"
- [ ] git push origin main
