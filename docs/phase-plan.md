# 阶段计划

## 第一阶段：项目骨架与核心主链路（已完成）

### 已完成内容

**后端：**

- Spring Boot 3 + Java 17 项目搭建
- MySQL 8 + MyBatis-Plus 配置
- 统一返回结构 Result<T> + PageResult<T>
- 统一异常处理 GlobalExceptionHandler
- JWT + HandlerInterceptor 鉴权
- 用户/角色/资产分类/资产/操作日志 表结构
- 折旧/盘点/财务 表结构预留
- 登录接口
- 资产 CRUD 接口（分页/详情/新增/编辑/删除）
- 资产分类查询接口
- 首页驾驶舱统计接口
- 初始化 SQL（含20条 mock 数据）
- 骨架接口（inventory, finance）

**前端：**

- Vue 3 + TypeScript + Vite 项目搭建
- Element Plus + Pinia + Vue Router + Axios + ECharts
- 登录页
- 首页驾驶舱（真实统计 + ECharts 图表）
- 资产台账（分页/查询/新增/编辑/删除/详情）
- 资产分类（树 + 列表）
- 10 个骨架页面（生命周期/盘点/折旧/财务/AI/用户管理）
- Axios 请求/响应拦截器（token 注入、401 处理）
- 路由守卫（未登录跳转、已登录跳转）
- 企业级 UI 风格

**数据库：**

- 9 张表（用户/角色/关联/分类/资产/日志/折旧/盘点任务/盘点明细）
- 默认管理员 admin/123456
- 4 个角色
- 5 个资产分类
- 20 条资产测试数据

---

## 第二阶段：生命周期单据流（计划中）

### 目标

让资产状态不再靠手动编辑，而是通过业务单据自动流转。

### 新表

| 表名 | 说明 |
|---|---|
| asset_inbound_order | 入库单 |
| asset_receive_order | 领用单 |
| asset_transfer_order | 调拨单 |
| asset_repair_order | 维修单 |
| asset_scrap_order | 报废单 |

### 实现功能

- 入库流程：新增资产 → 入库单 → 状态 IDLE
- 领用流程：选择资产 → 领用单 → 状态 IN_USE
- 调拨流程：选择资产 → 调拨单 → 状态 TRANSFERRING → IN_USE
- 维修流程：选择资产 → 维修单 → 状态 REPAIRING → IN_USE/IDLE
- 报废流程：选择资产 → 报废申请 → 状态 WAITING_SCRAP → SCRAPPED
- 每个流程自动记录 asset_operation_log

---

## 第三阶段：盘点管理（计划中）

### 目标

实现固定资产盘点完整流程。

### 实现功能

- 创建盘点任务（选择部门/地点范围）
- 生成盘点清单
- 二维码扫码盘点
- 盘点结果处理（正常/盘盈/盘亏/位置异常）
- 盘点报告
- 异常资产状态标记 INVENTORY_ABNORMAL

---

## 第四阶段：折旧报表与导出（计划中）

### 实现功能

- 月度折旧报表
- 按部门统计
- 按分类统计
- Excel 导出（POI 或 EasyExcel）
- 财务同步数据准备

---

## 第五阶段：用户管理与权限（计划中）

### 实现功能

- 用户列表
- 新增/编辑用户
- 角色分配
- 菜单权限
- 按钮级权限

---

## 第六阶段：AI / RFID / 财务对接（计划中）

### 实现功能

- AI 资产照片分类
- AI 故障风险预测
- AI 盘点路线推荐
- RFID 设备接口
- 财务系统模拟/真实对接
