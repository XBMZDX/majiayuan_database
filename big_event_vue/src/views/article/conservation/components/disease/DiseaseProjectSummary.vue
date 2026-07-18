<!-- 顶部项目摘要栏 -->
<script setup>
import { computed } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({ project: Object, survey: Object, diseaseCount: Number, severeCount: Number })
const emit = defineEmits(['save', 'submit', 'back'])

const riskTag = computed(() => {
    const m = { high: 'danger', medium: 'warning', low: 'success' }
    const t = { high: '高风险', medium: '中风险', low: '低风险' }
    return { type: m[props.project?.riskLevel] || 'info', text: t[props.project?.riskLevel] || '-' }
})
const statusTag = computed(() => {
    const m = { draft: 'info', submitted: 'warning', reviewed: 'success', returned: 'danger' }
    const t = { draft: '草稿', submitted: '已提交', reviewed: '已审核', returned: '退回' }
    return { type: m[props.survey?.status] || 'info', text: t[props.survey?.status] || '草稿' }
})
</script>

<template>
    <div class="summary-bar">
        <div class="sum-left">
            <el-button :icon="ArrowLeft" text @click="emit('back')">返回总览</el-button>
            <div class="sum-info">
                <span class="sum-project">{{ project?.projectCode || '-' }} {{ project?.projectName || '-' }}</span>
                <span class="sum-divider">|</span>
                <span>文物：{{ project?.artifactCode }} {{ project?.artifactName }}</span>
                <span class="sum-divider">|</span>
                <span>材质：{{ project?.material || '-' }}</span>
                <span class="sum-divider">|</span>
                <span>风险：<el-tag :type="riskTag.type" size="small">{{ riskTag.text }}</el-tag></span>
                <span class="sum-divider">|</span>
                <span>调查状态：<el-tag :type="statusTag.type" size="small">{{ statusTag.text }}</el-tag></span>
                <span class="sum-divider">|</span>
                <span>病害：{{ diseaseCount }} 项</span>
                <span v-if="severeCount > 0" style="color:#F56C6C"> 严重：{{ severeCount }} 项</span>
            </div>
        </div>
        <div class="sum-actions">
            <el-button size="small" @click="emit('save')">保存草稿</el-button>
            <el-button size="small" type="primary" @click="emit('submit')">提交调查</el-button>
        </div>
    </div>
</template>

<style scoped>
.summary-bar { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.sum-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.sum-info { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; font-size: 13px; color: #4E5969; }
.sum-project { font-size: 14px; font-weight: 700; color: #1D2129; }
.sum-divider { color: #E5E6EB; }
.sum-actions { display: flex; gap: 8px; white-space: nowrap; }
</style>
