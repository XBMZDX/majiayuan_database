<script setup>
import { nextTick, ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Download, Edit } from '@element-plus/icons-vue'
import * as resApi from '@/api/digitalArchive/digitalResource.js'

const props = defineProps({
    showSummary: { type: Boolean, default: true },
    showUpload: { type: Boolean, default: true },
    documentOnly: { type: Boolean, default: false }
})
const emit = defineEmits(['resource-changed'])

const loading = ref(false)
const list = ref([]); const total = ref(0)
const summary = ref({})
const selectedIds = ref([])
const tableRef = ref()

const q = reactive({ keyword: '', resourceType: '', resourceStatus: '', dataStatus: '', pageNum: 1, pageSize: 20 })
const typeOptions = [{ label: '全部', value: '' }, { label: '图片', value: 'image' }, { label: '文档', value: 'document' }, { label: '报告', value: 'report' }, { label: '视频/音频', value: 'video' }, { label: '三维模型', value: 'model_3d' }, { label: '其他', value: 'other' }]
const typeNames = { image: '图片', document: '文档', report: '报告', video: '视频', audio: '音频', spreadsheet: '表格', model_3d: '三维模型', other: '其他' }
const formatSize = (bytes) => { if (!bytes || bytes === 0) return '0 B'; const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes; while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }; return v.toFixed(1) + ' ' + units[i] }

const loadSummary = async () => { const r = await resApi.getSummary({}).catch(() => ({})); summary.value = r.data || {} }
const loadList = async () => {
    loading.value = true
    try {
        const r = await resApi.getList({ ...q, deleted: false, documentOnly: props.documentOnly })
        list.value = r.data?.records || []; total.value = r.data?.total || 0
        selectedIds.value = []
        await nextTick()
        tableRef.value?.clearSelection()
    } catch (error) {
        ElMessage.error(error?.message || '资源列表加载失败，请检查登录状态和后端服务')
    } finally {
        loading.value = false
    }
}
const handleSearch = () => { q.pageNum = 1; loadList() }
const handleReset = () => { Object.assign(q, { keyword: '', resourceType: '', resourceStatus: '', dataStatus: '', pageNum: 1, pageSize: 20 }); loadList() }

// 详情 Drawer
const drawerVisible = ref(false); const detailData = ref({}); const detailTab = ref('info')
const openDetail = async (row) => { const r = await resApi.getDetail(row.id); detailData.value = r.data || {}; drawerVisible.value = true; detailTab.value = 'info' }
const editMeta = async () => {
    await resApi.updateMeta(detailData.value.id, detailData.value)
    ElMessage.success('已保存'); drawerVisible.value = false; loadList()
}
const downloadResource = async (row) => {
    try {
        const response = await resApi.downloadResource(row.id)
        const contentType = response.headers?.['content-type'] || ''
        if (contentType.includes('application/json')) {
            const errorText = await response.data.text()
            let message = '文件下载失败'
            try { message = JSON.parse(errorText).message || message } catch (_) {}
            throw new Error(message)
        }
        const blobUrl = URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = blobUrl
        link.download = row.originalFileName || row.resourceName || 'download'
        document.body.appendChild(link)
        link.click()
        link.remove()
        URL.revokeObjectURL(blobUrl)
    } catch (error) {
        ElMessage.error(error.message || '文件下载失败')
    }
}
const doDelete = async (row) => {
    await ElMessageBox.confirm('确定删除该资源？删除后将不再在文件资源库中显示。', '确认删除', { type: 'warning' })
    await resApi.softDelete(row.id); ElMessage.success('已删除'); if (props.showSummary) await loadSummary(); await loadList(); emit('resource-changed')
}
const handleSelectionChange = (rows) => {
    selectedIds.value = rows.map(row => row.id)
}
const batchDelete = async () => {
    if (!selectedIds.value.length) {
        ElMessage.warning('请先选择需要删除的资源')
        return
    }
    const count = selectedIds.value.length
    await ElMessageBox.prompt(
        `即将删除 ${count} 个资源。请输入 ${count} 确认删除；删除后资源会从列表中隐藏。`,
        '批量删除确认',
        { inputPlaceholder: `请输入 ${count}`, inputValidator: input => input === String(count) || `请输入 ${count} 以确认` }
    )
    await resApi.batchDelete({ resourceIds: selectedIds.value })
    ElMessage.success(`已删除 ${count} 个资源`)
    selectedIds.value = []
    if (props.showSummary) await loadSummary()
    await loadList()
    emit('resource-changed')
}

