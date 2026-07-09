# Phase 16 - 角色化演示账号与界面优化任务清单

## 任务总览

| 编号 | 任务 | 类型 | 预估工时 |
| --- | --- | --- | --- |
| T1 | 创建 migration-v16-demo-roles.sql | 后端 / 数据库 | 1.5h |
| T2 | 执行 SQL 并验证账号可登录 | 后端 / 验证 | 0.5h |
| T3 | 创建 useRoleDashboard.ts composable | 前端 | 1.5h |
| T4 | 创建 QuickActionCard.vue 组件 | 前端 | 0.5h |
| T5 | 改造 Dashboard.vue 为角色差异化首页 | 前端 | 3h |
| T6 | 改造 MainLayout.vue 顶部显示角色和部门 | 前端 | 1h |
| T7 | 优化 AssetList.vue | 前端 | 1.5h |
| T8 | 优化 ApprovalTodo.vue 和 ApprovalDone.vue | 前端 | 1.5h |
| T9 | 优化 InventoryTask.vue | 前端 | 1h |
| T10 | 优化 DepreciationReport.vue 和 FinanceSync.vue | 前端 | 1.5h |
| T11 | 优化 WarningCenter.vue 和 AuditLog.vue | 前端 | 1.5h |
| T12 | 创建 docs/demo-accounts.md 文档 | 文档 | 0.5h |

---

## T1 - 创建 migration-v16-demo-roles.sql

**目标：** 创建数据库迁移脚本，新增 3 个角色、7 个用户及角色权限关联。

**输入：**
- 设计文档 `phase-16-role-ui-design.md` 中的角色权限矩阵
- 现有 `sys_role` / `sys_user` / `sys_permission` 表结构

**输出：**
- 文件 `src/main/resources/db/migration/migration-v16-demo-roles.sql`

**要点：**
1. `INSERT IGNORE` 插入 3 个新角色（DEPT_LEADER=5, OFFICE_STAFF=6, INVENTORY_CLERK=7）
2. `INSERT IGNORE` 插入 7 个新用户（密码使用 BCrypt 加密的 `123456`）
3. 插入 `sys_user_role` 关联（先 DELETE 后 INSERT 保证幂等）
4. 插入 `sys_role_permission` 关联（先 DELETE role_id IN (1..7) 后 INSERT）
5. 为 `admin` 和 `system.manager` 补全所有 34 项权限

**验收：** SQL 文件可在空库与已有数据库上重复执行不报错。

---

## T2 - 执行 SQL 并验证账号可登录

**目标：** 执行迁移脚本并验证 8 个账号均可登录系统。

**步骤：**
1. 在开发数据库执行 `migration-v16-demo-roles.sql`
2. 启动后端服务
3. 依次使用 8 个账号登录，记录每个账号返回的 `roles` 与 `permissions` 数组
4. 核对返回的权限与设计矩阵一致

**验收：**
- 8 个账号登录均返回 token
- `system.manager` 拥有全部 34 项权限
- `office.staff` 仅拥有 `dashboard:view / asset:view / receive:create / repair:create / approval:done` 等少量权限

---

## T3 - 创建 useRoleDashboard.ts composable

**目标：** 提供角色首页模块配置的 composable。

**输出：** `frontend/src/composables/useRoleDashboard.ts`

**接口设计：**
```ts
interface DashboardModule {
  key: string
  title: string
  type: 'chart' | 'stat' | 'list' | 'action'
  // ...
}

export function useRoleDashboard(): {
  modules: ComputedRef<DashboardModule[]>
  roleLabel: ComputedRef<string>
}
```

**要点：**
- 读取 `authStore.userInfo.roles` 判断当前角色
- 返回该角色应展示的模块列表
- `roleLabel` 用于顶部显示（如「资产管理员 · 资产管理部」）

---

## T4 - 创建 QuickActionCard.vue 组件

**目标：** 提供首页快捷操作入口卡片组件。

**输出：** `frontend/src/components/QuickActionCard.vue`

**Props：**
- `title: string` 标题
- `icon: string` Element Plus 图标名
- `path: string` 跳转路由
- `description?: string` 描述文案

**要点：**
- 点击卡片调用 `router.push(path)`
- 样式为圆角卡片，hover 时阴影抬升

---

