# Phase 16 - AI 辅助分析接入 DeepSeek 大模型 技术设计 (Design)

## 1. 整体架构

```
前端 AiAnalysis.vue
  ├─ 分析模式标签 (DEEPSEEK / RULE_FALLBACK)
  ├─ 模型与生成时间信息
  ├─ 结构化报告展示（摘要/关键风险/财务洞察/运营洞察/审计关注/建议/结论）
  ├─ Markdown 报告渲染区
  └─ Fallback 提示横幅
        │
        ▼ axios
frontend/src/api/ai.ts
  └─ getAiReport()  →  ReportData（新增 analysisMode/provider/model/keyRisks...）
        │
        ▼ HTTP
后端 AiAnalysisController (/api/ai/report)
  └─ AiAnalysisService.getReport()
       │
       ├─ 1. collectAiContext()        数据准备层
       │     ├─ AiAnalysisMapper（复用现有只读查询）
       │     ├─ DepreciationReportMapper（近12月趋势、本月折旧）
       │     ├─ WarningMapper（预警统计）
       │     ├─ AuditMapper（审计摘要）
       │     ├─ FinanceSyncRecordMapper（同步失败）
       │     └─ AiContextVO（组装上下文）
       │
       ├─ 2. generateByDeepSeek()       生成层（优先）
       │     ├─ PromptBuilder（system + user prompt）
       │     ├─ LlmClient → DeepSeekLlmClient
       │     │    └─ POST {apiBaseUrl}/chat/completions
       │     ├─ ResponseParser（解析模型输出为 AiAnalysisReportVO）
       │     └─ 失败 → 抛 LlmException
       │
       └─ 3. generateByRuleFallback()   兜底层（异常时）
             └─ 基于原有规则分析逻辑生成 AiAnalysisReportVO（analysisMode=RULE_FALLBACK）
```

**核心设计原则**：数据库统计 + 规则引擎 + DeepSeek 大模型生成 + fallback 兜底，四层各司其职，互不侵入。

## 2. 三层设计

### 2.1 collectAiContext 数据准备层

**职责**：从多个只读 Mapper 收集系统数据，组装成 `AiContextVO` 供 Prompt 使用。

**AiContextVO 字段**：

| 字段 | 类型 | 数据来源 |
|---|---|---|
| generatedAt | String | 当前时间 |
| totalCount | Long | `AiAnalysisMapper.selectTotalAssetCount()` |
| totalOriginalValue | BigDecimal | `AiAnalysisMapper.selectTotalOriginalValue()` |
| totalNetValue | BigDecimal | `AiAnalysisMapper.selectTotalNetValue()` |
| accumulatedDepreciation | BigDecimal | 原值 - 净值 |
| monthlyDepreciation | BigDecimal | `DepreciationReportMapper` 当月折旧 |
| statusDistribution | List | `AiAnalysisMapper.selectStatusDistribution()` |
| lowValueAssets | List | 净值 <= 原值 * 5% 的资产 |
| nearEndOfLifeAssets | List | 接近使用年限的资产 |
| idleAssets | List | `AiAnalysisMapper.selectIdleAssets()` |
| overdueRepairs | List | `asset_repair_order` 超期未完成 |
| inventoryAbnormals | List | `inventory_record` WHERE result != 'NORMAL' |
| financeSyncFailures | List | `finance_sync_record` WHERE status='FAILED' |
| depreciationTrend | List | `DepreciationReportMapper` 近 12 月趋势 |
| warningSummary | Map | `WarningMapper` 预警统计（高/中/低数量） |
| auditSummary | String | `AuditMapper` 审计摘要（今日操作数、异常数） |
| topRiskAssets | List | 综合筛选典型高风险资产 5-10 条 |

**实现要点**：
- 复用现有 Mapper 的只读查询方法，不新增 SQL、不修改表结构
- 对于需要新增统计维度（如本月折旧、近 12 月趋势、预警统计、审计摘要），在 `AiAnalysisMapper` 中新增 `@Select` 注解方法（只读查询，不改表）
- `topRiskAssets` 综合筛选逻辑：优先选取「净值归零 + 频繁维修 + 盘点异常 + 长期闲置」的资产，取 5-10 条

### 2.2 generateByDeepSeek 生成层

**职责**：将 `AiContextVO` 组织成 Prompt，调用 DeepSeek API，解析返回结果。

