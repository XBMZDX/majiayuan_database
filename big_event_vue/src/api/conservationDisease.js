import request from '@/utils/request'

export const getDiseaseWorkbench = projectId =>
    request.get(`/conservation/projects/${projectId}/disease-workbench`)

export const saveDiseaseWorkbench = (projectId, survey, records, submit = false) =>
    request.put(`/conservation/projects/${projectId}/disease-workbench`, {
        survey,
        records,
        submit
    })

export const uploadDiseaseMedia = (recordId, formData) =>
    request.post(`/conservation/disease-records/${recordId}/media`, formData)

export const getDiseaseMediaContent = mediaId =>
    request.get(`/conservation/disease-media/${mediaId}/content`, { responseType: 'blob' })

export const saveDiseaseMediaAnnotations = (mediaId, annotations) =>
    request.put(`/conservation/disease-media/${mediaId}/annotations`, { annotations })

export const deleteDiseaseMedia = mediaId =>
    request.delete(`/conservation/disease-media/${mediaId}`)
