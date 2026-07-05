# 国企固定资产全生命周期管理系统

## 1. 项目简介

本项目是一个面向国企、事业单位、大型企业的固定资产全生命周期管理平台，覆盖固定资产从入库、领用、调拨、维修、盘点、折旧计提、报废到财务对接的完整业务闭环。

系统采用前后端分离架构，体现企业级、规范化、可审计、可扩展的设计思路。

系统最终目标覆盖：

- 资产入库
- 资产领用
- 资产调拨
- 维修管理
- 盘点管理
- 折旧计提
- 报废管理
- 财务对接
- AI 智能分析
- RFID / 二维码扫码盘点

### 当前阶段

**第一阶段 —— 项目骨架与核心主链路**（已完成）

第一阶段聚焦资产台账核心链路，保证系统可启动、可登录、可进入后台、可管理资产台账。所有代码清晰可维护，不过度封装，不引入不必要的复杂架构。

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
│       │   │   ├── controller/                    # AssetController, AssetCategoryController
│       │   │   ├── dto/                           # CreateRequest, UpdateRequest, QueryRequest
│       │   │   ├── entity/                        # Asset, AssetCategory, AssetOperationLog
│       │   │   ├── enums/                         # AssetStatusEnum
│       │   │   ├── mapper/                        # AssetMapper, AssetCategoryMapper, AssetOperationLogMapper
│       │   │   ├── service/                       # AssetService, AssetCategoryService
│       │   │   └── vo/                            # 响应 VO
│       │   ├── auth/                              # 认证模块
│       │   │   ├── controller/                    # AuthController
│       │   │   ├── dto/                           # LoginRequest, LoginResponse
│       │   │   ├── interceptor/                   # AuthInterceptor
│       │   │   ├── service/                       # AuthService
│       │   │   └── util/                          # JwtUtil
│       │   ├── common/                            # 公共组件
│       │   │   ├── Result.java                    # 统一返回
│       │   │   ├── PageResult.java                # 统一分页
│       │   │   ├── ResultCode.java                # 状态码
│       │   │   ├── BusinessException.java         # 业务异常
│       │   │   └── GlobalExceptionHandler.java    # 统一异常处理
│       │   ├── config/                            # 配置
│       │   │   ├── MybatisPlusConfig.java         # MyBatis-Plus 分页
│       │   │   ├── WebConfig.java                 # CORS + 拦截器
│       │   │   └── BeanConfig.java                # Bean 配置
│       │   ├── context/                           # 用户上下文
│       │   │   ├── LoginUser.java
│       │   │   └── UserContext.java
│       │   ├── dashboard/                         # 首页驾驶舱
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   └── vo/
│       │   ├── depreciation/                      # 折旧模块（预留）
│       │   │   └── entity/
│       │   ├── finance/                           # 财务模块（骨架）
│       │   │   ├── controller/
│       │   │   └── service/
│       │   ├── inventory/                         # 盘点模块（骨架）
│       │   │   ├── controller/
│       │   │   ├── entity/
│       │   │   └── service/
│       │   └── user/                              # 用户模块
│       │       ├── entity/                        # SysUser, SysRole, SysUserRole
│       │       └── mapper/
│       └── resources/
│           ├── application.yml                    # 配置文件
│           ├── mapper/asset/AssetMapper.xml       # 资产 XML Mapper
│           └── sql/init.sql                       # 数据库初始化
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.ts                                # 入口
│       ├── App.vue                                # 根组件
│       ├── api/                                   # API 封装
│       │   ├── request.ts                         # Axios 封装（Token 注入、401 拦截）
│       │   ├── auth.ts                            # 登录相关
│       │   ├── asset.ts                           # 资产相关
│       │   ├── dashboard.ts                       # 驾驶舱相关
│       │   ├── inventory.ts                       # 盘点 API（预留）
│       │   ├── finance.ts                         # 财务 API（预留）
│       │   └── ai.ts                              # AI API（预留）
│       ├── router/
│       │   └── index.ts                           # 路由配置 + 守卫
│       ├── stores/
│       │   ├── auth.ts                            # 登录状态
│       │   └── app.ts                             # 应用状态
│       ├── layouts/
│       │   └── MainLayout.vue                     # 主布局（左侧导航 + 顶部栏）
│       ├── views/
│       │   ├── Login.vue                          # 登录页
│       │   ├── Dashboard.vue                      # 首页驾驶舱
│       │   ├── asset/
│       │   │   ├── AssetList.vue                  # 资产台账（完整实现）
│       │   │   ├── AssetDetail.vue                # 资产详情（完整实现）
│       │   │   └── AssetCategory.vue              # 资产分类（完整实现）
│       │   ├── lifecycle/                         # 生命周期页面（骨架）
│       │   │   ├── Inbound.vue
│       │   │   ├── Receive.vue
│       │   │   ├── Transfer.vue
│       │   │   ├── Repair.vue
│       │   │   └── Scrap.vue
│       │   ├── inventory/
│       │   │   └── InventoryTask.vue              # 盘点任务（骨架）
│       │   ├── depreciation/
│       │   │   └── DepreciationReport.vue         # 折旧报表（骨架）
│       │   ├── finance/
│       │   │   └── FinanceSync.vue                # 财务同步（骨架）
│       │   ├── ai/
│       │   │   └── AiAnalysis.vue                 # AI 智能分析（骨架）
│       │   └── system/
│       │       └── UserManage.vue                 # 用户管理（骨架）
│       ├── components/                            # 公共组件
│       │   ├── PageHeader.vue
│       │   ├── DataCard.vue
│       │   ├── AssetStatusTag.vue
│       │   ├── EmptyState.vue
│       │   └── TableToolbar.vue
│       ├── types/                                 # TypeScript 类型定义
│       │   ├── user.ts
│       │   ├── asset.ts
│       │   └── dashboard.ts
│       ├── utils/                                 # 工具函数
│       │   ├── token.ts
│       │   ├── format.ts
│       │   └── dict.ts
│       └── styles/                                # 样式文件
│           ├── variables.css
│           ├── global.css
│           └── layout.css
│
└── README.md                                      # 本文件
```

---

## 4. 数据库初始化方式

### SQL 文件位置

```
backend/src/main/resources/sql/init.sql
```

### 初始化内容

执行该 SQL 会自动完成：

1. 创建数据库 `fixed_asset_lifecycle_system`
2. 创建 9 张表：
   - `sys_user` — 系统用户表
   - `sys_role` — 系统角色表
   - `sys_user_role` — 用户角色关联表
   - `asset_category` — 资产分类表
   - `asset` — 固定资产表
   - `asset_operation_log` — 资产操作日志表
   - `depreciation_record` — 折旧记录表（结构预留）
   - `inventory_task` — 盘点任务表（结构预留）
   - `inventory_record` — 盘点明细表（结构预留）
3. 插入默认数据：
   - 1 个管理员账号（admin）
   - 4 个角色（ADMIN / ASSET_MANAGER / FINANCE / AUDITOR）
   - 5 个资产分类
   - 20 条固定资产测试数据
   - 20 条初始化操作日志

### 执行方式

```bash
# 方式一：命令行导入
mysql -uroot -p123456 < backend/src/main/resources/sql/init.sql

