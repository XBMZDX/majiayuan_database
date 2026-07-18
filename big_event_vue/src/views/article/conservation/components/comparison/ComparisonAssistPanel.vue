<script setup>
import { computed } from 'vue'
import { ArrowRight, EditPen, Warning } from '@element-plus/icons-vue'

const props = defineProps({
    group: { type: Object, required: true },
    step: { type: Object, default: null },
    completeness: { type: Number, default: 0 },
    missingItems: { type: Array, default: () => [] },
    readOnly: Boolean
})
const emit = defineEmits(['dirty', 'locate', 'navigate', 'reopen'])
const score = computed(() => props.group.evaluation?.overallScore || 0)
const severityMap = { minor: '轻微', moderate: '中等', severe: '严重', critical: '危急' }
</script>

<template>
    <aside class="assist-panel">
        <section>
            <div class="panel-title"><h3>关联病害</h3><el-button link type="primary" :icon="ArrowRight" @click="$emit('navigate','disease')">病害调查</el-button></div>
            <div v-for="item in group.diseases" :key="item.id" class="relation-item">
                <div><b>{{ item.diseaseName }}</b><el-tag :type="['severe','critical'].includes(item.originalSeverity) ? 'danger' : 'warning'" size="small">{{ severityMap[item.originalSeverity] || item.originalSeverity }}</el-tag></div>
                <span>原状态：{{ item.developmentStatus }} · {{ item.beforeStatus }}</span>
                <span>修复后：{{ item.afterStatus || '待评价' }}</span>
            </div>
            <el-empty v-if="!group.diseases.length" description="尚未关联病害" :image-size="44" />
        </section>

        <section>
            <div class="panel-title"><h3>关联修复步骤</h3><el-button link type="primary" :icon="ArrowRight" @click="$emit('navigate','process-step')">查看步骤</el-button></div>
            <template v-if="step">
                <div class="step-name">{{ step.stepName }}</div>
                <dl>
                    <div><dt>操作人员</dt><dd>{{ step.operatorName || '-' }}</dd></div>
                    <div><dt>完成时间</dt><dd>{{ step.actualEndTime || '尚未完成' }}</dd></div>
                    <div><dt>质量检查</dt><dd>{{ step.qualityChecks?.[0]?.checkResult || '无记录' }}</dd></div>
                    <div><dt>使用材料</dt><dd>{{ step.materials?.map(item => item.materialName).join('、') || '无' }}</dd></div>
                    <div><dt>异常数量</dt><dd>{{ step.issues?.length || 0 }}项</dd></div>
                </dl>
            </template>
        </section>

        <section class="completion">
            <div class="panel-title"><h3>对比完整度</h3><b>{{ completeness }}%</b></div>
            <el-progress :percentage="completeness" :stroke-width="8" :color="completeness >= 85 ? '#4f826f' : '#b48650'" />
            <small>完成评价需不存在关键缺失项</small>
        </section>

        <section>
            <div class="panel-title"><h3>缺失项</h3><el-tag :type="missingItems.length ? 'warning' : 'success'" size="small">{{ missingItems.length }}项</el-tag></div>
            <button v-for="item in missingItems" :key="item.key" class="missing-item" @click="$emit('locate',item.section)">
                <el-icon><Warning /></el-icon><span>{{ item.label }}</span><i>定位</i>
            </button>
            <div v-if="!missingItems.length" class="all-complete">关键内容已齐全，可以完成评价。</div>
        </section>

        <section>
            <div class="panel-title"><h3>专业评价</h3><b class="score">{{ score || '-' }}</b></div>
            <div class="effect">{{ group.evaluation.finalConclusion || '综合结论待填写' }}</div>
            <el-button v-if="group.evaluationStatus === 'completed'" type="primary" plain :icon="EditPen" @click="$emit('reopen')">重新编辑评价</el-button>
        </section>

        <section>
            <div class="panel-title"><h3>使用状态</h3></div>
            <div class="switch-list">
                <label><span>收入保护修复档案</span><el-switch v-model="group.selectedForArchive" :disabled="readOnly" @change="$emit('dirty')" /></label>
                <label><span>用于文物复原成果</span><el-switch v-model="group.selectedForRestoration" :disabled="readOnly" @change="$emit('dirty')" /></label>
                <label><span>作为后续监测基准</span><el-switch v-model="group.selectedAsMonitoringBaseline" :disabled="readOnly" @change="$emit('dirty')" /></label>
            </div>
        </section>
    </aside>
</template>

<style scoped>
.assist-panel { height: 100%; overflow-y: auto; background: #fbfcfb; border-left: 1px solid #e1e5e3; padding: 0 15px 30px; }
section { padding: 18px 0; border-bottom: 1px solid #e4e8e6; }
.panel-title { display: flex; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 11px; }
h3 { margin: 0; color: #354840; font-size: 13px; }
.panel-title > b { color: #3a725f; font-size: 15px; }
.relation-item { margin-bottom: 8px; padding: 9px; border-left: 2px solid #a67454; background: #fff; }
.relation-item > div { display: flex; align-items: center; gap: 7px; margin-bottom: 5px; }
.relation-item span { display: block; color: #7d8783; font-size: 10px; line-height: 1.6; }
.step-name { margin-bottom: 9px; color: #3d5149; font-weight: 700; font-size: 12px; }
dl { margin: 0; }
dl div { display: grid; grid-template-columns: 60px 1fr; gap: 6px; padding: 5px 0; font-size: 10px; }
dt { color: #9ba39f; } dd { margin: 0; color: #68736f; word-break: break-word; }
.completion small { display: block; margin-top: 7px; color: #9aa29f; }
.missing-item { width: 100%; border: 0; margin: 5px 0; padding: 8px; display: grid; grid-template-columns: 16px 1fr auto; align-items: center; gap: 6px; text-align: left; background: #fff8ee; color: #775d3f; cursor: pointer; }
.missing-item i { color: #b0814e; font-size: 9px; font-style: normal; }
.all-complete { padding: 10px; color: #477260; background: #f0f8f4; font-size: 10px; }
.score { font-size: 23px !important; }
.effect { margin-bottom: 10px; color: #707b77; font-size: 10px; line-height: 1.6; }
.switch-list label { display: flex; justify-content: space-between; align-items: center; gap: 8px; padding: 7px 0; color: #65706c; font-size: 10px; }
</style>
