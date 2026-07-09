# AI 辅助分析模块设计说明（答辩文档）

## 一、模块定位

本系统的 AI 辅助分析模块定位为**国有资产管理的智能决策辅助工具**，通过整合资产台账、折旧财务、维修记录、盘点异常、预警中心、财务同步、审计日志等多维度数据，为资产管理员、财务人员和审计人员提供自然语言形式的风险分析报告和处置建议。

**重要边界**：AI 输出仅作为辅助参考，不直接修改任何业务数据，不替代人工审批决策。

## 二、数据来源

AI 分析的数据全部来自系统现有数据库的只读查询，不编造数据：

| 数据维度 | 来源表 | 说明 |
|---|---|---|
| 资产总览 | asset | 总数、原值、净值、累计折旧 |
| 状态分布 | asset | 使用中/闲置/维修中/待报废/已报废/盘点异常 |
| 长期闲置 | asset | 闲置超过 1 年的资产 |
| 频繁维修 | asset + asset_repair_order | 维修次数 ≥ 3 次的资产 |
| 盘点异常 | asset | 状态为 INVENTORY_ABNORMAL |
| 低净值 | asset | 净值 ≤ 原值 5% |
| 接近年限 | asset | 已用年限 ≥ 使用年限 80% |
| 折旧趋势 | depreciation_record | 近 12 个月月度折旧和累计折旧 |
| 财务同步 | finance_sync_record | 同步失败记录数 |
| 审计日志 | asset_operation_log | 今日操作日志数 |
| 典型高风险 | asset | 综合风险排序 Top 10 |

## 三、实现架构

```
┌──────────────────────────────────────────────────┐
│              AiAnalysisController                │
│    GET /api/ai/report (报告生成入口)              │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│              AiAnalysisService                    │
│                                                  │
│  ┌──────────────────────────────────────────┐    │
│  │  第一层：collectAiContext（数据准备）      │    │
│  │  从 AiAnalysisMapper 查询 16 类数据       │    │
│  │  组织成结构化文本上下文                    │    │
│  └──────────────────┬───────────────────────┘    │
│                     │                            │
│         ┌───────────┴───────────┐                │
│         ▼                       ▼                │
│  ┌──────────────┐      ┌──────────────────┐      │
│  │ 第二层：      │      │ 第三层：          │      │
│  │ DeepSeek生成  │      │ 规则兜底          │      │
│  │              │      │                  │      │
│  │ LlmClient    │      │ 基于阈值和规则    │      │
│  │ →DeepSeek API│      │ 生成结构化报告    │      │
│  │ →JSON解析    │      │                  │      │
│  └──────┬───────┘      └────────┬─────────┘      │
│         │                       │                │
│         │   失败/fallback        │                │
│         └──────────────────────→│                │
└──────────────────────────────────────────────────┘
         │                                    │
         ▼                                    ▼
┌──────────────────────────────────────────────────┐
│              AiReportVO（统一返回）                │
│  analysisMode: DEEPSEEK / RULE_FALLBACK         │
│  summary, keyRisks, financialInsight,           │
│  operationInsight, auditFocus,                  │
│  recommendations[], conclusion,                 │
│  markdownReport, fallbackReason                 │
└──────────────────────────────────────────────────┘
```

## 四、为什么保留规则引擎

1. **可用性保障**：当 DeepSeek API 不可用（网络异常、API Key 未配置、额度耗尽、服务端故障）时，系统仍能生成结构化分析报告，保证业务连续性
2. **答辩稳定性**：演示环境网络不稳定时，规则引擎确保 AI 分析功能始终可用
3. **结果可预期**：规则引擎的输出基于固定阈值和逻辑，便于验证和审计
4. **渐进式升级**：用户可在配置 DeepSeek API Key 后无缝切换到大模型分析，无需改代码

## 五、DeepSeek 大模型的作用

DeepSeek 在本系统中负责**将结构化数据转化为自然语言分析报告**：

1. **数据理解**：模型接收 16 类系统数据（资产总览、状态分布、风险统计、折旧趋势、高风险资产清单等），理解数据含义
2. **风险解读**：基于数据生成风险分析文字，解释风险成因和影响
3. **建议生成**：根据风险情况生成具体的处置建议和管理措施
4. **报告撰写**：输出包含 7 个分区的结构化报告（摘要/风险/财务/运营/审计/建议/结论）

