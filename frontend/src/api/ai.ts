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
  generatedAt: string
  summary: string
  anomalyOverview: string
  suggestionOverview: string
}

export function getAiSummary() {
  return request.get<SummaryData>('/ai/summary')
}

export function getAiAlerts() {
  return request.get<AlertsData>('/ai/alerts')
}

export function getAiSuggestions() {
  return request.get<SuggestionsData>('/ai/suggestions')
}

export function getAiReport() {
  return request.get<ReportData>('/ai/report')
}
