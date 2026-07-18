<script setup>
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { init, use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, MarkLineComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ElMessage } from 'element-plus'
import { Camera, CircleCheck, Clock, Edit, Plus, Warning } from '@element-plus/icons-vue'

use([LineChart, GridComponent, MarkLineComponent, TitleComponent, TooltipComponent, CanvasRenderer])
const props = defineProps({ plan: Object, target: Object, task: Object, record: Object, readOnly: Boolean, activeTab: { type: String, default: 'overview' } })
const emit = defineEmits(['update:activeTab', 'dirty', 'add-target', 'generate-task', 'start-task', 'select-task', 'create-record', 'save-record', 'submit-record', 'complete-task', 'handle-alert', 'navigate'])
const tab = computed({ get: () => props.activeTab, set: value => emit('update:activeTab', value) })
const indicatorDialog = ref(false), indicatorEditing = ref(null), chartEl = ref(null)
let chart
const indicatorForm = reactive({ indicatorCode: '', indicatorName: '', indicatorCategory: 'structure', dataType: 'number', valueUnit: 'mm', baselineValue: 0, normalMin: 0, normalMax: 1, warningMin: null, warningMax: 1.5, criticalMin: null, criticalMax: 2, changeWarningValue: .2, expectedDirection: 'stable', observationMethod: '', instrumentName: '', required: true })
const typeMap = { disease: '病害记录', repair_part: '修复部位', restoration_part: '实体复原部分', environment: '保存环境', artifact: '文物整体' }
const taskMap = { pending: ['待开始','info'], in_progress: ['执行中','warning'], completed: ['已完成','success'], overdue: ['已逾期','danger'], cancelled: ['已取消','info'] }
const statusMap = { stable: '稳定', slight_change: '轻微变化', obvious_change: '明显变化', abnormal: '异常', critical: '严重异常' }
const resultMap = { normal: ['正常','success'], attention: ['关注','warning'], warning: ['黄色预警','warning'], critical: ['红色预警','danger'], pending: ['待填写','info'] }
const targetRecords = computed(() => props.plan?.records.filter(x => x.targetId === props.target?.id).sort((a,b) => String(a.monitoringDate).localeCompare(String(b.monitoringDate))) || [])
const latestSubmitted = computed(() => [...targetRecords.value].reverse().find(x => x.monitoringStatus === 'submitted'))
const trendIndicator = computed(() => props.target?.indicators[0])
const trendPoints = computed(() => {
    if (!props.target || !trendIndicator.value) return []
    const points = [{ date: props.target.baseline?.baselineDate || props.plan.startDate, value: Number(trendIndicator.value.baselineValue), label: '修复后基准', status: 'normal' }]
    targetRecords.value.forEach(record => {
        const value = record.values?.find(x => x.indicatorId === trendIndicator.value.id)
        if (value?.valueNumber != null) points.push({ date: String(record.monitoringDate).slice(0, 10), value: Number(value.valueNumber), label: record.recordCode, status: value.resultLevel })
    })
    return points
})
const renderChart = () => {
    if (tab.value !== 'trend' || !chartEl.value || !trendIndicator.value) return
    chart ||= init(chartEl.value)
    const i = trendIndicator.value
    chart.setOption({
        grid: { left: 45, right: 20, top: 42, bottom: 35 }, tooltip: { trigger: 'axis' },
        title: { text: `${i.indicatorName}变化趋势`, textStyle: { fontSize: 13, color: '#334b44' } },
        xAxis: { type: 'category', data: trendPoints.value.map(x => x.date), axisLabel: { fontSize: 10 } },
        yAxis: { type: 'value', name: i.valueUnit, axisLabel: { fontSize: 10 } },
        series: [
            { type: 'line', smooth: true, symbolSize: 8, data: trendPoints.value.map(x => ({ value: x.value, itemStyle: { color: x.status === 'critical' ? '#b84c43' : x.status === 'warning' ? '#c88b3a' : '#4c8874' } })), lineStyle: { color: '#4c8874', width: 3 }, areaStyle: { color: 'rgba(76,136,116,.1)' }, markLine: { silent: true, data: [i.warningMax != null && { yAxis: i.warningMax, name: '黄色阈值', lineStyle: { color: '#c88b3a' } }, i.criticalMax != null && { yAxis: i.criticalMax, name: '红色阈值', lineStyle: { color: '#b84c43' } }].filter(Boolean) } }
        ]
    }, true)
    chart.resize()
}
watch([tab, () => props.target?.id, () => props.plan?.records], () => nextTick(renderChart), { deep: true })
const resize = () => chart?.resize()
window.addEventListener('resize', resize)
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })

