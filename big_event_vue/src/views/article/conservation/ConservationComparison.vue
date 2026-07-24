<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Refresh, Setting } from '@element-plus/icons-vue'
import ComparisonSummaryBar from './components/comparison/ComparisonSummaryBar.vue'
import ComparisonGroupList from './components/comparison/ComparisonGroupList.vue'
import ComparisonWorkspace from './components/comparison/ComparisonWorkspace.vue'
import ComparisonAssistPanel from './components/comparison/ComparisonAssistPanel.vue'
import {
    getComparisonWorkbench,
    getComparisonMediaContent,
    saveComparisonWorkbench
} from '@/api/conservationComparison'
import { getProcessMediaContent } from '@/api/conservationProcess'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId || route.query.projectId) || null)

const loading = ref(true)
const saving = ref(false)
const loadError = ref('')
const project = ref(null)
const processRecord = ref(null)
const steps = ref([])
const diseases = ref([])
const sourceMedia = ref([])
const groups = ref([])
const activeId = ref(null)
const dirty = ref(false)
const editingCompleted = ref(false)
const createDialog = ref(false)
const createStep = ref(0)
const createFromProcess = ref(false)
const mobileListVisible = ref(false)
const assistDrawerVisible = ref(false)
const workspaceRef = ref(null)
const selectedMediaIds = ref([])

const createForm = reactive({
    stepId: null, diseaseIds: [], targetPart: '', shootingPosition: '',
    comparisonType: 'before_after', overallComparison: false,
    comparisonCode: '', comparisonTitle: '', beforeSummary: '', afterSummary: '',
    comparisonDescription: ''
})

const current = computed(() => groups.value.find(item => item.id === activeId.value) || null)
const currentStep = computed(() => steps.value.find(item => item.id === current.value?.stepId) || null)
const readOnly = computed(() => {
    if (!current.value) return true
    return ['completed', 'reviewed', 'archived'].includes(current.value.evaluationStatus) && !editingCompleted.value
})
const availableMedia = computed(() => sourceMedia.value.filter(item => !createForm.stepId || item.stepId === createForm.stepId))
const selectedMedia = computed(() => sourceMedia.value.filter(item => selectedMediaIds.value.includes(item.id)))
const createHasBefore = computed(() => selectedMedia.value.some(item => item.imageStage === 'before'))
const createHasAfter = computed(() => selectedMedia.value.some(item => item.imageStage === 'after'))
const sourceStageMap = { before: '修复前', during: '修复中', after: '修复后', follow_up: '复查', reference: '参考' }
const objectUrls = []

const baseEvaluation = () => ({
    diseaseControlScore: 0, structuralStabilityScore: 0, appearanceScore: 0,
    materialCompatibilityScore: 0, informationPreservationScore: 0, distinguishabilityScore: 0,
    overallScore: 0, diseaseControlComment: '', structuralComment: '', appearanceComment: '',
    compatibilityComment: '', informationPreservationComment: '', sideEffectComment: '',
    remainingIssue: '', finalConclusion: '', evaluator: '', evaluationDate: ''
})
const createEmptyComparison = (project, context, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId,
    processId: context.process?.id || null, stepId: null,
    comparisonCode: `CMP-${String(project.artifactCode || project.id).replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-3)}`,
    comparisonTitle: '', comparisonType: 'before_after', targetPart: '', shootingPosition: '',
    beforeSummary: '', afterSummary: '', comparisonDescription: '', overallEffect: '',
    evaluationStatus: 'draft', overallComparison: false, selectedForArchive: false,
    selectedForRestoration: false, selectedAsMonitoringBaseline: false, evaluator: '',
    evaluationDate: '', updateTime: '', noApplicableMetrics: false,
    images: [], diseases: [], metrics: [], evaluation: baseEvaluation(),
    monitoring: {
        unresolvedDiseases: '', stabilityConcern: '', reviewPart: '', indicators: '',
        cycle: '', warningConditions: '', requiresRework: false, notes: '', monitoringDecision: ''
    }
})

