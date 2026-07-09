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