# 方式二：使用数据库管理工具（Navicat / DataGrip / MySQL Workbench）
# 打开 backend/src/main/resources/sql/init.sql 并执行
```

### 配置修改

如果本机 MySQL 账号密码不是 `root / 123456`，请修改：

```
backend/src/main/resources/application.yml
```

```yaml
spring:
  datasource:
    username: root          # 改为你的用户名
    password: 123456        # 改为你的密码
    url: jdbc:mysql://localhost:3306/fixed_asset_lifecycle_system?...
```

---

## 5. 默认账号密码

| 字段 | 值 |
|---|---|
| 用户名 | `admin` |
| 密码 | `123456` |
| 真实姓名 | 系统管理员 |
| 所属部门 | 信息中心 |

### 默认角色

| 角色编码 | 角色名称 | 说明 |
|---|---|---|
| `ADMIN` | 系统管理员 | 系统超级管理员 |
| `ASSET_MANAGER` | 资产管理员 | 负责资产台账和盘点 |
| `FINANCE` | 财务人员 | 负责折旧与财务对接 |
| `AUDITOR` | 审计人员 | 只读审计角色 |

---

## 6. 后端启动方式

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8

### 启动命令

```bash
cd backend

# 编译打包
mvn -DskipTests package

# 方式一：Maven 直接启动
mvn spring-boot:run

