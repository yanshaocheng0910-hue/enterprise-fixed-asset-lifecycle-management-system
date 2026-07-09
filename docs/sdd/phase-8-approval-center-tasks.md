# Phase 8.2 审批中心页面完善 - 任务清单

## T1: 编写 SDD 文档

新增 4 个文件：
- docs/sdd/phase-8-approval-center-spec.md
- docs/sdd/phase-8-approval-center-design.md
- docs/sdd/phase-8-approval-center-tasks.md
- docs/sdd/phase-8-approval-center-acceptance.md

## T2: 补全前端 API

修改 frontend/src/api/approval.ts：
- 保留 getApprovalRecords
- 新增 ApprovalTodoItem、ApprovalDoneItem、ApprovalDetail 接口类型
- 新增 getApprovalTodoPage、getApprovalDonePage、getApprovalDetail
- 新增 approveApproval、rejectApproval

## T3: 新增待办页面

新增 frontend/src/views/approval/ApprovalTodo.vue：
- el-table 展示待办列表
- 操作列：查看详情、通过、驳回
- 通过/驳回弹窗填写 comment
- 分页、空状态、loading

## T4: 新增已办页面

新增 frontend/src/views/approval/ApprovalDone.vue：
- el-table 展示已办列表
- 操作列：查看详情
- 分页、空状态、loading

## T5: 新增审批详情弹窗

新增 frontend/src/components/approval/ApprovalDetailDialog.vue：
- el-descriptions 展示流程信息
- el-timeline 展示审批记录
- 动作映射

## T6: 接入路由

修改 frontend/src/router/index.ts：
- 新增 /approval/todo 路由，permission: approval:todo
- 新增 /approval/done 路由，permission: approval:done

## T7: 接入菜单

修改 frontend/src/layouts/MainLayout.vue：
- 新增"审批中心"el-sub-menu
- 子菜单：我的待办、我的已办

## T8: 构建验收

- 后端：cd backend && mvn -DskipTests package
- 前端：cd frontend && npm run build

## T9: 浏览器验收与提交

- 浏览器验收
- 输出审计报告
- git add + git commit
