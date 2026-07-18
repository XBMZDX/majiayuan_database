<script setup>
import { computed, reactive, ref } from 'vue'
import { Delete, Edit, Link, Plus } from '@element-plus/icons-vue'
import ComparisonViewer from './ComparisonViewer.vue'

const props = defineProps({
    group: { type: Object, required: true },
    step: { type: Object, default: null },
    readOnly: Boolean
})
const emit = defineEmits(['dirty', 'navigate', 'reorder'])

const statusMap = {
    draft: ['草稿', 'info'], pending: ['待评价', 'warning'], completed: ['评价完成', 'success'],
    reviewed: ['已审核', 'primary'], archived: ['已归档', 'primary']
}
const typeMap = {
    before_after: '修复前后两阶段', multi_stage: '多阶段过程', detail: '局部细节',
    microscope: '显微形貌', measurement: '量化指标', restoration: '复原前后'
}
const effectOptions = [
    ['excellent', '效果显著'], ['good', '效果良好'], ['partial', '部分改善'],
    ['limited', '效果有限'], ['unsatisfactory', '未达到预期'], ['monitoring_required', '需要持续监测']
]
const afterStatusOptions = [
    ['controlled', '已控制'], ['improved', '已改善'], ['stable', '已稳定'],
    ['unchanged', '无明显变化'], ['worsened', '出现恶化'],
    ['unresolved', '未解决'], ['monitoring_required', '需要持续监测']
]
const metricDialog = ref(false)
const metricEditingId = ref(null)
const metricForm = reactive({
    metricName: '', metricCategory: 'structure', beforeValue: null, afterValue: null,
    valueUnit: '', expectedDirection: 'decrease', description: ''
})
const beforeImage = computed(() => props.group.images.find(item => item.imageStage === 'before' && item.isPrimary)
    || props.group.images.find(item => item.imageStage === 'before'))
const afterImage = computed(() => props.group.images.find(item => item.imageStage === 'after' && item.isPrimary)
    || props.group.images.find(item => item.imageStage === 'after'))
