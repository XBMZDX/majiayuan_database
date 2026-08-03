<script setup>
import { userRegisterService,userLoginService } from '@/api/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
//控制注册与登录表单的显示， 默认显示注册
const isRegister = ref(false)

//定义注册数据模型
const registerData = ref({
    username:'',
    password:'',
    rePassword:''
})
//校验密码函数
const checkRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== registerData.value.password) {
    callback(new Error("请确保两次密码一样"))
  } else {
    callback()
  }
}
//定义表单校验规则
const rules={
    username:[
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 5, max: 16, message: '长度为5-16位非空字符', trigger: 'blur' },
    ],
    password:[
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 5, max: 16, message: '长度为5-16位非空字符', trigger: 'blur' },
    ],
    rePassword:[
        {validator:checkRePassword,trigger:'blur'}
    ]    
}
const register = async()=>{
    try {
        let result = await userRegisterService(registerData.value);
        ElMessage.success(result.message || '注册成功')
        // 注册成功后自动切换到登录
        isRegister.value = false
        clearRegisterData()
    } catch (error) {
        ElMessage.error(error.message || '注册失败，请重试')
    }
}
//绑定数据
//表单数据校验
import { useTokenStore } from '@/stores/token.js'
import {useRouter}from 'vue-router'
const router = useRouter()
const tokenStore =  useTokenStore();
const login = async()=>{
    try {
        let result = await userLoginService(registerData.value);
        ElMessage.success(result.message || '登陆成功')
        //把得到的token存储到pinia中
        tokenStore.setToken(result.data)
        //跳转到首页
        router.push('/')
    } catch (error) {
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    }
 }
  

//定义函数，清空数据模型里的函数
const clearRegisterData=()=>{
    registerData.value={
        username:'',
        password:'',
        rePassword:''
    }
}
</script>

<template>
    <main class="login-page">
        <div class="brand">马家塬数据库</div>
        <section class="form">
            <!-- 注册表单 -->
            <el-form ref="form" size="large" autocomplete="off" v-if="isRegister" :model = "registerData" :rules="rules">
                <el-form-item>
                    <h1>注册</h1>
                </el-form-item>
                <el-form-item prop="username">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
                </el-form-item>
                <el-form-item prop="rePassword">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入再次密码" v-model = "registerData.rePassword"></el-input>
                </el-form-item>
                <!-- 注册按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space @click="register">
                        注册
                    </el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = false; clearRegisterData()" >
                        ← 返回
                    </el-link>
                </el-form-item>
            </el-form>
            <!-- 登录表单 -->
            <el-form ref="form" size="large" autocomplete="off" v-else :model = "registerData" :rules="rules">
                <el-form-item>
                    <h1>登录</h1>
                </el-form-item>
                <el-form-item prop="username">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
                </el-form-item>
                <el-form-item class="flex">
                    <div class="flex">
                        <el-checkbox>记住我</el-checkbox>
                        <el-link type="primary" :underline="false">忘记密码？</el-link>
                    </div>
                </el-form-item>
                <!-- 登录按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = true; clearRegisterData()">
                        注册 →
                    </el-link>
                </el-form-item>
            </el-form>
        </section>
    </main>
    
</template>

<style lang="scss" scoped>
.login-page {
    position: relative;
    display: flex;
    min-height: 100vh;
    box-sizing: border-box;
    align-items: center;
    justify-content: center;
    padding: 32px;
    background: linear-gradient(135deg, #f5f8fb 0%, #eef4f7 48%, #f7fafb 100%);
}

.brand {
    position: absolute;
    top: 32px;
    left: 40px;
    color: #1d4e66;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 2px;
}

.form {
    width: min(400px, 100%);
    box-sizing: border-box;
    padding: 36px 40px 30px;
    user-select: none;
    background: rgba(255, 255, 255, 0.96);
    border: 1px solid #e3eaf0;
    border-radius: 16px;
    box-shadow: 0 18px 46px rgba(31, 72, 94, 0.13);

    h1 {
        width: 100%;
        margin: 0 0 8px;
        color: #1f2937;
        font-size: 25px;
        letter-spacing: 1px;
        text-align: center;
    }

    .button {
        width: 100%;
        height: 42px;
    }

    .flex {
        display: flex;
        width: 100%;
        justify-content: space-between;
    }
}

@media (max-width: 600px) {
    .login-page { padding: 24px; }
    .brand { top: 24px; left: 24px; font-size: 21px; }
    .form { padding: 30px 24px 24px; }
}
</style>
