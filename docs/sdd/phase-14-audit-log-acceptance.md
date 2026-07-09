# Phase 14 - 全局操作日志与审计页面 验收清单 (Acceptance)

## 1. 构建验收
- [ ] 后端 `mvn -DskipTests package` 成功（BUILD SUCCESS）
- [ ] 前端 `npm run build` 成功（无 TypeScript 错误）

## 2. 菜单与路由
- [ ] 登录 admin/123456 后侧边栏可见「审计追踪」菜单
- [ ] 点击菜单进入 /audit/logs 页面，标题显示「审计日志」
- [ ] 无 approval:audit 权限的角色不显示菜单（AUDITOR 角色可见）

## 3. 审计统计卡片
- [ ] 今日操作数 ≥ 0（与数据库今日操作数一致）
- [ ] 资产变更数 = asset_operation_log 总数
- [ ] 审批操作数 = approval_record 总数
- [ ] 盘点异常数 = inventory_record 中 result != 'NORMAL' 总数
- [ ] 财务同步数 = finance_sync_record 总数

## 4. 日志查询（4 类）
- [ ] 默认加载日志列表，按操作时间倒序
- [ ] 日志类型列显示中文（资产操作/审批操作/盘点异常/财务同步）
- [ ] 资产操作日志（ASSET_OPERATION）可查询，含资产编号/名称
- [ ] 审批操作日志（APPROVAL）可查询，含业务类型/审批动作
- [ ] 盘点异常日志（INVENTORY_ABNORMAL）可查询，含异常结果
- [ ] 财务同步日志（FINANCE_SYNC）可查询，含同步状态
- [ ] 操作时间格式为 yyyy-MM-dd HH:mm:ss

## 5. 筛选功能
- [ ] 按日志类型筛选正常（选择「资产操作」只显示 ASSET_OPERATION）
- [ ] 按资产编号筛选正常（输入 DEMO-EL-0001 只显示相关日志）
- [ ] 按资产名称筛选正常（模糊匹配）
- [ ] 按操作人筛选正常
- [ ] 按日期范围筛选正常
- [ ] 重置按钮清空所有筛选条件

## 6. 操作列
- [ ] 「查看资产」按钮：有 assetId 时跳转到 /assets/{assetId} 资产详情页
- [ ] 「查看详情」按钮：弹出详情弹窗，展示完整 AuditLogVO 字段
- [ ] 财务同步日志无 assetId 时「查看资产」按钮禁用或隐藏

## 7. 分页
- [ ] 分页器正常工作（切换页码加载对应数据）
- [ ] 总数显示正确
- [ ] 每页条数切换正常

## 8. 导出 Excel
- [ ] 点击「导出 Excel」按钮下载 .xlsx 文件
- [ ] 文件名包含「审计日志」
- [ ] 文件内容包含表头行和数据行
- [ ] 导出数据与当前筛选条件一致

## 9. 详情接口
- [ ] GET /api/audit/logs/{id} 返回完整 AuditLogVO
- [ ] 复合 ID（如 ASSET-123、APPROVAL-456）正确解析
- [ ] 不存在的 ID 返回 404/错误

## 10. 回归测试
- [ ] 资产台账页面正常（/assets）
- [ ] 审批中心待办/已办正常（/approval/todo, /approval/done）
- [ ] 盘点任务正常（/inventory/tasks）
- [ ] 财务同步正常（/finance/sync）
- [ ] 预警中心正常（/warning-center）
- [ ] AI 辅助分析正常（/ai/analysis）
- [ ] 其他 Excel 导出功能正常

## 11. 边界合规
- [ ] 未新增数据库表
- [ ] 未新增字段
- [ ] 未修改登录/JWT
- [ ] 未修改审批核心逻辑
- [ ] 未修改生命周期状态流转
- [ ] 未修改盘点/财务/预警/AI/Excel 主链路
- [ ] 未修改资产新增/编辑/删除主流程
- [ ] 未新增权限 SQL（复用 approval:audit）

## 12. 提交
- [ ] git add 仅包含本阶段新增/修改文件
- [ ] 排除 .trae-html-share-packages/、backend/custom-settings.xml、docs/.claude
- [ ] commit message: feat(audit): add global audit log page
- [ ] git push origin main 成功
