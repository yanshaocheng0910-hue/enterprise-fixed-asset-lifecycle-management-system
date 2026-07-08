# 第五阶段：用户管理与RBAC权限 · 需求规格说明

## 1. 背景

第一阶段已完成登录鉴权（JWT + HandlerInterceptor），第二阶段已完成生命周期单据流，第三阶段已完成审批流。系统现有 `sys_user`、`sys_role`、`sys_user_role` 三张表结构和种子数据（admin 用户 + 4 个角色），但缺少以下能力：

- 无用户 CRUD 接口和页面，当前只能使用预置的 admin 账号
- 无角色管理接口和页面，角色信息仅存在于种子 SQL 中无法动态管理
- 无权限控制体系，AuthInterceptor 只做认证不做授权，任何登录用户可调用任何接口
- 前端的 UserManage.vue 仅为骨架占位页

本阶段的目标是：**实现完整的用户管理、角色管理、RBAC 权限矩阵，覆盖全系统所有模块的操作权限控制。**

## 2. 目标

- 用户 CRUD：新增、编辑、删除、启用/禁用、分页查询、修改密码
- 角色管理：角色列表、新增、编辑、删除
- 角色分配：为用户分配/取消角色
- RBAC 权限矩阵：34 个权限项覆盖全部模块，通过 `sys_permission` + `sys_role_permission` 两张表实现可配置的权限矩阵
- 配置角色权限：在角色管理页按模块勾选权限项
- 后端权限拦截：`@RequirePermission` 注解 + AOP 切面，Controller 方法级别校验
- 前端路由守卫：基于角色的路由控制，不同角色看到不同菜单和页面
- 前端按钮权限：`v-permission` 指令控制按钮/操作显隐
- 403 无权限页面

## 3. 功能范围

### 本阶段做

| 模块 | 功能 |
|------|------|
| 用户管理 | 分页列表、新增、编辑、删除、启用/禁用、修改密码、角色分配 |
| 角色管理 | 角色列表、新增、编辑、删除、权限矩阵配置 |
| 权限查询 | 按模块分组的权限树、当前用户权限列表 |
| RBAC 拦截 | `@RequirePermission` 注解 + AOP 切面 |
| 前端路由守卫 | 角色驱动的路由访问控制和菜单过滤 |
| 前端按钮权限 | `v-permission` 指令 |
| 403 页面 | 无权限访问时的统一错误页 |
| 数据迁移 | migration-v5-rbac.sql 新增 sys_permission、sys_role_permission 表 + 种子数据 |

### 本阶段不做

- 菜单权限的可视化配置界面（菜单与路由的动态绑定，本期路由-菜单关系硬编码）
- 数据权限（部门级数据隔离，如部门负责人只能看本部门数据）
- 操作日志审计（sys_user 的操作记录单独写入日志，本期暂不做）
- 用户头像上传
- 组织架构/部门管理
- 登录验证码
- 密码强度策略
- LDAP/SSO 集成

## 4. 数据库设计

### 4.1 新增表

**sys_permission（权限项表）**

```sql
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    permission_code VARCHAR(64) NOT NULL UNIQUE COMMENT '权限编码，如 asset:create',
    permission_name VARCHAR(64) NOT NULL COMMENT '权限名称，如新增资产',
    module VARCHAR(32) NOT NULL COMMENT '所属模块',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='权限项表';
```

**sys_role_permission（角色-权限关联表）**

```sql
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) COMMENT='角色权限关联表';
```

### 4.2 权限项种子数据（34 项）

