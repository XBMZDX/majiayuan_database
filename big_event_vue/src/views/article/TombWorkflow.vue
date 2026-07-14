<script setup>
import { ref, computed, onMounted } from 'vue'
import { Edit, Delete, Document, PictureFilled, VideoCameraFilled, Headset, FolderOpened, Files } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useTokenStore } from '@/stores/token.js'
const token = computed(() => useTokenStore().token)

// ========== 墓葬下拉选择 ==========
const burialList = ref([])
const selectedBurialId = ref(null)
const fetchBurialList = async () => { const res = await request.get('/admin/burial/list/simple'); burialList.value = res.data || [] }
// ========== 流程树（左侧）—— 从 API 加载 ==========
const treeData = ref([])

const fetchTree = async () => { if (!selectedBurialId.value) return; const res = await request.get('/admin/workflow/tree', {params:{burialId:selectedBurialId.value}}); treeData.value = (res.data || []).map(t => ({ id: t.id, label: t.label })) }
const saveTreeToDB = async () => { if (!selectedBurialId.value) return; await request.put('/admin/workflow/tree', { burialId: selectedBurialId.value, nodes: treeData.value }) }

const selectedNode = ref(null)
const onNodeClick = (node) => { selectedNode.value = selectedNode.value?.id === node.id ? null : node }

// 11 种流程颜色
const flowColors = ['#409EFF','#67C23A','#E6A23C','#F56C6C','#909399','#B37FEB','#36CFC9','#FF85C0','#FFC069','#95DE64','#5CDBD3']
const getFlowColor = (flowId) => flowColors[(flowId - 1) % flowColors.length]

// 全时间轴排序（跨流程）
const sortedTimeline = computed(() => [...timelineData.value].sort((a, b) => (a.date || '').localeCompare(b.date || '')))

// 按流程分组，同时记录每个事件在全排序中的位置
const timelineGroups = computed(() => {
    const groups = {}
    treeData.value.forEach(t => { groups[t.id] = { label: t.label, events: [] } })
    sortedTimeline.value.forEach((e, globalIdx) => {
        const evt = { ...e, globalIdx }
        if (groups[e.flowId]) groups[e.flowId].events.push(evt)
        else { groups[e.flowId] = { label: '流程' + e.flowId, events: [evt] } }
    })
    return Object.entries(groups).filter(([, g]) => g.events.length > 0).map(([id, g]) => ({ flowId: parseInt(id), ...g }))
})

// 时间刻度：计算全局起止日期
const timeRange = computed(() => {
    if (!sortedTimeline.value.length) return { min: '2022', max: '2030', days: 2922 }
    const dates = sortedTimeline.value.map(e => new Date(e.date)).filter(d => !isNaN(d))
    if (!dates.length) return { min: '2022', max: '2030', days: 2922 }
    const min = new Date(Math.min(...dates))
    const max = new Date(Math.max(...dates))
    // 扩展边界
    min.setMonth(min.getMonth() - 1)
    max.setMonth(max.getMonth() + 1)
    return { min: min.toISOString().slice(0, 10), max: max.toISOString().slice(0, 10), days: Math.max((max - min) / 86400000, 1) }
})

// 日期 → 时间轴百分比位置
const getTimePercent = (dateStr) => {
    if (!dateStr || !timeRange.value.days) return 0
    const d = new Date(dateStr)
    const min = new Date(timeRange.value.min)
    return Math.max(0, Math.min(100, ((d - min) / 86400000) / timeRange.value.days * 100))
}

// 生成刻度标签（每 N 个月一个）
const scaleTicks = computed(() => {
    if (!timeRange.value.days) return []
    const ticks = []
    const min = new Date(timeRange.value.min)
    const max = new Date(timeRange.value.max)
    const months = Math.ceil(timeRange.value.days / 30)
    const step = Math.max(1, Math.ceil(months / 10))
    let cur = new Date(min.getFullYear(), min.getMonth(), 1)
    while (cur <= max) {
        ticks.push({ label: cur.getFullYear() + '-' + String(cur.getMonth() + 1).padStart(2, '0'), pct: getTimePercent(cur.toISOString().slice(0, 10)) })
        cur.setMonth(cur.getMonth() + step)
    }
    return ticks
})

// ========== 流程树编辑弹窗 ==========
const editDialogVisible = ref(false)
const dialogNodes = ref([])
const dialogNewLabel = ref('')

