# Phase 11 - 预警中心 Spec

## 1. 背景

系统已完成资产生命周期、审批中心、盘点闭环、折旧报表和财务模拟同步等核心模块，但各类风险数据分散在不同模块：

- 低净值资产、接近使用年限资产 → 仅在折旧报表页面展示
- 长期闲置资产 → 仅能通过资产台账手动筛选 status=IDLE
- 维修超期资产 → 仅能通过维修管理页面人工排查
- 盘点异常 → 仅在盘点任务详情中逐条查看
- 财务同步异常 → 仅在财务同步页面查看记录状态

缺乏集中预警入口，资产管理员、财务人员、审计人员无法在一处快速掌握全部风险资产和管理异常。

## 2. 目标

1. **集中预警入口**：新增"预警中心"页面，聚合展示 6 类预警
2. **预警总览**：统计总预警数及按风险等级（高/中/低）分布
3. **预警分类计数**：按预警类型展示数量
4. **预警列表**：支持按预警类型和风险等级筛选、分页
5. **资产联动**：每条预警可跳转到对应资产详情页
6. **只读提醒**：本阶段仅做网页端只读展示，不做推送、定时任务、AI 自动判断

## 3. 用户价值

- **资产管理员**：快速发现长期闲置、维修超期的资产，及时处置
- **财务人员**：识别低净值资产和本月未同步情况，保证财务数据完整
- **审计人员**：集中查看盘点异常和财务同步异常，便于审计追踪

## 4. 做什么

- 后端新增 `com.example.asset.warning` 模块
- 后端新增 WarningController、WarningService、WarningSummaryVO、WarningItemVO
- 后端新增 WarningMapper 及对应 XML（复用现有表查询，不新增表/字段）
- 前端新增 `api/warning.ts` 封装预警接口
- 前端新增 `views/warning/WarningCenter.vue` 预警中心页面
- 前端新增路由 `/warning-center`
- 前端在 MainLayout 新增"预警中心"菜单项

## 5. 不做什么

- 不修改登录/JWT
- 不修改审批核心逻辑
- 不修改生命周期状态流转
- 不修改盘点主链路
- 不修改财务同步主链路
- 不修改资产新增/编辑/删除主流程
- 不做短信、邮件、站内消息推送
- 不做定时任务
- 不做 AI 自动分析、AI 辅助建议
- 不新增数据库表
- 不新增数据库字段
- 不新增 RBAC 权限码（本阶段路由暂不加 permission，复用登录即可访问；Phase 12 多角色权限收敛时再补 `warning:view`）

## 6. 数据库复用说明

本阶段完全复用现有表，不新增表、不新增字段：

| 表名 | 复用字段 | 用于 |
|------|----------|------|
| asset | status、net_value、original_value、purchase_date、useful_life、created_at | 低净值、接近使用年限、长期闲置、维修超期预警 |
| asset_operation_log | operation_time、asset_id | 长期闲置资产最近操作时间 |
| asset_repair_order | status、repair_start_date、asset_id | 维修超期预警 |
| inventory_record | result、asset_id | 盘点异常预警 |
| finance_sync_record | status、sync_month | 财务同步异常预警 |

## 7. 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/warnings/summary | 获取预警总览（总数、按等级、按类型计数） |
| GET | /api/warnings/items?type=&level=&pageNum=&pageSize= | 获取预警列表，支持按类型和等级筛选分页 |

## 8. 预警类型与规则

### 8.1 预警类型枚举

| 类型编码 | 名称 | 等级 |
|----------|------|------|
| LOW_VALUE | 低净值资产 | LOW |
| NEAR_END | 接近使用年限 | MEDIUM |
| IDLE_LONG_TIME | 长期闲置 | MEDIUM |
| REPAIR_OVERDUE | 维修超期 | HIGH |
| INVENTORY_ABNORMAL | 盘点异常 | HIGH |
| FINANCE_SYNC_ABNORMAL | 财务同步异常 | HIGH |

### 8.2 预警规则（阈值常量集中管理）

