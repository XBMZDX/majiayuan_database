<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request.js'
import DigitalResourceLibrary from './DigitalResourceLibrary.vue'

const loading = ref(false)

const summary = ref({})
const typeDist = ref([])
const recentList = ref([])

const loadAll = async () => {
    loading.value = true
    const results = await Promise.allSettled([
        request.get('/api/digital-archive/overview/summary').then(r => r.data).catch(() => ({})),
        request.get('/api/digital-archive/overview/type-distribution').then(r => r.data).catch(() => []),
        request.get('/api/digital-archive/overview/recent-resources?limit=6').then(r => r.data).catch(() => [])
    ])
    summary.value = results[0].value || {}
    typeDist.value = results[1].value || []
    recentList.value = results[2].value || []
    loading.value = false
    nextTick(() => renderCharts())
}

const formatSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B'
    const units = ['B','KB','MB','GB','TB']; let i = 0, v = bytes
    while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }
    return v.toFixed(1) + ' ' + units[i]
}
const typeNameMap = { image: '图片', document: '文档', report: '报告', video: '视频', audio: '音频', spreadsheet: '表格', model_3d: '三维模型', other: '其他' }

// ECharts
const typeChartRef = ref(null)
let typeChart = null

const renderCharts = () => {
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
onBeforeUnmount(() => { if (typeChart) typeChart.dispose() })
</script>

<template>
    <div class="overview-page" v-loading="loading">
        <!-- 统计卡片 -->
        <div class="stat-grid">
            <div class="stat-card" v-for="s in [
                {label:'资源总数',value:summary.totalResources,icon:'📦'},
                {label:'图片资源',value:summary.imageCount,icon:'🖼'},
                {label:'文档与报告',value:(summary.documentCount||0)+(summary.reportCount||0),icon:'📄'},
                {label:'视频与音频',value:summary.videoCount,icon:'🎬'},
                {label:'三维模型',value:summary.modelCount,icon:'🧊'},
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
                <div class="chart-title">资源类型分布</div>
                <div ref="typeChartRef" style="height:280px"></div>
            </div>
            <div class="chart-card">
                <div class="card-title">最近资源</div>
                <el-table :data="recentList" size="small" max-height="280">
                    <el-table-column prop="resourceName" label="资源名称" min-width="140" show-overflow-tooltip />
                    <el-table-column label="类型" width="80"><template #default="{row}"><el-tag size="small">{{ typeNameMap[row.resourceType] || row.resourceType || '-' }}</el-tag></template></el-table-column>
                    <el-table-column label="大小" width="80"><template #default="{row}">{{ formatSize(row.fileSize) }}</template></el-table-column>
                    <el-table-column prop="uploadedBy" label="上传人" width="80" />
                </el-table>
            </div>
        </div>

        <div class="resource-table-section">
            <div class="section-title">全部资源</div>
            <DigitalResourceLibrary :show-summary="false" @resource-changed="loadAll" />
        </div>
    </div>
</template>

<style scoped>
.overview-page { padding: 0; }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { display: flex; align-items: center; gap: 12px; padding: 16px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; transition: box-shadow .15s; }
.stat-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-icon { font-size: 28px; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: #F5F7FA; border-radius: 10px; }
.stat-value { font-size: 22px; font-weight: 700; color: #1D2129; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 16px; }
.chart-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 16px; }
.chart-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 8px; }
.card-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 10px; }
.resource-table-section { margin-top: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: #1D2129; margin-bottom: 12px; }
@media (max-width: 1200px) { .stat-grid { grid-template-columns: repeat(3, 1fr); } .chart-row { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
