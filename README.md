# 国企固定资产全生命周期管理系统

## 1. 项目定位

本项目是一个**基于浏览器访问的固定资产网页管理系统**，包含资产管理、生命周期单据、审批流、用户与权限管理、数据展示和 AI 辅助分析等网页端能力。

系统采用前后端分离架构，通过浏览器即可完成固定资产从入库、领用、调拨、维修、报废到财务数据查看的完整业务闭环管理。

**AI 辅助分析模块**基于系统内部数据进行分析，数据来源包括资产台账、生命周期单据、维修记录、报废记录、审批记录和操作日志。AI 可用于资产状态摘要、异常资产提示、维修/报废辅助建议和管理报告辅助生成。AI 输出只作为辅助参考，不直接修改业务数据。

**财务模块**当前定位为网页端财务数据查看、折旧数据展示和模拟记录。

---

## 2. 当前已实现功能

以下功能在当前代码中已实现，可通过浏览器访问验证。

### 核心模块

| 模块 | 说明 |
|---|---|
| 登录鉴权 | 用户名/密码登录、JWT Token 校验、路由守卫、401 自动跳转 |
| 首页驾驶舱 | 资产总数、原值、净值、状态统计、分类分布图、部门排行、折旧趋势 |
| 资产台账 | 分页查询、多条件筛选、新增、编辑、逻辑删除、详情查看 |
| 资产分类 | 分类树 + 分类列表 |
| 操作日志 | 所有资产状态变化自动写入 `asset_operation_log` |

### 生命周期单据

| 模块 | 说明 |
|---|---|
| 资产入库 | 创建入库单，入库后资产状态设为 IDLE |
| 资产领用 | 仅 IDLE 资产可领用，领用后状态变为 IN_USE |
| 资产调拨 | 跨部门调拨，更新部门、地点、保管人 |
| 资产维修 | 创建维修单（DRAFT），维修完成后更新资产状态 |
| 资产报废 | 创建报废单，报废后资产状态变为 SCRAPPED |

### 审批流

| 功能 | 说明 |
|---|---|
| 审批模板 | 定义审批流程模板和节点 |
| 审批实例 | 基于模板创建审批实例 |
| 审批操作 | 提交、审批通过、审批驳回 |
| 审批记录 | 记录每次审批操作，可追溯 |

### 用户与权限管理（RBAC）

| 功能 | 说明 |
|---|---|
| 用户管理 | 用户 CRUD、状态切换、密码重置、角色分配 |
| 角色管理 | 角色 CRUD、角色权限分配 |
| 权限管理 | 34 项权限、`@RequirePermission` 注解、前端 `v-permission` 指令 |
| 菜单控制 | 前端菜单根据权限动态显示 |

### 财务与 AI 模块

| 模块 | 说明 |
|---|---|
| 财务数据模拟同步 | 网页端查看折旧数据、模拟同步记录、同步记录列表 |
| AI 辅助分析 | 页面入口，展示资产状态摘要、异常资产提示、维修/报废辅助建议、管理报告辅助生成 |

### 骨架页面

以下页面已有入口和基础布局，完整业务功能待后续开发：

| 页面 | 当前状态 |
|---|---|
| 盘点任务 | 骨架页面，有入口和说明 |
| 折旧报表 | 页面入口，待完整实现 |

---

## 3. 技术栈

### 后端

| 技术 | 说明 |
|---|---|
| Spring Boot 3 | 应用框架 |
| Java 17 | 运行环境 |
| MyBatis-Plus | ORM 框架 |
| MySQL 8 | 数据库 |
| JWT | 登录认证 |
| Spring AOP | 权限拦截（`@RequirePermission`） |
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

---

## 4. 目录结构