const openEditDialog = () => { dialogNodes.value = treeData.value.map(n => ({ ...n })); editDialogVisible.value = true }
const dialogAdd = () => { if (!dialogNewLabel.value.trim()) return; const maxId = Math.max(...dialogNodes.value.map(n => n.id), 0); dialogNodes.value.push({ id: maxId + 1, label: dialogNewLabel.value.trim() }); dialogNewLabel.value = '' }
const dialogRemove = (id) => { dialogNodes.value = dialogNodes.value.filter(n => n.id !== id) }
const dialogMoveUp = (idx) => { if (idx > 0) { const t = dialogNodes.value[idx]; dialogNodes.value[idx] = dialogNodes.value[idx-1]; dialogNodes.value[idx-1] = t } }
const dialogMoveDown = (idx) => { if (idx < dialogNodes.value.length - 1) { const t = dialogNodes.value[idx]; dialogNodes.value[idx] = dialogNodes.value[idx+1]; dialogNodes.value[idx+1] = t } }
const saveDialog = () => {
    // 找到被删除的流程ID，清除其时间轴事件
    const newIds = new Set(dialogNodes.value.map(n => n.id))
    timelineData.value = timelineData.value.filter(e => newIds.has(e.flowId))
    treeData.value = dialogNodes.value.map(n => ({ ...n })); editDialogVisible.value = false; selectedNode.value = null
    saveTreeToDB(); request.put('/admin/workflow/timeline', { burialId: selectedBurialId.value, items: timelineData.value })
}
const cancelDialog = () => { editDialogVisible.value = false }

// ========== 时间轴编辑弹窗 ==========
const timelineEditVisible = ref(false)
const dialogTimeline = ref([])
const newTitle = ref(''); const newDate = ref(''); const newStatus2 = ref('pending'); const newFlowId = ref(null)

const openTimelineEdit = () => {
    dialogTimeline.value = timelineData.value.map(t => ({ ...t }))
    timelineEditVisible.value = true
}
const tlAdd = () => {
    if (!newTitle.value.trim() || !newDate.value) return
    dialogTimeline.value.push({ date: newDate.value, title: newTitle.value.trim(), status: newStatus2.value, flowId: newFlowId.value || treeData.value[0]?.id || 1 })
    newTitle.value = ''; newDate.value = ''; newStatus2.value = 'pending'; newFlowId.value = null
}
const tlRemove = (idx) => { dialogTimeline.value.splice(idx, 1) }
const saveTimeline = () => {
    if (newTitle.value.trim() && newDate.value) tlAdd()
    dialogTimeline.value.sort((a, b) => (a.date || '').localeCompare(b.date || ''))
    timelineData.value = [...dialogTimeline.value.map(t => ({ ...t }))]; timelineEditVisible.value = false; selectedEvent.value = null
    // 保存到数据库
    request.put('/admin/workflow/timeline', { burialId: selectedBurialId.value, items: timelineData.value })
}

// ========== 时间轴（中间）—— 从 API 加载 ==========
const timelineData = ref([])
const fetchTimeline = async () => { if (!selectedBurialId.value) { timelineData.value = []; return }; const res = await request.get('/admin/workflow/timeline', {params:{burialId:selectedBurialId.value}}); timelineData.value = res.data || [] }

const selectedBurialName = computed(() => {
    const b = burialList.value.find(b => b.id === selectedBurialId.value)
    return b ? ((b.burialNo||'') + (b.name && b.name !== b.burialNo ? ' ' + b.name : '')) : '未选择'
})

const onBurialChange = () => { fetchTree(); fetchTimeline() }

onMounted(async () => { await fetchBurialList(); if (burialList.value.length > 0) { selectedBurialId.value = burialList.value[0].id; fetchTree(); fetchTimeline() } })

const selectedEvent = ref(null)
const fullArchiveVisible = ref(false)
const showAddDialog = ref(false)
const addType = ref('note')
const addForm2 = ref({ date: '', content: '', fileUrl: '', fileName: '', artifactCode: '' })

const submitAddContent = async () => {
    if (!selectedEvent.value) return
    if (addType.value === 'note') {
        if (!addForm2.value.content.trim()) return
        let content = addForm2.value.content
        if (addForm2.value.date) content = '[' + addForm2.value.date + '] ' + content
        await request.post('/admin/workflow/note', { timelineId: selectedEvent.value.id, noteType: '工作记录', content })
    } else {
        // 根据文件扩展名自动判断媒体类型
        let mediaType = addType.value === 'image' ? 'image' : 'file'
        const ext = (addForm2.value.fileName || '').split('.').pop()?.toLowerCase()
        if (['pdf','doc','docx'].includes(ext)) mediaType = 'document'
        else if (['xls','xlsx'].includes(ext)) mediaType = 'spreadsheet'
        else if (['dwg'].includes(ext)) mediaType = 'dwg'
        else if (['zip','rar'].includes(ext)) mediaType = 'archive'
        await request.post('/admin/workflow/media', { timelineId: selectedEvent.value.id, mediaType, fileName: addForm2.value.fileName, fileUrl: addForm2.value.fileUrl })
    }
    showAddDialog.value = false
    addForm2.value = { date: '', content: '', fileUrl: '', fileName: '', artifactCode: '' }
    loadArchiveData(selectedEvent.value.id)
}

