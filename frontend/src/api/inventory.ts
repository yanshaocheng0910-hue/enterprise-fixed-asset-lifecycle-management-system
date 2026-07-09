import request from './request'

export interface InventoryTaskItem {
  id: number
  taskCode: string
  taskName: string
  scopeType: string
  department: string | null
  location: string | null
  status: string
  startTime: string | null
  endTime: string | null
  createdBy: number | null
  createdAt: string | null
  updatedAt: string | null
  totalRecords: number
  completedRecords: number
}

export interface InventoryRecordItem {
  id: number
  taskId: number
  assetId: number
  assetCode: string | null
  assetName: string | null
  categoryName: string | null
  expectedLocation: string | null
  actualLocation: string | null
  expectedKeeper: string | null
  actualKeeper: string | null
  result: string | null
  scannedAt: string | null
  remark: string | null
}

export interface InventoryTaskCreateRequest {
  taskName: string
  scopeType: string
  department?: string
  location?: string
}

export interface InventoryRecordUpdateRequest {
  actualLocation?: string
  actualKeeper?: string
  result: string
  remark?: string
}

export function getInventoryTaskPage(params: Record<string, any>) {
  return request.get<any, { code: number; message: string; data: { records: InventoryTaskItem[]; total: number; pageNum: number; pageSize: number } }>(
    '/inventory/tasks/page', { params }
  )
}

export function createInventoryTask(data: InventoryTaskCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>(
    '/inventory/tasks', data
  )
}

export function getInventoryTaskDetail(id: number) {
  return request.get<any, { code: number; message: string; data: InventoryTaskItem }>(
    `/inventory/tasks/${id}`
  )
}

export function getInventoryRecords(taskId: number) {
  return request.get<any, { code: number; message: string; data: InventoryRecordItem[] }>(
    `/inventory/tasks/${taskId}/records`
  )
}

export function updateInventoryRecord(recordId: number, data: InventoryRecordUpdateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/inventory/records/${recordId}`, data
  )
}

export function completeInventoryTask(taskId: number) {
  return request.put<any, { code: number; message: string; data: null }>(
    `/inventory/tasks/${taskId}/complete`
  )
}