# 方式二：运行 Jar
java -jar target/fixed-asset-backend-0.0.1-SNAPSHOT.jar
```

### 后端端口

默认端口：**8080**

---

## 7. 前端启动方式

### 环境要求

- Node.js 18+
- npm 9+

### 启动命令

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev
```

### 前端端口

默认端口：**3000**

### 代理说明

前端通过 Vite proxy 将 `/api` 请求代理到后端 `http://localhost:8080`：

```typescript
// vite.config.ts
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 构建命令

```bash
npm run build       # 生产构建（TypeScript 检查 + Vite 打包）
```

构建产物输出到 `frontend/dist/` 目录。

---

## 8. 第一阶段已完成功能

### 8.1 登录鉴权

**已实现：**

- 用户名/密码登录（admin / 123456）
- JWT Token 签发与校验
- 前端请求自动携带 `Authorization: Bearer <token>`
- 后端 `HandlerInterceptor` 统一接口鉴权
- 无 Token / 错误 Token / 过期 Token 统一返回 HTTP 401
- 前端 Axios 拦截器收到 401 后清除登录态并跳转登录页
- 登录成功后跳转驾驶舱
- 已登录用户访问 `/login` 自动跳转驾驶舱
- 未登录用户访问后台页面自动跳转登录页

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/auth/login` | 登录，返回 JWT |
| `GET` | `/api/auth/me` | 获取当前登录用户信息 |

### 8.2 首页驾驶舱

**已实现：**

- 资产总数（真实统计）
- 资产总原值（真实统计）
- 累计折旧（真实统计）
- 资产净值（真实统计）
- 使用中资产数量（真实统计）
- 闲置资产数量（真实统计）
- 维修中资产数量（真实统计）
- 待报废资产数量（真实统计）
- 资产分类分布图（ECharts 饼图）
- 部门资产金额排行（ECharts 柱状图）
- 月度折旧趋势图（ECharts 折线图）

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/dashboard/stats` | 核心统计数据 |
| `GET` | `/api/dashboard/category-distribution` | 分类分布 |
| `GET` | `/api/dashboard/department-ranking` | 部门金额排行 |
| `GET` | `/api/dashboard/depreciation-trend` | 月度折旧趋势（第一阶段 mock） |

### 8.3 资产台账

**已实现：**

- 资产分页查询
- 多条件组合查询（编号、名称、分类、部门、使用人、地点、状态、购置日期范围）
- 新增资产
- 编辑资产
- 逻辑删除资产
- 资产详情查看
- 状态标签（颜色区分 7 种状态）
- 金额格式化
- 日期格式化
- 新增/编辑弹窗表单

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/assets/page` | 分页查询 |
| `GET` | `/api/assets/{id}` | 资产详情 |
| `POST` | `/api/assets` | 新增资产 |
| `PUT` | `/api/assets/{id}` | 编辑资产 |
| `DELETE` | `/api/assets/{id}` | 逻辑删除 |
| `GET` | `/api/assets/status-options` | 状态字典 |

**关键设计规则：**

| 规则 | 说明 |
|---|---|
| 资产编号 | 后端自动生成，格式 `FA + yyyyMM + 4位流水号` |
| 新增默认状态 | `IDLE`（闲置） |
| 折旧计算 | 后端平均年限法自动计算 |
| 净值计算 | 后端自动计算，不低于残值 |
| 操作日志 | 新增/编辑/删除均写入 `asset_operation_log` |
| 删除方式 | 逻辑删除，`deleted` 字段标记 |

### 8.4 资产详情

**已实现：**

展示资产完整信息：

- 基础信息：资产编号、资产名称、分类、品牌、规格型号
- 价值信息：原值、净值、累计折旧、残值率、使用年限、折旧方法
- 使用信息：购置日期、所属部门、使用人、存放地点
- 状态信息：资产状态（带颜色标签）
- 标签信息：二维码、RFID
- 备注信息

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/assets/{id}` | 资产详情 |

### 8.5 资产分类

**已实现：**

- 分类树展示（左侧树形结构）
- 分类列表展示（右侧表格）
- 分类编码、分类名称、默认折旧年限、备注

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/asset-categories/list` | 分类列表 |
| `GET` | `/api/asset-categories/tree` | 分类树状结构 |

