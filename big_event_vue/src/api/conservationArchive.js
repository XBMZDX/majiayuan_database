import request from '@/utils/request'

export const getConservationProject = (projectId) =>
    request.get(`/conservation/projects/${projectId}`)

export const getProjectArchive = (projectId) =>
    request.get(`/conservation/projects/${projectId}/archive`)

export const createProjectArchive = (projectId, data) =>
    request.post(`/conservation/projects/${projectId}/archive`, data)

export const updateArchive = (archiveId, archive, workspace, completeness) =>
    request.put(`/conservation/archives/${archiveId}`, { archive, workspace, completeness })

export const finalizeArchive = (archiveId, archive, workspace, completeness) =>
    request.post(`/conservation/archives/${archiveId}/finalize`, { archive, workspace, completeness })

export const exportArchiveFile = (archiveId, format) =>
    request.get(`/conservation/archives/${archiveId}/export`, {
        params: { format },
        responseType: 'blob'
    })

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

export const getDetectionCandidates = projectId =>
    request.get(`/conservation/projects/${projectId}/detection-candidates`)

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

export const getArchiveAttachmentContent = id =>
    request.get(`/conservation/archive-attachments/${id}/content`, { responseType: 'blob' })

export const getArchiveRevisions = (archiveId) =>
    request.get(`/conservation/archives/${archiveId}/revisions`)
