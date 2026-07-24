<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Plus, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import useUserInfoStore from '@/stores/userInfo.js'
import {
    revokeUserSessionService,
    userAvatarUploadService,
    userInfoUpdateService,
    userSecurityLogsService,
    userSecurityOverviewService,
    userSessionsService
} from '@/api/user.js'
import { useTokenStore } from '@/stores/token.js'
import defaultAvatar from '@/assets/default.png'

const router = useRouter()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const formRef = ref()
const uploadRef = ref()
const loading = ref(false)
const avatarSaving = ref(false)
const avatarFile = ref(null)
const avatarPreview = ref('')
const securityLogs = ref([])
const sessions = ref([])
const account = ref({})
const userInfo = reactive({ username: '', nickname: '', email: '', userPic: '', create_time: '', update_time: '', lastLoginTime: '' })

const rules = {
    nickname: [
        { required: true, message: '请输入用户昵称', trigger: 'blur' },
        { pattern: /^\S{2,10}$/, message: '昵称必须是 2-10 位的非空字符串', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入用户邮箱', trigger: 'blur' },
        { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ]
}

const formatTime = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : '—'
const securityEventName = (value) => ({
    login: '登录成功', logout: '退出登录', profile_update: '更新基本资料',
    avatar_update: '更新头像', password_change: '修改密码', session_revoke: '下线会话'
}[value] || value || '—')
const sessionStatusName = (row) => row.current ? '当前会话' : ({ active: '在线', logged_out: '已退出', revoked: '已下线' }[row.status] || row.status || '—')
const displayAvatar = () => avatarPreview.value || userInfo.userPic || defaultAvatar

const syncUser = (user = {}) => {
    Object.assign(userInfo, user)
    userInfo.userPic = user.userPic || user.user_pic || ''
    userInfoStore.setInfo({ ...user, userPic: userInfo.userPic, user_pic: userInfo.userPic })
}

const loadAccount = async () => {
    loading.value = true
    try {
        const [overview, logs, sessionList] = await Promise.all([
            userSecurityOverviewService(), userSecurityLogsService(), userSessionsService()
        ])
        account.value = overview.data || {}
        syncUser(account.value.user || {})
        securityLogs.value = logs.data || []
        sessions.value = sessionList.data || []
    } finally {
        loading.value = false
    }
}

const updateUserInfo = async () => {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    const result = await userInfoUpdateService({ nickname: userInfo.nickname, email: userInfo.email })
    ElMessage.success(result.message || '基本资料已保存')
    await loadAccount()
}

const selectAvatar = () => uploadRef.value?.$el?.querySelector('input')?.click()
const onAvatarChange = (uploadFile) => {
    const file = uploadFile.raw
    if (!file) return
    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
    if (!allowed.includes(file.type)) {
        ElMessage.error('头像仅支持 JPG、PNG、WEBP 或 GIF 格式')
        uploadRef.value?.clearFiles()
        return
    }
    if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('头像图片不能超过 5MB')
        uploadRef.value?.clearFiles()
        return
    }
    if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
    avatarFile.value = file
    avatarPreview.value = URL.createObjectURL(file)
}

const saveAvatar = async () => {
    if (!avatarFile.value) {
        ElMessage.warning('请先选择头像图片')
        return
    }
    avatarSaving.value = true
    try {
        const result = await userAvatarUploadService(avatarFile.value)
        const avatarUrl = result.data?.avatarUrl || ''
        if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
        avatarPreview.value = ''
        avatarFile.value = null
        uploadRef.value?.clearFiles()
        userInfo.userPic = avatarUrl
        userInfoStore.setInfo({ ...userInfoStore.info, userPic: avatarUrl, user_pic: avatarUrl })
        ElMessage.success('头像已更新')
        await loadAccount()
    } finally {
        avatarSaving.value = false
    }
}

const revokeSession = async (row) => {
    await ElMessageBox.confirm(
        row.current ? '下线当前会话后需要重新登录，是否继续？' : '确定下线该登录会话吗？',
        '确认下线', { type: 'warning' }
    )
    await revokeUserSessionService(row.sessionId)
    ElMessage.success('会话已下线')
    if (row.current) {
        tokenStore.removeToken()
        userInfoStore.removeInfo()
        router.replace('/Login')
        return
    }
    await loadAccount()
}

onMounted(loadAccount)
onBeforeUnmount(() => { if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value) })
</script>

