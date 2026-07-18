<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import ConservationStatistics from './components/ConservationStatistics.vue'
import ConservationStageBoard from './components/ConservationStageBoard.vue'
import ConservationFilter from './components/ConservationFilter.vue'
import ConservationProjectCard from './components/ConservationProjectCard.vue'
import ConservationProjectDialog from './components/ConservationProjectDialog.vue'

// ========== Mock 数据 ==========
const statistics = ref({ total: 175, pendingSurvey: 12, planning: 8, repairing: 25, restoring: 6, completed: 83, monitoring: 23, highRisk: 18 })
const stageStats = ref({ pendingSurvey: 12, disease: 18, planning: 8, protection: 15, repair: 25, restoration: 6, evaluation: 8, completed: 83, monitoring: 8 })

const allProjects = ref([
    { id: 1, projectCode: 'CR-M45C1-001', projectName: 'M45-C1马车本体保护修复', artifactId: 101, artifactCode: 'M45-C1', artifactName: '马车本体', material: '土体、木质、金属复合材质', tombCode: 'M45', status: 'active', currentStage: 'repair', riskLevel: 'high', progress: 68, principal: '张三', startDate: '2025-12-19', updateTime: '2026-07-15', diseases: [{ name: '裂隙', level: '严重' }, { name: '坍塌', level: '严重' }, { name: '风化', level: '中度' }] },
    { id: 2, projectCode: 'CR-M16G1-002', projectName: 'M16-G1铜鼎除锈保护', artifactId: 102, artifactCode: 'M16-G1', artifactName: '铜鼎', material: '青铜', tombCode: 'M16', status: 'active', currentStage: 'protection', riskLevel: 'medium', progress: 45, principal: '李四', startDate: '2026-02-10', updateTime: '2026-07-14', diseases: [{ name: '锈蚀', level: '中度' }, { name: '矿化', level: '轻度' }] },
    { id: 3, projectCode: 'CR-M62G2-003', projectName: 'M62-G2铜盘复原', artifactId: 103, artifactCode: 'M62-G2', artifactName: '铜盘', material: '青铜', tombCode: 'M62', status: 'active', currentStage: 'restoration', riskLevel: 'low', progress: 82, principal: '王五', startDate: '2025-08-01', updateTime: '2026-07-16', diseases: [{ name: '残缺', level: '严重' }, { name: '变形', level: '中度' }, { name: '锈蚀', level: '轻度' }] },
    { id: 4, projectCode: 'CR-M25-004', projectName: 'M25漆器保护修复', artifactId: 104, artifactCode: 'M25-L1', artifactName: '漆盒', material: '木胎漆器', tombCode: 'M25', status: 'completed', currentStage: 'completed', riskLevel: 'medium', progress: 100, principal: '赵六', startDate: '2024-06-01', updateTime: '2026-06-30', diseases: [{ name: '漆皮起翘', level: '轻度' }] },
    { id: 5, projectCode: 'CR-M45-005', projectName: 'M45出土陶器保护', artifactId: 105, artifactCode: 'M45-T01', artifactName: '彩绘陶罐', material: '陶器', tombCode: 'M45', status: 'draft', currentStage: 'pendingSurvey', riskLevel: 'high', progress: 0, principal: '张三', startDate: '2026-07-01', updateTime: '2026-07-10', diseases: [] }
])

// ========== 筛选 ==========
const activeStatKey = ref(''); const activeStage = ref(''); const filterParams = ref({})
const pageNum = ref(1); const pageSize = ref(12)
const filtered = computed(() => {
    let list = allProjects.value
    const statMap = { highRisk: p => p.riskLevel === 'high', completed: p => p.status === 'completed', repairing: p => p.currentStage === 'repair', restoring: p => p.currentStage === 'restoration', pendingSurvey: p => p.currentStage === 'pendingSurvey', monitoring: p => p.currentStage === 'monitoring', planning: p => p.currentStage === 'planning' }
    if (activeStatKey.value && statMap[activeStatKey.value]) list = list.filter(statMap[activeStatKey.value])
    if (activeStage.value) list = list.filter(p => p.currentStage === activeStage.value)
    const f = filterParams.value
    if (f.keyword) { const kw = f.keyword.toLowerCase(); list = list.filter(p => [p.projectCode, p.projectName, p.artifactCode].some(v => (v || '').toLowerCase().includes(kw))) }
    if (f.status) list = list.filter(p => p.status === f.status)
    if (f.currentStage) list = list.filter(p => p.currentStage === f.currentStage)
    if (f.riskLevel) list = list.filter(p => p.riskLevel === f.riskLevel)
    if (f.principal) list = list.filter(p => (p.principal || '').includes(f.principal))
    return list
})
const paged = computed(() => filtered.value.slice((pageNum.value - 1) * pageSize.value, pageNum.value * pageSize.value))
const onStatFilter = (key) => { activeStatKey.value = key; activeStage.value = ''; pageNum.value = 1 }
const onStageFilter = (key) => { activeStage.value = key; activeStatKey.value = ''; pageNum.value = 1 }
const onSearch = (f) => { filterParams.value = f; pageNum.value = 1 }
const onReset = () => { filterParams.value = {}; activeStatKey.value = ''; activeStage.value = ''; pageNum.value = 1 }

