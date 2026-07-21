<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Upload } from '@element-plus/icons-vue'
import {
    deleteProcessMedia,
    getProcessMediaContent,
    uploadProcessMedia
} from '@/api/conservationProcess'

const props = defineProps({
    step: { type: Object, required: true },
    formalPlan: { type: Object, default: () => ({}) },
    readOnly: Boolean
})

const emit = defineEmits(['dirty', 'navigate', 'recalculate'])
const markDirty = () => {
    emit('dirty')
    emit('recalculate')
}

const statusMap = {
    pending: ['待开始', 'info'], in_progress: ['进行中', 'primary'], completed: ['已完成', 'success'],
    paused: ['已暂停', 'warning'], skipped: ['已跳过', 'info'], rework: ['需返工', 'danger']
}
const severityMap = {
    minor: ['轻微', 'info'], moderate: ['中度', 'warning'], severe: ['严重', 'danger'], critical: ['危急', 'danger']
}
const environmentMap = {
    qualified: ['符合要求', 'success'], warning: ['接近限制', 'warning'], unqualified: ['不符合要求', 'danger']
}
const checkMap = {
    passed: ['通过', 'success'], conditional: ['有条件通过', 'warning'], failed: ['未通过', 'danger']
}
const mediaStageMap = {
    before: '操作前', during: '操作中', after: '操作后',
    detail: '局部细节', microscope: '显微影像', environment: '环境记录'
}

const actualMethodChanged = () => {
    const planned = String(props.step.plannedMethod || '').trim()
    const actual = String(props.step.actualMethod || '').trim()
    props.step.deviationFlag = Boolean(planned && actual && planned !== actual)
    markDirty()
}

const dialogMode = ref('create')
const editingId = ref(null)

const logDialog = ref(false)
const logForm = reactive({ recordTime: '', operatorName: '', operationAction: '', observation: '', parameterNote: '', resultNote: '' })
const openLog = (row = null) => {
    dialogMode.value = row ? 'edit' : 'create'; editingId.value = row?.id || null
    Object.assign(logForm, row || { recordTime: new Date().toLocaleString('zh-CN', { hour12: false }), operatorName: props.step.operatorName, operationAction: '', observation: '', parameterNote: '', resultNote: '' })
    logDialog.value = true
}
const saveLog = () => {
    if (!logForm.recordTime || !logForm.operatorName || !logForm.operationAction) return ElMessage.warning('请填写记录时间、操作人员和操作动作')
    if (editingId.value) {
        const index = props.step.operationLogs.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.operationLogs[index] = { ...props.step.operationLogs[index], ...logForm }
    } else props.step.operationLogs.push({ ...logForm, id: Date.now(), locked: false })
    props.step.operationLogs.sort((a, b) => String(a.recordTime).localeCompare(String(b.recordTime)))
    logDialog.value = false; markDirty()
}
const deleteLog = row => {
    if (row.locked) return ElMessage.warning('该操作日志已锁定，不能删除')
    props.step.operationLogs = props.step.operationLogs.filter(item => item.id !== row.id); markDirty()
}

const materialDialog = ref(false)
const materialForm = reactive({
    planMaterialId: null, materialName: '', manufacturer: '', batchNumber: '',
    plannedConcentration: '', actualConcentration: '', plannedMixRatio: '', actualMixRatio: '',
    actualAmount: null, amountUnit: 'ml', applicationMethod: '', applicationCount: 1,
    dryingTime: '', targetPart: '', usagePurpose: '', deviationFlag: false,
    deviationReason: '', safetyNote: '', resultNote: '', temporaryMaterial: false,
    temporaryReason: '', requiresPlanChange: false
})
const openMaterial = (row = null, planMaterial = null) => {
    dialogMode.value = row ? 'edit' : 'create'; editingId.value = row?.id || null
    const source = row || (planMaterial ? {
        planMaterialId: planMaterial.id, materialName: planMaterial.materialName,
        plannedConcentration: planMaterial.concentration || planMaterial.plannedConcentration || '',
        plannedMixRatio: planMaterial.mixRatio || planMaterial.plannedMixRatio || '',
        targetPart: planMaterial.targetPart || '', usagePurpose: planMaterial.purpose || ''
    } : {})
    Object.assign(materialForm, {
        planMaterialId: null, materialName: '', manufacturer: '', batchNumber: '',
        plannedConcentration: '', actualConcentration: '', plannedMixRatio: '', actualMixRatio: '',
        actualAmount: null, amountUnit: 'ml', applicationMethod: '', applicationCount: 1,
        dryingTime: '', targetPart: '', usagePurpose: '', deviationFlag: false,
        deviationReason: '', safetyNote: '', resultNote: '', temporaryMaterial: !planMaterial && !row,
        temporaryReason: '', requiresPlanChange: false,
        ...source
    })
    materialDialog.value = true
}
const compareMaterial = () => {
    materialForm.deviationFlag = Boolean(
        (materialForm.plannedConcentration && materialForm.actualConcentration && materialForm.plannedConcentration !== materialForm.actualConcentration)
        || (materialForm.plannedMixRatio && materialForm.actualMixRatio && materialForm.plannedMixRatio !== materialForm.actualMixRatio)
    )
}
const saveMaterial = () => {
    compareMaterial()
    if (!materialForm.materialName || materialForm.actualAmount === null || !materialForm.amountUnit) return ElMessage.warning('请填写材料名称、实际用量和单位')
    if (materialForm.deviationFlag && !materialForm.deviationReason) return ElMessage.warning('实际参数与计划不同，请填写偏差原因')
    if (materialForm.temporaryMaterial && !materialForm.temporaryReason) return ElMessage.warning('临时材料必须填写添加原因')
    if (editingId.value) {
        const index = props.step.materials.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.materials[index] = { ...props.step.materials[index], ...materialForm }
    } else props.step.materials.push({ ...materialForm, id: Date.now() })
    materialDialog.value = false; markDirty()
}
const deleteMaterial = row => {
    props.step.materials = props.step.materials.filter(item => item.id !== row.id); markDirty()
}

