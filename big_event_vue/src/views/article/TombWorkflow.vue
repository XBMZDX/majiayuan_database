<script setup>
import { ref, computed, onMounted } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

// ========== 统计卡片 ==========
const stats = ref({ total: 8, done: 3, doing: 4, pending: 1, materials: 156, photos: 420, reports: 12, artifacts: 89 })

// ========== 流程树（左侧）—— 从 API 加载 ==========
const treeData = ref([])

const fetchTree = async () => { const res = await request.get('/admin/workflow/tree'); treeData.value = (res.data || []).map(t => ({ id: t.id, label: t.label })) }
const saveTreeToDB = async () => { await request.put('/admin/workflow/tree', treeData.value) }

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
    saveTreeToDB(); request.put('/admin/workflow/timeline', timelineData.value)
}
const cancelDialog = () => { editDialogVisible.value = false }

// ========== 时间轴编辑弹窗 ==========
const timelineEditVisible = ref(false)
const dialogTimeline = ref([])
const newTitle = ref(''); const newDate = ref(''); const newFlowId = ref(null)

const openTimelineEdit = () => {
    dialogTimeline.value = timelineData.value.map(t => ({ ...t }))
    timelineEditVisible.value = true
}
const tlAdd = () => {
    if (!newTitle.value.trim() || !newDate.value) return
    dialogTimeline.value.push({ date: newDate.value, title: newTitle.value.trim(), flowId: newFlowId.value || treeData.value[0]?.id || 1 })
    newTitle.value = ''; newDate.value = ''; newFlowId.value = null
}
const tlRemove = (idx) => { dialogTimeline.value.splice(idx, 1) }
const saveTimeline = () => {
    if (newTitle.value.trim() && newDate.value) tlAdd()
    dialogTimeline.value.sort((a, b) => (a.date || '').localeCompare(b.date || ''))
    timelineData.value = [...dialogTimeline.value.map(t => ({ ...t }))]; timelineEditVisible.value = false; selectedEvent.value = null
    // 保存到数据库
    request.put('/admin/workflow/timeline', timelineData.value)
}

// ========== 时间轴（中间）—— 从 API 加载 ==========
const timelineData = ref([])
const fetchTimeline = async () => { const res = await request.get('/admin/workflow/timeline'); timelineData.value = res.data || [] }

onMounted(() => { fetchTree(); fetchTimeline() })

const selectedEvent = ref(null)
const onEventClick = (event) => { selectedEvent.value = event }

// 判断某流程是否有时间节点
const hasTimelineEvents = (flowId) => timelineData.value.some(e => e.flowId === flowId)

// ========== 详情（右侧） ==========
const detailData = computed(() => {
    if (!selectedEvent.value) return null
    return {
        title: selectedEvent.value.title,
        date: selectedEvent.value.date,
        artifacts: '陶器12件、青铜器3件',
        photos: '现场照片45张、细节照片18张',
        reports: '地层记录1份、遗迹图2张',
        personnel: '张三（负责人）、李四、王五',
    }
})
</script>

<template>
    <div class="workflow-page">
        <!-- 顶部统计卡片 -->
        <div class="stats-row">
            <el-card class="stat-card" shadow="never"><div class="sv">{{ stats.total }}</div><div class="sl">总阶段</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv" style="color:#67c23a">{{ stats.done }}</div><div class="sl">已完成</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv" style="color:#e6a23c">{{ stats.doing }}</div><div class="sl">进行中</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv">{{ stats.materials }}</div><div class="sl">资料数量</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv">{{ stats.photos }}</div><div class="sl">照片</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv">{{ stats.reports }}</div><div class="sl">报告</div></el-card>
            <el-card class="stat-card" shadow="never"><div class="sv">{{ stats.artifacts }}</div><div class="sl">文物</div></el-card>
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
                    <div v-if="!timelineGroups.length" style="text-align:center;padding:40px;color:#ccc">暂无时间节点，点击右上角"新增节点"添加</div>
                    <template v-else>
                        <div v-for="group in timelineGroups" :key="group.flowId" class="track-row">
                            <div class="track-label" :style="{color: getFlowColor(group.flowId)}">{{ group.label }}</div>
                            <div class="track-bar">
                                <div class="track-line" :style="{borderColor: getFlowColor(group.flowId)}"></div>
                                <div v-for="(evt, i) in group.events" :key="i" class="track-dot-wrap" :style="{left: getTimePercent(evt.date) + '%'}">
                                    <div class="track-dot" :style="{background: getFlowColor(group.flowId)}" @click="onEventClick(evt)" :title="evt.title + ' (' + evt.date + ')'"></div>
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
            <el-card class="col-right" shadow="never">
                <template #header><span style="font-weight:600">节点档案详情</span></template>
                <div v-if="detailData" class="detail-panel">
                    <div class="detail-title">{{ detailData.title }}</div>
                    <el-divider style="margin:8px 0" />
                    <div class="detail-row"><label>日期</label><span>{{ detailData.date }}</span></div>
                    <div class="detail-row"><label>状态</label>
                        <el-tag :type="detailData.status === 'done' ? 'success' : detailData.status === 'doing' ? 'warning' : 'info'" size="small">{{ detailData.status === 'done' ? '已完成' : detailData.status === 'doing' ? '进行中' : '待开始' }}</el-tag>
                    </div>
                    <el-divider style="margin:8px 0" />
                    <div class="detail-row"><label>关联文物</label><span>{{ detailData.artifacts }}</span></div>
                    <div class="detail-row"><label>照片</label><span>{{ detailData.photos }}</span></div>
                    <div class="detail-row"><label>报告</label><span>{{ detailData.reports }}</span></div>
                    <div class="detail-row"><label>人员</label><span>{{ detailData.personnel }}</span></div>
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
            <el-select v-model="item.flowId" size="small" style="width:120px" placeholder="关联流程">
                <el-option v-for="n in treeData" :key="n.id" :label="n.label" :value="n.id" />
            </el-select>
            <el-button size="small" circle type="danger" @click="tlRemove(idx)">✕</el-button>
        </div>
        <el-divider />
        <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center">
            <el-date-picker v-model="newDate" type="date" size="small" style="width:130px" placeholder="日期" value-format="YYYY-MM-DD" />
            <el-input v-model="newTitle" size="small" style="width:140px" placeholder="标题" />
            <el-select v-model="newFlowId" size="small" style="width:120px" placeholder="关联流程" clearable>
                <el-option v-for="n in treeData" :key="n.id" :label="n.label" :value="n.id" />
            </el-select>
        </div>
        <template #footer><el-button @click="timelineEditVisible = false">取消</el-button><el-button type="primary" @click="saveTimeline">保存</el-button></template>
    </el-dialog>
</template>

<style scoped>
.workflow-page { padding: 0; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.stats-row { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; flex-shrink: 0; }
.stat-card { flex: 1; min-width: 100px; text-align: center; border: 1px solid #E5E6EB; border-radius: 8px; }
.sv { font-size: 24px; font-weight: 700; color: #1D2129; }
.sl { font-size: 11px; color: #999; margin-top: 2px; }
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
.detail-title { font-size: 15px; font-weight: 700; color: #1D2129; }
.detail-row { margin-bottom: 8px; }
.detail-row label { font-size: 11px; color: #999; display: block; }
.detail-row span { font-size: 13px; color: #1D2129; }
.detail-empty { text-align: center; padding: 60px 0; color: #ccc; font-size: 13px; }
</style>