// ========== 弹窗 ==========
const dialogVisible = ref(false); const editData = ref(null)
const openCreate = () => { editData.value = null; dialogVisible.value = true }
const openEdit = (p) => { editData.value = { ...p }; dialogVisible.value = true }
const onSave = (data) => {
    if (editData.value) { const idx = allProjects.value.findIndex(p => p.id === data.id); if (idx >= 0) allProjects.value[idx] = data }
    else allProjects.value.unshift(data)
    ElMessage.success(editData.value ? '修改成功' : '创建成功')
}
const onDelete = (p) => { ElMessageBox.confirm('确定删除该项目？', '提示', { type: 'warning' }).then(() => { allProjects.value = allProjects.value.filter(x => x.id !== p.id); ElMessage.success('已删除') }).catch(() => {}) }
const onArchive = (p) => { p.status = 'archived'; ElMessage.success('已归档') }

onMounted(() => {
    allProjects.value.forEach(project => {
        try {
            const summary = JSON.parse(localStorage.getItem(`conservation-comparison-summary:${project.id}`) || 'null')
            if (summary) {
                project.comparisonSummary = summary
                project.updateTime = summary.updateTime || project.updateTime
                if (summary.total > 0 && summary.completed < summary.total && project.currentStage === 'evaluation') {
                    project.stageNotice = '修复已完成，效果评价待完善'
                }
            }
        } catch {
            // 单个本地摘要异常不影响总览其他项目展示。
        }
        try {
            const restoration = JSON.parse(localStorage.getItem(`conservation-restoration-summary:${project.id}`) || 'null')
            if (restoration) {
                project.restorationSummary = restoration
                project.updateTime = restoration.updateTime || project.updateTime
                if (restoration.processing > 0) project.currentStage = 'restoration'
            }
        } catch {
            // 单个复原摘要异常不影响其他项目。
        }
        try {
            const monitoring = JSON.parse(localStorage.getItem(`conservation-monitoring-summary:${project.id}`) || 'null')
            if (!monitoring) return
            project.monitoringSummary = monitoring
            project.updateTime = monitoring.updateTime || project.updateTime
            if (monitoring.activePlans > 0) {
                project.currentStage = 'monitoring'
                project.progress = Math.max(project.progress || 0, 95)
                project.riskLevel = monitoring.openAlerts > 0 || monitoring.highRiskTargets > 0 ? 'high' : project.riskLevel
            }
        } catch {
            // 单个监测摘要异常不影响其他项目。
        }
    })
})
</script>

<template>
    <div class="conservation-page">
        <ConservationStatistics :statistics="statistics" :activeStatKey="activeStatKey" @filter="onStatFilter" />
        <ConservationStageBoard :stageStats="stageStats" :activeStage="activeStage" @filter="onStageFilter" />
        <ConservationFilter @search="onSearch" @reset="onReset" @create="openCreate" />
        <div class="card-grid" v-if="paged.length">
            <ConservationProjectCard v-for="p in paged" :key="p.id" :project="p" @edit="openEdit" @delete="onDelete" @archive="onArchive" />
        </div>
        <el-empty v-else description="暂无保护修复项目" :image-size="80" style="padding:40px 0">
            <el-button type="primary" @click="openCreate">新建保护修复项目</el-button>
        </el-empty>
        <el-pagination v-if="filtered.length > pageSize" v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[12, 24, 48]" :total="filtered.length" layout="total, sizes, prev, pager, next" background style="margin-top:20px; justify-content:flex-end" />
        <ConservationProjectDialog v-model="dialogVisible" :editData="editData" @save="onSave" />
    </div>
</template>

<style scoped>
.conservation-page { padding: 0; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
</style>
