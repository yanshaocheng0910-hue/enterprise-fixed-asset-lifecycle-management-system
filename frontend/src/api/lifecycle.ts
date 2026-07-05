import request from './request'

export function getAssetSelect(status?: string) {
  return request.get<any, any>('/lifecycle/asset-select-options', { params: { status } })
}

export function getInboundPage(params: Record<string, any>) {
  return request.get<any, any>('/lifecycle/inbound/page', { params })
}

export function getInboundDetail(id: number) {
  return request.get<any, any>(`/lifecycle/inbound/${id}`)
}

export function createInbound(data: Record<string, any>) {
  return request.post<any, any>('/lifecycle/inbound', data)
}

export function getReceivePage(params: Record<string, any>) {
  return request.get<any, any>('/lifecycle/receive/page', { params })
}

export function getReceiveDetail(id: number) {
  return request.get<any, any>(`/lifecycle/receive/${id}`)
}

export function createReceive(data: Record<string, any>) {
  return request.post<any, any>('/lifecycle/receive', data)
}

export function getTransferPage(params: Record<string, any>) {
  return request.get<any, any>('/lifecycle/transfer/page', { params })
}

export function getTransferDetail(id: number) {
  return request.get<any, any>(`/lifecycle/transfer/${id}`)
}

export function createTransfer(data: Record<string, any>) {
  return request.post<any, any>('/lifecycle/transfer', data)
}

export function getRepairPage(params: Record<string, any>) {
  return request.get<any, any>('/lifecycle/repair/page', { params })
}

export function getRepairDetail(id: number) {
  return request.get<any, any>(`/lifecycle/repair/${id}`)
}

export function createRepair(data: Record<string, any>) {
  return request.post<any, any>('/lifecycle/repair', data)
}

export function completeRepair(id: number, data: Record<string, any>) {
  return request.put<any, any>(`/lifecycle/repair/${id}/complete`, data)
}

export function getScrapPage(params: Record<string, any>) {
  return request.get<any, any>('/lifecycle/scrap/page', { params })
}

export function getScrapDetail(id: number) {
  return request.get<any, any>(`/lifecycle/scrap/${id}`)
}

export function createScrap(data: Record<string, any>) {
  return request.post<any, any>('/lifecycle/scrap', data)
}
