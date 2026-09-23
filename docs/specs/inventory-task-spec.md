# 盘点任务管理规格草案

> 状态：规划中
> 对应 SDD 草案：`docs/sdd/phase-4-inventory-task-draft.md`

## 1. 功能定义

实现网页端盘点任务、盘点明细、盘点结果和盘点报告，补齐固定资产管理中的定期核对流程。

## 2. 做什么

- 盘点任务创建（选择部门/地点/分类范围）
- 盘点明细自动生成（拉取范围内的资产清单）
- 盘点结果录入（正常/盘盈/盘亏/位置异常）
- 盘点报告查看（统计和明细）
- 盘点异常资产状态标记

## 3. 不做什么

- 不做自动调度和定时触发
- 不做盘点结果的财务影响计算
- 不接入实物设备能力
- 不做盘点任务审批流（复用已有审批流）

## 4. 数据模型

复用已有表：

| 表名 | 说明 |
|---|---|
| `inventory_task` | 盘点任务（init.sql 已建） |
| `inventory_detail` | 盘点明细（init.sql 已建） |
| `asset` | 资产台账（读取清单、更新状态） |

如需新增字段，通过 migration SQL 实现。

## 5. 接口规划

- `GET /api/inventory/task/page` - 盘点任务分页查询
- `POST /api/inventory/task` - 创建盘点任务
- `PUT /api/inventory/task/{id}` - 编辑盘点任务
- `POST /api/inventory/task/{id}/start` - 启动盘点
- `POST /api/inventory/task/{id}/complete` - 完成盘点
- `GET /api/inventory/detail/page` - 盘点明细分页查询
- `PUT /api/inventory/detail/{id}/result` - 录入盘点结果
- `GET /api/inventory/report/{taskId}` - 盘点报告

## 6. 前端页面

- 盘点任务列表页（已有骨架，待完善）
- 盘点任务创建/编辑表单
- 盘点明细核对页
- 盘点结果确认页
- 盘点报告页

## 7. 验收标准

- 可创建盘点任务并选择范围
- 可生成盘点明细清单
- 可录入盘点结果
- 可查看盘点报告
- 盘点异常资产可标记为 INVENTORY_ABNORMAL
