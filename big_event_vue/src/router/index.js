import {createRouter,createWebHistory} from 'vue-router'
import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/layout.vue'

import RelicsManage from '@/views/article/RelicsManage.vue'
import ArtifactsManage from '@/views/article/ArtifactsManage.vue'
import ArtifactDetectionManage from '@/views/article/ArtifactDetectionManage.vue'
import UserAvatarVue from '@/views/user/UserAvatar.vue'
import UserInfoVue from '@/views/user/UserInfo.vue'
import UserResetPasswordVue from '@/views/user/UserResetPassword.vue'




import HeritageSitesManage from '@/views/article/HeritageSitesManage.vue'
import show from '@/views/article/show.vue'




//定义路由关系
//配置子路由
const routes = [
    {path:'/Login',component:LoginVue},
    {path:'/',component:LayoutVue,
        //重定向，在访问时候，默认加载的界面
        redirect:'/show/manage',
        //配置子路由
        children:[
        {path:'/site/manage',component:HeritageSitesManage},
        {path:'/relics/manage',component:RelicsManage},
        {path:'/artifacts/manage',component:ArtifactsManage},
        {path:'/artifact-detection/manage',component:ArtifactDetectionManage},
        {path:'/show/manage',component:show},
        {path:'/user/info',component:UserInfoVue},
        {path:'/user/avatar',component:UserAvatarVue},
        {path:'/user/resetPassword',component:UserResetPasswordVue}
    ]}
]

//创建路由器
const router = createRouter({
    history:createWebHistory(),
    routes:routes
})

//路由守卫：未登录时拦截所有需要认证的页面
import { useTokenStore } from '@/stores/token.js'

router.beforeEach((to, from, next) => {
    const tokenStore = useTokenStore()
    // 已登录用户访问登录页 → 重定向到首页
    if (to.path === '/Login' && tokenStore.token) {
        next('/')
        return
    }
    // 未登录用户访问非登录页 → 重定向到登录页
    if (to.path !== '/Login' && !tokenStore.token) {
        next('/Login')
        return
    }
    next()
})

//导出路由
export default router