<script setup>
defineProps({
    step: { type: Object, default: null },
    completeness: { type: Number, default: 0 },
    differences: { type: Array, default: () => [] },
    problems: { type: Object, default: () => ({}) },
    previousStep: { type: Object, default: null },
    nextStep: { type: Object, default: null }
})

defineEmits(['section', 'disease', 'previous', 'next', 'close', 'plan-change'])

const severityMap = {
    minor: ['轻微', 'info'], moderate: ['中度', 'warning'],
    severe: ['严重', 'danger'], critical: ['危急', 'danger']
}
</script>

<template>
    <aside v-if="step" class="assist-panel">
        <div class="assist-heading"><strong>执行辅助</strong><el-button class="close-btn" link @click="$emit('close')">收起</el-button></div>

        <section>
            <div class="section-title"><span>关联病害</span><b>{{ step.relatedDiseases?.length || 0 }}</b></div>
            <button v-for="item in step.relatedDiseases" :key="item.id" class="disease-item" @click="$emit('disease', item)">
                <span><b>{{ item.diseaseName }}</b><small>{{ item.developmentStatus }} · {{ item.partName }}</small></span>
                <el-tag :type="severityMap[item.severity]?.[1]" size="small">{{ severityMap[item.severity]?.[0] }}</el-tag>
            </button>
            <p v-if="!step.relatedDiseases?.length" class="empty-text">本步骤未关联病害</p>
        </section>

        <section>
            <div class="section-title"><span>计划与实际差异</span><b :class="{ danger: differences.length }">{{ differences.length }}</b></div>
            <div v-if="differences.length" class="difference-list">
                <div v-for="item in differences" :key="item.key">
                    <el-tag :type="item.level === 'major' ? 'danger' : item.level === 'general' ? 'warning' : 'info'" size="small">{{ item.levelText }}</el-tag>
                    <span>{{ item.label }}</span>
                    <small>{{ item.description }}</small>
                </div>
                <el-button v-if="differences.some(item => item.level === 'major')" link type="danger" @click="$emit('plan-change')">申请方案变更</el-button>
            </div>
            <p v-else class="empty-text">暂无计划与实际差异</p>
        </section>

        <section>
            <div class="section-title"><span>步骤完整度</span><b>{{ completeness }}%</b></div>
            <el-progress :percentage="completeness" :stroke-width="10" :color="completeness >= 80 ? '#67c23a' : '#1668c4'" />
            <p class="hint">{{ completeness >= 80 ? '已达到步骤完成度建议值' : `距完成建议值还差 ${80 - completeness}%` }}</p>
        </section>

        <section>
            <div class="section-title"><span>当前问题</span></div>
            <div class="problem-grid">
                <button @click="$emit('section', 'issues')"><b :class="{ danger: problems.unresolvedIssues }">{{ problems.unresolvedIssues || 0 }}</b><span>未解决异常</span></button>
                <button @click="$emit('section', 'materials')"><b :class="{ warning: problems.pendingDeviations }">{{ problems.pendingDeviations || 0 }}</b><span>待确认偏差</span></button>
                <button @click="$emit('section', 'checks')"><b :class="{ danger: problems.failedChecks }">{{ problems.failedChecks || 0 }}</b><span>质检失败</span></button>
                <button @click="$emit('section', 'issues')"><b>{{ problems.newDiseases || 0 }}</b><span>新发现病害</span></button>
            </div>
        </section>

        <section>
            <div class="section-title"><span>快速切换</span></div>
            <div class="step-switch">
                <button :disabled="!previousStep" @click="$emit('previous')"><small>上一步</small><b>{{ previousStep?.stepName || '无' }}</b></button>
                <button :disabled="!nextStep" @click="$emit('next')"><small>下一步</small><b>{{ nextStep?.stepName || '无' }}</b></button>
            </div>
        </section>
    </aside>
</template>

<style scoped>
.assist-panel { height: 100%; overflow-y: auto; padding: 14px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-sizing: border-box; }
.assist-heading, .section-title { display: flex; align-items: center; justify-content: space-between; }
.assist-heading { padding-bottom: 10px; color: #1d2129; font-size: 13px; border-bottom: 1px solid #f0f2f5; }
.close-btn { display: none; }
section { padding: 14px 1px; border-bottom: 1px solid #f0f2f5; }
section:last-child { border-bottom: 0; }
.section-title { margin-bottom: 9px; color: #4e5969; font-size: 11px; }
.section-title b { color: #1668c4; font-size: 15px; }
.section-title .danger, .danger { color: #f56c6c; }
.warning { color: #e6a23c; }
.disease-item { width: 100%; display: flex; align-items: center; justify-content: space-between; gap: 7px; margin-top: 5px; padding: 8px; text-align: left; background: #f7f8fa; border: 0; border-radius: 5px; cursor: pointer; }
.disease-item > span { display: flex; flex-direction: column; gap: 3px; }
.disease-item b { color: #303133; font-size: 11px; }
.disease-item small, .difference-list small { color: #86909c; font-size: 9px; }
.difference-list { display: flex; flex-direction: column; gap: 7px; }
.difference-list > div { display: grid; grid-template-columns: auto 1fr; gap: 3px 6px; padding: 7px; background: #fff8ee; border-radius: 5px; }
.difference-list span { color: #606266; font-size: 10px; }
.difference-list small { grid-column: 2; }
.empty-text, .hint { margin: 7px 0 0; color: #a0a5ad; font-size: 10px; }
.problem-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.problem-grid button { display: flex; flex-direction: column; gap: 2px; padding: 8px; text-align: left; background: #f7f8fa; border: 0; border-radius: 5px; cursor: pointer; }
.problem-grid b { color: #1d2129; font-size: 15px; }
.problem-grid span { color: #86909c; font-size: 9px; }
.step-switch { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.step-switch button { min-width: 0; display: flex; flex-direction: column; gap: 4px; padding: 8px; text-align: left; background: #f7f8fa; border: 1px solid #e5e6eb; border-radius: 5px; cursor: pointer; }
.step-switch button:disabled { opacity: .5; cursor: not-allowed; }
.step-switch small { color: #a0a5ad; font-size: 9px; }
.step-switch b { color: #4e5969; font-size: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 1280px) { .close-btn { display: inline-flex; } }
</style>