| 模块 | permission_code | permission_name | 说明 |
|------|----------------|-----------------|------|
| 首页 | `dashboard:view` | 查看驾驶舱 | 查看首页驾驶舱 |
| 资产管理 | `asset:view` | 查看资产 | 查看资产台账列表与详情 |
| 资产管理 | `asset:create` | 新增资产 | 新增资产记录 |
| 资产管理 | `asset:edit` | 编辑资产 | 编辑资产信息 |
| 资产管理 | `asset:delete` | 删除资产 | 逻辑删除资产 |
| 资产分类 | `category:view` | 查看分类 | 查看资产分类树与列表 |
| 资产分类 | `category:create` | 新增分类 | 新增资产分类 |
| 资产分类 | `category:edit` | 编辑分类 | 编辑资产分类 |
| 资产分类 | `category:delete` | 删除分类 | 删除资产分类 |
| 生命周期 | `inbound:create` | 资产入库 | 创建资产入库单 |
| 生命周期 | `receive:create` | 资产领用 | 创建资产领用单 |
| 生命周期 | `transfer:create` | 资产调拨 | 创建资产调拨单 |
| 生命周期 | `repair:create` | 维修管理 | 创建维修单并完成维修 |
| 生命周期 | `scrap:create` | 报废管理 | 创建报废单 |
| 盘点管理 | `inventory:view` | 查看盘点 | 查看盘点任务与明细 |
| 盘点管理 | `inventory:create` | 创建盘点 | 创建盘点任务 |
| 折旧报表 | `depreciation:view` | 查看折旧 | 查看折旧报表 |
| 财务对接 | `finance:view` | 查看财务 | 查看财务对接数据 |
| AI分析 | `ai:view` | 查看AI分析 | 查看AI智能分析 |
| 用户管理 | `user:view` | 查看用户 | 查看用户列表 |
| 用户管理 | `user:create` | 新增用户 | 新增系统用户 |
| 用户管理 | `user:edit` | 编辑用户 | 编辑用户信息 |
| 用户管理 | `user:delete` | 删除用户 | 删除系统用户 |
| 用户管理 | `user:status` | 启停用户 | 启用/禁用用户 |
| 用户管理 | `user:role` | 分配角色 | 为用户分配/取消角色 |
| 角色管理 | `role:view` | 查看角色 | 查看角色列表 |
| 角色管理 | `role:create` | 新增角色 | 新增系统角色 |
| 角色管理 | `role:edit` | 编辑角色 | 编辑角色信息 |
| 角色管理 | `role:delete` | 删除角色 | 删除系统角色 |
| 角色管理 | `role:permission` | 配置权限 | 配置角色的操作权限 |
| 审批管理 | `approval:todo` | 我的待办 | 查看和处理待审批项 |
| 审批管理 | `approval:done` | 我的已办 | 查看已处理的审批记录 |
| 审批管理 | `approval:manage` | 管理审批 | 审批配置管理 |
| 审批管理 | `approval:audit` | 审批审计 | 查看全部审批记录 |

### 4.3 默认角色-权限分配

| 角色 | 拥有的权限項 |
|------|-------------|
| ADMIN | 全部 34 项 |
| ASSET_MANAGER | `dashboard:view`, `asset:view`, `asset:create`, `asset:edit`, `asset:delete`, `category:view`, `category:create`, `category:edit`, `inbound:create`, `receive:create`, `transfer:create`, `repair:create`, `scrap:create`, `inventory:view`, `inventory:create`, `approval:todo`, `approval:done`, `approval:manage`（18 项） |
| FINANCE | `dashboard:view`, `asset:view`, `category:view`, `depreciation:view`, `finance:view`, `scrap:create`, `approval:todo`, `approval:done`（8 项） |
| AUDITOR | `dashboard:view`, `asset:view`, `category:view`, `inventory:view`, `depreciation:view`, `finance:view`, `ai:view`, `user:view`, `role:view`, `approval:done`, `approval:audit`（11 项，纯只读） |

## 5. 后端设计

### 5.1 新增文件清单

