<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Refresh, Setting, Tools } from '@element-plus/icons-vue'
import ProcessSummaryBar from './components/process/ProcessSummaryBar.vue'
import ProcessStepList from './components/process/ProcessStepList.vue'
import ProcessStepPaper from './components/process/ProcessStepPaper.vue'
import ProcessAssistPanel from './components/process/ProcessAssistPanel.vue'
import {
    createProcess as createProcessApi,
    generateProcessSteps,
    getProcessWorkbench,
    saveProcessWorkbench
} from '@/api/conservationProcess'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId || route.query.projectId) || null)

const loading = ref(true)
const loadError = ref('')
const project = ref(null)
const archive = ref(null)
const archiveWorkspace = ref(null)
const formalPlan = ref(null)
const processRecord = ref(null)
const steps = ref([])
const activeStepId = ref(null)
const saving = ref(false)
const dirty = ref(false)
const editMode = ref(false)
const assistOpen = ref(false)
const mobileStepsVisible = ref(false)

const tempStepDialog = ref(false)
const pauseDialog = ref(false)
const resumeDialog = ref(false)
const skipDialog = ref(false)
const pendingSkipId = ref(null)
const tempStepForm = reactive({
    stepName: '', stepType: 'other', temporaryReason: '', targetPart: '', diseaseIds: [],
    plannedMethod: '', plannedStartTime: '', plannedEndTime: '', operatorName: '',
    requiresPlanChange: false, requiresQualityCheck: true, requiresMedia: true,
    insertAfterId: null, progressWeight: 5
})
const pauseForm = reactive({ reason: '', pauseTime: '', artifactState: '', protectionMeasure: '', expectedResumeTime: '' })
const resumeForm = reactive({ resumeTime: '', operator: '', preCheck: '', artifactState: '', canContinue: true })
const skipReason = ref('')

const activeStep = computed(() => steps.value.find(item => item.id === activeStepId.value) || null)
const activeIndex = computed(() => steps.value.findIndex(item => item.id === activeStepId.value))
const previousStep = computed(() => activeIndex.value > 0 ? steps.value[activeIndex.value - 1] : null)
const nextStep = computed(() => activeIndex.value >= 0 && activeIndex.value < steps.value.length - 1 ? steps.value[activeIndex.value + 1] : null)
const completedSteps = computed(() => steps.value.filter(item => ['completed', 'skipped'].includes(item.stepStatus)).length)
const unresolvedIssues = computed(() => steps.value.reduce(
    (sum, step) => sum + step.issues.filter(item => !['resolved', 'closed'].includes(item.issueStatus)).length, 0
))

const calculateStepCompleteness = step => {
    if (!step) return 0
    let score = 0
    if (step.operatorName && step.plannedMethod && (step.actualMethod || step.stepStatus === 'pending')) score += 10
    if (step.operationLogs.length) score += 20
    if (step.noMaterialRequired || (step.materials.length && step.materials.every(item => item.actualAmount !== null && item.amountUnit))) score += 15
    if (step.tools.length && step.processParameters.length) score += 10
    if (step.environments.length) score += 5
    if (!step.requiresMedia) score += 20
    else {
        const stages = new Set(step.media.map(item => item.mediaStage))
        score += ['before', 'during', 'after'].filter(stage => stages.has(stage)).length / 3 * 20
    }
    const unresolved = step.issues.filter(item => !['resolved', 'closed'].includes(item.issueStatus))
    if (!unresolved.length) score += 10
    else if (!unresolved.some(item => ['high', 'critical'].includes(item.severity))) score += 5
    if (!step.requiresQualityCheck) score += 10
    else if (step.qualityChecks.some(item => item.checkResult === 'passed') && !step.qualityChecks.some(item => item.checkResult === 'failed')) score += 10
    else if (step.qualityChecks.some(item => item.checkResult === 'conditional')) score += 5
    return Math.round(score)
}

const recalculateAll = () => {
    steps.value.forEach(step => {
        if (step.plannedMethod && step.actualMethod && step.plannedMethod.trim() !== step.actualMethod.trim()) {
            step.deviationFlag = true
            step.deviationLevel ||= 'minor'
        }
        step.completionRate = step.stepStatus === 'completed' ? 100 : calculateStepCompleteness(step)
    })
    if (processRecord.value) {
        processRecord.value.totalSteps = steps.value.filter(item => item.stepStatus !== 'cancelled').length
        processRecord.value.completedSteps = completedSteps.value
        processRecord.value.progress = processProgress.value
    }
}

