# Phase 16 - 角色化演示账号与界面优化设计文档

## 1. 技术架构

### 1.1 前端

- Vue 3（Composition API）
- Element Plus（UI 组件库）
- Pinia（状态管理，存储用户 / 角色 / 权限信息）
- ECharts（图表渲染，折旧趋势 / 审批趋势 / 财务概览）

### 1.2 后端

- Spring Boot
- MyBatis-Plus
- 现有 RBAC 鉴权体系（基于 `@PreAuthorize` 与自定义权限注解）
- 现有登录接口返回 `roles` 与 `permissions` 数组

## 2. 数据库设计

### 2.1 新增角色

| ID | 编码 | 名称 |
| --- | --- | --- |
| 5 | `DEPT_LEADER` | 部门负责人 |
| 6 | `OFFICE_STAFF` | 普通员工 |
| 7 | `INVENTORY_CLERK` | 盘点人员 |

### 2.2 新增用户（7 个）

`system.manager` / `asset.manager` / `dept.leader` / `finance.officer` / `audit.officer` / `office.staff` / `inventory.clerk`

### 2.3 角色权限关联

为 7 个角色分别建立与现有 34 项权限的关联关系（详见第 3 节矩阵）。

### 2.4 SQL 文件

- 文件名：`migration-v16-demo-roles.sql`
- 执行位置：`src/main/resources/db/migration/`
- 幂等设计：
  - 角色与用户使用 `INSERT IGNORE` 插入。
  - 角色 - 权限关联采用 `DELETE FROM sys_role_permission WHERE role_id IN (...) ` 后重新插入的方式，保证可重复执行。

## 3. 角色权限矩阵表

权限项缩写说明：D=dashboard:view，AV=asset:view，AC=asset:create，AE=asset:edit，AD=asset:delete，
CV=category:view，CC/CE/CD 同理，IB=inbound:create，RC=receive:create，TC=transfer:create，
RP=repair:create，SC=scrap:create，IV=inventory:view，IC=inventory:create，DP=depreciation:view，
FV=finance:view，AI=ai:view，UV=user:view 等，RV=role:view 等，AT=approval:todo，ADn=approval:done，
AM=approval:manage，AA=approval:audit。

| 权限 | admin | system.manager | asset.manager | dept.leader | finance.officer | audit.officer | office.staff | inventory.clerk |
| --- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| dashboard:view | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| asset:view | ✓ | ✓ | ✓ | ✓ |  | ✓ | ✓ | ✓ |
| asset:create | ✓ | ✓ | ✓ |  |  |  |  |  |
| asset:edit | ✓ | ✓ | ✓ |  |  |  |  |  |
| asset:delete | ✓ | ✓ | ✓ |  |  |  |  |  |
| category:view | ✓ | ✓ | ✓ |  |  | ✓ |  |  |
| category:create | ✓ | ✓ | ✓ |  |  |  |  |  |
| category:edit | ✓ | ✓ | ✓ |  |  |  |  |  |
| category:delete | ✓ | ✓ | ✓ |  |  |  |  |  |
| inbound:create | ✓ | ✓ | ✓ |  |  |  |  |  |
| receive:create | ✓ | ✓ | ✓ |  |  |  | ✓ |  |
| transfer:create | ✓ | ✓ | ✓ |  |  |  |  |  |
| repair:create | ✓ | ✓ | ✓ |  |  |  | ✓ |  |
| scrap:create | ✓ | ✓ | ✓ |  |  |  |  |  |
| inventory:view | ✓ | ✓ | ✓ |  |  | ✓ |  | ✓ |
| inventory:create | ✓ | ✓ | ✓ |  |  |  |  | ✓ |
| depreciation:view | ✓ | ✓ |  |  | ✓ | ✓ |  |  |
| finance:view | ✓ | ✓ |  |  | ✓ | ✓ |  |  |
| ai:view | ✓ | ✓ | ✓ |  |  | ✓ |  |  |
| user:view | ✓ | ✓ |  |  |  |  |  |  |
| user:create | ✓ | ✓ |  |  |  |  |  |  |
| user:edit | ✓ | ✓ |  |  |  |  |  |  |
| user:delete | ✓ | ✓ |  |  |  |  |  |  |
| user:status | ✓ | ✓ |  |  |  |  |  |  |
| user:role | ✓ | ✓ |  |  |  |  |  |  |
| role:view | ✓ | ✓ |  |  |  |  |  |  |
| role:create | ✓ | ✓ |  |  |  |  |  |  |
| role:edit | ✓ | ✓ |  |  |  |  |  |  |
| role:delete | ✓ | ✓ |  |  |  |  |  |  |
| role:permission | ✓ | ✓ |  |  |  |  |  |  |
| approval:todo | ✓ | ✓ | ✓ | ✓ |  |  |  |  |
| approval:done | ✓ | ✓ | ✓ | ✓ |  | ✓ | ✓ |  |
| approval:manage | ✓ | ✓ | ✓ |  |  |  |  |  |
| approval:audit | ✓ | ✓ |  |  |  | ✓ |  |  |

