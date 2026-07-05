export interface DashboardStats {
  assetCount: number
  totalOriginalValue: number
  totalAccumulatedDepreciation: number
  totalNetValue: number
  inUseCount: number
  idleCount: number
  repairingCount: number
  waitingScrapCount: number
}

export interface NameValueItem {
  name: string
  value: number
}

export interface DepartmentRankingItem {
  department: string
  amount: number
}

export interface TrendPoint {
  month: string
  value: number
}
