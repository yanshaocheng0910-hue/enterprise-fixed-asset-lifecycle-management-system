export interface Asset {
  id: number
  assetCode: string
  assetName: string
  categoryId: number
  categoryName: string
  specification: string
  brand: string
  purchaseDate: string
  originalValue: number
  usefulLife: number
  residualRate: number
  depreciationMethod: string
  accumulatedDepreciation: number
  netValue: number
  department: string
  keeper: string
  location: string
  status: string
  qrCode: string
  rfidCode: string
  photoUrl: string
  remark: string
  createdAt: string
  updatedAt: string
}

export interface AssetQuery {
  assetCode?: string
  assetName?: string
  categoryId?: number
  department?: string
  keeper?: string
  location?: string
  status?: string
  purchaseDateStart?: string
  purchaseDateEnd?: string
  pageNum: number
  pageSize: number
}

export interface AssetCreateRequest {
  assetName: string
  categoryId: number
  specification?: string
  brand?: string
  purchaseDate: string
  originalValue: number
  usefulLife: number
  residualRate: number
  department: string
  keeper: string
  location: string
  status?: string
  qrCode?: string
  rfidCode?: string
  photoUrl?: string
  remark?: string
}

export interface AssetUpdateRequest extends AssetCreateRequest {
  status: string
}

export interface AssetStatusOption {
  code: string
  label: string
}

export interface AssetCategory {
  id: number
  categoryCode: string
  categoryName: string
  parentId: number
  depreciationYears: number
  remark: string
}

export interface AssetCategoryTree extends AssetCategory {
  children: AssetCategoryTree[]
}