const archiveTab = ref('notes')
const deleteMode = ref(false)
const archiveNotes = ref([])
const archiveImages = ref([])
const archiveFiles = ref([])

const onEventClick = (event) => { selectedEvent.value = event; loadArchiveData(event.id) }
const onEventDblClick = (event) => { selectedEvent.value = event; fullArchiveVisible = true; loadArchiveData(event.id) }

const loadArchiveData = async (timelineId) => {
    if (!timelineId) return
    const [notesRes, mediaRes] = await Promise.all([
        request.get('/admin/workflow/note/' + timelineId),
        request.get('/admin/workflow/media/' + timelineId)
    ])
    archiveNotes.value = notesRes.data || []
    const allMedia = mediaRes.data || []
    archiveImages.value = allMedia.filter(m => m.mediaType === 'image')
    archiveFiles.value = allMedia.filter(m => m.mediaType !== 'image')
}

// 备注管理
const newNote = ref({ noteType: '', content: '' })
const addNote = async () => {
    if (!newNote.value.content.trim() || !selectedEvent.value) return
    await request.post('/admin/workflow/note', { timelineId: selectedEvent.value.id, ...newNote.value })
    newNote.value = { noteType: '', content: '' }; loadArchiveData(selectedEvent.value.id)
}
const getNoteTime = (n) => { const m = (n.content || '').match(/^\[(.+?)\]/); return m ? m[1] : n.createTime }
const getNoteContent = (n) => (n.content || '').replace(/^\[.+?\]\s*/, '')
const deleteNote = async (id) => { await request.delete('/admin/workflow/note/' + id); loadArchiveData(selectedEvent.value?.id) }
const deleteMedia = async (id) => { await request.delete('/admin/workflow/media/' + id); loadArchiveData(selectedEvent.value?.id) }

// ========== 文件预览 ==========
const iconMap = { Document, PictureFilled, VideoCameraFilled, Headset, FolderOpened, Files }
const previewVisible = ref(false)
const previewFile = ref(null)

const getFileExt = (fileName) => (fileName || '').split('.').pop()?.toLowerCase() || ''

const getFileIcon = (ext) => {
    const m = {
        pdf:    { icon: iconMap.Document,          color: '#E74C3C' },
        doc:    { icon: iconMap.Document,          color: '#2980B9' },
        docx:   { icon: iconMap.Document,          color: '#2980B9' },
        xls:    { icon: iconMap.Document,          color: '#27AE60' },
        xlsx:   { icon: iconMap.Document,          color: '#27AE60' },
        ppt:    { icon: iconMap.Document,          color: '#E67E22' },
        pptx:   { icon: iconMap.Document,          color: '#E67E22' },
        jpg:    { icon: iconMap.PictureFilled,     color: '#E67E22' },
        jpeg:   { icon: iconMap.PictureFilled,     color: '#E67E22' },
        png:    { icon: iconMap.PictureFilled,     color: '#E67E22' },
        gif:    { icon: iconMap.PictureFilled,     color: '#E67E22' },
        bmp:    { icon: iconMap.PictureFilled,     color: '#E67E22' },
        mp4:    { icon: iconMap.VideoCameraFilled, color: '#8E44AD' },
        webm:   { icon: iconMap.VideoCameraFilled, color: '#8E44AD' },
        avi:    { icon: iconMap.VideoCameraFilled, color: '#8E44AD' },
        mov:    { icon: iconMap.VideoCameraFilled, color: '#8E44AD' },
        mp3:    { icon: iconMap.Headset,           color: '#16A085' },
        wav:    { icon: iconMap.Headset,           color: '#16A085' },
        flac:   { icon: iconMap.Headset,           color: '#16A085' },
        aac:    { icon: iconMap.Headset,           color: '#16A085' },
        zip:    { icon: iconMap.FolderOpened,      color: '#7F8C8D' },
        rar:    { icon: iconMap.FolderOpened,      color: '#7F8C8D' },
        '7z':   { icon: iconMap.FolderOpened,      color: '#7F8C8D' },
        dwg:    { icon: iconMap.Files,             color: '#C0392B' },
        dxf:    { icon: iconMap.Files,             color: '#C0392B' },
    }
    return m[ext] || { icon: iconMap.Files, color: '#95A5A6' }
}

