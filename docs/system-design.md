# 系统设计文档

## 1. 系统架构

### 1.1 架构概述

```
+------------------+       +------------------+       +------------------+
|                  | HTTP  |                  | JDBC  |                  |
|  Vue 3 前端      |------>|  Spring Boot 3   |------>|  MySQL 8         |
|  (Element Plus)  |       |  (RESTful API)   |       |  (数据库)         |
|                  |<------|                  |<------|                  |
+------------------+  JSON +------------------+  Data +------------------+
```

### 1.2 前后端分层

**前端分层：**

| 层 | 说明 |
|---|---|
| Views | 页面组件（Login, Dashboard, AssetList 等） |
| Components | 公共组件（PageHeader, DataCard, AssetStatusTag 等） |
| Router | 路由配置与守卫 |
| Stores (Pinia) | 全局状态管理（auth, app） |
| API | Axios 请求封装 |
| Types | TypeScript 类型定义 |
| Utils | 工具函数（token, format, dict） |
| Styles | 全局样式（variables, global, layout） |

**后端分层：**

| 层 | 说明 |
|---|---|
| Controller | RESTful 接口层 |
| Service | 业务逻辑层 |
| Mapper | 数据访问层（MyBatis-Plus） |
| Entity | 数据实体 |
| DTO | 请求数据传输对象 |
| VO | 响应视图对象 |
| Config | 配置类 |
| Common | 公共组件（Result, Exception等） |
| Auth | 鉴权模块 |
| Context | 用户上下文 |

### 1.3 技术选型理由

| 选型 | 理由 |
|---|---|
| Spring Boot 3 | 企业级开发标准框架，生态完善 |
| MyBatis-Plus | 单表 CRUD 无需写 SQL，代码简洁 |
| JWT | 无状态认证，适合前后端分离 |
| HandlerInterceptor | 轻量鉴权，避免 Spring Security 过度封装 |
| Vue 3 + TypeScript | 类型安全，组件化开发 |
| Element Plus | 企业级 UI 组件库，适合后台系统 |
| Pinia | 轻量状态管理，Vue 3 官方推荐 |
| ECharts | 成熟的数据可视化库 |

---

## 2. 鉴权流程

### 2.1 登录流程

```
用户输入账号密码
       |
       v
前端 POST /api/auth/login
       |
       v
后端 AuthService.login()
  ├── 查询 sys_user 表
  ├── BCrypt 校验密码
  ├── 查询用户角色
  ├── 生成 JWT Token（含 userId, username, roles）
  └── 返回 token + 用户信息
       |
       v
前端保存 token 到 localStorage
       |
       v
前端跳转 /dashboard
```

### 2.2 接口鉴权流程

```
前端请求 /api/**
       |
       v
Axios 请求拦截器注入 Authorization: Bearer <token>
       |
       v
AuthInterceptor.preHandle()
  ├── OPTIONS 请求放行
  ├── 检查 Authorization 头
  ├── 解析 JWT Token
  ├── 写入 UserContext（ThreadLocal）
  └── 放行到 Controller
       |
       v
Controller 处理业务
       |
       v
AuthInterceptor.afterCompletion()
  └── 清除 UserContext
```

### 2.3 401 处理流程

```
后端返回 HTTP 401
       |
       v
Axios 响应拦截器捕获 401
  ├── 清除 localStorage token
  ├── 清除 Pinia 用户信息
  ├── Element Plus Message 提示
  └── window.location.href = '/login'
```

### 2.4 路由守卫流程

```
访问任意路由
       |
       v
Router.beforeEach()
  ├── 访问 /login 且已登录 → 跳转 /dashboard
  ├── 访问 /login 且未登录 → 放行
  ├── 访问其他页面且已登录 → 放行
  └── 访问其他页面且未登录 → 跳转 /login
```

---

## 3. 资产台账流程

### 3.1 新增资产流程

```
前端打开新增弹窗
       |
       v
用户填写表单（不填 assetCode）
       |
       v
前端 POST /api/assets
  ├── 后端生成 assetCode（FA+年月+4位流水）
  ├── 默认状态 IDLE
  ├── 后端计算 accumulatedDepreciation
  ├── 后端计算 netValue
  ├── 写入 asset 表
  └── 写入 asset_operation_log
       |
       v
前端刷新表格
```

### 3.2 编辑资产流程

```
前端打开编辑弹窗（回显数据）
       |
       v
用户修改字段（可选修改状态）
       |
       v
前端 PUT /api/assets/{id}
  ├── 后端校验资产存在
  ├── 重新计算折旧和净值
  ├── 更新 asset 表
  ├── 如果状态变化：写入 asset_operation_log
  └── 如果状态未变：记录基本信息变更
       |
       v
前端刷新表格
```