const missingItems = computed(() => {
    const group = current.value
    if (!group) return []
    const result = []
    const add = (key, label, section, critical = true) => result.push({ key, label, section, critical })
    const before = group.images.find(item => item.imageStage === 'before')
    const after = group.images.find(item => item.imageStage === 'after')
    if (!group.comparisonTitle?.trim()) add('title', '缺少对比标题', 'status')
    if (!group.stepId) add('step', '尚未关联修复步骤', 'images')
    if (!group.targetPart?.trim()) add('part', '缺少目标部位', 'status')
    if (!group.diseases.length && !group.overallComparison) add('disease', '未关联病害或整体对比', 'status')
    if (!before) add('before-image', '缺少修复前影像', 'viewer')
    if (!after) add('after-image', '缺少修复后影像', 'viewer')
    if (before && after && before.targetPart !== after.targetPart) add('part-mismatch', '修复前后影像部位不一致', 'images')
    if (before && after && before.shootingPosition !== after.shootingPosition) add('angle', '修复前后拍摄角度不一致', 'images', false)
    if (before && after && new Date(after.shootingTime) <= new Date(before.shootingTime)) add('time', '修复后影像时间早于前图', 'images')
    if (!group.beforeSummary?.trim()) add('before-summary', '缺少修复前状态说明', 'status')
    if (!group.afterSummary?.trim()) add('after-summary', '缺少修复后状态说明', 'status')
    if (group.diseases.some(item => !item.treatmentEffect || !item.afterStatus?.trim())) add('disease-effect', '关联病害尚未填写效果结论', 'status')
    if (!group.noApplicableMetrics && !group.metrics.length) add('metrics', '尚未填写量化指标或确认不适用', 'metrics', false)
    if (!group.overallEffect) add('effect', '尚未确认整体效果', 'evaluation')
    if (!group.evaluation.diseaseControlComment?.trim()) add('control-comment', '缺少病害控制评价', 'evaluation')
    if (!group.evaluation.structuralComment?.trim()) add('structure-comment', '缺少结构稳定性评价', 'evaluation')
    if (!group.evaluation.finalConclusion?.trim()) add('conclusion', '缺少综合评价结论', 'evaluation')
    if (!group.monitoring.monitoringDecision) add('monitor-decision', '尚未选择是否作为监测基准', 'monitoring')
    return result
})

const completeness = computed(() => {
    const group = current.value
    if (!group) return 0
    const before = group.images.some(item => item.imageStage === 'before')
    const after = group.images.some(item => item.imageStage === 'after')
    const basic = group.comparisonTitle && group.stepId && group.targetPart && (group.diseases.length || group.overallComparison)
    const statuses = group.beforeSummary && group.afterSummary
    const diseaseEffect = group.overallComparison || (group.diseases.length && group.diseases.every(item => item.treatmentEffect && item.afterStatus))
    const professional = group.overallEffect && group.evaluation.finalConclusion && group.evaluation.diseaseControlComment
    const monitoring = Boolean(group.monitoring.monitoringDecision)
    let score = (basic ? 10 : 0) + (before ? 15 : 0) + (after ? 15 : 0)
        + (statuses ? 15 : 0) + (diseaseEffect ? 15 : 0)
        + (professional ? 15 : 0) + (monitoring ? 5 : 0)
    if (group.noApplicableMetrics) return Math.round(score / 90 * 100)
    score += group.metrics.length ? 10 : 0
    return score
})

const normalizeLoadedGroups = data => data.map(group => ({
    ...group,
    noApplicableMetrics: Boolean(group.noApplicableMetrics),
    overallComparison: Boolean(group.overallComparison),
    monitoring: {
        unresolvedDiseases: '', stabilityConcern: '', reviewPart: '', indicators: '', cycle: '',
        warningConditions: '', requiresRework: false, notes: '', monitoringDecision: '',
        ...group.monitoring,
        monitoringDecision: group.monitoring?.monitoringDecision
            || (group.selectedAsMonitoringBaseline ? 'baseline' : group.evaluationStatus === 'completed' ? 'not_required' : '')
    }
}))

