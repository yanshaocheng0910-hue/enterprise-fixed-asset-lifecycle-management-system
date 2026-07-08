-- 第五阶段：用户管理与RBAC权限
-- 新增 sys_permission（权限项表）和 sys_role_permission（角色权限关联表）

DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    permission_code VARCHAR(64) NOT NULL UNIQUE COMMENT '权限编码，如 asset:create',
    permission_name VARCHAR(64) NOT NULL COMMENT '权限名称，如新增资产',
    module VARCHAR(32) NOT NULL COMMENT '所属模块',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='权限项表';

DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) COMMENT='角色权限关联表';

-- 34个权限项种子数据
INSERT INTO sys_permission (id, permission_code, permission_name, module, description) VALUES
-- 首页 (1)
( 1, 'dashboard:view',       '查看驾驶舱', '首页',       '查看首页驾驶舱'),
-- 资产管理 (4)
( 2, 'asset:view',           '查看资产',   '资产管理',   '查看资产台账列表与详情'),
( 3, 'asset:create',         '新增资产',   '资产管理',   '新增资产记录'),
( 4, 'asset:edit',           '编辑资产',   '资产管理',   '编辑资产信息'),
( 5, 'asset:delete',         '删除资产',   '资产管理',   '逻辑删除资产'),
-- 资产分类 (4)
( 6, 'category:view',        '查看分类',   '资产分类',   '查看资产分类树与列表'),
( 7, 'category:create',      '新增分类',   '资产分类',   '新增资产分类'),
( 8, 'category:edit',        '编辑分类',   '资产分类',   '编辑资产分类'),
( 9, 'category:delete',      '删除分类',   '资产分类',   '删除资产分类'),
-- 生命周期 (5)
(10, 'inbound:create',       '资产入库',   '生命周期',   '创建资产入库单'),
(11, 'receive:create',       '资产领用',   '生命周期',   '创建资产领用单'),
(12, 'transfer:create',      '资产调拨',   '生命周期',   '创建资产调拨单'),
(13, 'repair:create',        '维修管理',   '生命周期',   '创建维修单并完成维修'),
(14, 'scrap:create',         '报废管理',   '生命周期',   '创建报废单'),
-- 盘点管理 (2)
(15, 'inventory:view',       '查看盘点',   '盘点管理',   '查看盘点任务与明细'),
(16, 'inventory:create',     '创建盘点',   '盘点管理',   '创建盘点任务'),
-- 折旧报表 (1)
(17, 'depreciation:view',    '查看折旧',   '折旧报表',   '查看折旧报表'),
-- 财务对接 (1)
(18, 'finance:view',         '查看财务',   '财务对接',   '查看财务对接数据'),
-- AI分析 (1)
(19, 'ai:view',              '查看AI分析', 'AI分析',    '查看AI智能分析'),
-- 用户管理 (6)
(20, 'user:view',            '查看用户',   '用户管理',   '查看用户列表'),
(21, 'user:create',          '新增用户',   '用户管理',   '新增系统用户'),
(22, 'user:edit',            '编辑用户',   '用户管理',   '编辑用户信息'),
(23, 'user:delete',          '删除用户',   '用户管理',   '删除系统用户'),
(24, 'user:status',          '启停用户',   '用户管理',   '启用或禁用用户'),
(25, 'user:role',            '分配角色',   '用户管理',   '为用户分配或取消角色'),
-- 角色管理 (5)
(26, 'role:view',            '查看角色',   '角色管理',   '查看角色列表'),
(27, 'role:create',          '新增角色',   '角色管理',   '新增系统角色'),
(28, 'role:edit',            '编辑角色',   '角色管理',   '编辑角色信息'),
(29, 'role:delete',          '删除角色',   '角色管理',   '删除系统角色'),
(30, 'role:permission',      '配置权限',   '角色管理',   '配置角色的操作权限'),
-- 审批管理 (4)
(31, 'approval:todo',        '我的待办',   '审批管理',   '查看和处理待审批项'),
(32, 'approval:done',        '我的已办',   '审批管理',   '查看已处理的审批记录'),
(33, 'approval:manage',      '管理审批',   '审批管理',   '审批配置管理'),
(34, 'approval:audit',       '审批审计',   '审批管理',   '查看全部审批记录');

-- 默认角色-权限分配
-- ADMIN (role_id=1): 全部34项
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9),(1,10),
(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),
(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),
(1,31),(1,32),(1,33),(1,34);

-- ASSET_MANAGER (role_id=2): 18项
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),(2, 2),(2, 3),(2, 4),(2, 5),(2, 6),(2, 7),(2, 8),(2,10),(2,11),
(2,12),(2,13),(2,14),(2,15),(2,16),(2,31),(2,32),(2,33);

-- FINANCE (role_id=3): 8项
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1),(3, 2),(3, 6),(3,14),(3,17),(3,18),(3,31),(3,32);

-- AUDITOR (role_id=4): 11项（纯只读）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 1),(4, 2),(4, 6),(4,15),(4,17),(4,18),(4,19),(4,20),(4,26),(4,32),(4,34);
