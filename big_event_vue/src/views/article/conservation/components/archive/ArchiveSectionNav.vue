<script setup>
defineProps({
    sections: { type: Array, default: () => [] },
    activeSection: { type: String, default: '' }
})

defineEmits(['select'])

const stateMap = {
    completed: { icon: '●', text: '已完成', className: 'completed' },
    partial: { icon: '◐', text: '部分完成', className: 'partial' },
    pending: { icon: '○', text: '未开始', className: 'pending' },
    missing: { icon: '!', text: '数据缺失', className: 'missing' },
    updated: { icon: '↻', text: '引用已更新', className: 'updated' }
}
</script>

<template>
    <nav class="section-nav">
        <div class="nav-heading">
            <strong>档案目录</strong>
            <span>13个章节</span>
        </div>
        <button
            v-for="(section, index) in sections"
            :key="section.code"
            type="button"
            class="nav-item"
            :class="{ active: activeSection === section.code }"
            @click="$emit('select', section.code)"
        >
            <span class="index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="nav-content">
                <span class="name">{{ section.name }}</span>
                <span class="state" :class="stateMap[section.status]?.className">
                    {{ stateMap[section.status]?.icon }} {{ stateMap[section.status]?.text }}
                    <em v-if="section.sourceType !== 'manual' && section.sourceType !== 'attachment'">引用</em>
                    <em v-else-if="section.editable">可编辑</em>
                </span>
            </span>
        </button>
    </nav>
</template>

<style scoped>
.section-nav { height: 100%; overflow-y: auto; padding: 10px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; }
.nav-heading { display: flex; align-items: baseline; justify-content: space-between; padding: 6px 8px 12px; border-bottom: 1px solid #f0f2f5; }
.nav-heading strong { color: #1d2129; font-size: 14px; }
.nav-heading span { color: #a0a5ad; font-size: 11px; }
.nav-item { width: 100%; display: flex; gap: 10px; margin-top: 4px; padding: 10px 8px; text-align: left; background: transparent; border: 0; border-radius: 7px; cursor: pointer; transition: .15s ease; }
.nav-item:hover { background: #f5f7fa; }
.nav-item.active { background: #eaf3ff; box-shadow: inset 3px 0 #1668c4; }
.index { flex: none; width: 25px; color: #a0a5ad; font-size: 12px; font-weight: 700; line-height: 20px; }
.active .index, .active .name { color: #1668c4; }
.nav-content { min-width: 0; display: flex; flex: 1; flex-direction: column; gap: 5px; }
.name { color: #303133; font-size: 12px; font-weight: 600; line-height: 20px; }
.state { color: #a0a5ad; font-size: 10px; }
.state.completed { color: #67c23a; }
.state.partial { color: #e6a23c; }
.state.missing { color: #f56c6c; }
.state.updated { color: #409eff; }
.state em { margin-left: 5px; padding: 1px 4px; color: #86909c; background: #f2f3f5; border-radius: 4px; font-style: normal; }
</style>
