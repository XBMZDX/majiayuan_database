<script setup>
import { computed } from 'vue'
import { CircleCheck, FolderOpened, Menu, MoreFilled, VideoPause, VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
    project: { type: Object, required: true },
    process: { type: Object, required: true },
    currentStep: { type: Object, default: null },
    progress: { type: Number, default: 0 },
    completedSteps: { type: Number, default: 0 },
    unresolvedIssues: { type: Number, default: 0 },
    saving: Boolean,
    dirty: Boolean
})

defineEmits(['save', 'start-step', 'complete-step', 'pause', 'resume', 'archive', 'plan', 'rework', 'open-nav'])

const processStatus = computed(() => {
    const map = {
        not_started: ['未开始', 'info'], in_progress: ['进行中', 'primary'],
        paused: ['已暂停', 'warning'], completed: ['已完成', 'success'],
        terminated: ['已终止', 'danger']
    }
    const value = map[props.process.processStatus] || [props.process.processStatus, 'info']
    return { text: value[0], type: value[1] }
})

const stageText = computed(() => {
    const map = { planning: '方案制定', repair: '修复处理中', evaluation: '效果评估', completed: '已完成' }
    return map[props.project.currentStage] || props.project.currentStage || '-'
})
</script>

<template>
    <header class="process-summary">
        <div class="summary-main">
            <div class="title-row">
                <el-button class="mobile-nav" :icon="Menu" circle @click="$emit('open-nav')" />
                <div>
                    <div class="eyebrow">保护修复执行与过程记录 <span v-if="dirty">未保存</span></div>
                    <h2>{{ process.processName }}</h2>
                </div>
            </div>
            <div class="meta-row">
                <span>过程编号 <b>{{ process.processCode }}</b></span>
                <span>文物 <b>{{ project.artifactCode }} · {{ project.artifactName }}</b></span>
                <span>项目 <b>{{ project.projectCode }}</b></span>
                <span>阶段 <b>{{ stageText }}</b></span>
                <el-tag :type="processStatus.type" size="small">{{ processStatus.text }}</el-tag>
                <span>当前步骤 <b>{{ currentStep?.stepName || '未选择' }}</b></span>
            </div>
            <div class="progress-row">
                <el-progress :percentage="progress" :stroke-width="8" />
                <span>{{ completedSteps }}/{{ process.totalSteps }} 步骤完成</span>
                <span>负责人：{{ process.supervisor || '-' }}</span>
                <span :class="{ danger: unresolvedIssues }">未解决异常：{{ unresolvedIssues }}项</span>
                <span>更新：{{ process.updateTime || '-' }}</span>
            </div>
        </div>

        <div class="summary-actions">
            <el-button :icon="FolderOpened" :loading="saving" @click="$emit('save')">保存当前步骤</el-button>
            <el-button v-if="currentStep?.stepStatus === 'pending' && process.processStatus !== 'paused'" type="primary" :icon="VideoPlay" @click="$emit('start-step')">开始步骤</el-button>
            <el-button v-if="currentStep?.stepStatus === 'in_progress'" type="success" :icon="CircleCheck" @click="$emit('complete-step')">完成步骤</el-button>
            <el-button v-if="process.processStatus === 'in_progress'" type="warning" :icon="VideoPause" @click="$emit('pause')">暂停过程</el-button>
            <el-button v-if="process.processStatus === 'paused'" type="primary" :icon="VideoPlay" @click="$emit('resume')">恢复过程</el-button>
            <el-dropdown @command="command => $emit(command)">
                <el-button :icon="MoreFilled">更多</el-button>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="plan">查看正式方案</el-dropdown-item>
                        <el-dropdown-item v-if="currentStep?.stepStatus === 'completed'" command="rework">申请返工</el-dropdown-item>
                        <el-dropdown-item command="archive" divided>返回保护修复档案</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </header>
</template>

<style scoped>
.process-summary { flex: none; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 14px 18px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-shadow: 0 2px 10px rgba(29,33,41,.04); }
.summary-main { min-width: 0; flex: 1; }
.title-row { display: flex; align-items: center; gap: 10px; }
.eyebrow { margin-bottom: 4px; color: #86909c; font-size: 11px; }
.eyebrow span { margin-left: 7px; color: #e6a23c; font-weight: 600; }
h2 { margin: 0; color: #1d2129; font-size: 18px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta-row, .progress-row { display: flex; align-items: center; flex-wrap: wrap; gap: 6px 15px; margin-top: 8px; color: #606266; font-size: 11px; }
.meta-row b { color: #303133; }
.progress-row :deep(.el-progress) { width: 190px; }
.progress-row .danger { color: #f56c6c; font-weight: 600; }
.summary-actions { display: flex; flex: none; align-items: center; justify-content: flex-end; flex-wrap: wrap; gap: 7px; max-width: 460px; }
.mobile-nav { display: none; }
@media (max-width: 1080px) {
    .process-summary { align-items: flex-start; }
    .summary-actions { max-width: 300px; }
}
@media (max-width: 920px) {
    .process-summary { flex-direction: column; }
    .summary-actions { width: 100%; max-width: none; justify-content: flex-start; }
    .mobile-nav { display: inline-flex; }
}
</style>
