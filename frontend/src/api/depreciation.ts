import request from './request'

/** 月度报表汇总 */
export function getMonthlySummary(month: string) {
  return request.get('/depreciation/report/summary', { params: { month } })
}

/** 月度折旧明细列表 */
export function getMonthlyItems(month: string) {
  return request.get('/depreciation/report/monthly', { params: { month } })
}

/** 部门统计 */
export function getDepartmentStats() {
  return request.get('/depreciation/statistics/department')
}

/** 分类统计 */
export function getCategoryStats() {
  return request.get('/depreciation/statistics/category')
}

/** 折旧趋势（近12个月） */
export function getDepreciationTrend() {
  return request.get('/depreciation/trend')
}