const blobUrl = async (loader, id) => {
    const response = await loader(id)
    const url = URL.createObjectURL(response.data)
    objectUrls.push(url)
    return url
}
const hydrateMedia = async () => {
    await Promise.all(sourceMedia.value.map(async media => {
        if (media.fileUrl) {
            media.thumbnailUrl ||= media.fileUrl
            return
        }
        try {
            const url = await blobUrl(getProcessMediaContent, media.sourceMediaId || media.id)
            media.fileUrl = media.thumbnailUrl = url
        } catch {
            media.fileUrl = media.thumbnailUrl = ''
        }
    }))
    await Promise.all(groups.value.flatMap(group => group.images.map(async image => {
        if (image.fileUrl) {
            image.thumbnailUrl ||= image.fileUrl
            return
        }
        try {
            const url = image.sourceMediaId
                ? await blobUrl(getProcessMediaContent, image.sourceMediaId)
                : await blobUrl(getComparisonMediaContent, image.id)
            image.fileUrl = image.thumbnailUrl = url
        } catch {
            try {
                const url = await blobUrl(getComparisonMediaContent, image.id)
                image.fileUrl = image.thumbnailUrl = url
            } catch {
                image.fileUrl = image.thumbnailUrl = ''
            }
        }
    })))
}

const loadPage = async () => {
    loading.value = true
    loadError.value = ''
    try {
        if (!projectId.value) throw new Error('缺少保护修复项目ID')
        const result = await getComparisonWorkbench(projectId.value)
        const data = result.data || {}
        project.value = data.project || null
        processRecord.value = data.processRecord || null
        steps.value = data.steps || []
        diseases.value = data.diseases || []
        sourceMedia.value = data.sourceMedia || []
        groups.value = normalizeLoadedGroups(data.groups || [])
        await hydrateMedia()
        activeId.value = Number(route.query.comparisonId)
            || groups.value.find(item => item.evaluationStatus === 'draft')?.id
            || groups.value[0]?.id
        dirty.value = false
    } catch (error) {
        console.error(error)
        loadError.value = error.message || '修复前后对比加载失败'
        ElMessage.error(loadError.value)
    } finally {
        loading.value = false
    }
}

const saveCurrent = async (showMessage = true) => {
    if (!current.value) return false
    saving.value = true
    try {
        current.value.evaluation.overallScore = Number(current.value.evaluation.overallScore || 0)
        current.value.evaluator = current.value.evaluation.evaluator
        current.value.evaluationDate = current.value.evaluation.evaluationDate
        const currentId = activeId.value
        const result = await saveComparisonWorkbench(projectId.value, groups.value)
        groups.value = normalizeLoadedGroups(result.data?.groups || [])
        activeId.value = groups.value.find(item => item.id === currentId)?.id || groups.value[0]?.id
        await hydrateMedia()
        dirty.value = false
        if (showMessage) ElMessage.success(`当前对比已保存：${current.value.updateTime}`)
        return true
    } catch (error) {
        ElMessage.error(error.message || '保存失败，请稍后重试')
        return false
    } finally {
        saving.value = false
    }
}

