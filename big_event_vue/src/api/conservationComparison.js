import request from '@/utils/request'

export const USE_CONSERVATION_COMPARISON_MOCK = true

export const getComparisonProject = projectId =>
    request.get(`/api/conservation/projects/${projectId}`)
export const getComparisonProcessSummary = projectId =>
    request.get(`/api/conservation/projects/${projectId}/process-summary`)
export const getComparisonSourceMedia = projectId =>
    request.get(`/api/conservation/projects/${projectId}/comparison-source-media`)
export const getProjectComparisons = projectId =>
    request.get(`/api/conservation/projects/${projectId}/comparisons`)
export const createProjectComparison = (projectId, data) =>
    request.post(`/api/conservation/projects/${projectId}/comparisons`, data)
export const getComparison = comparisonId =>
    request.get(`/api/conservation/comparisons/${comparisonId}`)
export const updateComparison = (comparisonId, data) =>
    request.put(`/api/conservation/comparisons/${comparisonId}`, data)
export const deleteComparison = comparisonId =>
    request.delete(`/api/conservation/comparisons/${comparisonId}`)
export const completeComparison = comparisonId =>
    request.post(`/api/conservation/comparisons/${comparisonId}/complete`)
export const selectComparisonForArchive = (comparisonId, selected) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/archive-select`, { selected })
export const setComparisonMonitoringBaseline = (comparisonId, selected) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/monitoring-baseline`, { selected })

export const getComparisonImages = comparisonId =>
    request.get(`/api/conservation/comparisons/${comparisonId}/images`)
export const createComparisonImage = (comparisonId, data) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/images`, data)
export const updateComparisonImage = (imageId, data) =>
    request.put(`/api/conservation/comparison-images/${imageId}`, data)
export const deleteComparisonImage = imageId =>
    request.delete(`/api/conservation/comparison-images/${imageId}`)

export const getComparisonDiseases = comparisonId =>
    request.get(`/api/conservation/comparisons/${comparisonId}/diseases`)
export const createComparisonDisease = (comparisonId, data) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/diseases`, data)
export const updateComparisonDisease = (id, data) =>
    request.put(`/api/conservation/comparison-diseases/${id}`, data)
export const deleteComparisonDisease = id =>
    request.delete(`/api/conservation/comparison-diseases/${id}`)

export const getComparisonMetrics = comparisonId =>
    request.get(`/api/conservation/comparisons/${comparisonId}/metrics`)
export const createComparisonMetric = (comparisonId, data) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/metrics`, data)
export const updateComparisonMetric = (id, data) =>
    request.put(`/api/conservation/comparison-metrics/${id}`, data)
export const deleteComparisonMetric = id =>
    request.delete(`/api/conservation/comparison-metrics/${id}`)

export const getComparisonEvaluation = comparisonId =>
    request.get(`/api/conservation/comparisons/${comparisonId}/evaluation`)
export const createComparisonEvaluation = (comparisonId, data) =>
    request.post(`/api/conservation/comparisons/${comparisonId}/evaluation`, data)
export const updateComparisonEvaluation = (id, data) =>
    request.put(`/api/conservation/comparison-evaluations/${id}`, data)
export const getComparisonSummary = projectId =>
    request.get(`/api/conservation/projects/${projectId}/comparison-summary`)
export const getComparisonStatistics = projectId =>
    request.get(`/api/conservation/projects/${projectId}/comparison-statistics`)