**调用流程**：
1. `PromptBuilder.buildSystemPrompt()` — 构建 system prompt
2. `PromptBuilder.buildUserPrompt(context)` — 构建 user prompt
3. `DeepSeekLlmClient.chat(systemPrompt, userPrompt)` — 调用 API
4. `ResponseParser.parse(rawText)` — 解析模型返回为 `AiAnalysisReportVO`
5. 设置 `analysisMode = DEEPSEEK`、`provider = deepseek`、`model = deepseek-v4-flash`

**异常处理**：任何步骤失败抛出 `LlmException`，由上层捕获后进入兜底层。

### 2.3 generateByRuleFallback 兜底层

**职责**：保留原有规则分析逻辑，生成 `AiAnalysisReportVO`（`analysisMode = RULE_FALLBACK`）。

**实现要点**：
- 复用 `collectAiContext()` 收集的数据
- 基于原有 `getReport()` 的模板拼接逻辑生成 summary / anomalyOverview / suggestionOverview
- 补充 keyRisks（基于规则识别的闲置/维修/异常资产）、recommendations（基于规则的建议）
- 设置 `fallbackReason` 说明降级原因（如「未配置 DEEPSEEK_API_KEY」「模型调用超时」「模型返回 HTTP 401」）
- 保证返回结构与 DEEPSEEK 模式一致，前端无感知差异

## 3. DeepSeek 客户端设计

### 3.1 LlmClient 接口

```java
public interface LlmClient {
    /**
     * 调用大模型对话接口
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 模型返回的原始文本
     * @throws LlmException 调用失败时抛出
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 获取模型提供商标识
     */
    String getProvider();

    /**
     * 获取模型名称
     */
    String getModel();
}
```

### 3.2 DeepSeekLlmClient 实现

**HTTP 调用**：使用 Spring Boot 3.3 内置的 `RestClient`（spring-web 6.1+），无需新增依赖。

**请求格式**（OpenAI 兼容）：

```json
POST {apiBaseUrl}/chat/completions
Authorization: Bearer {apiKey}
Content-Type: application/json

{
  "model": "deepseek-v4-flash",
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "..."}
  ],
  "temperature": 0.3,
  "max_tokens": 4096,
  "stream": false
}
```

**响应解析**：

```json
{
  "id": "...",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "模型返回的文本..."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {"prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0}
}
```

提取 `choices[0].message.content` 作为原始文本返回。

**超时与异常**：
- 连接超时 10 秒，读取超时 30 秒（对应 `ai.timeout-seconds`）
- HTTP 非 2xx 抛 `LlmException`（含状态码与错误信息）
- 网络异常、JSON 解析异常抛 `LlmException`
- 响应 `choices` 为空抛 `LlmException`

**安全**：
- API Key 从配置 `${DEEPSEEK_API_KEY:}` 注入，配置项绑定到 `AiProperties.apiKey`
- 日志只记录 `provider`、`model`、响应耗时，不记录 API Key 与完整 Prompt 内容
- 异常信息中不包含 API Key

### 3.3 AiProperties 配置类

```java
@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {
    private boolean enabled = true;
    private String provider = "deepseek";
    private String apiBaseUrl = "https://api.deepseek.com";
    private String apiKey = "";
    private String model = "deepseek-v4-flash";
    private int timeoutSeconds = 30;
    private boolean fallbackEnabled = true;
}
```

在 `AssetApplication` 上启用 `@EnableConfigurationProperties(AiProperties.class)`，或通过 `@ConfigurationPropertiesScan` 自动扫描。

## 4. Prompt 设计

### 4.1 System Prompt

```
你是一名固定资产管理分析专家，负责对企业固定资产数据进行综合分析与风险评估。

【角色约束】
- 你只能基于用户提供的系统数据进行分析，不得编造任何不存在的资产、数字、事件或操作记录
- 如果提供的数据为空或不足，应明确说明"数据不足，无法深入分析"
- 分析应客观、专业、聚焦资产管理实务

【输出语言】
- 必须使用简体中文输出

【输出格式】
- 严格按照指定的 JSON 结构输出，不要输出 JSON 以外的内容
- JSON 结构如下：
{
  "summary": "整体摘要，2-4 句话概括资产整体状况",
  "keyRisks": [
    {"title":"风险标题","severity":"high|medium|low","description":"风险描述","affectedAssets":["资产编号"],"suggestion":"处置建议"}
  ],
  "financialInsight": "财务维度洞察，包括折旧、净值、财务同步等",
  "operationInsight": "运营维度洞察，包括闲置、维修、盘点等",
  "auditFocus": "审计关注点，基于审计摘要提示高风险操作",
  "recommendations": ["建议1","建议2","建议3"],
  "conclusion": "总结结论，1-3 句话"
}
```

