import { createMockArchiveWorkspace } from './archiveMock'
import { createMockComparisonGroups } from './comparisonMock'
import { createMockProcessSteps } from './processMock'
import { createMockRestorations } from './restorationMock'

export const createMonitoringSources = project => {
    const archive = createMockArchiveWorkspace(project)
    return {
        archiveAdvice: archive.advice,
        diseases: archive.diseaseRecords,
        processSteps: createMockProcessSteps(project).filter(item => item.result?.monitoringRequired),
        comparisons: createMockComparisonGroups(project).filter(item => item.selectedAsMonitoringBaseline),
        restorations: createMockRestorations(project).filter(item => item.requiresMonitoring)
    }
}

const indicators = {
    crack: [{ id: 1, indicatorCode: 'CRACK-WIDTH', indicatorName: '裂隙最大宽度', indicatorCategory: 'structure', dataType: 'number', valueUnit: 'mm', baselineValue: 0.4, normalMin: 0, normalMax: 0.5, warningMax: 0.7, criticalMax: 1, changeWarningValue: 0.2, expectedDirection: 'stable', observationMethod: '裂隙宽度测量', instrumentName: '裂隙测宽仪', required: true }],
    joint: [
        { id: 2, indicatorCode: 'JOINT-WIDTH', indicatorName: '补配接缝宽度', indicatorCategory: 'structure', dataType: 'number', valueUnit: 'mm', baselineValue: 0.8, normalMin: 0, normalMax: 0.9, warningMax: 1, criticalMax: 1.3, changeWarningValue: 0.2, expectedDirection: 'stable', observationMethod: '接缝宽度测量', instrumentName: '游标卡尺', required: true },
        { id: 3, indicatorCode: 'COLOR-DIFF', indicatorName: '补配区域色差', indicatorCategory: 'appearance', dataType: 'number', valueUnit: 'ΔE', baselineValue: 2.1, normalMin: 0, normalMax: 3, warningMax: 5, criticalMax: 8, changeWarningValue: 2, expectedDirection: 'stable', observationMethod: '定点色差测量', instrumentName: '色差仪', required: false }
    ],
    environment: [
        { id: 4, indicatorCode: 'TEMPERATURE', indicatorName: '环境温度', indicatorCategory: 'environment', dataType: 'number', valueUnit: '℃', baselineValue: 20, normalMin: 18, normalMax: 25, warningMin: 16, warningMax: 27, criticalMin: 12, criticalMax: 30, expectedDirection: 'range', observationMethod: '温湿度仪读数', instrumentName: '温湿度记录仪', required: true },
        { id: 5, indicatorCode: 'HUMIDITY', indicatorName: '相对湿度', indicatorCategory: 'environment', dataType: 'number', valueUnit: '%RH', baselineValue: 52, normalMin: 45, normalMax: 60, warningMin: 40, warningMax: 65, criticalMin: 35, criticalMax: 70, expectedDirection: 'range', observationMethod: '温湿度仪读数', instrumentName: '温湿度记录仪', required: true }
    ]
}

export const createEmptyMonitoringPlan = (project, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId, archiveId: project.id,
    planCode: `MP-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-3)}`,
    planName: '', planType: 'comprehensive', planStatus: 'draft', monitoringPurpose: '',
    monitoringScope: '', overallStrategy: '', responsiblePerson: project.principal,
    participantNames: '', monitoringLocation: '文物保护实验室', startDate: '', expectedEndDate: '',
    nextMonitoringDate: '', defaultFrequencyValue: 1, defaultFrequencyUnit: 'month',
    autoGenerateTask: true, alertEnabled: true, executionCount: 0, overdueCount: 0,
    openAlertCount: 0, completionRate: 0, targets: [], tasks: [], records: [], alerts: []
})