## 4. 后端设计

**无需修改后端代码。**

- 现有登录接口已返回 `roles` 与 `permissions` 数组。
- 现有 `@PreAuthorize` / 自定义权限注解不变。
- 仅需执行 `migration-v16-demo-roles.sql` 写入角色、用户、关联数据。

## 5. 前端文件清单

### 5.1 新增文件

| 文件 | 说明 |
| --- | --- |
| `frontend/src/composables/useRoleDashboard.ts` | 角色首页 composable，根据角色返回模块配置 |
| `frontend/src/components/QuickActionCard.vue` | 快捷操作卡片组件，用于首页快捷入口 |

### 5.2 修改文件

| 文件 | 修改点 |
| --- | --- |
| `frontend/src/views/Dashboard.vue` | 改造为角色差异化首页，按角色渲染不同模块组合 |
| `frontend/src/layouts/MainLayout.vue` | 顶部显示当前用户角色与部门 |
| `frontend/src/views/asset/AssetList.vue` | 筛选条件收紧、状态标签着色、金额右对齐 |
| `frontend/src/views/approval/ApprovalTodo.vue` | 状态标签、业务类型标签 |
| `frontend/src/views/approval/ApprovalDone.vue` | 状态标签、业务类型标签 |
| `frontend/src/views/inventory/InventoryTask.vue` | 突出任务状态和盘点进度 |
| `frontend/src/views/depreciation/DepreciationReport.vue` | 图表标题和说明文案 |
| `frontend/src/views/finance/FinanceSync.vue` | 同步状态视觉强化 |
| `frontend/src/views/warning/WarningCenter.vue` | 预警等级视觉层级 |
| `frontend/src/views/audit/AuditLog.vue` | 日志类型和时间展示优化 |

## 6. 角色首页设计

### 6.1 角色判定逻辑

```ts
// 基于 authStore.userInfo.roles 数组判断角色
const roleCodes = authStore.userInfo.roles.map(r => r.code)
const isAdmin = roleCodes.includes('ADMIN')
const isAssetManager = roleCodes.includes('ASSET_MANAGER')
// ...其他角色同理
```

### 6.2 模块组合

通过 `useRoleDashboard.ts` 返回当前角色应渲染的模块列表，`Dashboard.vue` 根据该列表动态加载对应的 ECharts 图表与统计卡片。

### 6.3 快捷操作

`QuickActionCard.vue` 接收 `title`、`icon`、`path` 属性，资产管理员首页展示「新增资产 / 发起盘点 / 发起调拨 / 发起维修」等快捷入口。

## 7. 幂等性设计

- `sys_role` 与 `sys_user` 使用 `INSERT IGNORE`，依据 `id` 或 `username` 唯一约束避免重复。
- `sys_role_permission` 先 `DELETE FROM sys_role_permission WHERE role_id IN (1,2,3,4,5,6,7)`，再批量 `INSERT`，保证多次执行结果一致。
- `sys_user_role` 同理，先按 `user_id` 删除再插入。

## 8. 权限设计

- **复用现有 34 个权限项，不新增权限项。**
- 不修改 `sys_permission` 表结构。
- 角色权限差异完全通过 `sys_role_permission` 关联表实现。

## 9. 兼容性

- 原 `admin` 账号权限与菜单保持不变。
- 现有 `ASSET_MANAGER` / `FINANCE` / `AUDITOR` 角色权限保持不变（仅补充与新角色的区分）。
- 前端路由守卫逻辑不变，仅依据 `permissions` 数组控制页面访问。
