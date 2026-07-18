<!-- 统计概览卡片 -->
<script setup>
import { computed } from 'vue'
import { Collection, Clock, Setting, Tools, Finished, Monitor, Warning } from '@element-plus/icons-vue'

const props = defineProps({ statistics: Object, activeStatKey: String })
const emit = defineEmits(['filter'])

const cards = computed(() => [
    { key: 'total', label: '项目总数', value: props.statistics?.total || 0, icon: Collection, color: '#409EFF' },
    { key: 'pendingSurvey', label: '待调查', value: props.statistics?.pendingSurvey || 0, icon: Clock, color: '#909399' },
    { key: 'planning', label: '方案制定中', value: props.statistics?.planning || 0, icon: Setting, color: '#E6A23C' },
    { key: 'repairing', label: '修复进行中', value: props.statistics?.repairing || 0, icon: Tools, color: '#409EFF' },
    { key: 'restoring', label: '复原进行中', value: props.statistics?.restoring || 0, icon: Tools, color: '#67C23A' },
    { key: 'completed', label: '已完成', value: props.statistics?.completed || 0, icon: Finished, color: '#67C23A' },
    { key: 'monitoring', label: '监测中', value: props.statistics?.monitoring || 0, icon: Monitor, color: '#B37FEB' },
    { key: 'highRisk', label: '高风险项目', value: props.statistics?.highRisk || 0, icon: Warning, color: '#F56C6C' },
])
</script>

<template>
    <div class="stats-grid">
        <div v-for="c in cards" :key="c.key" class="stat-card"
             :class="{ active: activeStatKey === c.key }"
             @click="emit('filter', activeStatKey === c.key ? '' : c.key)">
            <div class="stat-icon" :style="{ background: c.color + '18', color: c.color }">
                <el-icon :size="22"><component :is="c.icon" /></el-icon>
            </div>
            <div class="stat-body">
                <div class="stat-value">{{ c.value }}</div>
                <div class="stat-label">{{ c.label }}</div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 12px; padding: 16px; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; cursor: pointer; transition: all .15s; }
.stat-card:hover { border-color: #409EFF; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-card.active { border-color: #409EFF; background: #ECF5FF; }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-value { font-size: 22px; font-weight: 700; color: #1D2129; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; white-space: nowrap; }
</style>
