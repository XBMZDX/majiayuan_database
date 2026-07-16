<!-- 新增/编辑数据弹窗 -->
<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ modelValue: Boolean, fields: Array, editRow: Object })
const emit = defineEmits(['update:modelValue', 'save'])

const form = ref({})
const isEdit = ref(false)

watch(() => props.modelValue, (v) => {
    if (v) {
        if (props.editRow) {
            form.value = { ...props.editRow }
            isEdit.value = true
        } else {
            form.value = {}
            isEdit.value = false
        }
    }
})

const submit = () => {
    emit('save', { ...form.value })
    emit('update:modelValue', false)
}

const renderInput = (f) => {
    switch (f.type) {
        case 'number': return { tag: 'el-input-number', props: { size: 'small', style: { width: '100%' } } }
        case 'date': return { tag: 'el-date-picker', props: { type: 'date', size: 'small', style: { width: '100%' }, valueFormat: 'YYYY-MM-DD' } }
        case 'textarea': return { tag: 'el-input', props: { type: 'textarea', rows: 3, size: 'small' } }
        case 'select': return { tag: 'el-input', props: { size: 'small', placeholder: '逗号分隔选项' } }
        default: return { tag: 'el-input', props: { size: 'small' } }
    }
}
</script>

<template>
    <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)"
               :title="isEdit ? '编辑数据' : '新增数据'" width="500px">
        <el-form label-width="90px" v-if="fields.length">
            <el-form-item v-for="f in fields" :key="f.field" :label="f.label">
                <component :is="renderInput(f).tag" v-model="form[f.field]" v-bind="renderInput(f).props" />
            </el-form-item>
        </el-form>
        <el-empty v-else description="请先配置字段" :image-size="40" />
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit" :disabled="!fields.length">保存</el-button>
        </template>
    </el-dialog>
</template>
