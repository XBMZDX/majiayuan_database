<script setup>
import { computed } from 'vue'
import { ArrowLeft, Check, Collection, Files, FolderAdd, Menu, Plus, Promotion } from '@element-plus/icons-vue'

const props = defineProps({
    project: { type: Object, required: true },
    groups: { type: Array, default: () => [] },
    current: { type: Object, default: null },
    saving: Boolean,
    dirty: Boolean
})
defineEmits(['create', 'generate', 'save', 'complete', 'batch-archive', 'process', 'archive', 'restoration', 'open-list'])

const stats = computed(() => ({
    draft: props.groups.filter(item => item.evaluationStatus === 'draft').length,
    completed: props.groups.filter(item => ['completed', 'reviewed', 'archived'].includes(item.evaluationStatus)).length,
    archived: props.groups.filter(item => item.selectedForArchive).length,
    baseline: props.groups.filter(item => item.selectedAsMonitoringBaseline).length
}))
</script>

<template>
    <header class="comparison-summary">
        <div class="summary-main">
            <el-button class="mobile-list-button" circle :icon="Menu" @click="$emit('open-list')" />
            <div>
                <div class="eyebrow">CONSERVATION COMPARISON WORKBENCH</div>
                <h1>{{ project.artifactCode }} {{ project.artifactName }}修复前后对比</h1>
                <p>{{ project.projectCode }} · {{ project.projectName }} · 最近更新 {{ current?.updateTime || '尚未保存' }}</p>
            </div>
        </div>
        <div class="stats">
            <div><b>{{ groups.length }}</b><span>对比组</span></div>
            <div><b>{{ stats.draft }}</b><span>草稿/待评价</span></div>
            <div><b>{{ stats.completed }}</b><span>已完成</span></div>
            <div><b>{{ stats.archived }}</b><span>收入档案</span></div>
            <div><b>{{ stats.baseline }}</b><span>监测基准</span></div>
        </div>
        <div class="actions">
            <el-button type="primary" :icon="Plus" @click="$emit('create')">新建对比组</el-button>
            <el-button :icon="Promotion" @click="$emit('generate')">从过程影像生成</el-button>
            <el-button v-if="current" :loading="saving" :type="dirty ? 'warning' : 'default'" :icon="Check" @click="$emit('save')">
                {{ dirty ? '保存草稿*' : '保存草稿' }}
            </el-button>
            <el-button type="primary" plain @click="$emit('restoration')">保存并进入复原成果</el-button>
            <el-button v-if="current?.evaluationStatus === 'draft'" type="success" :icon="Collection" @click="$emit('complete')">完成评价</el-button>
            <el-dropdown>
                <el-button :icon="FolderAdd">联动操作</el-button>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item :icon="Files" @click="$emit('batch-archive')">批量收入档案</el-dropdown-item>
                        <el-dropdown-item :icon="ArrowLeft" @click="$emit('process')">返回修复过程</el-dropdown-item>
                        <el-dropdown-item :icon="FolderAdd" @click="$emit('archive')">返回保护修复档案</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </header>
</template>

<style scoped>
.comparison-summary {
    min-height: 112px;
    display: grid;
    grid-template-columns: minmax(300px, 1fr) auto;
    grid-template-areas: "main stats" "main actions";
    gap: 10px 24px;
    align-items: center;
    padding: 14px 20px;
    background: #fff;
    border-bottom: 1px solid #e5e8eb;
    box-shadow: 0 3px 14px rgb(28 45 39 / 5%);
    position: relative;
    z-index: 4;
}
.summary-main { grid-area: main; display: flex; align-items: center; gap: 10px; min-width: 0; }
.eyebrow { color: #a47846; font-size: 10px; letter-spacing: .14em; font-weight: 700; }
h1 { margin: 4px 0; color: #23352f; font-size: 20px; line-height: 1.3; }
p { margin: 0; color: #84908c; font-size: 12px; }
.stats { grid-area: stats; display: flex; justify-content: flex-end; gap: 0; }
.stats > div { min-width: 76px; padding: 0 15px; text-align: center; border-left: 1px solid #edf0ef; }
.stats b, .stats span { display: block; }
.stats b { color: #2f6152; font-size: 18px; }
.stats span { color: #8c9692; font-size: 10px; }
.actions { grid-area: actions; display: flex; justify-content: flex-end; flex-wrap: wrap; gap: 8px; }
.mobile-list-button { display: none; }
@media (max-width: 1200px) {
    .comparison-summary { grid-template-columns: 1fr; grid-template-areas: "main" "stats" "actions"; }
    .stats, .actions { justify-content: flex-start; }
    .stats > div:first-child { border-left: 0; padding-left: 0; }
}
@media (max-width: 920px) {
    .comparison-summary { padding: 12px 14px; }
    .mobile-list-button { display: inline-flex; flex: 0 0 auto; }
    .stats { overflow-x: auto; }
    .stats > div { min-width: 68px; padding: 0 10px; }
    .actions :deep(.el-button) { margin-left: 0; }
}
</style>
