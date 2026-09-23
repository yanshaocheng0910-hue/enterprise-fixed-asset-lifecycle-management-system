# Phase 16 - AI 辅助分析接入 DeepSeek 大模型 任务清单 (Tasks)

## T1. SDD 文档创建
**任务名称**：编写 Phase 16 SDD 四件套
**具体工作内容**：
- 创建 `docs/sdd/phase-16-deepseek-ai-spec.md`（规格说明书）
- 创建 `docs/sdd/phase-16-deepseek-ai-design.md`（技术设计文档）
- 创建 `docs/sdd/phase-16-deepseek-ai-tasks.md`（任务分解）
- 创建 `docs/sdd/phase-16-deepseek-ai-acceptance.md`（验收标准）
**涉及文件**：
- `docs/sdd/phase-16-deepseek-ai-spec.md`
- `docs/sdd/phase-16-deepseek-ai-design.md`
- `docs/sdd/phase-16-deepseek-ai-tasks.md`
- `docs/sdd/phase-16-deepseek-ai-acceptance.md`
**完成标准**：4 个文档创建完成，内容覆盖背景、目标、架构、三层设计、Prompt 设计、配置、安全、任务分解、验收标准

---

## T2. 后端配置与属性类
**任务名称**：新增 AI 配置块与 AiProperties 属性类
**具体工作内容**：
- 在 `application.yml` 中新增 `ai.*` 配置块（enabled/provider/api-base-url/api-key/model/timeout-seconds/fallback-enabled）
- `api-key` 使用 `${DEEPSEEK_API_KEY:}` 占位符从环境变量读取
- 创建 `AiProperties` 配置类，使用 `@ConfigurationProperties(prefix = "ai")`
- 在 `AssetApplication` 上启用 `@EnableConfigurationProperties(AiProperties.class)`
- 在 `.env.example` 中新增 `DEEPSEEK_API_KEY=` 占位符
**涉及文件**：
- `backend/src/main/resources/application.yml`（修改）
- `backend/src/main/java/com/example/asset/ai/config/AiProperties.java`（新增）
- `backend/src/main/java/com/example/asset/AssetApplication.java`（修改）
- `.env.example`（修改）
**完成标准**：后端启动能读取 `ai.*` 配置，未配置 `DEEPSEEK_API_KEY` 时 `apiKey` 为空字符串，`.env.example` 包含占位符

---

## T3. LlmClient 接口与异常定义
**任务名称**：定义大模型客户端抽象接口
**具体工作内容**：
- 创建 `LlmClient` 接口，定义 `chat(systemPrompt, userPrompt)`、`getProvider()`、`getModel()` 方法
- 创建 `LlmException` 运行时异常类，包含错误码与消息字段
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/llm/LlmClient.java`（新增）
- `backend/src/main/java/com/example/asset/ai/llm/LlmException.java`（新增）
**完成标准**：接口定义清晰，异常类可携带状态码与原因，后续实现类可基于此接口扩展其他模型

---

## T4. DeepSeekLlmClient 客户端实现
**任务名称**：实现 DeepSeek OpenAI 兼容客户端
**具体工作内容**：
- 创建 `DeepSeekLlmClient` 实现 `LlmClient` 接口
- 使用 Spring Boot 3.3 内置 `RestClient` 构建 HTTP 请求
- 请求地址：`POST {apiBaseUrl}/chat/completions`
- 请求头：`Authorization: Bearer {apiKey}`、`Content-Type: application/json`
- 请求体：OpenAI 兼容格式（model/messages/temperature/max_tokens/stream）
- 响应解析：提取 `choices[0].message.content` 作为原始文本
- 超时设置：连接超时 10 秒，读取超时 `ai.timeout-seconds` 秒
- 异常处理：HTTP 非 2xx、网络异常、JSON 解析异常、choices 为空均抛 `LlmException`
- 日志安全：只记录 provider/model/状态码/耗时，不记录 API Key 与完整 Prompt
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/llm/DeepSeekLlmClient.java`（新增）
**完成标准**：客户端能正确调用 DeepSeek API 并返回文本，失败时抛 `LlmException`，日志无 API Key 泄露

---

