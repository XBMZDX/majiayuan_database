<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request.js'

const router = useRouter()
const loading = ref(false)

const queryParams = ref({ sourceModule: '', resourceType: '' })
const summary = ref({})
const sourceDist = ref([])
const typeDist = ref([])
const recentList = ref([])
const issues = ref([])

const loadAll = async () => {
    loading.value = true
    const p = { sourceModule: queryParams.value.sourceModule || undefined, resourceType: queryParams.value.resourceType || undefined }
    const results = await Promise.allSettled([
        request.get('/api/digital-archive/overview/summary', { params: p }).then(r => r.data).catch(() => ({})),
        request.get('/api/digital-archive/overview/source-distribution').then(r => r.data).catch(() => []),
        request.get('/api/digital-archive/overview/type-distribution').then(r => r.data).catch(() => []),
        request.get('/api/digital-archive/overview/recent-resources?limit=6').then(r => r.data).catch(() => []),
        request.get('/api/digital-archive/overview/issues').then(r => r.data).catch(() => [])
    ])
    summary.value = results[0].value || {}
    sourceDist.value = results[1].value || []
    typeDist.value = results[2].value || []
    recentList.value = results[3].value || []
    issues.value = results[4].value || []
    loading.value = false
    nextTick(() => renderCharts())
}

const sourceModuleNames = { tomb: '墓葬信息', coffin: '棺信息', vehicle: '车信息', artifact: '文物信息', detection: '检测分析', disease: '病害调查', conservation: '保护修复', process: '修复过程', comparison: '修复前后对比', restoration: '复原成果', monitoring: '后续监测', manual: '手工上传', digital_archive: '数字档案' }

const formatSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B'
    const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes
    while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }
    return v.toFixed(1) + ' ' + units[i]
}
const typeNameMap = { image: '图片', document: '文档', report: '报告', video: '视频', audio: '音频', spreadsheet: '表格', model_3d: '三维模型', other: '其他' }

// ECharts
const sourceChartRef = ref(null); const typeChartRef = ref(null)
let sourceChart = null, typeChart = null

const renderCharts = () => {
    if (sourceChartRef.value) {
        if (!sourceChart) sourceChart = echarts.init(sourceChartRef.value)
        sourceChart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: { type: 'category', data: sourceDist.value.map(i => sourceModuleNames[i.sourceModule] || i.sourceModule || '未知'), axisLabel: { rotate: 30, fontSize: 10 } },
            yAxis: { type: 'value' },
            series: [{ type: 'bar', data: sourceDist.value.map(i => i.totalCount || 0), itemStyle: { borderRadius: [4,4,0,0], color: '#409EFF' } }],
            grid: { top: 10, right: 10, bottom: 50, left: 50 }
        }, true)
        sourceChart.off('click')
        sourceChart.on('click', (p) => { if (p.dataIndex >= 0 && sourceDist.value[p.dataIndex]) router.push({ path: '/archive/manage', query: { sourceModule: sourceDist.value[p.dataIndex].sourceModule } }) })
    }
    if (typeChartRef.value) {
        if (!typeChart) typeChart = echarts.init(typeChartRef.value)
        typeChart.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            series: [{ type: 'pie', radius: ['45%','70%'], center: ['50%','55%'], label: { formatter: '{b}\n{d}%' }, data: typeDist.value.map(i => ({ name: typeNameMap[i.resourceType] || i.resourceType || '其他', value: i.resourceCount || 0 })) }],
            graphic: [{ type: 'text', left: 'center', top: '40%', style: { text: '资源类型', textAlign: 'center', fill: '#999', fontSize: 12 } }]
        }, true)
    }
}

onMounted(loadAll)
onBeforeUnmount(() => { if (sourceChart) sourceChart.dispose(); if (typeChart) typeChart.dispose() })
</script>

