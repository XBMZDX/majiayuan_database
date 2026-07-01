<script setup>
import {
    Management,
    Promotion,
    UserFilled,
    User,
    Crop,
    EditPen,
    SwitchButton,
    CaretBottom
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'

//调用函数获取详细信息
import { userInfoService } from '@/api/user.js'
//导入pinia
import useUserInfoStore from '@/stores/userInfo.js'
import { handleError } from 'vue';
import { useTokenStore } from '@/stores/token';
const tokenStore = useTokenStore();
const userInfoStore = useUserInfoStore();

//获取个人信息
const getUserInfo = async () => {
    let result = await userInfoService();
    //存储pinia
    userInfoStore.info = result.data;
}
getUserInfo()
//条目被点击过后被掉用的函数
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
const router = useRouter();
const handleCommand = (command) => {
    //判断指令
    if (command === 'logout') {
        //退出登录


        ElMessageBox.confirm(
            '您确认退出登录吗？',
            '温馨提示',
            {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )



            .then(async () => {
                //调用接口
                tokenStore.removeToken();
                userInfoStore.removeInfo();
                router.push('/login')
                ElMessage({
                    type: 'success',
                    message: '退出登录成功',
                })
                //刷新列表

            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消',
                })
            })
    } else {
        //路由
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
            <el-menu active-text-color="#ffd04b" background-color="#232323" text-color="#fff" router> 
                <!-- 展示的对应组件访问的路径 -->
                <!-- <el-menu-item index="/show/manage">
                    <el-icon>
                        <Promotion />
                    </el-icon>
                    <span>展示界面</span>
                </el-menu-item> -->
                <el-menu-item index="/site/manage">
                    <el-icon>
                        <Management />
                    </el-icon>
                    <span>遗址管理</span>
                </el-menu-item>

                <el-menu-item index="/relics/manage">
                    <el-icon>
                        <Promotion />
                    </el-icon>
                    <span>遗迹管理</span>
                </el-menu-item>
                <el-menu-item index="/artifacts/manage">
                    <el-icon>
                        <Promotion />
                    </el-icon>
                    <span>遗物管理</span>
                </el-menu-item>
                <el-menu-item index="/artifact-detection/manage">
                    <el-icon>
                        <Promotion />
                    </el-icon>
                    <span>文物检测信息</span>
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
            <!-- 头部区域 -->
            <el-header>
                <div>登陆人：<strong>{{ userInfoStore.info.nickname }}</strong></div>
                <!-- 下拉菜单-->
                <!--command: 条目被点击后会触发，在事件函数上可以声明一个函数，接收条目对应的指令-->
                <el-dropdown placement="bottom-end" @command="handleCommand">
                    <span class="el-dropdown__box">
                        <el-avatar :src="userInfoStore.info.user_pic ? userInfoStore.info.user_pic : avatar" />
                        <el-icon>
                            <CaretBottom />
                        </el-icon>
                    </span>
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
            <!-- 底部区域 -->
            <el-footer> 马家塬文物数据库©2023 Created by 王飞洋</el-footer>
        </el-container>
    </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
    height: 100vh;

    .el-aside {
        background-color: #232323;

        &__logo {
            height: 120px;
            background: url('@/assets/login222.png') no-repeat center / 200px auto;
        }

        .el-menu {
            border-right: none;
        }
    }

    .el-header {
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .el-dropdown__box {
            display: flex;
            align-items: center;

            .el-icon {
                color: #999;
                margin-left: 10px;
            }

            &:active,
            &:focus {
                outline: none;
            }
        }
    }

    .el-footer {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
    }
}
</style>