```
user/
├── controller/
│   └── UserController.java        # 用户 CRUD + 角色分配 + 密码修改
├── service/
│   └── UserService.java           # 用户业务逻辑
├── dto/
│   ├── UserPageRequest.java       # 分页查询请求
│   ├── UserCreateRequest.java     # 新增用户请求
│   ├── UserUpdateRequest.java     # 编辑用户请求
│   ├── UserStatusRequest.java     # 状态切换请求
│   ├── UserRoleRequest.java       # 角色分配请求
│   ├── ChangePasswordRequest.java # 修改密码请求
│   └── UserVO.java                # 用户视图对象（含角色）
role/
├── controller/
│   └── RoleController.java        # 角色 CRUD + 权限配置
├── service/
│   └── RoleService.java           # 角色业务逻辑
├── entity/
│   ├── SysPermission.java         # 权限项实体
│   └── SysRolePermission.java     # 角色权限关联实体
├── mapper/
│   ├── SysPermissionMapper.java   # 权限项 Mapper
│   └── SysRolePermissionMapper.java # 角色权限 Mapper
├── dto/
│   ├── RoleCreateRequest.java     # 新增角色
│   ├── RoleUpdateRequest.java     # 编辑角色
│   ├── RolePermissionRequest.java # 配置权限请求
│   ├── RoleVO.java                # 角色视图对象（含权限）
│   └── PermissionTreeVO.java      # 按模块分组的权限树
└── controller/
    └── PermissionController.java  # 查询权限树 + 当前用户权限
permission/
├── annotation/
│   └── RequirePermission.java     # 权限注解
├── aspect/
│   └── PermissionAspect.java      # 权限校验 AOP 切面
└── exception/
    └── PermissionDeniedException.java # 无权限异常
```

### 5.2 API 接口

**用户管理 (`/api/user`)**

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/user/page` | 分页查询用户列表（可搜索用户名/姓名） | `user:view` |
| GET | `/api/user/{id}` | 获取用户详情（含角色列表） | `user:view` |
| POST | `/api/user` | 新增用户 | `user:create` |
| PUT | `/api/user/{id}` | 编辑用户信息（不含密码和角色） | `user:edit` |
| PUT | `/api/user/{id}/status` | 启用/禁用用户 | `user:status` |
| DELETE | `/api/user/{id}` | 删除用户（不允许删除自己） | `user:delete` |
| PUT | `/api/user/{id}/roles` | 分配角色（全量覆盖式） | `user:role` |
| PUT | `/api/user/me/password` | 修改当前登录用户密码 | 无需额外权限 |

**角色管理 (`/api/role`)**

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/role/all` | 全部角色列表（分配角色下拉用） | 登录即可 |
| GET | `/api/role/page` | 分页角色列表 | `role:view` |
| GET | `/api/role/{id}` | 角色详情（含权限ID列表） | `role:view` |
| POST | `/api/role` | 新增角色 | `role:create` |
| PUT | `/api/role/{id}` | 编辑角色 | `role:edit` |
| DELETE | `/api/role/{id}` | 删除角色（不允许删除被分配的角色） | `role:delete` |
| PUT | `/api/role/{id}/permissions` | 配置角色权限（全量覆盖） | `role:permission` |

**权限查询 (`/api/permission`)**

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/permission/tree` | 按模块分组的权限树 | 登录即可 |
| GET | `/api/permission/my` | 当前登录用户权限列表 | 登录即可 |

**Auth 接口扩展**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/me` | 需扩展返回字段：增加 `permissions` 字段（List<String>） |

### 5.3 `@RequirePermission` 注解与 AOP

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value(); // 权限编码，如 "asset:create"
}
```

AOP 切面逻辑：
1. 从 UserContext 获取当前用户
2. 通过 sys_user_role + sys_role_permission + sys_permission 联表查询用户所有权限码
3. 检查目标权限码是否在用户权限列表中
4. 若无权限，抛出 PermissionDeniedException → 全局异常处理器返回 403

AOP 切面仅拦截 `@RestController` 内带 `@RequirePermission` 的方法。

### 5.4 AuthService 扩展

- `getCurrentUser()` 返回值增加 `permissions` 字段（从登录后的 JWT 解析或实时查询）

### 5.5 业务规则

- 新增用户时密码使用 BCrypt 加密
- 编辑用户时不修改密码（密码修改通过单独接口）
- 不能删除自己（userId == 当前登录用户时拒绝）
- 不能禁用自己
- 删除角色前检查是否有用户被分配了该角色，如有则拒绝删除并提示
- admin 账号不可删除
- 密码修改需要校验原密码

## 6. 前端设计

### 6.1 新增文件和改造文件

**新增文件：**

```
views/system/
├── UserManage.vue              # 改造：从骨架页改为完整 CRUD 页面
├── RoleManage.vue              # 新增：角色管理页面
└── Forbidden.vue               # 新增：403 无权限页面

