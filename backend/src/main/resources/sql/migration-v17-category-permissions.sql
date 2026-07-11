-- Phase 17: 补全各角色的资产分类权限
-- 问题：ASSET_MANAGER 缺少 category:delete；DEPT_LEADER/OFFICE_STAFF/INVENTORY_CLERK 缺少 category:view
-- 幂等：使用 INSERT IGNORE，可重复执行
USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;

-- ASSET_MANAGER (role_id=2): 补充 category:delete (9)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (2, 9);

-- DEPT_LEADER (role_id=5): 补充 category:view (6)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (5, 6);

-- OFFICE_STAFF (role_id=6): 补充 category:view (6)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (6, 6);

-- INVENTORY_CLERK (role_id=7): 补充 category:view (6)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (7, 6);

-- 验证查询：各角色的分类权限
SELECT r.id AS role_id, r.role_code, r.role_name, rp.permission_id, p.permission_code
FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE p.module = '资产分类'
ORDER BY r.id, p.id;