const toolDialog = ref(false)
const toolForm = reactive({ toolName: '', model: '', serialNumber: '', purpose: '', plannedParameter: '', actualParameter: '', targetPart: '', operatorNote: '', safetyNote: '' })
const openTool = (row = null) => {
    editingId.value = row?.id || null
    Object.assign(toolForm, row || { toolName: '', model: '', serialNumber: '', purpose: '', plannedParameter: '', actualParameter: '', targetPart: '', operatorNote: '', safetyNote: '' })
    toolDialog.value = true
}
const saveTool = () => {
    if (!toolForm.toolName || !toolForm.actualParameter) return ElMessage.warning('请填写工具名称和实际参数')
    if (editingId.value) {
        const index = props.step.tools.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.tools[index] = { ...props.step.tools[index], ...toolForm }
    } else props.step.tools.push({ ...toolForm, id: Date.now() })
    toolDialog.value = false; markDirty()
}

const parameterDialog = ref(false)
const parameterForm = reactive({ parameterName: '', plannedValue: '', actualValue: '', unit: '', description: '' })
const openParameter = (row = null) => {
    editingId.value = row?.id || null
    Object.assign(parameterForm, row || { parameterName: '', plannedValue: '', actualValue: '', unit: '', description: '' })
    parameterDialog.value = true
}
const saveParameter = () => {
    if (!parameterForm.parameterName || !parameterForm.actualValue) return ElMessage.warning('请填写参数名称和实际值')
    const data = { ...parameterForm, deviationFlag: Boolean(parameterForm.plannedValue && parameterForm.plannedValue !== parameterForm.actualValue) }
    if (editingId.value) {
        const index = props.step.processParameters.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.processParameters[index] = { ...props.step.processParameters[index], ...data }
    } else props.step.processParameters.push({ ...data, id: Date.now() })
    parameterDialog.value = false; markDirty()
}

const environmentDialog = ref(false)
const environmentForm = reactive({ recordTime: '', temperature: null, humidity: null, illumination: null, ventilationStatus: '', environmentStatus: 'qualified', recorder: '', description: '' })
const openEnvironment = (row = null) => {
    editingId.value = row?.id || null
    Object.assign(environmentForm, row || { recordTime: new Date().toLocaleString('zh-CN', { hour12: false }), temperature: null, humidity: null, illumination: null, ventilationStatus: '正常', environmentStatus: 'qualified', recorder: props.step.operatorName, description: '' })
    environmentDialog.value = true
}
const saveEnvironment = () => {
    if (!environmentForm.recordTime || environmentForm.temperature === null || environmentForm.humidity === null) return ElMessage.warning('请填写时间、温度和湿度')
    if (editingId.value) {
        const index = props.step.environments.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.environments[index] = { ...props.step.environments[index], ...environmentForm }
    } else props.step.environments.push({ ...environmentForm, id: Date.now() })
    if (environmentForm.environmentStatus === 'unqualified') {
        props.step.issues.push({
            id: Date.now() + 1, issueType: 'environment', severity: 'high', issueTitle: '操作环境不符合方案要求',
            issueDescription: environmentForm.description || '环境记录被标记为不符合要求',
            discoveredTime: environmentForm.recordTime, discoveredBy: environmentForm.recorder,
            immediateAction: '建议暂停操作并调整环境条件', solution: '', issueStatus: 'open',
            requiresPlanChange: false, requiresNewDiseaseRecord: false, requiresRework: false
        })
        ElMessage.warning('已生成环境异常，请评估是否暂停操作')
    }
    environmentDialog.value = false; markDirty()
}

const mediaDialog = ref(false)
const mediaFile = ref(null)
const mediaUploading = ref(false)
const mediaForm = reactive({
    mediaStage: 'before', title: '', shootingTime: '', shootingPosition: '',
    targetPart: '', photographer: '', description: '', selectedForComparison: false,
    selectedForArchive: false, selectedForRestoration: false
})
const openMedia = (stage = 'before', row = null) => {
    editingId.value = row?.id || null
    mediaFile.value = null
    Object.assign(mediaForm, row || {
        mediaStage: stage, title: '', shootingTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        shootingPosition: '', targetPart: props.step.targetPart, photographer: props.step.operatorName,
        description: '', selectedForComparison: false, selectedForArchive: true, selectedForRestoration: false
    })
    mediaDialog.value = true
}
const selectMediaFile = uploadFile => {
    mediaFile.value = uploadFile.raw
}
const saveMedia = async () => {
    if (!mediaForm.title || !mediaForm.photographer) return ElMessage.warning('请填写标题和摄影人')
    if (editingId.value) {
        const index = props.step.media.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.media[index] = { ...props.step.media[index], ...mediaForm }
        mediaDialog.value = false
        markDirty()
        return
    }
    if (!mediaFile.value) return ElMessage.warning('请选择需要上传的图片或视频')
    mediaUploading.value = true
    try {
        const form = new FormData()
        form.append('file', mediaFile.value)
        Object.entries(mediaForm).forEach(([key, value]) => form.append(key, value ?? ''))
        const result = await uploadProcessMedia(props.step.id, form)
        props.step.media.push(result.data)
        mediaDialog.value = false
        ElMessage.success('过程影像已上传到MySQL')
        emit('recalculate')
    } finally {
        mediaUploading.value = false
    }
}
const removeMedia = async item => {
    await ElMessageBox.confirm(`确认删除影像“${item.fileName}”吗？`, '删除影像', { type: 'warning' })
    await deleteProcessMedia(item.id)
    props.step.media = props.step.media.filter(row => row.id !== item.id)
    emit('recalculate')
    ElMessage.success('影像已删除')
}
const previewMedia = async item => {
    const response = await getProcessMediaContent(item.id)
    const url = URL.createObjectURL(response.data)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60000)
}
const groupedMedia = computed(() => Object.keys(mediaStageMap).reduce((result, stage) => {
    result[stage] = props.step.media.filter(item => item.mediaStage === stage)
    return result
}, {}))

const issueDialog = ref(false)
const issueForm = reactive({
    issueType: 'process', severity: 'medium', issueTitle: '', issueDescription: '',
    discoveredTime: '', discoveredBy: '', immediateAction: '', solution: '', issueStatus: 'open',
    requiresPlanChange: false, requiresNewDiseaseRecord: false, requiresRework: false
})
const openIssue = (row = null) => {
    editingId.value = row?.id || null
    Object.assign(issueForm, row || {
        issueType: 'process', severity: 'medium', issueTitle: '', issueDescription: '',
        discoveredTime: new Date().toLocaleString('zh-CN', { hour12: false }), discoveredBy: props.step.operatorName,
        immediateAction: '', solution: '', issueStatus: 'open', requiresPlanChange: false,
        requiresNewDiseaseRecord: false, requiresRework: false
    })
    issueDialog.value = true
}
const saveIssue = () => {
    if (!issueForm.issueTitle || !issueForm.issueDescription || !issueForm.discoveredBy) return ElMessage.warning('请填写异常标题、描述和发现人')
    if (editingId.value) {
        const index = props.step.issues.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.issues[index] = { ...props.step.issues[index], ...issueForm }
    } else props.step.issues.push({ ...issueForm, id: Date.now() })
    issueDialog.value = false; markDirty()
}
const resolveIssue = row => {
    if (!row.solution) return openIssue(row)
    row.issueStatus = 'resolved'; markDirty(); ElMessage.success('异常已标记为已解决')
}