directives/
└── permission.ts               # 新增：v-permission 指令

api/
└── permission.ts               # 新增：权限查询 API
```

**改造文件：**

| 文件 | 改造内容 |
|------|---------|
| `router/index.ts` | 新增角色管理路由、403 路由；改造 beforeEach 加入权限校验 |
| `layouts/MainLayout.vue` | 侧边栏菜单按权限过滤 |
| `stores/auth.ts` | 新增 permissions 状态；登录后获取权限列表 |
| `api/auth.ts` | getCurrentUser 返回类型扩展 permissions 字段 |
| `api/asset.ts` | （如有用户相关调用需调整） |
| `types/user.ts` | 新增 SysUser、SysRole、Permission 等类型定义 |

### 6.2 用户管理页面 (`UserManage.vue`)

**表格列**：用户名、真实姓名、部门、角色（el-tag 展示）、手机号、状态（el-switch）、操作（编辑/角色分配/删除）

**搜索栏**：用户名输入框、姓名输入框、状态下拉、搜索按钮、重置按钮

**新增/编辑弹窗**：
- 用户名（新增必填/编辑只读）、真实姓名、部门、手机号、邮箱、密码（仅新增时显示且必填）
- 数据校验：用户名2-20字符、真实姓名必填

**角色分配弹窗**：
- Checkbox-Group 展示全部可选角色
- 支持多选，全量提交

**删除**：二次确认弹窗

### 6.3 角色管理页面 (`RoleManage.vue`)

**表格列**：角色编码、角色名称、描述、操作（编辑/权限配置/删除）

**新增/编辑弹窗**：角色编码、角色名称、描述

**权限配置弹窗**：
- 按模块分组的 Checkbox 树（el-tree）
- 每个模块一个节点，节点下是子权限项
- 全选/取消模块即可批量操作该模块下所有权限
- 保存时全量提交角色拥有的权限 ID 列表

### 6.4 403 页面 (`Forbidden.vue`)

- 居中显示"403 无权限访问"提示
- "返回首页"按钮

### 6.5 `v-permission` 指令 (`directives/permission.ts`)

```typescript
// 用法: <el-button v-permission="'asset:create'">新增</el-button>
// 无权限时从 DOM 中移除元素
```

从 Pinia authStore 获取当前用户权限列表，检查目标权限码是否存在。

### 6.6 路由守卫改造

```typescript
// 路由 meta 增加 permission 字段
{ path: 'system/users', name: 'UserManage', ..., meta: { title: '用户管理', permission: 'user:view' } }
{ path: 'system/roles', name: 'RoleManage', ..., meta: { title: '角色管理', permission: 'role:view' } }

