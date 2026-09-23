import request from './request'
import { download } from '@/utils/download'

export interface AuditSummary {
  todayOperationCount: number
  assetChangeCount: number
  approvalOperationCount: number
  inventoryAbnormalCount: number
  financeSyncCount: number
}

export interface AuditLog {
  id: string
  logType: string
  logTypeName: string
  assetId: number | null
  assetCode: string | null
  assetName: string | null
  businessType: string | null
  businessId: number | null
  operation: string | null
  beforeStatus: string | null
  afterStatus: string | null
  operatorName: string | null
  operationTime: string | null
  remark: string | null
  source: string
}

export interface AuditLogQuery {
  logType?: string
  assetCode?: string
  assetName?: string
  operatorName?: string
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export function getAuditSummary() {
  return request.get<any, { code: number; message: string; data: AuditSummary }>('/audit/logs/summary')
}

export function getAuditLogPage(params: AuditLogQuery) {
  return request.get<any, { code: number; message: string; data: { records: AuditLog[]; total: number; pageNum: number; pageSize: number } }>('/audit/logs/page', { params })
}

export function getAuditLogDetail(id: string) {
  return request.get(`/audit/logs/${id}`)
}

export function exportAuditLogs(params: Partial<AuditLogQuery>) {
  return download('/export/audit/logs', params)
}
