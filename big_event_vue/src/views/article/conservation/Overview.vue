<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ConservationStatistics from './components/ConservationStatistics.vue'
import ConservationStageBoard from './components/ConservationStageBoard.vue'
import ConservationFilter from './components/ConservationFilter.vue'
import ConservationProjectCard from './components/ConservationProjectCard.vue'
import ConservationProjectDialog from './components/ConservationProjectDialog.vue'
import { getMonitoringStatistics } from '@/api/conservationMonitoring'
import {
    createConservationProject,
    deleteConservationProject,
    getConservationProjects,
    getProjectComparisonSummary,
    getProjectRestorationSummary,
    updateConservationProject
} from '@/api/conservationProject'

const loading = ref(false)
const allProjects = ref([])
const activeStatKey = ref('')
const activeStage = ref('')
const filterParams = ref({})
const pageNum = ref(1)
const pageSize = ref(12)
const dialogVisible = ref(false)
const editData = ref(null)

const countByStage = stage => allProjects.value.filter(item => item.currentStage === stage).length
const statistics = computed(() => ({
    total: allProjects.value.length,
    pendingSurvey: countByStage('pendingSurvey'),
    planning: countByStage('planning'),
    repairing: countByStage('repair'),
    restoring: countByStage('restoration'),
    completed: allProjects.value.filter(item => item.status === 'completed').length,
    monitoring: countByStage('monitoring'),
    highRisk: allProjects.value.filter(item => item.riskLevel === 'high').length
}))
const stageStats = computed(() => Object.fromEntries(
    ['pendingSurvey', 'disease', 'planning', 'protection', 'repair', 'restoration', 'evaluation', 'completed', 'monitoring']
        .map(stage => [stage, countByStage(stage)])
))

const filtered = computed(() => {
    let list = allProjects.value
    const statMap = {
        highRisk: item => item.riskLevel === 'high',
        completed: item => item.status === 'completed',
        repairing: item => item.currentStage === 'repair',
        restoring: item => item.currentStage === 'restoration',
        pendingSurvey: item => item.currentStage === 'pendingSurvey',
        monitoring: item => item.currentStage === 'monitoring',
        planning: item => item.currentStage === 'planning'
    }
    if (activeStatKey.value && statMap[activeStatKey.value]) list = list.filter(statMap[activeStatKey.value])
    if (activeStage.value) list = list.filter(item => item.currentStage === activeStage.value)
    const filters = filterParams.value
    if (filters.keyword) {
        const keyword = filters.keyword.toLowerCase()
        list = list.filter(item => [item.projectCode, item.projectName, item.artifactCode]
            .some(value => String(value || '').toLowerCase().includes(keyword)))
    }
    if (filters.status) list = list.filter(item => item.status === filters.status)
    if (filters.currentStage) list = list.filter(item => item.currentStage === filters.currentStage)
    if (filters.riskLevel) list = list.filter(item => item.riskLevel === filters.riskLevel)
    if (filters.principal) list = list.filter(item => (item.principal || '').includes(filters.principal))
    return list
})
const paged = computed(() => filtered.value.slice(
    (pageNum.value - 1) * pageSize.value,
    pageNum.value * pageSize.value
))

const loadProjects = async () => {
    loading.value = true
    try {
        const result = await getConservationProjects()
        const projects = result.data || []
        await Promise.all(projects.map(async project => {
            const [comparison, restoration, monitoring] = await Promise.allSettled([
                getProjectComparisonSummary(project.id),
                getProjectRestorationSummary(project.id),
                getMonitoringStatistics(project.id)
            ])
            if (comparison.status === 'fulfilled' && Number(comparison.value.data?.total) > 0) {
                project.comparisonSummary = comparison.value.data
                if (
                    Number(comparison.value.data.completed) < Number(comparison.value.data.total)
                    && project.currentStage === 'evaluation'
                ) {
                    project.stageNotice = '修复已完成，效果评价待完善'
                }
            }
            if (restoration.status === 'fulfilled' && Number(restoration.value.data?.total) > 0) {
                project.restorationSummary = restoration.value.data
            }
            if (monitoring.status === 'fulfilled' && Number(monitoring.value.data?.plans) > 0) {
                project.monitoringSummary = monitoring.value.data
            }
        }))
        allProjects.value = projects
    } catch (error) {
        ElMessage.error(error?.message || '项目列表加载失败')
    } finally {
        loading.value = false
    }
}

const onStatFilter = key => {
    activeStatKey.value = key
    activeStage.value = ''
    pageNum.value = 1
}
const onStageFilter = key => {
    activeStage.value = key
    activeStatKey.value = ''
    pageNum.value = 1
}
const onSearch = filters => {
    filterParams.value = filters
    pageNum.value = 1
}
const onReset = () => {
    filterParams.value = {}
    activeStatKey.value = ''
    activeStage.value = ''
    pageNum.value = 1
}
const openCreate = () => {
    editData.value = null
    dialogVisible.value = true
}
const openEdit = project => {
    editData.value = { ...project }
    dialogVisible.value = true
}
const onSave = async data => {
    try {
        if (data.id) await updateConservationProject(data.id, data)
        else await createConservationProject(data)
        dialogVisible.value = false
        ElMessage.success(data.id ? '项目修改成功' : '项目创建成功')
        await loadProjects()
    } catch (error) {
        ElMessage.error(error?.message || '项目保存失败')
    }
}
const onDelete = async project => {
    try {
        await ElMessageBox.confirm(
            `确定彻底删除“${project.projectName}”吗？项目档案及其病害调查、修复过程、前后对比、复原成果、监测等全部业务记录将一并删除，且不可恢复。`,
            '彻底删除项目',
            { type: 'warning', confirmButtonText: '彻底删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
        )
        await deleteConservationProject(project.id)
        ElMessage.success('项目档案及关联业务记录已删除')
        await loadProjects()
    } catch (error) {
        if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '项目删除失败')
    }
}
const onArchive = async project => {
    try {
        await updateConservationProject(project.id, { ...project, status: 'archived' })
        ElMessage.success('项目已归档')
        await loadProjects()
    } catch (error) {
        ElMessage.error(error?.message || '项目归档失败')
    }
}

onMounted(loadProjects)
</script>

<template>
    <div class="conservation-page" v-loading="loading">
        <ConservationStatistics :statistics="statistics" :activeStatKey="activeStatKey" @filter="onStatFilter" />
        <ConservationStageBoard :stageStats="stageStats" :activeStage="activeStage" @filter="onStageFilter" />
        <ConservationFilter @search="onSearch" @reset="onReset" @create="openCreate" />
        <div v-if="paged.length" class="card-grid">
            <ConservationProjectCard
                v-for="project in paged"
                :key="project.id"
                :project="project"
                @edit="openEdit"
                @delete="onDelete"
                @archive="onArchive"
            />
        </div>
        <el-empty v-else description="暂无保护修复项目" :image-size="80" style="padding: 40px 0">
            <el-button type="primary" @click="openCreate">新建保护修复项目</el-button>
        </el-empty>
        <el-pagination
            v-if="filtered.length > pageSize"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48]"
            :total="filtered.length"
            layout="total, sizes, prev, pager, next"
            background
            style="margin-top: 20px; justify-content: flex-end"
        />
        <ConservationProjectDialog v-model="dialogVisible" :editData="editData" @save="onSave" />
    </div>
</template>

<style scoped>
.conservation-page { min-height: 320px; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
</style>
