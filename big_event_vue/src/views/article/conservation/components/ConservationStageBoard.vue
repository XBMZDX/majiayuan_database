<!-- 项目阶段看板 -->
<script setup>
const props = defineProps({ stageStats: Object, activeStage: String })
const emit = defineEmits(['filter'])

const stages = [
    { key: 'pendingSurvey', label: '待调查' },
    { key: 'disease', label: '病害调查' },
    { key: 'planning', label: '方案制定' },
    { key: 'protection', label: '保护处理中' },
    { key: 'repair', label: '修复处理中' },
    { key: 'restoration', label: '复原处理中' },
    { key: 'evaluation', label: '效果评估' },
    { key: 'completed', label: '已完成' },
    { key: 'monitoring', label: '后续监测' },
]
</script>

<template>
    <div class="stage-board">
        <div v-for="(s, i) in stages" :key="s.key" style="display: flex; align-items: center; gap: 0;">
            <div class="stage-item" :class="{ active: activeStage === s.key }"
                 @click="emit('filter', activeStage === s.key ? '' : s.key)">
                <div class="stage-label">{{ s.label }}</div>
                <div class="stage-count">{{ props.stageStats?.[s.key] || 0 }}</div>
            </div>
            <span v-if="i < stages.length - 1" class="stage-arrow">→</span>
        </div>
    </div>
</template>

<style scoped>
.stage-board { display: flex; align-items: center; gap: 0; padding: 16px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; margin-bottom: 20px; overflow-x: auto; white-space: nowrap; }
.stage-item { padding: 10px 14px; border-radius: 8px; cursor: pointer; text-align: center; transition: all .15s; min-width: 70px; }
.stage-item:hover { background: #F5F7FA; }
.stage-item.active { background: #ECF5FF; }
.stage-label { font-size: 12px; color: #909399; margin-bottom: 2px; }
.stage-item.active .stage-label { color: #409EFF; font-weight: 600; }
.stage-count { font-size: 18px; font-weight: 700; color: #1D2129; }
.stage-item.active .stage-count { color: #409EFF; }
.stage-arrow { color: #C0C4CC; margin: 0 2px; flex-shrink: 0; }
</style>