```
fixed-asset-lifecycle-system/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/asset/
│       │   ├── AssetApplication.java          # 启动类
│       │   ├── auth/                          # 登录鉴权（JWT + Interceptor）
│       │   ├── asset/                         # 资产核心（台账、分类、操作日志）
│       │   ├── dashboard/                     # 首页驾驶舱
│       │   ├── lifecycle/                     # 生命周期单据（入库/领用/调拨/维修/报废）
│       │   ├── approval/                      # 审批流
│       │   ├── user/                          # 用户、角色、权限管理
│       │   ├── permission/                    # RBAC 权限注解与切面
│       │   ├── finance/                       # 财务数据模拟同步
│       │   ├── inventory/                     # 盘点（骨架）
│       │   ├── depreciation/                  # 折旧（预留）
│       │   ├── common/                        # 公共组件（Result, Exception）
│       │   ├── config/                        # 配置（CORS, MyBatis-Plus）
│       │   └── context/                       # 用户上下文
│       └── resources/
│           ├── application.yml
│           ├── mapper/                        # MyBatis XML
│           └── sql/                           # 数据库脚本
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── api/                               # 接口请求层
│       ├── router/                            # 路由配置
│       ├── stores/                            # Pinia 状态管理
│       ├── layouts/                           # 布局组件
│       ├── views/                             # 页面
│       │   ├── Dashboard.vue / Login.vue
│       │   ├── asset/                         # 资产台账、详情、分类
│       │   ├── lifecycle/                     # 5 个生命周期页面
│       │   ├── system/                        # 用户管理、角色管理
│       │   ├── finance/                       # 财务数据模拟同步
│       │   ├── ai/                            # AI 辅助分析
│       │   ├── inventory/                     # 盘点任务（骨架）
│       │   └── depreciation/                  # 折旧报表
│       ├── components/                        # 通用组件
│       └── types/ utils/ styles/
│
├── docs/                                      # 项目文档
│   ├── sdd/                                   # 规格驱动开发文档
│   └── specs/                                 # 设计规格
└── README.md
```

---

## 5. 数据库初始化

### 数据库信息

- MySQL 8
- 数据库名：`fixed_asset_lifecycle_system`
- 字符集：`utf8mb4`

### SQL 文件

按以下顺序执行：

| 顺序 | 文件 | 作用 |
|---|---|---|
| 1 | `backend/src/main/resources/sql/init.sql` | 创建数据库、基础表（用户、角色、资产分类、资产、操作日志、折旧记录、盘点表）、默认数据 |
| 2 | `backend/src/main/resources/sql/migration-v2-lifecycle.sql` | 生命周期单据表（入库、领用、调拨、维修、报废） |
| 3 | `backend/src/main/resources/sql/migration-v3-approval.sql` | 审批流表（审批模板、审批实例、审批节点、审批记录） |
| 4 | `backend/src/main/resources/sql/migration-v4-finance.sql` | 财务同步记录表 |
| 5 | `backend/src/main/resources/sql/migration-v5-rbac.sql` | RBAC 权限表（权限、角色权限关联、权限种子数据） |

### 执行方式

```bash
mysql -uroot -p123456 < backend/src/main/resources/sql/init.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v2-lifecycle.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v3-approval.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v4-finance.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v5-rbac.sql
```

如果本机 MySQL 账号密码不是 `root / 123456`，请修改 `backend/src/main/resources/application.yml`。

---

## 6. 启动方式

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8
- Node.js 18+
- npm 9+

### 启动步骤

**1. 启动 MySQL 服务**

确保 MySQL 8 已启动，并按上述顺序执行全部 SQL 文件。

**2. 启动后端**

```bash
cd backend
mvn -DskipTests package
mvn spring-boot:run
# 或 java -jar target/fixed-asset-backend-0.0.1-SNAPSHOT.jar
```

后端默认端口：**8081**

**3. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：**3000**

前端通过 Vite proxy 将 `/api` 代理到后端 `http://localhost:8081`。

**4. 访问系统**

打开浏览器访问：`http://localhost:3000`

### 默认账号

| 字段 | 值 |
|---|---|
| 用户名 | `admin` |
| 密码 | `123456` |

### 常见问题

| 问题 | 解决方案 |
|---|---|
| 后端启动报数据库连接失败 | 检查 MySQL 服务是否启动、`application.yml` 中密码是否正确 |
| 前端登录提示 401 | 确认后端已启动，检查 Vite proxy 端口与后端端口一致（8081） |
| 页面提示无权限 | 确认用户已分配角色和权限，检查 `migration-v5-rbac.sql` 是否已执行 |
| Maven 构建失败 | 确认 JDK 17+ 和 Maven 3.6+ 已正确安装 |

