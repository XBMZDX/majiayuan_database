<script setup>
import { computed, ref } from 'vue'
import { ArrowDown, ArrowUp, Delete, EditPen, Plus, Refresh, Search } from '@element-plus/icons-vue'

const props = defineProps({
    steps: { type: Array, default: () => [] },
    activeId: { type: [Number, String], default: null },
    editMode: Boolean
})

defineEmits(['select', 'add', 'sync', 'toggle-edit', 'move', 'delete', 'skip'])

const keyword = ref('')
const status = ref('')
const filtered = computed(() => props.steps.filter(step => {
    const key = keyword.value.trim().toLowerCase()
    const textMatch = !key || [step.stepCode, step.stepName, step.operatorName]
        .some(value => String(value || '').toLowerCase().includes(key))
    return textMatch && (!status.value || step.stepStatus === status.value)
}))

const statusMap = {
    pending: ['待开始', 'info'], in_progress: ['进行中', 'primary'],
    completed: ['已完成', 'success'], paused: ['已暂停', 'warning'],
    rework: ['需返工', 'danger'], skipped: ['已跳过', 'info'], cancelled: ['已取消', 'info']
}
</script>

<template>
    <aside class="step-panel">
        <div class="panel-title">
            <div><strong>修复步骤流程</strong><span>{{ steps.length }}个步骤</span></div>
            <el-button link type="primary" :icon="EditPen" @click="$emit('toggle-edit')">{{ editMode ? '完成调整' : '调整流程' }}</el-button>
        </div>
        <div class="filters">
            <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索步骤" clearable />
            <el-select v-model="status" placeholder="全部状态" clearable>
                <el-option v-for="(item, key) in statusMap" :key="key" :label="item[0]" :value="key" />
            </el-select>
        </div>
        <div class="toolbar">
            <el-button size="small" :icon="Refresh" @click="$emit('sync')">同步方案</el-button>
            <el-button size="small" type="primary" :icon="Plus" @click="$emit('add')">临时步骤</el-button>
        </div>

        <div class="step-flow">
            <button
                v-for="step in filtered"
                :key="step.id"
                type="button"
                class="step-card"
                :class="{ active: activeId === step.id, completed: step.stepStatus === 'completed' }"
                @click="$emit('select', step.id)"
            >
                <span class="flow-line"></span>
                <span class="step-dot"></span>
                <span class="step-content">
                    <span class="step-head">
                        <b>{{ String(step.sequenceNo).padStart(2, '0') }} {{ step.stepName }}</b>
                        <el-tag :type="statusMap[step.stepStatus]?.[1]" size="small">{{ statusMap[step.stepStatus]?.[0] }}</el-tag>
                    </span>
                    <span class="step-type">{{ step.stepCode }} · {{ step.stepType }}</span>
                    <span class="step-meta">
                        <em>人员 {{ step.operatorName || '待配置' }}</em>
                        <em>病害 {{ step.relatedDiseases?.length || 0 }}</em>
                        <em>影像 {{ step.media?.length || 0 }}</em>
                        <em :class="{ danger: step.issues?.some(item => !['resolved','closed'].includes(item.issueStatus)) }">异常 {{ step.issues?.filter(item => !['resolved','closed'].includes(item.issueStatus)).length || 0 }}</em>
                    </span>
                    <span class="complete-row">
                        <el-progress :percentage="step.completionRate || 0" :stroke-width="5" :show-text="false" />
                        <small>{{ step.completionRate || 0 }}%</small>
                        <el-tag v-if="step.temporaryStep" type="warning" effect="plain" size="small">临时新增</el-tag>
                    </span>
                    <span v-if="editMode && ['pending','skipped'].includes(step.stepStatus)" class="edit-actions" @click.stop>
                        <el-button link :icon="ArrowUp" @click="$emit('move', step.id, -1)" />
                        <el-button link :icon="ArrowDown" @click="$emit('move', step.id, 1)" />
                        <el-button v-if="step.stepStatus === 'pending'" link @click="$emit('skip', step.id)">跳过</el-button>
                        <el-button link type="danger" :icon="Delete" @click="$emit('delete', step.id)" />
                    </span>
                </span>
            </button>
        </div>
        <el-empty v-if="!filtered.length" description="暂无匹配步骤" :image-size="55" />
    </aside>
</template>

<style scoped>
.step-panel { height: 100%; display: flex; flex-direction: column; overflow: hidden; padding: 11px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-sizing: border-box; }
.panel-title, .panel-title > div, .filters, .toolbar, .step-head, .step-meta, .complete-row, .edit-actions { display: flex; align-items: center; }
.panel-title { justify-content: space-between; padding: 3px 5px 10px; border-bottom: 1px solid #f0f2f5; }
.panel-title > div { gap: 7px; }
.panel-title strong { color: #1d2129; font-size: 13px; }
.panel-title span { color: #a0a5ad; font-size: 10px; }
.filters { gap: 6px; margin-top: 9px; }
.filters :deep(.el-select) { width: 112px; flex: none; }
.toolbar { gap: 6px; margin-top: 7px; }
.step-flow { flex: 1; overflow-y: auto; margin-top: 8px; padding: 0 2px 20px 13px; }
.step-card { width: 100%; position: relative; display: flex; padding: 10px 8px 10px 18px; text-align: left; background: transparent; border: 0; border-radius: 7px; cursor: pointer; transition: .15s ease; }
.step-card:hover { background: #f7f8fa; }
.step-card.active { background: #eaf3ff; box-shadow: inset 3px 0 #1668c4; }
.flow-line { width: 1px; position: absolute; top: 0; bottom: 0; left: 7px; background: #dcdfe6; }
.step-card:first-child .flow-line { top: 18px; }
.step-card:last-child .flow-line { bottom: calc(100% - 18px); }
.step-dot { width: 9px; height: 9px; position: absolute; top: 17px; left: 3px; z-index: 1; background: #c0c4cc; border: 2px solid #fff; border-radius: 50%; box-shadow: 0 0 0 1px #c0c4cc; }
.step-card.active .step-dot { background: #1668c4; box-shadow: 0 0 0 1px #1668c4; }
.step-card.completed .step-dot { background: #67c23a; box-shadow: 0 0 0 1px #67c23a; }
.step-content { min-width: 0; display: flex; flex: 1; flex-direction: column; gap: 5px; }
.step-head { justify-content: space-between; gap: 7px; }
.step-head b { color: #303133; font-size: 11px; line-height: 1.45; }
.step-type { color: #a0a5ad; font-size: 9px; }
.step-meta { flex-wrap: wrap; gap: 3px 9px; color: #86909c; font-size: 9px; }
.step-meta em { font-style: normal; }
.step-meta .danger { color: #f56c6c; }
.complete-row { gap: 6px; }
.complete-row :deep(.el-progress) { min-width: 70px; flex: 1; }
.complete-row small { width: 28px; color: #86909c; font-size: 9px; }
.edit-actions { gap: 2px; padding-top: 3px; border-top: 1px dashed #dcdfe6; }
</style>
