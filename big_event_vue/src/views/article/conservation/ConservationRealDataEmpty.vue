<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMonitoringProject } from '@/api/conservationMonitoring'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const project = ref(null)
const error = ref('')
const projectId = computed(() => Number(route.params.projectId) || null)
const moduleName = computed(() => String(route.meta.moduleName || '当前模块'))

onMounted(async () => {
    try {
        if (!projectId.value) throw new Error('缺少项目编号')
        const result = await getMonitoringProject(projectId.value)
        project.value = result.data
    } catch (exception) {
        error.value = exception?.message || '项目加载失败'
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <div class="real-empty-page" v-loading="loading">
        <el-result
            v-if="error"
            icon="error"
            title="无法加载项目"
            :sub-title="error"
        >
            <template #extra>
                <el-button @click="router.push('/conservation/overview')">返回项目总览</el-button>
            </template>
        </el-result>
        <template v-else-if="project">
            <div class="project-heading">
                <div>
                    <span>{{ project.projectCode }}</span>
                    <h2>{{ project.projectName }}</h2>
                    <p>{{ project.artifactCode }} {{ project.artifactName }}</p>
                </div>
                <el-button @click="router.push('/conservation/overview')">返回项目总览</el-button>
            </div>
            <el-empty
                :description="`${moduleName}暂无数据库记录`"
                :image-size="110"
            >
                <p class="empty-tip">
                    旧示例数据已停用。该模块尚未完成数据库接口前，不会使用浏览器缓存或虚拟数据代替。
                </p>
                <el-button
                    type="primary"
                    @click="router.push(`/conservation/project/${projectId}/monitoring`)"
                >
                    进入后续监测
                </el-button>
            </el-empty>
        </template>
    </div>
</template>

<style scoped>
.real-empty-page { min-height: 460px; padding: 24px; background: #fff; border-radius: 10px; }
.project-heading { display: flex; justify-content: space-between; gap: 20px; padding-bottom: 20px; border-bottom: 1px solid #ebeef5; }
.project-heading span, .project-heading p { color: #909399; font-size: 13px; }
.project-heading h2 { margin: 6px 0; color: #303133; font-size: 22px; }
.empty-tip { max-width: 520px; margin: 0 auto 16px; color: #909399; line-height: 1.7; }
</style>
