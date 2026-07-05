import request from './request'

export function aiPhotoClassify(data: FormData) {
  return request.post('/ai/photo-classify', data)
}

export function aiFaultPredict(assetId: number) {
  return request.get('/ai/fault-predict', { params: { assetId } })
}

export function aiRouteRecommend() {
  return request.get('/ai/route-recommend')
}
