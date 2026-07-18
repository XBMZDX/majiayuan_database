<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DiseaseProjectSummary from './components/disease/DiseaseProjectSummary.vue'
import DiseaseRecordList from './components/disease/DiseaseRecordList.vue'
import DiseaseSurveyPaper from './components/disease/DiseaseSurveyPaper.vue'

const route = useRoute(); const router = useRouter()
const projectId = computed(() => Number(route.params.projectId || route.query.projectId) || null)
const loading = ref(true)

// ========== Mock 项目 ==========
const project = ref({
    id: 1, projectCode: 'CR-M45C1-001', projectName: 'M45-C1马车本体保护修复',
    artifactId: 101, artifactCode: 'M45-C1', artifactName: '马车本体',
    material: '土体、木质、金属复合材质', tombCode: 'M45',
    currentStage: 'disease', riskLevel: 'high', progress: 15
})

// ========== Mock 调查 ==========
const survey = ref({
    id: 1, projectId: 1, surveyCode: 'DS-M45C1-001', surveyDate: '2026-07-15',
    surveyor: '张三', surveyLocation: '文物保护实验室',
    preservationStatus: 'poor', structuralStability: 'partially_unstable',
    environmentSummary: '', summary: '', status: 'draft'
})

// ========== Mock 病害列表 ==========
const diseaseRecords = ref([
    { id: 1, surveyId: 1, projectId: 1, diseaseTypeId: 1, diseaseName: '裂隙', diseaseCategory: 'structural', severity: 'severe', developmentStatus: 'rapidly_developing', extentValue: 35, extentUnit: 'cm', partName: '车轮', side: '右侧', positionDescription: '车轮右侧上部存在纵向贯通裂隙', morphology: '裂隙边缘松散', causeAnalysis: '长期失水及结构受力变化', structuralImpact: 'local', emergency: true, recommendedAction: '建议优先进行临时支撑及裂隙加固', mediaCount: 3 },
    { id: 2, surveyId: 1, projectId: 1, diseaseTypeId: 2, diseaseName: '风化', diseaseCategory: 'physical', severity: 'moderate', developmentStatus: 'slowly_developing', extentValue: 18, extentUnit: '%', partName: '车厢表面', side: '整体', positionDescription: '车厢表面局部粉化', morphology: '表层颗粒松散', causeAnalysis: '环境干燥', structuralImpact: 'none', emergency: false, recommendedAction: '建议表面加固', mediaCount: 2 },
    { id: 3, surveyId: 1, projectId: 1, diseaseTypeId: 3, diseaseName: '坍塌', diseaseCategory: 'structural', severity: 'critical', developmentStatus: 'stable', extentValue: 1, extentUnit: '处', partName: '基座', side: '底部', positionDescription: '基座局部坍塌', morphology: '结构局部塌陷', causeAnalysis: '长期受力不均', structuralImpact: 'overall', emergency: true, recommendedAction: '紧急加固', mediaCount: 1 }
])

const activeDisease = ref(diseaseRecords.value[0] || null)
const selectDisease = (r) => { activeDisease.value = r }
const addDisease = () => {
    const newId = Math.max(...diseaseRecords.value.map(r => r.id), 0) + 1
    const r = { id: newId, surveyId: survey.value.id, projectId: project.value.id, diseaseName: '新病害', diseaseCategory: 'physical', severity: 'minor', developmentStatus: 'stable', partName: '', side: '', positionDescription: '', morphology: '', causeAnalysis: '', structuralImpact: 'none', emergency: false, recommendedAction: '', mediaCount: 0 }
    diseaseRecords.value.push(r); activeDisease.value = r
}

// ========== Mock 病害类型 ==========
const diseaseTypes = ref([
    { id: 1, code: 'LF', name: '裂隙', category: 'structural' }, { id: 2, code: 'FH', name: '风化', category: 'physical' },
    { id: 3, code: 'TT', name: '坍塌', category: 'structural' }, { id: 4, code: 'CQ', name: '残缺', category: 'structural' },
    { id: 5, code: 'SF', name: '酥粉', category: 'physical' }, { id: 6, code: 'XS', name: '锈蚀', category: 'chemical' },
    { id: 7, code: 'KH', name: '矿化', category: 'chemical' }, { id: 8, code: 'BX', name: '变形', category: 'structural' }
])

// ========== 风险计算 ==========
const calcRisk = () => {
    const hasCritical = diseaseRecords.value.some(r => r.severity === 'critical' || (r.severity === 'severe' && r.structuralImpact === 'overall'))
    const severeCount = diseaseRecords.value.filter(r => r.severity === 'severe' || r.severity === 'critical').length
    if (hasCritical || severeCount >= 2) return 'high'
    if (diseaseRecords.value.some(r => r.severity === 'moderate' || (r.severity === 'severe'))) return 'medium'
    return 'low'
}
const severeCount = computed(() => diseaseRecords.value.filter(r => r.severity === 'severe' || r.severity === 'critical').length)

// ========== 操作 ==========
const saveDraft = () => {
    survey.value.status = 'draft'
    ElMessage.success('草稿已保存')
}
const submitSurvey = () => {
    if (!survey.value.surveyDate || !survey.value.surveyor) { ElMessage.warning('请填写调查日期和调查人'); return }
    if (!diseaseRecords.value.length) { ElMessage.warning('请至少添加一条病害记录'); return }
    const urgentSevere = diseaseRecords.value.filter(r => (r.severity === 'severe' || r.severity === 'critical') && !r.recommendedAction)
    if (urgentSevere.length) { ElMessage.warning('严重/危急病害必须填写处理建议'); return }
    survey.value.status = 'submitted'
    project.value.riskLevel = calcRisk()
    project.value.currentStage = 'planning'
    project.value.progress = 30
    ElMessage.success('调查已提交，风险已更新')
}
const goBack = () => router.push('/conservation/overview')

const onUpdateSurvey = (v) => { Object.assign(survey.value, v) }
const onUpdateDisease = (v) => { if (activeDisease.value) Object.assign(activeDisease.value, v) }

onMounted(() => { loading.value = false })
</script>

<template>
    <div class="disease-page" v-loading="loading">
        <DiseaseProjectSummary :project="project" :survey="survey" :diseaseCount="diseaseRecords.length" :severeCount="severeCount" @save="saveDraft" @submit="submitSurvey" @back="goBack" />
        <div class="disease-body">
            <DiseaseRecordList :records="diseaseRecords" :activeId="activeDisease?.id" @select="selectDisease" @add="addDisease" />
            <DiseaseSurveyPaper :survey="survey" :activeDisease="activeDisease" :diseaseTypes="diseaseTypes" @update:survey="onUpdateSurvey" @update:disease="onUpdateDisease" />
        </div>
    </div>
</template>

<style scoped>
.disease-page { display: flex; flex-direction: column; height: calc(100vh - 140px); min-height: 500px; }
.disease-body { flex: 1; display: flex; gap: 14px; min-height: 0; }
@media (max-width: 1100px) { .disease-body { flex-direction: column; } .record-list { width: 100%; max-height: 300px; } }
</style>