## T5. AiAnalysisReportVO 返回对象
**任务名称**：定义结构化分析报告 VO
**具体工作内容**：
- 创建 `AiAnalysisReportVO`，包含字段：analysisMode/provider/model/generatedAt/summary/keyRisks/financialInsight/operationInsight/auditFocus/recommendations/conclusion/markdownReport/fallbackReason/rawText
- 兼容旧字段：anomalyOverview/suggestionOverview（由结构化内容派生）
- 内部静态类 `KeyRisk`：title/severity/description/affectedAssets/suggestion
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/vo/AiAnalysisReportVO.java`（新增）
**完成标准**：VO 字段完整，兼容旧版 `AiReportVO` 的 summary/anomalyOverview/suggestionOverview 字段

---

## T6. AiContextCollector 数据准备层
**任务名称**：实现 collectAiContext 数据收集
**具体工作内容**：
- 创建 `AiContextVO`，包含资产总数/原值/净值/累计折旧/本月折旧/状态分布/低净值资产/接近使用年限/长期闲置/维修超期/盘点异常/财务同步失败/近12月折旧趋势/预警统计/审计摘要/典型高风险资产
- 创建 `AiContextCollector` 服务类，注入各只读 Mapper
- 复用 `AiAnalysisMapper` 现有方法（selectTotalAssetCount/selectTotalOriginalValue/selectTotalNetValue/selectStatusDistribution/selectIdleAssets/selectFrequentRepairAssets/selectAbnormalAssets）
- 在 `AiAnalysisMapper` 中新增只读 `@Select` 方法：本月折旧、近12月折旧趋势、低净值资产、接近使用年限资产、维修超期资产、盘点异常统计、财务同步失败、预警统计、审计摘要
- `topRiskAssets` 综合筛选 5-10 条高风险资产
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/context/AiContextVO.java`（新增）
- `backend/src/main/java/com/example/asset/ai/context/AiContextCollector.java`（新增）
- `backend/src/main/java/com/example/asset/ai/mapper/AiAnalysisMapper.java`（修改：新增只读方法）
**完成标准**：`collectAiContext()` 返回完整上下文数据，所有数字来自数据库只读查询，不修改表结构

---

## T7. Prompt 构建器与响应解析器
**任务名称**：实现 Prompt 构建与模型返回解析
**具体工作内容**：
- 创建 `PromptBuilder`，实现 `buildSystemPrompt()` 与 `buildUserPrompt(AiContextVO)` 方法
- system prompt 约束：角色（固定资产分析专家）、输出语言（简体中文）、不可编造数据、JSON 输出格式
- user prompt 包含：资产总览、状态分布、折旧趋势、风险统计、审计摘要、典型高风险资产
- 创建 `ResponseParser`，将模型返回的 JSON 文本解析为 `AiAnalysisReportVO`
- 解析失败时抛 `LlmException`（触发兜底）
- 生成 `markdownReport`（将结构化内容组装成 Markdown 文本）
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/prompt/PromptBuilder.java`（新增）
- `backend/src/main/java/com/example/asset/ai/prompt/ResponseParser.java`（新增）
**完成标准**：Prompt 包含完整系统数据，模型返回能正确解析为结构化 VO，解析失败抛异常

---

## T8. generateByDeepSeek 生成层
**任务名称**：实现大模型生成编排
**具体工作内容**：
- 在 `AiAnalysisService` 中新增 `generateByDeepSeek(AiContextVO)` 方法
- 调用 `PromptBuilder` 构建 system/user prompt
- 调用 `DeepSeekLlmClient.chat()` 获取原始文本
- 调用 `ResponseParser` 解析为 `AiAnalysisReportVO`
- 设置 `analysisMode=DEEPSEEK`、`provider=deepseek`、`model=deepseek-v4-flash`、`generatedAt`
- 保留 `rawText` 用于调试
- 生成 `markdownReport`
- 派生兼容旧字段 `anomalyOverview`（由 keyRisks 派生）与 `suggestionOverview`（由 recommendations 派生）
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/service/AiAnalysisService.java`（修改）
**完成标准**：大模型生成成功时返回完整 `AiAnalysisReportVO`，失败时抛 `LlmException` 由上层兜底

---

