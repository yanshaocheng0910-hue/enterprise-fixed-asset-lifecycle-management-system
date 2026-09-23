# Phase 8.1 审批进度可视化增强 - 任务清单

## T1: 编写 SDD 文档

新增 4 个文件：
- `docs/sdd/phase-8-approval-progress-spec.md`
- `docs/sdd/phase-8-approval-progress-design.md`
- `docs/sdd/phase-8-approval-progress-tasks.md`
- `docs/sdd/phase-8-approval-progress-acceptance.md`

## T2: 新增前端 API

新增 `frontend/src/api/approval.ts`：
- 定义 `ApprovalRecordItem` 接口
- 封装 `getApprovalRecords(params)` 函数
- 请求 `GET /approval/records?businessType=&businessId=`

## T3: 新增 ApprovalProgress 组件

新增 `frontend/src/components/approval/ApprovalProgress.vue`：
- Props: businessType, businessId, status
- onMounted 加载审批记录
- el-timeline 展示记录
- 动作映射：SUBMIT→提交申请, APPROVED→审批通过, REJECTED→审批驳回
- 空状态：el-empty "暂无审批记录"
- 加载中：loading

## T4: 修改 LifecycleDetailDialog

修改 `frontend/src/components/LifecycleDetailDialog.vue`：
- 新增可选 prop: businessType
- 在 el-descriptions 下方集成 ApprovalProgress
- 当 businessType 和 data.id 存在时渲染

## T5: 领用详情接入

修改 `frontend/src/views/lifecycle/Receive.vue`：
- LifecycleDetailDialog 添加 `business-type="RECEIVE"`

## T6: 调拨详情接入

修改 `frontend/src/views/lifecycle/Transfer.vue`：
- LifecycleDetailDialog 添加 `business-type="TRANSFER"`

## T7: 维修详情接入

修改 `frontend/src/views/lifecycle/Repair.vue`：
- LifecycleDetailDialog 添加 `business-type="REPAIR"`

## T8: 报废详情接入

修改 `frontend/src/views/lifecycle/Scrap.vue`：
- LifecycleDetailDialog 添加 `business-type="SCRAP"`

## T9: 构建验证

- 后端：`cd backend && mvn -DskipTests package`
- 前端：`cd frontend && npm run build`

## T10: 浏览器验收

- 登录 admin / 123456
- 进入领用、调拨、维修、报废页面
- 打开单据详情，确认审批进度展示
- 确认无记录时显示空状态
- 确认入库详情不显示审批进度
- 确认不影响原提交审批、审批通过、审批驳回
- 确认资产详情时间线正常
- 确认资产列表和生命周期列表正常

## T11: 审计报告与提交

- 输出审计报告
- git add 精确文件
- git commit
