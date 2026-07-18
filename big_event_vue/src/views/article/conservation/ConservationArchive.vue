<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentAdd, Refresh, Setting } from '@element-plus/icons-vue'
import ArchiveSummaryBar from './components/archive/ArchiveSummaryBar.vue'
import ArchiveSectionNav from './components/archive/ArchiveSectionNav.vue'
import ArchivePaper from './components/archive/ArchivePaper.vue'
import ArchiveAssistPanel from './components/archive/ArchiveAssistPanel.vue'
import { USE_CONSERVATION_ARCHIVE_MOCK } from '@/api/conservationArchive'
import {
    archiveSectionDefinitions,
    createMockArchive,
    createMockArchiveWorkspace,
    mockProjects
} from './archiveMock'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId || route.query.projectId) || null)

const loading = ref(true)
const loadError = ref('')
const project = ref(null)
const archive = ref(null)
const workspace = ref(null)
const activeSection = ref(
    typeof route.query.section === 'string'
        && archiveSectionDefinitions.some(item => item.code === route.query.section)
        ? route.query.section
        : 'project'
)
const saving = ref(false)
const dirty = ref(false)
const mobileNavVisible = ref(false)
const assistOpen = ref(false)
const revisionDialogVisible = ref(false)
const revisionListVisible = ref(false)
const revisions = ref([])
const revisionForm = reactive({ versionNo: '', revisionDescription: '', operator: '' })
const storageKey = computed(() => `conservation-archive-workbench:${projectId.value}`)

const hasText = (value) => String(value || '').trim().length > 0

const calculateSectionStatus = (code) => {
    if (!workspace.value) return 'pending'
    const w = workspace.value
    switch (code) {
        case 'project':
            return project.value ? 'completed' : 'missing'
        case 'condition':
            if (!['submitted', 'reviewed'].includes(w.survey?.status)) return 'missing'
            return w.surveyReferenceUpdated ? 'updated' : 'completed'
        case 'disease':
            return w.diseaseRecords.length ? (w.surveyReferenceUpdated ? 'updated' : 'completed') : 'missing'
        case 'detection':
            return w.detectionReferences.length >= 2 ? 'completed' : w.detectionReferences.length ? 'partial' : 'missing'
        case 'principle':
            return w.principles.selected.length && hasText(w.goals.overall) && hasText(w.goals.diseaseControl)
                ? 'completed'
                : (w.principles.selected.length || hasText(w.goals.overall) ? 'partial' : 'pending')
        case 'plan':
            return hasText(w.plan.planName) && hasText(w.plan.selectedMethod) && w.planDiseaseList.length && hasText(w.plan.safetyRequirements)
                ? 'completed'
                : (hasText(w.plan.planName) || w.planDiseaseList.length ? 'partial' : 'pending')
        case 'material': {
            const materialsReady = w.planMaterials.length
                && w.planMaterials.every(item => hasText(item.safetyNote) && hasText(item.compatibilityNote))
            return materialsReady && w.tools.length && hasText(w.processParameters.qualityControl)
                ? 'completed'
                : (w.planMaterials.length || w.tools.length ? 'partial' : 'pending')
        }
        case 'process':
            if (!w.processSummary.steps.length) return 'pending'
            return w.processSummary.completed >= w.processSummary.planned ? 'completed' : 'partial'
        case 'comparison':
            return w.comparisons.length ? 'partial' : 'pending'
        case 'restoration':
            return w.restorationResults.length ? 'completed' : 'pending'
        case 'evaluation':
            return hasText(w.evaluation.acceptanceConclusion) && hasText(w.evaluation.evaluator)
                ? 'completed'
                : (hasText(w.evaluation.diseaseControl) || hasText(w.evaluation.goalAchievement) ? 'partial' : 'pending')
        case 'advice':
            return hasText(w.advice.followUpAdvice) && hasText(w.advice.reviewCycle) && hasText(w.advice.monitoringIndicators)
                ? 'completed'
                : (hasText(w.advice.temperatureRange) || hasText(w.advice.followUpAdvice) ? 'partial' : 'pending')
        case 'attachment':
            return w.attachments.length >= 5 ? 'completed' : w.attachments.length ? 'partial' : 'pending'
        default:
            return 'pending'
    }
}

const sections = computed(() =>
    archiveSectionDefinitions.map(item => ({ ...item, status: calculateSectionStatus(item.code) }))
)

