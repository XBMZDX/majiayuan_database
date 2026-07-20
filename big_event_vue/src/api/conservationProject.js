import request from '@/utils/request'

export const getConservationProjects = () => request.get('/conservation/projects')
export const createConservationProject = data => request.post('/conservation/projects', data)
export const updateConservationProject = (projectId, data) =>
    request.put(`/conservation/projects/${projectId}`, data)
export const deleteConservationProject = projectId =>
    request.delete(`/conservation/projects/${projectId}`)
export const getProjectComparisonSummary = projectId =>
    request.get(`/conservation/projects/${projectId}/comparison-summary`)
export const getProjectRestorationSummary = projectId =>
    request.get(`/conservation/projects/${projectId}/restoration-summary`)