### 3.3 删除资产流程

```
用户点击删除
       |
       v
二次确认弹窗
       |
       v
前端 DELETE /api/assets/{id}
  ├── 后端逻辑删除（deleted = 1）
  └── 写入 asset_operation_log
       |
       v
前端刷新表格
```

### 3.4 分页查询流程

```
前端提交查询表单
       |
       v
前端 GET /api/assets/page?pageNum=1&pageSize=10&...
  ├── 后端 MyBatis-Plus 分页
  ├── 自动过滤 deleted=0
  ├── 联表查询分类名称
  └── 返回 PageResult
       |
       v
前端渲染表格
```

---

## 4. 首页驾驶舱统计设计

### 4.1 统计口径

| 指标 | 来源 | 说明 |
|---|---|---|
| 资产总数 | asset 表 COUNT | 过滤 deleted=0 |
| 资产总原值 | asset 表 SUM(original_value) | 过滤 deleted=0 |
| 累计折旧 | asset 表 SUM(accumulated_depreciation) | 过滤 deleted=0 |
| 资产净值 | asset 表 SUM(net_value) | 过滤 deleted=0 |
| 使用中数量 | WHERE status = 'IN_USE' | 实时统计 |
| 闲置数量 | WHERE status = 'IDLE' | 实时统计 |
| 维修中数量 | WHERE status = 'REPAIRING' | 实时统计 |
| 待报废数量 | WHERE status = 'WAITING_SCRAP' | 实时统计 |
| 分类分布 | LEFT JOIN asset_category GROUP BY | 各分类资产数 |
| 部门排行 | GROUP BY department | 各部门资产原值合计 |
| 折旧趋势 | 第一阶段 mock | 后续接入真实数据 |

### 4.2 SQL 示例

```sql
-- 核心统计
SELECT
    COUNT(*) AS assetCount,
    COALESCE(SUM(original_value), 0) AS totalOriginalValue,
    COALESCE(SUM(accumulated_depreciation), 0) AS totalAccumulatedDepreciation,
    COALESCE(SUM(net_value), 0) AS totalNetValue,
    COALESCE(SUM(CASE WHEN status = 'IN_USE' THEN 1 ELSE 0 END), 0) AS inUseCount,
    COALESCE(SUM(CASE WHEN status = 'IDLE' THEN 1 ELSE 0 END), 0) AS idleCount,
    COALESCE(SUM(CASE WHEN status = 'REPAIRING' THEN 1 ELSE 0 END), 0) AS repairingCount,
    COALESCE(SUM(CASE WHEN status = 'WAITING_SCRAP' THEN 1 ELSE 0 END), 0) AS waitingScrapCount
FROM asset
WHERE deleted = 0;

-- 分类分布
SELECT c.category_name AS name, COUNT(a.id) AS value
FROM asset_category c
LEFT JOIN asset a ON a.category_id = c.id AND a.deleted = 0
GROUP BY c.id, c.category_name
ORDER BY value DESC;

-- 部门排行
SELECT department AS name, COALESCE(SUM(original_value), 0) AS value
FROM asset
WHERE deleted = 0
GROUP BY department
ORDER BY value DESC;
```

---

## 5. 前端设计

### 5.1 UI 风格

- 企业 Web 后台风格
- 左侧深蓝导航（#173B57）
- 顶部白色用户栏
- 主体灰白背景（#F5F7FA）
- 布局紧凑，适合大量数据管理

### 5.2 路由设计

```typescript
/login              → 登录页
/dashboard          → 首页驾驶舱
/assets             → 资产台账
/assets/:id         → 资产详情
/asset-categories   → 资产分类
/lifecycle/*        → 生命周期（骨架）
/inventory/tasks    → 盘点任务（骨架）
/depreciation/report → 折旧报表（骨架）
/finance/sync       → 财务同步（骨架）
/ai/analysis        → AI 智能分析（骨架）
/system/users       → 用户管理（骨架）
```

### 5.3 Vite Proxy 配置

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

---

## 6. 数据库设计原则

1. 表名使用 `sys_` 前缀避免关键字冲突
2. 主键统一使用 `id BIGINT AUTO_INCREMENT`
3. 时间字段统一使用 `created_at` / `updated_at`
4. 逻辑删除字段统一使用 `deleted TINYINT`
5. 业务表预留扩展字段，避免后续频繁改表
6. 操作日志独立于业务表，保证审计完整性
