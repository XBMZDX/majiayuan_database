import request from '@/utils/request'

// 后端保护修复模块完成后改为 false，即可切换到真实接口。
export const USE_CONSERVATION_ARCHIVE_MOCK = true

export const getConservationProject = (projectId) =>
    request.get(`/conservation/projects/${projectId}`)

export const getProjectArchive = (projectId) =>
    request.get(`/conservation/projects/${projectId}/archive`)

export const createProjectArchive = (projectId, data) =>
    request.post(`/conservation/projects/${projectId}/archive`, data)

export const updateArchive = (archiveId, data) =>
    request.put(`/conservation/archives/${archiveId}`, data)

export const submitArchive = (archiveId) =>
    request.post(`/conservation/archives/${archiveId}/submit`)

export const createArchiveRevision = (archiveId, data) =>
    request.post(`/conservation/archives/${archiveId}/revision`, data)

export const getArchiveSections = (archiveId) =>
    request.get(`/conservation/archives/${archiveId}/sections`)

export const updateArchiveSection = (sectionId, data) =>
    request.put(`/conservation/archive-sections/${sectionId}`, data)

export const getLatestDiseaseSurvey = (projectId) =>
    request.get(`/conservation/projects/${projectId}/latest-disease-survey`)

export const getDiseaseRecords = (surveyId) =>
    request.get(`/conservation/disease-surveys/${surveyId}/records`)

export const getDetectionReferences = (projectId) =>
    request.get(`/conservation/projects/${projectId}/detection-references`)

export const getArchivePlan = (archiveId) =>
    request.get(`/conservation/archives/${archiveId}/plan`)

export const saveArchivePlan = (archiveId, data) =>
    request.post(`/conservation/archives/${archiveId}/plan`, data)

export const updateConservationPlan = (planId, data) =>
    request.put(`/conservation/plans/${planId}`, data)

export const getConservationMaterials = () =>
    request.get('/conservation/materials')

export const getPlanMaterials = (planId) =>
    request.get(`/conservation/plans/${planId}/materials`)

export const addPlanMaterial = (planId, data) =>
    request.post(`/conservation/plans/${planId}/materials`, data)

export const updatePlanMaterial = (id, data) =>
    request.put(`/conservation/plan-materials/${id}`, data)

export const deletePlanMaterial = (id) =>
    request.delete(`/conservation/plan-materials/${id}`)

export const getProjectProcessSummary = (projectId) =>
    request.get(`/conservation/projects/${projectId}/process-summary`)

export const getProjectComparisonSummary = (projectId) =>
    request.get(`/conservation/projects/${projectId}/comparison-summary`)

export const getProjectRestorationSummary = (projectId) =>
    request.get(`/conservation/projects/${projectId}/restoration-summary`)

export const getProjectMonitoringSummary = (projectId) =>
    request.get(`/conservation/projects/${projectId}/monitoring-summary`)

export const getArchiveAttachments = (archiveId) =>
    request.get(`/conservation/archives/${archiveId}/attachments`)

export const addArchiveAttachment = (archiveId, data) =>
    request.post(`/conservation/archives/${archiveId}/attachments`, data)

export const deleteArchiveAttachment = (id) =>
    request.delete(`/conservation/archive-attachments/${id}`)

export const getArchiveRevisions = (archiveId) =>
    request.get(`/conservation/archives/${archiveId}/revisions`)