const processProgress = computed(() => {
    const valid = steps.value.filter(item => item.stepStatus !== 'cancelled')
    const totalWeight = valid.reduce((sum, item) => sum + (Number(item.progressWeight) || 0), 0)
    if (!valid.length) return 0
    if (!totalWeight) return Math.round(completedSteps.value / valid.length * 100)
    const completeWeight = valid
        .filter(item => ['completed', 'skipped'].includes(item.stepStatus))
        .reduce((sum, item) => sum + (Number(item.progressWeight) || 0), 0)
    return Math.round(completeWeight / totalWeight * 100)
})

const stepDifferences = computed(() => {
    const step = activeStep.value
    if (!step) return []
    const result = []
    const add = (key, label, description, level = 'minor') => result.push({
        key, label, description, level,
        levelText: level === 'major' ? '重大偏差' : level === 'general' ? '一般偏差' : '轻微偏差'
    })
    if (step.temporaryStep && step.requiresPlanChange) {
        add('temporary-plan-change', '临时步骤已登记方案调整', step.temporaryReason || '待补充临时步骤原因', 'major')
    }
    if (step.deviationFlag) add('method', '实际方法与计划不同', step.deviationReason || '待填写偏差原因', step.deviationLevel || 'general')
    step.materials.filter(item => item.deviationFlag).forEach(item => add(
        `material-${item.id}`, `${item.materialName}参数调整`, item.deviationReason || '浓度或配比发生变化',
        item.requiresPlanChange ? 'major' : 'general'
    ))
    step.processParameters.filter(item => item.plannedValue && item.plannedValue !== item.actualValue).forEach(item =>
        add(`parameter-${item.id}`, `${item.parameterName}发生偏差`, `${item.plannedValue} → ${item.actualValue}`, 'minor')
    )
    step.tools.filter(item => item.plannedParameter && item.plannedParameter !== item.actualParameter).forEach(item =>
        add(`tool-${item.id}`, `${item.toolName}参数变化`, `${item.plannedParameter} → ${item.actualParameter}`, 'minor')
    )
    return result
})

const stepProblems = computed(() => {
    const step = activeStep.value
    if (!step) return {}
    return {
        unresolvedIssues: step.issues.filter(item => !['resolved', 'closed'].includes(item.issueStatus)).length,
        pendingDeviations: stepDifferences.value.length,
        failedChecks: step.qualityChecks.filter(item => item.checkResult === 'failed').length,
        newDiseases: step.issues.filter(item => item.requiresNewDiseaseRecord && !['resolved', 'closed'].includes(item.issueStatus)).length
    }
})

const applyWorkbench = data => {
    project.value = data?.project || null
    archive.value = data?.archive || null
    archiveWorkspace.value = data?.archiveWorkspace || null
    formalPlan.value = data?.formalPlan || null
    processRecord.value = data?.processRecord || null
    steps.value = data?.steps || []
}

const loadPage = async () => {
    loading.value = true
    loadError.value = ''
    try {
        if (!projectId.value) throw new Error('缺少保护修复项目ID')
        const result = await getProcessWorkbench(projectId.value)
        applyWorkbench(result.data)
        recalculateAll()
        const requestedStepId = Number(route.query.stepId)
        activeStepId.value = steps.value.find(item => item.id === requestedStepId)?.id
            || steps.value.find(item => item.stepStatus === 'in_progress')?.id
            || steps.value.find(item => item.stepStatus === 'pending')?.id
            || steps.value[0]?.id
        dirty.value = false
    } catch (error) {
        console.error(error)
        loadError.value = error.message || '修复过程加载失败'
        ElMessage.error(loadError.value)
    } finally {
        loading.value = false
    }
}

const generateProcess = async () => {
    if (!formalPlan.value || formalPlan.value.planStatus !== 'completed') return
    loading.value = true
    try {
        const result = await createProcessApi(projectId.value)
        applyWorkbench(result.data)
        activeStepId.value = steps.value[0]?.id
        dirty.value = false
        recalculateAll()
        ElMessage.success(`已从正式方案生成${steps.value.length}个修复执行步骤`)
    } catch (error) {
        ElMessage.error(error.message || '生成修复过程失败')
    } finally {
        loading.value = false
    }
}

const saveCurrentStep = async (showMessage = true) => {
    if (!processRecord.value || !activeStep.value) return
    saving.value = true
    try {
        recalculateAll()
        processRecord.value.progress = processProgress.value
        processRecord.value.completedSteps = completedSteps.value
        const currentId = activeStepId.value
        const result = await saveProcessWorkbench(processRecord.value.id, processRecord.value, steps.value)
        applyWorkbench(result.data)
        activeStepId.value = steps.value.find(item => item.id === currentId)?.id || steps.value[0]?.id
        dirty.value = false
        if (showMessage) ElMessage.success(`当前步骤已保存：${processRecord.value.updateTime}`)
    } catch (error) {
        ElMessage.error(error.message || '保存失败，请稍后重试')
    } finally {
        saving.value = false
    }
}

