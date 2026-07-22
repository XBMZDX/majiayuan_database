<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, PictureFilled, VideoCamera, Headset } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const router = useRouter()
const loading = ref(false); const detailDrawer = ref(false); const previewVisible = ref(false)
const list = ref([]); const total = ref(0); const summary = ref({}); const activeMedia = ref(null)
const viewMode = ref('gallery'); const detailTab = ref('info')

const q = reactive({ keyword: '', mediaType: '', mediaStage: '', objectType: '', objectId: null, shootingPart: '', pageNum: 1, pageSize: 20 })
const stageMap = { exploration: '勘探', excavation: '发掘', unearthed: '出土', cleaning_before: '清理前', cleaning: '清理中', cleaning_after: '清理后', detection: '检测分析', sampling: '取样', disease_survey: '病害调查', conservation_before: '修复前', conservation_process: '修复中', conservation_after: '修复后', restoration: '复原', display: '展示', monitoring: '监测', other: '其他' }
const formatSize = (bytes) => { if (!bytes || bytes === 0) return '0 B'; const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes; while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }; return v.toFixed(1) + ' ' + units[i] }

const loadSummary = async () => { const r = await request.get('/api/digital-media/summary').catch(() => ({})); summary.value = r.data || {} }
const loadList = async () => {
    loading.value = true
    const r = await request.get('/api/digital-media', { params: q }).catch(() => ({ data: { records: [], total: 0 } }))
    list.value = r.data?.records || []; total.value = r.data?.total || 0
    loading.value = false
}
const handleSearch = () => { q.pageNum = 1; loadList() }

const openDetail = async (row) => {
    const r = await request.get('/api/digital-media/' + row.resourceId)
    activeMedia.value = r.data || {}; detailDrawer.value = true
}
const openPreview = (row) => { activeMedia.value = row; previewVisible.value = true }

// 阶段分组
const stageGroups = ref([])
const loadStageGroups = async () => { const r = await request.get('/api/digital-media/stage-groups'); stageGroups.value = r.data || [] }

onMounted(() => { loadSummary(); loadList(); loadStageGroups() })
</script>

