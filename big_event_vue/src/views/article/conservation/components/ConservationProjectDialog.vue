<!-- 新建/编辑项目弹窗 -->
<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Boolean, editData: Object })
const emit = defineEmits(['update:modelValue', 'save'])

const form = ref({ projectCode: '', projectName: '', artifactId: null, artifactCode: '', artifactName: '', projectType: '综合', riskLevel: 'medium', principal: '', department: '', startDate: '', expectedEndDate: '', summary: '' })
const isEdit = ref(false)

// Mock 文物搜索
const artifactOptions = ref([])
const searchArtifact = (query) => {
    if (!query) { artifactOptions.value = [] ; return }
    artifactOptions.value = [
        { id: 101, code: 'M45-C1', name: '马车本体' },
        { id: 102, code: 'M16-G1', name: '铜鼎' },
        { id: 103, code: 'M62-G2', name: '铜盘' },
    ].filter(a => a.code.includes(query) || a.name.includes(query))
}
const onArtifactSelect = (item) => {
    form.value.artifactId = item.id
    form.value.artifactCode = item.code
    form.value.artifactName = item.name
}

watch(() => props.modelValue, (v) => {
    if (v) {
        if (props.editData) {
            form.value = { ...props.editData }
            isEdit.value = true
        } else {
            form.value = { projectCode: 'CR-' + Date.now().toString(36).toUpperCase(), projectName: '', artifactId: null, artifactCode: '', artifactName: '', projectType: '综合', riskLevel: 'medium', principal: '', department: '', startDate: '', expectedEndDate: '', summary: '' }
            isEdit.value = false
        }
    }
})

const submit = () => {
    if (!form.value.projectName.trim()) { ElMessage.warning('请输入项目名称'); return }
    emit('save', { ...form.value, id: isEdit.value ? form.value.id : Date.now(), status: isEdit.value ? form.value.status : 'draft', currentStage: isEdit.value ? form.value.currentStage : 'pendingSurvey', progress: isEdit.value ? form.value.progress : 0, updateTime: new Date().toISOString() })
    emit('update:modelValue', false)
}
</script>

<template>
    <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)"
               :title="isEdit ? '编辑项目' : '新建保护修复项目'" width="680px" :close-on-click-modal="false">
        <el-form :model="form" label-width="90px">
            <el-divider content-position="left">选择文物</el-divider>
            <el-form-item label="文物">
                <el-select v-model="form.artifactId" placeholder="搜索文物编号或名称" filterable remote :remote-method="searchArtifact" style="width:100%" value-key="id">
                    <el-option v-for="a in artifactOptions" :key="a.id" :label="a.code + ' ' + a.name" :value="a.id" @click="onArtifactSelect(a)" />
                </el-select>
            </el-form-item>
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="项目编号"><el-input v-model="form.projectCode" placeholder="自动生成" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="项目名称"><el-input v-model="form.projectName" placeholder="如 M45-C1马车修复" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="项目类型">
                    <el-select v-model="form.projectType" style="width:100%">
                        <el-option label="保护" value="保护" /><el-option label="修复" value="修复" />
                        <el-option label="复原" value="复原" /><el-option label="综合" value="综合" />
                    </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="风险等级">
                    <el-select v-model="form.riskLevel" style="width:100%">
                        <el-option label="高风险" value="high" /><el-option label="中风险" value="medium" /><el-option label="低风险" value="low" />
                    </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.principal" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="执行部门"><el-input v-model="form.department" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="预计完成"><el-date-picker v-model="form.expectedEndDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="项目摘要"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
</template>