export const createMockMonitoringPlans = project => [{
    ...createEmptyMonitoringPlan(project, 1),
    id: 1, planCode: 'MP-M45C1-001', planName: 'M45-C1马车本体综合后续监测计划',
    planStatus: 'active', monitoringPurpose: '监测修复后病害控制效果、实体补配稳定性及保存环境',
    monitoringScope: '马车整体、车轮裂隙、补配接缝及保存环境',
    overallStrategy: '重点部位定期检查，量化指标与同角度影像同步记录，异常触发预警。',
    responsiblePerson: '张三', participantNames: '李四、王五', monitoringLocation: '文物保护实验室',
    startDate: '2026-07-25', expectedEndDate: '2028-07-25', nextMonitoringDate: '2026-09-10',
    executionCount: 2, overdueCount: 0, openAlertCount: 1, completionRate: 76,
    targets: [
        {
            id: 1, targetType: 'disease', targetName: '车轮右侧裂隙', sourceBusinessType: 'disease_record',
            sourceBusinessId: 1, targetPart: '右侧车轮', targetLocation: '车轮右侧上部纵向裂隙',
            riskLevel: 'high', priorityLevel: 'high', monitoringReason: '裂隙修复后仍需监测宽度变化和结构稳定性',
            currentStatus: '轻微变化', requiresImage: true, enabled: true, shootingPosition: '右前45°',
            baseline: { id: 1, sourceBusinessType: 'comparison_after', sourceBusinessId: 1, baselineDate: '2026-07-20', baselineStatus: '裂隙灌浆后状态稳定', baselineDescription: '残余可见宽度0.4mm', baselineFileUrl: '/mock/comparison/crack-after.svg', versionNo: 'V1.0', isCurrent: true },
            indicators: indicators.crack
        },
        {
            id: 2, targetType: 'restoration_part', targetName: '右侧车轮实体补配接缝', sourceBusinessType: 'restoration_part',
            sourceBusinessId: 21, targetPart: '右侧车轮', targetLocation: '原件与补配部分连接区域',
            riskLevel: 'medium', priorityLevel: 'medium', monitoringReason: '监测补配接缝、粘接稳定性和色差变化',
            currentStatus: '稳定', requiresImage: true, enabled: true, shootingPosition: '右侧正视',
            baseline: { id: 2, sourceBusinessType: 'restoration', sourceBusinessId: 2, baselineDate: '2026-07-25', baselineStatus: '实体补配完成状态', baselineDescription: '接缝宽度0.8mm，连接稳定', baselineFileUrl: '/mock/restoration-new/wheel.svg', versionNo: 'V1.0', isCurrent: true },
            indicators: indicators.joint
        },
        {
            id: 3, targetType: 'environment', targetName: '保存环境温湿度', sourceBusinessType: 'archive_advice',
            sourceBusinessId: 1, targetPart: '保存环境', targetLocation: '文物保护实验室',
            riskLevel: 'medium', priorityLevel: 'high', monitoringReason: '保证修复材料和文物处于适宜保存环境',
            currentStatus: '稳定', requiresImage: false, enabled: true, shootingPosition: '',
            baseline: { id: 3, sourceBusinessType: 'archive_advice', sourceBusinessId: 1, baselineDate: '2026-07-25', baselineStatus: '推荐保存环境', baselineDescription: '温度18–25℃，相对湿度45%–60%RH', baselineFileUrl: '', versionNo: 'V1.0', isCurrent: true },
            indicators: indicators.environment
        }
    ],
    tasks: [
        { id: 1, taskCode: 'MT-M45C1-202608', taskName: '2026年8月后续监测任务', taskType: 'routine', taskStatus: 'in_progress', plannedDate: '2026-08-25', dueDate: '2026-08-30', actualStartTime: '2026-08-25 09:00:00', actualEndTime: '', responsiblePerson: '张三', participantNames: '李四', targetCount: 3, completedTargetCount: 1, completionRate: 33, overallResult: '', summary: '', generatedAutomatically: true },
        { id: 2, taskCode: 'MT-M45C1-202607', taskName: '2026年7月基准复查任务', taskType: 'routine', taskStatus: 'completed', plannedDate: '2026-07-25', dueDate: '2026-07-28', actualStartTime: '2026-07-25 09:00:00', actualEndTime: '2026-07-25 11:20:00', responsiblePerson: '张三', participantNames: '李四', targetCount: 3, completedTargetCount: 3, completionRate: 100, overallResult: 'normal', summary: '各监测对象状态稳定。', generatedAutomatically: true }
    ],
    records: [
        {
            id: 1, taskId: 1, targetId: 1, recordCode: 'MR-M45C1-001', monitoringDate: '2026-08-25 09:30:00',
            monitorPerson: '张三', monitoringLocation: '文物保护实验室', overallStatus: 'slight_change',
            comparisonResult: 'minor_deviation', observationDescription: '裂隙外观整体稳定，局部宽度略有增加',
            changeDescription: '最大宽度由0.4mm增加至0.6mm', resultConclusion: '存在轻微变化，建议缩短下一次监测周期',
            requiresRecheck: true, requiresIntervention: false, requiresNewDiseaseSurvey: false, requiresNewProject: false,
            nextMonitoringDate: '2026-09-10', monitoringStatus: 'submitted',
            values: [{ id: 1, indicatorId: 1, indicatorName: '裂隙最大宽度', valueNumber: 0.6, valueUnit: 'mm', baselineValue: 0.4, previousValue: 0.4, changeValue: 0.2, changeRate: 50, resultLevel: 'warning', resultDescription: '达到黄色预警变化量', manuallyConfirmed: true }],
            media: [{ id: 1, fileUrl: '/mock/comparison/crack-during.svg', title: '2026-08-25同角度监测影像', shootingPosition: '右前45°', role: 'current' }]
        },
        { id: 2, taskId: 2, targetId: 1, recordCode: 'MR-M45C1-BASE', monitoringDate: '2026-07-25 09:20:00', monitorPerson: '张三', monitoringLocation: '文物保护实验室', overallStatus: 'stable', comparisonResult: 'same', observationDescription: '裂隙状态稳定', changeDescription: '无明显变化', resultConclusion: '保持原监测周期', nextMonitoringDate: '2026-08-25', monitoringStatus: 'submitted', values: [{ id: 2, indicatorId: 1, indicatorName: '裂隙最大宽度', valueNumber: 0.4, valueUnit: 'mm', baselineValue: 0.4, previousValue: 0.4, changeValue: 0, changeRate: 0, resultLevel: 'normal' }], media: [{ id: 2, fileUrl: '/mock/comparison/crack-after.svg', title: '首次复查影像', shootingPosition: '右前45°', role: 'history' }] }
    ],
    alerts: [{
        id: 1, alertCode: 'ALERT-M45C1-001', taskId: 1, recordId: 1, targetId: 1, indicatorId: 1,
        alertLevel: 'warning', alertTitle: '车轮右侧裂隙宽度增加', alertDescription: '裂隙最大宽度由0.4mm增加至0.6mm',
        triggerType: 'change_warning_value', triggerValue: '0.6mm', thresholdDescription: '较基准增加0.2mm时触发黄色预警',
        alertStatus: 'confirmed', discoveredTime: '2026-08-25 09:35:00', confirmedTime: '2026-08-25 10:00:00',
        confirmedBy: '王五', immediateAction: '缩短监测周期并限制周边振动', treatmentAdvice: '两周后复测，如继续增长则重新进行病害调查',
        requiresRecheck: true, requiresDiseaseSurvey: false, requiresIntervention: false, requiresNewProject: false
    }]
}]

