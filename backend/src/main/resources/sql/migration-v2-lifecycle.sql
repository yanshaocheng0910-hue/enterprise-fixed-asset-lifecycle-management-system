-- ============================================================
-- Phase 2 - Lifecycle Management Tables
-- Inbound / Receive / Transfer / Repair / Scrap orders
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------
-- 1. asset_inbound_order (入库单)
-- -------------------------------------------
DROP TABLE IF EXISTS asset_inbound_order;
CREATE TABLE asset_inbound_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_code          VARCHAR(64) NOT NULL UNIQUE COMMENT '入库单编号',
    asset_id            BIGINT NOT NULL COMMENT '资产ID',
    inbound_type        VARCHAR(32) DEFAULT NULL COMMENT '入库类型',
    supplier            VARCHAR(255) DEFAULT NULL COMMENT '供应商',
    purchase_order_no   VARCHAR(64) DEFAULT NULL COMMENT '采购单号',
    inbound_date        DATE DEFAULT NULL COMMENT '入库日期',
    handler             VARCHAR(64) DEFAULT NULL COMMENT '经办人',
    before_status       VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    after_status        VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '单据状态',
    remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by          BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_inbound_asset_id (asset_id),
    INDEX idx_inbound_order_code (order_code),
    INDEX idx_inbound_created_at (created_at)
) COMMENT='资产入库单表';

-- -------------------------------------------
-- 2. asset_receive_order (领用单)
-- -------------------------------------------
DROP TABLE IF EXISTS asset_receive_order;
CREATE TABLE asset_receive_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_code          VARCHAR(64) NOT NULL UNIQUE COMMENT '领用单编号',
    asset_id            BIGINT NOT NULL COMMENT '资产ID',
    receiver            VARCHAR(64) NOT NULL COMMENT '领用人',
    receiver_department VARCHAR(128) NOT NULL COMMENT '领用部门',
    receive_date        DATE NOT NULL COMMENT '领用日期',
    usage_purpose       VARCHAR(255) DEFAULT NULL COMMENT '使用用途',
    before_status       VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    after_status        VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '单据状态',
    remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by          BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_receive_asset_id (asset_id),
    INDEX idx_receive_order_code (order_code),
    INDEX idx_receive_created_at (created_at)
) COMMENT='资产领用单表';

-- -------------------------------------------
-- 3. asset_transfer_order (调拨单)
-- -------------------------------------------
DROP TABLE IF EXISTS asset_transfer_order;
CREATE TABLE asset_transfer_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_code          VARCHAR(64) NOT NULL UNIQUE COMMENT '调拨单编号',
    asset_id            BIGINT NOT NULL COMMENT '资产ID',
    from_department     VARCHAR(128) DEFAULT NULL COMMENT '调出部门',
    to_department       VARCHAR(128) NOT NULL COMMENT '调入部门',
    from_location       VARCHAR(255) DEFAULT NULL COMMENT '调出地点',
    to_location         VARCHAR(255) DEFAULT NULL COMMENT '调入地点',
    from_keeper         VARCHAR(64) DEFAULT NULL COMMENT '调出保管人',
    to_keeper           VARCHAR(64) NOT NULL COMMENT '调入保管人',
    transfer_date       DATE NOT NULL COMMENT '调拨日期',
    before_status       VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    after_status        VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '单据状态',
    remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by          BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_transfer_asset_id (asset_id),
    INDEX idx_transfer_order_code (order_code),
    INDEX idx_transfer_created_at (created_at)
) COMMENT='资产调拨单表';

-- -------------------------------------------
-- 4. asset_repair_order (维修单)
-- -------------------------------------------
DROP TABLE IF EXISTS asset_repair_order;
CREATE TABLE asset_repair_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_code          VARCHAR(64) NOT NULL UNIQUE COMMENT '维修单编号',
    asset_id            BIGINT NOT NULL COMMENT '资产ID',
    fault_description   VARCHAR(500) NOT NULL COMMENT '故障描述',
    repair_vendor       VARCHAR(128) DEFAULT NULL COMMENT '维修商',
    repair_cost         DECIMAL(18,2) DEFAULT NULL COMMENT '维修费用',
    repair_start_date   DATE DEFAULT NULL COMMENT '维修开始日期',
    repair_end_date     DATE DEFAULT NULL COMMENT '维修结束日期',
    repair_result       VARCHAR(32) DEFAULT NULL COMMENT '维修结果',
    before_status       VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    after_status        VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    status              VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '单据状态',
    remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by          BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_repair_asset_id (asset_id),
    INDEX idx_repair_order_code (order_code),
    INDEX idx_repair_created_at (created_at)
) COMMENT='资产维修单表';

-- -------------------------------------------
-- 5. asset_scrap_order (报废单)
-- -------------------------------------------
DROP TABLE IF EXISTS asset_scrap_order;
CREATE TABLE asset_scrap_order (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_code          VARCHAR(64) NOT NULL UNIQUE COMMENT '报废单编号',
    asset_id            BIGINT NOT NULL COMMENT '资产ID',
    scrap_reason        VARCHAR(500) NOT NULL COMMENT '报废原因',
    scrap_date          DATE NOT NULL COMMENT '报废日期',
    disposal_method     VARCHAR(64) DEFAULT NULL COMMENT '处置方式',
    residual_value      DECIMAL(18,2) DEFAULT NULL COMMENT '残值',
    before_status       VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    after_status        VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '单据状态',
    remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by          BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_scrap_asset_id (asset_id),
    INDEX idx_scrap_order_code (order_code),
    INDEX idx_scrap_created_at (created_at)
) COMMENT='资产报废单表';

SET FOREIGN_KEY_CHECKS = 1;