const openCreate = (autoSelect = false) => {
    if (autoSelect && (!processRecord.value || !sourceMedia.value.length)) {
        return ElMessage.info('当前没有可引用的修复过程影像，可使用“新建对比组”建立空白草稿')
    }
    createFromProcess.value = autoSelect
    createStep.value = 0
    selectedMediaIds.value = []
    const firstStep = steps.value.find(step => step.media?.some(item => item.selectedForComparison))
        || steps.value.find(step => step.id === 4)
        || steps.value[0]
    Object.assign(createForm, {
        stepId: autoSelect ? firstStep?.id : null, diseaseIds: [], targetPart: '',
        shootingPosition: '', comparisonType: 'before_after', overallComparison: false,
        comparisonCode: `CMP-${project.value.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-${String(groups.value.length + 1).padStart(3, '0')}`,
        comparisonTitle: '', beforeSummary: '', afterSummary: '', comparisonDescription: ''
    })
    if (autoSelect && firstStep) handleCreateStepChange(firstStep.id, true)
    createDialog.value = true
}
const handleCreateStepChange = (stepId, autoSelect = false) => {
    const step = steps.value.find(item => item.id === stepId)
    if (!step) return
    createForm.targetPart = step.targetPart
    createForm.diseaseIds = step.relatedDiseases?.map(item => item.diseaseRecordId) || []
    const stepMedia = sourceMedia.value.filter(item => item.stepId === stepId)
    const selected = stepMedia.filter(item => item.selectedForComparison)
    if (autoSelect) {
        selectedMediaIds.value = selected.length
            ? selected.map(item => item.id)
            : stepMedia.filter(item => ['before', 'after'].includes(item.imageStage)).map(item => item.id)
    } else selectedMediaIds.value = []
    const before = stepMedia.find(item => item.imageStage === 'before')
    createForm.shootingPosition = before?.shootingPosition || ''
    createForm.comparisonTitle = `${step.targetPart || project.value.artifactName}${step.stepName}前后对比`
}
const toggleMedia = media => {
    const index = selectedMediaIds.value.indexOf(media.id)
    if (index >= 0) selectedMediaIds.value.splice(index, 1)
    else selectedMediaIds.value.push(media.id)
}
const nextCreateStep = () => {
    if (createFromProcess.value && createStep.value === 0 && !createForm.stepId) return ElMessage.warning('请先选择关联修复步骤')
    if (createFromProcess.value && createStep.value === 1 && (!createHasBefore.value || !createHasAfter.value)) return ElMessage.warning('至少选择一张修复前和一张修复后影像')
    createStep.value += 1
}
const createGroup = () => {
    const group = createEmptyComparison(project.value, { process: processRecord.value })
    const step = steps.value.find(item => item.id === createForm.stepId)
    Object.assign(group, {
        ...createForm,
        comparisonTitle: createForm.comparisonTitle?.trim() || `未命名对比草稿${groups.value.length + 1}`,
        images: selectedMedia.value.map((media, index) => ({
            id: Date.now() + index, sourceMediaId: media.sourceMediaId || media.id,
            imageStage: media.imageStage, imageRole: media.imageRole, fileName: media.fileName,
            fileUrl: media.fileUrl, thumbnailUrl: media.thumbnailUrl || media.fileUrl,
            targetPart: media.targetPart, shootingPosition: media.shootingPosition,
            shootingTime: media.shootingTime, photographer: media.photographer,
            imageDescription: media.imageDescription, sequenceNo: index + 1,
            isPrimary: ['before', 'after'].includes(media.imageStage), manuallyUploaded: false,
            stepName: media.stepName, sourceModule: media.sourceModule
        })),
        diseases: diseases.value.filter(item => createForm.diseaseIds.includes(item.id)).map(item => ({
            id: Date.now() + item.id, diseaseRecordId: item.id, diseaseName: item.diseaseName,
            originalSeverity: item.severity, developmentStatus: item.developmentStatus,
            beforeStatus: `${item.morphology || ''} ${item.structuralImpact || ''}`.trim(),
            afterStatus: '', treatmentEffect: '', effectDescription: '', monitoringRequired: false
        }))
    })
    if (!group.beforeSummary) group.beforeSummary = group.diseases.map(item => item.beforeStatus).join('；')
    group.monitoring.reviewPart = createForm.targetPart
    group.comparisonDescription = createForm.comparisonDescription
        || (step ? `引用“${step.stepName}”过程影像建立的修复效果对比。` : '手工建立的空白前后对比草稿。')
    groups.value.unshift(group)
    activeId.value = group.id
    createDialog.value = false
    dirty.value = true
    editingCompleted.value = false
    nextTick(() => document.querySelector('.comparison-workspace-scroll')?.scrollTo({ top: 0 }))
}