const startStep = async () => {
    const step = activeStep.value
    if (!step) return
    const errors = []
    if (processRecord.value.processStatus === 'paused') errors.push('当前过程已暂停')
    if (step.stepStatus !== 'pending') errors.push('当前步骤不是待开始状态')
    if (previousStep.value && !['completed', 'skipped'].includes(previousStep.value.stepStatus)) errors.push('上一步尚未完成或跳过')
    if (steps.value.some(item => item.id !== step.id && item.stepStatus === 'in_progress' && !item.parallelStep)) errors.push('已有其他普通步骤正在进行')
    if (!step.operatorName) errors.push('未填写实际操作人员')
    if (!step.plannedMethod) errors.push('缺少计划方法')
    if (!step.relatedDiseases.length && !step.nonDiseaseStep) errors.push('未关联病害，也未标记为非病害处理步骤')
    if (errors.length) return showValidation('当前步骤无法开始', errors)

    await ElMessageBox.confirm(`确认开始“${step.stepName}”？`, '开始步骤', { type: 'warning' })
    step.stepStatus = 'in_progress'
    step.actualStartTime = new Date().toLocaleString('zh-CN', { hour12: false })
    processRecord.value.processStatus = 'in_progress'
    if (!processRecord.value.startDate) processRecord.value.startDate = step.actualStartTime.slice(0, 10)
    project.value.currentStage = 'repair'
    dirty.value = true
    recalculateAll()
    await saveCurrentStep(false)
    ElMessage.success('步骤已开始，项目阶段已更新为修复处理中')
}

const openPause = () => {
    Object.assign(pauseForm, {
        reason: '', pauseTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        artifactState: '', protectionMeasure: '', expectedResumeTime: ''
    })
    pauseDialog.value = true
}
const pauseProcessAction = async () => {
    if (!pauseForm.reason || !pauseForm.artifactState || !pauseForm.protectionMeasure) return ElMessage.warning('请填写暂停原因、文物状态和临时保护措施')
    processRecord.value.processStatus = 'paused'
    if (activeStep.value?.stepStatus === 'in_progress') activeStep.value.stepStatus = 'paused'
    processRecord.value.pauseRecord = { ...pauseForm }
    pauseDialog.value = false
    dirty.value = true
    await saveCurrentStep(false)
    ElMessage.success('修复过程已暂停')
}

const openResume = () => {
    Object.assign(resumeForm, {
        resumeTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        operator: processRecord.value.supervisor, preCheck: '', artifactState: '', canContinue: true
    })
    resumeDialog.value = true
}
const resumeProcessAction = async () => {
    if (!resumeForm.operator || !resumeForm.preCheck || !resumeForm.artifactState) return ElMessage.warning('请填写恢复人员、恢复前检查和文物当前状态')
    if (!resumeForm.canContinue) return ElMessage.warning('当前检查结论不允许继续原步骤')
    processRecord.value.processStatus = 'in_progress'
    if (activeStep.value?.stepStatus === 'paused') activeStep.value.stepStatus = 'in_progress'
    processRecord.value.pauseRecord = { ...processRecord.value.pauseRecord, resume: { ...resumeForm } }
    resumeDialog.value = false
    dirty.value = true
    await saveCurrentStep(false)
    ElMessage.success('修复过程已恢复')
}

const completionErrors = step => {
    const errors = []
    if (!step.actualMethod) errors.push({ text: '未填写实际操作方法', section: 'basic' })
    if (!step.operationLogs.length) errors.push({ text: '至少需要一条操作日志', section: 'logs' })
    if (!step.noMaterialRequired && (!step.materials.length || step.materials.some(item => item.actualAmount === null || !item.amountUnit))) errors.push({ text: '实际材料记录不完整，或请明确标记无需材料', section: 'materials' })
    if (!step.tools.length || !step.processParameters.length) errors.push({ text: '工具或工艺参数记录不完整', section: 'tools' })
    if (step.requiresMedia) {
        const stages = new Set(step.media.map(item => item.mediaStage))
        const missing = ['before', 'during', 'after'].filter(stage => !stages.has(stage))
        if (missing.length) errors.push({ text: '缺少操作前、中、后必需影像', section: 'media' })
    }
    if (step.issues.some(item => ['high', 'critical'].includes(item.severity) && !['resolved', 'closed'].includes(item.issueStatus))) errors.push({ text: '存在未解决的严重异常', section: 'issues' })
    if (step.qualityChecks.some(item => item.checkResult === 'failed')) errors.push({ text: '存在未通过的质量检查', section: 'checks' })
    if (step.requiresQualityCheck && !step.qualityChecks.some(item => item.checkResult === 'passed')) errors.push({ text: '关键步骤至少需要一条通过的质量检查', section: 'checks' })
    if (!step.result.actualResult || !step.result.finalConclusion) errors.push({ text: '未填写步骤实际结果或最终结论', section: 'result' })
    if (calculateStepCompleteness(step) < 80) errors.push({ text: `步骤完整度需达到80%，当前为${calculateStepCompleteness(step)}%`, section: 'basic' })
    return errors
}

