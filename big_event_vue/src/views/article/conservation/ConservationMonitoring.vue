<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MonitoringSummaryBar from './components/monitoring/MonitoringSummaryBar.vue'
import MonitoringPlanTargetList from './components/monitoring/MonitoringPlanTargetList.vue'
import MonitoringWorkspace from './components/monitoring/MonitoringWorkspace.vue'
import MonitoringAssistPanel from './components/monitoring/MonitoringAssistPanel.vue'
import { calculateIndicatorLevel, createDraftRecord, createEmptyMonitoringPlan } from './monitoringModel'
import {
    getMonitoringPlans, getMonitoringProject, getMonitoringSources,
    getComparisonMediaContent, getMonitoringMediaContent, getRestorationMediaContent,
    createProjectFromAlert, saveMonitoringWorkbench, uploadMonitoringMedia
} from '@/api/conservationMonitoring'

const route = useRoute(), router = useRouter()
const projectId = computed(() => Number(route.params.projectId || route.query.projectId) || null)
const loading = ref(true), loadError = ref(''), saving = ref(false), dirty = ref(false)
const project = ref(null), sources = ref(null), plans = ref([])
const activePlanId = ref(null), activeTargetId = ref(null), activeTaskId = ref(null), activeRecordId = ref(null), activeTab = ref('overview')
const planDialog = ref(false), planStep = ref(0), targetDialog = ref(false), assistDrawer = ref(false), listDrawer = ref(false), alertDialog = ref(false), handlingAlert = ref(null)
const projectDialog = ref(false), creatingProject = ref(false)
const planForm = reactive({ sourceMode: 'manual', planCode: '', planName: '', planType: 'comprehensive', monitoringPurpose: '', monitoringScope: '', overallStrategy: '', responsiblePerson: '', participantNames: '', monitoringLocation: '文物保护实验室', startDate: '', expectedEndDate: '', defaultFrequencyValue: 1, defaultFrequencyUnit: 'month', autoGenerateTask: true, alertEnabled: true, selectedDiseaseIds: [], selectedComparisonIds: [], selectedRestorationIds: [] })
const targetForm = reactive({ id: null, targetType: 'disease', targetName: '', sourceBusinessType: 'manual', sourceBusinessId: null, targetPart: '', targetLocation: '', riskLevel: 'medium', priorityLevel: 'medium', monitoringReason: '', currentStatus: '待首次监测', requiresImage: true, enabled: true, shootingPosition: '' })
const alertForm = reactive({ immediateAction: '', treatmentAdvice: '', alertStatus: 'processing', requiresRecheck: true, requiresDiseaseSurvey: false, requiresIntervention: false, requiresNewProject: false })
const projectForm = reactive({ projectCode: '', projectName: '', projectType: '综合', riskLevel: 'medium', principal: '', department: '', startDate: '', expectedEndDate: '', summary: '' })
const objectUrls = new Set()

const plan = computed(() => plans.value.find(x => x.id === activePlanId.value) || null)
const target = computed(() => plan.value?.targets.find(x => x.id === activeTargetId.value) || null)
const task = computed(() => plan.value?.tasks.find(x => x.id === activeTaskId.value) || plan.value?.tasks.find(x => x.taskStatus === 'in_progress') || null)
const record = computed(() => plan.value?.records.find(x => x.id === activeRecordId.value) || plan.value?.records.find(x => x.taskId === task.value?.id && x.targetId === target.value?.id) || null)
const missing = computed(() => {
    if (!record.value || record.value.monitoringStatus === 'submitted') return []
    const r = record.value, result = [], add = (key, label, tab = 'execution') => result.push({ key, label, tab })
    if (target.value.indicators.some(i => i.required && r.values.find(v => v.indicatorId === i.id)?.valueNumber == null)) add('values', '必填指标尚未全部记录')
    if (!r.overallStatus) add('status', '未选择整体状态')
    if (!r.comparisonResult) add('compare', '未填写与基准比较结果')
    if (!r.observationDescription?.trim()) add('observation', '缺少状态观察描述')
    if (target.value.requiresImage && !r.media?.length) add('media', '缺少同角度监测影像')
    if (!r.resultConclusion?.trim()) add('conclusion', '缺少监测结论')
    if (!r.nextMonitoringDate) add('next', '未安排下一次监测')
    return result
})
const completeness = computed(() => record.value ? Math.round((7 - missing.value.length) / 7 * 100) : 0)