### 8.6 首页驾驶舱关键数据

数据库当前概况（实时）：

| 指标 | 值 |
|---|---|
| 有效资产数量 | 20 条 |
| 已删除资产 | 3 条（测试产生） |
| 资产分类 | 5 个 |
| 操作日志 | 26 条 |

状态分布：

| 状态 | 数量 |
|---|---|
| 使用中（IN_USE） | 11 |
| 闲置（IDLE） | 4 |
| 维修中（REPAIRING） | 3 |
| 待报废（WAITING_SCRAP） | 2 |

---

## 9. 第一阶段骨架页面

以下页面当前为骨架页面——可打开、不白屏、有标题和模块说明，但未实现完整业务。后续由其他人继续开发。

| 路由 | 页面 | 当前状态 |
|---|---|---|
| `/lifecycle/inbound` | 资产入库 | 骨架，提示第二阶段接入 |
| `/lifecycle/receive` | 资产领用 | 骨架，提示第二阶段接入 |
| `/lifecycle/transfer` | 资产调拨 | 骨架，提示第二阶段接入 |
| `/lifecycle/repair` | 维修管理 | 骨架，提示第二阶段接入 |
| `/lifecycle/scrap` | 报废管理 | 骨架，提示第二阶段接入 |
| `/inventory/tasks` | 盘点任务 | 骨架，提示第二阶段接入 |
| `/depreciation/report` | 折旧报表 | 骨架 + 空表格 |
| `/finance/sync` | 财务同步 | 骨架，模拟状态 |
| `/ai/analysis` | AI 智能分析 | 骨架，3 个能力入口 |
| `/system/users` | 用户管理 | 骨架，提示第二阶段接入 |

---

## 10. 第一阶段验收结果

### 构建验证

| 项目 | 命令 | 结果 |
|---|---|---|
| 前端 TypeScript 检查 | `vue-tsc --noEmit` | ✅ 通过 |
| 前端 Vite 构建 | `vite build` | ✅ 通过 |
| 后端 Maven 编译 | `mvn -DskipTests package` | ✅ 通过 |

### 联调验证

| 验收项 | 结果 |
|---|---|
| 后端正常启动，8080 端口可访问 | ✅ |
| 前端正常启动，3000 端口可访问 | ✅ |
| 数据库初始化成功 | ✅ |
| `admin / 123456` 登录成功 | ✅ |
| 首页 stats 接口真实返回 asset 表数据 | ✅ |
| 首页 category-distribution 接口通过 | ✅ |
| 首页 department-ranking 接口通过 | ✅ |
| 首页 depreciation-trend 接口通过 | ✅ |
| 资产分页查询通过 | ✅ |
| 新增资产成功（编号自动生成、状态默认为 IDLE） | ✅ |
| 编辑资产成功（状态变更写入日志） | ✅ |
| 逻辑删除成功（不在分页返回） | ✅ |
| 资产详情接口通过 | ✅ |
| 资产分类列表/树接口通过 | ✅ |
| 路由鉴权：未登录跳转登录页 | ✅ |
| 路由鉴权：已登录访问 `/login` 跳转驾驶舱 | ✅ |
| 无 Token 请求返回 401 | ✅ |
| 无效 Token 请求返回 401 | ✅ |
| 前端构建无报错 | ✅ |
| 后端构建无报错 | ✅ |
| 浏览器无白屏 | ✅ |
| 控制台无明显报错 | ✅ |
| Network 无明显失败接口 | ✅ |

---

## 11. 当前未实现内容

以下功能第一阶段没有实现，等待后续开发：

**生命周期单据流：**

- 入库单（`asset_inbound_order`）
- 领用单（`asset_receive_order`）
- 调拨单（`asset_transfer_order`）
- 维修单（`asset_repair_order`）
- 报废单（`asset_scrap_order`）

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

**外部系统：**

