# 国企固定资产全生命周期管理系统

## 1. 项目简介

本项目是一个面向国企、事业单位、大型企业的固定资产全生命周期管理平台，覆盖固定资产从入库、领用、调拨、维修、盘点、折旧计提、报废到财务对接的完整业务闭环。

系统采用前后端分离架构，体现企业级、规范化、可审计、可扩展的设计思路。

### 当前阶段

**第二阶段 —— 生命周期单据流**（已完成）

当前项目已经完成第一阶段和第二阶段。

**第一阶段** 完成系统骨架与核心主链路：登录鉴权、首页驾驶舱、资产台账、资产详情、资产分类。

**第二阶段** 完成五个生命周期单据模块：资产入库、资产领用、资产调拨、维修管理（含维修完成）、报废管理。所有状态通过单据自动流转，变化留痕到操作日志。

---

## 2. 技术栈

### 后端

| 技术 | 说明 |
|---|---|
| Spring Boot 3 | 应用框架 |
| Java 17 | 运行环境 |
| MyBatis-Plus | ORM 框架 |
| MySQL 8 | 数据库 |
| JWT | 登录认证 |
| HandlerInterceptor | 接口鉴权 |
| Maven | 构建工具 |

### 前端

| 技术 | 说明 |
|---|---|
| Vue 3 | 前端框架 |
| TypeScript | 静态类型 |
| Vite | 构建工具 |
| Element Plus | 组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP 请求 |
| ECharts (vue-echarts) | 数据可视化 |

### 数据库

- MySQL 8
- 数据库名：`fixed_asset_lifecycle_system`
- 字符集：`utf8mb4`

---

## 3. 项目目录结构

```
fixed-asset-lifecycle-system/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/asset/
│       │   ├── AssetApplication.java              # 启动类
│       │   ├── asset/                             # 资产核心模块
│       │   ├── auth/                              # 认证模块（JWT + Interceptor）
│       │   ├── common/                            # 公共组件（Result, Exception）
│       │   ├── config/                            # 配置（CORS, MyBatis-Plus）
│       │   ├── context/                           # 用户上下文
│       │   ├── dashboard/                         # 首页驾驶舱
│       │   ├── depreciation/                      # 折旧模块（预留）
│       │   ├── finance/                           # 财务模块（骨架）
│       │   ├── inventory/                         # 盘点模块（骨架）
│       │   ├── lifecycle/                         # 生命周期单据模块（已实现）
│       │   │   ├── controller/                    # LifecycleController
│       │   │   ├── service/                       # LifecycleService
│       │   │   ├── mapper/                        # 5 个 Mapper
│       │   │   ├── entity/                        # InboundOrder, ReceiveOrder 等
│       │   │   ├── dto/                           # CreateRequest 系列
│       │   │   └── vo/                            # OrderPageVO 系列
│       │   └── user/                              # 用户模块
│       └── resources/
│           ├── application.yml
│           ├── mapper/asset/AssetMapper.xml
│           └── sql/
│               ├── init.sql                       # 第一阶段初始化
│               └── migration-v2-lifecycle.sql     # 第二阶段迁移
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.ts / App.vue
│       ├── api/
│       │   ├── request.ts, auth.ts, asset.ts
│       │   ├── dashboard.ts
│       │   └── lifecycle.ts                       # 生命周期 API（已实现）
│       ├── router/
│       ├── stores/
│       ├── layouts/
│       ├── views/
│       │   ├── Login.vue / Dashboard.vue
│       │   ├── asset/                             # AssetList, AssetDetail, AssetCategory
│       │   ├── lifecycle/                         # 5 个生命周期页面（完整实现）
│       │   │   ├── Inbound.vue
│       │   │   ├── Receive.vue
│       │   │   ├── Transfer.vue
│       │   │   ├── Repair.vue
│       │   │   └── Scrap.vue
│       │   ├── inventory/                         # 骨架
│       │   ├── depreciation/                      # 骨架
│       │   ├── finance/                           # 骨架
│       │   ├── ai/                                # 骨架
│       │   └── system/                            # 骨架
│       ├── components/
│       │   ├── PageHeader, DataCard, AssetStatusTag
│       │   ├── EmptyState, TableToolbar
│       │   ├── AssetSelect.vue                    # 资产选择器（已实现）
│       │   └── LifecycleDetailDialog.vue           # 单据详情弹窗（已实现）
│       ├── types/, utils/, styles/
│
├── docs/                             # 项目文档
├── scripts/                          # 启动脚本
├── http/                             # 接口测试
├── screenshots/                      # 截图
├── .env.example
├── .gitignore
└── README.md
```