const selectPlan = id => { activePlanId.value = id; activeTargetId.value = plan.value?.targets[0]?.id || null; activeTaskId.value = plan.value?.tasks.find(x => x.taskStatus === 'in_progress')?.id || plan.value?.tasks[0]?.id || null; activeRecordId.value = null }
const selectTarget = id => { activeTargetId.value = id; activeRecordId.value = plan.value?.records.find(x => x.taskId === task.value?.id && x.targetId === id)?.id || null; listDrawer.value = false }
const selectTask = item => { activeTaskId.value = item.id; activeRecordId.value = plan.value.records.find(x => x.taskId === item.id && x.targetId === target.value?.id)?.id || null }
const markDirty = () => { dirty.value = true }
const asObjectUrl = async requestCall => {
    try {
        const response = await requestCall()
        const url = URL.createObjectURL(response.data)
        objectUrls.add(url)
        return url
    } catch {
        return ''
    }
}
const hydrateProtectedMedia = async () => {
    const jobs = []
    for (const p of plans.value) {
        for (const t of p.targets) {
            const baseline = t.baseline
            if (baseline?.baselineMediaId) {
                const loader = baseline.sourceBusinessType === 'comparison_after'
                    ? getComparisonMediaContent
                    : baseline.sourceBusinessType === 'restoration'
                        ? getRestorationMediaContent
                        : null
                if (loader) jobs.push(asObjectUrl(() => loader(baseline.baselineMediaId)).then(url => { baseline.baselineFileUrl = url }))
            }
        }
        for (const r of p.records) {
            for (const media of r.media || []) {
                jobs.push(asObjectUrl(() => getMonitoringMediaContent(media.id)).then(url => { media.fileUrl = url }))
            }
        }
    }
    for (const comparison of sources.value.comparisons || []) {
        for (const media of comparison.images || []) {
            jobs.push(asObjectUrl(() => getComparisonMediaContent(media.id)).then(url => { media.fileUrl = url }))
        }
    }
    for (const restoration of sources.value.restorations || []) {
        for (const media of restoration.media || []) {
            jobs.push(asObjectUrl(() => getRestorationMediaContent(media.id)).then(url => { media.fileUrl = url }))
        }
    }
    await Promise.all(jobs)
}

const load = async () => {
    loading.value = true; loadError.value = ''
    try {
        if (!projectId.value) throw new Error('缺少保护修复项目ID')
        const [projectResult, sourceResult, planResult] = await Promise.all([
            getMonitoringProject(projectId.value),
            getMonitoringSources(projectId.value),
            getMonitoringPlans(projectId.value)
        ])
        project.value = projectResult.data
        sources.value = sourceResult.data
        plans.value = planResult.data || []
        if (!project.value) throw new Error('未找到对应的保护修复项目')
        await hydrateProtectedMedia()
        activePlanId.value = Number(route.query.planId) || plans.value[0]?.id
        activeTargetId.value = Number(route.query.targetId) || plan.value?.targets[0]?.id
        activeTaskId.value = Number(route.query.taskId) || plan.value?.tasks.find(x => x.taskStatus === 'in_progress')?.id || plan.value?.tasks[0]?.id
        activeRecordId.value = plan.value?.records.find(x => x.taskId === activeTaskId.value && x.targetId === activeTargetId.value)?.id || null
    } catch (error) { loadError.value = error.message || '监测数据加载失败，请稍后重试' }
    finally { loading.value = false }
}
const save = async (show = true) => {
    saving.value = true
    try {
        const result = await saveMonitoringWorkbench(projectId.value, plans.value)
        plans.value = result.data || plans.value
        await hydrateProtectedMedia()
        dirty.value = false
        if (show) ElMessage.success('监测工作台已保存到MySQL')
    } finally {
        saving.value = false
    }
}