// 新增资源
const createVisible = ref(false)
const createSubmitting = ref(false)
const selectedCreateFiles = ref([])
const createForm = reactive({
    file: null,
    resourceName: '',
    title: '',
    resourceType: '',
    description: '',
    keywords: ''
})
const openCreateDialog = () => { createVisible.value = true }
const inferResourceType = (fileName = '') => {
    const extension = fileName.includes('.') ? fileName.split('.').pop().toLowerCase() : ''
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'tif', 'tiff'].includes(extension)) return 'image'
    if (['mp4', 'mov', 'avi', 'mkv', 'flv', 'webm'].includes(extension)) return 'video'
    if (['mp3', 'wav', 'aac', 'flac', 'ogg'].includes(extension)) return 'audio'
    if (['obj', 'fbx', 'gltf', 'glb', 'stl', 'ply', '3ds'].includes(extension)) return 'model_3d'
    if (['xls', 'xlsx', 'csv'].includes(extension)) return 'spreadsheet'
    if (['pdf', 'doc', 'docx', 'ppt', 'pptx', 'txt', 'rtf'].includes(extension)) return 'document'
    return 'other'
}
const handleCreateFileChange = (uploadFile, uploadFiles = []) => {
    selectedCreateFiles.value = uploadFiles.map(item => item.raw).filter(Boolean)
    if (selectedCreateFiles.value.length === 1) {
        createForm.file = uploadFile.raw
        createForm.resourceType = inferResourceType(uploadFile.name)
        if (!createForm.resourceName) createForm.resourceName = (uploadFile.name || '').replace(/\.[^.]+$/, '')
        if (!createForm.title) createForm.title = createForm.resourceName
    } else {
        createForm.file = null
        createForm.resourceName = ''
        createForm.title = ''
        createForm.resourceType = 'batch'
    }
}
const handleCreateFileRemove = (uploadFile, uploadFiles = []) => {
    selectedCreateFiles.value = uploadFiles.map(item => item.raw).filter(Boolean)
    if (selectedCreateFiles.value.length === 1) {
        const file = selectedCreateFiles.value[0]
        createForm.file = file
        createForm.resourceType = inferResourceType(file.name)
        createForm.resourceName = file.name.replace(/\.[^.]+$/, '')
        createForm.title = createForm.resourceName
    } else if (!selectedCreateFiles.value.length) {
        createForm.file = null
        createForm.resourceType = ''
        createForm.resourceName = ''
        createForm.title = ''
    } else {
        createForm.file = null
        createForm.resourceType = 'batch'
    }
}
const resetCreateForm = () => {
    selectedCreateFiles.value = []
    createForm.file = null
    createForm.resourceName = ''
    createForm.title = ''
    createForm.resourceType = ''
    createForm.description = ''
    createForm.keywords = ''
}
const submitCreate = async () => {
    if (!selectedCreateFiles.value.length) {
        ElMessage.warning('请先选择文件')
        return
    }
    createSubmitting.value = true
    try {
        const fd = new FormData()
        const singleFile = selectedCreateFiles.value.length === 1
        if (singleFile) {
            const file = selectedCreateFiles.value[0]
            const defaultName = file.name.replace(/\.[^.]+$/, '')
            fd.append('file', file)
            fd.append('resourceName', createForm.resourceName || defaultName)
            fd.append('title', createForm.title || createForm.resourceName || defaultName)
            fd.append('resourceType', createForm.resourceType)
        } else {
            selectedCreateFiles.value.forEach(file => fd.append('files', file))
        }
        if (createForm.description) fd.append('description', createForm.description)
        if (createForm.keywords) fd.append('keywords', createForm.keywords)
        const response = await (singleFile ? resApi.createResource(fd) : resApi.batchUploadResources(fd))
        const failedCount = Number(response.data?.failedCount || 0)
        const successCount = Number(response.data?.successCount || selectedCreateFiles.value.length)
        if (failedCount) {
            ElMessage.warning(`已上传 ${successCount} 个文件，${failedCount} 个文件上传失败`)
        } else {
            ElMessage.success(singleFile ? '资源已上传' : `已成功上传 ${successCount} 个资源`)
        }
        createVisible.value = false
        resetCreateForm()
        if (props.showSummary) await loadSummary()
        await loadList()
        emit('resource-changed')
    } finally {
        createSubmitting.value = false
    }
}

