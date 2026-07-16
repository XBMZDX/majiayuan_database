<!-- 上传附件弹窗 -->
<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import { computed } from 'vue'
const token = computed(() => useTokenStore().token)

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'submit'])

const fileTypes = ['PDF', 'Word', 'Excel', 'ZIP', '图片', '其他']
const form = ref({ name: '', type: 'PDF', description: '', url: '', size: '' })

watch(() => props.modelValue, (v) => { if (v) form.value = { name: '', type: 'PDF', description: '', url: '', size: '' } })

const detectType = (name) => {
    const ext = (name || '').split('.').pop()?.toLowerCase()
    const map = { pdf: 'PDF', doc: 'Word', docx: 'Word', xls: 'Excel', xlsx: 'Excel', zip: 'ZIP', rar: 'ZIP', '7z': 'ZIP', jpg: '图片', jpeg: '图片', png: '图片', gif: '图片', bmp: '图片', webp: '图片' }
    return map[ext] || '其他'
}

const onUploadSuccess = (res, file) => {
    form.value.url = res.data
    form.value.name = file.name
    form.value.size = file.size ? (file.size / 1024 / 1024).toFixed(2) + ' MB' : '-'
    form.value.type = detectType(file.name)
    ElMessage.success('上传成功')
}

const submit = () => {
    if (!form.value.url) { ElMessage.warning('请先上传文件'); return }
    if (!form.value.name.trim()) form.value.name = '未命名文件'
    emit('submit', {
        ...form.value,
        id: Date.now(),
        uploadTime: new Date().toISOString()
    })
    emit('update:modelValue', false)
}
</script>

<template>
    <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)"
               title="上传附件资料" width="500px">
        <el-form :model="form" label-width="80px">
            <el-form-item label="选择文件">
                <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }"
                           :show-file-list="true" :auto-upload="true" :on-success="onUploadSuccess"
                           :limit="1">
                    <el-button type="primary">选择文件</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="文件名称"><el-input v-model="form.name" placeholder="如 XRF检测报告.pdf" /></el-form-item>
            <el-form-item label="文件类型">
                <el-select v-model="form.type" style="width:100%">
                    <el-option v-for="t in fileTypes" :key="t" :label="t" :value="t" />
                </el-select>
            </el-form-item>
            <el-form-item label="文件说明"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit">确认上传</el-button>
        </template>
    </el-dialog>
</template>