const openPlan = sourceMode => {
    planStep.value = 0
    Object.assign(planForm, { sourceMode: sourceMode || 'manual', planCode: `MP-${project.value.artifactCode.replace(/\W/g,'')}-${String(plans.value.length+1).padStart(3,'0')}`, planName: `${project.value.artifactCode}${project.value.artifactName}后续监测计划`, planType: 'comprehensive', monitoringPurpose: '', monitoringScope: '', overallStrategy: '定期量化监测与同角度影像记录，异常自动触发预警。', responsiblePerson: project.value.principal, participantNames: '', monitoringLocation: '文物保护实验室', startDate: '2026-08-01', expectedEndDate: '2028-08-01', defaultFrequencyValue: 1, defaultFrequencyUnit: 'month', autoGenerateTask: true, alertEnabled: true, selectedDiseaseIds: [], selectedComparisonIds: [], selectedRestorationIds: [] })
    if (sourceMode === 'archive') { planForm.monitoringPurpose = sources.value.archiveAdvice.followUpAdvice || '落实档案中的长期保存建议'; planForm.monitoringScope = sources.value.archiveAdvice.monitorDiseases || '重点病害与保存环境'; planForm.selectedDiseaseIds = sources.value.diseases.filter(x => ['severe','critical'].includes(x.severity)).map(x => x.id) }
    planDialog.value = true
}
const buildDiseaseTarget = disease => ({ id: Date.now()+disease.id, targetType: 'disease', targetName: `${disease.partName}${disease.side}${disease.diseaseName}`, sourceBusinessType: 'disease_record', sourceBusinessId: disease.id, targetPart: disease.partName, targetLocation: disease.positionDescription, riskLevel: ['severe','critical'].includes(disease.severity)?'high':'medium', priorityLevel: disease.emergency?'high':'medium', monitoringReason: disease.recommendedAction, currentStatus: disease.developmentStatus, requiresImage: true, enabled: true, shootingPosition: '固定标尺正视', indicators: [], baseline: null })
const createPlan = () => {
    if (!planForm.planName || !planForm.monitoringPurpose) return ElMessage.warning('请填写计划名称与监测目的')
    const item = createEmptyMonitoringPlan(project.value)
    Object.assign(item, planForm, { id: Date.now(), planStatus: 'draft', nextMonitoringDate: planForm.startDate, targets: [], tasks: [], records: [], alerts: [] })
    item.targets = sources.value.diseases.filter(x => planForm.selectedDiseaseIds.includes(x.id)).map(buildDiseaseTarget)
    sources.value.comparisons.filter(x => planForm.selectedComparisonIds.includes(x.id)).forEach((x,i) => item.targets.push(targetFromComparison(x, Date.now()+100+i)))
    sources.value.restorations.filter(x => planForm.selectedRestorationIds.includes(x.id)).forEach((x,i) => item.targets.push(targetFromRestoration(x, Date.now()+200+i)))
    plans.value.unshift(item); activePlanId.value = item.id; activeTargetId.value = item.targets[0]?.id || null; planDialog.value = false; dirty.value = true; ElMessage.success('监测计划草稿已创建')
}
const targetFromComparison = (x,id=Date.now()) => {
    const metric = x.metrics?.[0], after = (x.images || x.afterImages || []).find(m => m.imageStage === 'after') || x.afterImages?.[0]
    const sourceId = x.id || x.comparisonId, title = x.comparisonTitle || x.title || '修复后状态监测', part = x.targetPart || x.part
    const afterStatus = x.afterSummary || x.diseases?.map(d => d.afterStatus).filter(Boolean).join('；') || '已建立修复后基准'
    return { id, targetType: 'disease', targetName: title.replace('灌浆前后对比','后续监测'), sourceBusinessType:'comparison_after', sourceBusinessId:sourceId, targetPart:part, targetLocation:x.monitoring?.reviewPart||part, riskLevel:'high', priorityLevel:'high', monitoringReason:x.evaluation?.remainingIssue||x.monitoring?.notes||'持续观察修复后状态', currentStatus:'基准已建立', requiresImage:true, enabled:true, shootingPosition:x.shootingPosition, baseline:{ id:Date.now()+1, sourceBusinessType:'comparison_after', sourceBusinessId:sourceId, baselineDate:x.evaluationDate||after?.shootingTime?.slice(0,10)||'', baselineStatus:afterStatus, baselineDescription:`${metric?.metricName||'状态'} ${metric?.afterValue??'-'}${metric?.valueUnit||''}`, baselineMediaId:after?.id||null, baselineFileUrl:after?.fileUrl, versionNo:'V1.0', isCurrent:true }, indicators: metric ? [{ id:Date.now()+2, indicatorCode:'IMPORTED-METRIC', indicatorName:metric.metricName, indicatorCategory:metric.metricCategory, dataType:'number', valueUnit:metric.valueUnit, baselineValue:metric.afterValue, normalMin:0, normalMax:Number(metric.afterValue)+.1, warningMax:Number(metric.afterValue)+.2, criticalMax:Number(metric.afterValue)+.5, changeWarningValue:.2, expectedDirection:'stable', observationMethod:'同方法复测', instrumentName:'', required:true }] : [] }
}
const targetFromRestoration = (x,id=Date.now()) => {
    const media = x.media.find(m => m.selectedAsMonitoringBaseline)||x.media.find(m=>m.isPrimary), part=x.parts.find(p=>x.monitoring.partIds.includes(p.id))||x.parts[0]
    return { id, targetType:'restoration_part', targetName:part?.partName||x.resultName, sourceBusinessType:'restoration_part', sourceBusinessId:part?.id||x.id, targetPart:x.targetPart, targetLocation:part?.targetLocation||x.targetPart, riskLevel:'medium', priorityLevel:'medium', monitoringReason:x.monitoring.note||'实体复原部分长期稳定性监测', currentStatus:'稳定', requiresImage:true, enabled:true, shootingPosition:'固定正视', baseline:{ id:Date.now()+1, sourceBusinessType:'restoration', sourceBusinessId:x.id, baselineDate:x.completionDate, baselineStatus:x.evaluation.finalConclusion, baselineDescription:`完成状态 · ${x.currentVersion}`, baselineMediaId:media?.id||null, baselineFileUrl:media?.fileUrl, versionNo:'V1.0', isCurrent:true }, indicators:[{ id:Date.now()+2, indicatorCode:'RESTORATION-JOINT', indicatorName:(x.monitoring.indicators||'接缝宽度').split('、')[0], indicatorCategory:'structure', dataType:'number', valueUnit:'mm', baselineValue:.8, normalMin:0, normalMax:.9, warningMax:1, criticalMax:1.3, changeWarningValue:.2, expectedDirection:'stable', observationMethod:'接缝宽度测量', instrumentName:'游标卡尺', required:true }] }
}
const importComparison = () => {
    if (!plan.value) return openPlan('comparison')
    const candidates = sources.value.comparisons || []
    let count=0; candidates.forEach(x=>{ if(!plan.value.targets.some(t=>t.sourceBusinessType==='comparison_after'&&t.sourceBusinessId===x.id)){ plan.value.targets.push(targetFromComparison(x)); count++ } })
    if (count) { activeTargetId.value=plan.value.targets.at(-1).id; dirty.value=true; ElMessage.success(`已从前后对比建立 ${count} 个版本化基准`) } else ElMessage.info('已选前后对比基准均已关联')
}
const importRestoration = () => {
    if (!plan.value) return openPlan('restoration')
    const candidates = sources.value.restorations || []
    let count=0; candidates.forEach(x=>{ if(!plan.value.targets.some(t=>t.sourceBusinessType==='restoration_part'&&t.sourceBusinessId===(x.monitoring?.partIds?.[0]||x.id))){ plan.value.targets.push(targetFromRestoration(x)); count++ } })
    if(count){activeTargetId.value=plan.value.targets.at(-1).id;dirty.value=true;ElMessage.success(`已从复原成果生成 ${count} 个监测对象`)}else ElMessage.info('需要监测的复原成果均已关联')
}
const openTarget = item => { Object.assign(targetForm, item ? { ...item } : { id:null,targetType:'disease',targetName:'',sourceBusinessType:'manual',sourceBusinessId:null,targetPart:'',targetLocation:'',riskLevel:'medium',priorityLevel:'medium',monitoringReason:'',currentStatus:'待首次监测',requiresImage:true,enabled:true,shootingPosition:'' }); targetDialog.value=true }
const saveTarget = () => {
    if(!targetForm.targetName||!targetForm.targetPart)return ElMessage.warning('请填写对象名称和部位')
    if(targetForm.id)Object.assign(plan.value.targets.find(x=>x.id===targetForm.id),targetForm)
    else { const item={...targetForm,id:Date.now(),indicators:[],baseline:null};plan.value.targets.push(item);activeTargetId.value=item.id }
    targetDialog.value=false;dirty.value=true
}
const generateTask = () => {
    if(!plan.value.targets.length)return ElMessage.warning('请先添加监测对象')
    const index=plan.value.tasks.length+1,date=plan.value.nextMonitoringDate||new Date().toISOString().slice(0,10)
    const item={id:Date.now(),taskCode:`MT-${project.value.artifactCode.replace(/\W/g,'')}-${String(index).padStart(3,'0')}`,taskName:`${date.slice(0,7)}后续监测任务`,taskType:'routine',taskStatus:'pending',plannedDate:date,dueDate:date,actualStartTime:'',actualEndTime:'',responsiblePerson:plan.value.responsiblePerson,participantNames:plan.value.participantNames,targetCount:plan.value.targets.length,completedTargetCount:0,completionRate:0,overallResult:'',summary:'',generatedAutomatically:true}
    plan.value.tasks.unshift(item);activeTaskId.value=item.id;activeTab.value='tasks';dirty.value=true;ElMessage.success('已生成下一监测任务')
}
const startTask = item => { item.taskStatus='in_progress';item.actualStartTime=new Date().toLocaleString('zh-CN',{hour12:false});activeTaskId.value=item.id;startRecord(item) }
const startRecord = item => {
    if(!item)return ElMessage.warning('请先选择监测任务')
    if(item.taskStatus==='pending')startTask(item)
    else {
        let r=plan.value.records.find(x=>x.taskId===item.id&&x.targetId===target.value.id)
        if(r?.monitoringStatus==='submitted'){
            const next=plan.value.targets.find(t=>!plan.value.records.some(x=>x.taskId===item.id&&x.targetId===t.id&&x.monitoringStatus==='submitted'))
            if(!next)return ElMessage.success('当前任务的所有对象记录均已提交，可完成监测任务')
            activeTargetId.value=next.id;r=plan.value.records.find(x=>x.taskId===item.id&&x.targetId===next.id)
        }
        if(!r){r=createDraftRecord(plan.value,item,target.value);plan.value.records.push(r)}
        activeRecordId.value=r.id;activeTab.value='execution';dirty.value=true
    }
}
const calculateRecord = () => {
    const r=record.value
    r.values.forEach(v=>{const i=target.value.indicators.find(x=>x.id===v.indicatorId),result=calculateIndicatorLevel(i,v.valueNumber,v.baselineValue);v.previousValue=v.previousValue??v.baselineValue;v.changeValue=v.valueNumber==null?null:Number((Number(v.valueNumber)-Number(v.baselineValue)).toFixed(2));v.changeRate=v.valueNumber==null||!Number(v.baselineValue)?null:Number((v.changeValue/Number(v.baselineValue)*100).toFixed(1));v.resultLevel=result.level;v.resultDescription=result.description})
    const critical=r.values.some(x=>x.resultLevel==='critical'),warning=r.values.some(x=>x.resultLevel==='warning')
    if(critical||warning){const level=critical?'critical':'warning';target.value.riskLevel=critical?'high':target.value.riskLevel;target.value.currentStatus=critical?'严重异常':'轻微变化';if(!plan.value.alerts.some(a=>a.recordId===r.id)){const v=r.values.find(x=>x.resultLevel===level);plan.value.alerts.unshift({id:Date.now(),alertCode:`ALERT-${project.value.artifactCode}-${String(plan.value.alerts.length+1).padStart(3,'0')}`,taskId:r.taskId,recordId:r.id,targetId:target.value.id,indicatorId:v.indicatorId,alertLevel:level,alertTitle:`${target.value.targetName}${v.indicatorName}异常`,alertDescription:`${v.indicatorName}本次值${v.valueNumber}${v.valueUnit}，基准${v.baselineValue}${v.valueUnit}`,triggerType:'threshold',triggerValue:`${v.valueNumber}${v.valueUnit}`,thresholdDescription:v.resultDescription,alertStatus:'new',discoveredTime:new Date().toLocaleString('zh-CN',{hour12:false}),immediateAction:'',treatmentAdvice:'',requiresRecheck:true,requiresDiseaseSurvey:false,requiresIntervention:critical,requiresNewProject:false})}}
}
const saveRecord = () => { if(!record.value)return;calculateRecord();save();ElMessage.success('监测记录草稿已保存，指标结果已自动计算') }
const submitRecord = async () => {
    calculateRecord()
    if(missing.value.length){activeTab.value='execution';return ElMessage.warning(`记录尚缺 ${missing.value.length} 项关键内容`)}
    try{await ElMessageBox.confirm('提交后记录默认只读，确认提交？','提交监测记录',{type:'warning'});record.value.monitoringStatus='submitted';const t=task.value;t.completedTargetCount=plan.value.records.filter(x=>x.taskId===t.id&&x.monitoringStatus==='submitted').length;t.completionRate=Math.round(t.completedTargetCount/t.targetCount*100);plan.value.nextMonitoringDate=record.value.nextMonitoringDate;dirty.value=true;await save(false);ElMessage.success('对象监测记录已提交')}catch{}
}
const completeTask = async () => {
    if(!task.value)return
    const records=plan.value.records.filter(x=>x.taskId===task.value.id&&x.monitoringStatus==='submitted')
    if(records.length<task.value.targetCount)return ElMessage.warning(`还有 ${task.value.targetCount-records.length} 个对象未提交记录`)
    task.value.taskStatus='completed';task.value.actualEndTime=new Date().toLocaleString('zh-CN',{hour12:false});task.value.completionRate=100;task.value.completedTargetCount=task.value.targetCount;task.value.overallResult=plan.value.alerts.some(a=>a.taskId===task.value.id)?'attention':'normal';plan.value.executionCount++;plan.value.completionRate=100;dirty.value=true;await save(false);ElMessage.success('监测任务已完成，可生成下一周期任务')
}
const openAlert = item => { handlingAlert.value=item;Object.assign(alertForm,{immediateAction:item.immediateAction||'',treatmentAdvice:item.treatmentAdvice||'',alertStatus:item.alertStatus==='new'?'confirmed':item.alertStatus,requiresRecheck:item.requiresRecheck,requiresDiseaseSurvey:item.requiresDiseaseSurvey,requiresIntervention:item.requiresIntervention,requiresNewProject:item.requiresNewProject});alertDialog.value=true;activeTab.value='alerts';assistDrawer.value=false }
const saveAlert = () => { Object.assign(handlingAlert.value,alertForm,{confirmedTime:handlingAlert.value.confirmedTime||new Date().toLocaleString('zh-CN',{hour12:false}),confirmedBy:handlingAlert.value.confirmedBy||project.value.principal});alertDialog.value=false;dirty.value=true;ElMessage.success('预警处理状态已更新并保留处理轨迹') }
const uploadMedia = async file => {
    if (!record.value || !file) return
    await save(false)
    const form = new FormData()
    form.append('file', file)
    form.append('mediaRole', 'current')
    form.append('shootingPosition', target.value.shootingPosition || '')
    form.append('shootingTime', new Date().toISOString().slice(0, 19))
    form.append('title', `${target.value.targetName}本次监测影像`)
    form.append('createdBy', record.value.monitorPerson || project.value.principal || '')
    const result = await uploadMonitoringMedia(record.value.id, form)
    if (result.data.contentType?.startsWith('image/')) {
        result.data.fileUrl = await asObjectUrl(() => getMonitoringMediaContent(result.data.id))
    }
    record.value.media ||= []
    record.value.media.push(result.data)
    ElMessage.success('文件已上传并保存到MySQL')
}
const dateAfter = days => {
    const value = new Date()
    value.setDate(value.getDate() + days)
    return value.toISOString().slice(0, 10)
}
const createNewProject = item => {
    const alert = item?.id ? item : handlingAlert.value
    if (!alert) return ElMessage.warning('请先选择一条监测预警')
    handlingAlert.value = alert
    if (alert.createdProjectId) return router.push(`/conservation/project/${alert.createdProjectId}/disease`)
    if (!alertDialog.value) {
        Object.assign(alertForm, {
            immediateAction: alert.immediateAction || '',
            treatmentAdvice: alert.treatmentAdvice || '',
            alertStatus: alert.alertStatus === 'new' ? 'confirmed' : alert.alertStatus,
            requiresRecheck: alert.requiresRecheck,
            requiresDiseaseSurvey: alert.requiresDiseaseSurvey,
            requiresIntervention: alert.requiresIntervention,
            requiresNewProject: true
        })
    }
    alertForm.requiresNewProject = true
    const artifactLabel = project.value.artifactName || project.value.artifactCode || '文物'
    Object.assign(projectForm, {
        projectCode: `CR-ALERT-${alert.id}`,
        projectName: `${artifactLabel}${alert.alertTitle || '监测异常'}保护修复项目`,
        projectType: '综合',
        riskLevel: alert.alertLevel === 'critical' ? 'high' : 'medium',
        principal: project.value.principal || '',
        department: project.value.department || '',
        startDate: new Date().toISOString().slice(0, 10),
        expectedEndDate: dateAfter(90),
        summary: alert.treatmentAdvice || alertForm.treatmentAdvice || ''
    })
    projectDialog.value = true
}
const submitAlertProject = async () => {
    if (!handlingAlert.value) return
    if (!projectForm.projectName.trim()) return ElMessage.warning('请填写项目名称')
    if (!(project.value.artifactName || '').trim()) return ElMessage.warning('来源项目缺少文物名称，请先完善项目信息')
    creatingProject.value = true
    const alertId = handlingAlert.value.id
    try {
        Object.assign(handlingAlert.value, alertForm, {
            requiresNewProject: true,
            confirmedTime: handlingAlert.value.confirmedTime || new Date().toLocaleString('zh-CN', { hour12: false }),
            confirmedBy: handlingAlert.value.confirmedBy || project.value.principal
        })
        dirty.value = true
        await save(false)
        const result = await createProjectFromAlert(alertId, { ...projectForm })
        const created = result.data
        const persistedAlert = plans.value.flatMap(x => x.alerts || []).find(x => Number(x.id) === Number(alertId))
        if (persistedAlert) {
            persistedAlert.createdProjectId = created.id
            persistedAlert.projectCreatedTime = new Date().toLocaleString('zh-CN', { hour12: false })
            persistedAlert.requiresNewProject = true
            if (['new', 'confirmed'].includes(persistedAlert.alertStatus)) persistedAlert.alertStatus = 'processing'
        }
        projectDialog.value = false
        alertDialog.value = false
        dirty.value = false
        ElMessage.success(created.alreadyCreated
            ? `该预警已关联项目：${created.projectName}`
            : `已创建保护修复项目：${created.projectName}`)
    } finally {
        creatingProject.value = false
    }
}
const navigate = (type,id) => {
    const map={archive:`/conservation/project/${projectId.value}/archive?section=advice`,comparison:`/conservation/project/${projectId.value}/comparison?comparisonId=${id||''}`,comparison_after:`/conservation/project/${projectId.value}/comparison?comparisonId=${id||''}`,restoration:`/conservation/project/${projectId.value}/restoration?resultId=${id||''}`,restoration_part:`/conservation/project/${projectId.value}/restoration?resultId=${id||''}`,disease:`/conservation/project/${projectId.value}/disease`,disease_record:`/conservation/project/${projectId.value}/disease`,process_step:`/conservation/project/${projectId.value}/process?stepId=${id||''}`,archive_advice:`/conservation/project/${projectId.value}/archive?section=advice`}
    if(map[type])router.push(map[type])
}
onBeforeRouteLeave(async()=>{if(!dirty.value)return true;try{await ElMessageBox.confirm('监测工作台有未保存修改，是否保存后离开？','未保存提示',{confirmButtonText:'保存并离开',cancelButtonText:'直接离开',distinguishCancelAndClose:true});await save(false);return true}catch(e){return e==='cancel'}})
onBeforeUnmount(() => objectUrls.forEach(url => URL.revokeObjectURL(url)))
onMounted(load)
</script>

