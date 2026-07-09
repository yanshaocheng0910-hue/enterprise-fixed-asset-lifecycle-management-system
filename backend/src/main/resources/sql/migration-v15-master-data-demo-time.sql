-- Phase 15 基础数据字典与演示数据时间分布优化（幂等，可重复执行）
USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、基础数据字典表
-- ============================================================

-- 部门字典表
CREATE TABLE IF NOT EXISTS base_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    department_code VARCHAR(64) NOT NULL UNIQUE COMMENT '部门编码',
    department_name VARCHAR(128) NOT NULL COMMENT '部门名称',
    manager_name VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='基础部门字典表';

-- 地点字典表
CREATE TABLE IF NOT EXISTS base_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    location_code VARCHAR(64) NOT NULL UNIQUE COMMENT '地点编码',
    location_name VARCHAR(128) NOT NULL COMMENT '地点名称',
    building VARCHAR(64) DEFAULT NULL COMMENT '楼栋',
    floor_no VARCHAR(32) DEFAULT NULL COMMENT '楼层',
    room_no VARCHAR(64) DEFAULT NULL COMMENT '房间号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='基础地点字典表';

-- 部门种子数据（10个，幂等）
INSERT IGNORE INTO base_department (id, department_code, department_name, manager_name, status, sort_order) VALUES
(1, 'DEPT-IT', '信息中心', '张伟', 1, 1),
(2, 'DEPT-ASSET', '资产管理部', '彭超', 1, 2),
(3, 'DEPT-FIN', '财务部', '何琳', 1, 3),
(4, 'DEPT-AUDIT', '审计部', '韩梅', 1, 4),
(5, 'DEPT-OFFICE', '综合办公室', '王涛', 1, 5),
(6, 'DEPT-EDU', '教务部', '孙磊', 1, 6),
(7, 'DEPT-LAB', '实验实训中心', '许慧', 1, 7),
(8, 'DEPT-LOG', '后勤保障部', '钱坤', 1, 8),
(9, 'DEPT-LIB', '图书馆', '陈静', 1, 9),
(10, 'DEPT-SEC', '保卫处', '胡军', 1, 10);

-- 地点种子数据（14个，幂等）
INSERT IGNORE INTO base_location (id, location_code, location_name, building, floor_no, room_no, status, sort_order) VALUES
(1, 'LOC-IT-ROOM', '信息中心机房', 'A座', '3层', '机房', 1, 1),
(2, 'LOC-ZH-301', '综合楼301', '综合楼', '3层', '301', 1, 2),
(3, 'LOC-ZH-502', '综合楼502', '综合楼', '5层', '502', 1, 3),
(4, 'LOC-SY-A201', '实验楼A201', '实验楼', '2层', 'A201', 1, 4),
(5, 'LOC-SY-B305', '实验楼B305', '实验楼', '3层', 'B305', 1, 5),
(6, 'LOC-LIB-1F', '图书馆一楼', '图书馆', '1层', '阅览区', 1, 6),
(7, 'LOC-LIB-3F', '图书馆三楼', '图书馆', '3层', '藏书区', 1, 7),
(8, 'LOC-XZ-MTG', '行政楼会议室', '行政楼', '2层', '会议室', 1, 8),
(9, 'LOC-LOG-WH', '后勤仓库', '后勤楼', '1层', '仓库', 1, 9),
(10, 'LOC-SEC-MON', '保卫处监控室', '保卫处', '1层', '监控室', 1, 10),
(11, 'LOC-FIN-ARC', '财务档案室', 'B座', '2层', '档案室', 1, 11),
(12, 'LOC-AST-WH', '资产仓库', 'C座', '1层', '仓库', 1, 12),
(13, 'LOC-EDU-MM', '教学楼多媒体教室', '教学楼', '5层', '多媒体教室', 1, 13),
(14, 'LOC-LAB-EQ', '实训中心设备间', '实训中心', '1层', '设备间', 1, 14);

-- ============================================================
-- 二、演示数据时间分布优化（仅影响 DEMO 前缀数据）
-- ============================================================

-- 2.1 入库单 created_at 分散到最近12个月（按ID分组）
UPDATE asset_inbound_order SET created_at = '2025-08-20 09:30:00', updated_at = '2025-08-20 09:30:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1004;
UPDATE asset_inbound_order SET created_at = '2025-09-15 10:15:00', updated_at = '2025-09-15 10:15:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1005 AND 1009;
UPDATE asset_inbound_order SET created_at = '2025-10-18 14:00:00', updated_at = '2025-10-18 14:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1010 AND 1014;
UPDATE asset_inbound_order SET created_at = '2025-11-12 11:20:00', updated_at = '2025-11-12 11:20:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1015 AND 1019;
UPDATE asset_inbound_order SET created_at = '2025-12-08 15:45:00', updated_at = '2025-12-08 15:45:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1020 AND 1024;
UPDATE asset_inbound_order SET created_at = '2026-01-10 09:00:00', updated_at = '2026-01-10 09:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1025 AND 1029;

