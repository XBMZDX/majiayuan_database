import { createMockArchive, createMockArchiveWorkspace } from './archiveMock'

const nowText = () => new Date().toLocaleString('zh-CN', { hour12: false })

const baseStep = (data) => ({
    id: data.id,
    processId: 1,
    planMeasureId: data.planMeasureId || data.id,
    parentStepId: null,
    stepCode: `STEP-${String(data.sequenceNo).padStart(2, '0')}`,
    stepName: data.stepName,
    stepType: data.stepType,
    stepStatus: data.stepStatus || 'pending',
    sequenceNo: data.sequenceNo,
    progressWeight: data.progressWeight || 10,
    plannedStartTime: data.plannedStartTime || '',
    plannedEndTime: data.plannedEndTime || '',
    actualStartTime: data.actualStartTime || '',
    actualEndTime: data.actualEndTime || '',
    operatorName: data.operatorName || '',
    assistantNames: data.assistantNames || '',
    operationLocation: data.operationLocation || '文物保护实验室',
    targetPart: data.targetPart || '',
    plannedMethod: data.plannedMethod || '',
    actualMethod: data.actualMethod || '',
    operationDescription: data.operationDescription || '',
    resultDescription: data.resultDescription || '',
    deviationFlag: data.deviationFlag || false,
    deviationLevel: data.deviationLevel || 'minor',
    deviationReason: data.deviationReason || '',
    adjustmentDescription: data.adjustmentDescription || '',
    requiresMedia: data.requiresMedia ?? true,
    requiresQualityCheck: data.requiresQualityCheck ?? true,
    requiresMonitoring: data.requiresMonitoring || false,
    generateRestorationResult: data.generateRestorationResult || false,
    qualityStatus: data.qualityStatus || '',
    completionRate: data.completionRate || 0,
    temporaryStep: data.temporaryStep || false,
    temporaryReason: data.temporaryReason || '',
    nonDiseaseStep: data.nonDiseaseStep || false,
    noMaterialRequired: data.noMaterialRequired || false,
    planMaterials: data.planMaterials || [],
    relatedDiseases: data.relatedDiseases || [],
    operationLogs: data.operationLogs || [],
    materials: data.materials || [],
    tools: data.tools || [],
    processParameters: data.processParameters || [],
    environments: data.environments || [],
    media: data.media || [],
    issues: data.issues || [],
    qualityChecks: data.qualityChecks || [],
    result: data.result || {
        targetCompleted: '', diseaseTreatmentEffect: '', artifactStateChange: '',
        actualResult: '', expectedReached: '', sideEffects: '', remainingProblems: '',
        allowNextStep: true, monitoringRequired: false, monitoringObject: '',
        monitoringIndicators: '', monitoringCycle: '', warningConditions: '',
        monitoringAdvice: '', finalConclusion: ''
    }
})

const completeEvidence = (name, disease = []) => ({
    operationLogs: [{ id: Date.now() + Math.random(), recordTime: '2026-07-12 10:00:00', operatorName: '张三', operationAction: `完成${name}`, observation: '过程正常', parameterNote: '按方案执行', resultNote: '达到阶段目标', locked: true }],
    materials: [],
    tools: [{ id: Date.now() + Math.random(), toolName: '基础操作工具', model: '-', serialNumber: '-', purpose: name, plannedParameter: '按方案', actualParameter: '按方案', targetPart: '目标区域', operatorNote: '使用正常', safetyNote: '已落实安全要求' }],
    processParameters: [{ id: Date.now() + Math.random(), parameterName: '操作顺序', plannedValue: '按方案', actualValue: '按方案', unit: '', description: '无偏差' }],
    environments: [{ id: Date.now() + Math.random(), recordTime: '2026-07-12 09:30:00', temperature: 20.2, humidity: 49, illumination: 180, ventilationStatus: '正常', environmentStatus: 'qualified', recorder: '李四', description: '符合要求' }],
    media: [
        { id: Date.now() + Math.random(), mediaStage: 'before', fileName: `${name}-操作前.jpg`, title: '操作前', shootingTime: '2026-07-12 09:30:00', shootingPosition: '正面', targetPart: '目标区域', photographer: '李四', description: '操作前记录', selectedForComparison: true, selectedForArchive: true, selectedForRestoration: false },
        { id: Date.now() + Math.random(), mediaStage: 'during', fileName: `${name}-操作中.jpg`, title: '操作中', shootingTime: '2026-07-12 10:00:00', shootingPosition: '正面', targetPart: '目标区域', photographer: '李四', description: '操作过程记录', selectedForComparison: false, selectedForArchive: true, selectedForRestoration: false },
        { id: Date.now() + Math.random(), mediaStage: 'after', fileName: `${name}-操作后.jpg`, title: '操作后', shootingTime: '2026-07-12 11:00:00', shootingPosition: '正面', targetPart: '目标区域', photographer: '李四', description: '操作后记录', selectedForComparison: true, selectedForArchive: true, selectedForRestoration: false }
    ],
    issues: [],
    qualityChecks: [{ id: Date.now() + Math.random(), checkType: 'process', checkItem: '阶段目标完成情况', expectedStandard: '达到方案要求', actualResult: '已达到', checkResult: 'passed', checker: '王五', checkTime: '2026-07-12 11:20:00', requiresRework: false, reworkDescription: '' }],
    relatedDiseases: disease,
    result: {
        targetCompleted: 'yes', diseaseTreatmentEffect: '阶段目标已达到', artifactStateChange: '状态稳定',
        actualResult: `${name}完成，过程记录齐全。`, expectedReached: 'yes', sideEffects: '未发现',
        remainingProblems: '', allowNextStep: true, monitoringRequired: false,
        monitoringObject: '', monitoringIndicators: '', monitoringCycle: '',
        warningConditions: '', monitoringAdvice: '', finalConclusion: '本步骤质量检查通过，可以进入下一步骤。'
    }
})

