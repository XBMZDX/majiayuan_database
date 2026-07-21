<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
    Promotion,
    UserFilled,
    User,
    Crop,
    EditPen,
    SwitchButton,
    CaretBottom,
    Document,
    Box,
    Monitor,
    Umbrella,
    Reading,
    ChatDotSquare
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'

//调用函数获取详细信息
import { userInfoService } from '@/api/user.js'
//导入pinia
import useUserInfoStore from '@/stores/userInfo.js'
import { useTokenStore } from '@/stores/token';
const tokenStore = useTokenStore();
const userInfoStore = useUserInfoStore();

//获取个人信息
const getUserInfo = async () => {
    if (!tokenStore.token) return
    try {
        let result = await userInfoService();
        userInfoStore.info = result.data;
    } catch (error) {
        console.error('获取用户信息失败:', error)
    }
}
getUserInfo()

//导入路由用于跳转和获取当前页面标题
import { ElMessage, ElMessageBox } from 'element-plus'
const router = useRouter();
const route = useRoute();

// 侧边栏菜单名称与路由的映射
const menuTitleMap = {
    '/show/manage': '展示界面',
    '/artifacts/manage': '文物信息总览',
    '/tomb/manage': '墓葬总览',
    '/tomb/basic': '工作流程',
    '/tomb/excavation': '墓葬出土',
    '/tomb/coffin': '棺信息',
    '/tomb/coffin/basic': '棺工作流程',
    '/tomb/coffin/artifacts': '棺出土文物',
    '/tomb/chariot': '车信息',
    '/tomb/chariot/basic': '车工作流程',
    '/tomb/chariot/artifacts': '车出土文物',
    '/detection/manage': '文物检测分析',
    '/detection/experiment': '检测实验总览',
    '/detection/overview': '检测分析总览',
    '/detection/result': '检测分析结果',
    '/conservation/overview': '保护修复总览',
    '/conservation/disease': '病害调查',
    '/conservation/archive': '保护修复档案',
    '/conservation/process': '修复过程记录',
    '/conservation/compare': '修复前后对比',
    '/conservation/result': '文物复原成果',
    '/conservation/monitor': '后续监测',
    '/archive/manage': '文物数字档案中心',
    '/archive/catalog': '档案目录',
    '/archive/files': '文件资源库',
    '/archive/media': '图像与多媒体',
    '/archive/models': '三维模型库',
    '/archive/research': '档案编研与导出',
    '/library/manage': '知识中心',
    '/ai/manage': 'AI智能助手',
    '/user/info': '基本资料',
    '/user/avatar': '更换头像',
    '/user/resetPassword': '重置密码'
}

// 当前页面标题（从当前路由路径计算）
const pageTitle = computed(() => {
    if (route.path.startsWith('/detection/experiment/')) return '检测分析结果详情'
    if (/^\/conservation\/project\/[^/]+\/archive$/.test(route.path)) return '保护修复档案'
    if (/^\/conservation\/project\/[^/]+\/disease$/.test(route.path)) return '病害调查'
    if (/^\/conservation\/project\/[^/]+\/process$/.test(route.path)) return '修复过程记录'
    if (/^\/conservation\/project\/[^/]+\/comparison$/.test(route.path)) return '修复前后对比'
    if (/^\/conservation\/project\/[^/]+\/restoration$/.test(route.path)) return '文物复原成果'
    if (/^\/conservation\/project\/[^/]+\/monitoring$/.test(route.path)) return '后续监测'
    return menuTitleMap[route.path] || '文物信息总览'
})

// 项目级业务路由仍高亮对应的全局导航项。
const activeMenu = computed(() => {
    const match = route.path.match(/^\/conservation\/project\/[^/]+\/([^/]+)$/)
    if (!match) return route.path
    const map = {
        archive: '/conservation/archive',
        disease: '/conservation/disease',
        process: '/conservation/process',
        comparison: '/conservation/compare',
        restoration: '/conservation/result',
        monitoring: '/conservation/monitor'
    }
    return map[match[1]] || route.path
})

