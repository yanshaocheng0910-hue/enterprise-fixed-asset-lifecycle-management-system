import request from './request'

export interface StatusItem {
  status: string
  statusLabel: string
  count: number
  totalValue: number
}

export interface SummaryData {
  totalCount: number
  totalOriginalValue: number
  totalNetValue: number
  statusDistribution: StatusItem[]
}

export interface AlertItem {
  assetId: number
  assetCode: string
  assetName: string
  department: string
  keeper: string
  alertReason: string
  severity: string
}

export interface AlertsData {
  idleAlerts: AlertItem[]
  frequentRepairAlerts: AlertItem[]
  abnormalStatusAlerts: AlertItem[]
}

export interface SuggestionItem {
  assetId: number
  assetCode: string
  assetName: string
  department: string
  usefulLife: number
  netValue: number
  repairCount: number
  suggestion: string
  reason: string
}

export interface SuggestionsData {
  repairSuggestions: SuggestionItem[]
  scrapSuggestions: SuggestionItem[]
}

export interface ReportData {
  // 旧字段
  generatedAt: string
  summary: string
  anomalyOverview: string
  suggestionOverview: string
  // 新增字段
  analysisMode: string  // DEEPSEEK | RULE_FALLBACK
  provider: string
  model: string
  keyRisks: string
  financialInsight: string
  operationInsight: string
  auditFocus: string
  recommendations: string[]
  conclusion: string
  markdownReport: string
  fallbackReason: string | null
  rawText: string | null
}

export function getAiSummary() {
  return request.get<any, { code: number; message: string; data: SummaryData }>('/ai/summary')
}

export function getAiAlerts() {
  return request.get<any, { code: number; message: string; data: AlertsData }>('/ai/alerts')
}

export function getAiSuggestions() {
  return request.get<any, { code: number; message: string; data: SuggestionsData }>('/ai/suggestions')
}

export function getAiReport() {
  return request.get<any, { code: number; message: string; data: ReportData }>('/ai/report')
}