- 真实财务系统对接（用友/金蝶/SAP）
- 真实 AI 模型
- 真实 RFID 硬件
- 二维码打印与标签

**系统功能：**

- 审批流
- 消息通知
- 系统日志查询

---

## 12. 后续开发路线

### 第二阶段：生命周期单据流

**目标：** 让资产状态不再靠手动编辑，而是通过业务单据自动流转。

**新表：**

- `asset_inbound_order` — 入库单
- `asset_receive_order` — 领用单
- `asset_transfer_order` — 调拨单
- `asset_repair_order` — 维修单
- `asset_scrap_order` — 报废单

**实现：**

- 入库流程：新增资产 → 入库单 → 状态 IDLE
- 领用流程：选择资产 → 领用单 → 状态 IN_USE
- 调拨流程：选择资产 → 调拨单 → 状态 TRANSFERRING → IN_USE
- 维修流程：选择资产 → 维修单 → 状态 REPAIRING → IN_USE/IDLE
- 报废流程：选择资产 → 报废申请 → 状态 WAITING_SCRAP → SCRAPPED
- 每个流程写入 `asset_operation_log`

### 第三阶段：盘点管理

**目标：** 实现固定资产盘点完整流程。

**实现：**

- 创建盘点任务（选择部门/地点范围）
- 生成盘点清单
- 二维码扫码盘点
- 盘点结果（正常/盘盈/盘亏/位置异常）
- 盘点报告
- 异常资产状态标记为 `INVENTORY_ABNORMAL`

### 第四阶段：折旧报表与导出

**实现：**

- 月度折旧报表
- 按部门统计
- 按分类统计
- Excel 导出（后端 POI 或 EasyExcel）
- 财务同步数据准备

### 第五阶段：用户管理与权限

**实现：**

- 用户列表
- 新增/编辑用户
- 角色分配
- 菜单权限
- 按钮级权限

### 第六阶段：AI / RFID / 财务对接

**实现：**

- AI 资产照片分类
- AI 故障风险预测
- AI 盘点路线推荐
- RFID 设备接口
- 财务系统模拟/真实对接

---

## 13. 推荐团队分工

| 模块 | 建议负责 |
|---|---|
| 资产生命周期模块 | 入库、领用、调拨、维修、报废的业务单据设计和前端实现 |
| 盘点模块 | 二维码生成、扫码盘点、盘点任务、盘点报告 |
| 报表模块 | 折旧报表、Excel 导出、统计图表优化 |
| 权限模块 | 用户管理、角色分配、菜单权限、按钮权限 |
| AI / RFID / 财务模块 | 智能分类、故障预测、盘点路线、外部系统对接 |
| 测试与文档 | 接口测试、浏览器验收、使用说明、答辩文档 |

---

## 14. 开发注意事项

1. **不要重构第一阶段已完成的主链路。** 登录、资产台账、驾驶舱、资产详情、资产分类已稳定，新功能应在现有结构上增量开发。

2. **资产状态变化必须写入 `asset_operation_log`。** 这是审计和追溯的基础，任何状态变更都要记录操作人、变更时间和变更内容。

3. **新增业务要先设计表结构和接口，不要直接硬改 `asset` 表。** 生命周期单据应该使用独立的业务单据表（如 `asset_inbound_order`），通过 `asset_id` 关联资产。

4. **前端保持企业 Web 后台风格。** 左侧深蓝导航、顶部白色用户栏、主体灰白背景。不要做成 App 风格或炫酷大屏。

5. **后端接口保持统一 `Result<T>` 返回格式。** 所有接口的响应体结构一致，便于前端统一处理。

6. **需要真实 HTTP 验收，不要只看 build 通过。** 每次开发完成后，应同时启动前后端，使用浏览器验证功能是否正常。

7. **提交规范。**
   - 不要提交 `node_modules/`
   - 不要提交 `target/`（编译产物）
   - 建议添加 `.gitignore` 到项目根目录
   - 提交信息建议格式：`[模块] 做了什么`

---

## 15. 整个项目最终形态

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

*本项目是「国企固定资产全生命周期管理系统」第一阶段可运行版本。*
*更多信息请查看 `docs/` 目录下的设计文档。*
