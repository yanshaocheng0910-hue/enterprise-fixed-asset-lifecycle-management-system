# Phase 16 - AI 辅助分析接入 DeepSeek 大模型 需求规格 (Spec)

## 1. 背景

固定资产生命周期管理系统已完成 Phase 1-15，涵盖资产台账、生命周期、审批中心、资产时间线、盘点闭环、折旧财务、财务同步、预警中心、Excel 导出、演示数据、全局审计日志与基础数据时间增强等核心模块。系统已具备完整业务闭环、丰富演示数据与全链路审计能力。

**当前痛点**：现有 AI 辅助分析模块（Phase 9）的实现本质是「SQL 聚合 + 规则判断 + 模板报告拼接」，即通过 `AiAnalysisMapper` 执行固定 SQL 统计资产总数、状态分布、闲置/维修/异常资产，再用 `String.format` 拼接成固定模板文本返回给前端。这种方式存在以下问题：

1. **难以体现真实 AI 能力**：报告内容是预写好的模板字符串填充数字，没有任何自然语言生成与推理能力，答辩时难以说明"AI 体现在哪里"。
2. **缺乏解释性与推理能力**：只能输出"存在 N 项长期闲置资产"这类陈述句，无法解释"为什么这是风险""对财务有什么影响""应如何处置"。
3. **缺乏跨维度关联分析**：无法将资产统计、折旧趋势、维修异常、盘点异常、财务同步、预警、审计等多源数据综合起来给出整体研判。
4. **缺乏可读的自然语言报告**：返回的是单段中文文本，没有结构化的风险列表、财务洞察、运营建议与结论。

## 2. 目标

接入 DeepSeek 大模型（OpenAI 兼容 API），将系统多源数据组织成 Prompt，由大模型生成结构化的资产状态分析报告、风险解释和处置建议：

- 将资产统计、折旧趋势、维修异常、盘点异常、财务同步、预警数据、审计日志摘要等系统数据组织成结构化 Prompt
- 调用 DeepSeek OpenAI-compatible API（`POST {apiBaseUrl}/chat/completions`）由大模型生成资产状态分析报告
- 报告包含：整体摘要、关键风险、财务洞察、运营洞察、审计关注点、处置建议、总结结论
- 同时输出 Markdown 格式报告，便于前端渲染与导出
- 保留原有规则分析能力作为兜底（fallback），模型调用失败或未配置 API Key 时自动降级

## 3. 用户价值

- **资产管理员**：快速理解资产整体健康度，识别闲置、维修超期、盘点异常等风险资产，获得处置建议
- **财务人员**：获得折旧趋势、净值变动、财务同步失败等财务维度的洞察与解释，辅助财务决策
- **审计人员**：从审计摘要中快速定位高风险操作与异常关注点，提升审计效率
- **管理决策**：AI 输出的结构化报告与建议辅助管理者做出维修、报废、调拨等处置决策
- **答辩价值**：真实的大模型接入可清晰展示"AI 体现在哪里"——从数据收集、Prompt 组织、模型调用到结构化报告生成的完整链路

## 4. 安全边界

AI 输出严格遵循以下安全约束：

1. **只读辅助**：AI 输出仅作为辅助参考，不直接修改任何业务数据（资产状态、生命周期单据、审批、财务、盘点等均不受 AI 影响）
2. **不可编造数据**：通过 system prompt 约束模型只能基于提供的系统数据进行分析，不得编造不存在的资产、数字或事件
3. **不绕过权限**：AI 接口复用现有鉴权与权限体系，未授权用户无法访问
4. **不暴露敏感信息**：API Key 只从环境变量读取，不日志打印，不提交到代码仓库

## 5. 开发边界（必须遵守）

以下边界在开发过程中必须严格遵守：

