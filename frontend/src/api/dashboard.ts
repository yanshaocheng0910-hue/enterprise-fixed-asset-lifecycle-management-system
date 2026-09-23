import request from './request'
import type { DashboardStats, NameValueItem, DepartmentRankingItem, TrendPoint } from '@/types/dashboard'

export function getDashboardStats() {
  return request.get<any, { code: number; message: string; data: DashboardStats }>('/dashboard/stats')
}

export function getCategoryDistribution() {
  return request.get<any, { code: number; message: string; data: NameValueItem[] }>('/dashboard/category-distribution')
}

export function getDepartmentRanking() {
  return request.get<any, { code: number; message: string; data: DepartmentRankingItem[] }>('/dashboard/department-ranking')
}

export function getDepreciationTrend() {
  return request.get<any, { code: number; message: string; data: TrendPoint[] }>('/dashboard/depreciation-trend')
}