const selectGroup = async id => {
    if (id === activeId.value) return
    if (!await confirmDiscard()) return
    activeId.value = id
    editingCompleted.value = false
    mobileListVisible.value = false
    dirty.value = false
    nextTick(() => document.querySelector('.comparison-workspace-scroll')?.scrollTo({ top: 0 }))
}
const deleteGroup = async group => {
    if (group.evaluationStatus === 'archived') return ElMessage.warning('已归档对比组不可直接删除')
    if (group.selectedForArchive) return ElMessage.warning('请先解除保护修复档案引用后再删除')
    const impact = group.selectedAsMonitoringBaseline ? '该组已作为监测基准，删除会解除后续监测引用。' : '源过程影像不会被删除。'
    try {
        await ElMessageBox.confirm(`${impact} 确认删除“${group.comparisonTitle}”吗？`, '删除对比组', {
            type: 'warning', confirmButtonText: '删除对比组', cancelButtonText: '取消'
        })
        groups.value.splice(groups.value.indexOf(group), 1)
        if (activeId.value === group.id) activeId.value = groups.value[0]?.id || null
        const result = await saveComparisonWorkbench(projectId.value, groups.value)
        groups.value = normalizeLoadedGroups(result.data?.groups || [])
        await hydrateMedia()
        dirty.value = false
        ElMessage.success('对比组已删除，修复过程原始影像保持不变')
    } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
            ElMessage.error(error.message || '删除失败，请稍后重试')
            await loadPage()
        }
    }
}
const completeEvaluation = async () => {
    const critical = missingItems.value.filter(item => item.critical)
    if (critical.length) {
        ElMessageBox.alert(critical.map((item, index) => `${index + 1}. ${item.label}`).join('\n'), '暂不能完成评价', {
            type: 'warning', confirmButtonText: '继续完善'
        })
        return
    }
    try {
        await ElMessageBox.confirm(`当前完整度为 ${completeness.value}%，完成后默认进入只读模式。`, '完成专业评价', {
            type: 'success', confirmButtonText: '确认完成', cancelButtonText: '继续编辑'
        })
        current.value.evaluationStatus = 'completed'
        current.value.evaluator ||= current.value.evaluation.evaluator || '王五'
        current.value.evaluationDate ||= current.value.evaluation.evaluationDate || new Date().toISOString().slice(0, 10)
        current.value.evaluation.evaluator = current.value.evaluator
        current.value.evaluation.evaluationDate = current.value.evaluationDate
        editingCompleted.value = false
        dirty.value = true
        await saveCurrent(false)
        ElMessage.success('评价已完成，并已同步档案、总览、复原与监测摘要')
    } catch {
        // 用户继续编辑。
    }
}
const reopen = async () => {
    if (current.value.evaluationStatus === 'archived') return ElMessage.warning('已归档对比组不可直接修改')
    try {
        await ElMessageBox.confirm('重新编辑会更新当前评价的修改时间。', '重新编辑评价', {
            type: 'warning', confirmButtonText: '进入编辑', cancelButtonText: '取消'
        })
        editingCompleted.value = true
        dirty.value = true
    } catch {
        // 用户取消。
    }
}
const batchArchive = async () => {
    const candidates = groups.value.filter(item => ['completed', 'reviewed'].includes(item.evaluationStatus) && !item.selectedForArchive)
    if (!candidates.length) return ElMessage.info('没有可批量收入档案的已完成对比组')
    candidates.forEach(item => { item.selectedForArchive = true })
    dirty.value = true
    await saveCurrent(false)
    ElMessage.success(`已将 ${candidates.length} 个评价完成的对比组收入档案引用`)
}
const reorderImages = reordered => {
    if (!current.value) return
    current.value.images = reordered
    dirty.value = true
}
const markDirty = () => { dirty.value = true }
const locate = section => {
    assistDrawerVisible.value = false
    nextTick(() => workspaceRef.value?.scrollTo(section))
}
const navigate = async target => {
    const paths = {
        overview: '/conservation/overview',
        disease: `/conservation/project/${projectId.value}/disease`,
        process: `/conservation/project/${projectId.value}/process`,
        'process-step': `/conservation/project/${projectId.value}/process?stepId=${current.value?.stepId || ''}`,
        archive: `/conservation/project/${projectId.value}/archive?section=comparison`,
        detection: '/detection/manage'
    }
    if (!paths[target] || !await confirmDiscard()) return
    dirty.value = false
    router.push(paths[target])
}
const saveAndContinue = async () => {
    if (current.value && !(await saveCurrent(false))) return
    dirty.value = false
    router.push(`/conservation/project/${projectId.value}/restoration`)
}
const confirmDiscard = async () => {
    if (!dirty.value) return true
    try {
        await ElMessageBox.confirm('当前对比存在未保存修改，离开后修改将丢失。', '未保存提示', {
            type: 'warning', confirmButtonText: '放弃修改并离开', cancelButtonText: '继续编辑'
        })
        return true
    } catch {
        return false
    }
}
const handleBeforeUnload = event => {
    if (!dirty.value) return
    event.preventDefault()
    event.returnValue = ''
}

onBeforeRouteLeave(async () => {
    const allowed = await confirmDiscard()
    if (allowed) dirty.value = false
    return allowed
})
onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    loadPage()
})
onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
    objectUrls.forEach(url => URL.revokeObjectURL(url))
})
</script>

