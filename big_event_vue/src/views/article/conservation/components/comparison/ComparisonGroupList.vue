<script setup>
import { computed, ref } from 'vue'
import { Delete, Plus, Search } from '@element-plus/icons-vue'

const props = defineProps({
    groups: { type: Array, default: () => [] },
    activeId: { type: [Number, String], default: null },
    diseases: { type: Array, default: () => [] },
    steps: { type: Array, default: () => [] }
})
defineEmits(['select', 'create', 'delete'])

const keyword = ref('')
const diseaseId = ref('')
const stepId = ref('')
const status = ref('')
const targetPart = ref('')

const typeMap = {
    before_after: '前后对比', multi_stage: '阶段序列', detail: '局部细节',
    microscope: '显微对比', measurement: '指标对比', restoration: '复原对比'
}
const statusMap = {
    draft: ['草稿', 'info'], pending: ['待评价', 'warning'], completed: ['评价完成', 'success'],
    returned: ['审核退回', 'danger'], reviewed: ['已审核', 'primary'], archived: ['已归档', 'primary']
}
const effectMap = {
    excellent: '效果显著', good: '效果良好', partial: '部分改善',
    limited: '效果有限', unsatisfactory: '未达预期', monitoring_required: '持续监测'
}
const parts = computed(() => [...new Set(props.groups.map(item => item.targetPart).filter(Boolean))])
const filtered = computed(() => props.groups.filter(item => {
    const hitKeyword = !keyword.value || `${item.comparisonTitle}${item.comparisonCode}${item.targetPart}`.includes(keyword.value)
    const hitDisease = !diseaseId.value || item.diseases.some(disease => disease.diseaseRecordId === diseaseId.value)
    return hitKeyword
        && hitDisease
        && (!stepId.value || item.stepId === stepId.value)
        && (!status.value || item.evaluationStatus === status.value)
        && (!targetPart.value || item.targetPart === targetPart.value)
}))
const primaryImage = (group, stage) => group.images.find(item => item.imageStage === stage && item.isPrimary)
    || group.images.find(item => item.imageStage === stage)
</script>

<template>
    <aside class="group-list">
        <div class="list-head">
            <div><b>对比组</b><span>{{ filtered.length }} / {{ groups.length }}</span></div>
            <el-button type="primary" link :icon="Plus" @click="$emit('create')">新建</el-button>
        </div>
        <div class="filters">
            <el-input v-model="keyword" clearable :prefix-icon="Search" placeholder="搜索标题、编号或部位" />
            <div class="filter-grid">
                <el-select v-model="diseaseId" clearable placeholder="病害">
                    <el-option v-for="item in diseases" :key="item.id" :label="item.diseaseName" :value="item.id" />
                </el-select>
                <el-select v-model="stepId" clearable placeholder="步骤">
                    <el-option v-for="item in steps" :key="item.id" :label="item.stepName" :value="item.id" />
                </el-select>
                <el-select v-model="status" clearable placeholder="状态">
                    <el-option v-for="(item,key) in statusMap" :key="key" :label="item[0]" :value="key" />
                </el-select>
                <el-select v-model="targetPart" clearable placeholder="部位">
                    <el-option v-for="item in parts" :key="item" :label="item" :value="item" />
                </el-select>
            </div>
        </div>
        <div class="cards">
            <article
                v-for="item in filtered"
                :key="item.id"
                class="group-card"
                :class="{ active: item.id === activeId }"
                @click="$emit('select', item.id)"
            >
                <div class="card-title">
                    <h3>{{ item.comparisonTitle || '未命名对比组' }}</h3>
                    <el-tag :type="statusMap[item.evaluationStatus]?.[1]" size="small">{{ statusMap[item.evaluationStatus]?.[0] }}</el-tag>
                </div>
                <p>{{ item.comparisonCode }} · {{ typeMap[item.comparisonType] }}</p>
                <dl>
                    <div><dt>部位</dt><dd>{{ item.targetPart || '-' }}</dd></div>
                    <div><dt>病害</dt><dd>{{ item.diseases.map(d => `${d.diseaseName}·${d.originalSeverity}`).join('、') || '整体效果' }}</dd></div>
                    <div><dt>步骤</dt><dd>{{ steps.find(step => step.id === item.stepId)?.stepName || '-' }}</dd></div>
                </dl>
                <div class="thumb-pair">
                    <div><img v-if="primaryImage(item,'before')" :src="primaryImage(item,'before').thumbnailUrl" alt="修复前"><span v-else>缺少前图</span><small>修复前</small></div>
                    <i>→</i>
                    <div><img v-if="primaryImage(item,'after')" :src="primaryImage(item,'after').thumbnailUrl" alt="修复后"><span v-else>缺少后图</span><small>修复后</small></div>
                </div>
                <div class="card-foot">
                    <span>{{ effectMap[item.overallEffect] || '效果待评价' }}</span>
                    <div><el-tag v-if="item.selectedForArchive" size="small" type="success">档案</el-tag><el-tag v-if="item.selectedAsMonitoringBaseline" size="small" type="warning">基准</el-tag></div>
                    <el-button
                        v-if="item.evaluationStatus !== 'archived'"
                        link
                        type="danger"
                        :icon="Delete"
                        @click.stop="$emit('delete', item)"
                    />
                </div>
            </article>
            <el-empty v-if="!filtered.length" description="暂无符合条件的对比组" :image-size="64" />
        </div>
    </aside>
