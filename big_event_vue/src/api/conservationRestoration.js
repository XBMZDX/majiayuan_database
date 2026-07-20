import request from '@/utils/request'

export const getRestorationWorkbench = projectId =>
    request.get(`/conservation/projects/${projectId}/restoration-workbench`)

export const saveRestorationWorkbench = (projectId, results) =>
    request.put(`/conservation/projects/${projectId}/restoration-workbench`, { results })

export const deleteRestorationResult = resultId =>
    request.delete(`/conservation/restoration-results/${resultId}`)

export const uploadRestorationMedia = (resultId, formData) =>
    request.post(`/conservation/restoration-results/${resultId}/media`, formData)

export const getRestorationMediaContent = mediaId =>
    request.get(`/conservation/restoration-media/${mediaId}/content`, { responseType: 'blob' })

export const deleteRestorationMedia = mediaId =>
    request.delete(`/conservation/restoration-media/${mediaId}`)

export const uploadRestorationModel = (resultId, formData) =>
    request.post(`/conservation/restoration-results/${resultId}/models`, formData)

export const getRestorationModelContent = modelId =>
    request.get(`/conservation/restoration-models/${modelId}/content`, { responseType: 'blob' })

export const deleteRestorationModel = modelId =>
    request.delete(`/conservation/restoration-models/${modelId}`)

export const getRestorationVersion = (resultId, versionId) =>
    request.get(`/conservation/restoration-results/${resultId}/versions/${versionId}`)

export const restoreRestorationVersion = (resultId, versionId) =>
    request.post(`/conservation/restoration-results/${resultId}/versions/${versionId}/restore`)
