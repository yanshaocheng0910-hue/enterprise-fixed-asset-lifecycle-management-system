# Phase 13 - 演示数据增强与全场景样本补齐 Design

## 1. 编号规则

| 数据类型 | 编号格式 | 示例 |
|----------|----------|------|
| 资产编号 | DEMO-{分类码}-{序号} | DEMO-IT-0001 |
| 入库单 | DEMO-IB-{年月}-{序号} | DEMO-IB-2401-001 |
| 领用单 | DEMO-RC-{年月}-{序号} | DEMO-RC-2403-001 |
| 调拨单 | DEMO-TF-{年月}-{序号} | DEMO-TF-2406-001 |
| 维修单 | DEMO-RP-{年月}-{序号} | DEMO-RP-2409-001 |
| 报废单 | DEMO-SP-{年月}-{序号} | DEMO-SP-2412-001 |
| 盘点任务 | DEMO-IV-{年月}-{序号} | DEMO-IV-2506-001 |
| 审批实例 | 关联业务单据 ID | — |

## 2. 资产分类分布（10 类）

| ID | 编码 | 名称 | 折旧年限 |
|----|------|------|----------|
| 1 | OFFICE | 办公设备 | 5 |
| 2 | ELECTRONIC | 电子设备 | 4 |
| 3 | VEHICLE | 运输设备 | 8 |
| 4 | PRODUCTION | 生产设备 | 10 |
| 5 | BUILDING | 房屋及建筑物 | 20 |
| 6 | NETWORK | 网络设备 | 5 |
| 7 | SERVER | 服务器设备 | 5 |
| 8 | SECURITY | 安防设备 | 8 |
| 9 | TEACHING | 教学设备 | 8 |
| 10 | LAB | 实验仪器 | 10 |

## 3. 部门分布（10 个）

信息中心、资产管理部、财务部、审计部、综合办公室、教务部、实验实训中心、后勤保障部、图书馆、保卫处

## 4. 资产价值梯度

| 梯度 | 价值范围 | 数量占比 |
|------|----------|----------|
| 低值 | 500–3000 元 | 25% |
| 中值 | 3000–30000 元 | 50% |
| 高值 | 30000–300000 元 | 20% |
| 大型 | 300000+ 元 | 5% |

## 5. 设备名称多样化

联想 ThinkCentre 台式机、Dell OptiPlex 台式机、MacBook Pro、ThinkPad 笔记本、HP 打印机、佳能复印机、华为交换机、H3C 核心交换机、Dell 服务器、浪潮服务器、群晖 NAS、防火墙、UPS 电源、会议一体机、投影仪、监控摄像头、门禁控制器、示波器、频谱分析仪、3D 打印机、实验台、空调、发电机、叉车、公务车、办公桌、文件柜、会议桌

## 6. SQL 文件设计

### migration-v13-demo-data.sql

```sql
-- 1. 幂等清理：删除 DEMO 前缀数据
DELETE FROM inventory_record WHERE task_id IN (SELECT id FROM inventory_task WHERE task_code LIKE 'DEMO-%');
DELETE FROM inventory_task WHERE task_code LIKE 'DEMO-%';
DELETE FROM approval_record WHERE instance_id IN (SELECT id FROM approval_instance WHERE business_id IN (SELECT id FROM asset_repair_order WHERE order_code LIKE 'DEMO-%'));
DELETE FROM approval_instance WHERE business_id IN (SELECT id FROM asset_repair_order WHERE order_code LIKE 'DEMO-%');
-- ... 类似清理其他 DEMO 数据
DELETE FROM asset_scrap_order WHERE order_code LIKE 'DEMO-%';
DELETE FROM asset_repair_order WHERE order_code LIKE 'DEMO-%';
DELETE FROM asset_transfer_order WHERE order_code LIKE 'DEMO-%';
DELETE FROM asset_receive_order WHERE order_code LIKE 'DEMO-%';
DELETE FROM asset_inbound_order WHERE order_code LIKE 'DEMO-%';
DELETE FROM asset_operation_log WHERE asset_id IN (SELECT id FROM asset WHERE asset_code LIKE 'DEMO-%');
DELETE FROM depreciation_record WHERE asset_id IN (SELECT id FROM asset WHERE asset_code LIKE 'DEMO-%');
DELETE FROM finance_sync_record WHERE sync_batch_no LIKE 'DEMO-%' OR sync_month LIKE 'DEMO-%';
DELETE FROM asset WHERE asset_code LIKE 'DEMO-%';

-- 2. 补充分类（INSERT IGNORE）
-- 3. 插入资产（120 条）
-- 4. 插入操作日志
-- 5. 插入生命周期单据
-- 6. 插入审批数据
-- 7. 插入盘点数据
-- 8. 插入财务同步数据
-- 9. 插入折旧记录
```

## 7. 文件清单

### 新增文件（6）

- docs/sdd/phase-13-demo-data-spec.md
- docs/sdd/phase-13-demo-data-design.md
- docs/sdd/phase-13-demo-data-tasks.md
- docs/sdd/phase-13-demo-data-acceptance.md
- backend/src/main/resources/sql/migration-v13-demo-data.sql
- docs/demo-data-guide.md
