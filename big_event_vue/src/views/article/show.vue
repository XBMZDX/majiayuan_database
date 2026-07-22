<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { getHomeDashboard } from '@/api/dashboard.js'

const router = useRouter()
const loading = ref(false)
const dashboard = ref({
  summary: {},
  projectStages: [],
  resourceTypes: [],
  recentProjects: [],
  recentResources: [],
  recentArtifacts: [],
  recentDetections: [],
  recentArchives: [],
  recentAlerts: []
})

const stageChartRef = ref(null)
const resourceChartRef = ref(null)
let stageChart = null
let resourceChart = null

const summary = computed(() => dashboard.value.summary || {})
const stageRows = computed(() => dashboard.value.projectStages || [])
const resourceRows = computed(() => dashboard.value.resourceTypes || [])
const recentProjects = computed(() => dashboard.value.recentProjects || [])
const recentResources = computed(() => dashboard.value.recentResources || [])
const recentArtifacts = computed(() => dashboard.value.recentArtifacts || [])
const recentDetections = computed(() => dashboard.value.recentDetections || [])
const recentAlerts = computed(() => dashboard.value.recentAlerts || [])

const totalCount = computed(() => {
  const keys = [
    'burialCount',
    'coffinCount',
    'artifactCount',
    'detectionCount',
    'projectCount',
    'archiveCount',
    'monitoringAlertCount',
    'digitalResourceCount'
  ]
  return keys.reduce((sum, key) => sum + Number(summary.value[key] || 0), 0)
})

const statCards = computed(() => [
  { key: 'burialCount', label: '墓葬总数', desc: '当前墓葬总览的真实数据', suffix: '座', color: '#7c3aed', route: '/tomb/manage', badge: '墓葬' },
  { key: 'artifactCount', label: '出土文物', desc: '文物基础信息与分类', suffix: '件', color: '#0ea5e9', route: '/artifacts/manage', badge: '文物' },
  { key: 'detectionCount', label: '检测分析', desc: '实验、检测与结果数据', suffix: '项', color: '#f59e0b', route: '/detection/manage', badge: '检测' },
  { key: 'projectCount', label: '保护修复项目', desc: '病害、过程、对比、复原', suffix: '个', color: '#10b981', route: '/conservation/overview', badge: '修复' },
  { key: 'archiveCount', label: '保护修复档案', desc: '编研、版本与附件归档', suffix: '份', color: '#8b5cf6', route: '/conservation/archive', badge: '档案' },
  { key: 'monitoringAlertCount', label: '监测预警', desc: '后续监测与风险预警', suffix: '条', color: '#ef4444', route: '/conservation/monitor', badge: '预警' },
  { key: 'digitalResourceCount', label: '数字资源', desc: '档案资源、图片与模型', suffix: '项', color: '#14b8a6', route: '/archive/manage', badge: '资源' }
])

const moduleCards = [
  { title: '展示界面', desc: '平台首页与全局总览', route: '/show/manage' },
  { title: '文物信息总览', desc: '文物、遗迹、墓葬与车马', route: '/artifacts/manage' },
  { title: '文物检测分析', desc: '检测流程、结果与实验', route: '/detection/overview' },
  { title: '保护修复总览', desc: '项目、病害、过程、复原', route: '/conservation/overview' },
  { title: '档案资源中心', desc: '资源、媒体与模型管理', route: '/archive/manage' },
  { title: '后续监测', desc: '预警、任务与监测记录', route: '/conservation/monitor' }
]

const formatNumber = value => Number(value || 0).toLocaleString('zh-CN')
const formatBytes = value => {
  const num = Number(value || 0)
  if (!num) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = num
  let index = 0
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index += 1
  }
  return `${size.toFixed(size >= 10 || index === 0 ? 0 : 1)} ${units[index]}`
}
const formatDate = value => String(value || '').replace('T', ' ').slice(0, 19) || '—'
const progressValue = value => Math.max(0, Math.min(100, Number(value || 0)))
const stageLabel = value => ({
  pendingSurvey: '待调查',
  disease: '病害调查',
  planning: '方案制定',
  protection: '保护处理',
  repair: '修复执行',
  restoration: '复原成果',
  monitoring: '后续监测',
  completed: '已完成'
}[value] || value || '—')
const statusLabel = value => ({
  draft: '草稿',
  active: '进行中',
  completed: '已完成',
  suspended: '暂停',
  archived: '已归档',
  compiling: '编研中'
}[value] || value || '—')
const alertLevelLabel = value => ({
  danger: '严重',
  warning: '警告',
  info: '提示'
}[value] || value || '—')
const resourceTypeLabel = value => ({
  image: '图片',
  document: '文档',
  report: '报告',
  spreadsheet: '表格',
  video: '视频',
  audio: '音频',
  model_3d: '三维模型'
}[value] || value || '其他')