**Prompt 设计要点**：
- System Prompt 约束模型角色（国企固定资产管理系统 AI 助手）、输出语言（中文）、数据纪律（不可编造）、输出格式（JSON）
- User Prompt 提供完整的系统数据上下文
- 要求模型在数据不足时明确说明"当前数据不足以判断"

## 六、安全边界

| 安全项 | 措施 |
|---|---|
| API Key 存储 | 仅从环境变量 `DEEPSEEK_API_KEY` 读取，不硬编码到代码 |
| API Key 提交 | application.yml 使用 `${DEEPSEEK_API_KEY:}` 占位符，默认空值 |
| 日志安全 | 日志只记录 provider/model/状态码/耗时，不打印 API Key 和完整 prompt |
| 网络依赖 | 不强依赖网络，调用失败自动 fallback 到规则引擎 |
| 数据安全 | 大模型不直接访问数据库，所有数据由后端整理后传入 |
| 业务安全 | AI 输出不直接修改业务数据，仅作为参考 |

## 七、配置说明

### 环境变量

```bash
# DeepSeek API 配置（本地设置，不提交到 git）
DEEPSEEK_API_BASE_URL=https://api.deepseek.com
DEEPSEEK_API_KEY=你的DeepSeek API Key
DEEPSEEK_MODEL=deepseek-v4-flash

# 可选：关闭 AI 或切换模型
AI_ENABLED=true
DEEPSEEK_MODEL=deepseek-v4-pro  # 更强能力
```

### application.yml（已提交，无真实 Key）

```yaml
ai:
  enabled: ${AI_ENABLED:true}
  provider: deepseek
  api-base-url: ${DEEPSEEK_API_BASE_URL:https://api.deepseek.com}
  api-key: ${DEEPSEEK_API_KEY:}
  model: ${DEEPSEEK_MODEL:deepseek-v4-flash}
  timeout-seconds: 30
  fallback-enabled: true
```

## 八、答辩话术

**问：你们的 AI 辅助分析用了什么技术？**

答：我们接入了 DeepSeek 大模型（OpenAI 兼容 API），将系统中的资产台账、折旧趋势、维修记录、盘点异常、预警数据、财务同步、审计日志等 16 类业务数据组织成 prompt，由大模型生成自然语言的分析报告。同时保留规则引擎作为 fallback，确保在网络不可用或 API 异常时系统仍能输出结构化分析。

**问：AI 分析的数据从哪里来？**

答：所有数据来自系统现有数据库的只读查询，包括 asset 表（资产台账）、depreciation_record 表（折旧记录）、finance_sync_record 表（财务同步）、asset_operation_log 表（审计日志）等。大模型不直接访问数据库，数据由后端 Service 层整理后传入 prompt。

**问：如果 DeepSeek API 挂了怎么办？**

答：系统设计了三层架构：数据准备层 → DeepSeek 生成层 → 规则兜底层。当 API 调用失败（网络异常、401 认证失败、429 限流、500 服务端错误等）时，自动切换到规则引擎生成报告，返回 analysisMode=RULE_FALLBACK，前端会显示橙色提示告知用户当前为规则引擎模式。

**问：AI 分析会修改业务数据吗？**

答：不会。AI 输出仅作为辅助参考，不直接修改任何资产状态、审批单据或财务数据。所有业务操作仍需人工通过对应功能模块完成。

**问：API Key 安全吗？**

答：API Key 仅从环境变量 DEEPSEEK_API_KEY 读取，不硬编码到代码中，不提交到 git 仓库。application.yml 中使用 `${DEEPSEEK_API_KEY:}` 占位符，默认为空。日志中也不打印 API Key。

**问：怎么体现 AI 的智能？**

答：规则引擎只能输出固定模板文案（如"长期闲置资产 X 项，建议处理"），而 DeepSeek 能基于具体数据生成有针对性的分析，例如解释为什么某类资产风险较高、折旧趋势对财务的影响、哪些资产需要优先处置及原因。报告中 7 个分区的内容都是模型基于输入数据动态生成的，不是预设模板。