### 4.2 User Prompt

```
以下是截至 {generatedAt} 的固定资产管理系统数据，请基于这些数据生成资产状态分析报告。

【资产总览】
- 资产总数：{totalCount} 项
- 资产原值合计：{totalOriginalValue} 元
- 资产净值合计：{totalNetValue} 元
- 累计折旧：{accumulatedDepreciation} 元
- 本月折旧：{monthlyDepreciation} 元

【状态分布】
{statusDistribution 逐行：状态中文 数量项 净值XX元}

【折旧趋势（近 12 月）】
{depreciationTrend 逐行：2025-01 折旧XX元}
趋势变化：{上升/下降/平稳}

【风险统计】
- 长期闲置资产：{idleCount} 项
- 频繁维修资产：{frequentRepairCount} 项
- 盘点异常资产：{inventoryAbnormalCount} 项
- 维修超期资产：{overdueRepairCount} 项
- 财务同步失败：{financeSyncFailureCount} 条
- 低净值资产（净值<=原值5%）：{lowValueCount} 项
- 接近使用年限资产：{nearEndCount} 项
- 预警统计：高风险 {highWarning} 条、中风险 {mediumWarning} 条、低风险 {lowWarning} 条

【审计摘要】
{auditSummary}

【典型高风险资产】
{topRiskAssets 逐行：编号 名称 部门 状态 净值 风险标签}

请基于以上数据生成结构化分析报告，严格按照 system 指定的 JSON 格式输出。
```

### 4.3 Prompt 构建要点

- **数据脱敏**：Prompt 中只包含资产编号、名称、部门、状态、金额等业务数据，不包含任何用户密码、API Key 等敏感信息
- **数据完整性**：所有数字均来自 `AiContextVO`，不出现占位符
- **空数据处理**：当某项数据为空时，明确写"暂无"而非省略，避免模型误判
- **长度控制**：典型高风险资产限制 5-10 条，折旧趋势限制 12 个月，避免 Prompt 过长

## 5. 返回对象 AiAnalysisReportVO

```java
@Data
public class AiAnalysisReportVO {
    // 模式与元信息
    private String analysisMode;     // DEEPSEEK / RULE_FALLBACK
    private String provider;          // deepseek / rule
    private String model;             // deepseek-v4-flash / rule-engine
    private String generatedAt;

    // 结构化分析内容
    private String summary;
    private List<KeyRisk> keyRisks;
    private String financialInsight;
    private String operationInsight;
    private String auditFocus;
    private List<String> recommendations;
    private String conclusion;

    // 完整报告
    private String markdownReport;

    // 兜底与调试信息
    private String fallbackReason;    // DEEPSEEK 模式为 null
    private String rawText;           // 模型原始返回文本

    // 兼容旧字段（由结构化内容派生）
    private String anomalyOverview;   // 由 keyRisks 派生
    private String suggestionOverview;// 由 recommendations 派生

    @Data
    public static class KeyRisk {
        private String title;
        private String severity;       // high/medium/low
        private String description;
        private List<String> affectedAssets;
        private String suggestion;
    }
}
```

**Markdown 报告生成**：由 `markdownReport` 字段提供完整 Markdown 文本，前端使用 Markdown 渲染器展示。结构如下：

```markdown
# 固定资产状态分析报告

**分析模式**：DeepSeek 大模型分析
**模型**：deepseek-v4-flash
**生成时间**：2026-07-09 14:30:00

## 一、整体摘要
{summary}

## 二、关键风险
### 1. {keyRisks[0].title}（{severity}）
{description}
- 受影响资产：{affectedAssets}
- 处置建议：{suggestion}

## 三、财务洞察
{financialInsight}

## 四、运营洞察
{operationInsight}

## 五、审计关注点
{auditFocus}

## 六、处置建议
1. {recommendations[0]}
2. {recommendations[1]}

## 七、结论
{conclusion}
```

## 6. AiAnalysisService 编排逻辑