const currentDiseases = [
    { id: 1, diseaseRecordId: 1, diseaseName: '裂隙', severity: 'severe', developmentStatus: '快速发展', partName: '车轮右侧', treatmentStatus: 'in_treatment', treatmentResult: '', resultDescription: '', monitoringRequired: true },
    { id: 2, diseaseRecordId: 3, diseaseName: '局部结构失稳', severity: 'critical', developmentStatus: '快速发展', partName: '车体基座', treatmentStatus: 'in_treatment', treatmentResult: '', resultDescription: '', monitoringRequired: true }
]

export const createMockProcessContext = (project) => {
    const archive = createMockArchive(project)
    archive.archiveStatus = 'submitted'
    const archiveWorkspace = createMockArchiveWorkspace(project)
    archiveWorkspace.plan.planStatus = 'approved'
    archiveWorkspace.plan.reviewer = '王五'
    return { archive, archiveWorkspace, formalPlan: archiveWorkspace.plan }
}

export const createMockProcess = (project) => ({
    id: 1, projectId: project.id, archiveId: 1, planId: 1,
    processCode: `CP-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-001`,
    processName: `${project.artifactCode}${project.artifactName}保护修复执行过程`,
    processStatus: 'in_progress', executionMode: 'formal', supervisor: '张三',
    startDate: '2026-07-10', expectedEndDate: '2026-12-31', actualEndDate: '',
    totalSteps: 8, completedSteps: 3, progress: 0, executionSummary: '', finalResult: '',
    pauseRecord: null, updateTime: '2026-07-17 10:30:00'
})

