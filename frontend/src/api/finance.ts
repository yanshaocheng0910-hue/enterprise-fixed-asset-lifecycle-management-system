import request from './request'

export interface FinanceSyncRecordItem {
  id: number
  syncBatchNo: string | null
  syncMonth: string
  assetCount: number
  totalOriginalValue: number
  totalNetValue: number
  totalAccumulatedDepreciation: number
  monthlyDepreciation: number
  status: string
  operatorName: string | null
  syncTime: string | null
  remark: string | null
}

export function syncDepreciationData(month: string) {
  return request.post('/finance/sync/depreciation', null, { params: { month } })
}

export function getFinanceSyncRecords(params: { pageNum: number; pageSize: number }) {
  return request.get<any, { code: number; message: string; data: { records: FinanceSyncRecordItem[]; total: number; pageNum: number; pageSize: number } }>('/finance/sync/records', { params })
}

export function getFinanceSyncDetail(id: number) {
  return request.get(`/finance/sync/records/${id}`)
}