export const createDraftRecord = (plan, task, target) => ({
    id: Date.now(), taskId: task?.id, planId: plan.id, targetId: target.id, projectId: plan.projectId,
    recordCode: `MR-${String(Date.now()).slice(-8)}`, monitoringDate: new Date().toLocaleString('zh-CN', { hour12: false }),
    monitorPerson: task?.responsiblePerson || plan.responsiblePerson, monitoringLocation: plan.monitoringLocation,
    overallStatus: '', comparisonResult: '', observationDescription: '', changeDescription: '', resultConclusion: '',
    requiresRecheck: false, requiresIntervention: false, requiresNewDiseaseSurvey: false, requiresNewProject: false,
    nextMonitoringDate: plan.nextMonitoringDate, monitoringStatus: 'draft',
    values: target.indicators.map(item => ({ id: Date.now() + item.id, indicatorId: item.id, indicatorName: item.indicatorName, valueNumber: null, valueUnit: item.valueUnit, baselineValue: Number(item.baselineValue), previousValue: Number(item.baselineValue), changeValue: null, changeRate: null, resultLevel: 'pending', resultDescription: '', manuallyConfirmed: false })),
    media: []
})

export const calculateIndicatorLevel = (indicator, value, baseline = Number(indicator.baselineValue)) => {
    const number = Number(value)
    if (Number.isNaN(number)) return { level: 'pending', description: '待填写' }
    if ((indicator.criticalMax != null && number >= indicator.criticalMax) || (indicator.criticalMin != null && number <= indicator.criticalMin)) return { level: 'critical', description: '达到红色预警阈值' }
    if ((indicator.warningMax != null && number >= indicator.warningMax) || (indicator.warningMin != null && number <= indicator.warningMin) || (indicator.changeWarningValue != null && Math.abs(number - baseline) >= indicator.changeWarningValue)) return { level: 'warning', description: '达到黄色预警阈值' }
    if ((indicator.normalMax != null && number > indicator.normalMax) || (indicator.normalMin != null && number < indicator.normalMin)) return { level: 'attention', description: '偏离正常范围' }
    return { level: 'normal', description: '处于正常范围' }
}