<template>
<div class="monitoring-page">
  <el-skeleton v-if="loading" animated :rows="14" class="loading"/>
  <el-result v-else-if="loadError" icon="error" title="后续监测加载失败" :sub-title="loadError"><template #extra><el-button type="primary" @click="load">重新加载</el-button></template></el-result>
  <template v-else>
    <MonitoringSummaryBar :project="project" :plans="plans" :plan="plan" :saving="saving" @create="openPlan('manual')" @archive-plan="openPlan('archive')" @comparison-baseline="importComparison" @restoration-target="importRestoration" @execute="startRecord(task)" @save="save()" @navigate="navigate"/>
    <div class="mobile-tools"><el-button @click="listDrawer=true">计划与对象</el-button><el-button @click="assistDrawer=true">风险与完整度</el-button></div>
    <div class="layout">
      <MonitoringPlanTargetList class="left" :plans="plans" :active-plan-id="activePlanId" :active-target-id="activeTargetId" @select-plan="selectPlan" @select-target="selectTarget" @add-target="openTarget()"/>
      <MonitoringWorkspace :plan="plan" :target="target" :task="task" :record="record" v-model:active-tab="activeTab" @dirty="markDirty" @add-target="openTarget()" @generate-task="generateTask" @start-task="startTask" @select-task="selectTask" @create-record="startRecord" @save-record="saveRecord" @submit-record="submitRecord" @complete-task="completeTask" @handle-alert="openAlert" @create-project="createNewProject" @upload-media="uploadMedia" @navigate="navigate"/>
      <MonitoringAssistPanel class="right" :plan="plan" :target="target" :record="record" :missing="missing" :completeness="completeness" @focus="activeTab=$event" @navigate="navigate" @handle-alert="openAlert"/>
    </div>
  </template>
  <el-drawer v-model="listDrawer" title="计划与对象" direction="ltr" size="min(340px,92vw)"><MonitoringPlanTargetList :plans="plans" :active-plan-id="activePlanId" :active-target-id="activeTargetId" @select-plan="selectPlan" @select-target="selectTarget" @add-target="openTarget()"/></el-drawer>
  <el-drawer v-model="assistDrawer" title="风险与完整度" size="min(330px,92vw)"><MonitoringAssistPanel :plan="plan" :target="target" :record="record" :missing="missing" :completeness="completeness" @focus="activeTab=$event;assistDrawer=false" @navigate="navigate" @handle-alert="openAlert"/></el-drawer>

  <el-dialog v-model="planDialog" title="新建后续监测计划" width="min(900px,96vw)">
    <el-steps :active="planStep" finish-status="success" align-center><el-step title="来源"/><el-step title="基本信息"/><el-step title="监测对象"/><el-step title="指标策略"/><el-step title="基准确认"/></el-steps>
    <div class="step-body" v-if="planStep===0"><el-radio-group v-model="planForm.sourceMode"><el-radio-button label="manual">手工创建</el-radio-button><el-radio-button label="archive">档案建议</el-radio-button><el-radio-button label="comparison">前后对比</el-radio-button><el-radio-button label="restoration">复原成果</el-radio-button></el-radio-group><el-alert title="源模块数据仅引用，不复制、覆盖或删除。计划与执行记录分别保存。" type="info" :closable="false"/></div>
    <el-form v-else label-position="top" class="step-body">
      <div v-if="planStep===1" class="form-grid"><el-form-item label="计划编号"><el-input v-model="planForm.planCode"/></el-form-item><el-form-item label="计划名称"><el-input v-model="planForm.planName"/></el-form-item><el-form-item label="计划类型"><el-select v-model="planForm.planType"><el-option label="综合监测" value="comprehensive"/><el-option label="专项病害" value="disease"/><el-option label="修复部位" value="repair"/><el-option label="保存环境" value="environment"/></el-select></el-form-item><el-form-item label="负责人"><el-input v-model="planForm.responsiblePerson"/></el-form-item><el-form-item class="wide" label="监测目的"><el-input v-model="planForm.monitoringPurpose" type="textarea"/></el-form-item><el-form-item class="wide" label="监测范围"><el-input v-model="planForm.monitoringScope" type="textarea"/></el-form-item></div>
      <div v-if="planStep===2"><h4>选择重点病害</h4><el-checkbox-group v-model="planForm.selectedDiseaseIds" class="source-list"><el-checkbox v-for="x in sources.diseases" :key="x.id" :label="x.id"><b>{{ x.partName }} · {{ x.diseaseName }}</b><span>{{ x.positionDescription }}（{{ x.severity }}）</span></el-checkbox></el-checkbox-group><h4>选择对比基准</h4><el-checkbox-group v-model="planForm.selectedComparisonIds" class="source-list"><el-checkbox v-for="x in sources.comparisons" :key="x.id" :label="x.id"><b>{{ x.comparisonTitle }}</b><span>{{ x.afterSummary }}</span></el-checkbox></el-checkbox-group><h4>选择实体复原成果</h4><el-checkbox-group v-model="planForm.selectedRestorationIds" class="source-list"><el-checkbox v-for="x in sources.restorations" :key="x.id" :label="x.id"><b>{{ x.resultName }}</b><span>{{ x.monitoring.indicators }}</span></el-checkbox></el-checkbox-group></div>
      <div v-if="planStep===3" class="form-grid"><el-form-item label="默认周期"><div class="pair"><el-input-number v-model="planForm.defaultFrequencyValue" :min="1"/><el-select v-model="planForm.defaultFrequencyUnit"><el-option label="天" value="day"/><el-option label="周" value="week"/><el-option label="月" value="month"/><el-option label="年" value="year"/></el-select></div></el-form-item><el-form-item label="监测地点"><el-input v-model="planForm.monitoringLocation"/></el-form-item><el-form-item label="开始日期"><el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD"/></el-form-item><el-form-item label="预计结束"><el-date-picker v-model="planForm.expectedEndDate" type="date" value-format="YYYY-MM-DD"/></el-form-item><el-form-item class="wide" label="总体策略"><el-input v-model="planForm.overallStrategy" type="textarea" :rows="3"/></el-form-item><el-form-item label="自动生成任务"><el-switch v-model="planForm.autoGenerateTask"/></el-form-item><el-form-item label="开启阈值预警"><el-switch v-model="planForm.alertEnabled"/></el-form-item></div>
      <div v-if="planStep===4" class="review"><el-alert title="基准创建后按版本管理；本页面不修改前后对比或复原成果源影像。" type="warning" :closable="false"/><p>已选病害 {{ planForm.selectedDiseaseIds.length }} 项 · 前后对比基准 {{ planForm.selectedComparisonIds.length }} 组 · 复原成果 {{ planForm.selectedRestorationIds.length }} 项</p><p>计划周期：每 {{ planForm.defaultFrequencyValue }} {{ planForm.defaultFrequencyUnit }}；预警：{{ planForm.alertEnabled?'开启':'关闭' }}</p></div>
    </el-form>
    <template #footer><el-button @click="planDialog=false">取消</el-button><el-button v-if="planStep>0" @click="planStep--">上一步</el-button><el-button v-if="planStep<4" type="primary" @click="planStep++">下一步</el-button><el-button v-else type="primary" @click="createPlan">创建计划草稿</el-button></template>
  </el-dialog>

  <el-dialog v-model="targetDialog" :title="targetForm.id?'编辑监测对象':'新增监测对象'" width="min(700px,95vw)"><el-form label-position="top"><div class="form-grid"><el-form-item label="对象名称"><el-input v-model="targetForm.targetName"/></el-form-item><el-form-item label="对象类型"><el-select v-model="targetForm.targetType"><el-option label="病害" value="disease"/><el-option label="修复部位" value="repair_part"/><el-option label="复原补配" value="restoration_part"/><el-option label="保存环境" value="environment"/><el-option label="文物整体" value="artifact"/></el-select></el-form-item><el-form-item label="目标部位"><el-input v-model="targetForm.targetPart"/></el-form-item><el-form-item label="具体位置"><el-input v-model="targetForm.targetLocation"/></el-form-item><el-form-item label="风险等级"><el-select v-model="targetForm.riskLevel"><el-option label="高" value="high"/><el-option label="中" value="medium"/><el-option label="低" value="low"/></el-select></el-form-item><el-form-item label="固定拍摄机位"><el-input v-model="targetForm.shootingPosition"/></el-form-item><el-form-item class="wide" label="监测原因"><el-input v-model="targetForm.monitoringReason" type="textarea"/></el-form-item><el-form-item label="要求影像"><el-switch v-model="targetForm.requiresImage"/></el-form-item><el-form-item label="启用"><el-switch v-model="targetForm.enabled"/></el-form-item></div></el-form><template #footer><el-button @click="targetDialog=false">取消</el-button><el-button type="primary" @click="saveTarget">保存对象</el-button></template></el-dialog>

  <el-dialog v-model="alertDialog" title="预警确认与处理" width="min(680px,95vw)"><div v-if="handlingAlert" class="alert-detail"><el-alert :title="handlingAlert.alertTitle" :description="handlingAlert.alertDescription" type="warning" :closable="false"/><p>触发条件：{{ handlingAlert.thresholdDescription }}</p></div><el-form label-position="top"><el-form-item label="处理状态"><el-select v-model="alertForm.alertStatus"><el-option label="已确认" value="confirmed"/><el-option label="处理中" value="processing"/><el-option label="已解决" value="resolved"/><el-option label="已关闭" value="closed"/><el-option label="误报" value="false_alarm"/></el-select></el-form-item><el-form-item label="即时措施"><el-input v-model="alertForm.immediateAction" type="textarea"/></el-form-item><el-form-item label="处理建议"><el-input v-model="alertForm.treatmentAdvice" type="textarea"/></el-form-item><div class="checks"><el-checkbox v-model="alertForm.requiresRecheck">需要复测</el-checkbox><el-checkbox v-model="alertForm.requiresDiseaseSurvey">建立病害调查</el-checkbox><el-checkbox v-model="alertForm.requiresIntervention">需要再次干预</el-checkbox><el-checkbox v-model="alertForm.requiresNewProject">建议新项目</el-checkbox></div></el-form><template #footer><el-button v-if="alertForm.requiresDiseaseSurvey" @click="navigate('disease')">前往病害调查</el-button><el-button v-if="alertForm.requiresNewProject" @click="createNewProject(handlingAlert)">{{ handlingAlert?.createdProjectId ? '进入已创建项目' : '创建新保护修复项目' }}</el-button><el-button type="primary" @click="saveAlert">保存处理</el-button></template></el-dialog>

  <el-dialog v-model="projectDialog" title="从监测预警创建保护修复项目" width="min(760px,96vw)" :close-on-click-modal="false">
    <el-alert v-if="handlingAlert" :title="handlingAlert.alertTitle" :description="`来源预警：${handlingAlert.alertCode}；${handlingAlert.alertDescription || ''}`" type="warning" :closable="false"/>
    <el-form label-position="top" class="alert-project-form">
      <div class="form-grid">
        <el-form-item label="文物名称"><el-input :model-value="project.artifactName" disabled/></el-form-item>
        <el-form-item label="文物编号"><el-input :model-value="project.artifactCode || '未填写'" disabled/></el-form-item>
        <el-form-item label="项目编号"><el-input v-model="projectForm.projectCode"/></el-form-item>
        <el-form-item label="项目名称" required><el-input v-model="projectForm.projectName"/></el-form-item>
        <el-form-item label="项目类型"><el-select v-model="projectForm.projectType"><el-option label="保护" value="保护"/><el-option label="修复" value="修复"/><el-option label="复原" value="复原"/><el-option label="综合" value="综合"/></el-select></el-form-item>
        <el-form-item label="风险等级"><el-select v-model="projectForm.riskLevel"><el-option label="高风险" value="high"/><el-option label="中风险" value="medium"/><el-option label="低风险" value="low"/></el-select></el-form-item>
        <el-form-item label="负责人"><el-input v-model="projectForm.principal"/></el-form-item>
        <el-form-item label="执行部门"><el-input v-model="projectForm.department"/></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="projectForm.startDate" type="date" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item label="预计完成"><el-date-picker v-model="projectForm.expectedEndDate" type="date" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item class="wide" label="补充摘要"><el-input v-model="projectForm.summary" type="textarea" :rows="3" placeholder="系统会自动附加来源项目、预警、监测对象、触发阈值和处理建议"/></el-form-item>
      </div>
    </el-form>
    <template #footer><el-button @click="projectDialog=false">取消</el-button><el-button type="primary" :loading="creatingProject" @click="submitAlertProject">创建项目</el-button></template>
  </el-dialog>
