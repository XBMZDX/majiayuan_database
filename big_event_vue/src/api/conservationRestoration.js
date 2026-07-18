import request from '@/utils/request'

export const USE_CONSERVATION_RESTORATION_MOCK = true

export const getRestorationProject = projectId => request.get(`/api/conservation/projects/${projectId}`)
export const getRestorationResults = projectId => request.get(`/api/conservation/projects/${projectId}/restoration-results`)
export const createRestorationResult = (projectId, data) => request.post(`/api/conservation/projects/${projectId}/restoration-results`, data)
export const createRestorationFromSteps = (projectId, data) => request.post(`/api/conservation/projects/${projectId}/restoration-results/from-steps`, data)
export const createRestorationFromComparisons = (projectId, data) => request.post(`/api/conservation/projects/${projectId}/restoration-results/from-comparisons`, data)
export const getRestorationResult = resultId => request.get(`/api/conservation/restoration-results/${resultId}`)
export const updateRestorationResult = (resultId, data) => request.put(`/api/conservation/restoration-results/${resultId}`, data)
export const deleteRestorationResult = resultId => request.delete(`/api/conservation/restoration-results/${resultId}`)
export const completeRestorationResult = resultId => request.post(`/api/conservation/restoration-results/${resultId}/complete`)
export const selectRestorationForArchive = (resultId, selected) => request.post(`/api/conservation/restoration-results/${resultId}/archive-select`, { selected })
export const recommendRestorationResult = (resultId, selected) => request.post(`/api/conservation/restoration-results/${resultId}/recommend`, { selected })
export const selectRestorationForMonitoring = (resultId, selected) => request.post(`/api/conservation/restoration-results/${resultId}/monitoring-select`, { selected })

const resourceApi = (resource, itemPath) => ({
    list: resultId => request.get(`/api/conservation/restoration-results/${resultId}/${resource}`),
    create: (resultId, data) => request.post(`/api/conservation/restoration-results/${resultId}/${resource}`, data),
    update: (id, data) => request.put(`/api/conservation/${itemPath}/${id}`, data),
    remove: id => request.delete(`/api/conservation/${itemPath}/${id}`)
})

export const restorationPartApi = resourceApi('parts', 'restoration-parts')
export const restorationSourceApi = resourceApi('sources', 'restoration-sources')
export const restorationMediaApi = resourceApi('media', 'restoration-media')
export const restorationModelApi = resourceApi('models', 'restoration-models')
export const restorationVersionApi = {
    ...resourceApi('versions', 'restoration-versions'),
    setCurrent: id => request.post(`/api/conservation/restoration-versions/${id}/set-current`),
    setRecommended: id => request.post(`/api/conservation/restoration-versions/${id}/set-recommended`)
}
export const getRestorationSources = projectId => request.get(`/api/conservation/projects/${projectId}/restoration-sources`)
export const getRestorationStatistics = projectId => request.get(`/api/conservation/projects/${projectId}/restoration-statistics`)

