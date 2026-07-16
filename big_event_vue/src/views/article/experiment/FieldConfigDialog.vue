<!-- 字段配置弹窗 -->
<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Boolean, fields: Array })
const emit = defineEmits(['update:modelValue', 'save'])

const local = ref([])
watch(() => props.fields, (v) => { local.value = JSON.parse(JSON.stringify(v || [])) }, { immediate: true })

const typeOptions = [
    { label: '文本', value: 'text' },
    { label: '数值', value: 'number' },
    { label: '日期', value: 'date' },
    { label: '下拉框', value: 'select' },
    { label: '多行文本', value: 'textarea' }
]

const addField = () => { local.value.push({ field: 'field_' + Date.now(), label: '', type: 'text' }) }
const removeField = (idx) => { local.value.splice(idx, 1) }
const moveUp = (idx) => { if (idx > 0) { [local.value[idx], local.value[idx - 1]] = [local.value[idx - 1], local.value[idx]] } }
const moveDown = (idx) => { if (idx < local.value.length - 1) { [local.value[idx], local.value[idx + 1]] = [local.value[idx + 1], local.value[idx]] } }

const submit = () => {
    for (const f of local.value) {
        if (!f.label.trim()) { ElMessage.warning('字段名称不能为空'); return }
        if (!f.field.trim()) f.field = 'f_' + f.label
    }
    emit('save', JSON.parse(JSON.stringify(local.value)))
    emit('update:modelValue', false)
}
</script>

<template>
    <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="配置表格字段" width="620px">
        <div v-for="(f, i) in local" :key="i" style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
            <span style="color:#999;width:24px;text-align:center">{{ i + 1 }}</span>
            <el-input v-model="f.label" placeholder="字段名称" size="small" style="width:130px" />
            <el-input v-model="f.field" placeholder="标识" size="small" style="width:130px" />
            <el-select v-model="f.type" size="small" style="width:110px">
                <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-button size="small" circle @click="moveUp(i)" :disabled="i===0">↑</el-button>
            <el-button size="small" circle @click="moveDown(i)" :disabled="i===local.length-1">↓</el-button>
            <el-button size="small" circle type="danger" @click="removeField(i)">✕</el-button>
        </div>
        <el-button size="small" @click="addField" style="margin-top:8px">+ 新增字段</el-button>
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit">保存配置</el-button>
        </template>
    </el-dialog>
</template>
