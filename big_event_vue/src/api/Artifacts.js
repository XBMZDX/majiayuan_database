//导入request工具类
import request from '@/utils/request.js'

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

//文物批量删除
export const artifactsBatchDeleteService = (ids) => {
    return request.post('/artifacts/batch-delete', ids)
}

// 文物详情图片图库
export const artifactImagesListService = (artifactId) => {
    return request.get(`/artifacts/${artifactId}/images`)
}

export const artifactImageUploadService = (artifactId, formData) => {
    return request.post(`/artifacts/${artifactId}/images`, formData)
}

export const artifactImageUpdateService = (imageId, imageData) => {
    return request.put(`/artifacts/images/${imageId}`, imageData)
}

export const artifactImageDeleteService = (imageId) => {
    return request.delete(`/artifacts/images/${imageId}`)
}

export const artifactImageSetCoverService = (imageId) => {
    return request.post(`/artifacts/images/${imageId}/cover`)
}
