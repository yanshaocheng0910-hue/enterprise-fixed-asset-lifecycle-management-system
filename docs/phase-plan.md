# 阶段计划

## 第一阶段：项目骨架与核心主链路（已完成）

### 已完成内容

**后端：**

- Spring Boot 3 + Java 17 项目搭建
- MySQL 8 + MyBatis-Plus 配置
- 统一返回结构 Result<T> + PageResult<T>
- 统一异常处理 GlobalExceptionHandler
- JWT + HandlerInterceptor 鉴权
- 用户/角色/资产分类/资产/操作日志 表结构
- 折旧/盘点/财务 表结构预留
- 登录接口
- 资产 CRUD 接口（分页/详情/新增/编辑/删除）
- 资产分类查询接口
- 首页驾驶舱统计接口
- 初始化 SQL（含20条 mock 数据）
- 骨架接口（inventory, finance）

**前端：**

- Vue 3 + TypeScript + Vite 项目搭建
- Element Plus + Pinia + Vue Router + Axios + ECharts
- 登录页
- 首页驾驶舱（真实统计 + ECharts 图表）
- 资产台账（分页/查询/新增/编辑/删除/详情）
- 资产分类（树 + 列表）
- 10 个骨架页面（生命周期/盘点/折旧/财务/AI/用户管理）
- Axios 请求/响应拦截器（token 注入、401 处理）
- 路由守卫（未登录跳转、已登录跳转）
- 企业级 UI 风格

**数据库：**

- 9 张表（用户/角色/关联/分类/资产/日志/折旧/盘点任务/盘点明细）
- 默认管理员 admin/123456
- 4 个角色
- 5 个资产分类
- 20 条资产测试数据

---

## 第二阶段：生命周期单据流（已完成）

### 已实现

**新表：**

| 表名 | 说明 |
|---|---|
| asset_inbound_order | 入库单 |
| asset_receive_order | 领用单 |
| asset_transfer_order | 调拨单 |
| asset_repair_order | 维修单 |
| asset_scrap_order | 报废单 |

**新增接口（17 个）：**

- GET /api/lifecycle/asset-select-options - 资产选择列表
- 入库：POST /page/{id}
- 领用：POST /page/{id}
- 调拨：POST /page/{id}
- 维修：POST /page/{id} + PUT /{id}/complete
- 报废：POST /page/{id}

**状态流转实现：**

| 流程 | 条件 | 结果 |
|---|---|---|
| 领用 | IDLE → IN_USE | 更新部门/保管人 |
| 调拨 | IDLE/IN_USE → IN_USE | 更新部门/地点/保管人 |
| 维修 | IDLE/IN_USE → REPAIRING | 维修单 DRAFT |
| 维修完成 | REPAIRED → IN_USE | 维修单 COMPLETED |
| 维修完成 | SCRAP_SUGGESTED → WAITING_SCRAP | 维修单 COMPLETED |
| 报废 | IDLE/IN_USE/REPAIRING/WAITING_SCRAP → SCRAPPED | 报废单 COMPLETED |

---

## 第三阶段：审批流（已完成）

### 已实现

**新表：**

| 表名 | 说明 |
|---|---|
| approval_template | 审批模板 |
| approval_instance | 审批实例 |
| approval_node | 审批节点 |
| approval_record | 审批记录 |

**功能：**

- 审批模板管理（定义审批流程模板和节点）
- 审批实例创建（基于模板发起审批）
- 审批操作（提交、通过、驳回）
- 审批记录留痕（可追溯每次审批操作）

---

## 第四阶段：盘点管理（已完成）

### 已实现

- 创建盘点任务（选择部门/地点范围）
- 生成盘点清单
- 盘点任务管理（开始、执行、完成、删除）
- 盘点结果处理（正常/盘盈/盘亏/位置异常/保管人不符）
- 盘点报告
- 异常资产状态标记 INVENTORY_ABNORMAL
- 扫码盘点与批量扫码

### 后端接口

- `GET /api/inventory/tasks/page` - 盘点任务分页
- `POST /api/inventory/tasks` - 创建盘点任务
- `GET /api/inventory/tasks/{id}` - 任务详情
- `GET /api/inventory/tasks/{id}/records` - 盘点明细
- `PUT /api/inventory/records/{recordId}` - 更新盘点记录
- `POST /api/inventory/tasks/{id}/start` - 开始盘点
- `PUT /api/inventory/tasks/{id}/complete` - 完成盘点
- `PUT /api/inventory/records` - 扫码盘点
- `POST /api/inventory/tasks/{id}/batch-scan` - 批量扫码
- `GET /api/inventory/tasks/{id}/report` - 盘点报告

---

## 第五阶段：折旧报表与财务数据查看（已完成）

### 已实现

**后端接口（5 个）：**

| 接口 | 说明 |
|---|---|
| GET /api/depreciation/report/summary?month= | 月度报表汇总 |
| GET /api/depreciation/report/monthly?month= | 月度折旧明细 |
| GET /api/depreciation/statistics/department | 部门统计 |
| GET /api/depreciation/statistics/category | 分类统计 |
| GET /api/depreciation/trend | 折旧趋势 |

**新增文件：**

- `backend/.../depreciation/controller/DepreciationReportController.java`
- `backend/.../depreciation/service/DepreciationReportService.java`
- `backend/.../depreciation/mapper/DepreciationReportMapper.java`
- `backend/.../depreciation/vo/`（5 个 VO：CategoryStatVO、DepartmentStatVO、DepreciationTrendVO、MonthlyDepreciationItemVO、MonthlyReportSummaryVO）
- `backend/.../resources/mapper/depreciation/DepreciationReportMapper.xml`
- `frontend/src/api/depreciation.ts`

