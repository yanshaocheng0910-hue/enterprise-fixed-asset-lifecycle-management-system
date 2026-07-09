import request from './request'

export function getInventoryTaskPage(params: Record<string, any>) {
  return request.get<any, any>('/inventory/tasks/page', { params })
}

export function createInventoryTask(data: { taskName: string; scopeType?: string; department?: string; location?: string }) {
  return request.post<any, any>('/inventory/tasks', data)
}

export function getInventoryTaskDetail(id: number) {
  return request.get<any, any>('/inventory/tasks/' + id)
}

export function startInventoryTask(id: number) {
  return request.post<any, any>('/inventory/tasks/' + id + '/start')
}

export function completeInventoryTask(id: number) {
  return request.post<any, any>('/inventory/tasks/' + id + '/complete')
}

export function deleteInventoryTask(id: number) {
  return request.delete<any, any>('/inventory/tasks/' + id)
}

export function getInventoryRecords(taskId: number) {
  return request.get<any, any>('/inventory/tasks/' + taskId + '/records')
}

export function scanInventoryRecord(data: { recordId: number; actualLocation?: string; actualKeeper?: string; result?: string; remark?: string }) {
  return request.put<any, any>('/inventory/records', data)
}


export function batchScanPending(taskId: number) {
  return request.post<any, any>('/inventory/tasks/' + taskId + '/batch-scan')
}

export function getDepartments() {
  return request.get<any, any>('/inventory/departments')
}

export function getLocations() {
  return request.get<any, any>('/inventory/locations')
}

export function getInventoryReport(taskId: number) {
  return request.get<any, any>('/inventory/tasks/' + taskId + '/report')
}