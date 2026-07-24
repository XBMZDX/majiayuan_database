<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { userPasswordUpdateService } from '@/api/user.js'
import { useTokenStore } from '@/stores/token.js'
import useUserInfoStore from '@/stores/userInfo.js'

const router = useRouter()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const formRef = ref()
const submitting = ref(false)
const form = reactive({ old_pwd: '', new_pwd: '', re_pwd: '' })

const validateConfirmPassword = (rule, value, callback) => {
    if (!value) callback(new Error('请再次输入新密码'))
    else if (value !== form.new_pwd) callback(new Error('两次输入的新密码不一致'))
    else callback()
}

const rules = {
    old_pwd: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    new_pwd: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { pattern: /^\S{5,16}$/, message: '密码应为 5-16 位非空字符', trigger: 'blur' }
    ],
    re_pwd: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const submit = async () => {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
        await userPasswordUpdateService(form)
        ElMessage.success('密码已修改，请使用新密码重新登录')
        tokenStore.removeToken()
        userInfoStore.removeInfo()
        router.replace('/Login')
    } finally {
        submitting.value = false
    }
}
</script>

<template>
    <el-card class="page-container password-card">
        <template #header>
            <div class="header"><span>重置密码</span></div>
        </template>
        <el-alert title="修改密码后，当前账号的所有旧登录状态都会立即失效。" type="warning" :closable="false" show-icon />
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large" class="password-form" @submit.prevent>
            <el-form-item label="当前密码" prop="old_pwd">
                <el-input v-model="form.old_pwd" :prefix-icon="Lock" type="password" show-password autocomplete="current-password" placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="new_pwd">
                <el-input v-model="form.new_pwd" :prefix-icon="Lock" type="password" show-password autocomplete="new-password" placeholder="5-16 位非空字符" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="re_pwd">
                <el-input v-model="form.re_pwd" :prefix-icon="Lock" type="password" show-password autocomplete="new-password" placeholder="请再次输入新密码" @keyup.enter="submit" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" :loading="submitting" @click="submit">确认修改并重新登录</el-button>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<style scoped>
.password-card { max-width: 760px; }
.password-form { width: 520px; max-width: 100%; margin-top: 24px; }
</style>