const checkDialog = ref(false)
const checkForm = reactive({ checkType: 'process', checkItem: '', expectedStandard: '', actualResult: '', checkResult: 'passed', checker: '', checkTime: '', requiresRework: false, reworkDescription: '' })
const openCheck = (row = null) => {
    editingId.value = row?.id || null
    Object.assign(checkForm, row || {
        checkType: 'process', checkItem: '', expectedStandard: '', actualResult: '',
        checkResult: 'passed', checker: '', checkTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        requiresRework: false, reworkDescription: ''
    })
    checkDialog.value = true
}
const saveCheck = () => {
    checkForm.requiresRework = checkForm.checkResult === 'failed' || checkForm.requiresRework
    if (!checkForm.checkItem || !checkForm.actualResult || !checkForm.checker) return ElMessage.warning('请填写检查项、实际结果和检查人')
    if (checkForm.requiresRework && !checkForm.reworkDescription) return ElMessage.warning('需要返工时必须填写返工要求')
    if (editingId.value) {
        const index = props.step.qualityChecks.findIndex(item => item.id === editingId.value)
        if (index >= 0) props.step.qualityChecks[index] = { ...props.step.qualityChecks[index], ...checkForm }
    } else props.step.qualityChecks.push({ ...checkForm, id: Date.now() })
    if (checkForm.checkResult === 'failed') props.step.qualityStatus = 'failed'
    checkDialog.value = false; markDirty()
}

const removeItem = (listName, row) => {
    ElMessageBox.confirm('确认删除这条记录？', '提示', { type: 'warning' })
        .then(() => { props.step[listName] = props.step[listName].filter(item => item.id !== row.id); markDirty() })
        .catch(() => {})
}

watch(() => props.step.id, () => {
    logDialog.value = materialDialog.value = toolDialog.value = parameterDialog.value = false
    environmentDialog.value = mediaDialog.value = issueDialog.value = checkDialog.value = false
})
</script>