const completeStep = async () => {
    const step = activeStep.value
    if (!step || step.stepStatus !== 'in_progress') return
    const failedCheck = step.qualityChecks.some(item => item.checkResult === 'failed')
    if (failedCheck) {
        step.stepStatus = 'rework'
        dirty.value = true
        await saveCurrentStep(false)
        return ElMessage.error('质量检查存在未通过项，步骤已标记为需返工')
    }
    const errors = completionErrors(step)
    if (errors.length) {
        await showValidation('当前步骤暂不能完成', errors.map(item => item.text))
        scrollToSection(errors[0].section)
        return
    }
    await ElMessageBox.confirm(`确认完成“${step.stepName}”？完成后记录将进入只读模式。`, '完成步骤', { type: 'warning' })
    step.stepStatus = 'completed'
    step.actualEndTime = new Date().toLocaleString('zh-CN', { hour12: false })
    step.completionRate = 100
    step.relatedDiseases.forEach(item => {
        if (['untreated', 'in_treatment'].includes(item.treatmentStatus)) item.treatmentStatus = step.result.monitoringRequired ? 'monitoring_required' : 'controlled'
    })
    recalculateAll()
    const next = steps.value.find(item => item.sequenceNo > step.sequenceNo && item.stepStatus === 'pending')
    if (next) activeStepId.value = next.id
    else if (completedSteps.value === steps.value.filter(item => item.stepStatus !== 'cancelled').length) {
        processRecord.value.processStatus = 'completed'
        processRecord.value.actualEndDate = step.actualEndTime.slice(0, 10)
        project.value.currentStage = 'evaluation'
    }
    dirty.value = true
    await saveCurrentStep(false)
    ElMessage.success(next ? '步骤已完成，已定位到下一待执行步骤' : '全部修复步骤已完成')
}

const showValidation = (title, errors) => ElMessageBox.alert(
    `<div style="line-height:1.9">${errors.map(item => `• ${item}`).join('<br>')}</div>`,
    title,
    { dangerouslyUseHTMLString: true, type: 'warning', confirmButtonText: '返回完善' }
)

const openTempStep = (parent = null) => {
    Object.assign(tempStepForm, {
        stepName: parent ? `${parent.stepName}返工` : '', stepType: parent?.stepType || 'other',
        temporaryReason: parent ? '质量检查或完成后发现问题，需要返工' : '',
        targetPart: parent?.targetPart || '', diseaseIds: parent?.relatedDiseases.map(item => item.diseaseRecordId) || [],
        plannedMethod: '', plannedStartTime: '', plannedEndTime: '', operatorName: '',
        requiresPlanChange: false, requiresQualityCheck: true, requiresMedia: true,
        insertAfterId: parent?.id || activeStep.value?.id || null, progressWeight: 5,
        parentStepId: parent?.id || null
    })
    tempStepDialog.value = true
}