```java
public AiAnalysisReportVO getReport() {
    // 1. 数据准备层
    AiContextVO context = collectAiContext();

    // 2. 判断是否启用大模型
    if (!aiProperties.isEnabled() || !hasApiKey()) {
        return generateByRuleFallback(context, "AI 未启用或未配置 DEEPSEEK_API_KEY");
    }

    // 3. 大模型生成层
    try {
        return generateByDeepSeek(context);
    } catch (LlmException e) {
        // 4. 兜底层
        if (aiProperties.isFallbackEnabled()) {
            return generateByRuleFallback(context, "模型调用失败：" + e.getMessage());
        }
        throw e;
    }
}

private boolean hasApiKey() {
    return aiProperties.getApiKey() != null
        && !aiProperties.getApiKey().isBlank();
}
```

**降级条件汇总**：

| 条件 | analysisMode | fallbackReason |
|---|---|---|
| `ai.enabled = false` | RULE_FALLBACK | AI 未启用 |
| API Key 为空 | RULE_FALLBACK | 未配置 DEEPSEEK_API_KEY |
| 调用超时 | RULE_FALLBACK | 模型调用超时（30s） |
| HTTP 401/403 | RULE_FALLBACK | API Key 无效 |
| HTTP 429 | RULE_FALLBACK | 调用频率超限 |
| HTTP 5xx | RULE_FALLBACK | 模型服务异常 |
| 响应解析失败 | RULE_FALLBACK | 模型返回格式异常 |
| `ai.fallback-enabled = false` 且调用失败 | — | 抛 LlmException（不降级） |

## 7. 配置设计

### 7.1 application.yml 新增

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

### 7.2 .env.example 新增

```env
# ===== DeepSeek AI 配置 =====
# DeepSeek API Key（从 https://platform.deepseek.com 获取）
DEEPSEEK_API_KEY=
```

### 7.3 .gitignore 检查

确认 `.env` 已在 `.gitignore` 中（已有），确保真实 Key 不会被提交。

## 8. 安全设计

### 8.1 API Key 安全

- **来源**：只从环境变量 `DEEPSEEK_API_KEY` 读取，通过 Spring 配置占位符 `${DEEPSEEK_API_KEY:}` 注入
- **存储**：不写入代码、不写入 `application.yml` 明文、不写入 `.env.example` 真实值
- **传输**：通过 HTTPS 传输到 DeepSeek API（`https://api.deepseek.com`）
- **日志**：`DeepSeekLlmClient` 日志只记录 `provider`、`model`、响应耗时、HTTP 状态码，**绝不**记录 API Key、Authorization 头、完整 Prompt 内容

### 8.2 日志脱敏

```java
// 正确日志示例
log.info("DeepSeek 调用开始 provider={} model={}", provider, model);
log.info("DeepSeek 调用完成 status={} costMs={}", statusCode, costMs);

// 错误日志示例（不暴露 Key）
log.error("DeepSeek 调用失败 status={} reason={}", statusCode, reason);
```

### 8.3 Git 提交检查

- 提交前检查 `git diff` 中不包含真实 API Key
- `application.yml` 中只有 `${DEEPSEEK_API_KEY:}` 占位符
- `.env.example` 中只有 `DEEPSEEK_API_KEY=` 空占位符
- `.env` 文件被 `.gitignore` 忽略

## 9. 后端文件清单

### 9.1 新增文件

| 路径 | 说明 |
|---|---|
| `backend/.../ai/config/AiProperties.java` | AI 配置属性类 |
| `backend/.../ai/llm/LlmClient.java` | 大模型客户端接口 |
| `backend/.../ai/llm/LlmException.java` | 大模型调用异常 |
| `backend/.../ai/llm/DeepSeekLlmClient.java` | DeepSeek 客户端实现 |
| `backend/.../ai/context/AiContextVO.java` | AI 上下文数据 VO |
| `backend/.../ai/context/AiContextCollector.java` | 数据准备层（collectAiContext） |
| `backend/.../ai/prompt/PromptBuilder.java` | Prompt 构建器 |
| `backend/.../ai/prompt/ResponseParser.java` | 模型返回解析器 |
| `backend/.../ai/vo/AiAnalysisReportVO.java` | 结构化报告 VO（含 KeyRisk 内部类） |

### 9.2 修改文件

