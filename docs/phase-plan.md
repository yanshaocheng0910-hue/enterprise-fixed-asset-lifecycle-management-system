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

## 第四阶段：盘点管理（计划中）

### 目标

实现固定资产盘点完整流程。

### 实现功能

- 创建盘点任务（选择部门/地点范围）
- 生成盘点清单
- 盘点任务管理
- 盘点结果处理（正常/盘盈/盘亏/位置异常）
- 盘点报告
- 异常资产状态标记 INVENTORY_ABNORMAL

### 当前状态

前端已有盘点任务页面入口和骨架，后端表结构已预留，完整业务功能待开发。

---

## 第五阶段：折旧报表与财务数据查看（计划中）

### 实现功能

- 月度折旧报表
- 按部门统计
- 按分类统计
- Excel 导出（POI 或 EasyExcel）
- 网页端财务数据查看
- 折旧数据展示
- 模拟同步记录

### 当前状态

财务同步记录表已建（migration-v4-finance.sql），前端已有财务同步页面，支持网页端查看折旧数据和模拟同步记录。折旧报表完整业务功能待开发。

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

### 第九阶段：资产生命周期时间线（规划中）

**目标：**
在资产详情页展示资产从入库、领用、调拨、维修、报废、审批到操作日志的完整记录。

**开发优先级：** 1（后续增强第一优先级）

**草案文档：** `docs/sdd/phase-7-asset-timeline-draft.md`

---

### 第十阶段：审批流增强（规划中）

**目标：**
完善我的待办、我的已办、审批详情、审批记录、审批进度展示和驳回重提。

**开发优先级：** 2

**草案文档：** `docs/sdd/phase-6-approval-enhancement-draft.md`

---

### 第十一阶段：预警中心（规划中）

**目标：**
基于系统内部数据生成管理提醒，例如长期闲置提醒、维修次数较多提醒、待审批提醒、低净值资产提醒等。

**开发优先级：** 5

**草案文档：** `docs/sdd/phase-9-warning-center-draft.md`