onMounted(() => { if (props.showSummary) loadSummary(); loadList() })
</script>

<template>
    <div class="res-page" v-loading="loading">
        <!-- 统计摘要 -->
        <div v-if="showSummary" class="summary-bar">
            <div class="summary-item" v-for="s in [
                {label:'资源总数',value:summary.totalResources,color:'#409EFF'},
                {label:'图片',value:summary.imageCount,color:'#67C23A'},
                {label:'文档/报告',value:summary.documentCount,color:'#E6A23C'},
                {label:'视频/音频',value:summary.videoCount,color:'#B37FEB'},
                {label:'三维模型',value:summary.modelCount,color:'#F56C6C'},
                {label:'存储空间',value:formatSize(summary.totalStorageBytes),color:'#67C23A'}
            ]" :key="s.label" @click="q.resourceType = s.label === '图片' ? 'image' : s.label === '文档/报告' ? 'document' : s.label === '视频/音频' ? 'video' : s.label === '三维模型' ? 'model_3d' : ''; handleSearch()">
                <div class="si-value" :style="{color:s.color}">{{ s.value ?? '-' }}</div>
                <div class="si-label">{{ s.label }}</div>
            </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <el-input v-model="q.keyword" placeholder="搜索编号/名称/文件名..." :prefix-icon="Search" clearable size="default" style="width:240px" @keyup.enter="handleSearch" />
            <el-select v-if="!documentOnly" v-model="q.resourceType" placeholder="类型" clearable size="default" style="width:120px" @change="handleSearch"><el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" /></el-select>
            <el-select v-model="q.resourceStatus" placeholder="状态" clearable size="default" style="width:110px" @change="handleSearch"><el-option label="全部" value="" /><el-option label="正常" value="normal" /><el-option label="预览失败" value="preview_failed" /><el-option label="损坏" value="damaged" /></el-select>
            <el-button type="primary" size="default" @click="handleSearch">查询</el-button>
            <el-button size="default" @click="handleReset">重置</el-button>
            <el-button size="default" :icon="Refresh" @click="loadList" />
            <el-button v-if="showUpload" type="primary" size="default" :icon="Plus" @click="openCreateDialog">上传/新增资源</el-button>
            <el-button type="danger" size="default" :icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除{{ selectedIds.length ? ` (${selectedIds.length})` : '' }}</el-button>
            <span style="flex:1" />
        </div>

        <!-- 列表模式 -->
        <el-table ref="tableRef" :data="list" size="small" @row-dblclick="(r) => openDetail(r)" @selection-change="handleSelectionChange" highlight-current-row>
            <el-table-column type="selection" width="40" />
            <el-table-column label="资源名称" min-width="180" show-overflow-tooltip>
                <template #default="{row}">
                    <div style="display:flex;align-items:center;gap:6px;cursor:pointer" @click="openDetail(row)">
                        <el-image v-if="row.thumbnailUrl" :src="row.thumbnailUrl" fit="cover" style="width:32px;height:32px;border-radius:4px" />
                        <span v-else style="font-size:18px">{{ typeNames[row.resourceType] === '图片' ? '🖼' : typeNames[row.resourceType] === '文档' ? '📄' : typeNames[row.resourceType] === '视频' ? '🎬' : typeNames[row.resourceType] === '三维模型' ? '🧊' : '📎' }}</span>
                        <div><div style="font-size:13px;font-weight:500">{{ row.resourceName || '-' }}</div><div style="font-size:11px;color:#C0C4CC">{{ row.originalFileName || '' }}</div></div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="类型" width="80"><template #default="{row}">{{ typeNames[row.resourceType] || row.resourceType }}</template></el-table-column>
            <el-table-column label="格式" width="70"><template #default="{row}">{{ row.fileExtension || '-' }}</template></el-table-column>
            <el-table-column label="大小" width="80"><template #default="{row}">{{ formatSize(row.fileSize) }}</template></el-table-column>
            <el-table-column label="版本" width="70"><template #default="{row}">{{ row.currentVersion || 'V1.0' }}</template></el-table-column>
            <el-table-column label="状态" width="70"><template #default="{row}"><el-tag :type="row.resourceStatus==='normal'?'success':row.resourceStatus==='preview_failed'?'warning':'danger'" size="small">{{ row.resourceStatus === 'normal' ? '正常' : row.resourceStatus || '-' }}</el-tag></template></el-table-column>
            <el-table-column label="上传时间" width="110"><template #default="{row}">{{ (row.uploadTime||'').substring(0,10) }}</template></el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
                <template #default="{row}">
                    <el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button>
                    <el-button size="small" text type="primary" :icon="Download" @click="downloadResource(row)">下载</el-button>
                    <el-button size="small" text type="danger" @click="doDelete(row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 卡片模式 -->
        <div v-if="false" class="card-grid">
            <div v-for="row in list" :key="row.id" class="res-card" @dblclick="openDetail(row)">
                <div class="rc-thumb">
                    <el-image v-if="row.thumbnailUrl" :src="row.thumbnailUrl" fit="cover" style="height:160px" />
                    <div v-else class="rc-placeholder">{{ typeNames[row.resourceType] === '图片' ? '🖼' : typeNames[row.resourceType] === '视频' ? '🎬' : typeNames[row.resourceType] === '三维模型' ? '🧊' : '📎' }}</div>
                </div>
                <div class="rc-body">
                    <div class="rc-name">{{ row.resourceName || '-' }}</div>
                    <div class="rc-meta">
                        <el-tag size="small">{{ typeNames[row.resourceType] || '-' }}</el-tag>
                        <span>{{ formatSize(row.fileSize) }}</span>
                        <span>{{ (row.uploadTime||'').substring(0,10) }}</span>
                    </div>
                </div>
            </div>
        </div>

        <el-empty v-if="!list.length && !loading" description="暂无文件资源" :image-size="80" style="padding:60px 0" />

        <!-- 分页 -->
        <el-pagination v-if="total > q.pageSize" v-model:current-page="q.pageNum" :page-size="q.pageSize" :total="total" layout="total, prev, pager, next" background style="margin-top:16px;justify-content:flex-end" @current-change="loadList" />
    </div>

    <!-- 详情 Drawer -->
    <el-drawer v-model="drawerVisible" title="文件资源详情" size="680px">
        <template v-if="detailData">
            <el-tabs v-model="detailTab">
                <el-tab-pane label="基本信息" name="info">
                    <el-descriptions :column="2" size="small" border>
                        <el-descriptions-item label="资源编号">{{ detailData.resourceCode }}</el-descriptions-item>
                        <el-descriptions-item label="资源名称">{{ detailData.resourceName }}</el-descriptions-item>
                        <el-descriptions-item label="原始文件名">{{ detailData.originalFileName }}</el-descriptions-item>
                        <el-descriptions-item label="类型">{{ typeNames[detailData.resourceType] || detailData.resourceType }}</el-descriptions-item>
                        <el-descriptions-item label="格式">{{ detailData.fileExtension }}</el-descriptions-item>
                        <el-descriptions-item label="大小">{{ formatSize(detailData.fileSize) }}</el-descriptions-item>
                        <el-descriptions-item label="状态">{{ detailData.resourceStatus || 'normal' }}</el-descriptions-item>
                        <el-descriptions-item label="上传人">{{ detailData.uploadedBy || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="上传时间">{{ detailData.uploadTime }}</el-descriptions-item>
                        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
                    </el-descriptions>
                    <div style="margin-top:12px;display:flex;gap:8px">
                        <el-button type="primary" size="small" :icon="Edit" @click="editMeta">保存修改</el-button>
                        <el-button size="small" :icon="Download" @click="downloadResource(detailData)">下载文件</el-button>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </template>
    </el-drawer>

    <!-- 新增资源 Dialog -->
    <el-dialog v-model="createVisible" title="上传/新增资源" width="760px" @closed="resetCreateForm">
        <el-form :model="createForm" label-width="110px">
            <el-form-item label="文件">
                <el-upload
                    action=""
                    :auto-upload="false"
                    :limit="30"
                    multiple
                    :show-file-list="true"
                    :on-change="handleCreateFileChange"
                    :on-remove="handleCreateFileRemove">
                    <el-button type="primary">选择文件</el-button>
                </el-upload>
                <div v-if="selectedCreateFiles.length > 1" style="margin-top:6px;color:#409EFF;font-size:12px">
                    已选择 {{ selectedCreateFiles.length }} 个文件，将按各自文件名分别创建资源并自动识别类型。
                </div>
                <div style="margin-top:6px;color:#909399;font-size:12px">文件会先传到 OSS，MySQL 只保存 URL 和元数据。</div>
            </el-form-item>
            <el-form-item v-if="selectedCreateFiles.length <= 1" label="资源名称">
                <el-input v-model="createForm.resourceName" placeholder="不填则默认使用文件名" />
            </el-form-item>
            <el-form-item v-if="selectedCreateFiles.length <= 1" label="资源标题">
                <el-input v-model="createForm.title" placeholder="展示标题" />
            </el-form-item>
            <el-row :gutter="12">
                <el-col :span="24">
                    <el-form-item label="资源类型">
                        <el-input :model-value="selectedCreateFiles.length > 1 ? `已选择 ${selectedCreateFiles.length} 个文件，提交后分别自动识别` : (createForm.resourceType ? (typeNames[createForm.resourceType] || createForm.resourceType) : '请选择文件后自动识别')" disabled />
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="关键词">
                <el-input v-model="createForm.keywords" placeholder="多个关键词可用逗号分隔" />
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="资源说明" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="createVisible = false">取消</el-button>
            <el-button type="primary" :loading="createSubmitting" @click="submitCreate">提交</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.res-page { padding: 0; }
.summary-bar { display: flex; gap: 12px; margin-bottom: 14px; flex-wrap: wrap; }
.summary-item { flex: 1; min-width: 100px; padding: 12px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; text-align: center; transition: box-shadow .15s; }
.summary-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.si-value { font-size: 20px; font-weight: 700; }
.si-label { font-size: 11px; color: #909399; margin-top: 2px; }
.filter-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.res-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .15s; }
.res-card:hover { box-shadow: 0 2px 10px rgba(0,0,0,0.08); }
.rc-thumb { height: 160px; background: #F5F7FA; display: flex; align-items: center; justify-content: center; }
.rc-placeholder { font-size: 40px; }
.rc-body { padding: 10px 12px; }
.rc-name { font-size: 13px; font-weight: 600; color: #1D2129; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.rc-meta { display: flex; gap: 8px; align-items: center; font-size: 11px; color: #C0C4CC; }
@media (max-width: 1200px) { .card-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
