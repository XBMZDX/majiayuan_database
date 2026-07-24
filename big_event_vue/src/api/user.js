//导入请求函数工具
import request from '@/utils/request.js'

//提供调用注册接口的函数
export const userRegisterService = (registerData)=>{
    const params = new URLSearchParams()
    for(let key in registerData)
    {
        params.append(key,registerData[key]);
    }
    return request.post('/user/register',params);
}

//提供登录函数
export const userLoginService = (loginData)=>{
    const params = new URLSearchParams()
    for(let key in loginData)
    {
        params.append(key,loginData[key]);
    }
    return request.post('/user/login',params)
}

//获取用户详细信息
export const userInfoService = ()=>{
    return request.get('/user/userInfo')
}

//修改个人信息
export const userInfoUpdateService =(userInfoData)=>{
    return request.put('/user/update',userInfoData)
}


export const userAvatarUploadService = (file) => {
    const data = new FormData()
    data.append('file', file)
    return request.post('/user/avatar', data)
}

export const userPasswordUpdateService = (passwordData) => {
    return request.patch('/user/updatePwd', passwordData)
}

export const userSecurityOverviewService = () => request.get('/user/security/overview')
export const userSecurityLogsService = (limit = 20) => request.get('/user/security/logs', { params: { limit } })
export const userSessionsService = (limit = 20) => request.get('/user/sessions', { params: { limit } })
export const revokeUserSessionService = (sessionId) => request.delete(`/user/sessions/${sessionId}`)
export const userLogoutService = () => request.post('/user/logout')
