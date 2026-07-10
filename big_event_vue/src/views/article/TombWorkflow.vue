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
const filteredTimeline = computed(() => {
    const data = selectedNode.value ? timelineData.value.filter(t => t.flowId === selectedNode.value.id) : timelineData.value
    return [...data].sort((a, b) => (a.date || '').localeCompare(b.date || ''))
})
const onNodeClick = (node) => { selectedNode.value = selectedNode.value?.id === node.id ? null : node }

// ========== 流程树编辑弹窗 ==========
const editDialogVisible = ref(false)
const dialogNodes = ref([])
const dialogNewLabel = ref('')

const openEditDialog = () => { dialogNodes.value = treeData.value.map(n => ({ ...n })); editDialogVisible.value = true }
const dialogAdd = () => { if (!dialogNewLabel.value.trim()) return; const maxId = Math.max(...dialogNodes.value.map(n => n.id), 0); dialogNodes.value.push({ id: maxId + 1, label: dialogNewLabel.value.trim() }); dialogNewLabel.value = '' }
const dialogRemove = (id) => { dialogNodes.value = dialogNodes.value.filter(n => n.id !== id) }
const dialogMoveUp = (idx) => { if (idx > 0) { const t = dialogNodes.value[idx]; dialogNodes.value[idx] = dialogNodes.value[idx-1]; dialogNodes.value[idx-1] = t } }
const dialogMoveDown = (idx) => { if (idx < dialogNodes.value.length - 1) { const t = dialogNodes.value[idx]; dialogNodes.value[idx] = dialogNodes.value[idx+1]; dialogNodes.value[idx+1] = t } }
const saveDialog = () => { treeData.value = dialogNodes.value.map(n => ({ ...n })); editDialogVisible.value = false; selectedNode.value = null; saveTreeToDB() }
const cancelDialog = () => { editDialogVisible.value = false }

// ========== 时间轴编辑弹窗 ==========
const timelineEditVisible = ref(false)
const dialogTimeline = ref([])
const newTitle = ref(''); const newDate = ref(''); const newDesc = ref(''); const newFlowId = ref(null)

const openTimelineEdit = () => {
    dialogTimeline.value = timelineData.value.map(t => ({ ...t }))
    timelineEditVisible.value = true
}
const tlAdd = () => {
    if (!newTitle.value.trim() || !newDate.value) return
    dialogTimeline.value.push({ date: newDate.value, title: newTitle.value.trim(), desc: newDesc.value, status: 'pending', flowId: newFlowId.value || treeData.value[0]?.id || 1 })
    newTitle.value = ''; newDate.value = ''; newDesc.value = ''; newFlowId.value = null
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

// ========== 详情（右侧） ==========
const detailData = computed(() => {
    if (!selectedEvent.value) return null
    return {
        title: selectedEvent.value.title,
        date: selectedEvent.value.date,
        status: selectedEvent.value.status,
        desc: selectedEvent.value.desc,
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
                        <div class="flow-index">{{ idx + 1 }}</div>
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
                <el-timeline>
                    <el-timeline-item
                        v-for="item in filteredTimeline"
                        :key="item.date"
                        :timestamp="item.date"
                        :color="item.status === 'done' ? '#67c23a' : item.status === 'doing' ? '#e6a23c' : '#909399'"
                        placement="top"
                    >
                        <div class="timeline-item" @click="onEventClick(item)" style="cursor:pointer">
                            <strong :class="item.status === 'done' ? 'text-done' : item.status === 'doing' ? 'text-doing' : 'text-pending'">{{ item.title }}</strong>
                            <div style="font-size:12px;color:#999;margin-top:2px">{{ item.desc }}</div>
                        </div>
                    </el-timeline-item>
                </el-timeline>
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
                    <div class="detail-row"><label>描述</label><span>{{ detailData.desc }}</span></div>
                    <el-divider style="margin:8px 0" />
                    <div class="detail-row"><label>关联文物</label><span>{{ detailData.artifacts }}</span></div>
                    <div class="detail-row"><label>照片</label><span>{{ detailData.photos }}</span></div>
                    <div class="detail-row"><label>报告</label><span>{{ detailData.reports }}</span></div>
                    <div class="detail-row"><label>人员</label><span>{{ detailData.personnel }}</span></div>
                </div>
                <div v-else class="detail-empty">点击时间轴节点查看详情</div>
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
            <el-input v-model="item.desc" size="small" placeholder="描述" style="width:100%;margin-top:4px" />
        </div>
        <el-divider />
        <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center">
            <el-date-picker v-model="newDate" type="date" size="small" style="width:130px" placeholder="日期" value-format="YYYY-MM-DD" />
            <el-input v-model="newTitle" size="small" style="width:140px" placeholder="标题" />
            <el-select v-model="newFlowId" size="small" style="width:120px" placeholder="关联流程" clearable>
                <el-option v-for="n in treeData" :key="n.id" :label="n.label" :value="n.id" />
            </el-select>
            <el-input v-model="newDesc" size="small" placeholder="描述" />
        </div>
        <template #footer><el-button @click="timelineEditVisible = false">取消</el-button><el-button type="primary" @click="saveTimeline">保存</el-button></template>
    </el-dialog>
</template>

<style scoped>
.workflow-page { padding: 0; height: 100%; }
.stats-row { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 100px; text-align: center; border: 1px solid #E5E6EB; border-radius: 8px; }
.sv { font-size: 24px; font-weight: 700; color: #1D2129; }
.sl { font-size: 11px; color: #999; margin-top: 2px; }
.three-col { display: flex; gap: 12px; height: calc(100vh - 260px); }
.col-left { flex: 0 0 220px; overflow-y: auto; border: 1px solid #E5E6EB; border-radius: 8px; }
.flow-tree { padding: 4px 0; }
.flow-node { position: relative; display: flex; align-items: flex-start; padding: 12px 16px 12px 12px; cursor: pointer; transition: background .15s; }
.flow-node:hover { background: #F7F8FA; }
.flow-node.selected { background: #ECF5FF; }
.flow-index { width: 24px; height: 24px; border-radius: 50%; background: #E5E6EB; color: #666; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-right: 8px; }
.flow-node.selected .flow-index { background: #1668C4; color: #fff; }
.flow-connector { position: absolute; left: 11px; top: 36px; width: 1px; height: 24px; background: #E5E6EB; }
.flow-label { font-size: 13px; color: #1D2129; line-height: 24px; }
.col-center { flex: 1; overflow-y: auto; border: 1px solid #E5E6EB; border-radius: 8px; }
.col-right { flex: 0 0 260px; overflow-y: auto; border: 1px solid #E5E6EB; border-radius: 8px; }
.timeline-item { padding: 4px 0; }
.text-done { color: #67c23a; }
.text-doing { color: #e6a23c; }
.text-pending { color: #909399; }
.detail-panel { font-size: 13px; }
.detail-title { font-size: 15px; font-weight: 700; color: #1D2129; }
.detail-row { margin-bottom: 8px; }
.detail-row label { font-size: 11px; color: #999; display: block; }
.detail-row span { font-size: 13px; color: #1D2129; }
.detail-empty { text-align: center; padding: 60px 0; color: #ccc; font-size: 13px; }
</style>
