# 用户管理与 RBAC 权限系统 — 设计规格

> 日期: 2026-07-08  
> 状态: 待实现  
> 对应阶段: 第六阶段（用户管理与权限）

## 1. 概述

在现有认证骨架（JWT 登录、拦截器、ThreadLocal 上下文）之上，补齐完整的用户管理与 RBAC 权限系统，包括：

- 用户 CRUD（列表、新增、编辑、删除、启用/禁用）
- 角色管理（角色列表、新增、编辑、删除）
- 角色分配（为用户绑定/解绑角色）
- 权限矩阵（权限项定义、角色-权限关联配置）
- 路由级守卫（不同角色看到不同菜单和页面）
- 按钮级权限指令（`v-permission` 控制 UI 元素显隐）
- 后端方法级权限拦截（`@RequirePermission` 注解 + AOP）

## 2. 数据库设计

### 2.1 现有表（不变）

- `sys_user` — 系统用户表（id, username, password, real_name, department, phone, email, status）
- `sys_role` — 系统角色表（id, role_code, role_name, description）
- `sys_user_role` — 用户角色关联表（user_id, role_id, UK）

