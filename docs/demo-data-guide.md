# 演示数据说明文档

## 1. 数据覆盖范围

Phase 13 演示数据覆盖系统全部业务模块，包括：

| 数据类型 | 数量 | 说明 |
|----------|------|------|
| 演示资产 | 120 条 | DEMO 前缀，覆盖 7 种状态 |
| 资产分类 | 10 类 | 含网络设备、服务器、安防等 |
| 部门 | 10 个 | 覆盖信息中心、财务部等 |
| 入库单 | 30 条 | COMPLETED 状态 |
| 领用单 | 20 条 | COMPLETED 状态 |
| 调拨单 | 12 条 | COMPLETED 状态 |
| 维修单 | 12 条 | 含 5 条超期（DRAFT） |
| 报废单 | 10 条 | COMPLETED 状态 |
| 操作日志 | 120 条 | 每条资产 1 条入库日志 |
| 审批实例 | 20 条 | APPROVING/APPROVED/REJECTED |
| 审批记录 | 35 条 | SUBMIT/APPROVED/REJECTED |
| 盘点任务 | 4 个 | PENDING/IN_PROGRESS/COMPLETED |
| 盘点明细 | 105 条 | NORMAL/LOCATION_MISMATCH/KEEPER_MISMATCH/LOST/EXTRA |
| 财务同步 | 6 条 | SUCCESS(5)/FAILED(1) |
| 折旧记录 | 360 条 | 30 条资产 × 12 个月 |

## 2. 资产状态分布

| 状态 | 数量 | 说明 |
|------|------|------|
| IN_USE | 55 | 在用 |
| IDLE | 25 | 闲置（含长期闲置 >90 天） |
| TRANSFERRING | 5 | 调拨中 |
| REPAIRING | 8 | 维修中（含超期 >30 天） |
| WAITING_SCRAP | 7 | 待报废 |
| SCRAPPED | 10 | 已报废 |
| INVENTORY_ABNORMAL | 10 | 盘点异常 |

## 3. 执行方法

### 方法 1：Python pymysql

```python
import pymysql
conn = pymysql.connect(host='localhost', port=3306, user='root',
    password='123456', database='fixed_asset_lifecycle_system', charset='utf8mb4', autocommit=True)
with open('backend/src/main/resources/sql/migration-v13-demo-data.sql', 'r', encoding='utf-8') as f:
    sql = f.read()
# 按分号拆分并逐条执行
cursor = conn.cursor()
for stmt in sql.split(';'):
    stmt = stmt.strip()
    if stmt and not stmt.startswith('--'):
        cursor.execute(stmt)
conn.commit()
```

### 方法 2：MySQL 客户端

```bash
mysql -u root -p123456 fixed_asset_lifecycle_system < backend/src/main/resources/sql/migration-v13-demo-data.sql
```

SQL 文件幂等，可重复执行。

## 4. 推荐演示路径

1. **登录系统**（admin / 123456）
2. **资产台账**：查看 120 条演示资产，按状态/部门筛选，点击导出 Excel
3. **资产详情**：点击任意资产，查看生命周期时间线
4. **审批中心**：查看"我的待办"（5 条待审批）和"我的已办"
5. **盘点管理**：查看 4 个盘点任务，点击明细查看异常数据
6. **折旧报表**：查看月度明细、部门/分类统计、低净值资产、接近报废资产
7. **财务同步**：查看 6 条同步记录（含 1 条失败），点击"模拟同步"
8. **预警中心**：查看 6 种预警类型（低净值、接近年限、长期闲置、维修超期、盘点异常、财务同步异常）
9. **AI 分析**：查看资产总览、异常告警、处置建议
10. **Excel 导出**：在各页面点击导出按钮，下载 xlsx 文件

## 5. 10 个典型样本资产

| 序号 | 资产编号 | 特点 | 演示要点 |
|------|----------|------|----------|
| 1 | DEMO-EL-0001 | 低净值资产 | 净值率 < 20%，触发低净值预警 |
| 2 | DEMO-EL-0011 | 接近使用年限 | 剩余月数 < 6，触发接近年限预警 |
| 3 | DEMO-EL-0063 | 维修超期 | 维修单 DRAFT 且超 30 天 |
| 4 | DEMO-EL-0004 | 盘点异常 | 盘点结果 LOCATION_MISMATCH |
| 5 | DEMO-EL-0075 | 已报废 | 状态 SCRAPPED |
| 6 | DEMO-IT-0030 | 完整生命周期 | 有入库+领用+操作日志 |
| 7 | DEMO-EL-0042 | 审批驳回 | 审批实例 REJECTED |
| 8 | DEMO-FS-202602 | 财务同步样本 | 财务同步 FAILED |
| 9 | DEMO-IT-0005 | 长期闲置 | IDLE 状态，操作时间 >90 天 |
| 10 | DEMO-SE-0009 | 高价值服务器 | 原值 >50000 |

## 6. 预警触发说明

| 预警类型 | 触发条件 | 演示数据 |
|----------|----------|----------|
| 低净值资产 | netValue/originalValue <= 0.2 | 前 10 条资产（购置 >7 年） |
| 接近使用年限 | 剩余月数 < 6 | 第 11-20 条资产 |
| 长期闲置 | status=IDLE 且 >90 天 | IDLE 状态资产 |
| 维修超期 | 维修单 DRAFT 且 >30 天 | 5 条超期维修单 |
| 盘点异常 | inventory_record.result != NORMAL | 任务1的 10 条异常记录 |
| 财务同步异常 | finance_sync_record.status=FAILED | 1 条 FAILED 记录 |
