import request from './request'

export function syncDepreciation(data: Record<string, any>) {
  return request.post('/finance/sync-depreciation', data)
}

export function getSyncRecords() {
  return request.get('/finance/sync-records')
}
