-- ============================================================
-- Phase 10 - Finance Sync Enhancement
-- 为 finance_sync_record 表补充字段，支持完整的资产价值快照
-- 原因：原表仅有 sync_month/total_amount/record_count/status/remark/created_at，
-- 缺少批次号、资产数量、原值/净值/累计折旧快照、本月折旧额、操作人等字段，
-- 无法满足财务分析与模拟同步的完整数据记录需求。
-- ============================================================

USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;

ALTER TABLE finance_sync_record
    ADD COLUMN sync_batch_no VARCHAR(64) DEFAULT NULL COMMENT '同步批次号' AFTER sync_month,
    ADD COLUMN asset_count INT NOT NULL DEFAULT 0 COMMENT '资产数量' AFTER record_count,
    ADD COLUMN total_original_value DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '原值总额' AFTER total_amount,
    ADD COLUMN total_net_value DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '净值总额' AFTER total_original_value,
    ADD COLUMN total_accumulated_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '累计折旧总额' AFTER total_net_value,
    ADD COLUMN monthly_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '本月折旧额' AFTER total_accumulated_depreciation,
    ADD COLUMN operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人' AFTER status;