const openIndicator = item => {
    indicatorEditing.value = item?.id || null
    Object.assign(indicatorForm, item || { indicatorCode: '', indicatorName: '', indicatorCategory: 'structure', dataType: 'number', valueUnit: 'mm', baselineValue: 0, normalMin: 0, normalMax: 1, warningMin: null, warningMax: 1.5, criticalMin: null, criticalMax: 2, changeWarningValue: .2, expectedDirection: 'stable', observationMethod: '', instrumentName: '', required: true })
    indicatorDialog.value = true
}
const saveIndicator = () => {
    if (!indicatorForm.indicatorName) return ElMessage.warning('请填写指标名称')
    if (indicatorEditing.value) Object.assign(props.target.indicators.find(x => x.id === indicatorEditing.value), indicatorForm)
    else props.target.indicators.push({ ...indicatorForm, id: Date.now() })
    indicatorDialog.value = false; emit('dirty')
}
const addPlaceholderImage = () => {
    if (!props.record) return
    props.record.media ||= []
    props.record.media.push({ id: Date.now(), fileUrl: props.target.baseline?.baselineFileUrl || '/mock/comparison/crack-after.svg', title: '本次同角度监测影像（上传占位）', shootingPosition: props.target.shootingPosition, role: 'current' })
    emit('dirty'); ElMessage.success('已添加同角度影像占位')
}
const previousValue = id => {
    const records = targetRecords.value.filter(x => x.id !== props.record?.id && x.monitoringStatus === 'submitted')
    return [...records].reverse().find(x => x.values?.some(v => v.indicatorId === id))?.values.find(v => v.indicatorId === id)?.valueNumber ?? props.target?.indicators.find(x => x.id === id)?.baselineValue
}
</script>

