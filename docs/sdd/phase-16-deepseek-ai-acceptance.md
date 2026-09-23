# Phase 16 - AI 辅助分析接入 DeepSeek 大模型 验收清单 (Acceptance)

## 1. 构建验收

- [ ] 后端构建：`cd backend && mvn -DskipTests package` 通过（BUILD SUCCESS）
- [ ] 前端构建：`cd frontend && npm run build` 通过（无 TypeScript 错误）
- [ ] 后端无编译错误与未使用 import 警告
- [ ] 前端 dist 产物正常生成

## 2. API 验收 - report 接口

### 2.1 未配置 DEEPSEEK_API_KEY
- [ ] 启动后端时不设置 `DEEPSEEK_API_KEY` 环境变量
- [ ] `GET /api/ai/report` 返回 HTTP 200
- [ ] 响应 `data.analysisMode = "RULE_FALLBACK"`
- [ ] 响应 `data.provider = "rule"`
- [ ] 响应 `data.model = "rule-engine"`
- [ ] 响应 `data.fallbackReason` 包含"未配置"或"API Key"说明
- [ ] 响应包含 summary/keyRisks/recommendations 等结构化字段
- [ ] 响应包含兼容旧字段 anomalyOverview/suggestionOverview

### 2.2 配置正确 DEEPSEEK_API_KEY
- [ ] 启动后端时设置有效的 `DEEPSEEK_API_KEY` 环境变量
- [ ] `GET /api/ai/report` 返回 HTTP 200
- [ ] 响应 `data.analysisMode = "DEEPSEEK"`
- [ ] 响应 `data.provider = "deepseek"`
- [ ] 响应 `data.model = "deepseek-v4-flash"`
- [ ] 响应 `data.fallbackReason = null`
- [ ] 响应 `data.summary` 为大模型生成的中文摘要
- [ ] 响应 `data.keyRisks` 为非空数组，每项含 title/severity/description/suggestion
- [ ] 响应 `data.financialInsight` 为大模型生成的财务洞察
- [ ] 响应 `data.operationInsight` 为大模型生成的运营洞察
- [ ] 响应 `data.auditFocus` 为大模型生成的审计关注点
- [ ] 响应 `data.recommendations` 为非空建议列表
- [ ] 响应 `data.conclusion` 为大模型生成的结论
- [ ] 响应 `data.markdownReport` 为完整 Markdown 文本
- [ ] 响应 `data.rawText` 为模型原始返回文本
- [ ] 响应 `data.generatedAt` 格式为 yyyy-MM-dd HH:mm:ss

### 2.3 配置错误 DEEPSEEK_API_KEY
- [ ] 启动后端时设置无效的 `DEEPSEEK_API_KEY`（如 `sk-invalid-key-12345`）
- [ ] `GET /api/ai/report` 返回 HTTP 200（不允许 500）
- [ ] 响应 `data.analysisMode = "RULE_FALLBACK"`
- [ ] 响应 `data.fallbackReason` 包含失败原因（如"401"/"API Key 无效"/"调用失败"）
- [ ] 响应仍包含完整的结构化报告内容（规则生成）

### 2.4 AI 禁用场景
- [ ] `application.yml` 中设置 `ai.enabled: false`
- [ ] `GET /api/ai/report` 返回 HTTP 200
- [ ] 响应 `data.analysisMode = "RULE_FALLBACK"`
- [ ] 响应 `data.fallbackReason` 包含"AI 未启用"

### 2.5 fallback 禁用场景
- [ ] `application.yml` 中设置 `ai.fallback-enabled: false` 且配置错误 Key
- [ ] `GET /api/ai/report` 返回错误响应（非 200，提示模型调用失败）
- [ ] 验证后恢复 `ai.fallback-enabled: true`

## 3. API 验收 - 原有接口回归

- [ ] `GET /api/ai/summary` 返回 200，包含 totalCount/totalOriginalValue/totalNetValue/statusDistribution
- [ ] `GET /api/ai/alerts` 返回 200，包含 idleAlerts/frequentRepairAlerts/abnormalStatusAlerts
- [ ] `GET /api/ai/suggestions` 返回 200，包含 repairSuggestions/scrapSuggestions
- [ ] 以上三个接口行为与 Phase 9 一致（未受大模型接入影响）

## 4. 前端验收

- [ ] 登录后进入「AI 辅助分析」页面
- [ ] 资产状态摘要卡片正常（生成状态摘要按钮可用）
- [ ] 异常资产提示卡片正常（查看异常列表按钮可用）
- [ ] 维修/报废建议卡片正常
- [ ] AI 分析报告卡片展示分析模式标签：
  - DEEPSEEK 模式显示绿色标签「DeepSeek 大模型」
  - RULE_FALLBACK 模式显示橙色标签「规则分析」