export const createMockProcessSteps = (project) => {
    const diseaseCrack = [{ id: 1, diseaseRecordId: 1, diseaseName: '裂隙', severity: 'severe', developmentStatus: '快速发展', partName: '车轮右侧', treatmentStatus: 'controlled', monitoringRequired: true }]
    return [
        baseStep({
            id: 1, sequenceNo: 1, stepName: '建立修复过程档案', stepType: 'documentation',
            stepStatus: 'completed', progressWeight: 5, operatorName: '张三',
            plannedMethod: '建立过程记录并确认正式方案', actualMethod: '完成过程档案建立和方案确认',
            actualStartTime: '2026-07-10 09:00:00', actualEndTime: '2026-07-10 11:00:00',
            targetPart: '全过程', nonDiseaseStep: true, noMaterialRequired: true, requiresMedia: false,
            completionRate: 100, ...completeEvidence('建立过程档案')
        }),
        baseStep({
            id: 2, sequenceNo: 2, stepName: '表面浮尘及松散附着物清理', stepType: 'cleaning',
            stepStatus: 'completed', progressWeight: 10, operatorName: '李四',
            plannedMethod: '使用软毛刷和竹刀进行表面清理', actualMethod: '使用软毛刷、竹刀分区清理',
            actualStartTime: '2026-07-11 09:20:00', actualEndTime: '2026-07-11 16:30:00',
            targetPart: '车厢及车轮表面', noMaterialRequired: true, completionRate: 100,
            ...completeEvidence('表面清理', diseaseCrack)
        }),
        baseStep({
            id: 3, sequenceNo: 3, stepName: '局部临时稳定加固', stepType: 'stabilization',
            stepStatus: 'completed', progressWeight: 10, operatorName: '张三',
            plannedMethod: '使用支撑材料完成局部临时稳定', actualMethod: '按方案完成车轮及基座临时支撑',
            actualStartTime: '2026-07-13 09:00:00', actualEndTime: '2026-07-13 17:00:00',
            targetPart: '车轮和基座', noMaterialRequired: true, completionRate: 100,
            ...completeEvidence('临时稳定加固', currentDiseases)
        }),
        baseStep({
            id: 4, sequenceNo: 4, stepName: '裂隙灌浆加固', stepType: 'grouting',
            stepStatus: 'in_progress', progressWeight: 20, operatorName: '张三', assistantNames: '李四',
            plannedStartTime: '2026-07-17 09:00:00', plannedEndTime: '2026-07-18 17:00:00',
            actualStartTime: '2026-07-17 09:30:00', operationLocation: '文物保护实验室A区',
            targetPart: '车轮右侧及车体基座', plannedMethod: '采用低压灌浆方式填充结构裂隙',
            actualMethod: '采用低压分次灌浆，先对裂隙口进行预润湿后施加注浆料',
            operationDescription: '按区域由下至上实施灌浆，持续观察浆液渗入和裂隙变化。',
            completionRate: 72, relatedDiseases: currentDiseases,
            planMaterials: [{ id: 1, materialName: '天然水硬性石灰微收缩注浆料', plannedConcentration: '', plannedMixRatio: '细土与白灰1:1', targetPart: '车轮右侧裂隙', purpose: '裂隙填充和结构加固' }],
            operationLogs: [
                { id: 1, recordTime: '2026-07-17 09:30:00', operatorName: '张三', operationAction: '开始清理裂隙口周边松散土体', observation: '裂隙边缘局部酥松', parameterNote: '', resultNote: '完成灌浆前准备', locked: false },
                { id: 2, recordTime: '2026-07-17 10:10:00', operatorName: '张三', operationAction: '进行第一轮低压灌浆', observation: '浆液渗入状态正常', parameterNote: '注入量8ml', resultNote: '裂隙表层完成初步填充', locked: false }
            ],
            materials: [{
                id: 1, planMaterialId: 1, materialName: '天然水硬性石灰微收缩注浆料', manufacturer: '文保材料实验室',
                batchNumber: '202607-A01', plannedConcentration: '', actualConcentration: '',
                plannedMixRatio: '细土与白灰1:1', actualMixRatio: '细土与白灰1:1',
                actualAmount: 28.5, amountUnit: 'ml', applicationMethod: '低压灌浆', applicationCount: 2,
                dryingTime: '24h', targetPart: '车轮右侧裂隙', usagePurpose: '裂隙填充和结构加固',
                deviationFlag: false, deviationReason: '', safetyNote: '佩戴防尘口罩和护目镜', resultNote: '渗入情况正常'
            }],
            tools: [{
                id: 1, toolName: '低压注浆器', model: 'LP-20', serialNumber: 'LP20-006',
                purpose: '裂隙灌浆', plannedParameter: '压力≤0.05MPa', actualParameter: '0.03MPa',
                targetPart: '车轮右侧裂隙', operatorNote: '压力稳定', safetyNote: '持续观察压力及浆液回流'
            }],
            processParameters: [
                { id: 1, parameterName: '注浆压力', plannedValue: '≤0.05', actualValue: '0.03', unit: 'MPa', description: '符合方案要求' },
                { id: 2, parameterName: '施加次数', plannedValue: '2', actualValue: '2', unit: '次', description: '' },
                { id: 3, parameterName: '干燥时间', plannedValue: '24', actualValue: '24', unit: 'h', description: '' }
            ],
            environments: [{
                id: 1, recordTime: '2026-07-17 09:20:00', temperature: 19.6, humidity: 48.2,
                illumination: 210, ventilationStatus: '正常', environmentStatus: 'qualified',
                recorder: '李四', description: '环境满足操作要求'
            }],
            media: [
                { id: 1, mediaStage: 'before', fileName: '裂隙灌浆-操作前-01.jpg', title: '右侧车轮裂隙操作前', shootingTime: '2026-07-17 09:15:00', shootingPosition: '右前45°', targetPart: '车轮右侧', photographer: '李四', description: '操作前裂隙状态', selectedForComparison: true, selectedForArchive: true, selectedForRestoration: false },
                { id: 2, mediaStage: 'during', fileName: '裂隙灌浆-操作中-01.jpg', title: '第一轮低压灌浆', shootingTime: '2026-07-17 10:12:00', shootingPosition: '右侧近景', targetPart: '车轮右侧', photographer: '李四', description: '第一轮注浆过程', selectedForComparison: false, selectedForArchive: true, selectedForRestoration: false }
            ],
            issues: [{
                id: 1, issueType: 'artifact_state', severity: 'medium', issueTitle: '局部裂隙吸浆速度较慢',
                issueDescription: '车轮右侧上部裂隙局部吸浆速度明显低于其他区域',
                discoveredTime: '2026-07-17 10:35:00', discoveredBy: '张三',
                immediateAction: '暂停该区域灌浆，继续观察', solution: '', issueStatus: 'processing',
                requiresPlanChange: false, requiresNewDiseaseRecord: false, requiresRework: false
            }],
            qualityChecks: [{
                id: 1, checkType: 'process', checkItem: '灌浆材料是否充分进入裂隙',
                expectedStandard: '裂隙表面无明显空鼓，浆液进入深部',
                actualResult: '部分区域已达到要求，局部仍需补充', checkResult: 'conditional',
                checker: '王五', checkTime: '2026-07-17 14:30:00', requiresRework: false, reworkDescription: ''
            }]
        }),
        baseStep({
            id: 5, sequenceNo: 5, stepName: '局部锚固', stepType: 'anchoring', progressWeight: 20,
            plannedStartTime: '2026-07-20 09:00:00', plannedEndTime: '2026-07-21 17:00:00',
            targetPart: '车轮和基座', plannedMethod: '对局部结构薄弱部位实施锚固加固',
            planMaterials: [{ id: 2, materialName: '锚固材料', plannedMixRatio: '按小样试验', targetPart: '结构薄弱部位', purpose: '局部锚固' }],
            relatedDiseases: currentDiseases
        }),
        baseStep({
            id: 6, sequenceNo: 6, stepName: '表面渗透加固', stepType: 'consolidation', progressWeight: 15,
            targetPart: '车厢风化表面', plannedMethod: '低浓度、少量多次实施表面渗透加固',
            relatedDiseases: [{ id: 3, diseaseRecordId: 2, diseaseName: '风化', severity: 'moderate', developmentStatus: '缓慢发展', partName: '车厢表面', treatmentStatus: 'untreated', monitoringRequired: true }]
        }),
        baseStep({
            id: 7, sequenceNo: 7, stepName: '金属连接件稳定处理', stepType: 'rust_removal', progressWeight: 10,
            targetPart: '金属连接件', plannedMethod: '清理疏松腐蚀产物并实施稳定处理',
            relatedDiseases: [{ id: 4, diseaseRecordId: 4, diseaseName: '锈蚀', severity: 'minor', developmentStatus: '稳定', partName: '金属连接件', treatmentStatus: 'untreated', monitoringRequired: true }]
        }),
        baseStep({
            id: 8, sequenceNo: 8, stepName: '整体效果检查与阶段评估', stepType: 'evaluation', progressWeight: 10,
            targetPart: '整体', plannedMethod: '完成结构、外观、色差和表面强度检查',
            nonDiseaseStep: true, noMaterialRequired: true, generateRestorationResult: true
        })
    ]
}

