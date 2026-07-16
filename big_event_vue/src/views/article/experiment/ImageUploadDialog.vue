<!-- 上传图片弹窗 -->
<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import { computed } from 'vue'
const token = computed(() => useTokenStore().token)

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'submit'])

const categories = ['光谱图', 'CT图像', '显微照片', '检测照片', '三维模型截图', '其他']
const form = ref({ title: '', category: '光谱图', description: '', url: '' })

const resetForm = () => { form.value = { title: '', category: '光谱图', description: '', url: '' } }
watch(() => props.modelValue, (v) => { if (v) resetForm() })

const onUploadSuccess = (res) => { form.value.url = res.data; ElMessage.success('上传成功') }

const submit = () => {
    if (!form.value.url) { ElMessage.warning('请先上传图片'); return }
    if (!form.value.title.trim()) form.value.title = '未命名图片'
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
               title="上传实验图像" width="500px">
        <el-form :model="form" label-width="80px">
            <el-form-item label="选择图片">
                <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }"
                           :show-file-list="true" :auto-upload="true" :on-success="onUploadSuccess"
                           accept="image/*" :limit="1">
                    <el-button type="primary">选择图片文件</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="图片名称"><el-input v-model="form.title" placeholder="如 XRF 光谱图" /></el-form-item>
            <el-form-item label="图片分类">
                <el-select v-model="form.category" style="width:100%">
                    <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
                </el-select>
            </el-form-item>
            <el-form-item label="图片说明"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="emit('update:modelValue', false)">取消</el-button>
            <el-button type="primary" @click="submit">确认上传</el-button>
        </template>
    </el-dialog>
</template>
