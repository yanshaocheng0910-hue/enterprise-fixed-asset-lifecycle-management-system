-- Phase 16: 多角色演示账号与前端界面体验优化
-- 新增 3 个角色（DEPT_LEADER/OFFICE_STAFF/INVENTORY_CLERK）和 7 个岗位型演示账号
-- 幂等：使用 INSERT IGNORE 和 DELETE+重新插入确保可重复执行
-- 不破坏已有 admin 账号和现有角色/权限
USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;

-- ===== Step 1: 新增 3 个角色（幂等）=====
INSERT IGNORE INTO sys_role (id, role_code, role_name, description, created_at, updated_at) VALUES
(5, 'DEPT_LEADER',     '部门负责人', '部门审批与资产查看',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'OFFICE_STAFF',    '普通员工',   '资产领用与维修申请',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'INVENTORY_CLERK', '盘点人员',   '盘点任务执行与记录',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Step 2: 新增 7 个岗位型演示账号（幂等）=====
-- 密码统一为 123456，hash 与 admin 相同
INSERT IGNORE INTO sys_user (id, username, password, real_name, department, phone, email, status, created_at, updated_at) VALUES
(10, 'system.manager',  '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '系统管理员', '信息中心',   '13900000001', 'system.manager@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'asset.manager',   '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '张伟',       '资产管理部', '13900000002', 'asset.manager@example.com',  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'dept.leader',     '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '李娜',       '综合办公室', '13900000003', 'dept.leader@example.com',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'finance.officer', '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '陈敏',       '财务部',     '13900000004', 'finance.officer@example.com',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'audit.officer',   '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '王强',       '审计部',     '13900000005', 'audit.officer@example.com',  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'office.staff',    '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '刘洋',       '综合办公室', '13900000006', 'office.staff@example.com',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'inventory.clerk', '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '赵磊',       '资产管理部', '13900000007', 'inventory.clerk@example.com',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Step 3: 用户-角色关联（幂等，先清理再插入）=====
DELETE FROM sys_user_role WHERE user_id IN (10, 11, 12, 13, 14, 15, 16);
INSERT INTO sys_user_role (user_id, role_id) VALUES
-- system.manager -> ADMIN (全部权限)
(10, 1),
-- asset.manager -> ASSET_MANAGER
(11, 2),
-- dept.leader -> DEPT_LEADER
(12, 5),
-- finance.officer -> FINANCE
(13, 3),
-- audit.officer -> AUDITOR
(14, 4),
-- office.staff -> OFFICE_STAFF
(15, 6),
-- inventory.clerk -> INVENTORY_CLERK
(16, 7);

-- ===== Step 4: 新角色的权限分配（幂等，先清理再插入）=====

-- DEPT_LEADER (role_id=5): 首页、资产查看、待办、已办
DELETE FROM sys_role_permission WHERE role_id = 5;
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(5, 1),   -- dashboard:view
(5, 2),   -- asset:view
(5, 31),  -- approval:todo
(5, 32);  -- approval:done

-- OFFICE_STAFF (role_id=6): 首页、资产查看、领用、维修、待办、已办
DELETE FROM sys_role_permission WHERE role_id = 6;
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(6, 1),   -- dashboard:view
(6, 2),   -- asset:view
(6, 11),  -- receive:create
(6, 13),  -- repair:create
(6, 31),  -- approval:todo
(6, 32);  -- approval:done

-- INVENTORY_CLERK (role_id=7): 首页、资产查看、盘点查看、盘点创建、审计
DELETE FROM sys_role_permission WHERE role_id = 7;
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(7, 1),   -- dashboard:view
(7, 2),   -- asset:view
(7, 15),  -- inventory:view
(7, 16),  -- inventory:create
(7, 34);  -- approval:audit

-- ===== 验证查询 =====
SELECT u.id, u.username, u.real_name, u.department, r.role_code, r.role_name
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.id
JOIN sys_role r ON r.id = ur.role_id
WHERE u.id >= 10
ORDER BY u.id;