const getPreviewType = (fileName) => {
    const ext = getFileExt(fileName)
    if (['pdf','doc','docx','xls','xlsx','ppt','pptx'].includes(ext)) return 'browser'
    if (['mp4','webm','avi','mov'].includes(ext)) return 'video'
    if (['mp3','wav','flac','aac'].includes(ext)) return 'audio'
    if (['jpg','jpeg','png','gif','bmp'].includes(ext)) return 'image'
    return 'unsupported'
}

const openPreview = (file) => {
    previewFile.value = file
    const type = getPreviewType(file.fileName)
    if (type === 'browser') { window.open(file.fileUrl, '_blank'); return }
    if (type === 'image') { window.open(file.fileUrl, '_blank'); return }
    previewVisible.value = true
}

const downloadFile = (file) => {
    const a = document.createElement('a')
    a.href = file.fileUrl
    a.download = file.fileName || ''
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
}

// 判断某流程是否有时间节点
const hasTimelineEvents = (flowId) => timelineData.value.some(e => e.flowId === flowId)

// ========== 详情（右侧） ==========
const detailData = computed(() => {
    if (!selectedEvent.value) return null
    return {
        title: selectedEvent.value.title,
        date: selectedEvent.value.date,
        flowId: selectedEvent.value.flowId,
        status: selectedEvent.value.status || 'pending',
        artifacts: '陶器12件、青铜器3件',
        photos: '现场照片45张、细节照片18张',
        reports: '地层记录1份、遗迹图2张',
    }
})
</script>

