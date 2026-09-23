# Phase 7.1：资产生命周期时间线 - 任务清单

> 状态：正式任务清单
> 对应设计：`docs/sdd/phase-7-asset-timeline-design.md`

## 任务列表

### T1：正式化 SDD 文档 ✅

新增 4 个文件：
- `docs/sdd/phase-7-asset-timeline-spec.md`
- `docs/sdd/phase-7-asset-timeline-design.md`
- `docs/sdd/phase-7-asset-timeline-tasks.md`（本文件）
- `docs/sdd/phase-7-asset-timeline-acceptance.md`

### T2：新增 AssetTimelineQueryRequest

- 路径：`backend/src/main/java/com/example/asset/asset/dto/AssetTimelineQueryRequest.java`
- 字段：`private String eventType`
- 使用 Lombok @Data

### T3：新增 AssetTimelineEventVO

- 路径：`backend/src/main/java/com/example/asset/asset/vo/AssetTimelineEventVO.java`
- 字段：id, assetId, eventType, eventTypeName, title, description, orderCode, businessType, businessId, status, beforeStatus, afterStatus, operatorName, eventTime, source, remark
- 使用 Lombok @Data
- eventTime 使用 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

### T4：新增 AssetTimelineService

- 路径：`backend/src/main/java/com/example/asset/asset/service/AssetTimelineService.java`
- 注入 9 个 Mapper
- 实现：校验资产 → 查操作日志 → 查5类单据 → 聚合审批 → 合并排序过滤
- 所有查询使用 LambdaQueryWrapper，不新增 XML
- 空字段兜底，避免 NPE

### T5：新增 AssetTimelineController

- 路径：`backend/src/main/java/com/example/asset/asset/controller/AssetTimelineController.java`
- @RestController + @RequestMapping("/api/assets")
- @GetMapping("/{assetId}/timeline") + @RequirePermission("asset:view")
- 返回 Result<List<AssetTimelineEventVO>>

### T6：前端 asset.ts 新增类型和 API

- 路径：`frontend/src/api/asset.ts`
- 新增 AssetTimelineEvent 接口
- 新增 getAssetTimeline(assetId, params) 函数
- 不修改已有函数

### T7：新增 AssetTimeline.vue 组件

- 路径：`frontend/src/views/asset/components/AssetTimeline.vue`
- Props: assetId
- el-timeline 展示
- el-select 筛选事件类型
- el-empty 空状态
- 加载/失败状态处理

### T8：集成到 AssetDetail.vue

- 路径：`frontend/src/views/asset/AssetDetail.vue`
- 引入 AssetTimeline 组件
- 基础信息卡下方新增时间线区域
- 最大宽度 900px → 1100px
- 保留原有所有逻辑

### T9：后端构建

```bash
cd backend && mvn -DskipTests package
```

### T10：前端构建

```bash
cd frontend && npm run build
```

### T11：浏览器验收

1. 启动后端 8081 和前端
2. 登录 admin / 123456
3. 进入资产台账，打开任意资产详情页
4. 确认基础信息正常
5. 确认下方出现「生命周期时间线」
6. 确认时间线展示操作日志和生命周期单据
7. 确认筛选全部/入库/领用/调拨/维修/报废/审批/操作日志可用
8. 确认无事件资产显示空状态
9. 创建一次生命周期单据后回到详情页确认新增事件
10. 确认资产列表、新增、编辑、删除未被破坏
11. 确认生命周期页面未被破坏
12. 确认审批页面未被破坏

### T12：输出审计报告并提交

输出审计报告：
1. 是否新增数据库表（必须为否）
2. 是否新增字段（必须为否）
3. 是否修改生命周期/审批业务逻辑（必须为否）
4. 新增文件列表
5. 修改文件列表
6. 后端构建结果
7. 前端构建结果