// 固定章节权重 × 自动完成状态，不接受手工填写完整度。
const calculateArchiveCompleteness = (sectionList) => {
    const factor = { completed: 1, updated: .75, partial: .5, pending: 0, missing: 0 }
    return Math.round(sectionList.reduce((sum, item) => sum + item.weight * (factor[item.status] || 0), 0))
}
const completeness = computed(() => calculateArchiveCompleteness(sections.value))

const severeDiseasesWithoutPlan = computed(() => {
    if (!workspace.value) return []
    const linkedIds = new Set(workspace.value.planDiseaseList.flatMap(item => item.diseaseRecordIds || [item.diseaseRecordId]))
    return workspace.value.diseaseRecords.filter(
        item => ['severe', 'critical'].includes(item.severity) && !linkedIds.has(item.id)
    )
})

const missingItems = computed(() => {
    if (!workspace.value || !archive.value) return []
    const w = workspace.value
    const items = []
    const add = (key, label, section, critical = false) => items.push({ key, label, section, critical })
    if (!archive.value.compiler) add('compiler', '缺少档案编制人', 'principle', true)
    if (!['submitted', 'reviewed'].includes(w.survey.status)) add('survey', '缺少正式病害调查', 'condition', true)
    if (!w.detectionReferences.length) add('detection', '至少关联一个检测或技术依据', 'detection', true)
    if (!w.principles.selected.length) add('principle', '缺少保护修复原则', 'principle', true)
    if (!w.goals.overall) add('goal', '缺少总体保护修复目标', 'principle', true)
    if (!w.plan.planName || !w.plan.selectedMethod) add('plan', '缺少正式保护修复方案', 'plan', true)
    if (severeDiseasesWithoutPlan.value.length) {
        add('disease-plan', `${severeDiseasesWithoutPlan.value.length}条严重病害未关联措施`, 'plan', true)
    }
    if (!w.planMaterials.length) add('material', '方案至少配置一种材料', 'material', true)
    if (w.planMaterials.some(item => !item.safetyNote)) add('material-safety', '缺少材料安全说明', 'material', true)
    if (!w.plan.safetyRequirements) add('plan-safety', '缺少方案安全要求', 'plan', true)
    if (!w.plan.compatibilityDescription && !w.plan.reversibilityDescription) {
        add('compatibility', '缺少兼容性或可逆性说明', 'plan', true)
    }
    if (!w.attachments.some(item => item.fileType === '照片' && item.fileName.includes('修复后'))) {
        add('after-photo', '缺少修复后整体照片', 'comparison')
    }
    if (!w.evaluation.acceptanceConclusion) add('evaluation', '效果评估尚未完成', 'evaluation')
    if (!w.advice.followUpAdvice) add('advice', '未填写后续保存建议', 'advice')
    if (!archive.value.finalConclusion) add('conclusion', '缺少阶段性或最终结论', 'evaluation', true)
    return items
})

const dataSummaries = computed(() => {
    if (!workspace.value) return []
    const w = workspace.value
    return [
        { label: '病害调查', value: w.survey ? '1次' : '0次' },
        { label: '病害记录', value: `${w.diseaseRecords.length}项` },
        { label: '检测结果', value: `${w.detectionReferences.length}项` },
        { label: '方案措施', value: `${w.planDiseaseList.length}项` },
        { label: '计划材料', value: `${w.planMaterials.length}种` },
        { label: '修复步骤', value: `${w.processSummary.planned}项` },
        { label: '前后对比', value: `${w.comparisons.length}组` },
        { label: '复原成果', value: `${w.restorationResults.length}项` },
        { label: '监测计划', value: w.advice.followUpAdvice ? '1项' : '0项' },
        { label: '附件', value: `${w.attachments.length}个` }
    ]
})

const loadPage = async () => {
    loading.value = true
    loadError.value = ''
    try {
        await new Promise(resolve => setTimeout(resolve, 350))
        if (!projectId.value || !mockProjects[projectId.value]) {
            project.value = null
            archive.value = null
            return
        }
        project.value = structuredClone(mockProjects[projectId.value])
        if (!USE_CONSERVATION_ARCHIVE_MOCK) {
            throw new Error('真实保护修复接口尚未启用，请保持 Mock 模式。')
        }
        const saved = localStorage.getItem(storageKey.value)
        if (saved) {
            const data = JSON.parse(saved)
            archive.value = data.archive
            workspace.value = data.workspace
            revisions.value = data.revisions || []
        } else if (projectId.value !== 5) {
            archive.value = createMockArchive(project.value)
            workspace.value = createMockArchiveWorkspace(project.value)
            revisions.value = [{
                id: 1, versionNo: 'V1.0', revisionType: 'initial',
                revisionDescription: '建立保护修复档案并初始化默认章节',
                operator: project.value.principal, createTime: '2026-07-15 10:00:00'
            }]
        } else {
            // 项目5用于完整演示“尚未建立档案”的空状态和建立档案流程。
            archive.value = null
            workspace.value = null
            revisions.value = []
        }
        dirty.value = false
    } catch (error) {
        console.error(error)
        loadError.value = error.message || '档案加载失败'
        ElMessage.error(loadError.value)
    } finally {
        loading.value = false
    }
}