| 路径 | 修改内容 |
|---|---|
| `backend/.../ai/service/AiAnalysisService.java` | 新增 `getReport()` 编排逻辑（三层调用），保留原 `getSummary/getAlerts/getSuggestions` 不变 |
| `backend/.../ai/controller/AiAnalysisController.java` | `report()` 端点返回 `AiAnalysisReportVO`（替换原 `AiReportVO`） |
| `backend/.../ai/mapper/AiAnalysisMapper.java` | 新增只读查询方法（本月折旧、近12月趋势、预警统计、审计摘要等），不改原有方法 |
| `backend/src/main/resources/application.yml` | 新增 `ai.*` 配置块 |
| `backend/.../AssetApplication.java` | 启用 `@EnableConfigurationProperties(AiProperties.class)` |
| `.env.example` | 新增 `DEEPSEEK_API_KEY=` 占位符 |

### 9.3 不修改的文件（边界保护）

- 登录/JWT：`AuthController`、`AuthService`、`JwtUtil`、`AuthInterceptor`
- 资产台账：`AssetController`、`AssetService`、`AssetMapper`
- 生命周期：`LifecycleController`、`LifecycleService` 及各单据 Service/Mapper
- 审批：`ApprovalController`、`ApprovalService`
- 盘点：`InventoryController`、`InventoryService`
- 财务同步：`FinanceSyncController`、`FinanceService`
- 预警：`WarningController`、`WarningService`
- Excel 导出：`ExportController`、`ExcelExportUtil`
- 审计：`AuditController`、`AuditService`
- 基础数据：`MasterDataController`、`MasterDataService`
- 折旧报表主链路：`DepreciationReportController`、`DepreciationReportService`（仅只读复用其 Mapper）

## 10. 前端文件清单

### 10.1 修改文件

| 路径 | 修改内容 |
|---|---|
| `frontend/src/api/ai.ts` | 新增 `ReportData` 接口字段（analysisMode/provider/model/keyRisks/financialInsight 等） |
| `frontend/src/views/ai/AiAnalysis.vue` | 改造报告卡片区：展示分析模式标签、模型信息、结构化报告、Markdown 渲染、Fallback 提示 |

### 10.2 前端展示设计

```
┌─────────────────────────────────────────────────┐
│ AI 分析报告                          [生成报告]  │
├─────────────────────────────────────────────────┤
│ [分析模式: DEEPSEEK 大模型] [模型: deepseek-v4] │
│ [生成时间: 2026-07-09 14:30]                    │
│ ⚠ fallback 模式时显示降级提示横幅               │
├─────────────────────────────────────────────────┤
│ 整体摘要                                         │
│ {summary}                                        │
├─────────────────────────────────────────────────┤
│ 关键风险                                         │
│ 🔴 {title}（{severity}）                        │
│    {description}                                 │
│    受影响资产：{affectedAssets}                  │
│    建议：{suggestion}                            │
├─────────────────────────────────────────────────┤
│ 财务洞察 | 运营洞察 | 审计关注                  │
│ {financialInsight} | {operationInsight} | ...   │
├─────────────────────────────────────────────────┤
│ 处置建议                                         │
│ 1. {recommendation1}                             │
│ 2. {recommendation2}                             │
├─────────────────────────────────────────────────┤
│ 结论                                             │
│ {conclusion}                                     │
├─────────────────────────────────────────────────┤
│ 完整报告（Markdown 渲染）                        │
│ {markdownReport}                                 │
└─────────────────────────────────────────────────┘
```

**Fallback 提示横幅**：当 `analysisMode === 'RULE_FALLBACK'` 时，顶部显示橙色提示横幅："当前为规则分析模式（{fallbackReason}），未使用大模型生成。如需大模型分析，请配置 DEEPSEEK_API_KEY 环境变量。"

## 11. 不新增表/字段说明

- 所有数据收集复用现有表的只读查询，不新增数据库表
- 不新增字段（现有 `asset`、`depreciation_record`、`inventory_record`、`finance_sync_record`、`warning`、`audit` 相关表字段足够）
- 新增的 Mapper 方法均为 `@Select` 只读查询，不涉及 DDL
- 完全符合「不修改数据库表结构」约束

## 12. 依赖说明

- **不新增 Maven 依赖**：HTTP 调用使用 Spring Boot 3.3 内置的 `RestClient`（spring-web 6.1+），JSON 解析使用内置 Jackson
- **不新增前端依赖**：Markdown 渲染如需增强可选用 `marked`，但本阶段优先用 `<pre>` 或简单换行渲染，避免新增依赖
- **不新增数据库依赖**：复用现有 MySQL + MyBatis-Plus
