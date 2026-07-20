export const createEmptyMonitoringPlan = (project, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId, archiveId: project.id,
    planCode: `MP-${String(project.artifactCode || project.id).replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-3)}`,
    planName: '', planType: 'comprehensive', planStatus: 'draft', monitoringPurpose: '',
    monitoringScope: '', overallStrategy: '', responsiblePerson: project.principal,
    participantNames: '', monitoringLocation: '文物保护实验室', startDate: '', expectedEndDate: '',
    nextMonitoringDate: '', defaultFrequencyValue: 1, defaultFrequencyUnit: 'month',
    autoGenerateTask: true, alertEnabled: true, executionCount: 0, overdueCount: 0,
    openAlertCount: 0, completionRate: 0, targets: [], tasks: [], records: [], alerts: []
})

export const createDraftRecord = (plan, task, target) => ({
    id: Date.now(), taskId: task?.id, planId: plan.id, targetId: target.id, projectId: plan.projectId,
    recordCode: `MR-${String(Date.now()).slice(-8)}`,
    monitoringDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
    monitorPerson: task?.responsiblePerson || plan.responsiblePerson, monitoringLocation: plan.monitoringLocation,
    overallStatus: '', comparisonResult: '', observationDescription: '', changeDescription: '', resultConclusion: '',
    requiresRecheck: false, requiresIntervention: false, requiresNewDiseaseSurvey: false, requiresNewProject: false,
    nextMonitoringDate: plan.nextMonitoringDate, monitoringStatus: 'draft',
    values: target.indicators.map(item => ({
        id: Date.now() + Number(item.id), indicatorId: item.id, indicatorName: item.indicatorName,
        valueNumber: null, valueUnit: item.valueUnit, baselineValue: Number(item.baselineValue),
        previousValue: Number(item.baselineValue), changeValue: null, changeRate: null,
        resultLevel: 'pending', resultDescription: '', manuallyConfirmed: false
    })),
    media: []
})

export const calculateIndicatorLevel = (indicator, value, baseline = Number(indicator.baselineValue)) => {
    const number = Number(value)
    if (value == null || value === '' || Number.isNaN(number)) return { level: 'pending', description: '待填写' }
    if ((indicator.criticalMax != null && number >= indicator.criticalMax)
        || (indicator.criticalMin != null && number <= indicator.criticalMin)) {
        return { level: 'critical', description: '达到红色预警阈值' }
    }
    if ((indicator.warningMax != null && number >= indicator.warningMax)
        || (indicator.warningMin != null && number <= indicator.warningMin)
        || (indicator.changeWarningValue != null && Math.abs(number - baseline) >= indicator.changeWarningValue)) {
        return { level: 'warning', description: '达到黄色预警阈值' }
    }
    if ((indicator.normalMax != null && number > indicator.normalMax)
        || (indicator.normalMin != null && number < indicator.normalMin)) {
        return { level: 'attention', description: '偏离正常范围' }
    }
    return { level: 'normal', description: '处于正常范围' }
}
