<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Link, Plus, Search, Upload } from '@element-plus/icons-vue'
import {
    addArchiveAttachment,
    deleteArchiveAttachment,
    getArchiveAttachmentContent,
    getDetectionCandidates
} from '@/api/conservationArchive'

const props = defineProps({
    activeSection: { type: String, required: true },
    project: { type: Object, required: true },
    archive: { type: Object, required: true },
    sections: { type: Array, default: () => [] },
    workspace: { type: Object, required: true }
})

const emit = defineEmits(['dirty', 'navigate', 'refresh-survey'])

const activeMeta = computed(() => props.sections.find(item => item.code === props.activeSection) || {})
const markDirty = () => emit('dirty')

const severityMap = {
    minor: ['轻微', 'info'],
    moderate: ['中度', 'warning'],
    severe: ['严重', 'danger'],
    critical: ['危急', 'danger']
}
const riskMap = { low: '低风险', medium: '中风险', high: '高风险' }
const projectStatusMap = { draft: '草稿', active: '进行中', completed: '已完成', suspended: '暂停', archived: '已归档' }
const stageMap = {
    pendingSurvey: '待调查', disease: '病害调查', planning: '方案制定', protection: '保护处理中',
    repair: '修复处理中', restoration: '复原处理中', evaluation: '效果评估', monitoring: '后续监测'
}

const projectInfo = computed(() => [
    ['文物编号', props.project.artifactCode], ['文物名称', props.project.artifactName],
    ['文物类别', props.project.artifactCategory], ['材质', props.project.material],
    ['时代', props.project.era], ['出土地点', props.project.excavationLocation],
    ['所属墓葬', props.project.tombCode], ['遗迹单位', props.project.relicUnit],
    ['文物级别', props.project.artifactLevel], ['收藏单位', props.project.collectionUnit],
    ['提取日期', props.project.extractionDate]
])
const conservationProjectInfo = computed(() => [
    ['项目编号', props.project.projectCode], ['项目名称', props.project.projectName],
    ['项目类型', props.project.projectType === 'comprehensive' ? '综合保护修复' : props.project.projectType],
    ['项目负责人', props.project.principal], ['执行部门', props.project.department],
    ['开始日期', props.project.startDate], ['预计完成', props.project.expectedEndDate],
    ['当前阶段', stageMap[props.project.currentStage] || props.project.currentStage],
    ['项目状态', projectStatusMap[props.project.status] || props.project.status],
    ['风险等级', riskMap[props.project.riskLevel] || props.project.riskLevel]
])

const diseaseStats = computed(() => {
    const records = props.workspace.diseaseRecords
    return {
        total: records.length,
        minor: records.filter(item => item.severity === 'minor').length,
        moderate: records.filter(item => item.severity === 'moderate').length,
        severe: records.filter(item => item.severity === 'severe').length,
        critical: records.filter(item => item.severity === 'critical').length
    }
})

const selectedDisease = ref(props.workspace.diseaseRecords[0] || null)

const materialDialogVisible = ref(false)
const materialEditingId = ref(null)
const materialForm = reactive({
    materialId: null, materialName: '', materialType: '', concentration: '', mixRatio: '',
    applicationMethod: '', plannedAmount: '', targetPart: '', purpose: '', safetyNote: '',
    compatibilityNote: '', precautions: ''
})
const resetMaterialForm = () => Object.assign(materialForm, {
    materialId: null, materialName: '', materialType: '', concentration: '', mixRatio: '',
    applicationMethod: '', plannedAmount: '', targetPart: '', purpose: '', safetyNote: '',
    compatibilityNote: '', precautions: ''
})
const openMaterialDialog = (row = null) => {
    resetMaterialForm()
    materialEditingId.value = row?.id || null
    if (row) Object.assign(materialForm, row)
    materialDialogVisible.value = true
}
const syncMaterialDictionary = (materialId) => {
    const item = props.workspace.materialDictionary.find(row => row.id === materialId)
    if (!item) return
    Object.assign(materialForm, {
        materialId: item.id,
        materialName: item.materialName,
        materialType: item.materialType,
        safetyNote: materialForm.safetyNote || item.safetyNote,
        compatibilityNote: materialForm.compatibilityNote || item.compatibilityNote
    })
}
const saveMaterial = () => {
    if (!materialForm.materialName) {
        ElMessage.warning('请选择或填写材料名称')
        return
    }
    if (materialEditingId.value) {
        const index = props.workspace.planMaterials.findIndex(item => item.id === materialEditingId.value)
        if (index >= 0) props.workspace.planMaterials[index] = { ...props.workspace.planMaterials[index], ...materialForm }
    } else {
        props.workspace.planMaterials.push({ ...materialForm, id: Date.now() })
    }
    materialDialogVisible.value = false
    markDirty()
}
const removeMaterial = (row) => {
    ElMessageBox.confirm(`确认移除计划材料“${row.materialName}”？`, '提示', { type: 'warning' })
        .then(() => {
            props.workspace.planMaterials = props.workspace.planMaterials.filter(item => item.id !== row.id)
            markDirty()
        })
        .catch(() => {})
}

const diseasePlanDialogVisible = ref(false)
const diseasePlanEditingId = ref(null)
const diseasePlanForm = reactive({
    diseaseRecordIds: [], treatmentStrategy: '', priorityLevel: 'medium',
    expectedResult: '', riskDescription: '', precautions: ''
})
const openDiseasePlanDialog = (row = null) => {
    Object.assign(diseasePlanForm, {
        diseaseRecordIds: row?.diseaseRecordIds ? [...row.diseaseRecordIds] : row ? [row.diseaseRecordId] : [],
        treatmentStrategy: row?.treatmentStrategy || '',
        priorityLevel: row?.priorityLevel || 'medium',
        expectedResult: row?.expectedResult || '',
        riskDescription: row?.riskDescription || '',
        precautions: row?.precautions || ''
    })
    diseasePlanEditingId.value = row?.id || null
    diseasePlanDialogVisible.value = true
}
const saveDiseasePlan = () => {
    if (!diseasePlanForm.diseaseRecordIds.length || !diseasePlanForm.treatmentStrategy) {
        ElMessage.warning('请选择目标病害并填写处理措施')
        return
    }
    const selected = props.workspace.diseaseRecords.filter(item => diseasePlanForm.diseaseRecordIds.includes(item.id))
    const data = {
        ...diseasePlanForm,
        diseaseRecordIds: [...diseasePlanForm.diseaseRecordIds],
        diseaseRecordId: selected[0]?.id,
        diseaseName: selected.map(item => item.diseaseName).join('、'),
        severity: selected.some(item => item.severity === 'critical') ? 'critical' : selected[0]?.severity
    }
    if (diseasePlanEditingId.value) {
        const index = props.workspace.planDiseaseList.findIndex(item => item.id === diseasePlanEditingId.value)
        if (index >= 0) props.workspace.planDiseaseList[index] = { ...props.workspace.planDiseaseList[index], ...data }
    } else {
        props.workspace.planDiseaseList.push({ id: Date.now(), ...data })
    }
    diseasePlanDialogVisible.value = false
    markDirty()
}
const removeDiseasePlan = (row) => {
    props.workspace.planDiseaseList = props.workspace.planDiseaseList.filter(item => item.id !== row.id)
    markDirty()
}

const toolDialogVisible = ref(false)
const toolForm = reactive({ toolName: '', model: '', purpose: '', parameter: '', targetPart: '', safetyNote: '' })
const addTool = () => {
    if (!toolForm.toolName) return ElMessage.warning('请填写工具名称')
    props.workspace.tools.push({ ...toolForm, id: Date.now() })
    Object.assign(toolForm, { toolName: '', model: '', purpose: '', parameter: '', targetPart: '', safetyNote: '' })
    toolDialogVisible.value = false
    markDirty()
}

