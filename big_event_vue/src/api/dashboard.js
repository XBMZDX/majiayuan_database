import request from '@/utils/request'

export const getHomeDashboard = () => request.get('/dashboard/overview')
