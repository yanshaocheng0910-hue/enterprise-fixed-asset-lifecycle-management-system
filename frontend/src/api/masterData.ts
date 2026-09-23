import request from './request'

export interface MasterDataItem {
  id: number
  code: string
  name: string
  label: string
  value: string
  extraInfo: string | null
}

export function getDepartments() {
  return request.get<MasterDataItem[]>('/master-data/departments')
}

export function getLocations() {
  return request.get<MasterDataItem[]>('/master-data/locations')
}

export function getKeepers() {
  return request.get<MasterDataItem[]>('/master-data/keepers')
}