const attachmentDialogVisible = ref(false)
const attachmentFile = ref(null)
const attachmentUploading = ref(false)
const attachmentForm = reactive({
    fileType: '报告', sourceModule: '档案自身附件', sectionName: '',
    version: 'V1.0', description: ''
})
const selectAttachmentFile = uploadFile => {
    attachmentFile.value = uploadFile.raw
}
const addAttachment = async () => {
    if (!attachmentFile.value) return ElMessage.warning('请选择需要上传的文件')
    attachmentUploading.value = true
    try {
        const form = new FormData()
        form.append('file', attachmentFile.value)
        Object.entries({
            ...attachmentForm,
            uploadedBy: props.archive.compiler || '当前用户'
        }).forEach(([key, value]) => form.append(key, value || ''))
        const result = await addArchiveAttachment(props.archive.id, form)
        props.workspace.attachments.unshift(result.data)
        attachmentFile.value = null
        Object.assign(attachmentForm, {
            fileType: '报告', sourceModule: '档案自身附件', sectionName: '',
            version: props.archive.currentVersion, description: ''
        })
        attachmentDialogVisible.value = false
        ElMessage.success('附件已上传到MySQL')
    } finally {
        attachmentUploading.value = false
    }
}
const attachmentFilters = reactive({ keyword: '', source: '', type: '' })
const formatFileSize = value => {
    const bytes = Number(value || 0)
    if (!bytes) return '-'
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}
const filteredAttachments = computed(() => props.workspace.attachments.filter(item => {
    const keyword = attachmentFilters.keyword.trim().toLowerCase()
    const matchKeyword = !keyword || [item.fileName, item.description].some(value => (value || '').toLowerCase().includes(keyword))
    return matchKeyword && (!attachmentFilters.source || item.sourceModule === attachmentFilters.source) && (!attachmentFilters.type || item.fileType === attachmentFilters.type)
}))
const deleteAttachment = async row => {
    await ElMessageBox.confirm(`确认删除附件“${row.fileName}”吗？`, '删除附件', { type: 'warning' })
    await deleteArchiveAttachment(row.id)
    props.workspace.attachments = props.workspace.attachments.filter(item => item.id !== row.id)
    ElMessage.success('附件已删除')
}
const openAttachment = async (row, download = false) => {
    const response = await getArchiveAttachmentContent(row.id)
    const url = URL.createObjectURL(response.data)
    if (download) {
        const link = document.createElement('a')
        link.href = url
        link.download = row.fileName
        link.click()
    } else {
        window.open(url, '_blank')
    }
    setTimeout(() => URL.revokeObjectURL(url), 60000)
}

const detectionDialogVisible = ref(false)
const detectionLoading = ref(false)
const detectionCandidates = ref([])
const selectedDetectionCandidates = ref([])
const detectionFilters = reactive({ keyword: '', status: '' })
const detectionLinkForm = reactive({ relatedDiseaseIds: [], purpose: '保护修复技术依据' })
const linkedDetectionIds = computed(() => new Set(
    props.workspace.detectionReferences.map(item =>
        Number(item.analysisResultId || item.sourceBusinessId || item.id)
    )
))
const filteredDetectionCandidates = computed(() => {
    const keyword = detectionFilters.keyword.trim().toLowerCase()
    return detectionCandidates.value.filter(item => {
        const matchesKeyword = !keyword || [
            item.experimentName, item.method, item.instrumentModel, item.purpose,
            item.conclusion, item.resultStatus, item.samplePosition
        ].some(value => String(value || '').toLowerCase().includes(keyword))
        const matchesStatus = !detectionFilters.status
            || (detectionFilters.status === 'has_result'
                ? item.hasResult
                : item.resultStatus === detectionFilters.status)
        return matchesKeyword && matchesStatus
    })
})
const openDetectionDialog = async () => {
    const legacy = props.workspace.detectionReferences.filter(item =>
        item.experimentType === '待选择' || item.conclusion === '请在检测分析模块选择正式结果'
    )
    if (legacy.length) {
        props.workspace.detectionReferences = props.workspace.detectionReferences.filter(item => !legacy.includes(item))
        markDirty()
    }
    detectionDialogVisible.value = true
    detectionLoading.value = true
    selectedDetectionCandidates.value = []
    Object.assign(detectionFilters, { keyword: '', status: '' })
    Object.assign(detectionLinkForm, { relatedDiseaseIds: [], purpose: '保护修复技术依据' })
    try {
        const result = await getDetectionCandidates(props.project.id)
        detectionCandidates.value = result.data || []
    } catch (error) {
        ElMessage.error(error.message || '检测结果加载失败')
    } finally {
        detectionLoading.value = false
    }
}
const detectionSelectable = row => !linkedDetectionIds.value.has(Number(row.analysisResultId))
const onDetectionSelection = rows => {
    selectedDetectionCandidates.value = rows.filter(detectionSelectable)
}
const confirmDetectionReferences = () => {
    if (!selectedDetectionCandidates.value.length) return ElMessage.warning('请至少选择一项检测结果')
    const relatedDisease = props.workspace.diseaseRecords
        .filter(item => detectionLinkForm.relatedDiseaseIds.includes(item.id))
        .map(item => item.diseaseName)
        .join('、')
    const existing = linkedDetectionIds.value
    const additions = selectedDetectionCandidates.value
        .filter(item => !existing.has(Number(item.analysisResultId)))
        .map(item => ({
            ...item,
            id: item.analysisResultId,
            referenceId: item.analysisResultId,
            sourceBusinessType: 'analysis_result',
            sourceBusinessId: item.analysisResultId,
            relatedDisease,
            purpose: detectionLinkForm.purpose || item.purpose || '保护修复技术依据'
        }))
    props.workspace.detectionReferences.push(...additions)
    detectionDialogVisible.value = false
    markDirty()
    ElMessage.success(`已关联 ${additions.length} 项真实检测结果`)
}
const removeDetectionReference = row => {
    ElMessageBox.confirm(`确认解除“${row.experimentName}”的档案关联？`, '解除检测结果', { type: 'warning' })
        .then(() => {
            const targetId = Number(row.analysisResultId || row.sourceBusinessId || row.id)
            props.workspace.detectionReferences = props.workspace.detectionReferences.filter(item =>
                Number(item.analysisResultId || item.sourceBusinessId || item.id) !== targetId
            )
            markDirty()
        })
        .catch(() => {})
}

const generateMonitoringAdvice = () => {
    const severeNames = props.workspace.diseaseRecords
        .filter(item => ['severe', 'critical'].includes(item.severity))
        .map(item => item.diseaseName)
    props.workspace.advice.monitorDiseases = [...new Set(severeNames)].join('、')
    props.workspace.advice.reviewCycle ||= '修复后第1个月、3个月、6个月复查，此后每年一次'
    props.workspace.advice.monitoringIndicators ||= '裂隙宽度、结构位移、表面粉化程度、环境温湿度'
    props.workspace.advice.followUpAdvice ||= '建立定期巡查记录，发现病害扩展或环境异常时及时启动专项评估。'
    markDirty()
    ElMessage.success('已根据严重病害生成监测建议预览')
}
</script>

