-- ============================================================
-- Phase 4 - Finance Sync Table
-- ============================================================

USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS finance_sync_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sync_month      VARCHAR(7) NOT NULL COMMENT '同步月份，格式 YYYY-MM',
    total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '同步折旧总额',
    record_count    INT NOT NULL DEFAULT 0 COMMENT '同步记录数',
    status          VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '同步状态：SUCCESS/FAILED',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
    INDEX idx_sync_month (sync_month),
    INDEX idx_created_at (created_at)
) COMMENT='财务同步记录表';

SET FOREIGN_KEY_CHECKS = 1;