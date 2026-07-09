# Phase 15 - 基础数据字典与演示数据时间分布优化 任务清单

## T1: 编写 SDD 文档
- 新增 spec/design/tasks/acceptance 4 个文档
- 状态：已完成

## T2: 编写 migration-v15 SQL
- 新建 `base_department` 和 `base_location` 两张表
- 插入 10 个部门、14 个地点种子数据
- 幂等：使用 `CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`
- 文件：`backend/src/main/resources/sql/migration-v15-master-data-demo-time.sql`

## T3: 演示数据时间分布优化 SQL
- UPDATE 生命周期单据（inbound/receive/transfer/repair/scrap）`created_at` 分散到最近 12 个月
- UPDATE 审批实例 `created_at` = `started_at`
- UPDATE 审批记录 `created_at` = `COALESCE(approved_at, instance.started_at)`
- INSERT 新增盘点任务覆盖 2025-Q4、2026-Q1
- INSERT 新增 finance_sync_record 补齐 12 个月（含 1 条 FAILED）
- 仅影响 DEMO 前缀数据，不破坏真实数据

## T4: 后端 MasterDataVO
- 字段：id, code, name, label, value, extraInfo
- 文件：`backend/src/main/java/com/example/asset/masterdata/vo/MasterDataVO.java`

## T5: 后端 MasterDataMapper
- 接口方法：selectDepartments(), selectLocations(), selectKeepers()
- XML：查询 base_department、base_location、sys_user
- 文件：`MasterDataMapper.java` + `MasterDataMapper.xml`

## T6: 后端 MasterDataService
- 方法：getDepartments(), getLocations(), getKeepers()
- 调用 Mapper 并转换为 MasterDataVO 列表
- 文件：`backend/src/main/java/com/example/asset/masterdata/service/MasterDataService.java`

## T7: 后端 MasterDataController
- 3 个 GET 接口：/departments, /locations, /keepers
- 无需权限注解（登录即可访问）
- 文件：`backend/src/main/java/com/example/asset/masterdata/controller/MasterDataController.java`

## T8: 前端 masterData.ts API
- 封装 getDepartments, getLocations, getKeepers
- 文件：`frontend/src/api/masterData.ts`

## T9: 前端 useMasterDataOptions composable
- 可复用 composable，避免每个页面重复写请求
- 提供 departmentOptions, locationOptions, keeperOptions 响应式数据
- 文件：`frontend/src/composables/useMasterDataOptions.ts`

## T10: 前端表单改造
- AssetList.vue：搜索表单 + 编辑弹窗的 department/keeper/location 改为 el-select（filterable + allow-create）
- Receive.vue：编辑弹窗的 receiver/receiverDepartment 改为 el-select
- Transfer.vue：编辑弹窗的 toDepartment/toLocation/toKeeper 改为 el-select
- InventoryTask.vue：新建弹窗 department/location + 编辑明细 actualLocation/actualKeeper 改为 el-select

## T11: 后端构建
- 执行 `cd backend && mvn -DskipTests package`
- 确认 BUILD SUCCESS

## T12: 前端构建
- 执行 `cd frontend && npm run build`
- 确认构建成功

## T13: 数据库迁移
- 执行 migration-v15-master-data-demo-time.sql
- 确认 base_department 和 base_location 表创建成功
- 确认种子数据插入成功
- 确认时间分布优化生效

## T14: API/浏览器验收
- 登录 admin / 123456
- 验证基础数据接口返回正确数据
- 验证前端下拉选择正常
- 验证时间分布优化效果
- 回归测试

## T15: 审计报告 + git commit/push
- 输出审计报告
- git commit: `feat(master-data): add selectable departments locations and richer time distribution`
- git push origin main
