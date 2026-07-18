import request from '@/utils/request'

export const USE_CONSERVATION_PROCESS_MOCK = true

export const getProcessProject = projectId => request.get(`/conservation/projects/${projectId}`)
export const getProcessArchive = projectId => request.get(`/conservation/projects/${projectId}/archive`)
export const getFormalPlan = projectId => request.get(`/conservation/projects/${projectId}/formal-plan`)

export const getProjectProcesses = projectId => request.get(`/conservation/projects/${projectId}/processes`)
export const getProcess = processId => request.get(`/conservation/processes/${processId}`)
export const createProcess = (projectId, data) => request.post(`/conservation/projects/${projectId}/processes`, data)
export const generateProcessSteps = processId => request.post(`/conservation/processes/${processId}/generate-steps`)
export const updateProcess = (processId, data) => request.put(`/conservation/processes/${processId}`, data)
export const pauseProcess = (processId, data) => request.post(`/conservation/processes/${processId}/pause`, data)
export const resumeProcess = (processId, data) => request.post(`/conservation/processes/${processId}/resume`, data)
export const completeProcess = processId => request.post(`/conservation/processes/${processId}/complete`)

export const getProcessSteps = processId => request.get(`/conservation/processes/${processId}/steps`)
export const createProcessStep = (processId, data) => request.post(`/conservation/processes/${processId}/steps`, data)
export const getProcessStep = stepId => request.get(`/conservation/process-steps/${stepId}`)
export const updateProcessStep = (stepId, data) => request.put(`/conservation/process-steps/${stepId}`, data)
export const deleteProcessStep = stepId => request.delete(`/conservation/process-steps/${stepId}`)
export const startProcessStep = stepId => request.post(`/conservation/process-steps/${stepId}/start`)
export const pauseProcessStep = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/pause`, data)
export const completeProcessStep = stepId => request.post(`/conservation/process-steps/${stepId}/complete`)
export const skipProcessStep = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/skip`, data)
export const reworkProcessStep = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/rework`, data)

export const getStepDiseases = stepId => request.get(`/conservation/process-steps/${stepId}/diseases`)
export const addStepDisease = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/diseases`, data)
export const deleteStepDisease = id => request.delete(`/conservation/step-diseases/${id}`)

export const getStepOperationLogs = stepId => request.get(`/conservation/process-steps/${stepId}/operation-logs`)
export const addStepOperationLog = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/operation-logs`, data)
export const updateStepOperationLog = (id, data) => request.put(`/conservation/step-operation-logs/${id}`, data)
export const deleteStepOperationLog = id => request.delete(`/conservation/step-operation-logs/${id}`)

export const getStepMaterials = stepId => request.get(`/conservation/process-steps/${stepId}/materials`)
export const addStepMaterial = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/materials`, data)
export const updateStepMaterial = (id, data) => request.put(`/conservation/step-materials/${id}`, data)
export const deleteStepMaterial = id => request.delete(`/conservation/step-materials/${id}`)

export const getStepTools = stepId => request.get(`/conservation/process-steps/${stepId}/tools`)
export const addStepTool = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/tools`, data)
export const updateStepTool = (id, data) => request.put(`/conservation/step-tools/${id}`, data)
export const deleteStepTool = id => request.delete(`/conservation/step-tools/${id}`)

export const getStepEnvironments = stepId => request.get(`/conservation/process-steps/${stepId}/environments`)
export const addStepEnvironment = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/environments`, data)
export const updateStepEnvironment = (id, data) => request.put(`/conservation/step-environments/${id}`, data)
export const deleteStepEnvironment = id => request.delete(`/conservation/step-environments/${id}`)

export const getStepMedia = stepId => request.get(`/conservation/process-steps/${stepId}/media`)
export const addStepMedia = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/media`, data)
export const updateStepMedia = (id, data) => request.put(`/conservation/step-media/${id}`, data)
export const deleteStepMedia = id => request.delete(`/conservation/step-media/${id}`)

export const getStepIssues = stepId => request.get(`/conservation/process-steps/${stepId}/issues`)
export const addStepIssue = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/issues`, data)
export const updateStepIssue = (id, data) => request.put(`/conservation/step-issues/${id}`, data)
export const resolveStepIssue = (id, data) => request.post(`/conservation/step-issues/${id}/resolve`, data)
export const closeStepIssue = id => request.post(`/conservation/step-issues/${id}/close`)

export const getStepChecks = stepId => request.get(`/conservation/process-steps/${stepId}/checks`)
export const addStepCheck = (stepId, data) => request.post(`/conservation/process-steps/${stepId}/checks`, data)
export const updateStepCheck = (id, data) => request.put(`/conservation/step-checks/${id}`, data)
export const deleteStepCheck = id => request.delete(`/conservation/step-checks/${id}`)

export const getProcessProgress = processId => request.get(`/conservation/processes/${processId}/progress`)
export const getProcessSummary = processId => request.get(`/conservation/processes/${processId}/summary`)