- [ ] 报告卡片展示模型名称（deepseek-v4-flash 或 rule-engine）
- [ ] 报告卡片展示生成时间
- [ ] fallback 模式下顶部显示橙色降级提示横幅，说明降级原因
- [ ] 整体摘要区域展示 summary 文本
- [ ] 关键风险区域展示 keyRisks 列表，含严重程度颜色标识（high=红/medium=橙/low=蓝）
- [ ] 财务洞察/运营洞察/审计关注区域展示对应文本
- [ ] 处置建议区域展示 recommendations 列表
- [ ] 结论区域展示 conclusion 文本
- [ ] 完整报告区域展示 markdownReport 内容
- [ ] 「生成报告」按钮点击后能加载并展示报告

## 5. 回归测试

- [ ] 资产台账页面正常（/assets 列表、筛选、新增、编辑、详情）
- [ ] 折旧报表页面正常（/depreciation/report 月度报表、分类统计、低净值、接近年限）
- [ ] 预警中心正常（/warning-center 预警列表、统计）
- [ ] 财务同步正常（/finance/sync 同步记录、状态）
- [ ] 审计日志正常（/audit/logs 审计列表、统计、导出）
- [ ] Excel 导出正常（资产导出、审计导出等）
- [ ] 审批中心正常（待办、已办、详情）
- [ ] 盘点任务正常（任务列表、明细、结果）
- [ ] 生命周期单据正常（入库、领用、调拨、维修、报废）
- [ ] 资产时间线正常
- [ ] 基础数据管理正常
- [ ] 登录/退出正常

## 6. 安全检查

- [ ] `git diff` 中不包含真实 API Key 字符串
- [ ] 后端日志中不出现 API Key 明文（搜索 `sk-` 前缀确认）
- [ ] 后端日志中不出现 Authorization 头内容
- [ ] 后端日志中不出现完整 Prompt 中的敏感信息
- [ ] `application.yml` 中 `ai.api-key` 值为 `${DEEPSEEK_API_KEY:}` 占位符
- [ ] `.env.example` 中 `DEEPSEEK_API_KEY=` 为空占位符
- [ ] `.env` 文件已被 `.gitignore` 忽略（不提交）
- [ ] 异常信息中不包含 API Key（如 HTTP 401 错误不回显 Key）
- [ ] DeepSeek 调用日志只记录 provider/model/状态码/耗时

## 7. 审计项

| 审计项 | 期望 | 实际 |
|---|---|---|
| 是否新增数据库表 | 否 | [ ] |
| 是否新增数据库字段 | 否 | [ ] |
| 是否修改数据库表结构 | 否 | [ ] |
| 是否修改登录/JWT 逻辑 | 否 | [ ] |
| 是否修改资产台账主链路 | 否 | [ ] |
| 是否修改生命周期状态流转 | 否 | [ ] |
| 是否修改审批核心逻辑 | 否 | [ ] |
| 是否修改盘点主链路 | 否 | [ ] |
| 是否修改财务同步主链路 | 否 | [ ] |
| 是否修改预警规则主链路 | 否 | [ ] |
| 是否修改 Excel 导出主链路 | 否 | [ ] |
| 是否修改审计日志主链路 | 否 | [ ] |
| 是否修改基础数据主链路 | 否 | [ ] |
| 是否删除原有规则分析能力 | 否（保留为 fallback） | [ ] |
| 是否泄露 API Key | 否 | [ ] |
| 是否把 API Key 写死到代码 | 否（仅环境变量） | [ ] |
| 模型调用失败是否必 fallback | 是（fallback-enabled=true 时） | [ ] |
| 是否新增 Maven 依赖 | 否（用内置 RestClient） | [ ] |
| 是否新增前端依赖 | 否 | [ ] |

## 8. 答辩演示要点

- [ ] 演示未配置 Key 时规则分析模式（RULE_FALLBACK）正常工作
- [ ] 演示配置正确 Key 时大模型（DEEPSEEK）生成结构化报告
- [ ] 演示配置错误 Key 时自动降级，不报 500
- [ ] 展示三层架构：数据收集 → 大模型生成 → 规则兜底
- [ ] 展示 Prompt 设计（system + user prompt）
- [ ] 展示安全设计（API Key 环境变量、日志脱敏、Git 不提交）
- [ ] 展示兼容旧字段（前端渐进式升级）
- [ ] 展示回归测试全部通过

## 9. 提交规范

- [ ] `git add` 仅包含本阶段新增/修改文件
- [ ] 排除 `.trae-html-share-packages/`、`backend/custom-settings.xml`、`.env`
- [ ] 确认 `git diff --cached` 中无真实 API Key
- [ ] commit message: `feat(ai): integrate DeepSeek LLM for AI analysis report`
- [ ] `git push origin main` 成功
