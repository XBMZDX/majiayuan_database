<script setup>
import { computed } from 'vue'
import { Bell, Calendar, CirclePlus, DocumentChecked, FolderOpened, MoreFilled, VideoPlay } from '@element-plus/icons-vue'
const props = defineProps({ project: Object, plans: { type: Array, default: () => [] }, plan: Object, saving: Boolean })
defineEmits(['create', 'archive-plan', 'comparison-baseline', 'restoration-target', 'execute', 'save', 'navigate'])
const stats = computed(() => ({ active: props.plans.filter(x => x.planStatus === 'active').length, targets: props.plans.reduce((n, x) => n + x.targets.length, 0), due: props.plans.reduce((n, x) => n + x.tasks.filter(t => ['pending', 'in_progress', 'overdue'].includes(t.taskStatus)).length, 0), alerts: props.plans.reduce((n, x) => n + x.alerts.filter(a => !['resolved', 'closed', 'false_alarm'].includes(a.alertStatus)).length, 0) }))
</script>
<template>
<section class="summary">
  <div class="identity"><small>{{ project?.projectCode }} · {{ project?.artifactCode }}</small><h2>后续监测与风险预警</h2><p>{{ plan?.planName || '尚未选择监测计划' }}</p></div>
  <div class="stats"><div><b>{{ stats.active }}</b><span>执行中计划</span></div><div><b>{{ stats.targets }}</b><span>监测对象</span></div><div><b>{{ stats.due }}</b><span>待办任务</span></div><div class="alert"><b>{{ stats.alerts }}</b><span>开放预警</span></div></div>
  <div class="actions">
    <el-button type="primary" :icon="CirclePlus" @click="$emit('create')">新建计划</el-button>
    <el-dropdown><el-button :icon="DocumentChecked">从已有成果生成</el-button><template #dropdown><el-dropdown-menu><el-dropdown-item :icon="FolderOpened" @click="$emit('archive-plan')">从档案建议生成计划</el-dropdown-item><el-dropdown-item @click="$emit('comparison-baseline')">从前后对比建立基准</el-dropdown-item><el-dropdown-item @click="$emit('restoration-target')">从复原成果生成对象</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
    <el-button type="success" :icon="VideoPlay" :disabled="!plan" @click="$emit('execute')">执行监测</el-button><el-button :loading="saving" @click="$emit('save')">保存</el-button>
    <el-dropdown><el-button :icon="MoreFilled"/><template #dropdown><el-dropdown-menu><el-dropdown-item @click="$emit('navigate','restoration')">返回文物复原成果</el-dropdown-item><el-dropdown-item @click="$emit('navigate','comparison')">返回修复前后对比</el-dropdown-item><el-dropdown-item @click="$emit('navigate','archive')">查看保护修复档案</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
  </div>
  <div v-if="plan" class="cycle"><Calendar/> 下次监测 {{ plan.nextMonitoringDate || '待安排' }}<span><Bell/> {{ plan.alertEnabled ? '阈值预警已开启' : '预警未开启' }}</span></div>
</section>
</template>
<style scoped>
.summary{display:grid;grid-template-columns:minmax(230px,1fr) auto minmax(330px,auto);gap:18px;align-items:center;padding:18px 22px;background:linear-gradient(135deg,#183d35,#2b6559);color:#fff;border-radius:8px;box-shadow:0 8px 22px rgba(22,60,51,.16)}.identity small{color:#afc9c2;letter-spacing:.06em}.identity h2{margin:4px 0;font-size:21px}.identity p{margin:0;color:#d6e5e1;font-size:12px}.stats{display:flex;gap:5px}.stats div{min-width:66px;padding:7px 10px;border-left:1px solid rgba(255,255,255,.16);text-align:center}.stats b,.stats span{display:block}.stats b{font-size:20px}.stats span{font-size:10px;color:#c5d8d3}.stats .alert b{color:#ffd59a}.actions{display:flex;justify-content:flex-end;flex-wrap:wrap;gap:8px}.actions :deep(.el-button){margin:0}.cycle{grid-column:1/-1;display:flex;align-items:center;gap:7px;padding-top:10px;border-top:1px solid rgba(255,255,255,.12);font-size:11px;color:#cee0dc}.cycle svg{width:13px}.cycle span{display:flex;align-items:center;gap:5px;margin-left:18px}@media(max-width:1400px){.summary{grid-template-columns:1fr auto}.actions{grid-column:1/-1;justify-content:flex-start}}@media(max-width:760px){.summary{grid-template-columns:1fr;padding:16px}.stats{overflow-x:auto}.actions,.cycle{grid-column:auto}.cycle{flex-wrap:wrap}}
</style>
