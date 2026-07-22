<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Grid, List, Delete, Download, Edit } from '@element-plus/icons-vue'
import * as resApi from '@/api/digitalArchive/digitalResource.js'

const router = useRouter()
const loading = ref(false)
const viewMode = ref('table')
const showTrash = ref(false)
const list = ref([]); const total = ref(0)
const summary = ref({})
const selectedIds = ref([])

const q = reactive({ keyword: '', resourceType: '', sourceModule: '', resourceStatus: '', dataStatus: '', pageNum: 1, pageSize: 20 })
const typeOptions = [{ label: '全部', value: '' }, { label: '图片', value: 'image' }, { label: '文档', value: 'document' }, { label: '报告', value: 'report' }, { label: '视频/音频', value: 'video' }, { label: '三维模型', value: 'model_3d' }, { label: '其他', value: 'other' }]
const moduleOptions = [{ label: '全部', value: '' }, { label: '墓葬', value: 'tomb' }, { label: '文物', value: 'artifact' }, { label: '检测', value: 'detection' }, { label: '病害', value: 'disease' }, { label: '保护修复', value: 'conservation' }, { label: '手工上传', value: 'manual' }]
const sourceModuleNames = { tomb: '墓葬信息', coffin: '棺信息', vehicle: '车信息', artifact: '文物信息', detection: '检测分析', disease: '病害调查', conservation: '保护修复', process: '修复过程', monitoring: '后续监测', manual: '手工上传', digital_archive: '数字档案' }
const typeNames = { image: '图片', document: '文档', report: '报告', video: '视频', audio: '音频', spreadsheet: '表格', model_3d: '三维模型', other: '其他' }
const formatSize = (bytes) => { if (!bytes || bytes === 0) return '0 B'; const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes; while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }; return v.toFixed(1) + ' ' + units[i] }

const loadSummary = async () => { const r = await resApi.getSummary({}).catch(() => ({})); summary.value = r.data || {} }
const loadList = async () => {
    loading.value = true
    const r = await resApi.getList({ ...q, deleted: showTrash.value }).catch(() => ({ data: { records: [], total: 0 } }))
    list.value = r.data?.records || []; total.value = r.data?.total || 0
    loading.value = false
}
const handleSearch = () => { q.pageNum = 1; loadList() }
const handleReset = () => { Object.assign(q, { keyword: '', resourceType: '', sourceModule: '', resourceStatus: '', dataStatus: '', pageNum: 1, pageSize: 20 }); loadList() }

// 详情 Drawer
const drawerVisible = ref(false); const detailData = ref({}); const detailTab = ref('info')
const openDetail = async (row) => { const r = await resApi.getDetail(row.id); detailData.value = r.data || {}; drawerVisible.value = true; detailTab.value = 'info' }
const editMeta = async () => {
    await resApi.updateMeta(detailData.value.id, detailData.value)
    ElMessage.success('已保存'); drawerVisible.value = false; loadList()
}
const doDelete = async (row) => {
    await ElMessageBox.confirm('确定移入回收站？', '确认', { type: 'warning' })
    await resApi.softDelete(row.id); ElMessage.success('已移入回收站'); loadList()
}
const doRestore = async (row) => { await resApi.restore(row.id); ElMessage.success('已恢复'); loadList() }
const doPermanent = async (row) => {
    await ElMessageBox.confirm('永久删除后无法恢复！', '确认', { type: 'warning', confirmButtonClass: 'el-button--danger' })
    const r = await resApi.permanentDelete(row.id)
    if (r.data?.canDelete === false) { ElMessage.warning(r.data.blockReason); return }
    ElMessage.success('已永久删除'); loadList()
}

// 标签
const allTags = ref([])
const loadTags = async () => { const r = await resApi.getAllTags(); allTags.value = r.data || [] }
const tagVisible = ref(false); const tagForm = ref({ tagIds: [] })
const openTagDialog = (row) => {
    tagForm.value = { tagIds: (detailData.value.tags || []).map(t => t.id) }; tagVisible.value = true
}
const saveTags = async () => {
    await resApi.setTags(detailData.value.id, tagForm.value); ElMessage.success('标签已更新'); tagVisible.value = false; openDetail({ id: detailData.value.id })
}

