import request from './request'

export function syncDepreciation(month: string) {
  return request.post('/finance/sync-depreciation', null, { params: { month } })
}

export function getSyncRecords(params: { pageNum: number; pageSize: number }) {
  return request.get('/finance/sync-records', { params })
}