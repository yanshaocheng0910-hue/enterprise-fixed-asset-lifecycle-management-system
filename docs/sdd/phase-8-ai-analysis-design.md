# AI 辅助分析 — 设计

## 1. 数据库
不新建表不改字段。全部只读查询。

## 2. 包结构
```
com.example.asset.ai/
  controller/AiAnalysisController.java  # 4 个 GET
  service/AiAnalysisService.java         # 业务逻辑
  mapper/AiAnalysisMapper.java           # SQL
  vo/                                   # 4 个 VO
```

## 3. 接口

| 方法 | 路径 | 返回 |
|------|------|------|
| GET | `/api/ai/summary` | 总资产数、原值、净值、按状态分布 |
| GET | `/api/ai/alerts` | 长期闲置/频繁维修/盘点异常 三类列表 |
| GET | `/api/ai/suggestions` | 建议维修/建议报废 两类列表 |
| GET | `/api/ai/report` | summary + alerts + suggestions 拼文本 |

## 4. 查询

**summary：**
```sql
SELECT status, COUNT(*), SUM(net_value) FROM asset WHERE deleted=0 GROUP BY status
```

**alerts — 长期闲置：**
```sql
SELECT * FROM asset WHERE status='IDLE' AND updated_at < NOW() - INTERVAL 1 YEAR
```

**alerts — 频繁维修：**
```sql
SELECT a.*, COUNT(r.id) FROM asset a JOIN asset_repair_order r ON r.asset_id=a.id
  WHERE a.deleted=0 GROUP BY a.id HAVING COUNT(r.id) >= 3
```

**alerts — 盘点异常：**
```sql
SELECT * FROM asset WHERE status='INVENTORY_ABNORMAL'
```

**suggestions — 建议维修：** JOIN 同上，HAVING COUNT >= 2 且 status=IN_USE

**suggestions — 建议报废：**
```sql
SELECT * FROM asset WHERE net_value <= original_value*0.05
  OR DATEDIFF(NOW(), purchase_date)/365 >= useful_life
```

**report：** 复用上面三个的返回，不重复查。

## 5. 前端
`AiAnalysis.vue` 已有 4 个卡片骨架。加 4 个 API 函数、按钮事件、表格展示。全部用类型，不写 `any`。

不做权限拦截。