const createArchive = async () => {
    if (!project.value) return
    loading.value = true
    try {
        await new Promise(resolve => setTimeout(resolve, 300))
        archive.value = createMockArchive(project.value)
        workspace.value = createMockArchiveWorkspace(project.value)
        revisions.value = [{
            id: Date.now(), versionNo: 'V1.0', revisionType: 'initial',
            revisionDescription: '建立保护修复档案并初始化默认章节',
            operator: project.value.principal,
            createTime: new Date().toLocaleString('zh-CN', { hour12: false })
        }]
        activeSection.value = 'project'
        dirty.value = true
        ElMessage.success('保护修复档案已建立，已自动关联项目、文物和最新病害调查')
    } finally {
        loading.value = false
    }
}

const persistArchive = () => {
    localStorage.setItem(storageKey.value, JSON.stringify({
        archive: archive.value,
        workspace: workspace.value,
        revisions: revisions.value
    }))
}

const saveDraft = async (showMessage = true) => {
    if (!archive.value || !workspace.value) return
    saving.value = true
    try {
        await new Promise(resolve => setTimeout(resolve, 300))
        if (archive.value.archiveStatus === 'draft') archive.value.archiveStatus = 'compiling'
        archive.value.protectionGoal = workspace.value.goals.overall
        archive.value.conservationBasis = workspace.value.plan.technicalBasis
        archive.value.completenessRate = completeness.value
        archive.value.updateTime = new Date().toLocaleString('zh-CN', { hour12: false })
        persistArchive()
        dirty.value = false
        if (showMessage) ElMessage.success(`草稿已保存：${archive.value.updateTime}`)
    } catch {
        ElMessage.error('保存失败，请稍后重试')
    } finally {
        saving.value = false
    }
}

const submitForReview = async () => {
    const critical = missingItems.value.filter(item => item.critical)
    if (completeness.value < 80 || critical.length) {
        const list = [
            ...(completeness.value < 80 ? [`档案完整度需达到80%，当前为${completeness.value}%`] : []),
            ...critical.map(item => item.label)
        ]
        await ElMessageBox.alert(
            `<div style="line-height:1.9">${list.map(item => `• ${item}`).join('<br>')}</div>`,
            '暂不能提交审核',
            { dangerouslyUseHTMLString: true, confirmButtonText: '查看并完善', type: 'warning' }
        )
        if (critical[0]) selectSection(critical[0].section)
        return
    }
    await ElMessageBox.confirm(
        `当前完整度为${completeness.value}%，提交后将生成版本快照。是否继续？`,
        '提交审核',
        { type: 'warning' }
    )
    await saveDraft(false)
    archive.value.archiveStatus = 'submitted'
    archive.value.submittedTime = new Date().toLocaleString('zh-CN', { hour12: false })
    revisions.value.unshift({
        id: Date.now(), versionNo: archive.value.currentVersion, revisionType: 'submit',
        revisionDescription: '提交档案审核并生成内容快照',
        operator: archive.value.compiler, createTime: archive.value.submittedTime
    })
    persistArchive()
    dirty.value = false
    ElMessage.success('档案已提交审核')
}

const nextVersion = () => {
    const match = String(archive.value?.currentVersion || 'V1.0').match(/V(\d+)\.(\d+)/)
    return match ? `V${match[1]}.${Number(match[2]) + 1}` : 'V1.1'
}
const openRevisionDialog = () => {
    Object.assign(revisionForm, {
        versionNo: nextVersion(),
        revisionDescription: '',
        operator: archive.value.compiler || project.value.principal
    })
    revisionDialogVisible.value = true
}
const createRevision = async () => {
    if (!revisionForm.versionNo || !revisionForm.revisionDescription || !revisionForm.operator) {
        ElMessage.warning('请完整填写版本号、修改说明和操作人')
        return
    }
    await saveDraft(false)
    const time = new Date().toLocaleString('zh-CN', { hour12: false })
    archive.value.currentVersion = revisionForm.versionNo
    archive.value.updateTime = time
    revisions.value.unshift({
        id: Date.now(), versionNo: revisionForm.versionNo, revisionType: 'manual',
        revisionDescription: revisionForm.revisionDescription,
        operator: revisionForm.operator, createTime: time
    })
    persistArchive()
    revisionDialogVisible.value = false
    ElMessage.success(`新版本 ${revisionForm.versionNo} 已生成`)
}

