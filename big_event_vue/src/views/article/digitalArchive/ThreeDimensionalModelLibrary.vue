<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const router = useRouter()
const loading = ref(false); const detailDrawer = ref(false)
const list = ref([]); const total = ref(0); const summary = ref({}); const activeModel = ref(null)

const q = reactive({ keyword: '', modelType: '', objectType: '', objectId: null, pageNum: 1, pageSize: 20 })
const typeMap = { scan: '扫描模型', point_cloud: '点云模型', mesh: '网格模型', processed: '处理模型', repaired: '修复模型', restored: '数字复原模型', assembly: '装配模型', other: '其他' }
const stageMap = { raw: '原始数据', processing: '处理中', draft: '草稿', review: '校核中', final: '最终成果', published: '展示版本', archived: '历史归档', obsolete: '已废止' }
const formatSize = (bytes) => { if (!bytes || bytes === 0) return '0 B'; const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes; while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }; return v.toFixed(1) + ' ' + units[i] }

const loadSummary = async () => { const r = await request.get('/api/digital-models/summary').catch(() => ({})); summary.value = r.data || {} }
const loadList = async () => {
    loading.value = true
    const r = await request.get('/api/digital-models', { params: q }).catch(() => ({ data: { records: [], total: 0 } }))
    list.value = r.data?.records || []; total.value = r.data?.total || 0
    loading.value = false
}
const handleSearch = () => { q.pageNum = 1; loadList() }

const openDetail = async (row) => { const r = await request.get('/api/digital-models/' + row.resourceId); activeModel.value = r.data || {}; detailDrawer.value = true }
const openResource = (row) => router.push('/archive/files')
const goViewer = (row) => router.push('/archive/models?view=viewer&resourceId=' + row.resourceId)

onMounted(() => { loadSummary(); loadList() })
</script>