## T9. generateByRuleFallback 兜底层
**任务名称**：实现规则分析兜底逻辑
**具体工作内容**：
- 在 `AiAnalysisService` 中新增 `generateByRuleFallback(AiContextVO, reason)` 方法
- 复用原有 `getReport()` 的模板拼接逻辑生成 summary
- 基于 `AiContextVO` 的风险数据生成 `keyRisks`（闲置/维修/异常/低净值/接近年限）
- 生成 `recommendations`（基于规则的建议）
- 填充 `financialInsight`/`operationInsight`/`auditFocus`（基于规则文本）
- 生成 `conclusion` 与 `markdownReport`
- 设置 `analysisMode=RULE_FALLBACK`、`provider=rule`、`model=rule-engine`、`fallbackReason=reason`
- 派生兼容旧字段
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/service/AiAnalysisService.java`（修改）
**完成标准**：兜底返回结构与 DEEPSEEK 模式一致，`analysisMode=RULE_FALLBACK`，`fallbackReason` 说明降级原因

---

## T10. AiAnalysisService 编排与 Controller 调整
**任务名称**：编排三层调用并调整 report 端点
**具体工作内容**：
- 改造 `AiAnalysisService.getReport()` 方法，编排三层调用：
  1. `collectAiContext()` 数据准备
  2. 判断 `ai.enabled` 与 API Key 是否存在 → 不满足直接兜底
  3. `generateByDeepSeek()` 生成 → 成功返回
  4. 失败且 `fallback-enabled=true` → `generateByRuleFallback()` 兜底
  5. 失败且 `fallback-enabled=false` → 抛 `LlmException`
- 调整 `AiAnalysisController.report()` 返回 `Result<AiAnalysisReportVO>`
- 保留 `getSummary/getAlerts/getSuggestions` 原有逻辑不变
**涉及文件**：
- `backend/src/main/java/com/example/asset/ai/service/AiAnalysisService.java`（修改）
- `backend/src/main/java/com/example/asset/ai/controller/AiAnalysisController.java`（修改）
**完成标准**：`GET /api/ai/report` 返回 `AiAnalysisReportVO`，未配置 Key 时返回 RULE_FALLBACK，配置正确 Key 时返回 DEEPSEEK

---

## T11. 前端 API 类型与页面改造
**任务名称**：前端 AI 页面展示大模型报告
**具体工作内容**：
- 修改 `frontend/src/api/ai.ts` 中 `ReportData` 接口，新增字段：analysisMode/provider/model/keyRisks/financialInsight/operationInsight/auditFocus/recommendations/conclusion/markdownReport/fallbackReason/rawText
- 修改 `frontend/src/views/ai/AiAnalysis.vue` 报告卡片区：
  - 顶部展示分析模式标签（DEEPSEEK 绿色 / RULE_FALLBACK 橙色）
  - 展示模型名称与生成时间
  - fallback 模式显示降级提示横幅
  - 展示整体摘要
  - 展示关键风险列表（含 severity 颜色标识）
  - 展示财务洞察/运营洞察/审计关注
  - 展示处置建议列表
  - 展示结论
  - 展示 Markdown 报告（使用 `<pre>` 或简单渲染）
**涉及文件**：
- `frontend/src/api/ai.ts`（修改）
- `frontend/src/views/ai/AiAnalysis.vue`（修改）
**完成标准**：前端能展示分析模式、模型、结构化报告内容、fallback 提示，构建无 TypeScript 错误

---

## T12. 后端与前端构建验收
**任务名称**：构建通过验证
**具体工作内容**：
- 后端构建：`cd backend && mvn -DskipTests package`，确认 BUILD SUCCESS
- 前端构建：`cd frontend && npm run build`，确认无 TypeScript 错误
- 检查编译警告，确保无未使用 import
**涉及文件**：无（仅构建验证）
**完成标准**：后端打 jar 成功，前端 dist 产物生成，无构建错误

---

## T13. API/安全/回归验收
**任务名称**：API 验收、安全检查与回归测试
**具体工作内容**：
- API 验收：
  - 未配置 `DEEPSEEK_API_KEY` 时 `GET /api/ai/report` 返回 200，`analysisMode=RULE_FALLBACK`
  - 配置正确 Key 时 `analysisMode=DEEPSEEK`，返回结构化报告
  - 配置错误 Key 时自动 fallback，`analysisMode=RULE_FALLBACK`，不返回 500
  - `GET /api/ai/summary`、`/api/ai/alerts`、`/api/ai/suggestions` 正常返回
- 前端验收：
  - AI 页面展示分析模式标签
  - 展示模型与生成时间
  - 展示报告内容（摘要/风险/洞察/建议/结论）
  - fallback 时显示降级提示
- 回归测试：
  - 资产台账、折旧报表、预警中心、财务同步、审计日志、Excel 导出正常
- 安全检查：
  - `git diff` 无真实 API Key
  - 日志无 API Key 泄露
  - `application.yml` 只有 `${DEEPSEEK_API_KEY:}` 占位符
  - `.env.example` 只有空占位符
- 审计项检查：
  - 是否新增数据库表（否）
  - 是否新增字段（否）
  - 是否修改核心业务逻辑（否）
  - 是否泄露 API Key（否）
**涉及文件**：无（验收检查）
**完成标准**：全部验收项通过，可进入答辩演示阶段
