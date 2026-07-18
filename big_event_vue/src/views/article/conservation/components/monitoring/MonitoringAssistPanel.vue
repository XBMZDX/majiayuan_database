<script setup>
import { computed } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
const props = defineProps({ plan: Object, target: Object, record: Object, missing: { type: Array, default: () => [] }, completeness: Number })
defineEmits(['focus', 'navigate', 'handle-alert'])
const alert = computed(() => props.plan?.alerts.find(x => x.targetId === props.target?.id && !['closed', 'resolved', 'false_alarm'].includes(x.alertStatus)))
const lastValue = computed(() => props.record?.values?.[0])
const riskMap = { high: ['高风险','danger'], medium: ['中风险','warning'], low: ['低风险','success'] }
</script>
<template>
<aside class="assist" v-if="plan && target"><section class="risk" :class="target.riskLevel"><small>CURRENT RISK</small><h3>{{ riskMap[target.riskLevel]?.[0] }}</h3><p>{{ target.currentStatus }} · {{ target.targetPart }}</p></section>
<section><h4>状态快照</h4><dl><dt>监测基准</dt><dd>{{ target.baseline?.baselineDescription || '未建立' }}</dd><dt>最新记录</dt><dd>{{ lastValue ? `${lastValue.valueNumber}${lastValue.valueUnit}` : '暂无记录' }}</dd><dt>下次监测</dt><dd>{{ record?.nextMonitoringDate || plan.nextMonitoringDate || '待安排' }}</dd><dt>基准版本</dt><dd>{{ target.baseline?.versionNo || '-' }}（只读）</dd></dl></section>
<section v-if="alert" class="alert"><h4><WarningFilled/> 开放预警</h4><b>{{ alert.alertTitle }}</b><p>{{ alert.alertDescription }}</p><el-button size="small" type="warning" @click="$emit('handle-alert',alert)">进入处理</el-button></section>
<section><h4>记录完整度 <b>{{ completeness }}%</b></h4><el-progress :percentage="completeness" :stroke-width="7" :color="completeness===100?'#4b8b72':'#b88445'"/><button v-for="x in missing" :key="x.key" class="missing" @click="$emit('focus',x.tab)">{{ x.label }} <span>去填写</span></button><p v-if="!missing.length" class="ok">关键内容已完整，可提交记录。</p></section>
<section><h4>来源追溯</h4><button class="link" @click="$emit('navigate',target.sourceBusinessType,target.sourceBusinessId)">查看 {{ target.sourceBusinessType }}</button><button class="link" @click="$emit('navigate','archive')">查看档案后续建议</button></section></aside>
</template>
<style scoped>
.assist{height:100%;min-height:0;overflow:auto;background:#fff;border:1px solid #dde5e2}.assist section{padding:15px;border-bottom:1px solid #e9eeec}.risk{color:#fff;background:#6c8f85}.risk.high{background:#8e4d47}.risk.medium{background:#9b703b}.risk.low{background:#4c7e6b}.risk small{font-size:9px;letter-spacing:.15em;opacity:.75}.risk h3{margin:5px 0;font-size:21px}.risk p{margin:0;font-size:11px;opacity:.85}.assist h4{margin:0 0 11px;font-size:12px;color:#3d514a;display:flex;align-items:center;justify-content:space-between}.assist h4 svg{width:14px;margin-right:5px}.assist dl{margin:0}.assist dt{font-size:9px;color:#9aa6a2}.assist dd{margin:3px 0 10px;font-size:10px;color:#53635e;line-height:1.5}.alert{background:#fff8ed}.alert b{font-size:11px;color:#8e5c28}.alert p{font-size:10px;color:#7a6b5d;line-height:1.6}.missing,.link{width:100%;margin-top:7px;border:0;background:#f7f9f8;color:#596963;padding:7px;text-align:left;font-size:10px;cursor:pointer}.missing span{float:right;color:#a46c34}.link{color:#3d7767;border-left:2px solid #76a092}.ok{font-size:10px;color:#4b8b72}
</style>