- ✅ 不修改登录 / JWT 鉴权链路
- ✅ 不修改资产台账（新增/编辑/删除/查询）主链路
- ✅ 不修改生命周期（入库/领用/调拨/维修/报废）状态流转
- ✅ 不修改审批中心核心逻辑
- ✅ 不修改盘点任务主链路
- ✅ 不修改财务同步主链路
- ✅ 不修改预警规则主链路
- ✅ 不修改 Excel 导出主链路
- ✅ 不修改审计日志主链路
- ✅ 不修改基础数据主链路
- ✅ 不修改数据库表结构（不新增表、不新增字段、不修改字段类型）
- ✅ 不删除原有规则分析能力（保留为 fallback 兜底）
- ✅ 不把 API Key 写死到代码中
- ✅ 不提交真实 API Key 到 git 仓库
- ✅ 模型调用失败必须 fallback 到规则分析，不允许返回 500
- ✅ 本阶段只新增 AI 大模型接入相关代码（客户端、Prompt、生成层、兜底层），复用现有 Mapper 只读查询

## 6. 功能需求

### 6.1 AI 分析报告生成

- 接口：`GET /api/ai/report`
- 调用 DeepSeek 大模型生成结构化资产状态分析报告
- 返回 `AiAnalysisReportVO`，包含分析模式、模型信息、生成时间、摘要、关键风险、财务洞察、运营洞察、审计关注点、处置建议、结论、Markdown 报告等
- 兼容旧版 `AiReportVO` 字段（summary / anomalyOverview / suggestionOverview），保证前端渐进式升级

### 6.2 数据收集（collectAiContext）

收集以下系统数据组织成 Prompt 上下文：

| 数据项 | 数据来源 | 说明 |
|---|---|---|
| 资产总数 | `asset` 表 COUNT | 全量资产数量 |
| 资产原值 | `asset` 表 SUM(original_value) | 原值总计 |
| 资产净值 | `asset` 表 SUM(net_value) | 净值总计 |
| 累计折旧 | 原值 - 净值 | 已计提折旧总额 |
| 本月折旧 | `depreciation_record` 当月 SUM | 本月折旧费用 |
| 状态分布 | `asset` GROUP BY status | 各状态资产数量与净值 |
| 低净值资产 | `asset` WHERE net_value <= original_value * 0.05 | 净值接近归零的资产 |
| 接近使用年限资产 | `asset` WHERE 使用年限接近到期 | 剩余使用年限不足的资产 |
| 长期闲置资产 | `asset` WHERE status='IDLE' AND 超过 1 年 | 闲置超期资产 |
| 维修超期资产 | `asset_repair_order` 未按时完成 | 维修工单超期 |
| 盘点异常资产 | `inventory_record` WHERE result != 'NORMAL' | 盘点差异资产 |
| 财务同步失败 | `finance_sync_record` WHERE status='FAILED' | 财务同步失败记录 |
| 近 12 月折旧趋势 | `depreciation_record` 按月聚合 | 月度折旧趋势 |
| 预警统计 | `warning` 相关查询 | 各级预警数量 |
| 审计摘要 | `audit` 相关查询 | 近期操作与异常摘要 |
| 典型高风险资产 | 综合筛选 | 5-10 条高风险资产清单 |

### 6.3 原有接口保留

以下接口保持原有行为不变（基于规则分析）：

- `GET /api/ai/summary` — 资产状态摘要（SQL 聚合）
- `GET /api/ai/alerts` — 异常资产提示（规则判断）
- `GET /api/ai/suggestions` — 维修/报废建议（规则判断）

### 6.4 Fallback 兜底

当出现以下情况时，`GET /api/ai/report` 自动降级到规则分析模式：

1. 未配置 `DEEPSEEK_API_KEY` 环境变量
2. `ai.enabled = false`
3. `ai.fallback-enabled = true` 且模型调用失败（网络超时、HTTP 错误、响应解析失败等）
4. 模型返回内容为空或格式异常

兜底时 `analysisMode = RULE_FALLBACK`，返回基于规则生成的报告（保留原有模板分析逻辑），并在 `fallbackReason` 字段说明降级原因。

## 7. 配置需求

在 `application.yml` 中新增 AI 配置块：

