<!-- 附件资料模块（主组件） -->
<script setup>
import { ref, computed, watch } from 'vue'
import { Upload, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import AttachmentItem from './AttachmentItem.vue'
import AttachmentUploadDialog from './AttachmentUploadDialog.vue'

const props = defineProps({ experiment: Object })
const emit = defineEmits(['update'])

const fileTypes = ['全部', 'PDF', 'Word', 'Excel', 'ZIP', '图片', '其他']
const activeType = ref('全部')
const searchQuery = ref('')

// 解析数据
const attachments = ref([])
watch(() => props.experiment, () => {
    try { attachments.value = JSON.parse(props.experiment?.attachments || '[]') } catch { attachments.value = [] }
}, { immediate: true })

const filtered = computed(() => {
    let list = attachments.value
    if (activeType.value !== '全部') list = list.filter(a => a.type === activeType.value)
    if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase()
        list = list.filter(a => (a.name || '').toLowerCase().includes(q))
    }
    return list
})

// 持久化
const persist = () => emit('update', JSON.stringify(attachments.value))

// 上传
const uploadVisible = ref(false)
const onUploadSubmit = (att) => {
    attachments.value.unshift(att)
    persist()
}

// 下载
const downloadAtt = (att) => {
    if (!att.url) return
    const a = document.createElement('a')
    a.href = att.url; a.download = att.name || 'file'
    document.body.appendChild(a); a.click(); document.body.removeChild(a)
}

// 删除
const deleteAtt = (att) => {
    ElMessageBox.confirm('确定删除该附件？', '提示', { type: 'warning' }).then(() => {
        const idx = attachments.value.findIndex(a => a.id === att.id)
        if (idx >= 0) { attachments.value.splice(idx, 1); persist() }
    }).catch(() => {})
}

const refresh = () => {
    try { attachments.value = JSON.parse(props.experiment?.attachments || '[]') } catch { attachments.value = [] }
}
</script>

<template>
    <el-card shadow="never" class="mod-card">
        <template #header>
            <div class="card-header">
                <span class="mod-title">📎 附件资料</span>
                <div class="header-actions">
                    <el-button size="small" :icon="Upload" type="primary" @click="uploadVisible = true">上传附件</el-button>
                    <el-button size="small" :icon="Refresh" @click="refresh">刷新</el-button>
                </div>
            </div>
        </template>

        <!-- 搜索与筛选 -->
        <div class="toolbar" v-if="attachments.length">
            <el-input v-model="searchQuery" placeholder="搜索文件名称..." :prefix-icon="Search" clearable size="small" style="width:240px" />
            <el-radio-group v-model="activeType" size="small">
                <el-radio-button v-for="t in fileTypes" :key="t" :value="t">{{ t }}</el-radio-button>
            </el-radio-group>
        </div>

        <!-- 附件列表 -->
        <div v-if="filtered.length">
            <AttachmentItem v-for="att in filtered" :key="att.id" :attachment="att"
                            @download="downloadAtt" @delete="deleteAtt" />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
            <el-empty description="暂无附件资料" :image-size="60">
                <template #default>
                    <span style="color:#999;font-size:13px">点击右上角 <b>上传附件</b> 添加实验资料</span>
                </template>
            </el-empty>
        </div>
    </el-card>

    <AttachmentUploadDialog v-model="uploadVisible" @submit="onUploadSubmit" />
</template>

<style scoped>
.mod-card { margin-bottom: 20px; border: 1px solid #E5E6EB; border-radius: 10px; }
.mod-title { font-size: 15px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.empty-state { text-align: center; padding: 30px 0; }
</style>