export const createEmptyFormalSteps = (project) => {
    const context = createMockProcessContext(project)
    return context.archiveWorkspace.planDiseaseList.map((measure, index) => baseStep({
        id: Date.now() + index,
        sequenceNo: index + 1,
        stepName: measure.treatmentStrategy.split('、')[0] || `方案措施${index + 1}`,
        stepType: index === 0 ? 'stabilization' : 'consolidation',
        progressWeight: 10,
        plannedMethod: measure.treatmentStrategy,
        targetPart: measure.diseaseName,
        operatorName: '',
        relatedDiseases: context.archiveWorkspace.diseaseRecords
            .filter(item => (measure.diseaseRecordIds || [measure.diseaseRecordId]).includes(item.id))
            .map(item => ({
                id: item.id, diseaseRecordId: item.id, diseaseName: item.diseaseName,
                severity: item.severity, developmentStatus: item.developmentStatus,
                partName: item.partName, treatmentStatus: 'untreated', monitoringRequired: false
            })),
        planMaterials: context.archiveWorkspace.planMaterials.map(item => ({ ...item }))
    }))
}

export const createProcessRecordFromPlan = project => ({
    id: Date.now(), projectId: project.id, archiveId: project.id, planId: 1,
    processCode: `CP-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-001`,
    processName: `${project.artifactCode}${project.artifactName}保护修复执行过程`,
    processStatus: 'not_started', executionMode: 'formal', supervisor: project.principal,
    startDate: '', expectedEndDate: project.expectedEndDate, actualEndDate: '',
    totalSteps: 0, completedSteps: 0, progress: 0, executionSummary: '',
    finalResult: '', pauseRecord: null, updateTime: nowText()
})
