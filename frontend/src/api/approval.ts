import request from './request'

export interface ApprovalRecordItem {
  nodeName: string | null
  approverName: string | null
  action: string
  comment: string | null
  approvedAt: string | null
}

export function getApprovalRecords(params: { businessType: string; businessId: number }) {
  return request.get<any, { code: number; message: string; data: ApprovalRecordItem[] }>(
    '/approval/records', { params }
  )
}

export interface ApprovalTodoItem {
  instanceId: number
  businessType: string
  businessId: number
  flowName: string | null
  nodeName: string | null
  status: string
  startedBy: number | null
  applicantName: string | null
  startedAt: string | null
}

export interface ApprovalDoneItem {
  instanceId: number
  businessType: string
  businessId: number
  orderCode: string | null
  assetCode: string | null
  assetName: string | null
  action: string
  comment: string | null
  status: string
  approverName: string | null
  approvedAt: string | null
}

export interface ApprovalDetail {
  instanceId: number
  businessType: string
  businessId: number
  flowName: string | null
  currentNodeName: string | null
  status: string
  applicantName: string | null
  startedAt: string | null
  completedAt: string | null
  records: ApprovalRecordItem[]
}

export function getApprovalTodoPage(params: Record<string, any>) {
  return request.get<any, { code: number; message: string; data: { records: ApprovalTodoItem[]; total: number; pageNum: number; pageSize: number } }>(
    '/approval/todo/page', { params }
  )
}

export function getApprovalDonePage(params: Record<string, any>) {
  return request.get<any, { code: number; message: string; data: { records: ApprovalDoneItem[]; total: number; pageNum: number; pageSize: number } }>(
    '/approval/done/page', { params }
  )
}

export function getApprovalDetail(instanceId: number) {
  return request.get<any, { code: number; message: string; data: ApprovalDetail }>(
    `/approval/${instanceId}`
  )
}

export function approveApproval(instanceId: number, data: { action: string; comment?: string }) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/approval/${instanceId}/approve`, data
  )
}

export function rejectApproval(instanceId: number, data: { action: string; comment?: string }) {
  return request.post<any, { code: number; message: string; data: null }>(
    `/approval/${instanceId}/reject`, data
  )
}
