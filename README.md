# 国企固定资产全生命周期管理系统

## 1. 项目定位

本项目是一个**基于浏览器访问的固定资产网页管理系统**，覆盖固定资产从入库、领用、调拨、维修、报废到财务折旧、盘点、预警、AI 辅助分析的完整业务闭环。

系统采用前后端分离架构（Spring Boot + Vue 3），通过浏览器即可完成固定资产全生命周期管理，适用于国企、事业单位的固定资产管理场景。

**AI 辅助分析模块**接入 DeepSeek 大模型，基于系统内部 16 类数据生成自然语言分析报告，同时保留规则引擎兜底，确保在网络异常或 API 不可用时仍能输出结构化分析。AI 输出仅作为辅助参考，不直接修改业务数据。

---

## 2. 完整功能清单

以下功能均已实现，可通过浏览器访问验证。

### 核心模块

| 模块 | 说明 |
|---|---|
| 登录鉴权 | 用户名/密码登录、JWT Token 校验、路由守卫、401 自动跳转、`v-permission` 指令 |
| 首页驾驶舱 | 角色差异化工作台：核心统计卡片、状态分布、分类分布图、部门排行、折旧趋势、角色专属区块；状态卡片可点击跳转到资产台账筛选 |
| 资产台账 | 分页查询、常用+高级筛选折叠、新增/编辑/逻辑删除、详情 Drawer、金额千分位右对齐、状态标签、二维码生成 |
| 资产分类 | 分类树 + 分类列表，CRUD |
| 操作日志 | 所有资产状态变化自动写入 `asset_operation_log` |

### 生命周期单据

| 模块 | 说明 |
|---|---|
| 资产入库 | 创建入库单，入库后资产状态设为 IDLE |
| 资产领用 | 仅 IDLE 资产可领用，领用后状态变为 IN_USE |
| 资产调拨 | 跨部门调拨，更新部门、地点、保管人 |
| 资产维修 | 创建维修单，维修完成后更新资产状态 |
| 资产报废 | 创建报废单，报废后资产状态变为 SCRAPPED |

### 审批与时间线

| 模块 | 说明 |
|---|---|
| 审批中心 | 待办/已办列表、审批模板/实例/节点/记录、提交/通过/驳回 |
| 资产时间线 | 资产详情页聚合展示入库、领用、调拨、维修、报废、审批、操作日志事件 |

### 财务与折旧

| 模块 | 说明 |
|---|---|
| 折旧报表 | 核心指标卡片（原值/净值/累计折旧/月折旧）、月度折旧趋势图、部门/分类统计、低净值预警 |
| 财务同步 | 财务数据模拟同步、同步记录列表、同步状态大标签、金额右对齐 |

### 盘点与预警

| 模块 | 说明 |
|---|---|
| 盘点管理 | 盘点任务/明细/异常、状态标签+进度条、异常数量高亮、正常/异常/丢失/地点不符/保管人不符颜色区分 |
| 预警中心 | 风险等级统计卡片（总数/高/中/低）、高风险突出、预警类型标签、预警原因+处理建议 |

### AI 辅助分析

| 模块 | 说明 |
|---|---|
| AI 智能分析报告 | DeepSeek 大模型生成 + 规则引擎 fallback，三层架构（数据准备→DeepSeek→规则兜底），7 分区报告（摘要/风险/财务/运营/审计/建议/结论） |
| 辅助明细数据 | 资产状态摘要、异常资产提示、维修/报废辅助建议（折叠面板） |

### 数据导出与审计

| 模块 | 说明 |
|---|---|
| Excel 导出 | 10 个导出接口，Apache POI 生成，前端 blob 下载 |
| 审计日志 | 统计卡片（今日操作/资产变更/审批/盘点异常/财务同步）、日志/业务/来源三级标签、多维筛选、Drawer 详情、导出 |

### 基础数据与权限

| 模块 | 说明 |
|---|---|
| 基础数据字典 | 部门/地点/保管人下拉，`/api/master-data/*` |
| 用户管理 | 用户 CRUD、状态切换、密码重置、角色分配 |
| 角色管理 | 角色 CRUD、角色权限分配 |
| 权限管理 | RBAC 34 项权限、`@RequirePermission` 注解、前端 `v-permission` 指令、菜单动态显示 |
| 多角色演示账号 | 7 个岗位型演示账号，角色差异化菜单和首页 |

---

## 3. 技术栈