```yaml
ai:
  enabled: true
  provider: deepseek
  api-base-url: https://api.deepseek.com
  api-key: ${DEEPSEEK_API_KEY:}
  model: deepseek-v4-flash
  timeout-seconds: 30
  fallback-enabled: true
```

**配置说明**：

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `ai.enabled` | true | AI 大模型分析总开关 |
| `ai.provider` | deepseek | 模型提供商 |
| `ai.api-base-url` | https://api.deepseek.com | API 基础地址 |
| `ai.api-key` | ${DEEPSEEK_API_KEY:} | API Key，从环境变量读取，默认空 |
| `ai.model` | deepseek-v4-flash | 模型名称 |
| `ai.timeout-seconds` | 30 | 调用超时时间（秒） |
| `ai.fallback-enabled` | true | 是否允许失败时降级到规则分析 |

**环境变量**：在 `.env.example` 中新增 `DEEPSEEK_API_KEY=` 占位符，实际 Key 仅在本地 `.env` 或运行环境变量中配置。

## 8. 返回对象 AiAnalysisReportVO

| 字段 | 类型 | 说明 |
|---|---|---|
| analysisMode | String | 分析模式：`DEEPSEEK` / `RULE_FALLBACK` |
| provider | String | 模型提供商（deepseek），兜底时为 `rule` |
| model | String | 模型名称（deepseek-v4-flash），兜底时为 `rule-engine` |
| generatedAt | String | 生成时间（yyyy-MM-dd HH:mm:ss） |
| summary | String | 整体摘要（兼容旧字段） |
| keyRisks | List\<KeyRisk\> | 关键风险列表 |
| financialInsight | String | 财务洞察 |
| operationInsight | String | 运营洞察 |
| auditFocus | String | 审计关注点 |
| recommendations | List\<String\> | 处置建议列表 |
| conclusion | String | 总结结论 |
| markdownReport | String | Markdown 格式完整报告 |
| fallbackReason | String | 降级原因（DEEPSEEK 模式为 null） |
| rawText | String | 模型原始返回文本 |
| anomalyOverview | String | 异常概览（兼容旧字段，由 keyRisks 派生） |
| suggestionOverview | String | 建议概览（兼容旧字段，由 recommendations 派生） |

**KeyRisk 结构**：

| 字段 | 类型 | 说明 |
|---|---|---|
| title | String | 风险标题 |
| severity | String | 严重程度（high/medium/low） |
| description | String | 风险描述 |
| affectedAssets | List\<String\> | 受影响资产编号列表 |
| suggestion | String | 针对该风险的处置建议 |

## 9. 安全设计

1. **API Key 管理**：API Key 只通过环境变量 `DEEPSEEK_API_KEY` 注入，`application.yml` 中只保留 `${DEEPSEEK_API_KEY:}` 占位符
2. **日志脱敏**：日志中不打印 API Key、不打印完整请求体中的 Authorization 头
3. **Git 安全**：`.env` 文件已在 `.gitignore` 中忽略，不提交真实 Key
4. **调用隔离**：大模型调用在独立线程上下文中执行，不影响主业务流程
5. **超时保护**：30 秒超时，超时后自动 fallback

## 10. 验收标准概述

1. 后端 `mvn -DskipTests package` 成功
2. 前端 `npm run build` 成功
3. 未配置 `DEEPSEEK_API_KEY` 时 `GET /api/ai/report` 返回 200，`analysisMode = RULE_FALLBACK`
4. 配置正确 Key 时 `analysisMode = DEEPSEEK`，返回大模型生成的结构化报告
5. 配置错误 Key 时自动 fallback，`analysisMode = RULE_FALLBACK`，不返回 500
6. `GET /api/ai/summary`、`/api/ai/alerts`、`/api/ai/suggestions` 正常返回
7. 前端 AI 页面展示分析模式、模型、报告内容、fallback 提示
8. 回归资产台账、折旧报表、预警中心、财务同步、审计日志、Excel 导出正常
9. `git diff` 无真实 API Key，日志无 API Key 泄露
10. 未新增数据库表、未新增字段、未修改核心业务逻辑