<template>
    <div class="profile-page" v-loading="loading">
        <el-card class="page-container overview-card">
            <div class="overview-content">
                <el-avatar :size="76" :src="displayAvatar()" />
                <div class="overview-main">
                    <h2>{{ userInfo.nickname || userInfo.username || '个人中心' }}</h2>
                    <div class="account-name">登录账号：{{ userInfo.username || '—' }}</div>
                    <div class="overview-meta">
                        <span>注册时间：{{ formatTime(userInfo.create_time) }}</span>
                        <span>最近登录：{{ formatTime(userInfo.lastLoginTime) }}</span>
                        <span>当前会话：{{ account.activeSessionCount || 0 }}</span>
                    </div>
                </div>
                <el-button type="primary" plain :icon="EditPen" @click="router.push('/user/resetPassword')">修改密码</el-button>
            </div>
        </el-card>

        <el-row :gutter="16" class="content-row">
            <el-col :xs="24" :lg="15">
                <el-card class="page-container">
                    <template #header><div class="header"><span>基本资料</span></div></template>
                    <el-form ref="formRef" :model="userInfo" :rules="rules" label-width="100px" size="large" class="profile-form">
                        <el-form-item label="登录名称"><el-input v-model="userInfo.username" disabled /></el-form-item>
                        <el-form-item label="用户昵称" prop="nickname"><el-input v-model="userInfo.nickname" /></el-form-item>
                        <el-form-item label="用户邮箱" prop="email"><el-input v-model="userInfo.email" /></el-form-item>
                        <el-form-item label="资料更新时间"><span>{{ formatTime(userInfo.update_time) }}</span></el-form-item>
                        <el-form-item label="密码修改时间"><span>{{ formatTime(account.lastPasswordChangeTime) }}</span></el-form-item>
                        <el-form-item><el-button type="primary" @click="updateUserInfo">保存基本资料</el-button></el-form-item>
                    </el-form>
                </el-card>
            </el-col>
            <el-col :xs="24" :lg="9">
                <el-card class="page-container avatar-card">
                    <template #header><div class="header"><span>头像</span></div></template>
                    <el-upload ref="uploadRef" action="#" :auto-upload="false" :show-file-list="false" :on-change="onAvatarChange" accept="image/jpeg,image/png,image/webp,image/gif">
                        <img class="avatar-preview" :src="displayAvatar()" alt="用户头像" />
                    </el-upload>
                    <div class="avatar-tip">支持 JPG、PNG、WEBP、GIF，最大 5MB</div>
                    <div class="avatar-actions">
                        <el-button :icon="Plus" @click="selectAvatar">选择图片</el-button>
                        <el-button type="primary" :icon="Upload" :loading="avatarSaving" :disabled="!avatarFile" @click="saveAvatar">确认上传</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="16" class="content-row">
            <el-col :xs="24" :lg="14">
                <el-card class="page-container">
                    <template #header><div class="header"><span>账号安全记录</span></div></template>
                    <el-table :data="securityLogs" size="small" empty-text="暂无安全记录">
                        <el-table-column label="操作" width="130"><template #default="{ row }">{{ securityEventName(row.eventType) }}</template></el-table-column>
                        <el-table-column prop="eventDetail" label="详情" min-width="180" show-overflow-tooltip />
                        <el-table-column prop="ipAddress" label="IP 地址" width="130" />
                        <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
                    </el-table>
                </el-card>
            </el-col>
            <el-col :xs="24" :lg="10">
                <el-card class="page-container">
                    <template #header><div class="header"><span>登录会话</span></div></template>
                    <el-table :data="sessions" size="small" empty-text="暂无登录会话">
                        <el-table-column label="设备 / IP" min-width="150">
                            <template #default="{ row }"><div>{{ row.loginIp || '未知 IP' }}</div><small>{{ sessionStatusName(row) }}</small></template>
                        </el-table-column>
                        <el-table-column label="登录时间" width="150"><template #default="{ row }">{{ formatTime(row.loginTime) }}</template></el-table-column>
                        <el-table-column label="操作" width="70"><template #default="{ row }"><el-button v-if="row.status === 'active'" text type="danger" size="small" @click="revokeSession(row)">下线</el-button></template></el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped>
.profile-page { max-width: 1280px; }
.overview-card { margin-bottom: 16px; }
.overview-content { display: flex; align-items: center; gap: 18px; }
.overview-main { flex: 1; min-width: 0; }
.overview-main h2 { margin: 0 0 7px; color: #1D2129; font-size: 20px; }
.account-name { color: #4E5969; font-size: 14px; }
.overview-meta { display: flex; gap: 18px; flex-wrap: wrap; margin-top: 10px; color: #86909C; font-size: 12px; }
.content-row { margin-bottom: 16px; }
.profile-form { max-width: 620px; }
.avatar-card :deep(.el-card__body) { text-align: center; }
.avatar-preview { width: 148px; height: 148px; display: block; object-fit: cover; border-radius: 50%; border: 1px solid #E5E6EB; cursor: pointer; }
.avatar-tip { margin-top: 12px; color: #86909C; font-size: 12px; }
.avatar-actions { display: flex; justify-content: center; gap: 8px; margin-top: 14px; }
small { color: #86909C; }
@media (max-width: 768px) { .overview-content { align-items: flex-start; flex-wrap: wrap; } .overview-meta { gap: 8px; } }
</style>