| 类型 | 规则 | 阈值常量 |
|------|------|----------|
| LOW_VALUE | `net_value / original_value <= 0.2` 且 `status != SCRAPPED` | `LOW_VALUE_NET_VALUE_RATE = 0.2` |
| NEAR_END | `purchase_date + useful_life 年` 距离当前日期小于 6 个月 | `NEAR_END_REMAINING_MONTHS = 6` |
| IDLE_LONG_TIME | `status = IDLE` 且 `最近操作时间或 created_at 距今 > 90 天` | `IDLE_LONG_TIME_DAYS = 90` |
| REPAIR_OVERDUE | `asset.status = REPAIRING` 或维修单 `status = DRAFT` 且 `repair_start_date 距今 > 30 天` | `REPAIR_OVERDUE_DAYS = 30` |
| INVENTORY_ABNORMAL | `inventory_record.result IS NOT NULL AND result != 'NORMAL'` | `INVENTORY_NORMAL_RESULT = "NORMAL"` |
| FINANCE_SYNC_ABNORMAL | `finance_sync_record.status != 'SUCCESS'` 或当月无 SUCCESS 同步记录 | `FINANCE_SYNC_SUCCESS = "SUCCESS"` |

### 8.3 长期闲置判定细则

- 取 `asset_operation_log.operation_time` 作为最近操作时间
- 若资产无任何操作日志，回退使用 `asset.created_at`
- 距今天数 = `当前日期 - max(operation_time 或 created_at)`

### 8.4 财务同步异常判定细则

- 查询当月（当前系统月份）是否存在 `status = SUCCESS` 的同步记录
- 若存在 `status != SUCCESS` 的记录，逐条生成预警
- 若当月完全无 SUCCESS 记录，额外生成一条"本月未同步"预警（businessId=null，source="SYSTEM"）

## 9. WarningSummaryVO 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| totalWarningCount | Integer | 预警总数 |
| highWarningCount | Integer | 高风险预警数 |
| mediumWarningCount | Integer | 中风险预警数 |
| lowWarningCount | Integer | 低风险预警数 |
| lowValueCount | Integer | 低净值资产数 |
| nearEndCount | Integer | 接近使用年限资产数 |
| idleLongTimeCount | Integer | 长期闲置资产数 |
| repairOverdueCount | Integer | 维修超期资产数 |
| inventoryAbnormalCount | Integer | 盘点异常数 |
| financeSyncAbnormalCount | Integer | 财务同步异常数 |

## 10. WarningItemVO 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 预警项唯一 ID（程序生成，类型+业务ID 拼接哈希） |
| warningType | String | 预警类型编码 |
| warningTypeName | String | 预警类型名称 |
| warningLevel | String | 预警等级（HIGH/MEDIUM/LOW） |
| title | String | 预警标题 |
| description | String | 预警描述 |
| assetId | Long | 关联资产 ID（财务同步异常可能为空） |
| assetCode | String | 关联资产编号 |
| assetName | String | 关联资产名称 |
| businessId | Long | 关联业务 ID（维修单 ID、盘点记录 ID、同步记录 ID） |
| businessType | String | 业务来源类型（REPAIR/INVENTORY/FINANCE/ASSET） |
| source | String | 数据来源（ASSET/REPAIR/INVENTORY/FINANCE/SYSTEM） |
| createdAt | String | 预警生成时间（即查询时间，格式 yyyy-MM-dd HH:mm:ss） |
| suggestion | String | 处置建议（固定文案，非 AI 生成） |

## 11. 验收标准

1. 后端 `mvn -DskipTests package` 构建成功
2. 前端 `npm run build` 构建成功
3. GET /api/warnings/summary 返回 200，字段完整
4. GET /api/warnings/items 返回 200，分页正常
5. GET /api/warnings/items?type=LOW_VALUE 返回 200，仅返回低净值预警
6. GET /api/warnings/items?level=HIGH 返回 200，仅返回高风险预警
7. 前端预警中心页面显示 4 个统计卡片
8. 前端预警中心页面显示 6 个类型数量卡片
9. 前端预警中心页面显示预警列表表格
10. 前端筛选 warningType 和 warningLevel 正常
11. 前端点击"查看资产"能跳转资产详情页（assetId 为空时按钮禁用）
12. 前端空数据显示 el-empty，加载中显示 loading
13. 回归：资产台账、审批中心、盘点任务、财务同步、资产时间线正常
