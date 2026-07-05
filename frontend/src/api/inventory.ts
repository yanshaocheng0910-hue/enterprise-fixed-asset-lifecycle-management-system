import request from './request'

export function getInventoryTaskPage(params: Record<string, any>) {
  return request.get('/inventory/tasks/page', { params })
}