// beforeEach 改造：
// 1. 已登录 + 路由有 permission 要求 → 检查权限 → 无权限跳转 /403
// 2. 其余逻辑不变
```

### 6.7 侧边栏菜单过滤

MainLayout 中将菜单项配置化（从路由提取或硬编码映射），根据 authStore.permissions 过滤：

| 菜单 | 所需权限 |
|------|---------|
| 首页驾驶舱 | `dashboard:view` |
| 资产管理 → 资产台账 | `asset:view` |
| 资产管理 → 资产分类 | `category:view` |
| 生命周期 → 入库 | `inbound:create` |
| 生命周期 → 领用 | `receive:create` |
| 生命周期 → 调拨 | `transfer:create` |
| 生命周期 → 维修 | `repair:create` |
| 生命周期 → 报废 | `scrap:create` |
| 盘点管理 | `inventory:view` |
| 折旧报表 | `depreciation:view` |
| 财务对接 | `finance:view` |
| AI 智能分析 | `ai:view` |
| 系统设置 → 用户管理 | `user:view` |
| 系统设置 → 角色管理 | `role:view` |

### 6.8 按钮级权限示例

```html
<!-- 资产台账页面 -->
<el-button type="primary" v-permission="'asset:create'">新增资产</el-button>
<el-button type="danger" v-permission="'asset:delete'">删除</el-button>

<!-- 用户管理页面 -->
<el-button type="primary" v-permission="'user:create'">新增用户</el-button>
<el-button type="warning" v-permission="'user:edit'">编辑</el-button>
<el-button v-permission="'user:role'">角色分配</el-button>
<el-button type="danger" v-permission="'user:delete'">删除</el-button>
```

## 7. 数据流

```
1. 用户登录 → JWT 签发（含 userId, username, roles）
2. 前端 getCurrentUser() → 后端返回 roles + permissions
3. authStore 存储 permissions
4. 路由跳转 → beforeEach 检查 route.meta.permission 是否在 permissions 中
5. 侧边栏渲染 → 根据 permissions 过滤菜单
6. 页面按钮 → v-permission 指令根据 permissions 控制显隐
7. API 调用 → 后端 @RequirePermission AOP 二次校验
```

## 8. 异常规则

| 场景 | 处理方式 |
|------|---------|
| 后端无权限访问 | 返回 403 `{ code: 403, message: "无操作权限" }` |
| 前端路由无权限 | 重定向到 `/403` |
| 前端按钮无权限 | 元素不渲染（从 DOM 移除） |
| 删除自己 | 返回 400 "不允许删除自己的账号" |
| 禁用自己 | 返回 400 "不允许禁用自己的账号" |
| 删除有用户的角色 | 返回 400 "该角色下有用户被分配，请先取消分配" |
| 删除 admin 用户 | 返回 400 "系统管理员账号不可删除" |
| 修改密码原密码错误 | 返回 400 "原密码不正确" |
| 用户名重复 | 返回 400 "用户名已存在" |
| 角色编码重复 | 返回 400 "角色编码已存在" |

## 9. 验收标准

| 编号 | 验收项 | 验证方式 |
|------|--------|---------|
| AC-01 | 系统管理员可新增用户 | POST /api/user 创建后可以登录 |
| AC-02 | 系统管理员可编辑用户信息 | PUT /api/user/{id} 后信息更新 |
| AC-03 | 系统管理员可禁用用户 | 禁用后用户无法登录 |
| AC-04 | 系统管理员可删除用户 | DELETE /api/user/{id} 后用户不存在 |
| AC-05 | 系统管理员可为用户分配角色 | 分配后 getCurrentUser 返回正确角色 |
| AC-06 | 用户可修改自己的密码 | 新密码可以登录 |
| AC-07 | 角色可新增/编辑/删除 | CRUD 操作正确 |
| AC-08 | 角色可配置权限 | 配置后该角色用户拥有对应权限 |
| AC-09 | 无权限调用 API 返回 403 | @RequirePermission AOP 生效 |
| AC-10 | 无权限页面不可访问 | 路由守卫重定向到 /403 |
| AC-11 | 无权限菜单不可见 | 侧边栏按权限过滤 |
| AC-12 | 无权限按钮不可见 | v-permission 指令生效 |
| AC-13 | 所有角色登录后权限正确 | 切换不同角色用户登录验证 |
| AC-14 | 后端构建通过 | mvn -DskipTests package |
| AC-15 | 前端构建通过 | npm run build |
| AC-16 | 前三个阶段功能不受影响 | 回归测试通过 |
