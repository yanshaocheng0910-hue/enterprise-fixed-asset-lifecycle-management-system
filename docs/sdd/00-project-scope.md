# 项目总边界与阶段划分

## 项目名称

国企固定资产全生命周期管理系统

## 技术栈

- 后端：Spring Boot 3 / Java 17 / MyBatis-Plus / MySQL 8 / JWT
- 前端：Vue 3 / TypeScript / Vite / Element Plus / Pinia / ECharts
- 架构：前后端分离，RESTful API，JWT 鉴权

## 第一阶段：已完成

### 完成内容

- 登录鉴权（JWT + HandlerInterceptor）
- 首页驾驶舱（真实 asset 表统计 + ECharts 图表）
- 资产台账（分页查询、多条件查询、新增、编辑、逻辑删除）
- 资产详情（基础信息、价值信息、使用信息、状态信息）
- 资产分类（树 + 列表）
- 项目文档、启动脚本、接口测试文件、环境配置示例

### 本阶段不做

- 生命周期单据流（Phase 2）
- 审批流（Phase 3）
- 盘点管理（Phase 4）
- AI 辅助分析（Phase 7）
- 财务数据查看与模拟记录（Phase 5）

## 第二阶段：已完成

### 完成内容

- 资产入库（InboundOrder）
- 资产领用（ReceiveOrder）
- 资产调拨（TransferOrder）
- 维修管理（RepairOrder）
- 维修完成（REPAIRED / SCRAP_SUGGESTED）
- 报废管理（ScrapOrder）
- 5 张生命周期单据表
- 17 个生命周期接口
- 5 个生命周期前端页面
- 状态自动流转（IDLE / IN_USE / REPAIRING / WAITING_SCRAP / SCRAPPED）
- 非法状态拦截
- asset_operation_log 操作日志留痕
- 资产选择器（AssetSelect.vue）
- 单据详情弹窗（LifecycleDetailDialog.vue）

### 本阶段不做

- 审批流（Phase 3）
- 盘点管理（Phase 4）
- 折旧报表完整业务（Phase 5）
- 用户管理与权限（Phase 6）
- AI 辅助分析与财务数据查看（Phase 5 / 7）

## 后续阶段计划

| 阶段 | 状态 | 内容 |
|---|---|---|
| 第一阶段 | ✅ 已完成 | 项目骨架、登录、驾驶舱、资产台账、资产详情、资产分类 |
| 第二阶段 | ✅ 已完成 | 入库、领用、调拨、维修、报废五个生命周期模块 |
| 第三阶段 | ✅ 已完成 | 审批流（审批模板、审批实例、审批节点、审批记录） |
| 第四阶段 | 📋 计划中 | 盘点管理（盘点任务、盘点报告） |
| 第五阶段 | 📋 计划中 | 折旧报表与财务数据查看（月度报表、Excel 导出、模拟同步记录） |
| 第六阶段 | ✅ 已完成 | 用户管理与权限（用户 CRUD、角色分配、RBAC 权限） |
| 第七阶段 | 📋 计划中 | AI 辅助分析（资产状态摘要、异常提示、维修/报废辅助建议） |
| 第八阶段 | 📋 计划中 | 部署与验收材料（Docker、Nginx、使用说明、答辩文档） |
| 第九阶段 | 📋 规划中 | 资产生命周期时间线（串联资产操作、审批和日志记录） |
| 第十阶段 | 📋 规划中 | 审批流增强（待办、已办、进度、驳回重提） |
| 第十一阶段 | 📋 规划中 | 预警中心（基于规则的管理提醒） |

后续增强模块的详细规划见 `docs/roadmap.md`，阶段草案见 `docs/sdd/` 下的 draft 文件。