---

## 4. 数据库初始化方式

### SQL 文件位置

- 第一阶段基础表：`backend/src/main/resources/sql/init.sql`
- 第二阶段迁移：`backend/src/main/resources/sql/migration-v2-lifecycle.sql`

### 第一阶段 init.sql

执行后自动完成：

1. 创建数据库 `fixed_asset_lifecycle_system`
2. 创建 9 张基础表：
   - `sys_user`, `sys_role`, `sys_user_role` — 用户与角色
   - `asset_category` — 资产分类
   - `asset` — 固定资产
   - `asset_operation_log` — 资产操作日志
   - `depreciation_record` — 折旧记录（预留）
   - `inventory_task`, `inventory_record` — 盘点（预留）
3. 插入默认数据：1 个管理员、4 个角色、5 个分类、20 条资产、20 条日志

### 第二阶段 migration-v2-lifecycle.sql

执行后新增 5 张生命周期单据表：

| 表名 | 说明 |
|---|---|
| `asset_inbound_order` | 入库单 |
| `asset_receive_order` | 领用单 |
| `asset_transfer_order` | 调拨单 |
| `asset_repair_order` | 维修单 |
| `asset_scrap_order` | 报废单 |

### 执行方式

按顺序执行：

```bash
# 第一步：初始化基础数据
mysql -uroot -p123456 < backend/src/main/resources/sql/init.sql

# 第二步：执行生命周期迁移
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v2-lifecycle.sql
```

如果本机 MySQL 账号密码不是 `root / 123456`，请修改 `backend/src/main/resources/application.yml`。

---

## 5. 默认账号密码

| 字段 | 值 |
|---|---|
| 用户名 | `admin` |
| 密码 | `123456` |
| 真实姓名 | 系统管理员 |
| 所属部门 | 信息中心 |
| 角色 | ADMIN / ASSET_MANAGER / FINANCE / AUDITOR |

---

## 6. 后端启动方式

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8

### 启动命令

```bash
cd backend
mvn -DskipTests package
mvn spring-boot:run
# 或 java -jar target/fixed-asset-backend-0.0.1-SNAPSHOT.jar
```

默认端口：**8080**

---

## 7. 前端启动方式

### 环境要求

- Node.js 18+
- npm 9+

### 启动命令

```bash
cd frontend
npm install
npm run dev          # 开发模式，端口 3000
npm run build        # 生产构建
```

前端通过 Vite proxy 将 `/api` 代理到后端 `http://localhost:8080`。

---

## 8. 第一阶段已完成功能

### 8.1 登录鉴权

**已实现：** 用户名/密码登录、JWT Token 校验、前端自动携带 Token、后端 HandlerInterceptor 鉴权、无 Token/错误 Token 返回 HTTP 401、Axios 拦截器 401 自动跳转登录页、路由守卫。

**接口：** `POST /api/auth/login`、`GET /api/auth/me`

### 8.2 首页驾驶舱

**已实现：** 资产总数、资产总原值、累计折旧、资产净值、使用中/闲置/维修中/待报废数量（真实 asset 表统计）、分类分布图、部门金额排行、月度折旧趋势图。

