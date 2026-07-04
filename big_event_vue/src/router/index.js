import {createRouter,createWebHistory} from 'vue-router'
import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/layout.vue'

import ArtifactsManage from '@/views/article/ArtifactsManage.vue'
import show from '@/views/article/show.vue'
import TombManage from '@/views/article/TombManage.vue'
import TombExcavation from '@/views/article/TombExcavation.vue'
import CoffinBasic from '@/views/article/CoffinBasic.vue'
import CoffinArtifacts from '@/views/article/CoffinArtifacts.vue'
import ChariotBasic from '@/views/article/ChariotBasic.vue'
import ChariotArtifacts from '@/views/article/ChariotArtifacts.vue'
import DetectionAnalysis from '@/views/article/DetectionAnalysis.vue'
import Conservation from '@/views/article/Conservation.vue'
import Restoration from '@/views/article/Restoration.vue'
import DigitalLibrary from '@/views/article/DigitalLibrary.vue'
import AiChat from '@/views/article/AiChat.vue'
import UserAvatarVue from '@/views/user/UserAvatar.vue'
import UserInfoVue from '@/views/user/UserInfo.vue'
import UserResetPasswordVue from '@/views/user/UserResetPassword.vue'

const routes = [
    {path:'/Login',component:LoginVue},
    {path:'/',component:LayoutVue,redirect:'/show/manage',
        children:[
        {path:'/show/manage',component:show},
        {path:'/artifacts/manage',component:ArtifactsManage},
        {path:'/tomb/manage',component:TombManage},
        {path:'/tomb/basic',component:TombManage},
        {path:'/tomb/excavation',component:TombExcavation},
        {path:'/tomb/coffin',component:TombManage},
        {path:'/tomb/coffin/basic',component:CoffinBasic},
        {path:'/tomb/coffin/artifacts',component:CoffinArtifacts},
        {path:'/tomb/chariot',component:TombManage},
        {path:'/tomb/chariot/basic',component:ChariotBasic},
        {path:'/tomb/chariot/artifacts',component:ChariotArtifacts},
        {path:'/detection/manage',component:DetectionAnalysis},
        {path:'/conservation/manage',component:Conservation},
        {path:'/restoration/manage',component:Restoration},
        {path:'/library/manage',component:DigitalLibrary},
        {path:'/ai/manage',component:AiChat},
        {path:'/user/info',component:UserInfoVue},
        {path:'/user/avatar',component:UserAvatarVue},
        {path:'/user/resetPassword',component:UserResetPasswordVue}
    ]}
]

const router = createRouter({
    history:createWebHistory(),
    routes:routes
})

//路由守卫
import { useTokenStore } from '@/stores/token.js'

// 解析 JWT，判断是否有效（未过期）
const isTokenValid = (token) => {
    if (!token) return false
    try {
        // JWT 由三段组成，取中间 payload 段解码
        const payload = JSON.parse(atob(token.split('.')[1]))
        // exp 为秒级时间戳，转为毫秒与当前时间比较
        if (payload.exp && payload.exp * 1000 < Date.now()) return false
        return true
    } catch (e) {
        return false
    }
}

router.beforeEach((to, from, next) => {
    const tokenStore = useTokenStore()
    const valid = isTokenValid(tokenStore.token)
    // token 无效（不存在或已过期）则清除
    if (!valid && tokenStore.token) tokenStore.removeToken()

    // 已登录用户访问登录页 → 跳首页
    if (to.path === '/Login' && valid) { next('/'); return }
    // 未登录/过期用户访问非登录页 → 跳登录页
    if (to.path !== '/Login' && !valid) { next('/Login'); return }
    next()
})

export default router