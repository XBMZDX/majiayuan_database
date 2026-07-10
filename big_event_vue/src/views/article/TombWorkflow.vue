<script setup>
import { ref, computed } from 'vue'

// ========== 统计卡片 ==========
const stats = ref({ total: 8, done: 3, doing: 4, pending: 1, materials: 156, photos: 420, reports: 12, artifacts: 89 })

// ========== 流程树（左侧） ==========
const treeData = ref([
    { id: 1, label: '前期准备', children: [
        { id: 11, label: '资料收集' }, { id: 12, label: '现场勘查' }, { id: 13, label: '方案审批' }
    ]},
    { id: 2, label: '考古发掘', children: [
        { id: 21, label: '探方布设' }, { id: 22, label: '地层清理' }, { id: 23, label: '遗迹记录' }
    ]},
    { id: 3, label: '文物保护', children: [
        { id: 31, label: '现场保护' }, { id: 32, label: '实验室保护' }, { id: 33, label: '修复处理' }
    ]},
    { id: 4, label: '研究分析', children: [
        { id: 41, label: '年代测定' }, { id: 42, label: '成分分析' }, { id: 43, label: '对比研究' }
    ]},
    { id: 5, label: '成果整理', children: [
        { id: 51, label: '报告撰写' }, { id: 52, label: '图录编制' }, { id: 53, label: '档案归档' }
    ]},
])

const selectedNode = ref(null)
const onNodeClick = (node) => { selectedNode.value = node }

// ========== 时间轴（中间） ==========
const timelineData = ref([
    { date: '2026-07-10', title: '资料收集完成', desc: '完成M62墓葬相关文献、测绘资料收集整理', status: 'done', nodeId: 11 },
    { date: '2026-07-12', title: '现场勘查启动', desc: '对M62墓葬现场进行初步勘查', status: 'done', nodeId: 12 },
    { date: '2026-07-15', title: '探方布设', desc: '布设5m×5m探方网格', status: 'done', nodeId: 21 },
    { date: '2026-07-20', title: '地层清理进行中', desc: '第3层文化层清理，出土陶片若干', status: 'doing', nodeId: 22 },
    { date: '2026-08-01', title: '遗迹记录', desc: '墓室结构测绘与影像记录', status: 'doing', nodeId: 23 },
    { date: '2026-08-10', title: '现场保护', desc: '对脆弱文物进行临时加固', status: 'doing', nodeId: 31 },
    { date: '2026-08-15', title: '实验室保护', desc: '文物转运至实验室，准备保护处理', status: 'doing', nodeId: 32 },
    { date: '2026-09-01', title: '方案审批', desc: '发掘方案待提交审批', status: 'pending', nodeId: 13 },
])

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
                <template #header><span style="font-weight:600">流程树</span></template>
                <el-tree :data="treeData" node-key="id" default-expand-all :expand-on-click-node="false" highlight-current @node-click="onNodeClick" />
            </el-card>

            <!-- 中间：流程时间轴 -->
            <el-card class="col-center" shadow="never">
                <template #header><span style="font-weight:600">流程时间轴</span></template>
                <el-timeline>
                    <el-timeline-item
                        v-for="item in timelineData"
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
</template>

<style scoped>
.workflow-page { padding: 0; height: 100%; }
.stats-row { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 100px; text-align: center; border: 1px solid #E5E6EB; border-radius: 8px; }
.sv { font-size: 24px; font-weight: 700; color: #1D2129; }
.sl { font-size: 11px; color: #999; margin-top: 2px; }
.three-col { display: flex; gap: 12px; height: calc(100vh - 260px); }
.col-left { flex: 0 0 220px; overflow-y: auto; border: 1px solid #E5E6EB; border-radius: 8px; }
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