<template>
    <div class="workflow-page">
        <!-- 墓葬选择器 -->
        <div class="burial-selector">
            <el-select v-model="selectedBurialId" placeholder="请选择墓葬" style="width:340px" size="large" @change="onBurialChange">
                <el-option v-for="b in burialList" :key="b.id" :label="(b.burialNo||'') + (b.name && b.name !== b.burialNo ? ' ' + b.name : '')" :value="b.id" />
            </el-select>
        </div>

        <!-- 三栏主体 -->
        <div class="three-col">
            <!-- 左侧：流程树 -->
            <el-card class="col-left" shadow="never">
                <template #header>
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <span style="font-weight:600">流程树</span>
                        <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog">编辑</el-button>
                    </div>
                </template>
                <div class="flow-tree">
                    <div v-if="!treeData.length" style="text-align:center;padding:40px;color:#ccc;font-size:13px">请新建流程树</div>
                    <div v-for="(node, idx) in treeData" :key="node.id" class="flow-node" :class="{ selected: selectedNode && selectedNode.id === node.id }" @click="onNodeClick(node)">
                        <div class="flow-index" :style="hasTimelineEvents(node.id) ? {background: getFlowColor(node.id), color: '#fff'} : {}">{{ idx + 1 }}</div>
                        <div class="flow-label">{{ node.label }}</div>
                        <div v-if="idx < treeData.length - 1" class="flow-connector"></div>
                    </div>
                </div>
            </el-card>

            <!-- 中间：流程时间轴 -->
            <el-card class="col-center" shadow="never">
                <template #header>
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <span style="font-weight:600">流程时间轴</span>
                        <el-button type="primary" size="small" :icon="Edit" @click="openTimelineEdit">新增节点</el-button>
                    </div>
                </template>
                <div class="track-timeline">
                    <div v-if="!timelineGroups.length" style="text-align:center;padding:40px;color:#ccc">请新建时间轴</div>
                    <template v-else>
                        <div v-for="group in timelineGroups" :key="group.flowId" class="track-row">
                            <div class="track-label" :style="{color: getFlowColor(group.flowId)}">{{ group.label }}</div>
                            <div class="track-bar">
                                <div class="track-line" :style="{borderColor: getFlowColor(group.flowId)}"></div>
                                <div v-for="(evt, i) in group.events" :key="i" class="track-dot-wrap" :style="{left: getTimePercent(evt.date) + '%'}">
                                    <div class="track-dot" :style="{background: getFlowColor(group.flowId)}" @click="onEventClick(evt)" @dblclick="onEventDblClick(evt)" :title="evt.title + ' (' + evt.date + ')'"></div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
                <!-- 全局时间标尺 —— 卡片底部 -->
                <div v-if="timelineGroups.length" class="time-ruler">
                    <div v-for="(tick, i) in scaleTicks" :key="i" class="tick" :style="{left: tick.pct + '%'}">
                        <div class="tick-line"></div>
                        <div class="tick-label">{{ tick.label }}</div>
                    </div>
                </div>
            </el-card>

            <!-- 右侧：节点档案详情 -->
            <!-- 右侧：节点档案详情 -->
            <el-card class="col-right" shadow="never" @dblclick="detailData && (fullArchiveVisible = true)">
                <template #header><span style="font-weight:600">节点档案详情</span></template>
                <div v-if="detailData" class="detail-panel">
                    <!-- ① 基本信息 -->
                    <div class="detail-section">
                        <el-descriptions :column="1" border size="small">
                            <el-descriptions-item label="节点名称">{{ detailData.title }}</el-descriptions-item>
                            <el-descriptions-item label="所属流程">{{ treeData.find(t=>t.id===detailData.flowId)?.label || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="所属墓葬">{{ selectedBurialName }}</el-descriptions-item>
                            <el-descriptions-item label="节点时间">{{ detailData.date }}</el-descriptions-item>
                            <el-descriptions-item label="当前状态">
                                <el-tag :type="detailData.status === 'done' ? 'success' : detailData.status === 'doing' ? 'warning' : 'info'" size="small">{{ detailData.status === 'done' ? '已完成' : detailData.status === 'doing' ? '进行中' : '未开始' }}</el-tag>
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>

                    <!-- ② 最新工作记录 -->
                    <div class="detail-section">
                        <div class="section-title">最新工作记录<span class="section-more">查看更多 →</span></div>
                        <div v-if="!archiveNotes.length" class="note-card"><div style="color:#ccc;text-align:center;font-size:12px">暂无记录</div></div>
                        <div v-for="n in archiveNotes.slice(0,3)" :key="n.id" class="note-card" style="margin-bottom:6px">
                            <div class="note-time">{{ getNoteTime(n) }}</div>
                            <div class="note-content">{{ getNoteContent(n) }}</div>
                        </div>
                    </div>

                    <!-- ③ 最近图片 -->
                    <div class="detail-section">
                        <div class="section-title">最近图片</div>
                        <el-empty v-if="!archiveImages.length" description="暂无图片" :image-size="40" />
                        <div v-else class="image-grid">
                            <el-image v-for="(img, idx) in archiveImages.slice(0,4)" :key="img.id" :src="img.fileUrl" fit="cover" class="thumb-img" :preview-src-list="archiveImages.map(i=>i.fileUrl)" :initial-index="idx" />
                        </div>
                    </div>

                    <!-- ④ 底部提示 -->
                    <div class="detail-footer">双击查看完整档案</div>
                </div>
                <div v-else class="detail-empty">请选择时间轴节点查看档案</div>
            </el-card>
        </div>
    </div>

    <!-- 流程树编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑流程树" width="520px">
        <div v-for="(node, idx) in dialogNodes" :key="node.id" style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
            <span style="width:28px;text-align:center;color:#999;font-size:13px">{{ idx + 1 }}</span>
            <el-input v-model="node.label" size="small" style="flex:1" />
            <el-button size="small" circle @click="dialogMoveUp(idx)" :disabled="idx===0">↑</el-button>
            <el-button size="small" circle @click="dialogMoveDown(idx)" :disabled="idx===dialogNodes.length-1">↓</el-button>
            <el-button size="small" circle type="danger" @click="dialogRemove(node.id)">✕</el-button>
        </div>
        <div style="display:flex;gap:8px;margin-top:12px">
            <el-input v-model="dialogNewLabel" size="small" placeholder="新流程名称" @keyup.enter="dialogAdd" style="flex:1" />
            <el-button size="small" type="primary" @click="dialogAdd">新增</el-button>
        </div>
        <template #footer>
            <el-button @click="cancelDialog">取消</el-button>
            <el-button type="primary" @click="saveDialog">保存</el-button>
        </template>
    </el-dialog>

    <!-- 时间轴编辑弹窗 -->
    <el-dialog v-model="timelineEditVisible" title="编辑时间轴" width="680px" :close-on-click-modal="false">
        <div v-for="(item, idx) in dialogTimeline" :key="idx" style="display:flex;align-items:center;gap:6px;margin-bottom:8px;flex-wrap:wrap">
            <span style="width:22px;text-align:center;color:#999;font-size:12px">{{ idx + 1 }}</span>
            <el-date-picker v-model="item.date" type="date" size="small" style="width:120px" placeholder="日期" value-format="YYYY-MM-DD" />
            <el-input v-model="item.title" size="small" style="width:130px" placeholder="标题" />
            <el-select v-model="item.status" size="small" style="width:80px">
                <el-option label="已完成" value="done" /><el-option label="进行中" value="doing" /><el-option label="未开始" value="pending" />
            </el-select>
            <el-select v-model="item.flowId" size="small" style="width:120px" placeholder="关联流程">
                <el-option v-for="n in treeData" :key="n.id" :label="n.label" :value="n.id" />
            </el-select>
            <el-button size="small" circle type="danger" @click="tlRemove(idx)">✕</el-button>
        </div>
        <el-divider />
        <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center">
            <el-date-picker v-model="newDate" type="date" size="small" style="width:130px" placeholder="日期" value-format="YYYY-MM-DD" />
            <el-input v-model="newTitle" size="small" style="width:140px" placeholder="标题" />
            <el-select v-model="newStatus2" size="small" style="width:80px" placeholder="状态">
                <el-option label="已完成" value="done" /><el-option label="进行中" value="doing" /><el-option label="未开始" value="pending" />
            </el-select>
            <el-select v-model="newFlowId" size="small" style="width:120px" placeholder="关联流程" clearable>
                <el-option v-for="n in treeData" :key="n.id" :label="n.label" :value="n.id" />
            </el-select>
        </div>
        <template #footer><el-button @click="timelineEditVisible = false">取消</el-button><el-button type="primary" @click="saveTimeline">保存</el-button></template>
    </el-dialog>

    <!-- 完整档案对话框 -->
    <el-dialog v-model="fullArchiveVisible" title="节点档案" width="80%" top="5vh" :close-on-click-modal="false" @opened="loadArchiveData(selectedEvent?.id)" @closed="deleteMode = false">
        <div v-if="detailData" class="archive-dialog">
            <!-- 顶部固定信息 -->
            <el-descriptions :column="3" border size="small" class="archive-header">
                <el-descriptions-item label="节点名称">{{ detailData.title }}</el-descriptions-item>
                <el-descriptions-item label="所属流程">{{ treeData.find(t=>t.id===detailData.flowId)?.label || '-' }}</el-descriptions-item>
                <el-descriptions-item label="所属墓葬">{{ selectedBurialName }}</el-descriptions-item>
                <el-descriptions-item label="节点时间">{{ detailData.date }}</el-descriptions-item>
                <el-descriptions-item label="当前状态">
                    <el-tag :type="detailData.status==='done'?'success':detailData.status==='doing'?'warning':'info'" size="small">{{ detailData.status==='done'?'已完成':detailData.status==='doing'?'进行中':'未开始' }}</el-tag>
                </el-descriptions-item>
            </el-descriptions>
            <!-- 固定：工具栏 -->
            <div class="archive-toolbar">
                <el-button size="small" type="primary" @click="showAddDialog = true">新增</el-button>
                <el-button size="small" :type="deleteMode ? 'danger' : 'default'" :icon="Delete" @click="deleteMode = !deleteMode">{{ deleteMode ? '取消' : '删除' }}</el-button>
            </div>
            <!-- Tabs 填充区 -->
            <el-tabs v-model="archiveTab" class="archive-tabs" @tab-change="deleteMode = false">
                <el-tab-pane label="工作记录" name="notes">
                    <div class="tab-scroll">
                        <div v-for="n in archiveNotes" :key="n.id" style="display:flex;gap:12px;align-items:flex-start;padding:10px 0;border-bottom:1px solid #F0F0F0">
                            <div style="flex:1"><div style="font-size:12px;color:#999">{{ getNoteTime(n) }}</div><div style="font-weight:600;margin:4px 0">{{ n.noteType }}</div><div style="font-size:13px;color:#4E5969">{{ getNoteContent(n) }}</div></div>
                            <el-button size="small" type="danger" circle @click="deleteNote(n.id)">✕</el-button>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="图片档案" name="images">
                    <div class="tab-scroll">
                    <div v-if="!archiveImages.length" style="text-align:center;padding:40px;color:#ccc">暂无图片</div>
                    <div v-else class="archive-image-grid">
                        <div v-for="(img, idx) in archiveImages" :key="img.id" style="position:relative">
                            <el-image :src="img.fileUrl" fit="cover" style="height:150px;border-radius:6px;width:100%" :preview-src-list="archiveImages.map(i=>i.fileUrl)" :initial-index="idx" />
                            <el-button v-if="deleteMode" size="small" type="danger" circle style="position:absolute;top:4px;right:4px;z-index:1" @click="deleteMedia(img.id)">✕</el-button>
                        </div>
                    </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="附件资料" name="files">
                    <div class="tab-scroll table-wrapper">
                        <el-table :data="archiveFiles">
                        <el-table-column label="文件名">
                            <template #default="{row}">
                                <div style="display:flex;align-items:center;gap:6px">
                                    <el-icon :size="16" :color="getFileIcon(getFileExt(row.fileName)).color"><component :is="getFileIcon(getFileExt(row.fileName)).icon" /></el-icon>
                                    <el-link type="primary" :underline="false" class="file-link" @click="openPreview(row)">{{ row.fileName }}</el-link>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="mediaType" label="文件类型" />
                        <el-table-column prop="createTime" label="上传时间" />
                        <el-table-column v-if="deleteMode" label="操作">
                            <template #default="{row}">
                                <el-button size="small" type="danger" circle @click="deleteMedia(row.id)">✕</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </el-dialog>


    <!-- 文件预览对话框 -->
    <el-dialog v-model="previewVisible" :title="previewFile?.fileName || '文件预览'" width="700px" top="5vh" @closed="previewFile = null">
        <template v-if="previewFile">
            <!-- 视频播放 -->
            <div v-if="getPreviewType(previewFile.fileName) === 'video'" style="text-align:center">
                <video :src="previewFile.fileUrl" controls autoplay style="max-width:100%;max-height:60vh;border-radius:6px">您的浏览器不支持视频播放</video>
            </div>
            <!-- 音频播放 -->
            <div v-else-if="getPreviewType(previewFile.fileName) === 'audio'" style="text-align:center;padding:30px 0">
                <div style="margin-bottom:20px">
                    <el-icon :size="56" color="#16A085"><Headset /></el-icon>
                </div>
                <audio :src="previewFile.fileUrl" controls autoplay style="width:100%;max-width:500px">您的浏览器不支持音频播放</audio>
            </div>
            <!-- 不支持预览 -->
            <div v-else style="text-align:center;padding:40px 0">
                <el-icon :size="56" color="#C0C4CC"><Files /></el-icon>
                <p style="color:#909399;margin:16px 0;font-size:14px">该文件类型暂不支持在线预览，请下载后查看。</p>
                <el-button type="primary" @click="downloadFile(previewFile)">下载文件</el-button>
            </div>
        </template>
        <template #footer>
            <el-button @click="previewVisible = false">关闭</el-button>
            <el-button v-if="previewFile && getPreviewType(previewFile.fileName) !== 'unsupported'" type="primary" @click="downloadFile(previewFile)">下载</el-button>
        </template>
    </el-dialog>
    <!-- 新增内容对话框 -->
    <el-dialog v-model="showAddDialog" title="新增内容" width="500px">
        <el-radio-group v-model="addType" style="margin-bottom:12px">
            <el-radio-button value="note">工作记录</el-radio-button>
            <el-radio-button value="image">图片档案</el-radio-button>
            <el-radio-button value="file">附件资料</el-radio-button>
        </el-radio-group>
        <template v-if="addType === 'note'">
            <el-form-item label="时间"><el-date-picker v-model="addForm2.date" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item>
            <el-form-item label="内容"><el-input v-model="addForm2.content" type="textarea" :rows="4" placeholder="请输入工作描述" /></el-form-item>
        </template>
        <template v-else-if="addType === 'image' || addType === 'file'">
            <el-upload class="upload-demo" action="/api/upload" name="file"
                :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true"
                :on-success="(res) => { addForm2.fileUrl = res.data }"
                :before-upload="(file) => { addForm2.fileName = file.name; return true }"
                :accept="addType === 'image' ? 'image/*' : '.pdf,.doc,.docx,.xls,.xlsx,.dwg,.zip,.rar'">
                <el-button type="primary">选择文件</el-button>
            </el-upload>
            <el-form-item v-if="addForm2.fileName" label="文件名"><span style="font-size:13px">{{ addForm2.fileName }}</span></el-form-item>
        </template>
        <template #footer><el-button @click="showAddDialog = false">取消</el-button><el-button type="primary" @click="submitAddContent">确定</el-button></template>
    </el-dialog>
</template>

<style scoped>
.workflow-page { padding: 0; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.burial-selector { padding-bottom: 12px; border-bottom: 1px solid #E5E6EB; margin-bottom: 12px; }
.three-col { display: flex; gap: 12px; flex: 1; min-height: 0; }
.col-left { flex: 0 0 220px; display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; :deep(.el-card__body) { flex: 1; overflow-y: auto; } }
.flow-tree { padding: 4px 0; }
.flow-node { position: relative; display: flex; align-items: flex-start; padding: 12px 16px 12px 12px; cursor: pointer; transition: background .15s; }
.flow-node:hover { background: #F7F8FA; }
.flow-node.selected { background: #ECF5FF; }
.flow-index { width: 24px; height: 24px; border-radius: 50%; background: #E5E6EB; color: #666; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-right: 8px; }
.flow-node.selected .flow-index { box-shadow: 0 0 0 3px rgba(22,104,196,.3); }
.flow-connector { position: absolute; left: 11px; top: 36px; width: 1px; height: 24px; background: #E5E6EB; }
.flow-label { font-size: 13px; color: #1D2129; line-height: 24px; }
.col-center { flex: 1; display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; :deep(.el-card__body) { flex: 1; overflow-y: auto; } }
.track-timeline { padding: 8px 0 16px 0; }
.track-row { display: flex; align-items: center; margin-bottom: 6px; }
.track-label { flex: 0 0 90px; font-size: 12px; font-weight: 600; text-align: right; padding-right: 8px; line-height: 1.3; margin-left: -8px; }
.track-bar { flex: 1; position: relative; height: 48px; margin-left: -4px; }
.track-line { position: absolute; top: 24px; left: 0; right: 0; border-top: 2px solid #E5E6EB; }
.track-dot-wrap { position: absolute; top: 16px; transform: translateX(-50%); display: flex; flex-direction: column; align-items: center; }
.track-dot { width: 12px; height: 12px; border-radius: 50%; cursor: pointer; transition: transform .15s; border: 2px solid #fff; box-shadow: 0 0 0 2px currentColor; }
.track-dot:hover { transform: scale(1.3); }
.track-event-title { font-size: 10px; color: #1D2129; text-align: center; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; }
.track-date { font-size: 10px; color: #999; margin-top: 2px; text-align: center; white-space: nowrap; }
.time-ruler { position: relative; height: 24px; margin: 8px 16px 0 80px; padding-top: 4px; border-top: 1px solid #DCDFE6; }
.tick { position: absolute; top: 0; transform: translateX(-50%); }
.tick-line { width: 1px; height: 8px; background: #C0C4CC; margin: 0 auto; }
.tick-label { font-size: 10px; color: #909399; margin-top: 2px; white-space: nowrap; }
.col-right { flex: 0 0 260px; display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; :deep(.el-card__body) { flex: 1; overflow-y: auto; } }
.detail-panel { font-size: 13px; }
.detail-section { margin-bottom: 16px; }
.section-title { font-size: 13px; font-weight: 600; color: #1D2129; margin-bottom: 8px; display: flex; justify-content: space-between; }
.section-more { font-size: 11px; color: #409EFF; cursor: pointer; font-weight: 400; }
.note-card { background: #F7F8FA; border-radius: 6px; padding: 10px 12px; }
.note-time { font-size: 11px; color: #999; margin-bottom: 4px; }
.note-content { font-size: 12px; color: #4E5969; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.stat-item { background: #F7F8FA; border-radius: 6px; padding: 12px; text-align: center; transition: box-shadow .15s; cursor: default; }
.stat-item:hover { box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
.sn { font-size: 22px; font-weight: 700; color: #1D2129; }
.sl2 { font-size: 11px; color: #999; margin-top: 2px; }
.image-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.thumb-img { width: 100%; height: 60px; border-radius: 4px; object-fit: cover; cursor: pointer; }
.detail-footer { font-size: 12px; color: #909399; text-align: center; margin-top: 12px; padding-top: 8px; border-top: 1px solid #F0F0F0; }
.detail-empty { text-align: center; padding: 60px 0; color: #ccc; font-size: 13px; }

/* ========== 完整档案对话框 ========== */
.archive-dialog { height: 75vh; display: flex; flex-direction: column; overflow: hidden; }
.archive-header { flex-shrink: 0; margin-bottom: 12px; }
.archive-toolbar { flex-shrink: 0; display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.archive-tabs { flex: 1; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }
.archive-tabs :deep(.el-tabs__header) { flex-shrink: 0; margin-bottom: 8px; }
.archive-tabs :deep(.el-tabs__content) { flex: 1; min-height: 0; overflow: hidden; }
.archive-tabs :deep(.el-tab-pane) { height: 100%; }
.tab-scroll { height: 100%; overflow-y: auto; }
.table-wrapper { width: 100%; overflow-x: auto; }
.archive-image-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 8px; }
.file-link { cursor: pointer; }
.file-link:hover { opacity: 0.8; text-decoration: underline; }
</style>
