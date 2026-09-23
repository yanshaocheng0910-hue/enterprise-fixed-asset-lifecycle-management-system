-- Phase 15.1: 月度折旧趋势波动修复
-- 问题：depreciation_record 中 DEMO 资产每月 monthly_depreciation 总和相同（18672.72），导致趋势图是平线
-- 修复：删除 2025-08~2026-07 的 DEMO 记录，以 2025-07 原始月折旧额为基数按月乘以不同倍数重新插入
-- 目标月度折旧额：28000/31500/29800/34200/37600/33100/38900/42100/39700/44800/46300/49200
-- 幂等：每次执行都从 2025-07（未修改）复制基数重新生成，可重复执行
USE fixed_asset_lifecycle_system;
SET NAMES utf8mb4;

-- Step 1: 删除 DEMO 资产 2025-08 及之后的折旧记录（保留 2025-07 作为基数）
DELETE FROM depreciation_record
WHERE asset_id >= 1000 AND depreciation_month >= '2025-08';

-- Step 2: 按月插入带波动的折旧记录（基数来自 2025-07，乘以不同倍数）
-- 2025-08: 目标 28000, 倍数 1.4993
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2025-08', original_value, ROUND(monthly_depreciation * 1.4993, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2025-09: 目标 31500, 倍数 1.6870
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2025-09', original_value, ROUND(monthly_depreciation * 1.6870, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2025-10: 目标 29800, 倍数 1.5960
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2025-10', original_value, ROUND(monthly_depreciation * 1.5960, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2025-11: 目标 34200, 倍数 1.8316
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2025-11', original_value, ROUND(monthly_depreciation * 1.8316, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2025-12: 目标 37600, 倍数 2.0138
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2025-12', original_value, ROUND(monthly_depreciation * 2.0138, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-01: 目标 33100, 倍数 1.7729
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-01', original_value, ROUND(monthly_depreciation * 1.7729, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-02: 目标 38900, 倍数 2.0834
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-02', original_value, ROUND(monthly_depreciation * 2.0834, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-03: 目标 42100, 倍数 2.2547
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-03', original_value, ROUND(monthly_depreciation * 2.2547, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-04: 目标 39700, 倍数 2.1262
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-04', original_value, ROUND(monthly_depreciation * 2.1262, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-05: 目标 44800, 倍数 2.3993
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-05', original_value, ROUND(monthly_depreciation * 2.3993, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-06: 目标 46300, 倍数 2.4796
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-06', original_value, ROUND(monthly_depreciation * 2.4796, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- 2026-07: 目标 49200, 倍数 2.6348
INSERT INTO depreciation_record (asset_id, depreciation_month, original_value, monthly_depreciation, accumulated_depreciation, net_value)
SELECT asset_id, '2026-07', original_value, ROUND(monthly_depreciation * 2.6348, 2), 0, 0
FROM depreciation_record WHERE depreciation_month = '2025-07' AND asset_id >= 1000;

-- Step 3: 重新计算 accumulated_depreciation（从 2025-07 开始的累计月折旧额之和）
UPDATE depreciation_record r
JOIN (
    SELECT r1.id,
           (SELECT COALESCE(SUM(r2.monthly_depreciation), 0)
            FROM depreciation_record r2
            WHERE r2.asset_id = r1.asset_id
              AND r2.depreciation_month <= r1.depreciation_month
              AND r2.asset_id >= 1000) AS new_accum
    FROM depreciation_record r1
    WHERE r1.asset_id >= 1000
) calc ON r.id = calc.id
SET r.accumulated_depreciation = ROUND(calc.new_accum, 2);

-- Step 4: 重新计算 net_value = original_value - accumulated_depreciation（不低于 0）
UPDATE depreciation_record
SET net_value = GREATEST(ROUND(original_value - accumulated_depreciation, 2), 0)
WHERE asset_id >= 1000;

-- 验证：查看每月汇总
SELECT depreciation_month,
       COUNT(*) AS cnt,
       ROUND(SUM(monthly_depreciation), 2) AS total_monthly,
       ROUND(SUM(accumulated_depreciation), 2) AS total_acc,
       ROUND(SUM(net_value), 2) AS total_net
FROM depreciation_record
WHERE asset_id >= 1000
GROUP BY depreciation_month
ORDER BY depreciation_month;