**功能：**

- 月度折旧报表汇总与明细
- 按部门统计折旧数据
- 按分类统计折旧数据
- 折旧趋势图展示
- 网页端财务数据查看与模拟同步记录

---

## 第六阶段：用户管理与权限 RBAC（已完成）

### 已实现

**新表：**

| 表名 | 说明 |
|---|---|
| sys_permission | 权限表（34 项权限种子数据） |
| sys_role_permission | 角色权限关联表 |

**功能：**

- 用户管理：用户 CRUD、状态切换、密码重置、角色分配
- 角色管理：角色 CRUD、角色权限分配
- 权限管理：34 项权限、`@RequirePermission` AOP 注解、前端 `v-permission` 指令
- 菜单控制：前端菜单根据权限动态显示

---

## 第七阶段：AI 辅助分析（计划中）

### 实现功能

- AI 资产状态摘要
- AI 异常资产提示
- AI 维修/报废辅助建议
- AI 管理报告辅助生成

### 定位

AI 辅助分析模块基于系统内部数据进行分析，数据来源包括资产台账、生命周期单据、维修记录、报废记录、审批记录和操作日志。AI 输出只作为辅助参考，不直接修改业务数据。

### 当前状态

前端已有 AI 辅助分析页面入口，完整分析能力待开发。

---

## 第八阶段：部署与验收材料（计划中）

### 实现功能

- Docker 容器化部署
- Nginx 反向代理配置
- 部署文档
- 使用说明

---

## 后续增强阶段（规划中）

以下阶段为后续增强模块，详细规划见 `docs/roadmap.md`，阶段草案见 `docs/sdd/`。

### 第九阶段：资产生命周期时间线（已完成）

**目标：**
在资产详情页展示资产从入库、领用、调拨、维修、报废、审批到操作日志的完整记录。

**开发优先级：** 1（后续增强第一优先级）

**SDD 文档：**
- `docs/sdd/phase-7-asset-timeline-spec.md`
- `docs/sdd/phase-7-asset-timeline-design.md`
- `docs/sdd/phase-7-asset-timeline-tasks.md`
- `docs/sdd/phase-7-asset-timeline-acceptance.md`

**已实现：**

- 后端：`AssetTimelineController` + `AssetTimelineService` + DTO/VO
- 接口：`GET /api/assets/{assetId}/timeline?eventType=`
- 前端：`AssetTimeline.vue` 组件，集成到 `AssetDetail.vue`
- 事件类型：INBOUND、RECEIVE、TRANSFER、REPAIR、SCRAP、APPROVAL、OPERATION_LOG
- 支持按事件类型筛选，按时间倒序排列

---

### 第十阶段：审批流增强（规划中）

**目标：**
完善我的待办、我的已办、审批详情、审批记录、审批进度展示和驳回重提。

**开发优先级：** 2

**草案文档：** `docs/sdd/phase-6-approval-enhancement-draft.md`

---

### 第十一阶段：预警中心（已完成）

**已实现：**
- 预警摘要统计（`GET /api/warnings/summary`）
- 预警列表分页（`GET /api/warnings/items`）
- 预警类型：长期闲置、维修次数多、低净值资产、财务同步异常、待审批超时
- 风险等级：高、中、低
- 前端预警中心页面（统计卡片 + 风险列表 + 处置建议）

**草案文档：** `docs/sdd/phase-9-warning-center-draft.md`

---

### 第十二阶段：Excel 导出（已完成）

**已实现：**
- 10 个 Excel 导出接口（资产台账、资产时间线、审批记录、盘点任务、盘点明细、折旧报表、财务同步、预警列表、AI 报告、审计日志）
- Apache POI SXSSF 流式导出
- 前端下载工具（`frontend/src/utils/download.ts`）
- 导出 API 封装（`frontend/src/api/export.ts`）

---

### 第十三阶段：审计日志（已完成）

**已实现：**
- 审计日志查询（`GET /api/audit/logs/page`）
- 审计统计（`GET /api/audit/logs/summary`）
- 日志类型标签、来源模块标签
- 前端审计追踪页面（统计卡片 + 日志列表 + 详情 Drawer + 导出）

---

### 第十四阶段：基础数据字典（已完成）

**已实现：**
- 部门下拉（`GET /api/master-data/departments`）
- 地点下拉（`GET /api/master-data/locations`）
- 保管人下拉（`GET /api/master-data/keepers`）
- 前端 composable（`useMasterDataOptions.ts`）

---

### 第十五阶段：演示数据增强（已完成）

**已实现：**
- 120 条 DEMO 前缀演示资产数据
- 关联生命周期单据、审批记录、盘点记录、财务同步记录
- 月度折旧趋势数据变化
- 部门/地点/保管人时间分布数据

---

### 第十六阶段：多角色演示账号与前端体验优化（已完成）

**已实现：**
- 3 个新角色（DEPT_LEADER / OFFICE_STAFF / INVENTORY_CLERK）
- 7 个岗位型演示账号（密码统一 123456）
- 前端国企风格 UI 优化（浅灰背景 + 白色卡片 + 深蓝主色）
- 角色驾驶舱、状态标签、筛选区优化、表格体验提升

---

### 第十七阶段：最终文档、验收报告与答辩收口（已完成）

**已实现：**
- README.md 全面更新（功能清单、SQL 执行顺序、启动方式、演示账号、DeepSeek 配置）
- 用户手册（docs/user-manual.md）
- 验收报告（docs/acceptance-report.md）
- 项目总结（docs/final-project-summary.md）
- 答辩演示路径（docs/demo-script.md）
- AI 分析设计文档（docs/ai-analysis-design.md）
- 演示账号说明（docs/demo-accounts.md）
