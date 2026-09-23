import request from './request'

export interface WarningSummary {
  totalWarningCount: number
  highWarningCount: number
  mediumWarningCount: number
  lowWarningCount: number
  lowValueCount: number
  nearEndCount: number
  idleLongTimeCount: number
  repairOverdueCount: number
  inventoryAbnormalCount: number
  financeSyncAbnormalCount: number
}

export interface WarningItem {
  id: number
  warningType: string
  warningTypeName: string
  warningLevel: string
  title: string
  description: string
  assetId: number | null
  assetCode: string | null
  assetName: string | null
  businessId: number | null
  businessType: string
  source: string
  createdAt: string
  suggestion: string
}

export function getWarningSummary() {
  return request.get('/warnings/summary')
}

export function getWarningItems(params: {
  type?: string
  level?: string
  pageNum: number
  pageSize: number
}) {
  return request.get('/warnings/items', { params })
}