**接口：** `GET /api/dashboard/stats`、`GET /api/dashboard/category-distribution`、`GET /api/dashboard/department-ranking`、`GET /api/dashboard/depreciation-trend`

### 8.3 资产台账

**已实现：** 分页查询、多条件组合查询、新增资产、编辑资产、逻辑删除资产、资产详情查看、状态标签、金额/日期格式化、新增/编辑弹窗。

**接口：** `GET /api/assets/page`、`GET /api/assets/{id}`、`POST /api/assets`、`PUT /api/assets/{id}`、`DELETE /api/assets/{id}`、`GET /api/assets/status-options`

**关键设计规则：** 资产编号后端自动生成（FA + yyyyMM + 4位流水号）、默认状态 IDLE、折旧净值后端计算、操作日志写入 `asset_operation_log`、逻辑删除 deleted 字段。

### 8.4 资产详情

展示基础信息、价值信息、使用信息、状态信息、标签信息、备注。

**接口：** `GET /api/assets/{id}`

### 8.5 资产分类

分类树 + 分类列表。

**接口：** `GET /api/asset-categories/list`、`GET /api/asset-categories/tree`

---

## 9. 第二阶段已完成功能

### 9.1 资产入库

- 创建入库单（选择资产、入库类型、供应商、采购单号、入库日期、经办人）
- 入库后资产状态设为 IDLE
- 入库分页查询、详情查看

**接口：** `POST /api/lifecycle/inbound`、`GET /api/lifecycle/inbound/{id}`、`GET /api/lifecycle/inbound/page`

### 9.2 资产领用

- 仅允许 IDLE 状态资产领用
- 创建领用单（选择资产、领用人、领用部门、领用日期、使用用途）
- 领用后自动更新：asset.status = IN_USE、asset.department、asset.keeper
- 写入资产操作日志
- 非法状态拦截：非 IDLE 资产不能领用

**接口：** `POST /api/lifecycle/receive`、`GET /api/lifecycle/receive/{id}`、`GET /api/lifecycle/receive/page`

### 9.3 资产调拨

- 允许 IDLE 或 IN_USE 状态资产调拨
- 创建调拨单（选择资产、调入部门/地点/保管人、调拨日期）
- 调拨后更新 asset.department、asset.location、asset.keeper
- 非法状态拦截：SCRAPPED 资产不能调拨

**接口：** `POST /api/lifecycle/transfer`、`GET /api/lifecycle/transfer/{id}`、`GET /api/lifecycle/transfer/page`

### 9.4 维修管理

- 允许 IDLE 或 IN_USE 状态资产发起维修
- 创建维修单（选择资产、故障描述、维修厂商、维修费用、开始日期）
- 创建后 asset.status = REPAIRING，维修单 status = DRAFT

**接口：** `POST /api/lifecycle/repair`、`GET /api/lifecycle/repair/{id}`、`GET /api/lifecycle/repair/page`

### 9.5 维修完成

- 对 DRAFT 维修单执行完成操作
- `repairResult = REPAIRED` → asset.status = IN_USE
- `repairResult = SCRAP_SUGGESTED` → asset.status = WAITING_SCRAP
- 维修单状态变更为 COMPLETED
- 非法拦截：非 DRAFT 维修单不能重复完成

**接口：** `PUT /api/lifecycle/repair/{id}/complete`

### 9.6 报废管理

- 允许 IDLE、IN_USE、REPAIRING、WAITING_SCRAP 状态资产报废
- 创建报废单（选择资产、报废原因、报废日期、处置方式、残值）
- 报废后 asset.status = SCRAPPED
- 非法拦截：SCRAPPED 资产不能再次报废

**接口：** `POST /api/lifecycle/scrap`、`GET /api/lifecycle/scrap/{id}`、`GET /api/lifecycle/scrap/page`

### 9.7 资产选择器

