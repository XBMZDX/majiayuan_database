<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Boolean, editData: Object })
const emit = defineEmits(['update:modelValue', 'save'])

const emptyForm = () => ({
    projectCode: `CR-${Date.now().toString(36).toUpperCase()}`,
    projectName: '',
    artifactId: null,
    artifactCode: '',
    artifactName: '',
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
            <el-form-item label="文物">
                <el-input
                    v-model="form.artifactName"
                    placeholder="请输入文物名称"
                    maxlength="200"
                    clearable
                />
            </el-form-item>
            <el-row :gutter="16">
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
            <el-form-item label="项目摘要"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit">保存</el-button>
        </template>
    </el-dialog>
</template>