</template>

<style scoped>
.group-list { height: 100%; background: #f7f8f7; border-right: 1px solid #e1e5e3; display: flex; flex-direction: column; overflow: hidden; }
.list-head { padding: 16px 16px 10px; display: flex; justify-content: space-between; align-items: center; }
.list-head b { color: #30433c; font-size: 16px; }
.list-head span { margin-left: 8px; color: #929b98; font-size: 11px; }
.filters { padding: 0 12px 12px; border-bottom: 1px solid #e2e6e4; }
.filter-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 7px; margin-top: 8px; }
.cards { flex: 1; overflow-y: auto; padding: 10px; }
.group-card { margin-bottom: 10px; padding: 13px; background: #fff; border: 1px solid #e0e5e2; border-radius: 8px; cursor: pointer; transition: .2s; }
.group-card:hover { border-color: #92b4a9; transform: translateY(-1px); }
.group-card.active { border-color: #3f7765; box-shadow: inset 3px 0 #3f7765, 0 5px 16px rgb(47 97 82 / 10%); }
.card-title { display: flex; gap: 8px; align-items: flex-start; justify-content: space-between; }
h3 { margin: 0; color: #31433d; font-size: 14px; line-height: 1.45; }
.group-card > p { margin: 5px 0 9px; color: #9a8071; font-size: 10px; }
dl { margin: 0; font-size: 11px; }
dl div { display: grid; grid-template-columns: 38px 1fr; gap: 5px; margin: 4px 0; }
dt { color: #9aa29f; } dd { margin: 0; color: #5f6966; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.thumb-pair { display: grid; grid-template-columns: 1fr 16px 1fr; gap: 6px; align-items: center; margin: 10px 0; }
.thumb-pair > div { height: 68px; background: #ecefed; border-radius: 5px; overflow: hidden; position: relative; display: grid; place-items: center; }
.thumb-pair img { width: 100%; height: 100%; object-fit: cover; }
.thumb-pair span { color: #a2aaa7; font-size: 10px; }
.thumb-pair small { position: absolute; left: 5px; bottom: 4px; color: #fff; background: rgb(21 31 28 / 70%); padding: 1px 5px; border-radius: 3px; font-size: 9px; }
.thumb-pair i { color: #9ca8a4; font-style: normal; text-align: center; }
.card-foot { min-height: 24px; display: flex; justify-content: space-between; align-items: center; color: #66736f; font-size: 11px; }
.card-foot > div { display: flex; gap: 4px; margin-left: auto; }
@media (max-width: 920px) { .group-list { width: 310px; } }
</style>
