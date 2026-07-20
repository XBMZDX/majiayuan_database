import request from '@/utils/request'

export const getMonitoringProject = projectId => request.get(`/conservation/projects/${projectId}`)
export const getMonitoringSources = projectId => request.get(`/conservation/projects/${projectId}/monitoring-sources`)
export const getMonitoringStatistics = projectId => request.get(`/conservation/projects/${projectId}/monitoring-statistics`)
export const getMonitoringPlans = projectId => request.get(`/conservation/projects/${projectId}/monitoring-plans`)
export const saveMonitoringWorkbench = (projectId, plans) =>
    request.put(`/conservation/projects/${projectId}/monitoring-workbench`, { plans })
export const createMonitoringPlan = (projectId, data) => request.post(`/conservation/projects/${projectId}/monitoring-plans`, data)
export const createPlanFromArchive = (projectId, data) => request.post(`/conservation/projects/${projectId}/monitoring-plans/from-archive`, data)
export const createPlanFromComparisons = (projectId, data) => request.post(`/conservation/projects/${projectId}/monitoring-plans/from-comparisons`, data)
export const createPlanFromRestoration = (projectId, data) => request.post(`/conservation/projects/${projectId}/monitoring-plans/from-restoration`, data)
export const getMonitoringPlan = planId => request.get(`/conservation/monitoring-plans/${planId}`)
export const updateMonitoringPlan = (planId, data) => request.put(`/conservation/monitoring-plans/${planId}`, data)
export const deleteMonitoringPlan = planId => request.delete(`/conservation/monitoring-plans/${planId}`)
export const changeMonitoringPlanStatus = (planId, action) => request.post(`/conservation/monitoring-plans/${planId}/${action}`)

const collectionApi = (parent, resource, item) => ({
    list: id => request.get(`/conservation/${parent}/${id}/${resource}`),
    create: (id, data) => request.post(`/conservation/${parent}/${id}/${resource}`, data),
    update: (id, data) => request.put(`/conservation/${item}/${id}`, data),
    remove: id => request.delete(`/conservation/${item}/${id}`)
})

export const monitoringTargetApi = collectionApi('monitoring-plans', 'targets', 'monitoring-targets')
export const monitoringIndicatorApi = collectionApi('monitoring-targets', 'indicators', 'monitoring-indicators')
export const monitoringBaselineApi = {
    ...collectionApi('monitoring-targets', 'baselines', 'monitoring-baselines'),
    setCurrent: id => request.post(`/conservation/monitoring-baselines/${id}/set-current`)
}
export const monitoringTaskApi = {
    ...collectionApi('monitoring-plans', 'tasks', 'monitoring-tasks'),
    start: id => request.post(`/conservation/monitoring-tasks/${id}/start`),
    complete: id => request.post(`/conservation/monitoring-tasks/${id}/complete`),
    reschedule: (id, data) => request.post(`/conservation/monitoring-tasks/${id}/reschedule`, data),
    generateNext: id => request.post(`/conservation/monitoring-plans/${id}/generate-next-task`)
}
export const monitoringRecordApi = {
    ...collectionApi('monitoring-tasks', 'records', 'monitoring-records'),
    submit: id => request.post(`/conservation/monitoring-records/${id}/submit`),
    review: id => request.post(`/conservation/monitoring-records/${id}/review`)
}
export const monitoringValueApi = collectionApi('monitoring-records', 'values', 'monitoring-values')
export const monitoringMediaApi = collectionApi('monitoring-records', 'media', 'monitoring-media')
export const getMonitoringAlerts = projectId => request.get(`/conservation/projects/${projectId}/monitoring-alerts`)
export const handleMonitoringAlert = (alertId, action, data) => request.post(`/conservation/monitoring-alerts/${alertId}/${action}`, data)
export const createSurveyFromAlert = alertId => request.post(`/conservation/monitoring-alerts/${alertId}/create-disease-survey`)
export const createProjectFromAlert = (alertId, data) =>
    request.post(`/conservation/monitoring-alerts/${alertId}/create-project`, data)
export const getMonitoringTrend = targetId => request.get(`/conservation/monitoring-targets/${targetId}/trend`)
export const getMonitoringHistory = targetId => request.get(`/conservation/monitoring-targets/${targetId}/history`)
export const getMonitoringImageTimeline = targetId => request.get(`/conservation/monitoring-targets/${targetId}/image-timeline`)
export const uploadMonitoringMedia = (recordId, formData) =>
    request.post(`/conservation/monitoring-records/${recordId}/media`, formData)
export const deleteMonitoringMedia = mediaId => request.delete(`/conservation/monitoring-media/${mediaId}`)
export const getMonitoringMediaContent = mediaId =>
    request.get(`/conservation/monitoring-media/${mediaId}/content`, { responseType: 'blob' })
export const getComparisonMediaContent = mediaId =>
    request.get(`/conservation/comparison-media/${mediaId}/content`, { responseType: 'blob' })
export const getRestorationMediaContent = mediaId =>
    request.get(`/conservation/restoration-media/${mediaId}/content`, { responseType: 'blob' })