## T5 - 改造 Dashboard.vue 为角色差异化首页

**目标：** 根据 `useRoleDashboard` 返回的模块列表渲染不同首页。

**输入：** T3 的 composable、T4 的 QuickActionCard

**要点：**
- 使用 `v-if` / `v-for` 动态渲染模块
- 各角色首页模块组合见 `phase-16-role-ui-spec.md` 第 7 节
- 折旧趋势、审批趋势、资产总览等图表复用现有 ECharts 配置
- 普通员工首页「我的资产」「我的申请」需调用现有列表接口并按当前用户过滤

**验收：** 7 个角色登录后首页模块各不相同。

---

## T6 - 改造 MainLayout.vue 顶部显示角色和部门

**目标：** 顶部导航栏显示当前用户角色与所属部门。

**修改点：**
- 在用户头像下拉区域显示「姓名 - 角色 - 部门」
- 角色 / 部门信息来源于登录返回的 `userInfo`

**验收：** 切换账号后顶部显示信息随之变化。

---

## T7 - 优化 AssetList.vue

**目标：** 优化资产列表页视觉体验。

**修改点：**
1. 筛选条件区域收紧，减少垂直空间占用
2. 资产状态使用 Element Plus `el-tag` 按 `success / warning / danger / info` 着色
3. 金额列（原值 / 净值）右对齐，千分位格式化
4. 表格行 hover 高亮

**验收：** 列表页信息密度提升，状态与金额一眼可辨。

---

## T8 - 优化 ApprovalTodo.vue 和 ApprovalDone.vue

**目标：** 优化审批待办、已办页面的标签展示。

**修改点：**
1. 审批状态标签着色（待审批=warning，已通过=success，已拒绝=danger）
2. 业务类型标签（领用 / 调拨 / 维修 / 报废 / 盘点）使用不同颜色区分
3. 操作按钮区域收紧

**验收：** 待办与已办页状态、类型一目了然。

---

## T9 - 优化 InventoryTask.vue

**目标：** 突出盘点任务的状态与进度。

**修改点：**
1. 任务状态使用彩色标签
2. 盘点进度使用 `el-progress` 进度条展示（已盘点 / 总数）
3. 异常数量以红色徽标提示

**验收：** 盘点任务列表进度可视化清晰。

---

## T10 - 优化 DepreciationReport.vue 和 FinanceSync.vue

**目标：** 优化折旧报表与财务同步页面。

**修改点 - DepreciationReport.vue：**
1. 图表标题增加说明文案（如「近 12 个月折旧趋势」）
2. 坐标轴标签格式化（月份 / 金额千分位）
3. 数据为空时显示占位提示

**修改点 - FinanceSync.vue：**
1. 同步状态使用彩色图标（成功=绿色，失败=红色，进行中=橙色 loading）
2. 同步时间格式化显示

**验收：** 折旧趋势图 12 个月数据有明显变化；同步状态视觉强化。

---

## T11 - 优化 WarningCenter.vue 和 AuditLog.vue

**目标：** 优化预警中心与审计日志的视觉层级。

**修改点 - WarningCenter.vue：**
1. 预警等级（高 / 中 / 低）使用红 / 橙 / 蓝三色标签
2. 高等级预警置顶显示

**修改点 - AuditLog.vue：**
1. 日志类型（登录 / 资产 / 审批 / 财务 / 系统）使用彩色标签
2. 时间列格式化为「YYYY-MM-DD HH:mm:ss」
3. 操作人列突出显示

**验收：** 预警与日志信息层次分明，关键信息突出。

---

## T12 - 创建 docs/demo-accounts.md 文档

**目标：** 整理演示账号清单文档，便于演示与交接。

**输出：** `docs/demo-accounts.md`

**内容：**
- 8 个账号清单（账号 / 密码 / 姓名 / 角色 / 部门）
- 每个账号可访问的菜单与首页模块说明
- 演示场景建议（如「资产管理员发起盘点 → 部门负责人审批 → 盘点人员执行 → 审计人员核查」）

**验收：** 文档内容完整，可指导演示。

---

## 任务依赖关系

```
T1 ──► T2
T3 ──► T5
T4 ──► T5
T5 ──► T6
T7 / T8 / T9 / T10 / T11 可并行
T12 依赖 T2 验证通过
```
