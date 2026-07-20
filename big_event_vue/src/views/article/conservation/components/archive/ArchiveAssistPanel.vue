<script setup>
defineProps({
    completeness: { type: Number, default: 0 },
    missingItems: { type: Array, default: () => [] },
    summaries: { type: Array, default: () => [] },
    archive: { type: Object, required: true },
    revisionCount: { type: Number, default: 0 }
})

defineEmits(['select-section', 'show-revisions', 'close'])

const statusText = {
    draft: '草稿', compiling: '编制中', completed: '已定稿', archived: '已归档'
}
</script>

<template>
    <aside class="assist">
        <div class="assist-heading">
            <strong>档案辅助</strong>
            <el-button class="close-btn" link @click="$emit('close')">收起</el-button>
        </div>

        <section>
            <div class="section-title"><span>档案完整度</span><b>{{ completeness }}%</b></div>
            <el-progress :percentage="completeness" :stroke-width="10" :color="completeness >= 80 ? '#67c23a' : '#1668c4'" />
            <p class="hint">{{ completeness >= 80 ? '已达到建议定稿完整度' : `距定稿建议值还差 ${80 - completeness}%` }}</p>
        </section>

        <section>
            <div class="section-title"><span>缺失项</span><b class="danger">{{ missingItems.length }}</b></div>
            <div v-if="missingItems.length" class="missing-list">
                <button v-for="item in missingItems" :key="item.key" type="button" @click="$emit('select-section', item.section)">
                    <span>!</span>
                    <span>{{ item.label }}</span>
                </button>
            </div>
            <el-empty v-else description="暂无缺失项" :image-size="45" />
        </section>

        <section>
            <div class="section-title"><span>关联数据摘要</span></div>
            <div class="summary-grid">
                <div v-for="item in summaries" :key="item.label">
                    <b>{{ item.value }}</b>
                    <span>{{ item.label }}</span>
                </div>
            </div>
        </section>

        <section>
            <div class="section-title"><span>版本信息</span></div>
            <dl class="version-info">
                <div><dt>当前版本</dt><dd>{{ archive.currentVersion }}</dd></div>
                <div><dt>数据状态</dt><dd>{{ statusText[archive.archiveStatus] || archive.archiveStatus }}</dd></div>
                <div><dt>编制人</dt><dd>{{ archive.compiler || '-' }}</dd></div>
                <div><dt>最近修改</dt><dd>{{ archive.updateTime || '-' }}</dd></div>
            </dl>
            <el-button link type="primary" @click="$emit('show-revisions')">查看版本记录（{{ revisionCount }}）</el-button>
        </section>
    </aside>
</template>

<style scoped>
.assist { height: 100%; overflow-y: auto; padding: 14px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-shadow: -2px 0 12px rgba(29, 33, 41, .04); }
.assist-heading, .section-title { display: flex; align-items: center; justify-content: space-between; }
.assist-heading { padding-bottom: 10px; color: #1d2129; font-size: 14px; border-bottom: 1px solid #f0f2f5; }
.close-btn { display: none; }
section { padding: 15px 2px; border-bottom: 1px solid #f0f2f5; }
section:last-child { border-bottom: 0; }
.section-title { margin-bottom: 11px; color: #4e5969; font-size: 12px; }
.section-title b { color: #1668c4; font-size: 16px; }
.section-title .danger { color: #f56c6c; }
.hint { margin: 8px 0 0; color: #a0a5ad; font-size: 11px; }
.missing-list { display: flex; flex-direction: column; gap: 4px; }
.missing-list button { display: flex; gap: 7px; padding: 7px 8px; color: #606266; text-align: left; background: #fff7f7; border: 0; border-radius: 5px; cursor: pointer; font-size: 11px; }
.missing-list button span:first-child { color: #f56c6c; font-weight: 800; }
.missing-list button:hover { color: #1668c4; background: #eef6ff; }
.summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 7px; }
.summary-grid div { display: flex; flex-direction: column; padding: 9px; background: #f7f8fa; border-radius: 6px; }
.summary-grid b { color: #1d2129; font-size: 16px; }
.summary-grid span { margin-top: 2px; color: #86909c; font-size: 10px; }
.version-info { margin: 0 0 7px; }
.version-info div { display: flex; justify-content: space-between; gap: 10px; padding: 4px 0; font-size: 11px; }
.version-info dt { color: #a0a5ad; }
.version-info dd { margin: 0; color: #4e5969; text-align: right; }
@media (max-width: 1280px) { .close-btn { display: inline-flex; } }
</style>