const reloadCharts = () => {
  const stageOption = {
    title: { text: '保护修复阶段分布', left: 8, top: 6, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 52, bottom: 30, containLabel: true },
    xAxis: {
      type: 'category',
      data: stageRows.value.map(item => item.name),
      axisLabel: { interval: 0, rotate: 20 }
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: stageRows.value.map(item => item.value),
      barMaxWidth: 28,
      itemStyle: { color: '#2563eb', borderRadius: [6, 6, 0, 0] }
    }]
  }

  const resourceOption = {
    title: { text: '数字资源类型分布', left: 8, top: 6, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '47%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: resourceRows.value.map(item => ({ name: item.name, value: item.value }))
    }]
  }

  if (stageChartRef.value) {
    if (stageChart) stageChart.dispose()
    stageChart = echarts.init(stageChartRef.value)
    stageChart.setOption(stageOption)
  }
  if (resourceChartRef.value) {
    if (resourceChart) resourceChart.dispose()
    resourceChart = echarts.init(resourceChartRef.value)
    resourceChart.setOption(resourceOption)
  }
}

const resizeCharts = () => {
  stageChart?.resize()
  resourceChart?.resize()
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const result = await getHomeDashboard()
    dashboard.value = result.data || {}
    await nextTick()
    reloadCharts()
  } catch (error) {
    console.error(error)
    ElMessage.error('首页数据加载失败，请检查后端接口和数据库表')
  } finally {
    loading.value = false
  }
}

const openModule = path => {
  router.push(path)
}

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  stageChart?.dispose()
  resourceChart?.dispose()
})
</script>