<template>
    <article class="step-paper" :class="{ readonly: readOnly }">
        <header class="paper-header">
            <span>修复过程步骤记录</span>
            <h1>{{ step.stepName }}</h1>
            <p>{{ step.stepCode }} · {{ step.stepType }} · {{ statusMap[step.stepStatus]?.[0] }}</p>
            <el-alert v-if="readOnly" title="该步骤已完成，当前为只读模式。如需修改，请创建返工或修订记录。" type="info" :closable="false" show-icon />
        </header>

        <!-- 01 基本信息 -->
        <section id="process-section-basic">
            <div class="section-heading"><div><span>01</span><h3>步骤基本信息</h3><p>计划值只读引用正式方案，实际执行信息独立记录。</p></div><el-tag :type="statusMap[step.stepStatus]?.[1]">{{ statusMap[step.stepStatus]?.[0] }}</el-tag></div>
            <div class="plan-reference">
                <div><label>关联方案措施</label><p>{{ step.plannedMethod || '-' }}</p></div>
                <div><label>计划材料</label><p>{{ step.planMaterials?.map(item => item.materialName).join('、') || '无需或待配置' }}</p></div>
                <div><label>目标部位</label><p>{{ step.targetPart || '-' }}</p></div>
                <div><label>关联病害</label><p>{{ step.relatedDiseases?.map(item => item.diseaseName).join('、') || '非病害处理步骤' }}</p></div>
                <div><label>计划时间</label><p>{{ step.plannedStartTime || '-' }} 至 {{ step.plannedEndTime || '-' }}</p></div>
                <div><label>方案环境/安全要求</label><p>{{ formalPlan.environmentRequirements }}；{{ formalPlan.safetyRequirements }}</p></div>
            </div>
            <el-form label-position="top" class="form-grid" :disabled="readOnly" @input="markDirty" @change="markDirty">
                <el-form-item label="实际操作人员"><el-input v-model="step.operatorName" /></el-form-item>
                <el-form-item label="协助人员"><el-input v-model="step.assistantNames" /></el-form-item>
                <el-form-item label="操作地点"><el-input v-model="step.operationLocation" /></el-form-item>
                <el-form-item label="当前状态"><el-select v-model="step.stepStatus" disabled><el-option v-for="(item,key) in statusMap" :key="key" :label="item[0]" :value="key" /></el-select></el-form-item>
                <el-form-item label="实际开始时间"><el-date-picker v-model="step.actualStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" disabled /></el-form-item>
                <el-form-item label="实际结束时间"><el-date-picker v-model="step.actualEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" disabled /></el-form-item>
                <el-form-item label="实际方法" class="span-2"><el-input v-model="step.actualMethod" type="textarea" :rows="3" @input="actualMethodChanged" /></el-form-item>
                <el-form-item label="操作描述" class="span-2"><el-input v-model="step.operationDescription" type="textarea" :rows="2" /></el-form-item>
                <template v-if="step.deviationFlag">
                    <el-form-item label="偏差等级"><el-select v-model="step.deviationLevel"><el-option label="轻微偏差" value="minor" /><el-option label="一般偏差" value="general" /><el-option label="重大偏差" value="major" /></el-select></el-form-item>
                    <el-form-item label="偏差原因"><el-input v-model="step.deviationReason" /></el-form-item>
                    <el-form-item label="调整说明" class="span-2"><el-input v-model="step.adjustmentDescription" type="textarea" :rows="2" /></el-form-item>
                </template>
            </el-form>
        </section>

        <!-- 02 操作日志 -->
        <section id="process-section-logs">
            <div class="section-heading"><div><span>02</span><h3>实际操作日志</h3><p>按时间记录每次操作、观察、参数变化和当前结果。</p></div><el-button v-if="!readOnly" type="primary" :icon="Plus" @click="openLog()">新增日志</el-button></div>
            <el-timeline v-if="step.operationLogs.length">
                <el-timeline-item v-for="item in step.operationLogs" :key="item.id" :timestamp="item.recordTime" type="primary">
                    <div class="log-entry">
                        <div><b>{{ item.operationAction }}</b><span>{{ item.operatorName }}</span></div>
                        <p v-if="item.observation">现场观察：{{ item.observation }}</p>
                        <p v-if="item.parameterNote">参数变化：{{ item.parameterNote }}</p>
                        <p v-if="item.resultNote">当前结果：{{ item.resultNote }}</p>
                        <div v-if="!readOnly && !item.locked"><el-button link type="primary" @click="openLog(item)">编辑</el-button><el-button link type="danger" @click="deleteLog(item)">删除</el-button></div>
                    </div>
                </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="尚无实际操作日志" :image-size="60" />
        </section>

        <!-- 03 实际材料 -->
        <section id="process-section-materials">
            <div class="section-heading"><div><span>03</span><h3>实际材料</h3><p>计划参数与实际参数分别保存，差异会自动进入执行辅助栏。</p></div><div><el-checkbox v-model="step.noMaterialRequired" :disabled="readOnly" @change="markDirty">本步骤无需材料</el-checkbox><el-button v-if="!readOnly && !step.noMaterialRequired" type="primary" :icon="Plus" @click="openMaterial()">新增实际材料</el-button></div></div>
            <div v-if="!step.noMaterialRequired && step.planMaterials?.length" class="plan-materials">
                <span>从方案材料引用：</span>
                <el-button v-for="item in step.planMaterials" :key="item.id" link type="primary" @click="openMaterial(null, item)">{{ item.materialName }}</el-button>
            </div>
            <div class="table-scroll">
                <el-table v-if="!step.noMaterialRequired" :data="step.materials" border>
                    <el-table-column prop="materialName" label="材料" min-width="150" />
                    <el-table-column label="计划参数" min-width="150"><template #default="{row}">浓度 {{ row.plannedConcentration || '-' }}<br>配比 {{ row.plannedMixRatio || '-' }}</template></el-table-column>
                    <el-table-column label="实际参数" min-width="150"><template #default="{row}">浓度 {{ row.actualConcentration || '-' }}<br>配比 {{ row.actualMixRatio || '-' }}</template></el-table-column>
                    <el-table-column label="实际用量" width="105"><template #default="{row}">{{ row.actualAmount }} {{ row.amountUnit }}</template></el-table-column>
                    <el-table-column label="偏差" width="90"><template #default="{row}"><el-tag :type="row.deviationFlag ? 'warning' : 'success'" size="small">{{ row.deviationFlag ? '有偏差' : '无偏差' }}</el-tag></template></el-table-column>
                    <el-table-column prop="resultNote" label="结果说明" min-width="150" show-overflow-tooltip />
                    <el-table-column v-if="!readOnly" label="操作" width="95" fixed="right"><template #default="{row}"><el-button link :icon="Edit" @click="openMaterial(row)" /><el-button link type="danger" :icon="Delete" @click="deleteMaterial(row)" /></template></el-table-column>
                </el-table>
            </div>
        </section>

        <!-- 04 工具与工艺 -->
        <section id="process-section-tools">
            <div class="section-heading"><div><span>04</span><h3>工具与工艺参数</h3><p>计划参数与实际参数逐项对照，不隐藏在长文本中。</p></div><div><el-button v-if="!readOnly" :icon="Plus" @click="openTool()">添加工具</el-button><el-button v-if="!readOnly" type="primary" :icon="Plus" @click="openParameter()">添加参数</el-button></div></div>
            <h4 class="sub-title">工具设备</h4>
            <div class="table-scroll"><el-table :data="step.tools" border>
                <el-table-column prop="toolName" label="工具名称" width="125" />
                <el-table-column prop="model" label="型号" width="110" />
                <el-table-column prop="serialNumber" label="编号" width="110" />
                <el-table-column prop="purpose" label="用途" min-width="130" />
                <el-table-column prop="plannedParameter" label="计划参数" min-width="130" />
                <el-table-column prop="actualParameter" label="实际参数" min-width="130" />
                <el-table-column prop="safetyNote" label="安全说明" min-width="160" />
                <el-table-column v-if="!readOnly" label="操作" width="95" fixed="right"><template #default="{row}"><el-button link :icon="Edit" @click="openTool(row)" /><el-button link type="danger" :icon="Delete" @click="removeItem('tools',row)" /></template></el-table-column>
            </el-table></div>
            <h4 class="sub-title">可扩展工艺参数</h4>
            <el-table :data="step.processParameters" border>
                <el-table-column prop="parameterName" label="参数名称" />
                <el-table-column prop="plannedValue" label="计划值" />
                <el-table-column prop="actualValue" label="实际值" />
                <el-table-column prop="unit" label="单位" width="80" />
                <el-table-column label="偏差" width="90"><template #default="{row}"><el-tag :type="row.plannedValue && row.plannedValue !== row.actualValue ? 'warning' : 'success'" size="small">{{ row.plannedValue && row.plannedValue !== row.actualValue ? '有偏差' : '无偏差' }}</el-tag></template></el-table-column>
                <el-table-column prop="description" label="说明" min-width="160" />
                <el-table-column v-if="!readOnly" label="操作" width="95"><template #default="{row}"><el-button link :icon="Edit" @click="openParameter(row)" /><el-button link type="danger" :icon="Delete" @click="removeItem('processParameters',row)" /></template></el-table-column>
            </el-table>
        </section>

        <!-- 05 环境 -->
        <section id="process-section-environment">
            <div class="section-heading"><div><span>05</span><h3>环境记录</h3><p>不符合要求的记录会自动生成环境异常建议。</p></div><el-button v-if="!readOnly" type="primary" :icon="Plus" @click="openEnvironment()">新增环境记录</el-button></div>
            <el-table :data="step.environments" border>
                <el-table-column prop="recordTime" label="记录时间" width="165" />
                <el-table-column label="温度" width="80"><template #default="{row}">{{ row.temperature }}℃</template></el-table-column>
                <el-table-column label="湿度" width="80"><template #default="{row}">{{ row.humidity }}%</template></el-table-column>
                <el-table-column label="光照" width="85"><template #default="{row}">{{ row.illumination || '-' }} lx</template></el-table-column>
                <el-table-column prop="ventilationStatus" label="通风" width="90" />
                <el-table-column label="环境状态" width="105"><template #default="{row}"><el-tag :type="environmentMap[row.environmentStatus]?.[1]" size="small">{{ environmentMap[row.environmentStatus]?.[0] }}</el-tag></template></el-table-column>
                <el-table-column prop="recorder" label="记录人" width="90" />
                <el-table-column prop="description" label="说明" min-width="160" />
                <el-table-column v-if="!readOnly" label="操作" width="95" fixed="right"><template #default="{row}"><el-button link :icon="Edit" @click="openEnvironment(row)" /><el-button link type="danger" :icon="Delete" @click="removeItem('environments',row)" /></template></el-table-column>
            </el-table>
        </section>

        <!-- 06 影像 -->
        <section id="process-section-media">
            <div class="section-heading"><div><span>06</span><h3>操作前、中、后影像</h3><p>影像可复用于正式档案、前后对比和复原成果。</p></div><el-button v-if="!readOnly" type="primary" :icon="Upload" @click="openMedia()">上传影像</el-button></div>
            <el-tabs>
                <el-tab-pane v-for="(label,stage) in mediaStageMap" :key="stage" :label="`${label} ${groupedMedia[stage]?.length || 0}`">
                    <div class="media-grid">
                        <div v-for="item in groupedMedia[stage]" :key="item.id" class="media-item">
                            <div class="media-thumb">{{ label }}</div>
                            <div class="media-info"><b>{{ item.title }}</b><span>{{ item.fileName }}</span><small>{{ item.targetPart }} · {{ item.photographer }}</small></div>
                            <div class="media-flags">
                                <el-tag v-if="item.selectedForComparison" type="primary" size="small">前后对比</el-tag>
                                <el-tag v-if="item.selectedForArchive" type="success" size="small">正式档案</el-tag>
                                <el-tag v-if="item.selectedForRestoration" type="warning" size="small">复原成果</el-tag>
                            </div>
                            <div class="media-actions">
                                <el-button link type="primary" @click="previewMedia(item)">预览</el-button>
                                <el-button v-if="!readOnly" link @click="openMedia(stage,item)">编辑</el-button>
                                <el-button v-if="!readOnly" link type="danger" @click="removeMedia(item)">删除</el-button>
                            </div>
                        </div>
                    </div>
                    <el-empty v-if="!groupedMedia[stage]?.length" :description="`暂无${label}影像`" :image-size="55"><el-button v-if="!readOnly" type="primary" plain @click="openMedia(stage)">添加{{ label }}影像</el-button></el-empty>
                </el-tab-pane>
            </el-tabs>
        </section>

        <!-- 07 异常 -->
        <section id="process-section-issues">
            <div class="section-heading">
                <div><span>07</span><h3>问题、异常和方案调整</h3><p>方案调整仅作信息记录，可直接修改正式方案，无需申请或审批。</p></div>
                <div class="heading-actions">
                    <el-button type="primary" plain @click="$emit('navigate','plan-edit')">直接修改正式方案</el-button>
                    <el-button v-if="!readOnly" type="primary" :icon="Plus" @click="openIssue()">新增异常</el-button>
                </div>
            </div>
            <div v-for="item in step.issues" :key="item.id" class="issue-row">
                <div><el-tag :type="item.severity === 'high' || item.severity === 'critical' ? 'danger' : item.severity === 'medium' ? 'warning' : 'info'" size="small">{{ item.severity }}</el-tag><b>{{ item.issueTitle }}</b><el-tag size="small" effect="plain">{{ item.issueStatus }}</el-tag></div>
                <p>{{ item.issueDescription }}</p>
                <dl><span>发现：{{ item.discoveredTime }} · {{ item.discoveredBy }}</span><span>临时处理：{{ item.immediateAction || '-' }}</span><span>解决方案：{{ item.solution || '待处理' }}</span></dl>
                <div class="issue-flags"><el-tag v-if="item.requiresPlanChange" type="warning">已登记方案调整</el-tag><el-tag v-if="item.requiresNewDiseaseRecord" type="warning">新发现病害</el-tag><el-tag v-if="item.requiresRework" type="danger">需要返工</el-tag></div>
                <div class="row-actions">
                    <el-button v-if="item.requiresPlanChange" link type="primary" @click="$emit('navigate','plan-edit')">修改正式方案</el-button>
                    <el-button v-if="item.requiresNewDiseaseRecord" link type="warning" @click="$emit('navigate','new-disease')">前往新增病害</el-button>
                    <template v-if="!readOnly"><el-button link type="primary" @click="openIssue(item)">编辑/处理</el-button><el-button v-if="!['resolved','closed'].includes(item.issueStatus)" link type="success" @click="resolveIssue(item)">标记解决</el-button></template>
                </div>
            </div>
            <el-empty v-if="!step.issues.length" description="当前步骤暂无异常" :image-size="55" />
        </section>

        <!-- 08 质检 -->
        <section id="process-section-checks">
            <div class="section-heading"><div><span>08</span><h3>质量检查</h3><p>关键步骤检查失败时不能直接完成，将进入返工状态。</p></div><el-button v-if="!readOnly" type="primary" :icon="Plus" @click="openCheck()">新增检查项</el-button></div>
            <el-table :data="step.qualityChecks" border>
                <el-table-column prop="checkType" label="类型" width="90" />
                <el-table-column prop="checkItem" label="检查项" min-width="180" />
                <el-table-column prop="expectedStandard" label="预期标准" min-width="180" />
                <el-table-column prop="actualResult" label="实际结果" min-width="180" />
                <el-table-column label="结论" width="105"><template #default="{row}"><el-tag :type="checkMap[row.checkResult]?.[1]" size="small">{{ checkMap[row.checkResult]?.[0] }}</el-tag></template></el-table-column>
                <el-table-column prop="checker" label="检查人" width="90" />
                <el-table-column prop="checkTime" label="检查时间" width="165" />
                <el-table-column v-if="!readOnly" label="操作" width="95" fixed="right"><template #default="{row}"><el-button link :icon="Edit" @click="openCheck(row)" /><el-button link type="danger" :icon="Delete" @click="removeItem('qualityChecks',row)" /></template></el-table-column>
            </el-table>
        </section>

        <!-- 09 结果 -->
        <section id="process-section-result">
            <div class="section-heading"><div><span>09</span><h3>步骤结果与结论</h3><p>更新病害处理状态，但不覆盖原始病害调查数据。</p></div></div>
            <el-form label-position="top" class="form-grid" :disabled="readOnly" @input="markDirty" @change="markDirty">
                <el-form-item label="阶段目标是否完成"><el-select v-model="step.result.targetCompleted"><el-option label="已完成" value="yes" /><el-option label="部分完成" value="partial" /><el-option label="未完成" value="no" /></el-select></el-form-item>
                <el-form-item label="是否达到预期"><el-select v-model="step.result.expectedReached"><el-option label="达到" value="yes" /><el-option label="部分达到" value="partial" /><el-option label="未达到" value="no" /></el-select></el-form-item>
                <el-form-item label="病害处理效果"><el-input v-model="step.result.diseaseTreatmentEffect" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="文物状态变化"><el-input v-model="step.result.artifactStateChange" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="实际结果" class="span-2"><el-input v-model="step.result.actualResult" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="是否存在副作用"><el-input v-model="step.result.sideEffects" /></el-form-item>
                <el-form-item label="是否允许进入下一步"><el-switch v-model="step.result.allowNextStep" /></el-form-item>
                <el-form-item label="遗留问题" class="span-2"><el-input v-model="step.result.remainingProblems" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="最终步骤结论" class="span-2"><el-input v-model="step.result.finalConclusion" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="需要后续监测"><el-switch v-model="step.result.monitoringRequired" /></el-form-item>
                <el-form-item label="监测对象"><el-input v-model="step.result.monitoringObject" /></el-form-item>
                <template v-if="step.result.monitoringRequired">
                    <el-form-item label="监测指标"><el-input v-model="step.result.monitoringIndicators" /></el-form-item>
                    <el-form-item label="建议周期"><el-input v-model="step.result.monitoringCycle" /></el-form-item>
                    <el-form-item label="预警条件"><el-input v-model="step.result.warningConditions" /></el-form-item>
                    <el-form-item label="监测建议"><el-input v-model="step.result.monitoringAdvice" /></el-form-item>
                </template>
            </el-form>
            <div class="disease-result">
                <h4>关联病害处理状态</h4>
                <div v-for="item in step.relatedDiseases" :key="item.id">
                    <span><b>{{ item.diseaseName }}</b><small>{{ item.partName }}</small></span>
                    <el-select v-model="item.treatmentStatus" :disabled="readOnly" @change="markDirty">
                        <el-option label="未处理" value="untreated" /><el-option label="处理中" value="in_treatment" />
                        <el-option label="已控制" value="controlled" /><el-option label="已改善" value="improved" />
                        <el-option label="未解决" value="unresolved" /><el-option label="需要持续监测" value="monitoring_required" />
                    </el-select>
                </div>
            </div>
        </section>

        <!-- Dialogs -->
        <el-dialog v-model="logDialog" :title="editingId ? '编辑操作日志' : '新增操作日志'" width="620px">
            <el-form label-position="top" class="form-grid">
                <el-form-item label="记录时间"><el-input v-model="logForm.recordTime" /></el-form-item><el-form-item label="操作人员"><el-input v-model="logForm.operatorName" /></el-form-item>
                <el-form-item label="操作动作" class="span-2"><el-input v-model="logForm.operationAction" /></el-form-item><el-form-item label="现场观察"><el-input v-model="logForm.observation" type="textarea" /></el-form-item>
                <el-form-item label="参数变化"><el-input v-model="logForm.parameterNote" type="textarea" /></el-form-item><el-form-item label="当前结果" class="span-2"><el-input v-model="logForm.resultNote" type="textarea" /></el-form-item>
            </el-form><template #footer><el-button @click="logDialog=false">取消</el-button><el-button type="primary" @click="saveLog">保存</el-button></template>
        </el-dialog>

        <el-dialog v-model="materialDialog" :title="editingId ? '编辑实际材料' : '新增实际材料'" width="760px">
            <el-form label-position="top" class="form-grid">
                <el-form-item label="材料名称"><el-input v-model="materialForm.materialName" /></el-form-item><el-form-item label="厂商"><el-input v-model="materialForm.manufacturer" /></el-form-item>
                <el-form-item label="批次"><el-input v-model="materialForm.batchNumber" /></el-form-item><el-form-item label="使用部位"><el-input v-model="materialForm.targetPart" /></el-form-item>
                <el-form-item label="计划浓度"><el-input v-model="materialForm.plannedConcentration" disabled /></el-form-item><el-form-item label="实际浓度"><el-input v-model="materialForm.actualConcentration" @input="compareMaterial" /></el-form-item>
                <el-form-item label="计划配比"><el-input v-model="materialForm.plannedMixRatio" disabled /></el-form-item><el-form-item label="实际配比"><el-input v-model="materialForm.actualMixRatio" @input="compareMaterial" /></el-form-item>
                <el-form-item label="实际用量"><el-input-number v-model="materialForm.actualAmount" :min="0" /></el-form-item><el-form-item label="单位"><el-input v-model="materialForm.amountUnit" /></el-form-item>
                <el-form-item label="施加方式"><el-input v-model="materialForm.applicationMethod" /></el-form-item><el-form-item label="施加次数"><el-input-number v-model="materialForm.applicationCount" :min="1" /></el-form-item>
                <el-form-item label="干燥时间"><el-input v-model="materialForm.dryingTime" /></el-form-item><el-form-item label="使用目的"><el-input v-model="materialForm.usagePurpose" /></el-form-item>
                <el-form-item v-if="materialForm.deviationFlag" label="偏差原因" class="span-2"><el-input v-model="materialForm.deviationReason" type="textarea" /></el-form-item>
                <el-form-item v-if="materialForm.temporaryMaterial" label="临时材料原因" class="span-2"><el-input v-model="materialForm.temporaryReason" type="textarea" /></el-form-item>
                <el-form-item label="安全说明"><el-input v-model="materialForm.safetyNote" type="textarea" /></el-form-item><el-form-item label="结果说明"><el-input v-model="materialForm.resultNote" type="textarea" /></el-form-item>
                <el-form-item v-if="materialForm.temporaryMaterial" label="纳入方案调整统计"><el-switch v-model="materialForm.requiresPlanChange" /></el-form-item>
            </el-form><template #footer><el-button @click="materialDialog=false">取消</el-button><el-button type="primary" @click="saveMaterial">保存</el-button></template>
        </el-dialog>

        <el-dialog v-model="toolDialog" title="工具设备" width="650px"><el-form label-position="top" class="form-grid">
            <el-form-item label="工具名称"><el-input v-model="toolForm.toolName" /></el-form-item><el-form-item label="型号"><el-input v-model="toolForm.model" /></el-form-item>
            <el-form-item label="编号"><el-input v-model="toolForm.serialNumber" /></el-form-item><el-form-item label="用途"><el-input v-model="toolForm.purpose" /></el-form-item>
            <el-form-item label="计划参数"><el-input v-model="toolForm.plannedParameter" /></el-form-item><el-form-item label="实际参数"><el-input v-model="toolForm.actualParameter" /></el-form-item>
            <el-form-item label="目标部位"><el-input v-model="toolForm.targetPart" /></el-form-item><el-form-item label="安全说明"><el-input v-model="toolForm.safetyNote" /></el-form-item>
            <el-form-item label="操作说明" class="span-2"><el-input v-model="toolForm.operatorNote" type="textarea" /></el-form-item>
        </el-form><template #footer><el-button @click="toolDialog=false">取消</el-button><el-button type="primary" @click="saveTool">保存</el-button></template></el-dialog>

        <el-dialog v-model="parameterDialog" title="工艺参数" width="560px"><el-form label-position="top" class="form-grid">
            <el-form-item label="参数名称"><el-input v-model="parameterForm.parameterName" /></el-form-item><el-form-item label="单位"><el-input v-model="parameterForm.unit" /></el-form-item>
            <el-form-item label="计划值"><el-input v-model="parameterForm.plannedValue" /></el-form-item><el-form-item label="实际值"><el-input v-model="parameterForm.actualValue" /></el-form-item>
            <el-form-item label="说明" class="span-2"><el-input v-model="parameterForm.description" type="textarea" /></el-form-item>
        </el-form><template #footer><el-button @click="parameterDialog=false">取消</el-button><el-button type="primary" @click="saveParameter">保存</el-button></template></el-dialog>

        <el-dialog v-model="environmentDialog" title="环境记录" width="650px"><el-form label-position="top" class="form-grid">
            <el-form-item label="记录时间"><el-input v-model="environmentForm.recordTime" /></el-form-item><el-form-item label="记录人"><el-input v-model="environmentForm.recorder" /></el-form-item>
            <el-form-item label="温度℃"><el-input-number v-model="environmentForm.temperature" /></el-form-item><el-form-item label="湿度%"><el-input-number v-model="environmentForm.humidity" /></el-form-item>
            <el-form-item label="光照lx"><el-input-number v-model="environmentForm.illumination" /></el-form-item><el-form-item label="通风状态"><el-input v-model="environmentForm.ventilationStatus" /></el-form-item>
            <el-form-item label="环境状态"><el-select v-model="environmentForm.environmentStatus"><el-option v-for="(item,key) in environmentMap" :key="key" :label="item[0]" :value="key" /></el-select></el-form-item>
            <el-form-item label="说明"><el-input v-model="environmentForm.description" /></el-form-item>
        </el-form><template #footer><el-button @click="environmentDialog=false">取消</el-button><el-button type="primary" @click="saveEnvironment">保存</el-button></template></el-dialog>

        <el-dialog v-model="mediaDialog" :title="editingId ? '编辑操作影像' : '上传操作影像'" width="680px"><el-form label-position="top" class="form-grid">
            <el-form-item v-if="!editingId" label="选择文件" class="span-2">
                <el-upload :auto-upload="false" :limit="1" accept="image/*,video/*" :on-change="selectMediaFile">
                    <el-button :icon="Upload">选择图片或视频</el-button>
                    <template #tip><div class="el-upload__tip">单个文件不超过50MB，文件内容将保存到MySQL。</div></template>
                </el-upload>
            </el-form-item>
            <el-form-item label="影像阶段"><el-select v-model="mediaForm.mediaStage"><el-option v-for="(label,key) in mediaStageMap" :key="key" :label="label" :value="key" /></el-select></el-form-item>
            <el-form-item label="标题"><el-input v-model="mediaForm.title" /></el-form-item><el-form-item label="拍摄时间"><el-input v-model="mediaForm.shootingTime" /></el-form-item>
            <el-form-item label="拍摄角度"><el-input v-model="mediaForm.shootingPosition" /></el-form-item><el-form-item label="拍摄部位"><el-input v-model="mediaForm.targetPart" /></el-form-item>
            <el-form-item label="摄影人"><el-input v-model="mediaForm.photographer" /></el-form-item><el-form-item label="图片说明"><el-input v-model="mediaForm.description" /></el-form-item>
            <el-form-item class="span-2"><el-checkbox v-model="mediaForm.selectedForComparison">加入修复前后对比</el-checkbox><el-checkbox v-model="mediaForm.selectedForArchive">收入正式档案</el-checkbox><el-checkbox v-model="mediaForm.selectedForRestoration">用于复原成果</el-checkbox></el-form-item>
        </el-form><template #footer><el-button @click="mediaDialog=false">取消</el-button><el-button type="primary" :loading="mediaUploading" @click="saveMedia">{{ editingId ? '保存修改' : '上传影像' }}</el-button></template></el-dialog>

        <el-dialog v-model="issueDialog" title="问题与异常" width="720px"><el-form label-position="top" class="form-grid">
            <el-form-item label="异常类型"><el-select v-model="issueForm.issueType"><el-option v-for="item in ['material','tool','environment','artifact_state','new_disease','process','safety','other']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
            <el-form-item label="严重程度"><el-select v-model="issueForm.severity"><el-option label="低" value="low" /><el-option label="中" value="medium" /><el-option label="高" value="high" /><el-option label="危急" value="critical" /></el-select></el-form-item>
            <el-form-item label="异常标题" class="span-2"><el-input v-model="issueForm.issueTitle" /></el-form-item><el-form-item label="异常描述" class="span-2"><el-input v-model="issueForm.issueDescription" type="textarea" /></el-form-item>
            <el-form-item label="发现时间"><el-input v-model="issueForm.discoveredTime" /></el-form-item><el-form-item label="发现人"><el-input v-model="issueForm.discoveredBy" /></el-form-item>
            <el-form-item label="临时处理"><el-input v-model="issueForm.immediateAction" type="textarea" /></el-form-item><el-form-item label="最终解决方案"><el-input v-model="issueForm.solution" type="textarea" /></el-form-item>
            <el-form-item label="当前状态"><el-select v-model="issueForm.issueStatus"><el-option label="待处理" value="open" /><el-option label="处理中" value="processing" /><el-option label="已解决" value="resolved" /><el-option label="已关闭" value="closed" /></el-select></el-form-item>
            <el-form-item><el-checkbox v-model="issueForm.requiresPlanChange">纳入方案调整统计</el-checkbox><el-checkbox v-model="issueForm.requiresNewDiseaseRecord">发现新病害</el-checkbox><el-checkbox v-model="issueForm.requiresRework">需要返工</el-checkbox></el-form-item>
        </el-form><template #footer><el-button @click="issueDialog=false">取消</el-button><el-button type="primary" @click="saveIssue">保存</el-button></template></el-dialog>

        <el-dialog v-model="checkDialog" title="质量检查" width="680px"><el-form label-position="top" class="form-grid">
            <el-form-item label="检查类型"><el-input v-model="checkForm.checkType" /></el-form-item><el-form-item label="检查项"><el-input v-model="checkForm.checkItem" /></el-form-item>
            <el-form-item label="预期标准"><el-input v-model="checkForm.expectedStandard" type="textarea" /></el-form-item><el-form-item label="实际结果"><el-input v-model="checkForm.actualResult" type="textarea" /></el-form-item>
            <el-form-item label="检查结论"><el-select v-model="checkForm.checkResult"><el-option v-for="(item,key) in checkMap" :key="key" :label="item[0]" :value="key" /></el-select></el-form-item>
            <el-form-item label="检查人"><el-input v-model="checkForm.checker" /></el-form-item><el-form-item label="检查时间"><el-input v-model="checkForm.checkTime" /></el-form-item>
            <el-form-item label="是否需要返工"><el-switch v-model="checkForm.requiresRework" /></el-form-item><el-form-item v-if="checkForm.requiresRework" label="返工要求" class="span-2"><el-input v-model="checkForm.reworkDescription" type="textarea" /></el-form-item>
        </el-form><template #footer><el-button @click="checkDialog=false">取消</el-button><el-button type="primary" @click="saveCheck">保存</el-button></template></el-dialog>
    </article>