// 关联
const relVisible = ref(false); const relForm = ref({ relationType: 'artifact', relationId: '', relationCode: '', relationName: '', isPrimary: false })
const addRelation = () => { relVisible.value = true }
const saveRelation = async () => {
    await resApi.addRelation(detailData.value.id, relForm.value); ElMessage.success('已添加'); relVisible.value = false; openDetail({ id: detailData.value.id })
}
const removeRel = async (rel) => {
    await resApi.removeRelation(rel.id); ElMessage.success('已移除'); openDetail({ id: detailData.value.id })
}

onMounted(() => { loadSummary(); loadList(); loadTags() })
</script>

<template>
    <div class="res-page" v-loading="loading">
        <!-- 统计摘要 -->
        <div class="summary-bar">
            <div class="summary-item" v-for="s in [
                {label:'资源总数',value:summary.totalResources,color:'#409EFF'},
                {label:'图片',value:summary.imageCount,color:'#67C23A'},
                {label:'文档/报告',value:summary.documentCount,color:'#E6A23C'},
                {label:'视频/音频',value:summary.videoCount,color:'#B37FEB'},
                {label:'三维模型',value:summary.modelCount,color:'#F56C6C'},
                {label:'已关联',value:summary.relatedCount,color:'#909399'},
                {label:'待完善',value:summary.incompleteCount,color:'#E6A23C'},
                {label:'存储空间',value:formatSize(summary.totalStorageBytes),color:'#67C23A'}
            ]" :key="s.label" @click="q.resourceType = s.label === '图片' ? 'image' : s.label === '文档/报告' ? 'document' : s.label === '视频/音频' ? 'video' : s.label === '三维模型' ? 'model_3d' : ''; handleSearch()">
                <div class="si-value" :style="{color:s.color}">{{ s.value ?? '-' }}</div>
                <div class="si-label">{{ s.label }}</div>
            </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <el-input v-model="q.keyword" placeholder="搜索编号/名称/文件名..." :prefix-icon="Search" clearable size="default" style="width:240px" @keyup.enter="handleSearch" />
            <el-select v-model="q.resourceType" placeholder="类型" clearable size="default" style="width:120px" @change="handleSearch"><el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" /></el-select>
            <el-select v-model="q.sourceModule" placeholder="来源" clearable size="default" style="width:120px" @change="handleSearch"><el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" /></el-select>
            <el-select v-model="q.resourceStatus" placeholder="状态" clearable size="default" style="width:110px" @change="handleSearch"><el-option label="全部" value="" /><el-option label="正常" value="normal" /><el-option label="预览失败" value="preview_failed" /><el-option label="损坏" value="damaged" /></el-select>
            <el-button type="primary" size="default" @click="handleSearch">查询</el-button>
            <el-button size="default" @click="handleReset">重置</el-button>
            <el-button size="default" :icon="Refresh" @click="loadList" />
            <span style="flex:1" />
            <el-button size="default" :type="viewMode==='table'?'primary':''" :icon="List" @click="viewMode='table'" />
            <el-button size="default" :type="viewMode==='card'?'primary':''" :icon="Grid" @click="viewMode='card'" />
            <el-button size="default" :type="showTrash?'danger':''" @click="showTrash=!showTrash; loadList()">{{ showTrash ? '回收站' : '正常文件' }}</el-button>
        </div>

        <!-- 列表模式 -->
        <el-table v-if="viewMode==='table'" :data="list" size="small" @row-dblclick="(r) => openDetail(r)" highlight-current-row>
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
            <el-table-column label="来源" width="100"><template #default="{row}">{{ sourceModuleNames[row.sourceModule] || row.sourceModule || '-' }}</template></el-table-column>
            <el-table-column label="格式" width="70"><template #default="{row}">{{ row.fileExtension || '-' }}</template></el-table-column>
            <el-table-column label="大小" width="80"><template #default="{row}">{{ formatSize(row.fileSize) }}</template></el-table-column>
            <el-table-column label="版本" width="70"><template #default="{row}">{{ row.currentVersion || 'V1.0' }}</template></el-table-column>
            <el-table-column label="状态" width="70"><template #default="{row}"><el-tag :type="row.resourceStatus==='normal'?'success':row.resourceStatus==='preview_failed'?'warning':'danger'" size="small">{{ row.resourceStatus === 'normal' ? '正常' : row.resourceStatus || '-' }}</el-tag></template></el-table-column>
            <el-table-column label="上传时间" width="110"><template #default="{row}">{{ (row.uploadTime||'').substring(0,10) }}</template></el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
                <template #default="{row}">
                    <el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button>
                    <el-button v-if="!showTrash" size="small" text type="danger" @click="doDelete(row)">删除</el-button>
                    <el-button v-else size="small" text type="success" @click="doRestore(row)">恢复</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 卡片模式 -->
        <div v-else class="card-grid">
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
                        <el-descriptions-item label="来源模块">{{ sourceModuleNames[detailData.sourceModule] || detailData.sourceModule }}</el-descriptions-item>
                        <el-descriptions-item label="当前版本">{{ detailData.currentVersion || 'V1.0' }}</el-descriptions-item>
                        <el-descriptions-item label="状态">{{ detailData.resourceStatus || 'normal' }}</el-descriptions-item>
                        <el-descriptions-item label="上传人">{{ detailData.uploadedBy || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="上传时间">{{ detailData.uploadTime }}</el-descriptions-item>
                        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
                    </el-descriptions>
                    <div style="margin-top:12px;display:flex;gap:8px">
                        <el-button type="primary" size="small" :icon="Edit" @click="editMeta">保存修改</el-button>
                        <el-button size="small" @click="openTagDialog(detailData)">管理标签</el-button>
                        <el-button size="small" @click="addRelation">添加关联</el-button>
                        <el-button size="small" type="danger" @click="doPermanent(detailData)" v-if="showTrash">永久删除</el-button>
                    </div>
                </el-tab-pane>
                <!-- 业务关联 -->
                <el-tab-pane label="业务关联" name="relations">
                    <div v-for="rel in (detailData.relations||[])" :key="rel.id" style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #F0F0F0">
                        <div>
                            <el-tag size="small">{{ rel.isPrimary ? '⭐' : '' }} {{ rel.relationType }}</el-tag>
                            <span style="margin-left:8px">{{ rel.relationCode }} {{ rel.relationName }}</span>
                        </div>
                        <div>
                            <el-button size="small" text @click="setPrimary(rel.id)">设为主</el-button>
                            <el-button size="small" text type="danger" @click="removeRel(rel)">移除</el-button>
                        </div>
                    </div>
                    <el-empty v-if="!detailData.relations?.length" description="暂无业务关联" :image-size="40" />
                </el-tab-pane>
                <!-- 版本 -->
                <el-tab-pane label="版本记录" name="versions">
                    <div v-for="v in (detailData.versions||[])" :key="v.id" style="padding:6px 0;border-bottom:1px solid #F0F0F0">
                        <span style="font-weight:600">{{ v.versionNo }}</span> {{ v.originalFileName }} <span style="color:#C0C4CC">{{ formatSize(v.fileSize) }}</span>
                        <span style="margin-left:8px"><el-tag size="small" :type="v.versionStatus==='current'?'success':'info'">{{ v.versionStatus }}</el-tag></span>
                    </div>
                    <el-empty v-if="!detailData.versions?.length" description="暂无版本记录" :image-size="40" />
                </el-tab-pane>
                <!-- 标签 -->
                <el-tab-pane label="标签" name="tags">
                    <el-tag v-for="t in (detailData.tags||[])" :key="t.id" size="small" effect="plain" style="margin:2px">{{ t.tagName }}</el-tag>
                    <el-empty v-if="!detailData.tags?.length" description="暂无标签" :image-size="40" />
                </el-tab-pane>
            </el-tabs>
        </template>
    </el-drawer>

    <!-- 标签 Dialog -->
    <el-dialog v-model="tagVisible" title="管理标签" width="450px">
        <el-checkbox-group v-model="tagForm.tagIds">
            <el-checkbox v-for="t in allTags" :key="t.id" :label="t.id" :value="t.id">{{ t.tagName }}</el-checkbox>
        </el-checkbox-group>
        <template #footer><el-button @click="tagVisible=false">取消</el-button><el-button type="primary" @click="saveTags">确定</el-button></template>
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
