<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { searchConservationArtifacts } from '@/api/conservationProject'

const props = defineProps({ modelValue: Boolean, editData: Object })
const emit = defineEmits(['update:modelValue', 'save'])

const emptyForm = () => ({
    projectCode: `CR-${Date.now().toString(36).toUpperCase()}`,
    projectName: '',
    artifactId: null,
    artifactCode: '',
    artifactName: '',
    recordMode: 'standard',
    projectType: '综合',
    riskLevel: 'medium',
    principal: '',
    department: '',
    startDate: '',
    expectedEndDate: '',
    summary: ''
})
const form = ref(emptyForm())
const isEdit = ref(false)
let resolveTimer

const normalizeCode = value => String(value || '')
    .replaceAll('：', ':').replaceAll(' ', '').replaceAll('-', '').replaceAll('*', '')
const optionLabel = item => `${item.code || '无编号'} · ${item.name || '未命名文物'}${item.material ? `（${item.material}）` : ''}`
const selectArtifact = item => {
    if (!item) return
    form.value.artifactId = item.id
    form.value.artifactCode = item.code || ''
    form.value.artifactName = item.name || ''
}
const fetchArtifactSuggestions = async (query, callback) => {
    const keyword = String(query || '').trim()
    if (!keyword) return callback([])
    try {
        const response = await searchConservationArtifacts(keyword)
        callback((response.data || []).map(item => ({ ...item, value: optionLabel(item) })))
    } catch {
        callback([])
    }
}
const resolveTypedArtifact = async () => {
    const code = String(form.value.artifactCode || '').trim()
    const name = String(form.value.artifactName || '').trim()
    const keyword = code || name
    if (!keyword) return
    try {
        const response = await searchConservationArtifacts(keyword)
        const exact = (response.data || []).filter(item => {
            const codeMatched = code && normalizeCode(item.code) === normalizeCode(code)
            const nameMatched = name && String(item.name || '').trim() === name
            return code && name ? codeMatched && nameMatched : Boolean(codeMatched || nameMatched)
        })
        if (exact.length === 1) selectArtifact(exact[0])
    } catch {
        // 联想检索失败不影响将车、棺等非文物对象作为手工项目继续录入。
    }
}
const onArtifactInput = () => {
    form.value.artifactId = null
    clearTimeout(resolveTimer)
    resolveTimer = setTimeout(resolveTypedArtifact, 350)
}

watch(() => props.modelValue, visible => {
    if (!visible) return
    isEdit.value = Boolean(props.editData)
    form.value = props.editData ? { ...emptyForm(), ...props.editData } : emptyForm()
})

const submit = () => {
    if (!form.value.projectName?.trim()) return ElMessage.warning('请输入项目名称')
    if (!form.value.artifactName?.trim()) return ElMessage.warning('请输入文物名称')
    emit('save', {
        ...form.value,
        status: form.value.status || 'draft',
        currentStage: form.value.currentStage || 'pendingSurvey',
        progress: form.value.progress || 0
    })
}
</script>

<template>
    <el-dialog
        :model-value="modelValue"
        :title="isEdit ? '编辑项目' : '新建保护修复项目'"
        width="680px"
        :close-on-click-modal="false"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <el-form :model="form" label-width="90px">
            <el-form-item label="建档模式">
                <el-radio-group v-model="form.recordMode">
                    <el-radio-button value="quick">快速记录</el-radio-button>
                    <el-radio-button value="standard">标准修复</el-radio-button>
                    <el-radio-button value="full">完整项目</el-radio-button>
                </el-radio-group>
                <div style="margin-top: 7px; color: #909399; font-size: 12px; line-height: 1.5">
                    {{ form.recordMode === 'quick' ? '适合珠子、碎片等简单处理：仅填写问题、方法、前后照片和结论。' : form.recordMode === 'standard' ? '按需填写病害、过程、对比、复原和监测，不要求全部启用。' : '适合墓葬、棺、车等复杂项目，使用完整保护修复工作流。' }}
                </div>
            </el-form-item>
            <el-form-item label="文物">
                <el-autocomplete
                    v-model="form.artifactName"
                    :fetch-suggestions="fetchArtifactSuggestions"
                    placeholder="请输入文物名称"
                    maxlength="200"
                    clearable
                    style="width: 100%"
                    @input="onArtifactInput"
                    @select="selectArtifact"
                />
            </el-form-item>
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="文物编号"><el-autocomplete v-model="form.artifactCode" :fetch-suggestions="fetchArtifactSuggestions" placeholder="可手动输入" style="width: 100%" @input="onArtifactInput" @select="selectArtifact" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="项目编号"><el-input v-model="form.projectCode" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="项目名称"><el-input v-model="form.projectName" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="项目类型">
                    <el-select v-model="form.projectType" style="width: 100%">
                        <el-option label="保护" value="保护" />
                        <el-option label="修复" value="修复" />
                        <el-option label="复原" value="复原" />
                        <el-option label="综合" value="综合" />
                    </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="风险等级">
                    <el-select v-model="form.riskLevel" style="width: 100%">
                        <el-option label="高风险" value="high" />
                        <el-option label="中风险" value="medium" />
                        <el-option label="低风险" value="low" />
                    </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.principal" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="执行部门"><el-input v-model="form.department" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="预计完成"><el-date-picker v-model="form.expectedEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
            </el-row>
            <div class="artifact-link-tip" :class="{ linked: form.artifactId }">
                {{ form.artifactId ? `已关联文物总览（ID：${form.artifactId}），项目卡片会显示材质和所属墓葬。` : '可直接输入或从联想列表选择；未匹配时仍可用于车、棺等非文物对象。' }}
            </div>
            <el-form-item label="项目摘要"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit">保存</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.artifact-link-tip { margin: -8px 0 14px 90px; font-size: 12px; color: #909399; line-height: 1.5; }
.artifact-link-tip.linked { color: #52906f; }
</style>