<template>
<main v-if="plan && target" class="workspace">
  <header><div><small>{{ plan.planCode }} / {{ typeMap[target.targetType] }}</small><h2>{{ target.targetName }}</h2><p>{{ target.targetLocation }}</p></div><div><el-tag :type="target.riskLevel==='high'?'danger':target.riskLevel==='medium'?'warning':'success'">{{ target.riskLevel }} risk</el-tag><el-tag type="info">{{ target.currentStatus }}</el-tag></div></header>
  <el-tabs v-model="tab" class="tabs">
    <el-tab-pane label="计划总览" name="overview">
      <div class="paper">
        <div class="section-head"><div><h3>监测计划与对象</h3><p>计划描述长期策略；监测对象、指标与基准分别维护。</p></div><el-button :icon="Plus" @click="$emit('add-target')">新增对象</el-button></div>
        <el-descriptions :column="3" border size="small"><el-descriptions-item label="计划状态">{{ plan.planStatus }}</el-descriptions-item><el-descriptions-item label="负责人">{{ plan.responsiblePerson }}</el-descriptions-item><el-descriptions-item label="执行周期">{{ plan.defaultFrequencyValue }} {{ plan.defaultFrequencyUnit }}</el-descriptions-item><el-descriptions-item label="执行期限">{{ plan.startDate }} 至 {{ plan.expectedEndDate }}</el-descriptions-item><el-descriptions-item label="监测地点">{{ plan.monitoringLocation }}</el-descriptions-item><el-descriptions-item label="下次监测">{{ plan.nextMonitoringDate }}</el-descriptions-item></el-descriptions>
        <div class="text-grid"><section><small>监测目的</small><p>{{ plan.monitoringPurpose }}</p></section><section><small>监测范围</small><p>{{ plan.monitoringScope }}</p></section><section class="wide"><small>总体策略</small><p>{{ plan.overallStrategy }}</p></section></div>
        <div class="section-head line"><div><h3>当前对象配置</h3><p>{{ target.monitoringReason }}</p></div><el-button type="primary" :icon="Plus" @click="openIndicator()">配置指标</el-button></div>
        <el-table :data="target.indicators" size="small"><el-table-column prop="indicatorName" label="监测指标" min-width="140"/><el-table-column prop="indicatorCategory" label="类别" width="90"/><el-table-column label="基准"><template #default="{row}">{{ row.baselineValue }} {{ row.valueUnit }}</template></el-table-column><el-table-column label="正常范围"><template #default="{row}">{{ row.normalMin ?? '-' }} ~ {{ row.normalMax ?? '-' }}</template></el-table-column><el-table-column label="预警/严重"><template #default="{row}">{{ row.warningMax ?? row.warningMin ?? '-' }} / {{ row.criticalMax ?? row.criticalMin ?? '-' }}</template></el-table-column><el-table-column prop="instrumentName" label="仪器/方法" min-width="120"/><el-table-column label="操作" width="70"><template #default="{row}"><el-button link :icon="Edit" @click="openIndicator(row)">编辑</el-button></template></el-table-column></el-table>
        <section class="baseline"><div><small>当前监测基准 · {{ target.baseline?.versionNo }}</small><h4>{{ target.baseline?.baselineStatus }}</h4><p>{{ target.baseline?.baselineDescription }}</p><span>来源：{{ target.baseline?.sourceBusinessType }} #{{ target.baseline?.sourceBusinessId }} · {{ target.baseline?.baselineDate }}</span></div><img v-if="target.baseline?.baselineFileUrl" :src="target.baseline.baselineFileUrl" alt="监测基准"><el-tag type="success">当前版本 · 只读</el-tag></section>
      </div>
    </el-tab-pane>
    <el-tab-pane label="监测任务" name="tasks">
      <div class="paper"><div class="section-head"><div><h3>任务调度</h3><p>计划与实际记录分离，每个周期生成独立任务。</p></div><el-button type="primary" :icon="Plus" @click="$emit('generate-task')">生成下一任务</el-button></div>
      <article v-for="x in plan.tasks" :key="x.id" class="task" :class="{selected:x.id===task?.id}" @click="$emit('select-task',x)"><div class="task-icon"><CircleCheck v-if="x.taskStatus==='completed'"/><Clock v-else/></div><div><b>{{ x.taskName }}</b><small>{{ x.taskCode }} · 计划 {{ x.plannedDate }} · 截止 {{ x.dueDate }}</small><el-progress :percentage="x.completionRate" :stroke-width="5"/></div><el-tag :type="taskMap[x.taskStatus]?.[1]">{{ taskMap[x.taskStatus]?.[0] }}</el-tag><el-button v-if="x.taskStatus==='pending'" size="small" type="primary" @click.stop="$emit('start-task',x)">开始</el-button><el-button v-if="x.taskStatus==='in_progress'" size="small" @click.stop="$emit('create-record',x)">继续执行</el-button></article>
      </div>
    </el-tab-pane>
    <el-tab-pane label="执行记录" name="execution">
      <div class="paper" v-if="record">
        <div class="execution-head"><div><small>{{ record.recordCode }} · {{ task?.taskName }}</small><h3>本次监测执行记录</h3></div><el-tag :type="record.monitoringStatus==='submitted'?'success':'warning'">{{ record.monitoringStatus==='submitted'?'已提交':'草稿' }}</el-tag></div>
        <section class="compare-values"><h4>基准、上次与本次</h4><div v-for="v in record.values" :key="v.indicatorId"><b>{{ v.indicatorName }}</b><span>基准<strong>{{ v.baselineValue }}{{ v.valueUnit }}</strong></span><span>上次<strong>{{ previousValue(v.indicatorId) }}{{ v.valueUnit }}</strong></span><span class="current">本次<strong>{{ v.valueNumber ?? '-' }}{{ v.valueUnit }}</strong></span><el-tag :type="resultMap[v.resultLevel]?.[1]">{{ resultMap[v.resultLevel]?.[0] }}</el-tag></div></section>
        <section><h4>动态指标记录</h4><div class="value-grid"><el-form-item v-for="v in record.values" :key="v.indicatorId" :label="`${v.indicatorName}（${v.valueUnit}）`"><el-input-number v-model="v.valueNumber" :precision="2" :disabled="record.monitoringStatus==='submitted'" @change="$emit('dirty')"/><small>{{ v.resultDescription || '保存时自动判断阈值' }}</small></el-form-item></div></section>
        <section><h4>状态观察</h4><div class="form-grid"><el-form-item label="整体状态"><el-select v-model="record.overallStatus" :disabled="record.monitoringStatus==='submitted'"><el-option v-for="(label,key) in statusMap" :key="key" :label="label" :value="key"/></el-select></el-form-item><el-form-item label="与基准比较"><el-select v-model="record.comparisonResult" :disabled="record.monitoringStatus==='submitted'"><el-option label="一致" value="same"/><el-option label="轻微偏离" value="minor_deviation"/><el-option label="明显偏离" value="major_deviation"/></el-select></el-form-item><el-form-item class="wide" label="观察描述"><el-input v-model="record.observationDescription" type="textarea" :rows="2" :disabled="record.monitoringStatus==='submitted'"/></el-form-item><el-form-item class="wide" label="变化说明"><el-input v-model="record.changeDescription" type="textarea" :rows="2" :disabled="record.monitoringStatus==='submitted'"/></el-form-item></div></section>
        <section><div class="section-head"><div><h4>同角度影像</h4><p>要求机位：{{ target.shootingPosition || '无固定机位' }}</p></div><el-button v-if="record.monitoringStatus!=='submitted'" :icon="Camera" @click="addPlaceholderImage">上传本次影像</el-button></div><el-alert v-if="target.requiresImage&&!record.media?.length" title="当前对象要求上传同角度影像" type="warning" :closable="false"/><div class="images"><figure><img v-if="target.baseline?.baselineFileUrl" :src="target.baseline.baselineFileUrl"><div v-else>无基准图</div><figcaption>修复后基准 · {{ target.baseline?.baselineDate }}</figcaption></figure><figure><img v-if="record.media?.[0]" :src="record.media[0].fileUrl"><div v-else>等待上传本次影像</div><figcaption>本次监测 · {{ String(record.monitoringDate).slice(0,10) }}</figcaption></figure></div></section>
        <section><h4>结论与后续安排</h4><div class="form-grid"><el-form-item class="wide" label="监测结论"><el-input v-model="record.resultConclusion" type="textarea" :rows="3" :disabled="record.monitoringStatus==='submitted'"/></el-form-item><el-form-item label="下一次监测"><el-date-picker v-model="record.nextMonitoringDate" type="date" value-format="YYYY-MM-DD" :disabled="record.monitoringStatus==='submitted'"/></el-form-item><el-form-item label="监测人"><el-input v-model="record.monitorPerson" :disabled="record.monitoringStatus==='submitted'"/></el-form-item></div><div class="checks"><el-checkbox v-model="record.requiresRecheck">需要复测</el-checkbox><el-checkbox v-model="record.requiresIntervention">需要干预</el-checkbox><el-checkbox v-model="record.requiresNewDiseaseSurvey">新建病害调查</el-checkbox><el-checkbox v-model="record.requiresNewProject">建议新保护修复项目</el-checkbox></div></section>
        <footer v-if="record.monitoringStatus!=='submitted'"><el-button @click="$emit('save-record')">保存草稿</el-button><el-button type="primary" @click="$emit('submit-record')">提交对象记录</el-button><el-button type="success" @click="$emit('complete-task')">完成监测任务</el-button></footer>
      </div>
      <el-empty v-else description="请选择执行中任务并开始记录"><el-button type="primary" @click="$emit('create-record',task)">开始当前对象监测</el-button></el-empty>
    </el-tab-pane>
    <el-tab-pane label="趋势分析" name="trend">
      <div class="paper"><div class="section-head"><div><h3>长期趋势与状态演变</h3><p>基准数据作为序列起点，不修改来源数据。</p></div><el-tag :type="target.riskLevel==='high'?'danger':'warning'">{{ target.currentStatus }}</el-tag></div><div ref="chartEl" class="chart"></div>
      <div class="trend-grid"><section><h4>历史状态时间轴</h4><el-timeline><el-timeline-item v-for="x in trendPoints" :key="`${x.date}-${x.label}`" :timestamp="x.date" :type="x.status==='warning'?'warning':x.status==='critical'?'danger':'success'"><b>{{ x.value }} {{ trendIndicator?.valueUnit }}</b> · {{ x.label }}</el-timeline-item></el-timeline></section><section><h4>影像时间序列</h4><div class="timeline-images"><figure v-if="target.baseline?.baselineFileUrl"><img :src="target.baseline.baselineFileUrl"><figcaption>{{ target.baseline.baselineDate }} · 基准</figcaption></figure><figure v-for="r in targetRecords.filter(x=>x.media?.length)" :key="r.id"><img :src="r.media[0].fileUrl"><figcaption>{{ String(r.monitoringDate).slice(0,10) }} · {{ statusMap[r.overallStatus] }}</figcaption></figure></div></section></div>
      <el-alert :title="trendPoints.length>1 ? `较基准变化 ${Number(trendPoints.at(-1).value-trendPoints[0].value).toFixed(2)}${trendIndicator?.valueUnit}，当前${target.currentStatus}` : '完成首次监测后将形成趋势判断'" :type="target.riskLevel==='high'?'warning':'success'" :closable="false"/>
      </div>
    </el-tab-pane>
    <el-tab-pane label="预警处理" name="alerts">
      <div class="paper"><div class="section-head"><div><h3>预警确认与闭环处理</h3><p>预警保留触发值、阈值与处理轨迹，不允许直接删除。</p></div><el-tag type="danger">{{ plan.alerts.length }} 条</el-tag></div>
      <article v-for="a in plan.alerts" :key="a.id" class="alert-row"><Warning/><div><small>{{ a.alertCode }} · {{ a.discoveredTime }}</small><h4>{{ a.alertTitle }}</h4><p>{{ a.alertDescription }}</p><span>触发：{{ a.thresholdDescription }}</span><div v-if="a.immediateAction" class="action-note">即时措施：{{ a.immediateAction }}<br>处理建议：{{ a.treatmentAdvice }}</div></div><aside><el-tag :type="a.alertLevel==='critical'?'danger':'warning'">{{ a.alertLevel }}</el-tag><el-tag type="info">{{ a.alertStatus }}</el-tag><el-button size="small" type="warning" @click="$emit('handle-alert',a)">确认/处理</el-button><el-button size="small" @click="$emit('navigate','disease',a.targetId)">病害调查</el-button></aside></article>
      <el-empty v-if="!plan.alerts.length" description="暂无监测预警"/></div>
    </el-tab-pane>
  </el-tabs>
  <el-dialog v-model="indicatorDialog" title="监测指标与预警阈值" width="min(760px,95vw)"><el-form label-position="top"><div class="dialog-grid"><el-form-item label="指标名称"><el-input v-model="indicatorForm.indicatorName"/></el-form-item><el-form-item label="指标编码"><el-input v-model="indicatorForm.indicatorCode"/></el-form-item><el-form-item label="类别"><el-select v-model="indicatorForm.indicatorCategory"><el-option label="结构" value="structure"/><el-option label="外观" value="appearance"/><el-option label="环境" value="environment"/><el-option label="材料" value="material"/></el-select></el-form-item><el-form-item label="单位"><el-input v-model="indicatorForm.valueUnit"/></el-form-item><el-form-item label="基准值"><el-input-number v-model="indicatorForm.baselineValue"/></el-form-item><el-form-item label="变化预警量"><el-input-number v-model="indicatorForm.changeWarningValue"/></el-form-item><el-form-item label="正常最小/最大"><div class="pair"><el-input-number v-model="indicatorForm.normalMin"/><el-input-number v-model="indicatorForm.normalMax"/></div></el-form-item><el-form-item label="黄色预警最大值"><el-input-number v-model="indicatorForm.warningMax"/></el-form-item><el-form-item label="红色预警最大值"><el-input-number v-model="indicatorForm.criticalMax"/></el-form-item><el-form-item label="仪器"><el-input v-model="indicatorForm.instrumentName"/></el-form-item><el-form-item class="wide" label="观察方法"><el-input v-model="indicatorForm.observationMethod"/></el-form-item></div></el-form><template #footer><el-button @click="indicatorDialog=false">取消</el-button><el-button type="primary" @click="saveIndicator">保存指标</el-button></template></el-dialog>