const handleCommand = (command) => {
    if (command === 'logout') {
        ElMessageBox.confirm(
            '您确认退出登录吗？',
            '温馨提示',
            { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
        )
            .then(async () => {
                tokenStore.removeToken();
                userInfoStore.removeInfo();
                router.push('/login')
                ElMessage({ type: 'success', message: '退出登录成功' })
            })
            .catch(() => { ElMessage({ type: 'info', message: '已取消' }) })
    } else {
        router.push('/user/' + command)
    }
}

</script>

<template>
    <!-- element-plus 中的容器 -->
    <el-container class="layout-container">
        <!-- 左侧菜单 -->
        <el-aside width="200px">
            <div class="el-aside__logo"></div>
            <!-- 菜单标签 -->
            <el-menu :default-active="activeMenu" active-text-color="#ffd04b" background-color="#1668C4" text-color="#fff" router>
                <!-- 展示的对应组件访问的路径 -->
                <el-menu-item index="/show/manage">
                    <el-icon><Promotion /></el-icon>
                    <span>展示界面</span>
                </el-menu-item>
                <el-menu-item index="/artifacts/manage">
                    <el-icon><Document /></el-icon>
                    <span>文物信息总览</span>
                </el-menu-item>
                <el-sub-menu index="/tomb">
                    <template #title>
                        <el-icon><Box /></el-icon>
                        <span>墓葬信息</span>
                    </template>
                    <el-menu-item index="/tomb/manage">墓葬总览</el-menu-item>
                    <el-menu-item index="/tomb/basic">工作流程</el-menu-item>
                    <el-menu-item index="/tomb/excavation">墓葬出土</el-menu-item>
                    <el-sub-menu index="/tomb/coffin/group">
                        <template #title><span>棺信息</span></template>
                        <el-menu-item index="/tomb/coffin/basic">工作流程</el-menu-item>
                        <el-menu-item index="/tomb/coffin/artifacts">棺出土文物</el-menu-item>
                    </el-sub-menu>
                    <el-sub-menu index="/tomb/chariot/group">
                        <template #title><span>车信息</span></template>
                        <el-menu-item index="/tomb/chariot/basic">工作流程</el-menu-item>
                        <el-menu-item index="/tomb/chariot/artifacts">车出土文物</el-menu-item>
                    </el-sub-menu>
                </el-sub-menu>
                <el-sub-menu index="/detection/group">
                    <template #title>
                        <el-icon><Monitor /></el-icon>
                        <span>文物检测分析</span>
                    </template>
                    <el-menu-item index="/detection/experiment">检测实验总览</el-menu-item>
                    <el-menu-item index="/detection/overview">检测分析总览</el-menu-item>
                    <el-menu-item index="/detection/result">检测分析结果</el-menu-item>
                </el-sub-menu>
                <el-sub-menu index="/conservation/group">
                    <template #title>
                        <el-icon><Umbrella /></el-icon>
                        <span>文物保护、修复及复原</span>
                    </template>
                    <el-menu-item index="/conservation/overview">保护修复总览</el-menu-item>
                    <el-menu-item index="/conservation/disease">病害调查</el-menu-item>
                    <el-menu-item index="/conservation/archive">保护修复档案</el-menu-item>
                    <el-menu-item index="/conservation/process">修复过程记录</el-menu-item>
                    <el-menu-item index="/conservation/compare">修复前后对比</el-menu-item>
                    <el-menu-item index="/conservation/result">文物复原成果</el-menu-item>
                    <el-menu-item index="/conservation/monitor">后续监测</el-menu-item>
                </el-sub-menu>
                <el-sub-menu index="/archive/group">
                    <template #title>
                        <el-icon><Reading /></el-icon>
                        <span>文物数字档案中心</span>
                    </template>
                    <el-menu-item index="/archive/manage">档案资源总览</el-menu-item>
                    <el-menu-item index="/archive/catalog">档案目录</el-menu-item>
                    <el-menu-item index="/archive/files">文件资源库</el-menu-item>
                    <el-menu-item index="/archive/media">图像与多媒体</el-menu-item>
                    <el-menu-item index="/archive/models">三维模型库</el-menu-item>
                    <el-menu-item index="/archive/research">档案编研与导出</el-menu-item>
                </el-sub-menu>
                <el-menu-item index="/library/manage">
                    <el-icon><Reading /></el-icon>
                    <span>知识中心</span>
                </el-menu-item>
                <el-menu-item index="/ai/manage">
                    <el-icon><ChatDotSquare /></el-icon>
                    <span>AI智能助手</span>
                </el-menu-item>
                <el-sub-menu index="/user">
                    <template #title>
                        <el-icon>
                            <UserFilled />
                        </el-icon>
                        <span>个人中心</span>
                    </template>

                    <el-menu-item index="/user/info">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>基本资料</span>
                    </el-menu-item>

                    <el-menu-item index="/user/avatar">
                        <el-icon>
                            <Crop />
                        </el-icon>
                        <span>更换头像</span>
                    </el-menu-item>
                    <el-menu-item index="/user/resetPassword">
                        <el-icon>
                            <EditPen />
                        </el-icon>
                        <span>重置密码</span>
                    </el-menu-item>
                </el-sub-menu>
            </el-menu>

        </el-aside>
        <!-- 右侧主区域 -->
        <el-container>
            <!-- 头部区域：通栏导航 -->
            <el-header class="top-header">
                <!-- 左侧：小图标 + 当前页面标题 -->
                <div class="header-left">
                    <el-icon :size="18" color="#1668C4"><Promotion /></el-icon>
                    <span class="page-title">{{ pageTitle }}</span>
                </div>
                <!-- 右侧：用户信息下拉按钮 -->
                <el-dropdown placement="bottom-end" @command="handleCommand">
                    <div class="user-btn">
                        <el-avatar :size="28" :src="userInfoStore.info.user_pic || avatar" />
                        <span class="user-name">{{ userInfoStore.info.nickname || '未登录' }}</span>
                        <el-icon :size="12"><CaretBottom /></el-icon>
                    </div>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
                            <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
                            <el-dropdown-item command="resetPassword" :icon="EditPen">重置密码</el-dropdown-item>
                            <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-header>
            <!-- 中间区域 -->
            <el-main>
                <!-- 声明router-view标签 -->
                <router-view></router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
    height: 100vh;

    .el-aside {
        background-color: #1668C4;

        &__logo {
            height: 120px;
            background: url('@/assets/login222.png') no-repeat center / 200px auto;
        }

        .el-menu {
            border-right: none;
        }
    }

    .top-header {
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 20px;
        border-bottom: 1px solid #E5E6EB;
        height: 52px;
    }
    .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .page-title {
        font-size: 15px;
        font-weight: 600;
        color: #1D2129;
    }
    .user-btn {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 12px;
        background: #F2F3F5;
        border-radius: 20px;
        cursor: pointer;
    }
    .user-name {
        font-size: 13px;
        color: #4E5969;
    }
    .el-main { padding: 12px 20px; }
}
</style>