- 通用资产选择下拉组件（AssetSelect.vue）
- 支持按状态过滤（如仅显示 IDLE 资产用于领用）
- 显示资产编号、资产名称、部门、状态标签
- 选中后展示当前资产详情摘要

**接口：** `GET /api/lifecycle/asset-select-options?status=`

### 9.8 单据详情弹窗

- 通用生命周期单据详情展示组件（LifecycleDetailDialog.vue）
- el-descriptions 布局，条件性显示不同单据的特有字段

### 9.9 生命周期接口总览

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/lifecycle/asset-select-options?status=` | 资产选择列表 |
| `GET` | `/api/lifecycle/inbound/page` | 入库分页 |
| `GET` | `/api/lifecycle/inbound/{id}` | 入库详情 |
| `POST` | `/api/lifecycle/inbound` | 创建入库单 |
| `GET` | `/api/lifecycle/receive/page` | 领用分页 |
| `GET` | `/api/lifecycle/receive/{id}` | 领用详情 |
| `POST` | `/api/lifecycle/receive` | 创建领用单 |
| `GET` | `/api/lifecycle/transfer/page` | 调拨分页 |
| `GET` | `/api/lifecycle/transfer/{id}` | 调拨详情 |
| `POST` | `/api/lifecycle/transfer` | 创建调拨单 |
| `GET` | `/api/lifecycle/repair/page` | 维修分页 |
| `GET` | `/api/lifecycle/repair/{id}` | 维修详情 |
| `POST` | `/api/lifecycle/repair` | 创建维修单 |
| `PUT` | `/api/lifecycle/repair/{id}/complete` | 完成维修 |
| `GET` | `/api/lifecycle/scrap/page` | 报废分页 |
| `GET` | `/api/lifecycle/scrap/{id}` | 报废详情 |
| `POST` | `/api/lifecycle/scrap` | 创建报废单 |

### 9.10 单据编号规则

生命周期单据编号统一采用：**前缀 + yyyyMMdd + 4位流水号**

| 单据类型 | 前缀 | 示例 |
|---|---|---|
| 入库单 | IN | IN202607050001 |
| 领用单 | RE | RE202607050001 |
| 调拨单 | TF | TF202607050001 |
| 维修单 | RP | RP202607050001 |
| 报废单 | SC | SC202607050001 |

规则：前缀 + 当天日期 + 4位流水号，跨5张表查询最大编号保证唯一性，每天流水号从 0001 开始。

### 9.11 状态流转规则

| 流程 | 操作 | 资产状态变化 |
|---|---|---|
| 入库 | 创建入库单 | 任意 → IDLE |
| 领用 | 创建领用单 | IDLE → IN_USE |
| 调拨 | 创建调拨单 | IDLE / IN_USE → IN_USE |
| 创建维修 | 创建维修单 | IDLE / IN_USE → REPAIRING |
| 维修完成 | repairResult = REPAIRED | REPAIRING → IN_USE |
| 维修完成 | repairResult = SCRAP_SUGGESTED | REPAIRING → WAITING_SCRAP |
| 报废 | 创建报废单 | IDLE/IN_USE/REPAIRING/WAITING_SCRAP → SCRAPPED |

所有状态变化自动写入 `asset_operation_log`。

### 9.12 非法状态拦截

| 场景 | 拦截结果 |
|---|---|
| 非 IDLE 资产领用 | 400：仅闲置资产可领用 |
| SCRAPPED 资产继续操作 | 400：当前状态不允许 |
| 非 DRAFT 维修单完成 | 400：维修单状态不是草稿 |
| 已报废资产入库（参数完整） | 400：已报废资产不能入库 |

---

## 10. 骨架页面

以下页面当前为骨架页面——可打开、不白屏、有标题和模块说明，但未实现完整业务。后续由其他人继续开发。

| 路由 | 页面 | 当前状态 |
|---|---|---|
| `/inventory/tasks` | 盘点任务 | 骨架，提示后续阶段接入 |
| `/depreciation/report` | 折旧报表 | 骨架 + 空表格 |
| `/finance/sync` | 财务同步 | 骨架，模拟状态 |
| `/ai/analysis` | AI 智能分析 | 骨架，3 个能力入口 |
| `/system/users` | 用户管理 | 骨架，提示后续阶段接入 |

---

## 11. 验收结果

### 第一阶段验收

| 验收项 | 结果 |
|---|---|
| 后端启动正常，8080 端口可访问 | ✅ |
| 前端启动正常，3000 端口可访问 | ✅ |
| 数据库初始化成功 | ✅ |
| admin / 123456 登录成功 | ✅ |
| 首页驾驶舱真实数据 | ✅ |
| 资产分页/新增/编辑/删除/详情 | ✅ |
| 资产分类列表/树 | ✅ |
| 路由鉴权 / 401 拦截 | ✅ |
| 浏览器无白屏、控制台无报错 | ✅ |

### 第二阶段验收

| 验收项 | 结果 |
|---|---|
| 干净数据库复测 | ✅ 40/40 通过 |
| migration-v2-lifecycle.sql 执行成功 | ✅ 5 张表 |
| 后端 mvn -DskipTests package | ✅ BUILD SUCCESS |
| 前端 npm run build | ✅ 通过（2275 模块） |
| 入库流程完整验证 | ✅ 创建/分页/详情 |
| 领用流程完整验证 | ✅ 状态/department/keeper 更新 |
| 调拨流程完整验证 | ✅ department/location/keeper 更新 |
| 维修创建流程验证 | ✅ 资产 REPAIRING + 单据 DRAFT |
| 维修完成（REPAIRED）验证 | ✅ 资产 IN_USE + 单据 COMPLETED |
| 维修完成（SCRAP_SUGGESTED）验证 | ✅ 资产 WAITING_SCRAP |
| 报废流程完整验证 | ✅ 资产 SCRAPPED |
| 非法状态拦截测试 | ✅ 7 项全部 400 |
| 操作日志写入验证 | ✅ 各流程均留痕 |
| 第一阶段回归测试 | ✅ 全部通过 |

---

## 12. SDD 开发规范

本项目后续开发遵循 **SDD（Spec-Driven Development，规格驱动开发）** 方式。

### 开发流程

```
Spec → Design → Tasks → Implementation → Acceptance → Review → Commit
```

### 关键规则

1. **每个新阶段**必须在 `docs/sdd/` 下先编写需求规格文档。
2. **每次开发前**必须明确「做什么」和「不做什么」。
3. 没有 Design 文档，不允许建表和写接口代码。
4. **每个阶段**都必须有验收报告。
5. 未通过 `mvn -DskipTests package` 和 `npm run build` 不能提交代码。
6. **第一阶段和第二阶段主链路不允许随意重构**。

### 相关文档

- 项目范围：`docs/sdd/00-project-scope.md`
- 领域模型：`docs/sdd/01-domain-model.md`
- 开发流程：`docs/sdd/02-development-process.md`
- 模板：`docs/sdd/03-spec-template.md` / `04-design-template.md` / `05-task-template.md` / `06-acceptance-template.md`

---

## 13. 当前未实现内容

以下功能尚未实现，等待后续开发：

**审批流：**

- 生命周期单据审批流程

**盘点管理：**

- 创建盘点任务
- 二维码/扫码盘点
- 盘盈/盘亏/位置异常处理
- 盘点报告

**报表与导出：**

- 折旧报表完整业务
- Excel 导出

**用户与权限：**

- 用户管理（新增、编辑、角色分配）
- 按钮级 RBAC 权限控制
- 部门数据权限

**外部系统：**

- 财务系统真实对接（用友/金蝶/SAP）
- AI 资产照片分类
- AI 故障风险预测
- AI 盘点路线推荐
- RFID 真实硬件接入
- 二维码打印与标签

**系统功能：**

- 消息通知
- 系统日志查询
- Docker / Nginx 部署

---

## 14. 后续开发路线

| 阶段 | 状态 | 内容 |
|---|---|---|
| 第一阶段 | ✅ 已完成 | 项目骨架、登录、驾驶舱、资产台账、资产详情、资产分类 |
| 第二阶段 | ✅ 已完成 | 入库、领用、调拨、维修（含完成）、报废五个生命周期模块 |
| 第三阶段 | 📋 计划中 | 审批流 |
| 第四阶段 | 📋 计划中 | 盘点管理（盘点任务、扫码盘点、盘点报告） |
| 第五阶段 | 📋 计划中 | 折旧报表与财务对接（月度报表、Excel导出、财务同步） |
| 第六阶段 | 📋 计划中 | 用户管理与权限（用户CRUD、角色分配、菜单/按钮权限） |
| 第七阶段 | 📋 计划中 | AI / RFID / 二维码（照片分类、故障预测、盘点路线、RFID） |
| 第八阶段 | 📋 计划中 | 部署与验收材料（Docker、Nginx、使用说明、答辩文档） |

---

## 15. 推荐团队分工

| 模块 | 建议负责 |
|---|---|
| 资产生命周期模块 | 入库、领用、调拨、维修、报废的业务单据设计和前端实现 |
| 盘点模块 | 二维码生成、扫码盘点、盘点任务、盘点报告 |
| 报表模块 | 折旧报表、Excel 导出、统计图表优化 |
| 权限模块 | 用户管理、角色分配、菜单权限、按钮权限 |
| AI / RFID / 财务模块 | 智能分类、故障预测、盘点路线、外部系统对接 |
| 测试与文档 | 接口测试、浏览器验收、使用说明、答辩文档 |

---

## 16. 开发注意事项

1. **不要重构已完成的主链路。** 登录、资产台账、驾驶舱、资产详情、资产分类、生命周期单据流已稳定，新功能应在现有结构上增量开发。

2. **资产状态变化必须写入 `asset_operation_log`。** 这是审计和追溯的基础，任何状态变更都要记录操作人、变更时间和变更内容。

3. **新增业务要先设计表结构和接口，不要直接硬改 `asset` 表。** 生命周期单据使用独立的业务单据表，通过 `asset_id` 关联资产。

4. **前端保持企业 Web 后台风格。** 左侧深蓝导航、顶部白色用户栏、主体灰白背景。不要做成 App 风格或炫酷大屏。

5. **后端接口保持统一 `Result<T>` 返回格式。** 所有接口的响应体结构一致，便于前端统一处理。

6. **需要真实 HTTP 验收，不要只看 build 通过。** 每次开发完成后，应同时启动前后端，使用浏览器验证功能是否正常。

7. **提交规范。** 不要提交 `node_modules/`、`target/`、本地 `.env`、IDE 缓存文件。

---

## 17. 整个项目最终形态

项目完成后应该达到以下标准：

- ✅ 登录权限完整，多种角色可访问
- ✅ 资产台账完整，支持增删改查和高效筛选
- ✅ 生命周期流程完整，入库到报废全链路可追踪
- ✅ 盘点管理完整，支持扫码和异常处理
- ✅ 折旧报表完整，支持多种维度和导出
- ✅ 财务对接可用，能导出财务所需数据
- ✅ AI 智能分析可展示，辅助资产决策
- ✅ RFID / 二维码标签可扩展，支持实物盘点
- ✅ 操作日志可审计，全链路留痕
- ✅ 数据可导出，支持 Excel 报表
- ✅ 页面风格统一，企业 Web 后台风格一致
- ✅ 系统可部署演示，前后端分离独立运行

---

*当前项目已完成第二阶段，可在此基础上继续开发审批流、盘点管理、折旧报表、权限管理、AI/RFID/财务对接等企业级能力。*
*更多信息请查看 `docs/` 目录下的设计文档。*
