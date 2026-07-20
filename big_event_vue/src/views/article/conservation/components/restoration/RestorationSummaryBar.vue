<script setup>
import { computed } from 'vue'
import { Check, Connection, Files, Menu, Plus, Promotion } from '@element-plus/icons-vue'

const props = defineProps({
    project: { type: Object, required: true },
    results: { type: Array, default: () => [] },
    current: { type: Object, default: null },
    saving: Boolean,
    dirty: Boolean
})
defineEmits(['create', 'from-steps', 'from-comparisons', 'save', 'version', 'comparison', 'archive', 'open-list'])

const stats = computed(() => ({
    physical: props.results.filter(item => ['physical', 'hybrid'].includes(item.restorationType)).length,
    d2: props.results.filter(item => item.restorationType === 'digital_2d').length,
    d3: props.results.filter(item => ['digital_3d', 'hybrid'].includes(item.restorationType)).length,
    processing: props.results.filter(item => ['draft', 'in_progress'].includes(item.resultStatus)).length,
    completed: props.results.filter(item => item.resultStatus === 'completed').length,
    archived: props.results.filter(item => item.selectedForArchive).length,
    monitoring: props.results.filter(item => item.requiresMonitoring).length
}))
</script>

<template>
    <header class="summary">
        <div class="identity">
            <el-button class="mobile-menu" circle :icon="Menu" @click="$emit('open-list')" />
            <div><div class="eyebrow">ARTIFACT RESTORATION RESULT WORKBENCH</div><h1>{{ project.artifactCode }} {{ project.artifactName }}文物复原成果</h1><p>{{ project.projectCode }} · {{ project.projectName }} · 最近更新 {{ current?.updateTime || '尚未保存' }}</p></div>
        </div>
        <div class="stats">
            <div><b>{{ results.length }}</b><span>成果</span></div><div><b>{{ stats.physical }}</b><span>实体</span></div>
            <div><b>{{ stats.d2 }}</b><span>二维</span></div><div><b>{{ stats.d3 }}</b><span>三维</span></div>
            <div><b>{{ stats.processing }}</b><span>记录中</span></div><div><b>{{ stats.completed }}</b><span>资料完整</span></div>
            <div><b>{{ stats.archived }}</b><span>入档</span></div><div><b>{{ stats.monitoring }}</b><span>监测</span></div>
        </div>
        <div class="actions">
            <el-button type="primary" :icon="Plus" @click="$emit('create')">新建成果</el-button>
            <el-button :icon="Promotion" @click="$emit('from-steps')">从修复步骤生成</el-button>
            <el-button :icon="Connection" @click="$emit('from-comparisons')">从前后对比生成</el-button>
            <el-button v-if="current" :type="dirty ? 'warning' : 'default'" :loading="saving" :icon="Check" @click="$emit('save')">{{ dirty ? '保存当前成果*' : '保存当前成果' }}</el-button>
            <el-dropdown>
                <el-button :icon="Files">更多</el-button>
                <template #dropdown><el-dropdown-menu>
                    <el-dropdown-item :disabled="!current" @click="$emit('version')">生成新版本</el-dropdown-item>
                    <el-dropdown-item @click="$emit('comparison')">返回修复前后对比</el-dropdown-item>
                    <el-dropdown-item @click="$emit('archive')">返回保护修复档案</el-dropdown-item>
                </el-dropdown-menu></template>
            </el-dropdown>
        </div>
    </header>
</template>

<style scoped>
.summary { min-height: 112px; display: grid; grid-template-columns: minmax(300px,1fr) auto; grid-template-areas: "identity stats" "identity actions"; align-items: center; gap: 10px 22px; padding: 14px 20px; background: #fff; border-bottom: 1px solid #e2e6e4; box-shadow: 0 3px 14px rgb(27 45 39 / 5%); z-index: 4; }
.identity { grid-area: identity; display: flex; align-items: center; gap: 10px; min-width: 0; }.eyebrow { color: #9c7448; font-size: 10px; letter-spacing: .14em; font-weight: 700; }
h1 { margin: 4px 0; color: #253a33; font-size: 20px; }p { margin: 0; color: #87918e; font-size: 11px; }
.stats { grid-area: stats; display: flex; justify-content: flex-end; }.stats div { min-width: 56px; padding: 0 9px; text-align: center; border-left: 1px solid #edf0ef; }.stats b,.stats span { display: block; }.stats b { color: #396d5d; font-size: 18px; }.stats span { color: #929b98; font-size: 9px; }
.actions { grid-area: actions; display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 7px; }.actions :deep(.el-button) { margin-left: 0; }.mobile-menu { display: none; }
@media (max-width:1280px){.summary{grid-template-columns:1fr;grid-template-areas:"identity" "stats" "actions"}.stats,.actions{justify-content:flex-start}.stats div:first-child{border-left:0;padding-left:0}}
@media (max-width:920px){.summary{padding:12px 14px}.mobile-menu{display:inline-flex}.stats{overflow-x:auto}.stats div{min-width:55px;padding:0 7px}}
</style>