### 后端

| 技术 | 说明 |
|---|---|
| Spring Boot 3.3.2 | 应用框架 |
| Java 17 | 运行环境（JDK 17+） |
| MyBatis-Plus 3.5.7 | ORM 框架 |
| MySQL 8 | 数据库 |
| JWT | 登录认证 |
| Spring AOP | 权限拦截（`@RequirePermission`） |
| RestClient | DeepSeek API 调用（Spring 6.1 内置） |
| Apache POI | Excel 导出 |
| Maven | 构建工具 |

### 前端

| 技术 | 说明 |
|---|---|
| Vue 3 | 前端框架 |
| TypeScript | 静态类型 |
| Vite 5 | 构建工具 |
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
│       │   ├── inventory/                     # 盘点管理
│       │   ├── depreciation/                  # 折旧报表
│       │   ├── warning/                       # 预警中心
│       │   ├── ai/                            # AI 辅助分析（DeepSeek + 规则兜底）
│       │   ├── audit/                         # 审计日志
│       │   ├── masterdata/                    # 基础数据字典
│       │   ├── export/                        # Excel 导出
│       │   ├── common/                        # 公共组件（Result, Excel 工具）
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
│       ├── components/                        # 通用组件（DataCard/PageHeader/StatusTag 等）
│       ├── composables/                       # 组合式函数
│       └── styles/                            # 全局样式（variables/global/layout）
│
├── docs/                                      # 项目文档
│   ├── demo-accounts.md                       # 演示账号说明
│   ├── user-manual.md                         # 用户手册
│   ├── acceptance-report.md                   # 验收报告
│   ├── final-project-summary.md               # 项目总结
│   ├── demo-script.md                         # 答辩演示路径
│   ├── ai-analysis-design.md                  # AI 模块设计说明
│   └── sdd/                                   # SDD 规格驱动开发文档
└── README.md
```

---

## 5. 数据库初始化

### 数据库信息

- MySQL 8
- 数据库名：`fixed_asset_lifecycle_system`
- 字符集：`utf8mb4`

### SQL 文件执行顺序

按以下顺序依次执行，缺一不可：

| 顺序 | 文件 | 作用 |
|---|---|---|
| 1 | `backend/src/main/resources/sql/init.sql` | 创建数据库、基础表（用户、角色、资产分类、资产、操作日志、折旧记录、盘点表）、默认 admin 账号 |
| 2 | `backend/src/main/resources/sql/migration-v2-lifecycle.sql` | 生命周期单据表（入库、领用、调拨、维修、报废） |
| 3 | `backend/src/main/resources/sql/migration-v3-approval.sql` | 审批流表（审批模板、审批实例、审批节点、审批记录） |
| 4 | `backend/src/main/resources/sql/migration-v4-finance.sql` | 财务同步记录表 |
| 5 | `backend/src/main/resources/sql/migration-v5-rbac.sql` | RBAC 权限表（权限、角色权限关联、34 项权限种子数据） |
| 6 | `backend/src/main/resources/sql/migration-v6-finance-enhance.sql` | 财务模块增强 |
| 7 | `backend/src/main/resources/sql/migration-v13-demo-data.sql` | 演示数据（120 条资产 + 关联单据、维修、盘点数据） |
| 8 | `backend/src/main/resources/sql/migration-v15-master-data-demo-time.sql` | 基础数据（部门/地点/保管人）+ 时间分布优化 |
| 9 | `backend/src/main/resources/sql/migration-v15-1-depreciation-trend-variation.sql` | 折旧月度趋势变化数据 |
| 10 | `backend/src/main/resources/sql/migration-v16-demo-roles.sql` | 多角色演示账号（7 个岗位型账号 + 3 个新角色） |

### 执行方式

```bash
# 进入 MySQL 后依次执行，或用命令行导入
mysql -uroot -p123456 < backend/src/main/resources/sql/init.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v2-lifecycle.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v3-approval.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v4-finance.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v5-rbac.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v6-finance-enhance.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v13-demo-data.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v15-master-data-demo-time.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v15-1-depreciation-trend-variation.sql
mysql -uroot -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v16-demo-roles.sql
```

如果本机 MySQL 账号密码不是 `root / 123456`，请修改 `backend/src/main/resources/application.yml`。

所有 migration SQL 均为幂等脚本，可重复执行。

---

## 6. 启动方式

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8
- Node.js 18+
- npm 9+

### 启动步骤

**1. 启动 MySQL 服务并执行 SQL**

确保 MySQL 8 已启动，并按上述顺序执行全部 10 个 SQL 文件。

**2. 配置 DeepSeek API Key（可选，不配置则使用规则引擎）**

```bash
# Windows PowerShell（用户级环境变量，永久生效）
[Environment]::SetEnvironmentVariable("DEEPSEEK_API_KEY", "你的API Key", "User")

