import request from '@/utils/request'

export const getComparisonWorkbench = projectId =>
    request.get(`/conservation/projects/${projectId}/comparison-workbench`)

export const saveComparisonWorkbench = (projectId, groups) =>
    request.put(`/conservation/projects/${projectId}/comparison-workbench`, { groups })

export const getComparisonMediaContent = mediaId =>
    request.get(`/conservation/comparison-media/${mediaId}/content`, { responseType: 'blob' })