const handleExportArchive = () => ElMessage.info('档案导出功能将在后续接入 Word/PDF 生成接口。')

const confirmLeave = async () => {
    if (!dirty.value) return true
    try {
        await ElMessageBox.confirm('当前档案存在未保存内容，离开后修改将丢失。', '未保存提示', {
            confirmButtonText: '放弃修改并离开',
            cancelButtonText: '继续编辑',
            type: 'warning'
        })
        return true
    } catch {
        return false
    }
}
const backToOverview = async () => {
    if (await confirmLeave()) {
        dirty.value = false
        router.push('/conservation/overview')
    }
}

const selectSection = (code) => {
    activeSection.value = code
    mobileNavVisible.value = false
    nextTick(() => document.querySelector('.paper-scroll')?.scrollTo({ top: 0, behavior: 'smooth' }))
}

const navigateBusiness = async (target) => {
    const routes = {
        overview: '/conservation/overview',
        artifact: '/artifacts/manage',
        disease: `/conservation/project/${projectId.value}/disease`,
        process: `/conservation/project/${projectId.value}/process`,
        comparison: `/conservation/project/${projectId.value}/comparison`,
        restoration: `/conservation/project/${projectId.value}/restoration`,
        monitoring: `/conservation/project/${projectId.value}/monitoring`,
        'detection-detail': '/detection/result'
    }
    if (routes[target] && await confirmLeave()) {
        dirty.value = false
        router.push(routes[target])
    }
}

const refreshSurveyReference = () => {
    workspace.value.surveyReferenceUpdated = false
    archive.value.sourceSurveyVersion = 'V1.1'
    archive.value.sourceSurveyId = 2
    dirty.value = true
    ElMessage.success('已刷新最新正式病害调查引用，人工编制内容未被覆盖')
}

const handleBeforeUnload = (event) => {
    if (!dirty.value) return
    event.preventDefault()
    event.returnValue = ''
}

onBeforeRouteLeave(async () => {
    const allowed = await confirmLeave()
    if (allowed) dirty.value = false
    return allowed
})
onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    loadPage()
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', handleBeforeUnload))
</script>