const createTempStep = async () => {
    if (!tempStepForm.stepName || !tempStepForm.temporaryReason || !tempStepForm.plannedMethod) return ElMessage.warning('请填写步骤名称、新增原因和计划方法')
    const insertIndex = steps.value.findIndex(item => item.id === tempStepForm.insertAfterId)
    const selectedDiseases = archiveWorkspace.value.diseaseRecords
        .filter(item => tempStepForm.diseaseIds.includes(item.id))
        .map(item => ({
            id: Date.now() + item.id, diseaseRecordId: item.id, diseaseName: item.diseaseName,
            severity: item.severity, developmentStatus: item.developmentStatus, partName: item.partName,
            treatmentStatus: 'untreated', monitoringRequired: false
        }))
    const step = {
        id: Date.now(), processId: processRecord.value.id, planMeasureId: null,
        parentStepId: tempStepForm.parentStepId || null, stepCode: '', stepName: tempStepForm.stepName,
        stepType: tempStepForm.stepType, stepStatus: 'pending', sequenceNo: 0,
        progressWeight: tempStepForm.progressWeight, plannedStartTime: tempStepForm.plannedStartTime,
        plannedEndTime: tempStepForm.plannedEndTime, actualStartTime: '', actualEndTime: '',
        operatorName: tempStepForm.operatorName, assistantNames: '', operationLocation: '文物保护实验室',
        targetPart: tempStepForm.targetPart, plannedMethod: tempStepForm.plannedMethod, actualMethod: '',
        operationDescription: '', resultDescription: '', deviationFlag: false, deviationLevel: 'minor',
        deviationReason: '', adjustmentDescription: '', requiresMedia: tempStepForm.requiresMedia,
        requiresQualityCheck: tempStepForm.requiresQualityCheck, requiresMonitoring: false,
        generateRestorationResult: false, qualityStatus: '', completionRate: 0,
        temporaryStep: true, temporaryReason: tempStepForm.temporaryReason, nonDiseaseStep: !selectedDiseases.length,
        noMaterialRequired: false, requiresPlanChange: tempStepForm.requiresPlanChange,
        planMaterials: [], relatedDiseases: selectedDiseases, operationLogs: [], materials: [], tools: [],
        processParameters: [], environments: [], media: [], issues: [], qualityChecks: [],
        result: {
            targetCompleted: '', diseaseTreatmentEffect: '', artifactStateChange: '', actualResult: '',
            expectedReached: '', sideEffects: '', remainingProblems: '', allowNextStep: true,
            monitoringRequired: false, monitoringObject: '', monitoringIndicators: '', monitoringCycle: '',
            warningConditions: '', monitoringAdvice: '', finalConclusion: ''
        }
    }
    if (insertIndex >= 0) steps.value.splice(insertIndex + 1, 0, step)
    else steps.value.push(step)
    resequenceSteps()
    activeStepId.value = step.id
    tempStepDialog.value = false
    dirty.value = true
    recalculateAll()
    await saveCurrentStep(false)
    ElMessage.success('临时步骤已添加并保存')
}

const moveStep = (id, direction) => {
    const index = steps.value.findIndex(item => item.id === id)
    const target = index + direction
    if (index < 0 || target < 0 || target >= steps.value.length) return
    if (!['pending', 'skipped'].includes(steps.value[target].stepStatus)) return ElMessage.warning('不能跨越已开始或已完成步骤调整顺序')
    const [step] = steps.value.splice(index, 1)
    steps.value.splice(target, 0, step)
    resequenceSteps()
    dirty.value = true
}
const resequenceSteps = () => steps.value.forEach((step, index) => {
    step.sequenceNo = index + 1
    step.stepCode = `STEP-${String(index + 1).padStart(2, '0')}`
})
const deleteStep = async id => {
    const step = steps.value.find(item => item.id === id)
    if (!step || !['pending', 'skipped'].includes(step.stepStatus)) return ElMessage.warning('仅能删除尚未执行的步骤')
    await ElMessageBox.confirm(`确认删除“${step.stepName}”？正式方案不会被修改。`, '删除步骤', { type: 'warning' })
    steps.value = steps.value.filter(item => item.id !== id)
    resequenceSteps()
    activeStepId.value = steps.value[Math.max(0, activeIndex.value - 1)]?.id
    dirty.value = true
    recalculateAll()
}
const openSkip = id => {
    pendingSkipId.value = id
    skipReason.value = ''
    skipDialog.value = true
}
const confirmSkip = () => {
    if (!skipReason.value) return ElMessage.warning('跳过计划步骤必须填写原因')
    const step = steps.value.find(item => item.id === pendingSkipId.value)
    if (step) {
        step.stepStatus = 'skipped'
        step.adjustmentDescription = `跳过原因：${skipReason.value}`
        step.deviationFlag = true
        step.deviationLevel = 'general'
        step.deviationReason = skipReason.value
    }
    skipDialog.value = false
    dirty.value = true
    recalculateAll()
}

const syncFromPlan = async () => {
    if (processRecord.value.processStatus !== 'not_started') {
        return ElMessage.info('过程已开始，可直接修改正式方案；为保留历史记录，已开始的执行步骤不会自动重建。')
    }
    await ElMessageBox.confirm('重新同步会用正式方案重新生成尚未执行的步骤，是否继续？', '同步正式方案', { type: 'warning' })
    const result = await generateProcessSteps(processRecord.value.id)
    applyWorkbench(result.data)
    activeStepId.value = steps.value[0]?.id
    dirty.value = false
    recalculateAll()
    ElMessage.success('已重新同步正式方案')
}

