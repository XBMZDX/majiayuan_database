//导入request工具类
import request from '@/utils/request.js'
import { useTokenStore } from '../stores/token'
//后端接口

//文物列表查询
export const artifactsListService = (params) => {
    return request.get('/artifacts', { params: params })
}

//文物添加
export const artifactsAddService = (artifactData) => {
    return request.post('/artifacts', artifactData)
}

//文物更新
export const artifactsUpdateService = (artifactData) => {
    return request.put('/artifacts', artifactData)
}

//文物删除
export const artifactsDeleteService = (id) => {
    return request.delete('/artifacts?id=' + id)
}

//文物批量导入
export const artifactsBatchImportService = (artifactsData) => {
    return request.post('/artifacts/batch', artifactsData)
}