<template>
    <div class="overview-page" v-loading="loading">
        <!-- 筛选区 -->
        <div class="filter-bar">
            <el-select v-model="queryParams.sourceModule" placeholder="来源模块" clearable size="default" style="width:140px">
                <el-option v-for="m in [{label:'全部',value:''},{label:'墓葬信息',value:'tomb'},{label:'文物信息',value:'artifact'},{label:'检测分析',value:'detection'},{label:'病害调查',value:'disease'},{label:'保护修复',value:'conservation'},{label:'修复过程',value:'process'},{label:'手工上传',value:'manual'}]" :key="m.value" :label="m.label" :value="m.value" />
            </el-select>
            <el-select v-model="queryParams.resourceType" placeholder="资源类型" clearable size="default" style="width:130px">
                <el-option v-for="t in [{label:'全部',value:''},{label:'图片',value:'image'},{label:'文档',value:'document'},{label:'报告',value:'report'},{label:'视频/音频',value:'video'},{label:'三维模型',value:'model_3d'},{label:'其他',value:'other'}]" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-button type="primary" size="default" @click="loadAll">查询</el-button>
            <el-button size="default" @click="queryParams = {sourceModule:'',resourceType:''}; loadAll()">重置</el-button>
        </div>

        <!-- 统计卡片 -->
        <div class="stat-grid">
            <div class="stat-card" v-for="s in [
                {label:'资源总数',value:summary.totalResources,icon:'📦'},
                {label:'图片资源',value:summary.imageCount,icon:'🖼'},
                {label:'文档与报告',value:(summary.documentCount||0)+(summary.reportCount||0),icon:'📄'},
                {label:'视频与音频',value:summary.videoCount,icon:'🎬'},
                {label:'三维模型',value:summary.modelCount,icon:'🧊'},
                {label:'已关联资源',value:summary.relatedResourceCount,icon:'🔗'},
                {label:'待完善资源',value:summary.incompleteResourceCount,icon:'⚠'},
                {label:'存储空间',value:formatSize(summary.totalStorageBytes),icon:'💾'}
            ]" :key="s.label">
                <div class="stat-icon">{{ s.icon }}</div>
                <div class="stat-body">
                    <div class="stat-value">{{ s.value ?? '-' }}</div>
                    <div class="stat-label">{{ s.label }}</div>
                </div>
            </div>
        </div>

        <!-- 图表区 -->
        <div class="chart-row">
            <div class="chart-card">
                <div class="chart-title">来源模块分布</div>
                <div ref="sourceChartRef" style="height:280px"></div>
            </div>
            <div class="chart-card">
                <div class="chart-title">资源类型分布</div>
                <div ref="typeChartRef" style="height:280px"></div>
            </div>
        </div>

        <!-- 底部 -->
        <div class="bottom-row">
            <div class="bottom-card">
                <div class="card-title">最近资源</div>
                <el-table :data="recentList" size="small" max-height="280">
                    <el-table-column prop="resourceName" label="资源名称" min-width="140" show-overflow-tooltip />
                    <el-table-column label="类型" width="80"><template #default="{row}"><el-tag size="small">{{ typeNameMap[row.resourceType] || row.resourceType || '-' }}</el-tag></template></el-table-column>
                    <el-table-column label="来源" width="90"><template #default="{row}">{{ sourceModuleNames[row.sourceModule] || row.sourceModule || '-' }}</template></el-table-column>
                    <el-table-column label="大小" width="80"><template #default="{row}">{{ formatSize(row.fileSize) }}</template></el-table-column>
                    <el-table-column prop="uploadedBy" label="上传人" width="80" />
                </el-table>
            </div>
            <div class="bottom-card">
                <div class="card-title">数据问题</div>
                <div v-for="iss in issues" :key="iss.issueType" class="issue-item">
                    <el-tag :type="iss.issueLevel==='danger'?'danger':iss.issueLevel==='warning'?'warning':'info'" size="small">{{ iss.issueName }}</el-tag>
                    <span class="issue-count">{{ iss.issueCount }}</span>
                </div>
                <el-empty v-if="!issues.length" description="暂无数据问题" :image-size="40" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.overview-page { padding: 0; }
.filter-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { display: flex; align-items: center; gap: 12px; padding: 16px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; transition: box-shadow .15s; }
.stat-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-icon { font-size: 28px; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: #F5F7FA; border-radius: 10px; }
.stat-value { font-size: 22px; font-weight: 700; color: #1D2129; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 16px; }
.chart-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 16px; }
.chart-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 8px; }
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.bottom-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 16px; }
.card-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 10px; }
.issue-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #F0F0F0; }
.issue-count { font-size: 16px; font-weight: 700; color: #1D2129; }
@media (max-width: 1200px) { .stat-grid { grid-template-columns: repeat(3, 1fr); } .chart-row, .bottom-row { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
