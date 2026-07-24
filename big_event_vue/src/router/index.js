import {createRouter,createWebHistory} from 'vue-router'
const LoginVue = () => import('@/views/Login.vue')
const LayoutVue = () => import('@/views/layout.vue')
const ArtifactsManage = () => import('@/views/article/ArtifactsManage.vue')
const show = () => import('@/views/article/show.vue')
const TombManage = () => import('@/views/article/TombManage.vue')
const TombWorkflow = () => import('@/views/article/TombWorkflow.vue')
const TombExcavation = () => import('@/views/article/TombExcavation.vue')
const CoffinBasic = () => import('@/views/article/CoffinBasic.vue')
const CoffinArtifacts = () => import('@/views/article/CoffinArtifacts.vue')
const ChariotBasic = () => import('@/views/article/ChariotBasic.vue')
const ChariotArtifacts = () => import('@/views/article/ChariotArtifacts.vue')
const DetectionAnalysis = () => import('@/views/article/DetectionAnalysis.vue')
const LabInstruments = () => import('@/views/article/LabInstruments.vue')
const DetectionOverview = () => import('@/views/article/DetectionOverview.vue')
const DetectionResult = () => import('@/views/article/DetectionResult.vue')
const ExperimentDetail = () => import('@/views/article/ExperimentDetail.vue')
const UserInfoVue = () => import('@/views/user/UserInfo.vue')
const UserResetPasswordVue = () => import('@/views/user/UserResetPassword.vue')

const routes = [
    {path:'/Login',component:LoginVue},
    {path:'/',component:LayoutVue,redirect:'/show/manage',
        children:[
        {path:'/show/manage',component:show},
        {path:'/artifacts/manage',component:ArtifactsManage},
        {path:'/tomb/manage',component:TombManage},
        {path:'/tomb/basic',component:TombWorkflow},
        {path:'/tomb/excavation',component:TombExcavation},
        {path:'/tomb/coffin',component:TombManage},
        {path:'/tomb/coffin/basic',component:CoffinBasic},
        {path:'/tomb/coffin/artifacts',component:CoffinArtifacts},
        {path:'/tomb/chariot',component:TombManage},
        {path:'/tomb/chariot/basic',component:ChariotBasic},
        {path:'/tomb/chariot/artifacts',component:ChariotArtifacts},
        {path:'/detection/manage',component:DetectionAnalysis},
        {path:'/detection/overview',component:DetectionOverview},
        {path:'/detection/result',component:DetectionResult},
        {path:'/detection/experiment/:id',component:ExperimentDetail},
        {path:'/conservation/overview',component:()=>import('@/views/article/conservation/Overview.vue')},
        {path:'/conservation/disease',component:()=>import('@/views/article/conservation/ConservationDiseaseEntry.vue')},
        {path:'/conservation/archive',redirect:'/conservation/overview'},
        {path:'/conservation/process',redirect:'/conservation/overview'},
        {path:'/conservation/compare',redirect:'/conservation/overview'},
        {path:'/conservation/result',redirect:'/conservation/overview'},
        {path:'/conservation/monitor',redirect:'/conservation/overview'},
        {path:'/conservation/project/:projectId/archive',component:()=>import('@/views/article/conservation/ConservationArchive.vue')},
        {path:'/conservation/project/:projectId/disease',component:()=>import('@/views/article/conservation/DiseaseSurvey.vue')},
        {path:'/conservation/project/:projectId/quick-record',component:()=>import('@/views/article/conservation/ConservationQuickRecord.vue')},
        {path:'/conservation/project/:projectId/process',component:()=>import('@/views/article/conservation/ConservationProcess.vue')},
        {path:'/conservation/project/:projectId/comparison',component:()=>import('@/views/article/conservation/ConservationComparison.vue'),meta:{moduleName:'修复前后对比'}},
        {path:'/conservation/project/:projectId/restoration',component:()=>import('@/views/article/conservation/ConservationRestoration.vue'),meta:{moduleName:'文物复原成果'}},
        {path:'/conservation/project/:projectId/monitoring',component:()=>import('@/views/article/conservation/Monitor.vue')},
         {path:'/archive/manage',component:()=>import('@/views/article/digitalArchive/ArchiveResourceOverview.vue')},
         {path:'/archive/files',component:()=>import('@/views/article/digitalArchive/DigitalResourceLibrary.vue'),props:{documentOnly:true,showSummary:false,showUpload:false}},
         {path:'/archive/media',component:()=>import('@/views/article/digitalArchive/MediaResourceCenter.vue')},
         {path:'/archive/models',component:()=>import('@/views/article/digitalArchive/ThreeDimensionalModelLibrary.vue')},
        {path:'/detection/experiment',component:LabInstruments},
        {path:'/user/info',component:UserInfoVue},
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