</template>

<style scoped>
.step-paper { min-height: 100%; padding: 32px 42px 55px; color: #303133; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-shadow: 0 3px 16px rgba(29,33,41,.06); box-sizing: border-box; }
.paper-header { margin-bottom: 10px; padding-bottom: 20px; text-align: center; border-bottom: 2px solid #1d2129; }
.paper-header > span { color: #86909c; font-size: 11px; letter-spacing: 4px; }
.paper-header h1 { margin: 8px 0 6px; color: #1d2129; font-size: 24px; }
.paper-header p { margin: 0 0 12px; color: #a0a5ad; font-size: 11px; }
section { padding: 28px 0; border-bottom: 1px solid #ebeef5; scroll-margin-top: 18px; }
section:last-of-type { border-bottom: 0; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.section-heading > div:first-child { min-width: 0; }
.section-heading > div:first-child > span { float: left; margin-right: 8px; color: #1668c4; font-size: 11px; font-weight: 700; line-height: 23px; }
.section-heading h3 { margin: 0 0 4px; color: #1d2129; font-size: 16px; }
.section-heading p { margin: 0; color: #a0a5ad; font-size: 10px; line-height: 1.5; }
.section-heading > div:last-child { display: flex; align-items: center; gap: 8px; }
.plan-reference { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 0 22px; margin-bottom: 20px; padding: 14px 16px; background: #f7f8fa; border-left: 3px solid #9bc2e9; }
.plan-reference div { padding: 8px 0; border-bottom: 1px dashed #dfe3e8; }
.plan-reference label { color: #a0a5ad; font-size: 10px; }
.plan-reference p { margin: 5px 0 0; color: #4e5969; font-size: 11px; line-height: 1.6; }
.form-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 0 18px; }
.span-2 { grid-column: 1/-1; }
.form-grid :deep(.el-select), .form-grid :deep(.el-date-editor), .form-grid :deep(.el-input-number) { width: 100%; }
.log-entry { padding: 2px 0 5px; }
.log-entry > div:first-child { display: flex; align-items: center; gap: 10px; }
.log-entry b { font-size: 12px; }
.log-entry span { color: #86909c; font-size: 10px; }
.log-entry p { margin: 5px 0; color: #606266; font-size: 10px; line-height: 1.5; }
.plan-materials { margin-bottom: 10px; padding: 8px 10px; color: #86909c; background: #f7f8fa; border-radius: 5px; font-size: 10px; }
.table-scroll { overflow-x: auto; }
.sub-title { margin: 20px 0 10px; color: #4e5969; font-size: 12px; }
.media-grid { display: grid; grid-template-columns: repeat(auto-fill,minmax(190px,1fr)); gap: 12px; }
.media-item { position: relative; border: 1px solid #e5e6eb; border-radius: 7px; overflow: hidden; }
.media-thumb { height: 115px; display: flex; align-items: center; justify-content: center; color: #86909c; background: linear-gradient(145deg,#e7edf1,#d7e0e5); font-size: 12px; }
.media-info { display: flex; flex-direction: column; gap: 3px; padding: 9px; }
.media-info b { font-size: 11px; }
.media-info span,.media-info small { color: #86909c; font-size: 9px; }
.media-flags,.media-actions { display: flex; flex-wrap: wrap; gap: 4px; padding: 0 9px 8px; }
.issue-row { margin-bottom: 10px; padding: 13px; border: 1px solid #e5e6eb; border-left: 3px solid #e6a23c; border-radius: 6px; }
.issue-row > div:first-child { display: flex; align-items: center; gap: 8px; }
.issue-row b { font-size: 12px; }
.issue-row p { margin: 9px 0; color: #606266; font-size: 11px; line-height: 1.6; }
.issue-row dl { display: grid; grid-template-columns: repeat(3,1fr); gap: 8px; margin: 0; color: #86909c; font-size: 9px; }
.issue-flags,.row-actions { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 8px; }
.disease-result { margin-top: 18px; padding: 14px; background: #f7f8fa; border-radius: 6px; }
.disease-result h4 { margin: 0 0 10px; font-size: 12px; }
.disease-result > div { display: grid; grid-template-columns: 1fr 180px; gap: 10px; align-items: center; padding: 7px 0; border-bottom: 1px dashed #dfe3e8; }
.disease-result span { display: flex; flex-direction: column; gap: 3px; }
.disease-result b { font-size: 11px; }
.disease-result small { color: #86909c; font-size: 9px; }
@media (max-width: 1050px) { .step-paper { padding: 28px; } }
@media (max-width: 720px) {
    .step-paper { padding: 22px 18px 45px; }
    .form-grid,.plan-reference { grid-template-columns: 1fr; }
    .span-2 { grid-column: auto; }
    .issue-row dl { grid-template-columns: 1fr; }
}
</style>
