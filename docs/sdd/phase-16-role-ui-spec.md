# Phase 16 - 角色化演示账号与界面优化规格说明书

## 1. 背景

固定资产生命周期管理系统经过多期迭代，已实现以下完整功能模块：

- 资产台账管理
- 资产生命周期管理（领用 / 调拨 / 维修 / 报废）
- 审批中心（待办 / 已办 / 管理 / 审计）
- 资产时间线
- 盘点闭环管理
- 折旧财务核算
- 财务同步
- 预警中心
- AI 辅助分析
- Excel 导出
- 演示数据增强
- 全局审计日志
- 基础数据字典
- 部门 / 地点 / 保管人下拉
- 演示数据时间分布优化

**当前问题：**

1. 系统长期使用 `admin` 单账号进行演示，无法体现国企固定资产管理系统中真实的岗位分工与权责分离。
2. 前端界面在视觉层次、信息密度、状态标识等方面仍有优化空间，高频页面（资产列表、审批、盘点、折旧、财务、预警、审计）的体验可进一步提升。

## 2. 目标

1. 新增真实岗位型演示账号，覆盖国企固定资产管理典型岗位。
2. 实现不同角色的菜单差异与首页工作台差异。
3. 优化高频页面的视觉体验，包括状态标签、金额对齐、视觉层级等。
4. 保证原有功能链路完整、可用，不破坏既有 RBAC 结构。

## 3. 现有 RBAC 结构

### 3.1 数据表

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_user_role` | 用户角色关联表 |
| `sys_permission` | 权限表（34 项） |
| `sys_role_permission` | 角色权限关联表 |

### 3.2 现有角色

| 角色 | 编码 | ID |
| --- | --- | --- |
| 系统管理员 | ADMIN | 1 |
| 资产管理员 | ASSET_MANAGER | 2 |
| 财务 | FINANCE | 3 |
| 审计 | AUDITOR | 4 |

### 3.3 现有权限（34 项）

```
dashboard:view
asset:view / asset:create / asset:edit / asset:delete
category:view / category:create / category:edit / category:delete
inbound:create
receive:create
transfer:create
repair:create
scrap:create
inventory:view / inventory:create
depreciation:view
finance:view
ai:view
user:view / user:create / user:edit / user:delete / user:status / user:role
role:view / role:create / role:edit / role:delete / role:permission
approval:todo / approval:done / approval:manage / approval:audit
```

## 4. 新增账号

| 账号 | 姓名 | 岗位 | 部门 | 密码 |
| --- | --- | --- | --- | --- |
| `system.manager` | 系统管理员 | 系统管理员 | 信息中心 | 123456 |
| `asset.manager` | 张伟 | 资产管理员 | 资产管理部 | 123456 |
| `dept.leader` | 李娜 | 部门负责人 | 综合办公室 | 123456 |
| `finance.officer` | 陈敏 | 财务人员 | 财务部 | 123456 |
| `audit.officer` | 王强 | 审计人员 | 审计部 | 123456 |
| `office.staff` | 刘洋 | 普通员工 | 综合办公室 | 123456 |
| `inventory.clerk` | 赵磊 | 盘点人员 | 资产管理部 | 123456 |

> 原 `admin` 账号保留，密码保持不变。所有新增账号密码统一为 `123456`。

## 5. 新增角色

| 角色 | 编码 | ID |
| --- | --- | --- |
| 部门负责人 | DEPT_LEADER | 5 |
| 普通员工 | OFFICE_STAFF | 6 |
| 盘点人员 | INVENTORY_CLERK | 7 |

## 6. 角色权限设计

| 角色 | 可见菜单 / 功能 |
| --- | --- |
| `admin` / `system.manager` | 全部权限 |
| `asset.manager` | 首页 / 资产管理 / 生命周期 / 盘点 / 审批中心 / 预警 |
| `dept.leader` | 首页 / 审批中心 / 资产查看 / 待办 / 已办 |
| `finance.officer` | 首页 / 折旧 / 财务 / 预警 / Excel 导出 |
| `audit.officer` | 首页 / 审计 / 资产只读 / 时间线 / 审批记录 / 盘点 / 财务 / 预警 |
| `office.staff` | 首页 / 资产查看 / 领用 / 维修 / 审批进度 |
| `inventory.clerk` | 首页 / 盘点 / 资产只读 / 盘点异常 / 审计 |

## 7. 首页工作台差异化

| 角色 | 首页模块 |
| --- | --- |
| 系统管理员 | 总览 / 系统管理 / 预警 / 审计 / 财务 / 最近操作 |
| 资产管理员 | 资产总览 / 生命周期 / 盘点 / 预警 / 快捷操作 |
| 部门负责人 | 我的待办 / 本部门资产 / 审批趋势 |
| 财务人员 | 资产原值 / 净值 / 累计折旧 / 本月折旧 / 财务同步 / 低净值资产 |
| 审计人员 | 审计日志 / 盘点异常 / 审批记录 / 时间线 / 财务异常 |
| 普通员工 | 我的资产 / 我的申请 / 审批进度 / 维修入口 |
| 盘点人员 | 待盘点 / 已完成 / 异常记录 / 盘点进度 |

## 8. 开发边界

**允许修改：**

- 新增演示账号、角色、角色权限关联。
- 前端首页、布局、高频页面视觉优化。

**禁止修改：**

- 登录 / JWT 核心逻辑。
- 审批 / 生命周期 / 盘点 / 财务 / 预警 / AI / Excel / 审计主链路业务逻辑。
- 基础数据字典与下拉数据源。
- 不做移动端适配，不做深色模式。

## 9. 验收标准

1. 8 个账号（含 `admin`）均可成功登录。
2. 不同角色登录后看到的菜单不同。
3. 不同角色登录后首页工作台模块不同。
4. 访问无权限页面时跳转 403。
5. 折旧趋势图表 12 个月数据有明显变化。
6. 部门 / 地点 / 保管人下拉可用。
7. Excel 导出功能可用。
8. 后端 `mvn package` 与前端 `npm run build` 均构建通过。