-- 2.2 领用单 created_at 分散到最近12个月
UPDATE asset_receive_order SET created_at = '2025-09-05 10:00:00', updated_at = '2025-09-05 10:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1003;
UPDATE asset_receive_order SET created_at = '2025-10-10 11:30:00', updated_at = '2025-10-10 11:30:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1004 AND 1007;
UPDATE asset_receive_order SET created_at = '2025-11-15 14:20:00', updated_at = '2025-11-15 14:20:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1008 AND 1011;
UPDATE asset_receive_order SET created_at = '2025-12-20 09:45:00', updated_at = '2025-12-20 09:45:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1012 AND 1015;
UPDATE asset_receive_order SET created_at = '2026-01-08 15:10:00', updated_at = '2026-01-08 15:10:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1016 AND 1019;

-- 2.3 调拨单 created_at 分散到最近12个月
UPDATE asset_transfer_order SET created_at = '2025-08-25 10:30:00', updated_at = '2025-08-25 10:30:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1002;
UPDATE asset_transfer_order SET created_at = '2025-10-05 11:00:00', updated_at = '2025-10-05 11:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1003 AND 1005;
UPDATE asset_transfer_order SET created_at = '2025-11-20 14:15:00', updated_at = '2025-11-20 14:15:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1006 AND 1008;
UPDATE asset_transfer_order SET created_at = '2026-01-15 09:20:00', updated_at = '2026-01-15 09:20:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1009 AND 1011;

-- 2.4 维修单 created_at 分散到最近12个月
UPDATE asset_repair_order SET created_at = '2025-09-10 10:00:00', updated_at = '2025-09-10 10:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1002;
UPDATE asset_repair_order SET created_at = '2025-11-05 11:30:00', updated_at = '2025-11-05 11:30:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1003 AND 1005;
UPDATE asset_repair_order SET created_at = '2026-01-20 14:00:00', updated_at = '2026-01-20 14:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1006 AND 1008;
UPDATE asset_repair_order SET created_at = '2026-03-10 09:15:00', updated_at = '2026-03-10 09:15:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1009 AND 1011;

-- 2.5 报废单 created_at 分散到最近12个月
UPDATE asset_scrap_order SET created_at = '2025-08-15 10:00:00', updated_at = '2025-08-15 10:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1000 AND 1002;
UPDATE asset_scrap_order SET created_at = '2025-10-20 11:00:00', updated_at = '2025-10-20 11:00:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1003 AND 1005;
UPDATE asset_scrap_order SET created_at = '2025-12-10 14:30:00', updated_at = '2025-12-10 14:30:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1006 AND 1008;
UPDATE asset_scrap_order SET created_at = '2026-02-05 09:45:00', updated_at = '2026-02-05 09:45:00' WHERE order_code LIKE 'DEMO-%' AND id BETWEEN 1009 AND 1009;

-- 2.6 审批实例 created_at 参考 started_at（DEMO数据）
UPDATE approval_instance SET created_at = started_at, updated_at = COALESCE(completed_at, started_at) WHERE id >= 1000;

-- 2.7 审批记录 created_at 参考 approved_at 或实例 started_at
UPDATE approval_record ar
JOIN approval_instance ai ON ar.instance_id = ai.id
SET ar.created_at = COALESCE(ar.approved_at, ai.started_at)
WHERE ar.id >= 1000;

-- ============================================================
-- 三、扩展财务同步记录（补齐最近12个月，含2条FAILED）
-- ============================================================

-- 先清理旧的扩展数据（仅DEMO前缀且id>=1006）
DELETE FROM finance_sync_record WHERE sync_batch_no LIKE 'DEMO-%' AND id >= 1006;

-- 补齐 2025-08 到 2026-01 共6条（已有2026-02~07共6条）
INSERT IGNORE INTO finance_sync_record (id, sync_batch_no, sync_month, total_amount, record_count, asset_count, total_original_value, total_net_value, total_accumulated_depreciation, monthly_depreciation, status, operator_name, remark, created_at) VALUES
(1006, 'DEMO-FS-202508', '2025-08', 14203.55, 88, 102, 985234.12, 612345.78, 372888.34, 14203.55, 'SUCCESS', '系统管理员', '演示同步成功', '2025-08-28 16:20:00'),
(1007, 'DEMO-FS-202509', '2025-09', 14890.22, 95, 108, 1034567.89, 598234.56, 436333.33, 14890.22, 'SUCCESS', '系统管理员', '演示同步成功', '2025-09-27 14:10:00'),
(1008, 'DEMO-FS-202510', '2025-10', 15120.88, 97, 110, 1056789.45, 567890.12, 488899.33, 15120.88, 'SUCCESS', '系统管理员', '演示同步成功', '2025-10-29 10:45:00'),
(1009, 'DEMO-FS-202511', '2025-11', 14670.33, 91, 105, 998765.43, 545678.90, 453086.53, 14670.33, 'SUCCESS', '系统管理员', '演示同步成功', '2025-11-28 15:30:00'),
(1010, 'DEMO-FS-202512', '2025-12', 15580.67, 100, 115, 1078901.23, 523456.78, 555444.45, 15580.67, 'SUCCESS', '系统管理员', '演示同步成功', '2025-12-29 11:15:00'),
(1011, 'DEMO-FS-202601', '2026-01', 15890.44, 101, 116, 1098765.34, 501234.56, 597530.78, 15890.44, 'FAILED', '系统管理员', '同步失败：财务接口连接超时', '2026-01-30 09:50:00');

