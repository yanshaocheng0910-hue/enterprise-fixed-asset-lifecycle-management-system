-- ========================================
-- 第三阶段：审批流（幂等版本）
-- ========================================

USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 审批模板
CREATE TABLE IF NOT EXISTS approval_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    flow_code VARCHAR(64) NOT NULL UNIQUE COMMENT '审批模板编码',
    flow_name VARCHAR(128) NOT NULL COMMENT '审批模板名称',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型：RECEIVE/TRANSFER/REPAIR/SCRAP',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='审批模板表';

-- 审批节点
CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    flow_id BIGINT NOT NULL COMMENT '审批模板ID',
    node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    approver_role VARCHAR(64) NOT NULL COMMENT '审批角色编码',
    sort_order INT NOT NULL COMMENT '节点排序',
    required TINYINT NOT NULL DEFAULT 1 COMMENT '是否必须审批：1是 0否',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='审批节点表';

-- 审批实例
CREATE TABLE IF NOT EXISTS approval_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    business_id BIGINT NOT NULL COMMENT '业务单据ID',
    flow_id BIGINT NOT NULL COMMENT '审批模板ID',
    current_node_id BIGINT DEFAULT NULL COMMENT '当前待处理节点ID',
    status VARCHAR(32) NOT NULL COMMENT '审批状态：SUBMITTED/APPROVING/APPROVED/REJECTED',
    started_by BIGINT NOT NULL COMMENT '发起人用户ID',
    started_at DATETIME NOT NULL COMMENT '发起时间',
    completed_at DATETIME DEFAULT NULL COMMENT '全部完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_business (business_type, business_id),
    INDEX idx_status (status),
    INDEX idx_current_node (current_node_id)
) COMMENT='审批实例表';

-- 审批记录
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    instance_id BIGINT NOT NULL COMMENT '审批实例ID',
    node_id BIGINT NOT NULL COMMENT '审批节点ID',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人用户ID（SUBMIT 记录可为空）',
    approver_name VARCHAR(64) DEFAULT NULL COMMENT '审批人姓名（SUBMIT 记录可为空）',
    action VARCHAR(32) NOT NULL COMMENT '审批动作：SUBMIT/APPROVED/REJECTED',
    comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    status VARCHAR(32) NOT NULL COMMENT '审批后状态',
    approved_at DATETIME DEFAULT NULL COMMENT '审批时间（SUBMIT 记录可为空）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_instance (instance_id),
    INDEX idx_approver (approver_id)
) COMMENT='审批记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化审批模板（幂等：flow_code 唯一约束自动去重）
INSERT IGNORE INTO approval_flow (flow_code, flow_name, business_type, enabled, remark) VALUES
('APPROVE_RECEIVE', '领用审批', 'RECEIVE', 1, '资产领用审批，由部门负责人审批'),
('APPROVE_TRANSFER', '调拨审批', 'TRANSFER', 1, '资产调拨审批，由资产管理员审批'),
('APPROVE_REPAIR', '维修审批', 'REPAIR', 1, '资产维修审批，由资产管理员审批'),
('APPROVE_SCRAP', '报废审批', 'SCRAP', 1, '资产报废审批，两级审批：资产管理员+财务');

-- 初始化审批节点（幂等：先清空再插入，避免重复执行导致数据膨胀）
DELETE FROM approval_record;
DELETE FROM approval_instance;
DELETE FROM approval_node;

INSERT INTO approval_node (flow_id, node_code, node_name, approver_role, sort_order, required) VALUES
(1, 'DEPT_HEAD_APPROVE_RECEIVE', '部门负责人审批', 'ASSET_MANAGER', 1, 1),
(2, 'ASSET_MANAGER_APPROVE_TRANSFER', '资产管理员审批', 'ASSET_MANAGER', 1, 1),
(3, 'ASSET_MANAGER_APPROVE_REPAIR', '资产管理员审批', 'ASSET_MANAGER', 1, 1),
(4, 'ASSET_MANAGER_APPROVE_SCRAP', '资产管理员审批', 'ASSET_MANAGER', 1, 1),
(4, 'FINANCE_APPROVE_SCRAP', '财务人员审批', 'FINANCE', 2, 1);
