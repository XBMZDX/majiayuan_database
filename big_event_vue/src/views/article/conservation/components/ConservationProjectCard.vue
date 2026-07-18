<!-- 保护修复项目卡片 -->
<script setup>
import { computed } from 'vue'
import { MoreFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({ project: Object })
const emit = defineEmits(['edit', 'delete', 'archive'])
const router = useRouter()

const statusTag = computed(() => {
    const m = { draft: 'info', active: '', completed: 'success', suspended: 'warning', archived: 'info' }
    const t = { draft: '草稿', active: '进行中', completed: '已完成', suspended: '暂停', archived: '已归档' }
    return { type: m[props.project.status] || 'info', text: t[props.project.status] || props.project.status }
})
const stageText = computed(() => {
    const m = { pendingSurvey: '待调查', disease: '病害调查', planning: '方案制定', protection: '保护处理中', repair: '修复处理中', restoration: '复原处理中', evaluation: '效果评估', monitoring: '后续监测' }
    return m[props.project.currentStage] || props.project.currentStage
})
const riskTag = computed(() => {
    const m = { high: 'danger', medium: 'warning', low: 'success' }
    const t = { high: '高风险', medium: '中风险', low: '低风险' }
    return { type: m[props.project.riskLevel] || 'info', text: t[props.project.riskLevel] || '未知' }
})

const projectPath = (page) => `/conservation/project/${props.project.id}/${page}`
const goArchive = () => { router.push(projectPath('archive')) }
const goContinue = () => {
    const stageMap = {
        pendingSurvey: projectPath('disease'), disease: projectPath('disease'),
        planning: projectPath('archive'), protection: projectPath('process'),
        repair: projectPath('process'), restoration: projectPath('restoration'),
        evaluation: projectPath('comparison'), monitoring: projectPath('monitoring')
    }
    const path = stageMap[props.project.currentStage] || '/conservation/overview'
    router.push(path)
}

const handleMore = (cmd) => {
    switch (cmd) {
        case 'edit': emit('edit', props.project); break
        case 'disease': router.push(projectPath('disease')); break
        case 'archive': goArchive(); break
        case 'process': router.push(projectPath('process')); break
        case 'compare': router.push(projectPath('comparison')); break
        case 'result': router.push(projectPath('restoration')); break
        case 'monitor': router.push(projectPath('monitoring')); break
        case 'archive2': emit('archive', props.project); break
        case 'delete': emit('delete', props.project); break
    }
}
</script>

<template>
    <div class="proj-card">
        <!-- 头部 -->
        <div class="card-top">
            <span class="card-artifact">{{ project.artifactCode }} {{ project.artifactName }}</span>
            <el-tag :type="statusTag.type" size="small">{{ statusTag.text }}</el-tag>
        </div>
        <div class="card-code">项目编号：{{ project.projectCode }}</div>

        <!-- 基础信息 -->
        <div class="card-info">
            <div class="info-row"><label>文物材质</label><span>{{ project.material || '-' }}</span></div>
            <div class="info-row"><label>所属墓葬</label><span>{{ project.tombCode || '-' }}</span></div>
            <div class="info-row"><label>负责人</label><span>{{ project.principal || '-' }}</span></div>
            <div class="info-row"><label>当前阶段</label><el-tag size="small" type="primary" effect="plain">{{ stageText }}</el-tag></div>
        </div>

        <!-- 病害标签 -->
        <div class="disease-tags" v-if="project.diseases?.length">
            <el-tag v-for="d in project.diseases.slice(0,3)" :key="d.name" size="small"
                    :type="d.level === '严重' ? 'danger' : d.level === '中度' ? 'warning' : 'info'" effect="plain">
                {{ d.name }}·{{ d.level }}
            </el-tag>
        </div>
        <div class="disease-tags" v-else>
            <span class="no-disease">病害信息待调查</span>
        </div>

        <!-- 进度 -->
        <div class="card-progress">
            <el-progress :percentage="project.progress || 0" :stroke-width="6" :color="project.progress >= 80 ? '#67C23A' : '#409EFF'" />
        </div>
        <div v-if="project.comparisonSummary" class="comparison-summary">
            <b>修复前后对比：{{ project.comparisonSummary.completed }}/{{ project.comparisonSummary.total }}组已完成</b>
            <span>收入档案 {{ project.comparisonSummary.selectedForArchive }}组 · 监测基准 {{ project.comparisonSummary.monitoringBaselines }}组</span>
            <small v-if="project.stageNotice">{{ project.stageNotice }}</small>
        </div>
        <div v-if="project.restorationSummary" class="restoration-summary">
            <b>复原成果：{{ project.restorationSummary.total }}项 · 已完成{{ project.restorationSummary.completed }}项</b>
            <span>实体 {{ project.restorationSummary.physical }}项 · 数字 {{ project.restorationSummary.digital }}项</span>
            <small v-if="project.restorationSummary.recommended">推荐：{{ project.restorationSummary.recommended }}</small>
        </div>
        <div v-if="project.monitoringSummary" class="monitoring-summary">
            <b>后续监测：{{ project.monitoringSummary.activePlans }}项计划执行中 · {{ project.monitoringSummary.targets }}个对象</b>
            <span>待办 {{ project.monitoringSummary.pendingTasks }}项 · 开放预警 {{ project.monitoringSummary.openAlerts }}条</span>
            <small>下次监测：{{ project.monitoringSummary.nextMonitoringDate || '待安排' }}</small>
        </div>

        <!-- 底部 -->
        <div class="card-footer">
            <span class="card-risk"><el-tag :type="riskTag.type" size="small">{{ riskTag.text }}</el-tag></span>
            <span class="card-time">{{ (project.updateTime || '').substring(0, 10) }}</span>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
            <el-button size="small" type="primary" @click="goArchive">查看档案</el-button>
            <el-button size="small" @click="goContinue">继续处理</el-button>
            <el-dropdown @command="handleMore" trigger="click">
                <el-button size="small" :icon="MoreFilled" />
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑项目</el-dropdown-item>
                        <el-dropdown-item command="disease">病害调查</el-dropdown-item>
                        <el-dropdown-item command="archive">保护修复档案</el-dropdown-item>
                        <el-dropdown-item command="process">修复过程记录</el-dropdown-item>
                        <el-dropdown-item command="compare">修复前后对比</el-dropdown-item>
                        <el-dropdown-item command="result">文物复原成果</el-dropdown-item>
                        <el-dropdown-item command="monitor">后续监测</el-dropdown-item>
                        <el-dropdown-item command="archive2" divided>归档项目</el-dropdown-item>
                        <el-dropdown-item command="delete" style="color:#F56C6C">删除项目</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </div>
</template>

<style scoped>
.proj-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 18px; transition: box-shadow .15s; display: flex; flex-direction: column; gap: 12px; }
.proj-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.card-top { display: flex; justify-content: space-between; align-items: center; }
.card-artifact { font-size: 15px; font-weight: 700; color: #1D2129; }
.card-code { font-size: 12px; color: #909399; }
.card-info { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.info-row { display: flex; flex-direction: column; }
.info-row label { font-size: 11px; color: #C0C4CC; }
.info-row span { font-size: 12px; color: #4E5969; }
.disease-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.no-disease { font-size: 12px; color: #C0C4CC; }
.card-progress { padding: 0; }
.comparison-summary { padding: 9px 10px; border-left: 3px solid #5d8a7b; background: #f3f8f6; }
.comparison-summary b, .comparison-summary span, .comparison-summary small { display: block; }
.comparison-summary b { color: #3f6256; font-size: 11px; }
.comparison-summary span { margin-top: 4px; color: #788680; font-size: 10px; }
.comparison-summary small { margin-top: 5px; color: #a46d37; }
.restoration-summary { padding: 9px 10px; border-left: 3px solid #a37648; background: #faf6f0; }
.restoration-summary b, .restoration-summary span, .restoration-summary small { display: block; }
.restoration-summary b { color: #70553b; font-size: 11px; }
.restoration-summary span, .restoration-summary small { margin-top: 4px; color: #8c7965; font-size: 10px; }
.monitoring-summary { padding: 9px 10px; border-left: 3px solid #b77a39; background: #fff8ed; }
.monitoring-summary b, .monitoring-summary span, .monitoring-summary small { display: block; }
.monitoring-summary b { color: #76522f; font-size: 11px; }
.monitoring-summary span, .monitoring-summary small { margin-top: 4px; color: #8a7560; font-size: 10px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-risk { font-size: 12px; }
.card-time { font-size: 11px; color: #C0C4CC; }
.card-actions { display: flex; gap: 6px; padding-top: 8px; border-top: 1px solid #F0F0F0; }
</style>