const selectStep = id => {
    activeStepId.value = id
    mobileStepsVisible.value = false
    nextTick(() => document.querySelector('.process-paper-scroll')?.scrollTo({ top: 0, behavior: 'smooth' }))
}
const scrollToSection = section => {
    assistOpen.value = false
    nextTick(() => document.getElementById(`process-section-${section}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
}
const navigate = async target => {
    const routes = {
        archive: `/conservation/project/${projectId.value}/archive`,
        plan: `/conservation/project/${projectId.value}/archive?section=plan`,
        'plan-edit': `/conservation/project/${projectId.value}/archive?section=plan&sourceStepId=${activeStepId.value}&mode=direct`,
        disease: `/conservation/project/${projectId.value}/disease`,
        'new-disease': `/conservation/project/${projectId.value}/disease?sourceStepId=${activeStepId.value}`
    }
    if (routes[target] && await confirmLeave()) {
        dirty.value = false
        router.push(routes[target])
    }
}

const confirmLeave = async () => {
    if (!dirty.value) return true
    try {
        await ElMessageBox.confirm('当前步骤存在未保存内容，离开后修改将丢失。', '未保存提示', {
            confirmButtonText: '放弃修改并离开', cancelButtonText: '继续编辑', type: 'warning'
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
    <div class="process-page">
        <el-skeleton v-if="loading" animated :rows="12" class="loading-shell" />

        <el-result v-else-if="loadError" icon="error" title="修复过程加载失败" :sub-title="loadError">
            <template #extra><el-button type="primary" :icon="Refresh" @click="loadPage">重新加载</el-button></template>
        </el-result>

        <el-result v-else-if="!project" icon="warning" title="未找到对应的保护修复项目">
            <template #extra><el-button type="primary" @click="router.push('/conservation/overview')">返回保护修复总览</el-button></template>
        </el-result>

        <el-result v-else-if="!archive" icon="warning" title="当前项目尚未建立保护修复档案" sub-title="建立正式档案后才能从方案生成修复执行过程。">
            <template #extra><el-button type="primary" :icon="Document" @click="router.push(`/conservation/project/${projectId}/archive`)">前往保护修复档案</el-button></template>
        </el-result>

        <el-result v-else-if="!formalPlan || formalPlan.planStatus!=='completed'" icon="warning" title="当前档案尚未完成保护修复方案" sub-title="请先在保护修复档案中完善方案并将方案状态设为“已完成”。">
            <template #extra><el-button type="primary" @click="router.push(`/conservation/project/${projectId}/archive?section=plan`)">前往编制方案</el-button></template>
        </el-result>

        <div v-else-if="!processRecord" class="empty-process">
            <div class="empty-icon"><el-icon><Tools /></el-icon></div>
            <h2>当前项目尚未建立修复执行过程</h2>
            <p>可以根据已提交的正式保护修复方案，自动生成修复步骤，并开始记录实际操作。</p>
            <div class="source-summary">
                <span>正式方案：{{ formalPlan.planName }}</span>
                <span>方案状态：{{ formalPlan.planStatus }}</span>
                <span>方案措施：{{ archiveWorkspace.planDiseaseList.length }}项</span>
                <span>计划材料：{{ archiveWorkspace.planMaterials.length }}种</span>
            </div>
            <el-button type="primary" size="large" :icon="Tools" @click="generateProcess">从正式方案生成修复过程</el-button>
            <el-button size="large" @click="router.push(`/conservation/project/${projectId}/archive`)">返回保护修复档案</el-button>
        </div>

        <template v-else>
            <ProcessSummaryBar
                :project="project"
                :process="processRecord"
                :current-step="activeStep"
                :progress="processProgress"
                :completed-steps="completedSteps"
                :unresolved-issues="unresolvedIssues"
                :saving="saving"
                :dirty="dirty"
                @save="saveCurrentStep"
                @start-step="startStep"
                @complete-step="completeStep"
                @pause="openPause"
                @resume="openResume"
                @archive="navigate('archive')"
                @plan="navigate('plan')"
                @rework="openTempStep(activeStep)"
                @open-nav="mobileStepsVisible = true"
            />

            <div class="process-body">
                <div class="steps-column">
                    <ProcessStepList
                        :steps="steps"
                        :active-id="activeStepId"
                        :edit-mode="editMode"
                        @select="selectStep"
                        @add="openTempStep()"
                        @sync="syncFromPlan"
                        @toggle-edit="editMode = !editMode"
                        @move="moveStep"
                        @delete="deleteStep"
                        @skip="openSkip"
                    />
                </div>
                <main class="process-paper-scroll">
                    <ProcessStepPaper
                        v-if="activeStep"
                        :step="activeStep"
                        :formal-plan="formalPlan"
                        :read-only="activeStep.stepStatus === 'completed'"
                        @dirty="dirty = true"
                        @recalculate="recalculateAll"
                        @navigate="navigate"
                    />
                    <el-empty v-else description="当前修复过程尚无执行步骤">
                        <el-button type="primary" @click="syncFromPlan">从正式方案生成步骤</el-button>
                        <el-button @click="openTempStep()">新增临时步骤</el-button>
                    </el-empty>
                </main>
                <div class="assist-column" :class="{ open: assistOpen }">
                    <ProcessAssistPanel
                        :step="activeStep"
                        :completeness="activeStep?.completionRate || 0"
                        :differences="stepDifferences"
                        :problems="stepProblems"
                        :previous-step="previousStep"
                        :next-step="nextStep"
                        @section="scrollToSection"
                        @disease="navigate('disease')"
                        @previous="previousStep && selectStep(previousStep.id)"
                        @next="nextStep && selectStep(nextStep.id)"
                        @plan-edit="navigate('plan-edit')"
                        @close="assistOpen = false"
                    />
                </div>
                <el-button class="assist-fab" :icon="Setting" circle type="primary" @click="assistOpen = !assistOpen" />
            </div>

            <el-drawer v-model="mobileStepsVisible" title="修复步骤流程" direction="ltr" size="320px">
                <ProcessStepList
                    :steps="steps"
                    :active-id="activeStepId"
                    :edit-mode="editMode"
                    @select="selectStep"
                    @add="openTempStep()"
                    @sync="syncFromPlan"
                    @toggle-edit="editMode = !editMode"
                    @move="moveStep"
                    @delete="deleteStep"
                    @skip="openSkip"
                />
            </el-drawer>

            <el-dialog v-model="tempStepDialog" title="新增临时修复步骤" width="760px">
                <el-alert title="过程开始后新增的步骤将作为临时步骤保存；如需调整正式方案，可直接修改，无需申请或审批。" type="info" :closable="false" />
                <el-form label-position="top" class="dialog-grid">
                    <el-form-item label="步骤名称"><el-input v-model="tempStepForm.stepName" /></el-form-item>
                    <el-form-item label="步骤类型"><el-select v-model="tempStepForm.stepType"><el-option v-for="item in ['documentation','cleaning','stabilization','desalination','rust_removal','consolidation','correction','bonding','filling','grouting','anchoring','coloring','surface_finish','restoration','evaluation','other']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
                    <el-form-item label="新增/返工原因" class="span-2"><el-input v-model="tempStepForm.temporaryReason" type="textarea" /></el-form-item>
                    <el-form-item label="目标部位"><el-input v-model="tempStepForm.targetPart" /></el-form-item>
                    <el-form-item label="关联病害"><el-select v-model="tempStepForm.diseaseIds" multiple><el-option v-for="item in archiveWorkspace.diseaseRecords" :key="item.id" :label="`${item.diseaseName} · ${item.partName}`" :value="item.id" /></el-select></el-form-item>
                    <el-form-item label="计划方法" class="span-2"><el-input v-model="tempStepForm.plannedMethod" type="textarea" /></el-form-item>
                    <el-form-item label="计划开始"><el-input v-model="tempStepForm.plannedStartTime" /></el-form-item>
                    <el-form-item label="计划结束"><el-input v-model="tempStepForm.plannedEndTime" /></el-form-item>
                    <el-form-item label="操作人员"><el-input v-model="tempStepForm.operatorName" /></el-form-item>
                    <el-form-item label="步骤权重"><el-input-number v-model="tempStepForm.progressWeight" :min="1" :max="100" /></el-form-item>
                    <el-form-item label="插入位置"><el-select v-model="tempStepForm.insertAfterId"><el-option v-for="item in steps" :key="item.id" :label="`在 ${item.stepName} 之后`" :value="item.id" /></el-select></el-form-item>
                    <el-form-item><el-checkbox v-model="tempStepForm.requiresPlanChange">纳入方案调整统计</el-checkbox><el-checkbox v-model="tempStepForm.requiresQualityCheck">要求质量检查</el-checkbox><el-checkbox v-model="tempStepForm.requiresMedia">要求前中后影像</el-checkbox></el-form-item>
                </el-form>
                <template #footer><el-button @click="tempStepDialog=false">取消</el-button><el-button type="primary" @click="createTempStep">添加步骤</el-button></template>
            </el-dialog>

            <el-dialog v-model="pauseDialog" title="暂停修复过程" width="620px">
                <el-form label-position="top" class="dialog-grid">
                    <el-form-item label="暂停原因" class="span-2"><el-input v-model="pauseForm.reason" type="textarea" /></el-form-item>
                    <el-form-item label="暂停时间"><el-input v-model="pauseForm.pauseTime" /></el-form-item>
                    <el-form-item label="预计恢复时间"><el-input v-model="pauseForm.expectedResumeTime" /></el-form-item>
                    <el-form-item label="当前文物状态"><el-input v-model="pauseForm.artifactState" type="textarea" /></el-form-item>
                    <el-form-item label="临时保护措施"><el-input v-model="pauseForm.protectionMeasure" type="textarea" /></el-form-item>
                </el-form>
                <template #footer><el-button @click="pauseDialog=false">取消</el-button><el-button type="warning" @click="pauseProcessAction">确认暂停</el-button></template>
            </el-dialog>

            <el-dialog v-model="resumeDialog" title="恢复修复过程" width="620px">
                <el-form label-position="top" class="dialog-grid">
                    <el-form-item label="恢复时间"><el-input v-model="resumeForm.resumeTime" /></el-form-item>
                    <el-form-item label="恢复人员"><el-input v-model="resumeForm.operator" /></el-form-item>
                    <el-form-item label="恢复前检查"><el-input v-model="resumeForm.preCheck" type="textarea" /></el-form-item>
                    <el-form-item label="文物当前状态"><el-input v-model="resumeForm.artifactState" type="textarea" /></el-form-item>
                    <el-form-item label="可以继续原步骤"><el-switch v-model="resumeForm.canContinue" /></el-form-item>
                </el-form>
                <template #footer><el-button @click="resumeDialog=false">取消</el-button><el-button type="primary" @click="resumeProcessAction">恢复过程</el-button></template>
            </el-dialog>

            <el-dialog v-model="skipDialog" title="跳过计划步骤" width="520px">
                <el-alert title="跳过计划步骤不会修改正式方案，并将作为执行偏差记录。" type="warning" :closable="false" />
                <el-input v-model="skipReason" type="textarea" :rows="4" placeholder="请填写跳过原因" style="margin-top:14px" />
                <template #footer><el-button @click="skipDialog=false">取消</el-button><el-button type="warning" @click="confirmSkip">确认跳过</el-button></template>
            </el-dialog>
        </template>
    </div>
</template>

<style scoped>
.process-page { height: calc(100vh - 76px); min-height: 580px; display: flex; flex-direction: column; gap: 12px; overflow: hidden; }
.loading-shell { padding: 24px; background: #fff; border-radius: 10px; box-sizing: border-box; }
.process-body { min-height: 0; position: relative; display: grid; flex: 1; grid-template-columns: 300px minmax(540px,1fr) 280px; gap: 12px; overflow: hidden; }
.steps-column,.assist-column { min-height: 0; }
.process-paper-scroll { min-width: 0; overflow-y: auto; overflow-x: hidden; padding: 0 4px 18px; }
.assist-fab { display: none; position: absolute; right: 12px; bottom: 18px; z-index: 12; box-shadow: 0 3px 12px rgba(22,104,196,.3); }
.empty-process { max-width: 720px; margin: auto; padding: 48px; text-align: center; background: #fff; border: 1px solid #e5e6eb; border-radius: 14px; box-shadow: 0 6px 25px rgba(29,33,41,.06); }
.empty-icon { width: 76px; height: 76px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; color: #1668c4; background: #eaf3ff; border-radius: 50%; font-size: 35px; }
.empty-process h2 { color: #1d2129; font-size: 21px; }
.empty-process > p { max-width: 520px; margin: 12px auto 22px; color: #86909c; font-size: 13px; line-height: 1.8; }
.source-summary { display: flex; justify-content: center; flex-wrap: wrap; gap: 8px 18px; margin-bottom: 25px; padding: 13px; color: #606266; background: #f7f8fa; border-radius: 8px; font-size: 11px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 0 18px; margin-top: 14px; }
.dialog-grid .span-2 { grid-column: 1/-1; }
.dialog-grid :deep(.el-select),.dialog-grid :deep(.el-input-number) { width: 100%; }
@media (max-width: 1280px) {
    .process-body { grid-template-columns: 280px minmax(520px,1fr); }
    .assist-column { width: 290px; position: absolute; top: 0; right: 0; bottom: 0; z-index: 11; transform: translateX(calc(100% + 20px)); transition: transform .2s ease; }
    .assist-column.open { transform: translateX(0); }
    .assist-fab { display: inline-flex; }
}
@media (max-width: 920px) {
    .process-body { display: block; }
    .steps-column { display: none; }
    .process-paper-scroll { height: 100%; }
}
@media (max-width: 650px) {
    .dialog-grid { grid-template-columns: 1fr; }
    .dialog-grid .span-2 { grid-column: auto; }
}
</style>
