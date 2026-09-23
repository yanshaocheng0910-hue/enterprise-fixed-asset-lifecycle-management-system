import { download } from '@/utils/download'

export function exportAssets(params?: { status?: string; department?: string; keyword?: string }) {
  return download('/export/assets', params)
}

export function exportAssetTimeline(assetId: number) {
  return download(`/export/assets/${assetId}/timeline`)
}

export function exportApprovalRecords() {
  return download('/export/approval/records')
}

export function exportInventoryTasks() {
  return download('/export/inventory/tasks')
}

export function exportInventoryTaskRecords(taskId: number) {
  return download(`/export/inventory/tasks/${taskId}/records`)
}

export function exportDepreciationReport(month?: string) {
  return download('/export/depreciation/report', { month })
}

export function exportFinanceSyncRecords() {
  return download('/export/finance/sync/records')
}

export function exportWarnings(params?: { type?: string; level?: string }) {
  return download('/export/warnings', params)
}

export function exportAiReport() {
  return download('/export/ai/report')
}
