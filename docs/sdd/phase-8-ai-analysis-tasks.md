# AI 辅助分析 — 任务

## 后端
- [ ] AiAnalysisMapper.java — 7 条 @Select SQL
- [ ] AiAnalysisService.java — 4 个方法，不互相调用（report 复用返回不重复查）
- [ ] AiAnalysisController.java — 4 个 GET
- [ ] AiSummaryVO.java — status, count, totalValue
- [ ] AiAlertVO.java — idleAlerts, frequentRepairWarnings, abnormalStatusAlerts
- [ ] AiSuggestionVO.java — repairSuggestions, scrapSuggestions
- [ ] AiReportVO.java — summary, anomalyOverview, suggestionOverview
- [ ] 编译通过

## 前端
- [ ] api/ai.ts — 4 个函数（用类型，不用 any）
- [ ] AiAnalysis.vue — 4 个按钮绑 API + 表格展示 + loading/error 处理
- [ ] 构建通过

## 验收
- [ ] 4 个接口返回正确
- [ ] 前端 4 个按钮可点、展示数据
- [ ] 标注"辅助参考"