</main>
</template>

<style scoped>
.workspace{height:100%;min-height:0;background:#f4f6f5;border:1px solid #dde5e2;overflow:hidden;display:flex;flex-direction:column}.workspace>header{display:flex;justify-content:space-between;align-items:center;padding:15px 20px;background:#fff;border-bottom:1px solid #e3e9e6}.workspace>header small{font-size:9px;color:#799088}.workspace>header h2{margin:3px 0;font-size:18px;color:#2e453e}.workspace>header p{margin:0;font-size:10px;color:#89958f}.workspace>header>div:last-child{display:flex;gap:6px}.tabs{min-height:0;flex:1;display:flex;flex-direction:column}.tabs :deep(.el-tabs__header){margin:0;padding:0 20px;background:#fff}.tabs :deep(.el-tabs__content){min-height:0;flex:1;overflow:auto}.paper{max-width:1120px;margin:18px auto 32px;padding:25px 30px 38px;background:#fff;border:1px solid #e1e6e4;box-shadow:0 5px 20px rgba(43,65,58,.06)}.section-head,.execution-head{display:flex;justify-content:space-between;align-items:flex-start;gap:15px;margin-bottom:18px}.section-head h3,.execution-head h3{margin:0 0 4px;color:#2f4941;font-size:16px}.section-head p{margin:0;color:#8a9893;font-size:10px}.section-head.line{margin-top:28px;padding-top:22px;border-top:1px solid #e5eae8}.text-grid,.form-grid,.dialog-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px 18px;margin-top:18px}.text-grid section{padding:12px;background:#f7f9f8}.text-grid small{color:#8c9995}.text-grid p{margin:5px 0;color:#42554f;font-size:11px;line-height:1.6}.wide{grid-column:1/-1}.baseline{position:relative;display:flex;gap:16px;align-items:center;margin-top:20px;padding:16px;border-left:4px solid #5e8c7e;background:#f2f7f5}.baseline>div{flex:1}.baseline h4{margin:5px 0}.baseline p,.baseline span{font-size:10px;color:#687a74}.baseline img{width:150px;height:82px;object-fit:cover}.task{display:grid;grid-template-columns:34px 1fr auto auto;gap:12px;align-items:center;margin-bottom:9px;padding:13px;border:1px solid #e1e6e4;cursor:pointer}.task.selected{border-color:#669485;background:#f5f9f7}.task-icon{width:30px;height:30px;border-radius:50%;display:grid;place-items:center;background:#e8f1ee;color:#4e8171}.task-icon svg{width:15px}.task b,.task small{display:block}.task b{font-size:12px;color:#3b4b46}.task small{margin:4px 0 8px;font-size:9px;color:#8a9792}.execution-head{padding-bottom:15px;border-bottom:2px solid #466f63}.execution-head small{font-size:9px;color:#83928d}.compare-values{background:#f4f8f6;padding:15px}.paper>section{margin-top:22px}.paper section>h4{color:#354b44;font-size:13px}.compare-values>div{display:grid;grid-template-columns:1.2fr repeat(3,1fr) auto;align-items:center;gap:8px;padding:9px 0;border-top:1px solid #e0e8e5;font-size:10px}.compare-values span,.compare-values strong{display:block}.compare-values span{color:#8b9893}.compare-values strong{margin-top:2px;color:#495b55}.compare-values .current strong{color:#ac6b2e;font-size:13px}.value-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:10px 18px}.value-grid small{display:block;color:#9a7a52}.images{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:12px}.images figure,.timeline-images figure{margin:0;border:1px solid #dde4e1;background:#f1f4f3}.images img,.images figure>div{width:100%;height:230px;object-fit:cover;display:grid;place-items:center;color:#9ba6a2}.images figcaption,.timeline-images figcaption{padding:8px;font-size:9px;color:#697a74}.checks{display:flex;flex-wrap:wrap;gap:10px;padding:10px;background:#f8f9f8}.paper footer{display:flex;justify-content:flex-end;gap:8px;margin-top:25px;padding-top:18px;border-top:1px solid #e2e8e5}.chart{height:330px}.trend-grid{display:grid;grid-template-columns:1fr 1.4fr;gap:18px}.trend-grid>section{padding:15px;border:1px solid #e3e8e6}.timeline-images{display:grid;grid-template-columns:repeat(3,1fr);gap:8px}.timeline-images img{width:100%;height:95px;object-fit:cover}.alert-row{display:grid;grid-template-columns:30px 1fr 120px;gap:12px;padding:16px;border-left:4px solid #c88839;background:#fff8ef;margin-bottom:10px}.alert-row>svg{width:22px;color:#c88839}.alert-row small,.alert-row span{font-size:9px;color:#8e7b67}.alert-row h4{margin:5px 0;color:#74512e}.alert-row p{font-size:11px;color:#695d51}.alert-row aside{display:flex;flex-direction:column;align-items:stretch;gap:6px}.action-note{margin-top:10px;padding:8px;background:#fff;font-size:10px;line-height:1.7;color:#675f56}.pair{display:flex;gap:6px}
@media(max-width:900px){.paper{margin:10px;padding:18px}.text-grid,.form-grid,.value-grid,.dialog-grid,.trend-grid{grid-template-columns:1fr}.wide{grid-column:auto}.images{grid-template-columns:1fr}.compare-values>div{grid-template-columns:1fr 1fr}.task{grid-template-columns:30px 1fr}.task>*:nth-child(n+3){grid-column:2}.alert-row{grid-template-columns:25px 1fr}.alert-row aside{grid-column:2;flex-direction:row;flex-wrap:wrap}}
</style>
