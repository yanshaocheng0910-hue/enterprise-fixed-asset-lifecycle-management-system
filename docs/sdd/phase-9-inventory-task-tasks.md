# Phase 9 盘点任务管理完善 - 任务清单（Tasks）

## T1: SDD 文档
- 新增 `docs/sdd/phase-9-inventory-task-spec.md`
- 新增 `docs/sdd/phase-9-inventory-task-design.md`
- 新增 `docs/sdd/phase-9-inventory-task-tasks.md`
- 新增 `docs/sdd/phase-9-inventory-task-acceptance.md`
- 状态：✅ 本文档即为此任务产出

## T2: 后端接口实现
- 新增 `InventoryTaskMapper.java`（extends BaseMapper<InventoryTask>）
- 新增 `InventoryRecordMapper.java`（extends BaseMapper<InventoryRecord>，含 selectRecordsByTaskId、countUnrecorded）
- 新增 `dto/InventoryTaskCreateRequest.java`（taskName, scopeType, department, location）
- 新增 `dto/InventoryTaskQueryRequest.java`（pageNum, pageSize, status, scopeType）
- 新增 `dto/InventoryRecordUpdateRequest.java`（actualLocation, actualKeeper, result, remark）
- 新增 `vo/InventoryTaskVO.java`（含 totalRecords, completedRecords, createdByName）
- 新增 `vo/InventoryRecordVO.java`（含 assetCode, assetName, categoryName）
- 修改 `InventoryService.java`：注入两个 Mapper + AssetMapper，实现 page/create/detail/getRecords/updateRecord/complete
- 修改 `InventoryController.java`：新增 5 个接口端点

## T3: 前端 API 补全
- 修改 `frontend/src/api/inventory.ts`：
  - 保留已有 `getInventoryTaskPage`
  - 新增 `createInventoryTask(data)`
  - 新增 `getInventoryTaskDetail(id)`
  - 新增 `getInventoryRecords(taskId, params)`
  - 新增 `updateInventoryRecord(recordId, data)`
  - 新增 `completeInventoryTask(taskId)`

## T4: 盘点任务列表
- 修改 `frontend/src/views/inventory/InventoryTask.vue`：
  - 顶部筛选区（状态筛选 + 新建按钮）
  - 任务列表表格（任务编号、名称、范围、状态、进度、时间、操作）
  - 分页
  - 空数据 el-empty
  - 加载 loading

## T5: 新建任务
- 在 InventoryTask.vue 中实现新建弹窗
- 表单：任务名称、范围类型、部门（条件显示）、地点（条件显示）
- 提交调用 createInventoryTask
- 成功后刷新列表

## T6: 明细核对
- 在 InventoryTask.vue 中实现明细弹窗
- 明细表格展示资产信息 + 应在/实际地点和保管人 + 结果
- 编辑弹窗：录入实际地点、实际保管人、结果、备注
- 保存调用 updateInventoryRecord

## T7: 完成任务
- 在明细弹窗底部实现"完成任务"按钮
- 调用 completeInventoryTask
- 成功后刷新任务列表和明细

## T8: 构建验收
- 后端：`cd backend && mvn -DskipTests package`
- 前端：`cd frontend && npm run build`
- 浏览器端到端验收（登录 → 创建任务 → 生成明细 → 录入结果 → 完成任务 → 回归）

## T9: 提交
- 输出审计报告
- `git add` 相关文件
- `git commit -m "feat(inventory): complete inventory task workflow"`
