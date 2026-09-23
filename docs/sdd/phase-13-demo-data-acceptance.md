# Phase 13 - 演示数据增强 Acceptance

## 1. SQL 幂等性
- [ ] SQL 可重复执行不报错
- [ ] 不删除非 DEMO 数据
- [ ] 不修改 admin 账号

## 2. 数据覆盖
- [ ] 资产总数 >= 100
- [ ] 资产状态覆盖 7 种
- [ ] 资产分类 >= 8 类
- [ ] 部门 >= 8 个
- [ ] 生命周期单据 >= 80 条
- [ ] 审批记录有待办和已办
- [ ] 盘点任务 4 个，明细 100+
- [ ] 财务同步 6 条（含 SUCCESS 和 FAILED）
- [ ] 预警 6 种类型各有数据

## 3. 构建
- [ ] 后端 mvn package 成功
- [ ] 前端 npm run build 成功

## 4. API 回归
- [ ] /api/assets/page 正常
- [ ] /api/warnings/summary 6 种预警有数据
- [ ] /api/approval/todo/page 有数据
- [ ] /api/inventory/tasks/page 有数据
- [ ] /api/finance/sync/records 有数据