# Linux / macOS
export DEEPSEEK_API_KEY="你的API Key"
```

配置后重启终端或重新启动后端生效。未配置时 AI 分析自动切换为规则引擎模式（`analysisMode=RULE_FALLBACK`）。

**3. 启动后端**

```bash
cd backend
mvn -DskipTests package
java -jar target/fixed-asset-backend-0.0.1-SNAPSHOT.jar
# 或 mvn spring-boot:run
```

后端默认端口：**8081**

**4. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：**3000**（开发环境通过 Vite proxy 将 `/api` 代理到后端 `http://localhost:8081`）

**5. 访问系统**

打开浏览器访问：`http://localhost:3000`

---

## 7. 默认账号与演示账号

所有账号密码统一为 `123456`。

### 默认开发账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| admin | 123456 | 系统默认开发账号，拥有全部权限，仅用于开发调试 |

### 岗位型演示账号

| 用户名 | 姓名 | 部门 | 角色 | 推荐演示场景 |
|---|---|---|---|---|
| system.manager | 系统管理员 | 信息中心 | ADMIN | 全系统功能演示 |
| asset.manager | 张伟 | 资产管理部 | ASSET_MANAGER | 资产台账、生命周期、盘点 |
| dept.leader | 李娜 | 综合办公室 | DEPT_LEADER | 审批待办/已办 |
| finance.officer | 陈敏 | 财务部 | FINANCE | 折旧报表、财务同步 |
| audit.officer | 王强 | 审计部 | AUDITOR | 审计日志、预警中心 |
| office.staff | 刘洋 | 综合办公室 | OFFICE_STAFF | 资产领用、维修申请 |
| inventory.clerk | 赵磊 | 资产管理部 | INVENTORY_CLERK | 盘点任务执行 |

详细账号说明见 [docs/demo-accounts.md](docs/demo-accounts.md)。

---

## 8. DeepSeek AI 配置说明

AI 辅助分析模块支持 DeepSeek 大模型，配置方式如下：

### 环境变量

| 变量名 | 默认值 | 说明 |
|---|---|---|
| `DEEPSEEK_API_KEY` | （空） | DeepSeek API Key，**必须配置才能启用大模型分析** |
| `DEEPSEEK_API_BASE_URL` | `https://api.deepseek.com` | API 基础地址 |
| `DEEPSEEK_MODEL` | `deepseek-v4-flash` | 模型名称 |
| `AI_ENABLED` | `true` | 是否启用 AI 分析 |

### 安全说明

- API Key **仅从环境变量读取**，不硬编码到代码
- `application.yml` 使用 `${DEEPSEEK_API_KEY:}` 占位符，默认空值
- API Key **不提交到 git 仓库**（`.gitignore` 已排除 `.env` 文件）
- 未配置 API Key 时，系统自动切换为规则引擎分析模式

### 工作模式

| 模式 | 标识 | 说明 |
|---|---|---|
| DeepSeek 大模型 | `analysisMode=DEEPSEEK` | 调用 DeepSeek API 生成自然语言分析报告 |
| 规则引擎兜底 | `analysisMode=RULE_FALLBACK` | API 不可用时自动切换，输出结构化分析报告 |

详细设计说明见 [docs/ai-analysis-design.md](docs/ai-analysis-design.md)。

---

## 9. Excel 导出说明

系统提供 10 个 Excel 导出接口，使用 Apache POI 生成，前端通过 blob 下载：

| 导出内容 | 接口 |
|---|---|
| 资产台账 | `GET /api/export/assets` |
| 资产时间线 | `GET /api/export/assets/{assetId}/timeline` |
| 审批记录 | `GET /api/export/approval/records` |
| 盘点任务 | `GET /api/export/inventory/tasks` |
| 盘点明细 | `GET /api/export/inventory/tasks/{taskId}/records` |
| 折旧报表 | `GET /api/export/depreciation/report` |
| 财务同步记录 | `GET /api/export/finance/sync/records` |
| 预警列表 | `GET /api/export/warnings` |
| AI 分析报告 | `GET /api/export/ai/report` |
| 审计日志 | `GET /api/export/audit/logs` |