<template>
    <div class="comparison-page">
        <el-skeleton v-if="loading" animated :rows="13" class="loading-shell" />

        <el-result v-else-if="loadError" icon="error" title="修复前后对比加载失败" :sub-title="loadError">
            <template #extra><el-button type="primary" :icon="Refresh" @click="loadPage">重新加载</el-button></template>
        </el-result>

        <el-result v-else-if="!project" icon="warning" title="未找到对应的保护修复项目">
            <template #extra><el-button type="primary" @click="router.push('/conservation/overview')">返回保护修复总览</el-button></template>
        </el-result>

        <template v-else>
            <ComparisonSummaryBar
                :project="project"
                :groups="groups"
                :current="current"
                :saving="saving"
                :dirty="dirty"
                @create="openCreate(false)"
                @generate="openCreate(true)"
                @save="saveCurrent"
                @complete="completeEvaluation"
                @batch-archive="batchArchive"
                @process="navigate('process')"
                @archive="navigate('archive')"
                @restoration="saveAndContinue"
                @open-list="mobileListVisible = true"
            />

            <div v-if="groups.length && current" class="comparison-body">
                <div class="list-column">
                    <ComparisonGroupList
                        :groups="groups"
                        :active-id="activeId"
                        :diseases="diseases"
                        :steps="steps"
                        @select="selectGroup"
                        @create="openCreate(false)"
                        @delete="deleteGroup"
                    />
                </div>
                <main class="comparison-workspace-scroll">
                    <ComparisonWorkspace
                        ref="workspaceRef"
                        :group="current"
                        :step="currentStep"
                        :read-only="readOnly"
                        @dirty="markDirty"
                        @navigate="navigate"
                        @reorder="reorderImages"
                    />
                </main>
                <div class="assist-column">
                    <ComparisonAssistPanel
                        :group="current"
                        :step="currentStep"
                        :completeness="completeness"
                        :missing-items="missingItems"
                        :read-only="readOnly"
                        @dirty="markDirty"
                        @locate="locate"
                        @navigate="navigate"
                        @reopen="reopen"
                    />
                </div>
                <el-button class="assist-fab" circle type="primary" :icon="Setting" @click="assistDrawerVisible = true" />
            </div>

            <div v-else class="empty-comparison">
                <div class="empty-visual"><el-icon><Picture /></el-icon></div>
                <h2>当前项目尚未建立修复前后对比组</h2>
                <p>可以先建立空白草稿，修复过程、前后影像及评价信息均可稍后补充，不影响进入后续页面。</p>
                <div class="empty-flow"><span>选择步骤</span><i>→</i><span>匹配前后影像</span><i>→</i><span>填写评价</span><i>→</i><span>形成监测基准</span></div>
                <el-button v-if="processRecord && sourceMedia.length" type="primary" size="large" :icon="Picture" @click="openCreate(true)">从过程影像生成</el-button>
                <el-button size="large" @click="openCreate(false)">新建空白对比组</el-button>
            </div>
        </template>
    </div>

    <el-drawer v-model="mobileListVisible" title="对比组列表" direction="ltr" size="min(330px, 90vw)" :with-header="false">
        <ComparisonGroupList
            :groups="groups"
            :active-id="activeId"
            :diseases="diseases"
            :steps="steps"
            @select="selectGroup"
            @create="openCreate(false)"
            @delete="deleteGroup"
        />
    </el-drawer>
    <el-drawer v-model="assistDrawerVisible" title="评价辅助" direction="rtl" size="min(330px, 90vw)">
        <ComparisonAssistPanel
            v-if="current"
            :group="current"
            :step="currentStep"
            :completeness="completeness"
            :missing-items="missingItems"
            :read-only="readOnly"
            @dirty="markDirty"
            @locate="locate"
            @navigate="navigate"
            @reopen="reopen"
        />
    </el-drawer>

    <el-dialog v-model="createDialog" :title="createFromProcess ? '从修复过程建立对比组' : '新建对比草稿'" width="min(920px, 96vw)" top="5vh" destroy-on-close>
        <el-steps :active="createStep" align-center finish-status="success">
            <el-step title="关联信息" description="步骤、病害与部位" />
            <el-step title="选择影像" description="前图、后图与过程图" />
            <el-step title="基本信息" description="标题与状态摘要" />
        </el-steps>

        <div v-if="createStep === 0" class="create-pane">
            <el-form label-position="top">
                <div class="form-grid">
                    <el-form-item label="修复过程"><el-input :model-value="processRecord?.processName || '未建立（可保存空白草稿）'" disabled /></el-form-item>
                    <el-form-item label="修复步骤">
                        <el-select v-model="createForm.stepId" filterable placeholder="选择产生影像的步骤" @change="handleCreateStepChange">
                            <el-option v-for="item in steps" :key="item.id" :label="`${item.sequenceNo}. ${item.stepName}`" :value="item.id">
                                <span>{{ item.sequenceNo }}. {{ item.stepName }}</span><small class="option-status">{{ item.stepStatus }}</small>
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="目标部位"><el-input v-model="createForm.targetPart" /></el-form-item>
                    <el-form-item label="拍摄方位"><el-input v-model="createForm.shootingPosition" /></el-form-item>
                    <el-form-item label="对比类型">
                        <el-select v-model="createForm.comparisonType">
                            <el-option label="修复前后两阶段" value="before_after" /><el-option label="多阶段过程" value="multi_stage" />
                            <el-option label="局部细节" value="detail" /><el-option label="显微形貌" value="microscope" />
                            <el-option label="检测或量化指标" value="measurement" /><el-option label="复原前后" value="restoration" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="关联病害">
                        <el-select v-model="createForm.diseaseIds" multiple collapse-tags :disabled="createForm.overallComparison">
                            <el-option v-for="item in diseases" :key="item.id" :label="`${item.diseaseName} · ${item.partName}`" :value="item.id" />
                        </el-select>
                    </el-form-item>
                    <el-form-item class="span-2"><el-checkbox v-model="createForm.overallComparison">本组为整体效果对比，不关联单项病害</el-checkbox></el-form-item>
                </div>
            </el-form>
        </div>

        <div v-else-if="createStep === 1" class="create-pane">
            <el-alert
                v-if="selectedMedia.some(item => item.imageStage === 'before') && selectedMedia.some(item => item.imageStage === 'after') && selectedMedia.find(item => item.imageStage === 'before')?.shootingPosition !== selectedMedia.find(item => item.imageStage === 'after')?.shootingPosition"
                title="当前选择的修复前后影像拍摄角度可能不一致，可能影响对比效果。"
                type="warning"
                :closable="false"
                show-icon
            />
            <div class="media-summary">
                <el-tag :type="createHasBefore ? 'success' : 'danger'">修复前 {{ selectedMedia.filter(item => item.imageStage === 'before').length }}张</el-tag>
                <el-tag :type="createHasAfter ? 'success' : 'danger'">修复后 {{ selectedMedia.filter(item => item.imageStage === 'after').length }}张</el-tag>
                <el-tag type="info">过程/其他 {{ selectedMedia.filter(item => !['before','after'].includes(item.imageStage)).length }}张</el-tag>
                <span>优先显示已在过程记录中标记“加入前后对比”的影像。</span>
            </div>
            <div class="source-media-grid">
                <button
                    v-for="item in availableMedia"
                    :key="item.id"
                    class="source-media"
                    :class="{ selected: selectedMediaIds.includes(item.id), recommended: item.selectedForComparison }"
                    @click="toggleMedia(item)"
                >
                    <img :src="item.thumbnailUrl || item.fileUrl" :alt="item.fileName">
                    <div><b>{{ sourceStageMap[item.imageStage] }}</b><span>{{ item.fileName }}</span><small>{{ item.targetPart }} · {{ item.shootingPosition }} · {{ item.shootingTime }}</small></div>
                    <el-tag v-if="item.selectedForComparison" size="small" type="primary">过程已标记</el-tag>
                    <i>{{ selectedMediaIds.includes(item.id) ? '✓' : '+' }}</i>
                </button>
                <el-empty v-if="!availableMedia.length" description="该步骤暂无可用过程影像" />
            </div>
        </div>

        <div v-else class="create-pane">
            <el-form label-position="top">
                <div class="form-grid">
                    <el-form-item label="对比编号"><el-input v-model="createForm.comparisonCode" /></el-form-item>
                    <el-form-item label="对比标题"><el-input v-model="createForm.comparisonTitle" /></el-form-item>
                    <el-form-item class="span-2" label="修复前状态摘要"><el-input v-model="createForm.beforeSummary" type="textarea" :rows="3" /></el-form-item>
                    <el-form-item class="span-2" label="修复后状态摘要"><el-input v-model="createForm.afterSummary" type="textarea" :rows="3" /></el-form-item>
                    <el-form-item class="span-2" label="对比说明"><el-input v-model="createForm.comparisonDescription" type="textarea" :rows="3" /></el-form-item>
                </div>
            </el-form>
        </div>

        <template #footer>
            <el-button v-if="createStep > 0" @click="createStep--">上一步</el-button>
            <el-button @click="createDialog = false">取消</el-button>
            <el-button v-if="createStep < 2" type="primary" @click="nextCreateStep">下一步</el-button>
            <el-button v-else type="primary" @click="createGroup">建立对比组</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.comparison-page { height: calc(100vh - 64px); min-height: 650px; display: flex; flex-direction: column; overflow: hidden; background: #eef1ef; }
.loading-shell { margin: 24px; padding: 24px; background: #fff; }
.comparison-body { min-height: 0; flex: 1; display: grid; grid-template-columns: 300px minmax(520px, 1fr) 280px; overflow: hidden; }
.list-column, .assist-column { min-width: 0; height: 100%; overflow: hidden; }
.comparison-workspace-scroll { min-width: 0; height: 100%; overflow-y: auto; overflow-x: hidden; background: #eef1ef; }
.assist-fab { display: none; position: fixed; z-index: 9; right: 22px; bottom: 28px; box-shadow: 0 4px 14px rgb(33 70 58 / 30%); }
.empty-comparison { flex: 1; display: grid; place-content: center; justify-items: center; padding: 40px; text-align: center; background: #f7f9f8; }
.empty-visual { width: 92px; height: 92px; display: grid; place-items: center; border-radius: 50%; color: #4c7869; background: #e6f0ec; font-size: 42px; }
.empty-comparison h2 { margin: 22px 0 8px; color: #30453d; }
.empty-comparison p { max-width: 620px; margin: 0 0 18px; color: #84918c; }
.empty-flow { display: flex; align-items: center; gap: 10px; margin-bottom: 25px; color: #60726b; font-size: 11px; }
.empty-flow span { padding: 7px 10px; background: #fff; border: 1px solid #dce4e0; }
.empty-flow i { color: #a27c52; font-style: normal; }
:deep(.el-drawer__body) { padding: 0; overflow: hidden; }
.create-pane { min-height: 430px; margin-top: 22px; padding: 4px 6px; max-height: 62vh; overflow-y: auto; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
.form-grid .span-2 { grid-column: 1/-1; }
.option-status { float: right; margin-left: 20px; color: #9aa29f; }
.media-summary { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
.media-summary span { margin-left: auto; color: #899490; font-size: 10px; }
.source-media-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.source-media { min-width: 0; display: grid; grid-template-columns: 110px 1fr auto; gap: 10px; align-items: center; padding: 8px; text-align: left; border: 1px solid #dfe5e2; background: #fff; border-radius: 7px; cursor: pointer; position: relative; }
.source-media:hover { border-color: #7ba394; }
.source-media.recommended { box-shadow: inset 3px 0 #759c8d; }
.source-media.selected { border-color: #39725f; background: #f2f8f5; }
.source-media img { width: 110px; height: 76px; object-fit: cover; border-radius: 4px; }
.source-media > div { min-width: 0; }
.source-media b, .source-media span, .source-media small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.source-media b { color: #3d5149; font-size: 11px; }.source-media span { margin: 4px 0; color: #68736f; font-size: 10px; }.source-media small { color: #9aa29f; font-size: 8px; }
.source-media i { position: absolute; right: 7px; bottom: 7px; width: 20px; height: 20px; display: grid; place-items: center; border-radius: 50%; color: #fff; background: #5f8d7c; font-style: normal; }
@media (max-width: 1280px) {
    .comparison-body { grid-template-columns: 290px minmax(480px, 1fr); }
    .assist-column { display: none; }
    .assist-fab { display: inline-flex; }
}
@media (max-width: 920px) {
    .comparison-page { height: calc(100vh - 56px); }
    .comparison-body { grid-template-columns: 1fr; }
    .list-column { display: none; }
    .source-media-grid { grid-template-columns: 1fr; }
}
@media (max-width: 620px) {
    .form-grid { grid-template-columns: 1fr; }
    .form-grid .span-2 { grid-column: auto; }
    .empty-flow { flex-wrap: wrap; justify-content: center; }
    .source-media { grid-template-columns: 88px 1fr; }
    .source-media img { width: 88px; height: 66px; }
}
</style>