<template>
  <div class="home-page" v-loading="loading">
    <section class="hero">
      <div class="hero__content">
        <p class="hero__kicker">Digital Archaeology & Conservation Dashboard</p>
        <h1>文物数字档案与保护修复总览</h1>
        <p class="hero__desc">
          这是系统的第一个展示页面，直接汇总文物、检测、保护修复、档案、监测和数字资源的真实数据。
        </p>
        <div class="hero__meta">
          <el-tag type="success" effect="dark">实时读取数据库</el-tag>
          <el-tag type="info" effect="dark">首页总量 {{ formatNumber(totalCount) }}</el-tag>
        </div>
      </div>
      <div class="hero__actions">
        <el-button type="primary" @click="loadDashboard">刷新看板</el-button>
        <el-button @click="openModule('/conservation/overview')">进入保护修复</el-button>
        <el-button @click="openModule('/archive/manage')">进入档案中心</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <el-card
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
        shadow="never"
        @click="openModule(card.route)"
      >
        <div class="stat-card__top">
          <div class="stat-badge" :style="{ background: card.color }">{{ card.badge }}</div>
          <el-tag size="small" effect="plain">可点击跳转</el-tag>
        </div>
        <div class="stat-card__value">
          {{ formatNumber(summary[card.key] || 0) }}<span>{{ card.suffix }}</span>
        </div>
        <div class="stat-card__label">{{ card.label }}</div>
        <div class="stat-card__desc">{{ card.desc }}</div>
      </el-card>
    </section>

    <section class="chart-grid">
      <el-card class="chart-card" shadow="never">
        <div ref="stageChartRef" class="chart-box"></div>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <div ref="resourceChartRef" class="chart-box"></div>
      </el-card>
    </section>

    <section class="content-grid">
      <el-card class="content-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近保护修复项目</span>
            <el-tag effect="plain">{{ recentProjects.length }} 条</el-tag>
          </div>
        </template>
        <el-table :data="recentProjects" size="small" stripe height="360">
          <el-table-column prop="projectCode" label="项目编号" width="140" />
          <el-table-column prop="projectName" label="项目名称" min-width="180" />
          <el-table-column label="当前阶段" width="120">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ stageLabel(row.currentStage) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'completed' ? 'success' : row.status === 'archived' ? 'info' : 'warning'">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="180">
            <template #default="{ row }">
              <el-progress :percentage="progressValue(row.progress)" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column prop="principal" label="负责人" width="120" />
          <el-table-column prop="updateTime" label="更新时间" width="170">
            <template #default="{ row }">{{ formatDate(row.updateTime || row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="side-stack">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>模块入口</span>
              <el-tag effect="plain">常用导航</el-tag>
            </div>
          </template>
          <div class="module-grid">
            <button
              v-for="item in moduleCards"
              :key="item.route"
              class="module-card"
              @click="openModule(item.route)"
            >
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </button>
          </div>
        </el-card>

        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最新预警</span>
              <el-tag effect="plain">{{ recentAlerts.length }} 条</el-tag>
            </div>
          </template>
          <div class="compact-list">
            <div v-for="item in recentAlerts" :key="item.id" class="compact-item">
              <div class="compact-item__main">
                <strong>{{ item.alertTitle || item.alertCode || '未命名预警' }}</strong>
                <span>项目 #{{ item.projectId || '—' }}</span>
              </div>
              <div class="compact-item__meta">
                <el-tag size="small" type="danger">{{ alertLevelLabel(item.alertLevel) }}</el-tag>
                <el-tag size="small" effect="plain">{{ statusLabel(item.alertStatus) }}</el-tag>
              </div>
            </div>
            <el-empty v-if="!recentAlerts.length" description="暂无预警数据" :image-size="80" />
          </div>
        </el-card>
      </div>
    </section>

    <section class="mini-grid">
      <el-card class="mini-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近文物</span>
            <el-tag effect="plain">{{ recentArtifacts.length }} 条</el-tag>
          </div>
        </template>
        <div class="compact-list">
          <div v-for="item in recentArtifacts" :key="item.id" class="compact-item">
            <div class="compact-item__main">
              <strong>{{ item.artifactName || item.newArtifactName || item.originalArtifactName || '未命名文物' }}</strong>
              <span>
                {{ item.artifactCode || item.newArtifactCode || item.originalArtifactCode || '无编号' }}
                · {{ item.excavationRelic || '未填出土点' }}
              </span>
            </div>
            <el-tag size="small" type="info">{{ item.completeness || '未录入完整度' }}</el-tag>
          </div>
          <el-empty v-if="!recentArtifacts.length" description="暂无文物数据" :image-size="72" />
        </div>
      </el-card>

      <el-card class="mini-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近检测分析</span>
            <el-tag effect="plain">{{ recentDetections.length }} 条</el-tag>
          </div>
        </template>
        <div class="compact-list">
            <div v-for="item in recentDetections" :key="item.id" class="compact-item">
              <div class="compact-item__main">
                <strong>{{ item.artifactName || item.artifactCode || '未命名检测' }}</strong>
                <span>{{ item.purpose || '检测目的未填写' }}</span>
              </div>
              <span class="compact-time">{{ formatDate(item.updateTime || item.createTime) }}</span>
            </div>
          <el-empty v-if="!recentDetections.length" description="暂无检测数据" :image-size="72" />
        </div>
      </el-card>

      <el-card class="mini-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>资源类型汇总</span>
            <el-tag effect="plain">{{ recentResources.length }} 条</el-tag>
          </div>
        </template>
        <div class="compact-list">
          <div v-for="item in resourceRows" :key="item.name" class="compact-item">
            <div class="compact-item__main">
              <strong>{{ item.name }}</strong>
              <span>数量 {{ formatNumber(item.value) }}</span>
            </div>
            <el-tag size="small" type="success">{{ item.name }}</el-tag>
          </div>
        </div>
      </el-card>
    </section>

    <el-card class="resource-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>最近数字资源</span>
          <el-tag effect="plain">{{ recentResources.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="recentResources" size="small" stripe height="320">
        <el-table-column prop="resourceCode" label="资源编号" width="150" />
        <el-table-column prop="resourceName" label="资源名称" min-width="180" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ resourceTypeLabel(row.resourceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceModule" label="来源模块" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.resourceStatus === 'archived' ? 'success' : 'warning'">
              {{ statusLabel(row.resourceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="110">
          <template #default="{ row }">{{ formatBytes(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="{ row }">{{ formatDate(row.uploadTime || row.updateTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 88px);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 26px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 40%, #38bdf8 100%);
}

.hero__content {
  max-width: 820px;
}

.hero__kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  opacity: 0.8;
}

.hero h1 {
  margin: 0;
  font-size: 30px;
}

.hero__desc {
  margin: 10px 0 0;
  line-height: 1.7;
  opacity: 0.96;
}

.hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.hero__actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  min-width: 180px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  cursor: pointer;
  border-radius: 18px;
  border: 1px solid #edf0f7;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.stat-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.stat-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.stat-card__value {
  font-size: 30px;
  font-weight: 700;
  color: #111827;
}

.stat-card__value span {
  margin-left: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

.stat-card__label {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.stat-card__desc {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.chart-card {
  border-radius: 18px;
}

.chart-box {
  height: 360px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 14px;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.content-card,
.mini-card,
.resource-card {
  border-radius: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.module-card {
  text-align: left;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
  background: #fff;
  cursor: pointer;
  transition: 0.2s ease;
}

.module-card:hover {
  border-color: #93c5fd;
  background: #eff6ff;
  transform: translateY(-1px);
}

.module-card strong,
.module-card span {
  display: block;
}

.module-card span {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.compact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.compact-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #fff;
}

.compact-item__main {
  min-width: 0;
}

.compact-item__main strong {
  display: block;
  font-size: 14px;
  color: #111827;
}

.compact-item__main span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.compact-item__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.compact-time {
  min-width: 120px;
  text-align: right;
  font-size: 12px;
  color: #64748b;
}

.mini-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 1280px) {
  .stats-grid,
  .mini-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .hero,
  .chart-grid,
  .stats-grid,
  .mini-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    flex-direction: column;
  }

  .hero__actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
