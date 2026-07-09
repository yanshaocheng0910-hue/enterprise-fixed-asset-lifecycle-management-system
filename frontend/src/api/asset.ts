import request from './request'
import type { Asset, AssetCreateRequest, AssetUpdateRequest, AssetStatusOption, AssetCategory, AssetCategoryTree } from '@/types/asset'

export function getAssetPage(params: Record<string, any>) {
  return request.get<any, { code: number; message: string; data: { records: Asset[]; total: number; pageNum: number; pageSize: number } }>('/assets/page', { params })
}

export function getAssetDetail(id: number) {
  return request.get<any, { code: number; message: string; data: Asset }>(`/assets/${id}`)
}

export function createAsset(data: AssetCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>('/assets', data)
}

export function updateAsset(id: number, data: AssetUpdateRequest) {
  return request.put<any, { code: number; message: string; data: null }>(`/assets/${id}`, data)
}

export function deleteAsset(id: number) {
  return request.delete<any, { code: number; message: string; data: null }>(`/assets/${id}`)
}

export function getStatusOptions() {
  return request.get<any, { code: number; message: string; data: AssetStatusOption[] }>('/assets/status-options')
}

export function getCategoryList() {
  return request.get<any, { code: number; message: string; data: AssetCategory[] }>('/asset-categories/list')
}

export function getCategoryTree() {
  return request.get<any, { code: number; message: string; data: AssetCategoryTree[] }>('/asset-categories/tree')
}

export interface AssetTimelineEvent {
  id: string
  assetId: number | null
  eventType: string
  eventTypeName: string
  title: string
  description: string
  orderCode: string | null
  businessType: string | null
  businessId: number | null
  status: string | null
  beforeStatus: string | null
  afterStatus: string | null
  operatorName: string | null
  eventTime: string
  source: string
  remark: string | null
}

export function getAssetTimeline(assetId: number, params?: { eventType?: string }) {
  return request.get<any, { code: number; message: string; data: AssetTimelineEvent[] }>(
    `/assets/${assetId}/timeline`, { params }
  )
}