const imageQuality = computed(() => {
    if (!beforeImage.value || !afterImage.value) return ['资料不完整', 'danger']
    if (beforeImage.value.targetPart !== afterImage.value.targetPart) return ['不适合精确对比', 'danger']
    if (beforeImage.value.shootingPosition !== afterImage.value.shootingPosition) return ['一般', 'warning']
    return ['优秀', 'success']
})
const overallScore = computed(() => {
    const evaluation = props.group.evaluation
    const scores = [
        evaluation.diseaseControlScore, evaluation.structuralStabilityScore, evaluation.appearanceScore,
        evaluation.materialCompatibilityScore, evaluation.informationPreservationScore,
        evaluation.distinguishabilityScore
    ].map(Number).filter(Boolean)
    return scores.length ? +(scores.reduce((sum, item) => sum + item, 0) / scores.length).toFixed(2) : 0
})
const changeValue = metric => {
    if (metric.beforeValue === null || metric.afterValue === null || metric.beforeValue === '' || metric.afterValue === '') return null
    return +(Number(metric.afterValue) - Number(metric.beforeValue)).toFixed(6)
}
const changeRate = metric => {
    if (!Number(metric.beforeValue)) return null
    return +(changeValue(metric) / Number(metric.beforeValue) * 100).toFixed(2)
}
const metricResult = metric => {
    const change = changeValue(metric)
    if (change === null) return ['待判断', 'info']
    if (metric.expectedDirection === 'increase') return change > 0 ? ['达到预期', 'success'] : change === 0 ? ['保持稳定', 'info'] : ['未达预期', 'danger']
    if (metric.expectedDirection === 'decrease') return change < 0 ? ['达到预期', 'success'] : change === 0 ? ['保持稳定', 'info'] : ['未达预期', 'danger']
    if (metric.expectedDirection === 'stable') {
        const rate = Math.abs(changeRate(metric) || 0)
        return rate <= 5 ? ['保持稳定', 'success'] : ['偏离稳定范围', 'warning']
    }
    return ['待专业判断', 'warning']
}
const markDirty = () => {
    props.group.evaluation.overallScore = overallScore.value
    emit('dirty')
}
const openMetric = metric => {
    metricEditingId.value = metric?.id || null
    Object.assign(metricForm, metric || {
        metricName: '', metricCategory: 'structure', beforeValue: null, afterValue: null,
        valueUnit: '', expectedDirection: 'decrease', description: ''
    })
    metricDialog.value = true
}
const saveMetric = () => {
    if (!metricForm.metricName) return
    const data = {
        ...metricForm,
        beforeValue: metricForm.beforeValue === '' ? null : metricForm.beforeValue,
        afterValue: metricForm.afterValue === '' ? null : metricForm.afterValue
    }
    const existing = props.group.metrics.find(item => item.id === metricEditingId.value)
    if (existing) Object.assign(existing, data)
    else props.group.metrics.push({ id: Date.now(), ...data })
    metricDialog.value = false
    markDirty()
}
const removeMetric = metric => {
    props.group.metrics.splice(props.group.metrics.indexOf(metric), 1)
    markDirty()
}
const scrollTo = section => document.getElementById(`comparison-section-${section}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
defineExpose({ scrollTo })
</script>

<template>
    <article class="comparison-paper" @input="markDirty" @change="markDirty">
        <header class="paper-title">
            <div>
                <div class="code">{{ group.comparisonCode }} · {{ typeMap[group.comparisonType] }}</div>
                <el-input v-if="!readOnly" v-model="group.comparisonTitle" class="title-input" placeholder="填写对比标题" />
                <h2 v-else>{{ group.comparisonTitle }}</h2>
                <p>{{ group.targetPart || '未填写部位' }} · {{ group.shootingPosition || '未填写拍摄方位' }} · {{ step?.stepName || '未关联步骤' }}</p>
            </div>
            <el-tag :type="statusMap[group.evaluationStatus]?.[1]" size="large">{{ statusMap[group.evaluationStatus]?.[0] }}</el-tag>
        </header>

        <section>
            <div class="section-heading"><span>01</span><div><h3>对比视图</h3><p>影像引用自修复过程记录；拖动影像可检查局部细节。</p></div></div>
            <ComparisonViewer
                :images="group.images"
                :shooting-position="group.shootingPosition"
                :read-only="readOnly"
                @dirty="markDirty"
                @reorder="$emit('reorder',$event)"
            />
        </section>

        <section id="comparison-section-images">
            <div class="section-heading"><span>02</span><div><h3>影像信息</h3><p>核对前后影像来源、部位、角度和时间顺序。</p></div><el-tag :type="imageQuality[1]">对比影像质量：{{ imageQuality[0] }}</el-tag></div>
            <div class="image-info-grid">
                <div v-for="(image,label) in { '修复前影像': beforeImage, '修复后影像': afterImage }" :key="label" class="image-info">
                    <div class="info-caption"><b>{{ label }}</b><el-tag v-if="image" size="small" effect="plain">{{ image.imageRole }}</el-tag></div>
                    <template v-if="image">
                        <dl>
                            <div><dt>文件名称</dt><dd>{{ image.fileName }}</dd></div>
                            <div><dt>来源步骤</dt><dd>{{ image.stepName || step?.stepName }}</dd></div>
                            <div><dt>拍摄时间</dt><dd>{{ image.shootingTime }}</dd></div>
                            <div><dt>部位 / 方位</dt><dd>{{ image.targetPart }} / {{ image.shootingPosition }}</dd></div>
                            <div><dt>摄影人</dt><dd>{{ image.photographer }}</dd></div>
                            <div><dt>来源模块</dt><dd>{{ image.sourceModule }} · 媒体ID {{ image.sourceMediaId }}</dd></div>
                            <div class="wide"><dt>图片说明</dt><dd>{{ image.imageDescription }}</dd></div>
                        </dl>
                    </template>
                    <el-empty v-else :description="`尚未关联${label}`" :image-size="52" />
                </div>
            </div>
            <el-alert
                v-if="beforeImage && afterImage && beforeImage.shootingPosition !== afterImage.shootingPosition"
                title="前后影像拍摄方位不一致，建议重新选择同机位影像或在评价中说明限制。"
                type="warning"
                :closable="false"
                show-icon
            />
        </section>

        <section id="comparison-section-status">
            <div class="section-heading"><span>03</span><div><h3>修复前后状态</h3><p>修复前信息只读引用病害调查，修复后状态独立记录。</p></div></div>
            <div class="before-after-form">
                <div>
                    <h4>修复前状态 <small>引用原始调查，不覆盖</small></h4>
                    <div class="source-disease" v-for="disease in group.diseases" :key="disease.id">
                        <b>{{ disease.diseaseName }}</b>
                        <el-tag type="danger" size="small">{{ disease.originalSeverity }}</el-tag>
                        <span>{{ disease.developmentStatus }} · {{ disease.beforeStatus }}</span>
                    </div>
                    <el-form label-position="top">
                        <el-form-item label="修复前状态摘要">
                            <el-input v-model="group.beforeSummary" type="textarea" :rows="5" :disabled="readOnly" placeholder="记录病害范围、结构、表面、外观和风险状态" />
                        </el-form-item>
                    </el-form>
                </div>
                <div>
                    <h4>修复后状态 <small>效果评价记录</small></h4>
                    <div v-for="disease in group.diseases" :key="disease.id" class="disease-result">
                        <div>
                            <b>{{ disease.diseaseName }}</b>
                            <el-select v-model="disease.treatmentEffect" :disabled="readOnly" placeholder="处理效果">
                                <el-option v-for="item in afterStatusOptions" :key="item[0]" :label="item[1]" :value="item[0]" />
                            </el-select>
                        </div>
                        <el-input v-model="disease.afterStatus" :disabled="readOnly" placeholder="修复后状态" />
                        <el-input v-model="disease.effectDescription" type="textarea" :rows="2" :disabled="readOnly" placeholder="病害控制与结构状态变化说明" />
                        <el-checkbox v-model="disease.monitoringRequired" :disabled="readOnly">需要持续监测</el-checkbox>
                    </div>
                    <el-form label-position="top">
                        <el-form-item label="修复后综合状态摘要">
                            <el-input v-model="group.afterSummary" type="textarea" :rows="4" :disabled="readOnly" placeholder="记录范围、稳定性、表面、外观、新异常和目标完成情况" />
                        </el-form-item>
                    </el-form>
                </div>
            </div>
            <el-form label-position="top">
                <el-form-item label="前后对比说明">
                    <el-input v-model="group.comparisonDescription" type="textarea" :rows="3" :disabled="readOnly" placeholder="说明本组证据的对比目的、方法和局限" />
                </el-form-item>
            </el-form>
        </section>

        <section id="comparison-section-metrics">
            <div class="section-heading">
                <span>04</span>
                <div><h3>量化指标</h3><p>差值和变化率自动计算，改善方向由指标预期方向判断。</p></div>
                <div class="heading-actions">
                    <el-checkbox v-model="group.noApplicableMetrics" :disabled="readOnly">本组无适用量化指标</el-checkbox>
                    <template v-if="!readOnly && !group.noApplicableMetrics">
                        <el-button :icon="Link" @click="$emit('navigate','detection')">从检测分析引用</el-button>
                        <el-button type="primary" :icon="Plus" @click="openMetric()">新增指标</el-button>
                    </template>
                </div>
            </div>
            <el-table v-if="!group.noApplicableMetrics" :data="group.metrics" border>
                <el-table-column prop="metricName" label="指标" min-width="150" />
                <el-table-column prop="beforeValue" label="修复前" width="90" align="right" />
                <el-table-column prop="afterValue" label="修复后" width="90" align="right" />
                <el-table-column prop="valueUnit" label="单位" width="70" />
                <el-table-column label="变化" width="100" align="right">
                    <template #default="{row}">{{ changeValue(row) ?? '-' }}</template>
                </el-table-column>
                <el-table-column label="变化率" width="100" align="right">
                    <template #default="{row}">{{ changeRate(row) === null ? '-' : `${changeRate(row)}%` }}</template>
                </el-table-column>
                <el-table-column label="结果" width="115">
                    <template #default="{row}"><el-tag :type="metricResult(row)[1]" size="small">{{ metricResult(row)[0] }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="description" label="说明" min-width="150" />
                <el-table-column v-if="!readOnly" label="操作" width="88" fixed="right">
                    <template #default="{row}">
                        <el-button link :icon="Edit" @click="openMetric(row)" />
                        <el-button link type="danger" :icon="Delete" @click="removeMetric(row)" />
                    </template>
                </el-table-column>
            </el-table>
            <el-empty v-if="!group.noApplicableMetrics && !group.metrics.length" description="暂无量化指标，可新增或从检测分析引用" :image-size="58" />
            <el-alert v-if="group.noApplicableMetrics" title="已确认本组无适用量化指标，完整度将按其他评价内容重新分配。" type="info" :closable="false" />
        </section>

        <section id="comparison-section-evaluation">
            <div class="section-heading"><span>05</span><div><h3>效果评价</h3><p>分数仅作辅助，专业文字结论仍是完成评价的必要内容。</p></div><div class="score-badge"><b>{{ overallScore || '-' }}</b><span>综合得分 / 5</span></div></div>
            <div class="score-grid">
                <label>病害控制<el-rate v-model="group.evaluation.diseaseControlScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
                <label>结构稳定<el-rate v-model="group.evaluation.structuralStabilityScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
                <label>外观协调<el-rate v-model="group.evaluation.appearanceScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
                <label>材料兼容<el-rate v-model="group.evaluation.materialCompatibilityScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
                <label>信息保留<el-rate v-model="group.evaluation.informationPreservationScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
                <label>修复可辨识性<el-rate v-model="group.evaluation.distinguishabilityScore" allow-half show-score :disabled="readOnly" @change="markDirty" /></label>
            </div>
            <el-form label-position="top">
                <div class="evaluation-grid">
                    <el-form-item label="病害控制评价"><el-input v-model="group.evaluation.diseaseControlComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="结构稳定性评价"><el-input v-model="group.evaluation.structuralComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="外观效果评价"><el-input v-model="group.evaluation.appearanceComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="材料兼容性评价"><el-input v-model="group.evaluation.compatibilityComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="原始信息保留评价"><el-input v-model="group.evaluation.informationPreservationComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="副作用说明"><el-input v-model="group.evaluation.sideEffectComment" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="整体效果">
                        <el-select v-model="group.overallEffect" :disabled="readOnly" placeholder="由评价人员确认">
                            <el-option v-for="item in effectOptions" :key="item[0]" :label="item[1]" :value="item[0]" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="评价人"><el-input v-model="group.evaluation.evaluator" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="评价日期"><el-date-picker v-model="group.evaluation.evaluationDate" value-format="YYYY-MM-DD" :disabled="readOnly" /></el-form-item>
                    <el-form-item class="span-all" label="综合结论"><el-input v-model="group.evaluation.finalConclusion" type="textarea" :rows="4" :disabled="readOnly" placeholder="形成专业评价结论，说明是否达到预期目标" /></el-form-item>
                </div>
            </el-form>
        </section>

        <section id="comparison-section-monitoring">
            <div class="section-heading"><span>06</span><div><h3>遗留问题与监测建议</h3><p>所选修复后影像和指标将作为后续监测的基准数据。</p></div></div>
            <el-form label-position="top">
                <div class="monitor-grid">
                    <el-form-item label="尚未完全解决的病害"><el-input v-model="group.monitoring.unresolvedDiseases" type="textarea" :rows="2" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="材料长期稳定性问题"><el-input v-model="group.monitoring.stabilityConcern" type="textarea" :rows="2" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="需要复查的部位"><el-input v-model="group.monitoring.reviewPart" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="建议监测指标"><el-input v-model="group.monitoring.indicators" :disabled="readOnly" /></el-form-item>
                    <el-form-item label="建议监测周期"><el-input v-model="group.monitoring.cycle" :disabled="readOnly" placeholder="如：30天" /></el-form-item>
                    <el-form-item label="预警条件"><el-input v-model="group.monitoring.warningConditions" :disabled="readOnly" /></el-form-item>
                    <el-form-item class="span-all" label="其他注意事项"><el-input v-model="group.monitoring.notes" type="textarea" :rows="3" :disabled="readOnly" /></el-form-item>
                </div>
                <div class="usage-checks">
                    <div class="monitor-decision">
                        <span>监测基准决策</span>
                        <el-radio-group v-model="group.monitoring.monitoringDecision" :disabled="readOnly">
                            <el-radio value="baseline" @change="group.selectedAsMonitoringBaseline = true">设置为监测基准</el-radio>
                            <el-radio value="not_required" @change="group.selectedAsMonitoringBaseline = false">暂不作为监测基准</el-radio>
                        </el-radio-group>
                    </div>
                    <el-checkbox v-model="group.selectedForArchive" :disabled="readOnly">收入保护修复档案</el-checkbox>
                    <el-checkbox v-model="group.selectedForRestoration" :disabled="readOnly">用于文物复原成果</el-checkbox>
                    <el-checkbox
                        v-model="group.selectedAsMonitoringBaseline"
                        :disabled="readOnly"
                        @change="group.monitoring.monitoringDecision = $event ? 'baseline' : 'not_required'"
                    >将修复后影像设置为监测基准</el-checkbox>
                    <el-checkbox v-model="group.monitoring.requiresRework" :disabled="readOnly">需要再次修复</el-checkbox>
                </div>
            </el-form>
        </section>
    </article>

    <el-dialog v-model="metricDialog" :title="metricEditingId ? '编辑量化指标' : '新增量化指标'" width="min(620px, 94vw)" destroy-on-close>
        <el-form label-position="top">
            <div class="dialog-grid">
                <el-form-item label="指标名称" class="span-2"><el-input v-model="metricForm.metricName" placeholder="如：裂隙最大宽度" /></el-form-item>
                <el-form-item label="指标类别">
                    <el-select v-model="metricForm.metricCategory">
                        <el-option label="结构" value="structure" /><el-option label="强度" value="strength" />
                        <el-option label="外观" value="appearance" /><el-option label="材料" value="material" />
                    </el-select>
                </el-form-item>
                <el-form-item label="单位"><el-input v-model="metricForm.valueUnit" placeholder="mm、%或HA" /></el-form-item>
                <el-form-item label="修复前值"><el-input-number v-model="metricForm.beforeValue" :precision="4" controls-position="right" /></el-form-item>
                <el-form-item label="修复后值"><el-input-number v-model="metricForm.afterValue" :precision="4" controls-position="right" /></el-form-item>
                <el-form-item label="预期变化方向" class="span-2">
                    <el-select v-model="metricForm.expectedDirection">
                        <el-option label="应增加" value="increase" /><el-option label="应降低" value="decrease" />
                        <el-option label="应保持稳定" value="stable" /><el-option label="应达到目标范围" value="target_range" />
                    </el-select>
                </el-form-item>
                <el-form-item label="说明" class="span-2"><el-input v-model="metricForm.description" type="textarea" :rows="3" /></el-form-item>
            </div>
        </el-form>
        <template #footer><el-button @click="metricDialog = false">取消</el-button><el-button type="primary" :disabled="!metricForm.metricName" @click="saveMetric">保存指标</el-button></template>
    </el-dialog>
</template>

<style scoped>
.comparison-paper { max-width: 1160px; min-width: 0; margin: 18px auto 36px; padding: 30px 36px 50px; background: #fff; border: 1px solid #e3e6e5; box-shadow: 0 5px 22px rgb(39 52 48 / 7%); color: #3a4843; }
.paper-title { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; padding-bottom: 22px; border-bottom: 2px solid #314c43; }
.paper-title > div { flex: 1; min-width: 0; }
.code { color: #a07850; font-size: 10px; letter-spacing: .08em; }
.paper-title h2 { margin: 7px 0; color: #253a33; font-size: 25px; font-family: "Noto Serif SC", "SimSun", serif; }
.paper-title p { margin: 0; color: #84908c; font-size: 12px; }
.title-input { max-width: 700px; margin: 8px 0; }
.title-input :deep(.el-input__wrapper) { padding: 0; box-shadow: none; border-bottom: 1px solid #d6ddda; border-radius: 0; }
.title-input :deep(.el-input__inner) { height: 38px; color: #253a33; font-size: 23px; font-weight: 700; font-family: "Noto Serif SC", "SimSun", serif; }
section { padding: 28px 0; border-bottom: 1px solid #e6e9e8; scroll-margin-top: 15px; }
.section-heading { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; }
.section-heading > span { width: 34px; height: 34px; border: 1px solid #b58a58; border-radius: 50%; display: grid; place-items: center; color: #9c7040; font-size: 11px; }
.section-heading > div:nth-child(2) { flex: 1; min-width: 0; }
.section-heading h3 { margin: 0 0 3px; color: #2a4038; font-size: 17px; }
.section-heading p { margin: 0; color: #98a19e; font-size: 11px; }
.image-info-grid, .before-after-form { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.image-info { padding: 16px; border: 1px solid #e1e6e3; background: #fafbfa; }
.info-caption { display: flex; justify-content: space-between; margin-bottom: 12px; color: #34473f; }
.image-info dl { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px; margin: 0; }
.image-info dl div { min-width: 0; }
.image-info dl .wide { grid-column: 1/-1; }
dt { color: #9aa29f; font-size: 10px; } dd { margin: 3px 0 0; color: #56615d; font-size: 11px; word-break: break-word; }
.image-info-grid + :deep(.el-alert) { margin-top: 12px; }
.before-after-form > div { padding: 18px; border-top: 3px solid #526d63; background: #f8faf9; }
.before-after-form > div:last-child { border-color: #a37849; }
h4 { margin: 0 0 14px; color: #354941; } h4 small { margin-left: 7px; color: #9aa39f; font-weight: 400; }
.source-disease { display: flex; align-items: center; gap: 7px; margin-bottom: 8px; padding: 9px 10px; background: #fff; border-left: 2px solid #a36d59; font-size: 11px; }
.source-disease span { color: #7d8884; }
.disease-result { margin-bottom: 13px; padding: 10px; background: #fff; border: 1px solid #e4e8e6; }
.disease-result > div { display: grid; grid-template-columns: 1fr 150px; align-items: center; gap: 10px; margin-bottom: 8px; }
.disease-result :deep(.el-textarea) { margin-top: 8px; }
.heading-actions { display: flex; align-items: center; justify-content: flex-end; gap: 8px; flex-wrap: wrap; }
.score-badge { text-align: right; }
.score-badge b, .score-badge span { display: block; }.score-badge b { color: #2f6755; font-size: 25px; }.score-badge span { color: #9aa29f; font-size: 9px; }
.score-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px; }
.score-grid label { display: flex; flex-direction: column; gap: 8px; padding: 12px; background: #f8faf9; color: #5a6762; font-size: 11px; }
.evaluation-grid, .monitor-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0 16px; }
.evaluation-grid .span-all, .monitor-grid .span-all { grid-column: 1/-1; }
.usage-checks { display: flex; flex-wrap: wrap; gap: 12px 24px; padding: 16px; border: 1px solid #dfe6e2; background: #f6f9f7; }
.monitor-decision { width: 100%; display: flex; align-items: center; gap: 18px; padding-bottom: 11px; border-bottom: 1px solid #e1e7e4; color: #596963; font-size: 11px; }
.dialog-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
.dialog-grid .span-2 { grid-column: 1/-1; }
@media (max-width: 1100px) {
    .comparison-paper { padding: 24px; margin: 12px; }
    .score-grid { grid-template-columns: 1fr 1fr; }
    .evaluation-grid, .monitor-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 720px) {
    .comparison-paper { margin: 0; padding: 20px 14px; border: 0; }
    .paper-title, .section-heading { align-items: flex-start; }
    .image-info-grid, .before-after-form, .score-grid, .evaluation-grid, .monitor-grid { grid-template-columns: 1fr; }
    .evaluation-grid .span-all, .monitor-grid .span-all { grid-column: auto; }
    .section-heading { flex-wrap: wrap; }
    .heading-actions { width: 100%; justify-content: flex-start; }
}
</style>