-- ============================================================
-- 四、扩展盘点任务（覆盖2025-Q4和2026-Q1）
-- ============================================================

-- 先清理旧的扩展盘点任务（仅DEMO前缀且id>=1004）
DELETE FROM inventory_record WHERE task_id IN (SELECT id FROM inventory_task WHERE task_code LIKE 'DEMO-%' AND id >= 1004);
DELETE FROM inventory_task WHERE task_code LIKE 'DEMO-%' AND id >= 1004;

-- 新增3条盘点任务覆盖2025-Q4、2026-Q1、2026-Q2
INSERT IGNORE INTO inventory_task (id, task_code, task_name, scope_type, department, location, status, start_time, end_time, created_by, created_at, updated_at) VALUES
(1004, 'DEMO-IV-202510-001', '2025年10月后勤保障部盘点', 'DEPARTMENT', '后勤保障部', '后勤仓库', 'COMPLETED', '2025-10-15 08:00:00', '2025-10-20 17:00:00', 1, '2025-10-14 14:00:00', '2025-10-20 17:00:00'),
(1005, 'DEMO-IV-202602-001', '2026年2月图书馆盘点', 'DEPARTMENT', '图书馆', '图书馆一楼', 'COMPLETED', '2026-02-10 09:00:00', '2026-02-15 16:00:00', 1, '2026-02-09 15:00:00', '2026-02-15 16:00:00'),
(1006, 'DEMO-IV-202604-001', '2026年4月保卫处盘点', 'DEPARTMENT', '保卫处', '保卫处监控室', 'COMPLETED', '2026-04-05 08:30:00', '2026-04-10 17:30:00', 1, '2026-04-04 10:00:00', '2026-04-10 17:30:00');

-- 为新增盘点任务补充几条盘点明细（简化，仅展示异常和正常各几条）
INSERT IGNORE INTO inventory_record (id, task_id, asset_id, expected_location, actual_location, expected_keeper, actual_keeper, result, scanned_at, remark) VALUES
(1105, 1004, 1018, 'A座2层文印室', 'A座2层文印室', '沈月', '沈月', 'NORMAL', '2025-10-16 10:20:00', '演示盘点'),
(1106, 1004, 1019, '仓库A区', '仓库B区', '彭超', '彭超', 'LOCATION_MISMATCH', '2025-10-17 11:15:00', '演示盘点'),
(1107, 1004, 1020, 'B座2层财务室', 'B座2层财务室', '孙磊', '孙磊', 'NORMAL', '2025-10-18 14:30:00', '演示盘点'),
(1108, 1004, 1021, '二号车间', '二号车间', '钱坤', '韩梅', 'KEEPER_MISMATCH', '2025-10-19 09:45:00', '演示盘点'),
(1109, 1005, 1040, '保卫处监控室', '保卫处监控室', '张伟', '张伟', 'NORMAL', '2026-02-11 10:00:00', '演示盘点'),
(1110, 1005, 1041, 'B座3层培训室', 'B座3层培训室', '周芳', '周芳', 'NORMAL', '2026-02-12 11:30:00', '演示盘点'),
(1111, 1005, 1042, 'B座3层培训室', NULL, '周芳', NULL, 'LOST', '2026-02-13 14:15:00', '演示盘点'),
(1112, 1005, 1047, 'A座4层研发室', 'A座4层研发室', '张伟', '张伟', 'NORMAL', '2026-02-14 09:20:00', '演示盘点'),
(1113, 1006, 1110, 'A座3层机房', 'A座3层机房', '胡军', '胡军', 'NORMAL', '2026-04-06 10:15:00', '演示盘点'),
(1114, 1006, 1114, 'C座2层办公室', 'C座2层办公室', '钱坤', '钱坤', 'NORMAL', '2026-04-07 11:00:00', '演示盘点'),
(1115, 1006, 1117, '数据中心1机柜', '数据中心2机柜', '孙磊', '孙磊', 'LOCATION_MISMATCH', '2026-04-08 14:45:00', '演示盘点'),
(1116, 1006, 1118, 'B座3层培训室', 'B座3层培训室', '胡军', '胡军', 'NORMAL', '2026-04-09 09:30:00', '演示盘点');

SET FOREIGN_KEY_CHECKS = 1;