前端导出工具：`frontend/src/utils/download.ts`，API 封装：`frontend/src/api/export.ts`。

---

## 10. 常见问题

| 问题 | 解决方案 |
|---|---|
| 后端启动报数据库连接失败 | 检查 MySQL 服务是否启动、`application.yml` 中密码是否正确 |
| 前端登录提示 401 | 确认后端已启动，检查 Vite proxy 端口与后端端口一致（8081） |
| 页面提示无权限 | 确认用户已分配角色和权限，检查 `migration-v5-rbac.sql` 是否已执行 |
| AI 分析显示规则引擎模式 | 未配置 `DEEPSEEK_API_KEY` 环境变量，或 API 调用失败，属正常 fallback 行为 |
| 前端只能 IPv6 监听 | `vite.config.ts` 已配置 `host: '127.0.0.1'` 强制 IPv4 |
| Maven 构建失败 | 确认 JDK 17+ 和 Maven 3.6+ 已正确安装 |
| 演示账号登录失败 | 确认 `migration-v16-demo-roles.sql` 已执行 |

---

## 11. 开发规范

### 开发流程

新功能开发遵循 SDD（Spec-Driven Development）方式：

```
Spec → Design → Tasks → Implementation → Acceptance → Review → Commit
```

相关模板见 `docs/sdd/` 目录。

### 代码规范

1. **新功能先写 SDD。** 在 `docs/sdd/` 下先编写需求规格文档，明确做什么和不做什么。
2. **不要随意重构已稳定主链路。** 登录、资产台账、生命周期单据、审批流、RBAC 权限、AI、Excel、审计模块已稳定，新功能应在现有结构上增量开发。
3. **数据库变更必须有 migration SQL。** 新增表或字段时，在 `backend/src/main/resources/sql/` 下新增迁移脚本，必须幂等可重复执行。
4. **后端接口保持统一 `Result<T>` 返回格式。** 所有接口的响应体结构一致，便于前端统一处理。
5. **前端接口统一通过 api 层。** 所有 HTTP 请求通过 `frontend/src/api/` 下的模块发送，不要在组件中直接调用 Axios。
6. **资产状态变化必须写入操作日志。** 任何资产状态变更都要记录到 `asset_operation_log`。
7. **API Key 不入库。** 敏感配置仅通过环境变量注入，不硬编码到代码，不提交到 git。

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

不要提交 `node_modules/`、`target/`、本地 `.env`、IDE 缓存文件、`custom-settings.xml`、`.trae-html-share-packages/`。

---

## 12. 关键设计规则

### 资产编号规则

格式：`FA` + `yyyyMM` + `4位流水号`（如 FA2026070001），由后端自动生成。演示数据使用 `DEMO` 前缀。

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

### 状态标签颜色规范

| 颜色 | 状态 |
|---|---|
| 绿色 | 正常 / 成功 / 已完成 |
| 蓝色 | 审批中 / 处理中 / 进行中 |
| 灰蓝色 | 闲置 / 草稿 / 未开始 |
| 橙色 | 警告 / 待处理 / 维修中 |
| 红色 | 驳回 / 异常 / 失败 / 报废 |

### 逻辑删除

`asset` 表使用 `deleted` 字段标记，MyBatis-Plus 全局配置自动过滤已删除数据。

---

## 13. 项目文档索引

| 文档 | 说明 |
|---|---|
| [docs/demo-accounts.md](docs/demo-accounts.md) | 演示账号说明（7 个岗位账号） |
| [docs/user-manual.md](docs/user-manual.md) | 用户手册（按角色和模块说明） |
| [docs/acceptance-report.md](docs/acceptance-report.md) | 最终验收报告 |
| [docs/final-project-summary.md](docs/final-project-summary.md) | 项目总结（答辩用） |
| [docs/demo-script.md](docs/demo-script.md) | 答辩演示路径 |
| [docs/ai-analysis-design.md](docs/ai-analysis-design.md) | AI 模块设计说明 |
| [docs/database-design.md](docs/database-design.md) | 数据库设计 |
| [docs/api-design.md](docs/api-design.md) | API 设计 |
| [docs/sdd/](docs/sdd/) | SDD 规格驱动开发文档 |
