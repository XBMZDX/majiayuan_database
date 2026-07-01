//导入request工具类
import request from '@/utils/request.js'
import { useTokenStore } from '../stores/token'

//文物检测列表查询
export const detectionListService = (params) => {
    return request.get('/artifact-detections', { params: params })
}

//文物检测添加
export const detectionAddService = (detectionData) => {
    return request.post('/artifact-detections', detectionData)
}

//文物检测更新
export const detectionUpdateService = (detectionData) => {
    return request.put('/artifact-detections', detectionData)
}

//文物检测删除
export const detectionDeleteService = (id) => {
    return request.delete('/artifact-detections?id=' + id)
}