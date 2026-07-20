<!-- 左侧病害记录列表 -->
<script setup>
import { ref, computed } from 'vue'
import { Delete, Plus, Search } from '@element-plus/icons-vue'

const props = defineProps({ records: Array, activeId: Number })
const emit = defineEmits(['select', 'add', 'delete'])

const searchQuery = ref('')
const severityFilter = ref('')

const severities = [
    { label: '全部程度', value: '' }, { label: '严重/危急', value: 'severe' }, { label: '中度', value: 'moderate' }, { label: '轻微', value: 'minor' }
]
const filtered = computed(() => {
    let list = props.records || []
    if (searchQuery.value) { const q = searchQuery.value.toLowerCase(); list = list.filter(r => (r.diseaseName || '').toLowerCase().includes(q) || (r.partName || '').toLowerCase().includes(q)) }
    if (severityFilter.value === 'severe') list = list.filter(r => r.severity === 'severe' || r.severity === 'critical')
    else if (severityFilter.value) list = list.filter(r => r.severity === severityFilter.value)
    return list
})
const severityType = (s) => ({ critical: 'danger', severe: 'danger', moderate: 'warning', minor: 'info' }[s] || 'info')
const severityText = (s) => ({ critical: '危急', severe: '严重', moderate: '中度', minor: '轻微' }[s] || s)
</script>

<template>
    <div class="record-list">
        <div class="list-toolbar">
            <el-input v-model="searchQuery" :prefix-icon="Search" placeholder="搜索病害" size="small" clearable />
            <el-select v-model="severityFilter" size="small" style="width:100%">
                <el-option v-for="s in severities" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-button size="small" :icon="Plus" type="primary" @click="emit('add')" style="width:100%">新增病害</el-button>
        </div>
        <div class="list-scroll">
            <div v-if="!filtered.length" class="empty-list">暂无病害记录</div>
            <div v-for="r in filtered" :key="r.id" class="disease-card" :class="{ active: r.id === activeId }" @click="emit('select', r)">
                <div class="dc-top">
                    <span class="dc-name">{{ r.diseaseName || '未命名' }}</span>
                    <el-tag :type="severityType(r.severity)" size="small">{{ severityText(r.severity) }}</el-tag>
                </div>
                <div class="dc-part">{{ r.partName || '-' }}{{ r.side ? ' · ' + r.side : '' }}</div>
                <div class="dc-dev">{{ ({ stable: '稳定', slowly_developing: '缓慢发展', rapidly_developing: '快速发展' })[r.developmentStatus] || r.developmentStatus || '-' }}</div>
                <div class="dc-footer">
                    <span>图片 {{ r.mediaCount || 0 }} 张</span>
                    <div class="dc-actions">
                        <el-tag v-if="r.emergency" type="danger" size="small" effect="dark">紧急</el-tag>
                        <el-button
                            :icon="Delete"
                            type="danger"
                            text
                            size="small"
                            @click.stop="emit('delete', r)"
                        />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.record-list { width: 290px; flex-shrink: 0; display: flex; flex-direction: column; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; }
.list-toolbar { padding: 12px; display: flex; flex-direction: column; gap: 8px; border-bottom: 1px solid #F0F0F0; }
.list-scroll { flex: 1; overflow-y: auto; padding: 8px; }
.empty-list { text-align: center; padding: 30px 0; color: #C0C4CC; font-size: 13px; }
.disease-card { padding: 12px; border: 1px solid #E5E6EB; border-radius: 8px; margin-bottom: 8px; cursor: pointer; transition: all .15s; }
.disease-card:hover { border-color: #409EFF; }
.disease-card.active { border-color: #409EFF; background: #ECF5FF; }
.dc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.dc-name { font-size: 14px; font-weight: 600; color: #1D2129; }
.dc-part { font-size: 12px; color: #4E5969; margin-bottom: 4px; }
.dc-dev { font-size: 11px; color: #E6A23C; margin-bottom: 6px; }
.dc-footer { display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: #C0C4CC; }
.dc-actions { display: flex; align-items: center; gap: 4px; }
</style>