<template>
    <div class="model-page" v-loading="loading">
        <!-- 统计 -->
        <div class="summary-bar">
            <div class="summary-item" v-for="s in [
                {label:'模型总数',value:summary.totalCount,icon:'🧊'},{label:'扫描模型',value:summary.scanCount,icon:'📡'},{label:'点云',value:summary.pointCloudCount,icon:'☁'},{label:'数字复原',value:summary.restoredCount,icon:'🔧'},{label:'关键模型',value:summary.keyModelCount,icon:'⭐'},{label:'待完善',value:summary.incompleteCount,icon:'⚠'}
            ]" :key="s.label" @click="s.label==='扫描模型'?(q.modelType='scan',handleSearch()):s.label==='数字复原'?(q.modelType='restored',handleSearch()):null">
                <div class="si-icon">{{ s.icon }}</div><div class="si-num">{{ s.value ?? 0 }}</div><div class="si-label">{{ s.label }}</div>
            </div>
        </div>

        <!-- 筛选 -->
        <div class="filter-bar">
            <el-input v-model="q.keyword" placeholder="搜索名称/对象..." :prefix-icon="Search" clearable style="width:200px" @keyup.enter="handleSearch" />
            <el-select v-model="q.modelType" placeholder="模型类型" clearable style="width:130px" @change="handleSearch"><el-option label="全部" value="" /><el-option v-for="(v,k) in typeMap" :key="k" :label="v" :value="k" /></el-select>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="loadList" />
        </div>

        <!-- 卡片 -->
        <div class="card-grid">
            <div v-for="row in list" :key="row.resourceId" class="model-card" @click="openDetail(row)">
                <div class="mc-thumb">
                    <el-image v-if="row.thumbnailUrl" :src="row.thumbnailUrl" fit="cover" style="height:180px" />
                    <div v-else class="mc-placeholder">🧊</div>
                    <el-tag v-if="row.keyModel" size="small" type="warning" effect="dark" style="position:absolute;top:6px;left:6px">⭐</el-tag>
                </div>
                <div class="mc-body">
                    <div class="mc-name">{{ row.modelTitle || row.resourceName || '-' }}</div>
                    <div class="mc-meta">
                        <el-tag size="small">{{ typeMap[row.modelType] || row.modelType || '-' }}</el-tag>
                        <span>{{ row.modelFormat || row.fileExtension || '-' }}</span>
                        <span>{{ formatSize(row.fileSize) }}</span>
                    </div>
                    <div class="mc-stats">
                        <span v-if="row.vertexCount">顶点 {{ (row.vertexCount/1000).toFixed(1) }}K</span>
                        <span v-if="row.faceCount">面 {{ (row.faceCount/1000).toFixed(1) }}K</span>
                        <span v-if="row.pointCount">点 {{ (row.pointCount/1000).toFixed(1) }}K</span>
                        <span>{{ row.primaryObjectCode || '' }}</span>
                    </div>
                </div>
            </div>
        </div>

        <el-empty v-if="!list.length && !loading" description="暂无三维模型" :image-size="80" style="padding:60px 0">
            <el-button type="primary" @click="router.push('/archive/files')">进入文件资源库上传模型</el-button>
        </el-empty>
        <el-pagination v-if="total > q.pageSize" v-model:current-page="q.pageNum" :page-size="q.pageSize" :total="total" layout="total, prev, pager, next" background style="margin-top:16px;justify-content:flex-end" @current-change="loadList" />
    </div>

    <!-- 详情 Drawer -->
    <el-drawer v-model="detailDrawer" title="三维模型详情" size="680px">
        <template v-if="activeModel">
            <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="资源名称">{{ activeModel.resourceName }}</el-descriptions-item>
                <el-descriptions-item label="模型标题">{{ activeModel.modelTitle || '-' }}</el-descriptions-item>
                <el-descriptions-item label="模型类型">{{ typeMap[activeModel.modelType] || activeModel.modelType || '-' }}</el-descriptions-item>
                <el-descriptions-item label="模型阶段">{{ stageMap[activeModel.modelStage] || activeModel.modelStage || '-' }}</el-descriptions-item>
                <el-descriptions-item label="模型格式">{{ activeModel.modelFormat || activeModel.fileExtension || '-' }}</el-descriptions-item>
                <el-descriptions-item label="文件大小">{{ formatSize(activeModel.fileSize) }}</el-descriptions-item>
                <el-descriptions-item label="比例单位">{{ activeModel.scaleUnit || '-' }}</el-descriptions-item>
                <el-descriptions-item label="真实比例">{{ activeModel.realScale ? '是' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="顶点数">{{ activeModel.vertexCount ? (activeModel.vertexCount/1000).toFixed(1)+'K' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="面数">{{ activeModel.faceCount ? (activeModel.faceCount/1000).toFixed(1)+'K' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="点数量">{{ activeModel.pointCount ? (activeModel.pointCount/1000).toFixed(1)+'K' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="纹理数">{{ activeModel.textureCount || '-' }}</el-descriptions-item>
                <el-descriptions-item label="质量等级">{{ activeModel.qualityLevel || '-' }}</el-descriptions-item>
                <el-descriptions-item label="元数据状态">{{ activeModel.metadataStatus || '-' }}</el-descriptions-item>
                <el-descriptions-item label="主要对象">{{ activeModel.primaryObjectCode }} {{ activeModel.primaryObjectName || '' }}</el-descriptions-item>
                <el-descriptions-item label="采集方法">{{ activeModel.acquisitionMethod || '-' }}</el-descriptions-item>
                <el-descriptions-item label="采集设备">{{ activeModel.acquisitionDevice || '-' }}</el-descriptions-item>
                <el-descriptions-item label="处理软件">{{ activeModel.processingSoftware || '-' }}</el-descriptions-item>
                <el-descriptions-item label="采集时间">{{ activeModel.acquisitionDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="模型说明" :span="2">{{ activeModel.modelDescription || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top:12px;display:flex;gap:8px">
                <el-button type="primary" size="small" @click="goViewer(activeModel)">打开查看器</el-button>
                <el-button size="small" @click="openResource(activeModel)">文件资源库</el-button>
            </div>
        </template>
    </el-drawer>
</template>

<style scoped>
.model-page { padding: 0; }
.summary-bar { display: flex; gap: 12px; margin-bottom: 14px; flex-wrap: wrap; }
.summary-item { flex: 1; min-width: 90px; padding: 12px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; text-align: center; transition: box-shadow .15s; }
.summary-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.si-icon { font-size: 22px; } .si-num { font-size: 18px; font-weight: 700; color: #1D2129; } .si-label { font-size: 11px; color: #909399; }
.filter-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
.model-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .15s; }
.model-card:hover { box-shadow: 0 2px 10px rgba(0,0,0,0.08); }
.mc-thumb { height: 180px; background: #F5F7FA; position: relative; }
.mc-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; }
.mc-body { padding: 10px 12px; }
.mc-name { font-size: 13px; font-weight: 600; color: #1D2129; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mc-meta { display: flex; gap: 6px; align-items: center; margin: 4px 0; font-size: 11px; color: #C0C4CC; }
.mc-stats { display: flex; gap: 8px; font-size: 11px; color: #909399; }
@media (max-width: 1200px) { .card-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