</div>
</template>

<style scoped>
.monitoring-page{height:calc(100vh - 118px);min-height:680px;display:flex;flex-direction:column;gap:13px;overflow:hidden}.loading{padding:25px;background:#fff}.layout{display:grid;grid-template-columns:280px minmax(530px,1fr) 235px;gap:10px;min-height:0;flex:1}.mobile-tools{display:none}.step-body{min-height:340px;margin-top:25px;padding:15px}.step-body>.el-alert{margin-top:20px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 18px}.wide{grid-column:1/-1}.source-list{display:grid;grid-template-columns:1fr 1fr;gap:8px}.source-list :deep(.el-checkbox){height:auto;align-items:flex-start;padding:9px;border:1px solid #e2e7e5;margin:0}.source-list b,.source-list span{display:block}.source-list span{font-size:10px;color:#87928e;white-space:normal}.pair{display:flex;gap:8px}.review{padding:25px;background:#f5f8f7}.review p{color:#566862}.checks{display:flex;flex-wrap:wrap;gap:12px}.alert-detail p{font-size:11px;color:#7a6e61}
@media(max-width:1280px){.layout{grid-template-columns:280px minmax(530px,1fr)}.right{display:none}.mobile-tools{display:flex;justify-content:flex-end}.mobile-tools .el-button:first-child{display:none}}
@media(max-width:920px){.monitoring-page{height:auto;min-height:calc(100vh - 110px);overflow:visible}.layout{display:block;min-height:720px}.left{display:none}.mobile-tools{display:flex}.mobile-tools .el-button:first-child{display:inline-flex}.source-list{grid-template-columns:1fr}}
@media(max-width:600px){.form-grid{grid-template-columns:1fr}.wide{grid-column:auto}.monitoring-page{min-height:0}.layout{min-height:650px}}
</style>