<template>
    <div class="media-page" v-loading="loading">
        <!-- 统计 -->
        <div class="summary-bar">
            <div class="summary-item" v-for="s in [
                {label:'媒体总数',value:summary.totalCount,icon:'📦'},{label:'图片',value:summary.imageCount,icon:'🖼'},{label:'视频',value:summary.videoCount,icon:'🎬'},{label:'音频',value:summary.audioCount,icon:'🎵'},{label:'关键影像',value:summary.keyMediaCount,icon:'⭐'},{label:'待完善',value:summary.incompleteCount,icon:'⚠'}
            ]" :key="s.label" @click="s.label==='图片'?(q.mediaType='image',handleSearch()):s.label==='视频'?(q.mediaType='video',handleSearch()):null">
                <div class="si-icon">{{ s.icon }}</div><div class="si-num">{{ s.value ?? 0 }}</div><div class="si-label">{{ s.label }}</div>
            </div>
        </div>

        <!-- 筛选 -->
        <div class="filter-bar">
            <el-input v-model="q.keyword" placeholder="搜索名称/对象..." :prefix-icon="Search" clearable style="width:200px" @keyup.enter="handleSearch" />
            <el-select v-model="q.mediaType" placeholder="类型" clearable style="width:100px" @change="handleSearch"><el-option label="全部" value="" /><el-option label="图片" value="image" /><el-option label="视频" value="video" /><el-option label="音频" value="audio" /></el-select>
            <el-select v-model="q.mediaStage" placeholder="阶段" clearable style="width:120px" @change="handleSearch"><el-option label="全部" value="" /><el-option v-for="(v,k) in stageMap" :key="k" :label="v" :value="k" /></el-select>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="loadList" />
            <span style="flex:1" />
            <el-radio-group v-model="viewMode" size="small"><el-radio-button value="gallery">图片墙</el-radio-button><el-radio-button value="list">列表</el-radio-button></el-radio-group>
        </div>

        <!-- 图片墙 -->
        <div v-if="viewMode==='gallery'" class="gallery">
            <div v-for="row in list" :key="row.resourceId" class="gallery-item" @click="openDetail(row)">
                <div class="gi-thumb">
                    <el-image v-if="row.thumbnailUrl" :src="row.thumbnailUrl" fit="cover" style="height:180px" />
                    <div v-else class="gi-placeholder">{{ row.mediaType === 'video' ? '🎬' : row.mediaType === 'audio' ? '🎵' : '🖼' }}</div>
                    <el-tag v-if="row.keyMedia" size="small" type="warning" effect="dark" style="position:absolute;top:6px;left:6px">⭐</el-tag>
                </div>
                <div class="gi-info">
                    <div class="gi-name">{{ row.mediaTitle || row.resourceName || '-' }}</div>
                    <div class="gi-meta"><el-tag size="small">{{ stageMap[row.mediaStage] || row.mediaStage || '-' }}</el-tag><span>{{ row.primaryObjectCode }}</span></div>
                </div>
            </div>
        </div>

        <!-- 列表 -->
        <el-table v-else :data="list" size="small" @row-dblclick="openDetail" highlight-current-row>
            <el-table-column label="预览" width="60"><template #default="{row}"><el-image v-if="row.thumbnailUrl" :src="row.thumbnailUrl" fit="cover" style="width:36px;height:36px;border-radius:4px;cursor:pointer" @click="openPreview(row)" /></template></el-table-column>
            <el-table-column label="名称" min-width="150" show-overflow-tooltip><template #default="{row}">{{ row.mediaTitle || row.resourceName || '-' }}</template></el-table-column>
            <el-table-column label="类型" width="70"><template #default="{row}">{{ row.mediaType==='image'?'图片':row.mediaType==='video'?'视频':'音频' }}</template></el-table-column>
            <el-table-column label="阶段" width="90"><template #default="{row}"><el-tag size="small">{{ stageMap[row.mediaStage] || row.mediaStage || '-' }}</el-tag></template></el-table-column>
            <el-table-column label="部位" width="80"><template #default="{row}">{{ row.shootingPart || '-' }}</template></el-table-column>
            <el-table-column label="对象" width="100"><template #default="{row}">{{ row.primaryObjectCode }} {{ row.primaryObjectName || '' }}</template></el-table-column>
            <el-table-column label="时间" width="100"><template #default="{row}">{{ (row.shootingDate||row.uploadTime||'').substring(0,10) }}</template></el-table-column>
            <el-table-column label="操作" width="60"><template #default="{row}"><el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button></template></el-table-column>
        </el-table>

        <el-empty v-if="!list.length && !loading" description="暂无媒体资源" :image-size="80" style="padding:60px 0" />
        <el-pagination v-if="total > q.pageSize" v-model:current-page="q.pageNum" :page-size="q.pageSize" :total="total" layout="total, prev, pager, next" background style="margin-top:16px;justify-content:flex-end" @current-change="loadList" />
    </div>

    <!-- 详情 Drawer -->
    <el-drawer v-model="detailDrawer" title="媒体详情" size="680px">
        <template v-if="activeMedia">
            <el-tabs v-model="detailTab">
                <el-tab-pane label="预览" name="preview">
                    <div style="text-align:center">
                        <el-image v-if="activeMedia.previewUrl" :src="activeMedia.previewUrl" fit="contain" style="max-height:400px" :preview-src-list="[activeMedia.previewUrl]" />
                        <video v-else-if="activeMedia.mediaType==='video' && activeMedia.previewUrl" :src="activeMedia.previewUrl" controls style="max-width:100%;max-height:400px" />
                        <el-empty v-else description="暂无预览" :image-size="60" />
                    </div>
                </el-tab-pane>
                <el-tab-pane label="基本信息" name="info">
                    <el-descriptions :column="2" size="small" border>
                        <el-descriptions-item label="资源名称">{{ activeMedia.resourceName }}</el-descriptions-item>
                        <el-descriptions-item label="原始文件">{{ activeMedia.originalFileName }}</el-descriptions-item>
                        <el-descriptions-item label="类型">{{ activeMedia.mediaType }}</el-descriptions-item>
                        <el-descriptions-item label="大小">{{ formatSize(activeMedia.fileSize) }}</el-descriptions-item>
                        <el-descriptions-item label="上传时间">{{ activeMedia.uploadTime }}</el-descriptions-item>
                    </el-descriptions>
                </el-tab-pane>
            </el-tabs>
        </template>
    </el-drawer>
</template>

<style scoped>
.media-page { padding: 0; }
.summary-bar { display: flex; gap: 12px; margin-bottom: 14px; flex-wrap: wrap; }
.summary-item { flex: 1; min-width: 90px; padding: 12px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; text-align: center; transition: box-shadow .15s; }
.summary-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.si-icon { font-size: 22px; } .si-num { font-size: 18px; font-weight: 700; color: #1D2129; } .si-label { font-size: 11px; color: #909399; }
.filter-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.gallery { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.gallery-item { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .15s; }
.gallery-item:hover { box-shadow: 0 2px 10px rgba(0,0,0,0.08); }
.gi-thumb { height: 180px; background: #F5F7FA; position: relative; }
.gi-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 40px; }
.gi-info { padding: 10px 12px; }
.gi-name { font-size: 13px; font-weight: 600; color: #1D2129; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.gi-meta { display: flex; gap: 8px; align-items: center; margin-top: 4px; font-size: 11px; color: #C0C4CC; }
@media (max-width: 1200px) { .gallery { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .gallery { grid-template-columns: repeat(2, 1fr); } }
</style>
