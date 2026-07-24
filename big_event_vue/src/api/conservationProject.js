import request from '@/utils/request'

export const getConservationProjects = () => request.get('/conservation/projects')
export const createConservationProject = data => request.post('/conservation/projects', data)
export const updateConservationProject = (projectId, data) =>
    request.put(`/conservation/projects/${projectId}`, data)
export const deleteConservationProject = projectId =>
    request.delete(`/conservation/projects/${projectId}`)
export const searchConservationArtifacts = keyword =>
    request.get('/conservation/artifacts/search', { params: { keyword } })
export const getQuickRecord = projectId => request.get(`/conservation/projects/${projectId}/quick-record`)
export const saveQuickRecord = (projectId, data) => request.put(`/conservation/projects/${projectId}/quick-record`, data)
export const uploadQuickRecordMedia = (projectId, data) =>
    request.post(`/conservation/projects/${projectId}/quick-record/media`, data)
export const deleteQuickRecordMedia = mediaId => request.delete(`/conservation/quick-record-media/${mediaId}`)
export const getProjectComparisonSummary = projectId =>
    request.get(`/conservation/projects/${projectId}/comparison-summary`)
export const getProjectRestorationSummary = projectId =>
    request.get(`/conservation/projects/${projectId}/restoration-summary`)
