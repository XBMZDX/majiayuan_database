import request from '@/utils/request'

// request.js 已配置 baseURL 为 /api；这里不能再重复添加 /api，
// 否则浏览器会请求 /api/api/digital-resources 并导致所有资源库接口 404。
const base = '/digital-resources'

export const getSummary = (p) => request({ url: `${base}/summary`, method: 'get', params: p })
export const getList = (p) => request({ url: base, method: 'get', params: p })
export const createResource = (data) => request({ url: base, method: 'post', data })
export const batchUploadResources = (data) => request({ url: `${base}/batch-upload`, method: 'post', data })
export const getDetail = (id) => request({ url: `${base}/${id}`, method: 'get' })
export const downloadResource = (id) => request({ url: `${base}/${id}/download`, method: 'get', responseType: 'blob' })
export const updateMeta = (id, data) => request({ url: `${base}/${id}`, method: 'put', data })
export const softDelete = (id) => request({ url: `${base}/${id}`, method: 'delete' })
export const restore = (id) => request({ url: `${base}/${id}/restore`, method: 'post' })
export const permanentDelete = (id) => request({ url: `${base}/${id}/permanent`, method: 'delete' })
export const getOperations = (id) => request({ url: `${base}/${id}/operations`, method: 'get' })
export const addRelation = (id, data) => request({ url: `${base}/${id}/relations`, method: 'post', data })
export const removeRelation = (relId) => request({ url: `${base}/relations/${relId}`, method: 'delete' })
export const setPrimary = (relId) => request({ url: `${base}/relations/${relId}/set-primary`, method: 'post' })
export const getVersions = (id) => request({ url: `${base}/${id}/versions`, method: 'get' })
export const setTags = (id, data) => request({ url: `${base}/${id}/tags`, method: 'post', data })
export const getAllTags = () => request({ url: `${base}/tags`, method: 'get' })
export const createTag = (data) => request({ url: `${base}/tags`, method: 'post', data })
export const batchTags = (data) => request({ url: `${base}/batch/tags`, method: 'post', data })
export const batchDelete = (data) => request({ url: `${base}/batch/delete`, method: 'post', data })