<template>
    <div class="archive-page">
        <el-skeleton v-if="loading" animated :rows="12" class="loading-shell" />

        <el-result v-else-if="loadError" icon="error" title="档案加载失败" :sub-title="loadError">
            <template #extra><el-button type="primary" :icon="Refresh" @click="loadPage">重新加载</el-button></template>
        </el-result>

        <el-result v-else-if="!project" icon="warning" title="未找到对应的保护修复项目" sub-title="请从保护修复总览选择有效项目进入。">
            <template #extra><el-button type="primary" @click="router.push('/conservation/overview')">返回保护修复总览</el-button></template>
        </el-result>

        <div v-else-if="!archive" class="empty-archive">
            <div class="empty-icon"><el-icon><DocumentAdd /></el-icon></div>
            <h2>当前保护修复项目尚未建立正式档案</h2>
            <p>建立档案后，可以汇总病害调查、检测依据、保护修复方案、修复过程、前后对比、复原成果和后续监测数据。</p>
            <div class="empty-project">
                <span>{{ project.projectCode }}</span>
                <b>{{ project.projectName }}</b>
                <em>{{ project.artifactCode }} · {{ project.artifactName }}</em>
            </div>
            <div>
                <el-button type="primary" size="large" :icon="DocumentAdd" @click="createArchive">建立保护修复档案</el-button>
                <el-button size="large" @click="router.push('/conservation/overview')">返回保护修复总览</el-button>
            </div>
        </div>

        <template v-else>
            <ArchiveSummaryBar
                :project="project"
                :archive="archive"
                :completeness="completeness"
                :saving="saving"
                :dirty="dirty"
                @save="saveDraft"
                @submit="submitForReview"
                @revision="openRevisionDialog"
                @export="handleExportArchive"
                @back="backToOverview"
                @open-nav="mobileNavVisible = true"
            />

            <div class="archive-body">
                <div class="archive-nav">
                    <ArchiveSectionNav :sections="sections" :active-section="activeSection" @select="selectSection" />
                </div>
                <main class="paper-scroll">
                    <ArchivePaper
                        :active-section="activeSection"
                        :project="project"
                        :archive="archive"
                        :sections="sections"
                        :workspace="workspace"
                        @dirty="dirty = true"
                        @navigate="navigateBusiness"
                        @refresh-survey="refreshSurveyReference"
                    />
                </main>
                <div class="assist-panel" :class="{ open: assistOpen }">
                    <ArchiveAssistPanel
                        :completeness="completeness"
                        :missing-items="missingItems"
                        :summaries="dataSummaries"
                        :archive="archive"
                        :revision-count="revisions.length"
                        @select-section="selectSection"
                        @show-revisions="revisionListVisible = true"
                        @close="assistOpen = false"
                    />
                </div>
                <el-button class="assist-fab" :icon="Setting" circle type="primary" title="档案辅助信息" @click="assistOpen = !assistOpen" />
            </div>

            <el-drawer v-model="mobileNavVisible" title="档案目录" direction="ltr" size="280px">
                <ArchiveSectionNav :sections="sections" :active-section="activeSection" @select="selectSection" />
            </el-drawer>

            <el-dialog v-model="revisionDialogVisible" title="生成档案新版本" width="520px">
                <el-form label-position="top">
                    <el-form-item label="新版本号"><el-input v-model="revisionForm.versionNo" /></el-form-item>
                    <el-form-item label="修改说明"><el-input v-model="revisionForm.revisionDescription" type="textarea" :rows="4" placeholder="说明本版本的主要变更" /></el-form-item>
                    <el-form-item label="操作人"><el-input v-model="revisionForm.operator" /></el-form-item>
                </el-form>
                <template #footer><el-button @click="revisionDialogVisible = false">取消</el-button><el-button type="primary" @click="createRevision">生成版本</el-button></template>
            </el-dialog>

            <el-dialog v-model="revisionListVisible" title="档案版本记录" width="720px">
                <el-table :data="revisions" border>
                    <el-table-column prop="versionNo" label="版本" width="90" />
                    <el-table-column prop="revisionType" label="类型" width="100" />
                    <el-table-column prop="revisionDescription" label="修改说明" min-width="250" />
                    <el-table-column prop="operator" label="操作人" width="90" />
                    <el-table-column prop="createTime" label="生成时间" width="170" />
                </el-table>
            </el-dialog>
        </template>
    </div>
</template>

<style scoped>
.archive-page { height: calc(100vh - 76px); min-height: 580px; display: flex; flex-direction: column; gap: 12px; overflow: hidden; }
.loading-shell { padding: 24px; background: #fff; border-radius: 10px; box-sizing: border-box; }
.archive-body { min-height: 0; position: relative; display: grid; flex: 1; grid-template-columns: 230px minmax(520px, 1fr) 280px; gap: 12px; overflow: hidden; }
.archive-nav, .assist-panel { min-height: 0; }
.paper-scroll { min-width: 0; overflow-y: auto; overflow-x: hidden; padding: 0 4px 18px; }
.assist-fab { display: none; position: absolute; right: 12px; bottom: 18px; z-index: 12; box-shadow: 0 3px 12px rgba(22, 104, 196, .3); }
.empty-archive { max-width: 700px; margin: auto; padding: 50px; text-align: center; background: #fff; border: 1px solid #e5e6eb; border-radius: 14px; box-shadow: 0 6px 25px rgba(29, 33, 41, .06); }
.empty-icon { width: 78px; height: 78px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; color: #1668c4; background: #eaf3ff; border-radius: 50%; font-size: 36px; }
.empty-archive h2 { color: #1d2129; font-size: 21px; }
.empty-archive > p { max-width: 540px; margin: 12px auto 24px; color: #86909c; font-size: 13px; line-height: 1.8; }
.empty-project { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 8px 14px; margin-bottom: 27px; padding: 13px; background: #f7f8fa; border-radius: 8px; }
.empty-project span { color: #1668c4; font-size: 12px; }
.empty-project b { color: #303133; font-size: 13px; }
.empty-project em { color: #86909c; font-size: 12px; font-style: normal; }
@media (max-width: 1280px) {
    .archive-body { grid-template-columns: 220px minmax(520px, 1fr); }
    .assist-panel { width: 290px; position: absolute; top: 0; right: 0; bottom: 0; z-index: 11; transform: translateX(calc(100% + 20px)); transition: transform .2s ease; }
    .assist-panel.open { transform: translateX(0); }
    .assist-fab { display: inline-flex; }
}
@media (max-width: 900px) {
    .archive-body { display: block; }
    .archive-nav { display: none; }
    .paper-scroll { height: 100%; }
}
</style>