---

## 7. 开发规范

### 开发流程

新功能开发遵循 SDD（Spec-Driven Development）方式：

```
Spec → Design → Tasks → Implementation → Acceptance → Review → Commit
```

相关模板见 `docs/sdd/` 目录。

### 代码规范

1. **新功能先写 SDD。** 在 `docs/sdd/` 下先编写需求规格文档，明确做什么和不做什么。
2. **不要随意重构已稳定主链路。** 登录、资产台账、生命周期单据、审批流、RBAC 权限模块已稳定，新功能应在现有结构上增量开发。
3. **数据库变更必须有 migration SQL。** 新增表或字段时，在 `backend/src/main/resources/sql/` 下新增迁移脚本。
4. **后端接口保持统一 `Result<T>` 返回格式。** 所有接口的响应体结构一致，便于前端统一处理。
5. **前端接口统一通过 api 层。** 所有 HTTP 请求通过 `frontend/src/api/` 下的模块发送，不要在组件中直接调用 Axios。
6. **资产状态变化必须写入操作日志。** 任何资产状态变更都要记录到 `asset_operation_log`。

### 提交前检查

```bash
# 后端构建
cd backend && mvn -DskipTests package

# 前端构建
cd frontend && npm run build

# 代码差异检查
git diff --check
```

未通过 `mvn -DskipTests package`、`npm run build` 和 `git diff --check` 不能提交代码。

### 提交规范

不要提交 `node_modules/`、`target/`、本地 `.env`、IDE 缓存文件。

---

## 8. 关键设计规则

### 资产编号规则

格式：`FA` + `yyyyMM` + `4位流水号`（如 FA2026070001），由后端自动生成。

### 生命周期单据编号规则

格式：`前缀` + `yyyyMMdd` + `4位流水号`

| 单据类型 | 前缀 | 示例 |
|---|---|---|
| 入库单 | IN | IN202607050001 |
| 领用单 | RE | RE202607050001 |
| 调拨单 | TF | TF202607050001 |
| 维修单 | RP | RP202607050001 |
| 报废单 | SC | SC202607050001 |

### 资产状态流转

| 操作 | 状态变化 |
|---|---|
| 入库 | → IDLE |
| 领用 | IDLE → IN_USE |
| 调拨 | IDLE / IN_USE → IN_USE |
| 创建维修 | IDLE / IN_USE → REPAIRING |
| 维修完成（修复） | REPAIRING → IN_USE |
| 维修完成（建议报废） | REPAIRING → WAITING_SCRAP |
| 报废 | → SCRAPPED |

### 折旧计算

方法：平均年限法

- 年折旧额 = 原值 × (1 - 残值率) / 使用年限
- 月折旧额 = 年折旧额 / 12
- 累计折旧 = 已使用月份 × 月折旧额
- 资产净值 = 原值 - 累计折旧

### 逻辑删除

`asset` 表使用 `deleted` 字段标记，MyBatis-Plus 全局配置自动过滤已删除数据。

---

## 9. 后续路线图

README 只放简表，详细规划见 `docs/roadmap.md`。

| 阶段 | 模块 | 状态 | 说明 |
|---|---|---|---|
| 已完成 | 登录、资产台账、生命周期单据 | 已完成 | 当前主流程可运行 |
| 已完成 | 审批流、用户与权限 | 已完成 | 审批模板/实例/记录、RBAC 34 项权限 |
| 已完成 | 财务数据查看、折旧报表 | 已完成 | 网页端财务数据查看与模拟同步记录；月度折旧报表、部门/分类统计、折旧趋势 |
| 已完成 | 资产生命周期时间线 | 已完成 | 资产详情页聚合展示入库、领用、调拨、维修、报废、审批、操作日志事件 |
| 已完成 | AI 辅助分析 | 已完成 | 资产状态摘要、异常资产提示、维修/报废辅助建议、管理报告辅助生成 |
| 后续阶段 | 盘点任务管理 | 规划中 | 网页端任务、明细、结果和报告 |
| 后续阶段 | 审批流增强 | 规划中 | 待办、已办、进度、驳回重提 |
| 后续阶段 | 预警中心 | 规划中 | 基于规则的管理提醒 |