<template>
    <article class="archive-paper">
        <header class="paper-header">
            <span>文物保护修复档案</span>
            <h1>{{ activeMeta.name }}</h1>
            <p>{{ archive.archiveCode }} · {{ project.projectCode }}</p>
        </header>

        <!-- 01 文物与项目信息 -->
        <section v-if="activeSection === 'project'" class="paper-section">
            <div class="section-heading">
                <div><h3>文物信息</h3><p>只读引用文物信息主数据，不在档案内重复保存。</p></div>
                <el-tag type="info" effect="plain">只读引用</el-tag>
            </div>
            <div class="info-layout">
                <div class="artifact-photo">
                    <div class="photo-placeholder">M45-C1<br><small>文物整体照片</small></div>
                </div>
                <dl class="info-grid">
                    <div v-for="item in projectInfo" :key="item[0]"><dt>{{ item[0] }}</dt><dd>{{ item[1] || '-' }}</dd></div>
                </dl>
            </div>
            <el-divider />
            <div class="section-heading"><div><h3>保护修复项目</h3><p>项目状态与档案状态相互独立。</p></div></div>
            <dl class="info-grid project-grid">
                <div v-for="item in conservationProjectInfo" :key="item[0]"><dt>{{ item[0] }}</dt><dd>{{ item[1] || '-' }}</dd></div>
            </dl>
            <div class="read-block"><label>项目摘要</label><p>{{ project.summary || '暂无项目摘要。' }}</p></div>
            <div class="section-actions">
                <el-button @click="$emit('navigate', 'artifact')">查看文物详情</el-button>
                <el-button @click="$emit('navigate', 'overview')">返回保护修复总览</el-button>
            </div>
        </section>

        <!-- 02 保存现状 -->
        <section v-else-if="activeSection === 'condition'" class="paper-section">
            <el-alert v-if="workspace.surveyReferenceUpdated" title="病害调查引用数据已更新，请刷新档案内容。" type="warning" show-icon :closable="false">
                <template #default><el-button link type="primary" @click="$emit('refresh-survey')">刷新引用数据</el-button></template>
            </el-alert>
            <div class="section-heading">
                <div><h3>最新有效病害调查</h3><p>仅引用已经保存并完成提交的调查数据。</p></div>
                <el-tag type="success">已保存提交</el-tag>
            </div>
            <dl class="info-grid">
                <div><dt>调查编号</dt><dd>{{ workspace.survey.surveyCode }}</dd></div>
                <div><dt>调查日期</dt><dd>{{ workspace.survey.surveyDate }}</dd></div>
                <div><dt>调查人员</dt><dd>{{ workspace.survey.surveyor }}</dd></div>
                <div><dt>调查地点</dt><dd>{{ workspace.survey.surveyLocation }}</dd></div>
                <div><dt>整体保存状态</dt><dd>{{ workspace.survey.preservationStatus }}</dd></div>
                <div><dt>结构稳定性</dt><dd>{{ workspace.survey.structuralStability }}</dd></div>
                <div><dt>综合风险</dt><dd><el-tag type="danger">{{ riskMap[workspace.survey.overallRisk] }}</el-tag></dd></div>
                <div><dt>引用版本</dt><dd>{{ archive.sourceSurveyVersion }}</dd></div>
            </dl>
            <div class="read-block"><label>保存环境摘要</label><p>{{ workspace.survey.environmentSummary }}</p></div>
            <div class="read-block"><label>调查总结</label><p>{{ workspace.survey.summary }}</p></div>
            <div class="section-actions">
                <el-button type="primary" plain @click="$emit('navigate', 'disease')">查看完整病害调查</el-button>
                <el-button @click="$emit('navigate', 'disease')">编辑病害调查</el-button>
                <el-button @click="$emit('refresh-survey')">刷新引用数据</el-button>
            </div>
        </section>

        <!-- 03 病害调查 -->
        <section v-else-if="activeSection === 'disease'" class="paper-section">
            <div class="stat-strip">
                <div><b>{{ diseaseStats.total }}</b><span>病害总数</span></div>
                <div><b>{{ diseaseStats.minor }}</b><span>轻微</span></div>
                <div><b class="warning">{{ diseaseStats.moderate }}</b><span>中度</span></div>
                <div><b class="danger">{{ diseaseStats.severe }}</b><span>严重</span></div>
                <div><b class="danger">{{ diseaseStats.critical }}</b><span>危急</span></div>
            </div>
            <div class="disease-split">
                <div class="disease-list">
                    <button v-for="item in workspace.diseaseRecords" :key="item.id" :class="{ active: selectedDisease?.id === item.id }" @click="selectedDisease = item">
                        <span><b>{{ item.diseaseName }}</b><small>{{ item.partName }} · {{ item.side }}</small></span>
                        <el-tag :type="severityMap[item.severity]?.[1]" size="small">{{ severityMap[item.severity]?.[0] }}</el-tag>
                    </button>
                </div>
                <div v-if="selectedDisease" class="disease-detail">
                    <div class="section-heading"><div><h3>{{ selectedDisease.diseaseName }}</h3><p>{{ selectedDisease.positionDescription }}</p></div><el-tag v-if="selectedDisease.emergency" type="danger">需优先处置</el-tag></div>
                    <dl class="detail-list">
                        <div><dt>发展状态</dt><dd>{{ selectedDisease.developmentStatus }}</dd></div>
                        <div><dt>影响范围</dt><dd>{{ selectedDisease.extentValue }}{{ selectedDisease.extentUnit }}</dd></div>
                        <div><dt>形态特征</dt><dd>{{ selectedDisease.morphology }}</dd></div>
                        <div><dt>结构影响</dt><dd>{{ selectedDisease.structuralImpact }}</dd></div>
                        <div><dt>成因分析</dt><dd>{{ selectedDisease.causeAnalysis }}</dd></div>
                        <div><dt>初步建议</dt><dd>{{ selectedDisease.recommendedAction }}</dd></div>
                    </dl>
                    <div class="media-placeholder">病害影像 {{ selectedDisease.mediaCount || 0 }} 张（只读引用）</div>
                </div>
            </div>
            <div class="section-actions">
                <el-button type="primary" plain @click="$emit('navigate', 'disease')">查看完整调查</el-button>
                <el-button @click="$emit('navigate', 'disease')">编辑病害调查</el-button>
            </div>
        </section>

        <!-- 04 检测分析依据 -->
        <section v-else-if="activeSection === 'detection'" class="paper-section">
            <div class="section-heading">
                <div><h3>已关联检测结果</h3><p>档案仅保存检测结果ID和摘要，不复制实验原始数据。</p></div>
                <el-button type="primary" :icon="Link" @click="openDetectionDialog">从检测分析选择</el-button>
            </div>
            <el-table v-if="workspace.detectionReferences.length" :data="workspace.detectionReferences" border>
                <el-table-column prop="experimentName" label="实验名称" min-width="150" />
                <el-table-column prop="experimentType" label="类型" width="100" />
                <el-table-column prop="method" label="检测方法" width="120" />
                <el-table-column prop="instrumentModel" label="仪器型号" min-width="130" />
                <el-table-column prop="detectionDate" label="检测日期" width="110" />
                <el-table-column label="关联病害" min-width="130">
                    <template #default="{ row }"><el-input v-model="row.relatedDisease" size="small" placeholder="填写关联病害" @input="markDirty" /></template>
                </el-table-column>
                <el-table-column label="关联用途" min-width="150">
                    <template #default="{ row }"><el-input v-model="row.purpose" size="small" placeholder="填写关联用途" @input="markDirty" /></template>
                </el-table-column>
                <el-table-column label="操作" width="125" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="$emit('navigate', 'detection-detail', row.analysisResultId || row.sourceBusinessId || row.id)">查看</el-button>
                        <el-button link type="danger" @click="removeDetectionReference(row)">解除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-empty v-else description="尚未关联检测分析模块的真实结果" :image-size="70">
                <el-button type="primary" :icon="Link" @click="openDetectionDialog">选择检测结果</el-button>
            </el-empty>
            <div v-for="item in workspace.detectionReferences" :key="`conclusion-${item.id}`" class="reference-summary">
                <b>{{ item.experimentName }}</b>
                <span>{{ item.conclusion }}</span>
                <a v-if="item.reportName">{{ item.reportName }}</a>
            </div>
        </section>

        <!-- 05 保护原则与目标 -->
        <section v-else-if="activeSection === 'principle'" class="paper-section editable-section" @input="markDirty" @change="markDirty">
            <div class="section-heading"><div><h3>档案基本管理信息</h3><p>记录档案标题、编制人和内容摘要，数据保存后可直接导出。</p></div><el-tag type="primary">可编辑</el-tag></div>
            <el-form label-position="top" class="form-grid">
                <el-form-item label="档案标题" class="span-2"><el-input v-model="archive.archiveTitle" /></el-form-item>
                <el-form-item label="编制人"><el-input v-model="archive.compiler" /></el-form-item>
                <el-form-item label="执行摘要" class="span-2"><el-input v-model="archive.executiveSummary" type="textarea" :rows="3" /></el-form-item>
            </el-form>
            <el-divider />
            <div class="section-heading"><div><h3>保护修复原则</h3><p>可多选，并为本项目补充具体执行说明。</p></div><el-tag type="primary">可编辑</el-tag></div>
            <el-checkbox-group v-model="workspace.principles.selected" class="principle-options">
                <el-checkbox v-for="item in workspace.principles.options" :key="item" :value="item" border>{{ item }}</el-checkbox>
            </el-checkbox-group>
            <el-form label-position="top" class="form-grid">
                <el-form-item v-for="item in workspace.principles.selected" :key="item" :label="`${item} · 项目说明`">
                    <el-input v-model="workspace.principles.notes[item]" :placeholder="`说明${item}在本项目中的落实方式`" />
                </el-form-item>
                <el-form-item label="自定义原则">
                    <el-input v-model="workspace.principles.custom" placeholder="填写其他适用原则" />
                </el-form-item>
            </el-form>
            <el-divider />
            <div class="section-heading"><div><h3>保护修复目标</h3><p>目标应可验证，并与病害、措施和预期效果对应。</p></div></div>
            <el-form label-position="top" class="form-grid">
                <el-form-item label="总体保护目标" class="span-2"><el-input v-model="workspace.goals.overall" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="病害控制目标"><el-input v-model="workspace.goals.diseaseControl" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="结构稳定目标"><el-input v-model="workspace.goals.structuralStability" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="外观协调目标"><el-input v-model="workspace.goals.appearance" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="原始信息保留目标"><el-input v-model="workspace.goals.informationRetention" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="展示利用目标"><el-input v-model="workspace.goals.displayUse" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="长期保存目标"><el-input v-model="workspace.goals.longTerm" type="textarea" :rows="2" /></el-form-item>
            </el-form>
        </section>

        <!-- 06 保护修复方案 -->
        <section v-else-if="activeSection === 'plan'" class="paper-section editable-section" @input="markDirty" @change="markDirty">
            <div class="section-heading"><div><h3>正式保护修复方案</h3><p>方案可自主维护，修改后直接保存，无需提交申请或等待审批。</p></div><el-tag type="primary">可直接修改</el-tag></div>
            <el-alert
                title="方案变更采用直接登记模式"
                description="数据库仅保存当前方案及修复过程中的调整信息。修改不会覆盖已经完成的过程记录；如需调整未执行步骤，可在修复过程页面继续编辑。"
                type="info"
                :closable="false"
                show-icon
                class="plan-edit-alert"
            />
            <el-form label-position="top" class="form-grid">
                <el-form-item label="方案编号"><el-input v-model="workspace.plan.planCode" /></el-form-item>
                <el-form-item label="方案名称"><el-input v-model="workspace.plan.planName" /></el-form-item>
                <el-form-item label="方案状态"><el-select v-model="workspace.plan.planStatus"><el-option label="草稿" value="draft" /><el-option label="已完成" value="completed" /></el-select></el-form-item>
                <el-form-item label="编制人"><el-input v-model="workspace.plan.compiler" /></el-form-item>
                <el-form-item label="编制日期"><el-date-picker v-model="workspace.plan.compiledDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
                <el-form-item label="方案目标" class="span-2"><el-input v-model="workspace.plan.planGoal" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="技术依据" class="span-2"><el-input v-model="workspace.plan.technicalBasis" type="textarea" :rows="2" /></el-form-item>
            </el-form>
            <div class="subsection-title">
                <div><h4>病害—措施对应关系</h4><p>严重或危急病害必须至少关联一项正式措施。</p></div>
                <el-button type="primary" :icon="Plus" @click="openDiseasePlanDialog()">添加措施</el-button>
            </div>
            <el-table :data="workspace.planDiseaseList" border>
                <el-table-column prop="diseaseName" label="目标病害" width="120" />
                <el-table-column label="严重程度" width="95">
                    <template #default="{ row }"><el-tag :type="severityMap[row.severity]?.[1]" size="small">{{ severityMap[row.severity]?.[0] }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="treatmentStrategy" label="处理措施" min-width="210" />
                <el-table-column prop="priorityLevel" label="优先级" width="90" />
                <el-table-column prop="expectedResult" label="预期效果" min-width="200" />
                <el-table-column label="操作" width="110" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" :icon="Edit" @click="openDiseasePlanDialog(row)" />
                        <el-button link type="danger" :icon="Delete" @click="removeDiseasePlan(row)" />
                    </template>
                </el-table-column>
            </el-table>
            <el-form label-position="top" class="form-grid plan-detail">
                <el-form-item label="总体实施方法" class="span-2"><el-input v-model="workspace.plan.selectedMethod" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="环境要求"><el-input v-model="workspace.plan.environmentRequirements" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="安全要求"><el-input v-model="workspace.plan.safetyRequirements" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="可逆性说明"><el-input v-model="workspace.plan.reversibilityDescription" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="兼容性说明"><el-input v-model="workspace.plan.compatibilityDescription" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="预期结果"><el-input v-model="workspace.plan.expectedResult" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="方案风险"><el-input v-model="workspace.plan.riskAnalysis" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="应急措施" class="span-2"><el-input v-model="workspace.plan.emergencyMeasures" type="textarea" :rows="2" /></el-form-item>
            </el-form>
        </section>

        <!-- 07 材料、工具与工艺 -->
        <section v-else-if="activeSection === 'material'" class="paper-section editable-section" @input="markDirty" @change="markDirty">
            <el-tabs v-model="workspace.materialTab">
                <el-tab-pane label="计划材料" name="materials">
                    <div class="subsection-title">
                        <div><h4>方案计划材料</h4><p>材料来自材料字典，表内记录本方案的计划使用参数。</p></div>
                        <el-button type="primary" :icon="Plus" @click="openMaterialDialog()">添加材料</el-button>
                    </div>
                    <el-table :data="workspace.planMaterials" border>
                        <el-table-column prop="materialName" label="材料名称" width="130" />
                        <el-table-column prop="materialType" label="类型" width="110" />
                        <el-table-column prop="concentration" label="浓度" width="90" />
                        <el-table-column prop="mixRatio" label="配比" min-width="130" />
                        <el-table-column prop="targetPart" label="使用部位" min-width="140" />
                        <el-table-column prop="applicationMethod" label="施加方式" min-width="150" />
                        <el-table-column prop="plannedAmount" label="计划用量" min-width="140" />
                        <el-table-column prop="purpose" label="用途" min-width="130" />
                        <el-table-column label="操作" width="105" fixed="right">
                            <template #default="{ row }">
                                <el-button link type="primary" :icon="Edit" @click="openMaterialDialog(row)" />
                                <el-button link type="danger" :icon="Delete" @click="removeMaterial(row)" />
                            </template>
                        </el-table-column>
                    </el-table>
                    <div v-for="item in workspace.planMaterials" :key="`safe-${item.id}`" class="material-note">
                        <b>{{ item.materialName }}</b>
                        <span>安全：{{ item.safetyNote || '待补充' }}</span>
                        <span>兼容性：{{ item.compatibilityNote || '待补充' }}</span>
                        <span>注意：{{ item.precautions || '待补充' }}</span>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="工具设备" name="tools">
                    <div class="subsection-title"><div><h4>计划工具设备</h4><p>记录型号、用途、参数和安全要求。</p></div><el-button type="primary" :icon="Plus" @click="toolDialogVisible = true">添加工具</el-button></div>
                    <el-table :data="workspace.tools" border>
                        <el-table-column prop="toolName" label="工具名称" />
                        <el-table-column prop="model" label="型号" />
                        <el-table-column prop="purpose" label="用途" />
                        <el-table-column prop="parameter" label="参数" />
                        <el-table-column prop="targetPart" label="使用部位" />
                        <el-table-column prop="safetyNote" label="安全注意事项" min-width="180" />
                        <el-table-column label="操作" width="70">
                            <template #default="{ row }"><el-button link type="danger" @click="workspace.tools = workspace.tools.filter(item => item.id !== row.id); markDirty()">删除</el-button></template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>
                <el-tab-pane label="工艺参数" name="process">
                    <el-form label-position="top" class="form-grid">
                        <el-form-item label="操作方法"><el-input v-model="workspace.processParameters.operationMethod" type="textarea" :rows="2" /></el-form-item>
                        <el-form-item label="施加顺序"><el-input v-model="workspace.processParameters.applicationOrder" type="textarea" :rows="2" /></el-form-item>
                        <el-form-item label="材料浓度"><el-input v-model="workspace.processParameters.materialConcentration" /></el-form-item>
                        <el-form-item label="干燥时间"><el-input v-model="workspace.processParameters.dryingTime" /></el-form-item>
                        <el-form-item label="操作次数"><el-input v-model="workspace.processParameters.operationTimes" /></el-form-item>
                        <el-form-item label="环境温度"><el-input v-model="workspace.processParameters.temperature" /></el-form-item>
                        <el-form-item label="环境湿度"><el-input v-model="workspace.processParameters.humidity" /></el-form-item>
                        <el-form-item label="参数限制"><el-input v-model="workspace.processParameters.parameterLimit" /></el-form-item>
                        <el-form-item label="质量控制点" class="span-2"><el-input v-model="workspace.processParameters.qualityControl" type="textarea" :rows="2" /></el-form-item>
                        <el-form-item label="应急处理要求" class="span-2"><el-input v-model="workspace.processParameters.emergencyRequirement" type="textarea" :rows="2" /></el-form-item>
                    </el-form>
                </el-tab-pane>
            </el-tabs>
        </section>

        <!-- 08 修复过程 -->
        <section v-else-if="activeSection === 'process'" class="paper-section">
            <div class="section-heading"><div><h3>修复过程摘要</h3><p>只读引用修复过程页面中的实际操作记录。</p></div><el-tag type="info" effect="plain">只读引用</el-tag></div>
            <div class="stat-strip">
                <div><b>{{ workspace.processSummary.planned }}</b><span>计划步骤</span></div>
                <div><b class="success">{{ workspace.processSummary.completed }}</b><span>已完成</span></div>
                <div><b class="warning">{{ workspace.processSummary.processing }}</b><span>进行中</span></div>
                <div><b>{{ workspace.processSummary.pending }}</b><span>待开始</span></div>
            </div>
            <el-timeline>
                <el-timeline-item v-for="item in workspace.processSummary.steps" :key="item.id" :timestamp="item.date || '待安排'" :type="item.status === 'completed' ? 'success' : item.status === 'processing' ? 'primary' : 'info'">
                    <div class="timeline-title"><b>{{ item.title }}</b><el-tag size="small" :type="item.status === 'completed' ? 'success' : item.status === 'processing' ? 'primary' : 'info'">{{ item.statusText }}</el-tag></div>
                    <p>{{ item.operator || '待安排人员' }} · {{ item.disease || '未关联病害' }} · {{ item.material || '未登记材料' }} · 影像 {{ item.imageCount || 0 }} 张</p>
                </el-timeline-item>
            </el-timeline>
            <el-empty v-if="!workspace.processSummary.steps.length" description="当前项目尚无修复过程记录。" />
            <div class="section-actions"><el-button type="primary" @click="$emit('navigate', 'process')">进入修复过程记录</el-button></div>
        </section>

        <!-- 09 前后对比 -->
        <section v-else-if="activeSection === 'comparison'" class="paper-section">
            <div class="section-heading"><div><h3>精选修复前后对比</h3><p>只读引用前后对比页面中的正式影像组。</p></div><el-tag type="info" effect="plain">只读引用</el-tag></div>
            <div v-if="workspace.comparisons.length" class="comparison-grid">
                <div v-for="item in workspace.comparisons" :key="item.id" class="comparison-item">
                    <div class="compare-images"><div>修复前</div><div>修复后</div></div>
                    <h4>{{ item.title }}</h4>
                    <p>{{ item.part }} · {{ item.disease }} · {{ item.step }}</p>
                    <span>{{ item.description }}</span>
                </div>
            </div>
            <el-empty v-else description="当前项目尚无修复前后对比记录。" />
            <div class="section-actions"><el-button type="primary" @click="$emit('navigate', 'comparison')">进入修复前后对比</el-button></div>
        </section>

        <!-- 10 复原成果 -->
        <section v-else-if="activeSection === 'restoration'" class="paper-section">
            <div class="section-heading"><div><h3>文物复原成果</h3><p>清晰区分原始保存、修复补配与推测复原内容。</p></div><el-tag type="info" effect="plain">只读引用</el-tag></div>
            <div v-if="workspace.restorationResults.length" class="result-list">
                <div v-for="item in workspace.restorationResults" :key="item.id" class="result-item">
                    <div class="result-thumb">成果影像</div>
                    <div>
                        <h4>{{ item.name }} <el-tag size="small">{{ item.type }}</el-tag></h4>
                        <p>复原范围：{{ item.scope }}</p>
                        <p>复原依据：{{ item.basis }}</p>
                        <div class="content-legend">
                            <span class="original">原始：{{ item.originalPart }}</span>
                            <span class="repair">补配：{{ item.repairedPart }}</span>
                            <span class="inferred">推测：{{ item.inferredPart }}</span>
                        </div>
                        <small>证据等级 {{ item.evidenceLevel }} · 可信度 {{ item.confidence }}%</small>
                    </div>
                </div>
            </div>
            <el-empty v-else description="当前项目尚无文物复原成果。" />
            <div class="section-actions"><el-button type="primary" @click="$emit('navigate', 'restoration')">进入文物复原成果</el-button></div>
        </section>

        <!-- 11 效果评估 -->
        <section v-else-if="activeSection === 'evaluation'" class="paper-section editable-section" @input="markDirty" @change="markDirty">
            <div class="section-heading"><div><h3>保护修复效果评估</h3><p>可填写阶段性评估，并关联复测结果。</p></div><el-tag type="primary">可编辑</el-tag></div>
            <el-form label-position="top" class="form-grid">
                <el-form-item label="病害控制情况"><el-input v-model="workspace.evaluation.diseaseControl" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="结构稳定性变化"><el-input v-model="workspace.evaluation.structuralChange" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="表面强度变化"><el-input v-model="workspace.evaluation.surfaceStrength" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="外观协调性"><el-input v-model="workspace.evaluation.appearanceCoordination" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="色度变化"><el-input v-model="workspace.evaluation.colorChange" /></el-form-item>
                <el-form-item label="光泽度变化"><el-input v-model="workspace.evaluation.glossChange" /></el-form-item>
                <el-form-item label="材料兼容性"><el-input v-model="workspace.evaluation.materialCompatibility" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="修复目标达成情况"><el-input v-model="workspace.evaluation.goalAchievement" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="是否存在副作用"><el-select v-model="workspace.evaluation.hasSideEffects"><el-option label="未发现" value="no" /><el-option label="存在" value="yes" /><el-option label="待观察" value="pending" /></el-select></el-form-item>
                <el-form-item label="评估人员"><el-input v-model="workspace.evaluation.evaluator" /></el-form-item>
                <el-form-item label="遗留问题" class="span-2"><el-input v-model="workspace.evaluation.remainingIssues" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="验收结论" class="span-2"><el-input v-model="workspace.evaluation.acceptanceConclusion" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="档案阶段性/最终结论" class="span-2"><el-input v-model="archive.finalConclusion" type="textarea" :rows="3" placeholder="填写可直接保存和导出的正式结论" /></el-form-item>
                <el-form-item label="评估日期"><el-date-picker v-model="workspace.evaluation.evaluationDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
                <el-form-item label="关联复测结果"><el-select v-model="workspace.evaluation.retestIds" multiple><el-option v-for="item in workspace.retestOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
            </el-form>
        </section>

        <!-- 12 保存建议 -->
        <section v-else-if="activeSection === 'advice'" class="paper-section editable-section" @input="markDirty" @change="markDirty">
            <div class="section-heading"><div><h3>保存环境与后续建议</h3><p>此处形成后续监测计划的业务依据。</p></div><el-button type="primary" plain @click="generateMonitoringAdvice">生成后续监测建议</el-button></div>
            <el-form label-position="top" class="form-grid">
                <el-form-item label="推荐温度范围"><el-input v-model="workspace.advice.temperatureRange" /></el-form-item>
                <el-form-item label="推荐湿度范围"><el-input v-model="workspace.advice.humidityRange" /></el-form-item>
                <el-form-item label="光照要求"><el-input v-model="workspace.advice.lighting" /></el-form-item>
                <el-form-item label="空气质量要求"><el-input v-model="workspace.advice.airQuality" /></el-form-item>
                <el-form-item label="包装要求"><el-input v-model="workspace.advice.packaging" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="展示要求"><el-input v-model="workspace.advice.display" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="搬运要求"><el-input v-model="workspace.advice.handling" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="防震要求"><el-input v-model="workspace.advice.shockproof" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="复查周期"><el-input v-model="workspace.advice.reviewCycle" /></el-form-item>
                <el-form-item label="重点监测病害"><el-input v-model="workspace.advice.monitorDiseases" /></el-form-item>
                <el-form-item label="监测指标" class="span-2"><el-input v-model="workspace.advice.monitoringIndicators" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="后续保护建议" class="span-2"><el-input v-model="workspace.advice.followUpAdvice" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="风险预警条件" class="span-2"><el-input v-model="workspace.advice.warningConditions" type="textarea" :rows="2" /></el-form-item>
            </el-form>
        </section>

        <!-- 13 附件 -->
        <section v-else-if="activeSection === 'attachment'" class="paper-section">
            <div class="section-heading"><div><h3>影像及附件档案</h3><p>汇总整个项目的文件资源，并保留来源关系。</p></div><el-button type="primary" :icon="Upload" @click="attachmentDialogVisible = true">上传附件</el-button></div>
            <div class="attachment-filter">
                <el-input v-model="attachmentFilters.keyword" placeholder="搜索文件名称或说明" clearable />
                <el-select v-model="attachmentFilters.source" placeholder="来源模块" clearable>
                    <el-option v-for="item in ['病害调查','检测分析','保护修复方案','修复过程','修复前后对比','复原成果','后续监测','档案自身附件']" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="attachmentFilters.type" placeholder="文件类型" clearable>
                    <el-option v-for="item in ['报告','照片','视频','图纸','表格','材料说明','三维模型','其他']" :key="item" :label="item" :value="item" />
                </el-select>
            </div>
            <el-table :data="filteredAttachments" border>
                <el-table-column label="文件名称" min-width="190"><template #default="{ row }"><el-button link type="primary">{{ row.fileName }}</el-button></template></el-table-column>
                <el-table-column prop="fileType" label="类型" width="90" />
                <el-table-column label="大小" width="90"><template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template></el-table-column>
                <el-table-column prop="sourceModule" label="来源模块" width="120" />
                <el-table-column prop="sectionName" label="所属章节" min-width="120" />
                <el-table-column prop="uploadedBy" label="上传人" width="90" />
                <el-table-column prop="uploadTime" label="上传时间" width="160" />
                <el-table-column prop="version" label="版本" width="75" />
                <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
                <el-table-column label="操作" width="145" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="openAttachment(row)">预览</el-button>
                        <el-button link type="primary" @click="openAttachment(row, true)">下载</el-button>
                        <el-button link type="danger" @click="deleteAttachment(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </section>

        <!-- 真实检测结果选择Dialog -->
        <el-dialog
            v-model="detectionDialogVisible"
            title="从检测分析模块选择真实结果"
            width="min(1050px, 96vw)"
            destroy-on-close
        >
            <div class="detection-picker">
                <el-alert
                    :title="`当前文物：${project.artifactCode || '未填写编号'} · ${project.artifactName || '未填写名称'}`"
                    description="列表按文物编号精确匹配检测分析数据；项目未填写文物编号时，才按文物名称匹配。"
                    type="info"
                    :closable="false"
                    show-icon
                />
                <div class="detection-toolbar">
                    <el-input
                        v-model="detectionFilters.keyword"
                        :prefix-icon="Search"
                        clearable
                        placeholder="搜索实验、方法、仪器、结论或取样部位"
                    />
                    <el-select v-model="detectionFilters.status" clearable placeholder="全部结果状态">
                        <el-option label="已有实验内容" value="has_result" />
                        <el-option label="已完成" value="已完成" />
                        <el-option label="检测中" value="检测中" />
                        <el-option label="待检测" value="待检测" />
                    </el-select>
                </div>

                <el-table
                    v-loading="detectionLoading"
                    :data="filteredDetectionCandidates"
                    row-key="analysisResultId"
                    border
                    max-height="390"
                    @selection-change="onDetectionSelection"
                >
                    <el-table-column type="selection" width="45" reserve-selection :selectable="detectionSelectable" />
                    <el-table-column prop="experimentName" label="实验/检测项目" min-width="145" show-overflow-tooltip />
                    <el-table-column prop="method" label="检测方法" min-width="125" show-overflow-tooltip />
                    <el-table-column prop="instrumentModel" label="仪器型号" min-width="120" show-overflow-tooltip />
                    <el-table-column label="状态" width="92">
                        <template #default="{ row }">
                            <el-tag :type="row.resultStatus === '已完成' ? 'success' : row.hasResult ? 'warning' : 'info'" size="small">
                                {{ row.resultStatus || (row.hasResult ? '已有内容' : '未录结果') }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="detectionDate" label="检测日期" width="110" />
                    <el-table-column prop="conclusion" label="检测结论" min-width="190" show-overflow-tooltip />
                    <el-table-column label="关联情况" width="90">
                        <template #default="{ row }">
                            <el-tag v-if="!detectionSelectable(row)" type="info" size="small">已关联</el-tag>
                            <span v-else>可选择</span>
                        </template>
                    </el-table-column>
                </el-table>

                <el-empty
                    v-if="!detectionLoading && !filteredDetectionCandidates.length"
                    description="未找到当前文物的检测分析结果"
                    :image-size="80"
                >
                    <el-button type="primary" plain @click="$emit('navigate', 'detection-overview')">前往检测分析模块录入</el-button>
                </el-empty>

                <el-form label-position="top" class="detection-link-form">
                    <el-form-item label="关联病害（可选）">
                        <el-select
                            v-model="detectionLinkForm.relatedDiseaseIds"
                            multiple
                            collapse-tags
                            collapse-tags-tooltip
                            placeholder="选择该结果支持的病害记录"
                        >
                            <el-option
                                v-for="item in workspace.diseaseRecords"
                                :key="item.id"
                                :label="`${item.diseaseName} · ${item.partName || '未填写部位'}`"
                                :value="item.id"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="档案引用用途">
                        <el-input v-model="detectionLinkForm.purpose" placeholder="例如：保护修复技术依据" />
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <el-button @click="detectionDialogVisible = false">取消</el-button>
                <el-button
                    type="primary"
                    :disabled="!selectedDetectionCandidates.length"
                    @click="confirmDetectionReferences"
                >
                    关联所选结果（{{ selectedDetectionCandidates.length }}）
                </el-button>
            </template>
        </el-dialog>

        <!-- 材料Dialog -->
        <el-dialog v-model="materialDialogVisible" :title="materialEditingId ? '编辑计划材料' : '添加计划材料'" width="720px" destroy-on-close>
            <el-form label-position="top" class="form-grid">
                <el-form-item label="材料字典">
                    <el-select v-model="materialForm.materialId" filterable allow-create placeholder="选择材料" @change="syncMaterialDictionary">
                        <el-option v-for="item in workspace.materialDictionary" :key="item.id" :label="`${item.materialName} · ${item.materialType}`" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="材料名称"><el-input v-model="materialForm.materialName" /></el-form-item>
                <el-form-item label="材料类型"><el-input v-model="materialForm.materialType" /></el-form-item>
                <el-form-item label="浓度"><el-input v-model="materialForm.concentration" /></el-form-item>
                <el-form-item label="配比"><el-input v-model="materialForm.mixRatio" /></el-form-item>
                <el-form-item label="使用部位"><el-input v-model="materialForm.targetPart" /></el-form-item>
                <el-form-item label="施加方式"><el-input v-model="materialForm.applicationMethod" /></el-form-item>
                <el-form-item label="计划用量"><el-input v-model="materialForm.plannedAmount" /></el-form-item>
                <el-form-item label="用途" class="span-2"><el-input v-model="materialForm.purpose" /></el-form-item>
                <el-form-item label="安全说明"><el-input v-model="materialForm.safetyNote" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="兼容性说明"><el-input v-model="materialForm.compatibilityNote" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="注意事项" class="span-2"><el-input v-model="materialForm.precautions" type="textarea" :rows="2" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="materialDialogVisible = false">取消</el-button><el-button type="primary" @click="saveMaterial">保存</el-button></template>
        </el-dialog>

        <!-- 病害措施Dialog -->
        <el-dialog v-model="diseasePlanDialogVisible" :title="diseasePlanEditingId ? '编辑病害措施' : '关联病害与措施'" width="680px" destroy-on-close>
            <el-form label-position="top">
                <el-form-item label="目标病害">
                    <el-select v-model="diseasePlanForm.diseaseRecordIds" multiple style="width:100%">
                        <el-option v-for="item in workspace.diseaseRecords" :key="item.id" :label="`${item.diseaseName} · ${severityMap[item.severity]?.[0]} · ${item.partName}`" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="处理措施"><el-input v-model="diseasePlanForm.treatmentStrategy" type="textarea" :rows="3" /></el-form-item>
                <el-form-item label="优先级"><el-select v-model="diseasePlanForm.priorityLevel"><el-option label="高" value="high" /><el-option label="中" value="medium" /><el-option label="低" value="low" /></el-select></el-form-item>
                <el-form-item label="预期效果"><el-input v-model="diseasePlanForm.expectedResult" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="风险说明"><el-input v-model="diseasePlanForm.riskDescription" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="注意事项"><el-input v-model="diseasePlanForm.precautions" type="textarea" :rows="2" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="diseasePlanDialogVisible = false">取消</el-button><el-button type="primary" @click="saveDiseasePlan">保存关联</el-button></template>
        </el-dialog>

        <!-- 工具Dialog -->
        <el-dialog v-model="toolDialogVisible" title="添加工具设备" width="620px">
            <el-form label-position="top" class="form-grid">
                <el-form-item label="工具名称"><el-input v-model="toolForm.toolName" /></el-form-item>
                <el-form-item label="型号"><el-input v-model="toolForm.model" /></el-form-item>
                <el-form-item label="用途"><el-input v-model="toolForm.purpose" /></el-form-item>
                <el-form-item label="参数"><el-input v-model="toolForm.parameter" /></el-form-item>
                <el-form-item label="使用部位"><el-input v-model="toolForm.targetPart" /></el-form-item>
                <el-form-item label="安全注意事项"><el-input v-model="toolForm.safetyNote" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="toolDialogVisible = false">取消</el-button><el-button type="primary" @click="addTool">添加</el-button></template>
        </el-dialog>

        <!-- 附件Dialog -->
        <el-dialog v-model="attachmentDialogVisible" title="上传档案附件" width="620px">
            <el-form label-position="top" class="form-grid dialog-form">
                <el-form-item label="选择文件" class="span-2">
                    <el-upload :auto-upload="false" :limit="1" :on-change="selectAttachmentFile">
                        <el-button :icon="Upload">选择文件</el-button>
                        <template #tip><div class="el-upload__tip">单个文件不超过 50MB，文件内容将保存到 MySQL。</div></template>
                    </el-upload>
                </el-form-item>
                <el-form-item label="文件类型"><el-select v-model="attachmentForm.fileType"><el-option v-for="item in ['报告','照片','视频','图纸','表格','材料说明','三维模型','其他']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
                <el-form-item label="来源模块"><el-select v-model="attachmentForm.sourceModule"><el-option v-for="item in ['病害调查','检测分析','保护修复方案','修复过程','修复前后对比','复原成果','后续监测','档案自身附件']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
                <el-form-item label="所属章节"><el-input v-model="attachmentForm.sectionName" /></el-form-item>
                <el-form-item label="版本"><el-input v-model="attachmentForm.version" /></el-form-item>
                <el-form-item label="说明" class="span-2"><el-input v-model="attachmentForm.description" type="textarea" :rows="2" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="attachmentDialogVisible = false">取消</el-button><el-button type="primary" :loading="attachmentUploading" @click="addAttachment">上传附件</el-button></template>
        </el-dialog>
    </article>
</template>

<style scoped>
.archive-paper { min-height: 100%; padding: 32px 42px 55px; color: #303133; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-shadow: 0 3px 16px rgba(29, 33, 41, .06); box-sizing: border-box; }
.paper-header { margin-bottom: 28px; padding-bottom: 20px; text-align: center; border-bottom: 2px solid #1d2129; }
.paper-header > span { color: #86909c; font-size: 12px; letter-spacing: 4px; }
.paper-header h1 { margin: 8px 0 6px; color: #1d2129; font-family: "Noto Serif SC", "Songti SC", serif; font-size: 25px; }
.paper-header p { margin: 0; color: #a0a5ad; font-size: 11px; letter-spacing: 1px; }
.paper-section { min-width: 0; }
.section-heading, .subsection-title { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; margin-bottom: 18px; }
.section-heading h3, .subsection-title h4 { margin: 0 0 5px; color: #1d2129; font-size: 17px; }
.section-heading p, .subsection-title p { margin: 0; color: #a0a5ad; font-size: 11px; line-height: 1.6; }
.subsection-title { align-items: center; margin-top: 25px; padding-top: 18px; border-top: 1px solid #ebeef5; }
.subsection-title h4 { font-size: 14px; }
.info-layout { display: flex; gap: 28px; }
.artifact-photo { flex: none; width: 190px; }
.photo-placeholder { height: 180px; display: flex; align-items: center; justify-content: center; flex-direction: column; color: #637282; background: linear-gradient(145deg, #e9eef2, #cfd9df); border-radius: 8px; font-size: 24px; font-weight: 700; }
.photo-placeholder small { margin-top: 8px; font-size: 11px; font-weight: 400; }
.info-grid { min-width: 0; display: grid; flex: 1; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 25px; margin: 0; }
.info-grid div { display: grid; grid-template-columns: 90px minmax(0, 1fr); padding: 9px 0; border-bottom: 1px dashed #ebeef5; font-size: 12px; }
.info-grid dt { color: #a0a5ad; }
.info-grid dd { margin: 0; color: #303133; }
.project-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.read-block { margin-top: 17px; padding: 13px 15px; background: #f7f8fa; border-left: 3px solid #a7c9ee; }
.read-block label { color: #86909c; font-size: 11px; }
.read-block p { margin: 7px 0 0; color: #4e5969; font-size: 12px; line-height: 1.8; }
.section-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 22px; }
.stat-strip { display: grid; grid-template-columns: repeat(5, 1fr); margin-bottom: 22px; border: 1px solid #e5e6eb; border-radius: 8px; overflow: hidden; }
.stat-strip div { display: flex; align-items: center; flex-direction: column; gap: 3px; padding: 15px 6px; border-right: 1px solid #e5e6eb; }
.stat-strip div:last-child { border-right: 0; }
.stat-strip b { color: #1d2129; font-size: 21px; }
.stat-strip span { color: #86909c; font-size: 10px; }
.stat-strip .warning { color: #e6a23c; }
.stat-strip .danger { color: #f56c6c; }
.stat-strip .success { color: #67c23a; }
.disease-split { display: grid; grid-template-columns: 210px 1fr; min-height: 360px; border: 1px solid #e5e6eb; border-radius: 8px; overflow: hidden; }
.disease-list { padding: 8px; background: #f7f8fa; border-right: 1px solid #e5e6eb; }
.disease-list button { width: 100%; display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 11px 9px; text-align: left; background: transparent; border: 0; border-radius: 6px; cursor: pointer; }
.disease-list button.active { background: #fff; box-shadow: 0 1px 7px rgba(29,33,41,.08); }
.disease-list button > span { min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.disease-list b { font-size: 12px; }
.disease-list small { color: #a0a5ad; font-size: 10px; }
.disease-detail { padding: 20px; }
.detail-list { margin: 0; }
.detail-list div { display: grid; grid-template-columns: 85px 1fr; padding: 9px 0; border-bottom: 1px dashed #ebeef5; font-size: 12px; }
.detail-list dt { color: #a0a5ad; }
.detail-list dd { margin: 0; line-height: 1.6; }
.media-placeholder { margin-top: 15px; padding: 25px; color: #86909c; text-align: center; background: #f5f7fa; border: 1px dashed #c9cdd4; border-radius: 6px; font-size: 11px; }
.reference-summary, .material-note { display: grid; grid-template-columns: 150px 1fr auto; gap: 12px; padding: 11px 4px; border-bottom: 1px dashed #ebeef5; font-size: 11px; line-height: 1.6; }
.reference-summary a { color: #1668c4; cursor: pointer; }
.principle-options { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
.principle-options :deep(.el-checkbox) { margin: 0; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.span-2 { grid-column: 1 / -1; }
.form-grid :deep(.el-select), .form-grid :deep(.el-date-editor) { width: 100%; }
.plan-detail { margin-top: 22px; }
.plan-edit-alert { margin-bottom: 18px; }
.material-note { grid-template-columns: 100px repeat(3, 1fr); }
.timeline-title { display: flex; align-items: center; gap: 10px; }
.timeline-title + p { margin: 6px 0; color: #86909c; font-size: 11px; }
.comparison-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.comparison-item { padding-bottom: 14px; border: 1px solid #e5e6eb; border-radius: 8px; overflow: hidden; }
.compare-images { display: grid; grid-template-columns: 1fr 1fr; height: 150px; }
.compare-images div { display: flex; align-items: center; justify-content: center; color: #86909c; background: #e9eef2; font-size: 12px; }
.compare-images div:last-child { color: #52755a; background: #e6f2e8; }
.comparison-item h4, .comparison-item p, .comparison-item > span { margin: 10px 14px 0; }
.comparison-item p, .comparison-item > span { display: block; color: #86909c; font-size: 11px; }
.result-list { display: flex; flex-direction: column; gap: 14px; }
.result-item { display: grid; grid-template-columns: 150px 1fr; gap: 18px; padding: 14px; border: 1px solid #e5e6eb; border-radius: 8px; }
.result-thumb { min-height: 120px; display: flex; align-items: center; justify-content: center; color: #86909c; background: #eef1f4; border-radius: 5px; }
.result-item h4 { margin: 2px 0 10px; }
.result-item p { margin: 5px 0; color: #606266; font-size: 11px; }
.content-legend { display: flex; flex-wrap: wrap; gap: 6px; margin: 9px 0; }
.content-legend span { padding: 4px 7px; border-radius: 4px; font-size: 10px; }
.original { color: #236b3a; background: #e7f4eb; }
.repair { color: #8b6515; background: #fff4d8; }
.inferred { color: #7b4f91; background: #f3eaf7; }
.attachment-filter { display: grid; grid-template-columns: minmax(200px, 1fr) 150px 130px; gap: 10px; margin-bottom: 14px; }
.detection-toolbar { display: grid; grid-template-columns: minmax(0, 1fr) 180px; gap: 10px; margin: 14px 0; }
.detection-link-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; margin-top: 16px; padding: 14px 16px 0; background: #f7f8fa; border-radius: 7px; }
.detection-link-form :deep(.el-select) { width: 100%; }
.dialog-form { margin-top: 14px; }
@media (max-width: 1100px) {
    .archive-paper { padding: 28px; }
    .info-layout { flex-direction: column; }
    .artifact-photo { width: 100%; }
    .photo-placeholder { height: 150px; }
    .material-note { grid-template-columns: 1fr; }
}
@media (max-width: 760px) {
    .archive-paper { padding: 22px 18px 45px; }
    .paper-header h1 { font-size: 21px; }
    .info-grid, .project-grid, .form-grid, .comparison-grid { grid-template-columns: 1fr; }
    .span-2 { grid-column: auto; }
    .disease-split { grid-template-columns: 1fr; }
    .disease-list { border-right: 0; border-bottom: 1px solid #e5e6eb; }
    .stat-strip { grid-template-columns: repeat(3, 1fr); }
    .stat-strip div { border-bottom: 1px solid #e5e6eb; }
    .result-item { grid-template-columns: 1fr; }
    .attachment-filter, .detection-toolbar, .detection-link-form { grid-template-columns: 1fr; }
    .reference-summary { grid-template-columns: 1fr; }
}
</style>
