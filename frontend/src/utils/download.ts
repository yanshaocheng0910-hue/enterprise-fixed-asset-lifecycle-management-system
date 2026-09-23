import axios from 'axios'
import { getToken } from '@/utils/token'
import { ElMessage } from 'element-plus'

const downloadRequest = axios.create({
  baseURL: '/api',
  timeout: 60000,
  responseType: 'blob'
})

downloadRequest.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

downloadRequest.interceptors.response.use(
  (response) => {
    const disposition = response.headers['content-disposition'] || ''
    let filename = 'export.xlsx'
    const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/i)
    if (match) {
      filename = decodeURIComponent(match[1].replace(/["']/g, ''))
    }
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    return response
  },
  (error) => {
    ElMessage.error('导出失败，请稍后重试')
    return Promise.reject(error)
  }
)

export function download(url: string, params?: Record<string, any>) {
  return downloadRequest.get(url, { params })
}
