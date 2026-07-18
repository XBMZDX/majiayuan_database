import { createMockArchiveWorkspace } from './archiveMock'
import { createMockProcess, createMockProcessSteps } from './processMock'

const imageUrlMap = {
    '4-before': '/mock/comparison/crack-before.svg',
    '4-during': '/mock/comparison/crack-during.svg',
    '4-after': '/mock/comparison/crack-after.svg',
    '6-before': '/mock/comparison/weather-before.svg',
    '6-during': '/mock/comparison/weather-before.svg',
    '6-after': '/mock/comparison/weather-after.svg'
}

const normalizeSourceMedia = (step, media, index) => ({
    id: step.id * 100 + index + 1,
    sourceMediaId: media.id,
    processId: 1,
    stepId: step.id,
    stepName: step.stepName,
    stepStatus: step.stepStatus,
    imageStage: media.mediaStage,
    imageRole: media.imageRole || 'detail',
    fileName: media.fileName,
    fileUrl: imageUrlMap[`${step.id}-${media.mediaStage}`] || (
        media.mediaStage === 'after'
            ? '/mock/comparison/crack-after.svg'
            : media.mediaStage === 'during'
                ? '/mock/comparison/crack-during.svg'
                : '/mock/comparison/crack-before.svg'
    ),
    thumbnailUrl: imageUrlMap[`${step.id}-${media.mediaStage}`] || '/mock/comparison/crack-before.svg',
    targetPart: media.targetPart || step.targetPart,
    shootingPosition: media.shootingPosition || '正面',
    shootingTime: media.shootingTime,
    photographer: media.photographer || step.operatorName,
    imageDescription: media.description,
    selectedForComparison: Boolean(media.selectedForComparison),
    sourceModule: '修复过程记录',
    projectId: step.projectId || 1,
    artifactId: 101
})

export const createComparisonContext = project => {
    const process = createMockProcess(project)
    const steps = createMockProcessSteps(project)
    const workspace = createMockArchiveWorkspace(project)
    const sourceMedia = steps.flatMap(step =>
        step.media.map((media, index) => normalizeSourceMedia(step, media, index))
    )

    sourceMedia.push(
        {
            id: 499, sourceMediaId: 108, processId: process.id, stepId: 4, stepName: '裂隙灌浆加固',
            stepStatus: 'completed', imageStage: 'after', imageRole: 'detail',
            fileName: '裂隙灌浆-操作后-01.jpg', fileUrl: '/mock/comparison/crack-after.svg',
            thumbnailUrl: '/mock/comparison/crack-after.svg', targetPart: '车轮右侧',
            shootingPosition: '右前45°', shootingTime: '2026-07-19 15:20:00', photographer: '李四',
            imageDescription: '裂隙灌浆完成后的局部状态', selectedForComparison: true,
            sourceModule: '修复过程记录', projectId: project.id, artifactId: project.artifactId
        },
        {
            id: 601, sourceMediaId: 201, processId: process.id, stepId: 6, stepName: '表面渗透加固',
            stepStatus: 'completed', imageStage: 'before', imageRole: 'detail',
            fileName: '车厢风化-操作前.jpg', fileUrl: '/mock/comparison/weather-before.svg',
            thumbnailUrl: '/mock/comparison/weather-before.svg', targetPart: '车厢表面',
            shootingPosition: '左侧', shootingTime: '2026-07-21 09:10:00', photographer: '赵宁',
            imageDescription: '车厢表面加固前粉化状态', selectedForComparison: true,
            sourceModule: '修复过程记录', projectId: project.id, artifactId: project.artifactId
        },
        {
            id: 602, sourceMediaId: 205, processId: process.id, stepId: 6, stepName: '表面渗透加固',
            stepStatus: 'completed', imageStage: 'after', imageRole: 'detail',
            fileName: '车厢风化-操作后.jpg', fileUrl: '/mock/comparison/weather-after.svg',
            thumbnailUrl: '/mock/comparison/weather-after.svg', targetPart: '车厢表面',
            shootingPosition: '左前30°', shootingTime: '2026-07-23 16:40:00', photographer: '赵宁',
            imageDescription: '车厢表面加固后状态', selectedForComparison: true,
            sourceModule: '修复过程记录', projectId: project.id, artifactId: project.artifactId
        }
    )

    return {
        process,
        steps,
        diseases: workspace.diseaseRecords,
        sourceMedia
    }
}

const baseEvaluation = () => ({
    diseaseControlScore: 0,
    structuralStabilityScore: 0,
    appearanceScore: 0,
    materialCompatibilityScore: 0,
    informationPreservationScore: 0,
    distinguishabilityScore: 0,
    overallScore: 0,
    diseaseControlComment: '',
    structuralComment: '',
    appearanceComment: '',
    compatibilityComment: '',
    informationPreservationComment: '',
    sideEffectComment: '',
    remainingIssue: '',
    finalConclusion: '',
    evaluator: '',
    evaluationDate: ''
})

export const createMockComparisonGroups = project => [
    {
        id: 1, projectId: project.id, artifactId: project.artifactId, processId: 1, stepId: 4,
        comparisonCode: 'CMP-M45C1-001', comparisonTitle: '车轮右侧裂隙灌浆前后对比',
        comparisonType: 'before_after', targetPart: '车轮右侧', shootingPosition: '右前45°',
        beforeSummary: '修复前存在纵向裂隙，裂隙边缘局部酥松，最大宽度3.2mm。',
        afterSummary: '灌浆后裂隙得到填充，表面状态趋于稳定，未发现继续发展。',
        comparisonDescription: '对比低压分次灌浆前后的裂隙宽度、结构和表面状态。',
        overallEffect: 'good', evaluationStatus: 'completed', overallComparison: false,
        selectedForArchive: true, selectedForRestoration: false, selectedAsMonitoringBaseline: true,
        evaluator: '王五', evaluationDate: '2026-07-20', updateTime: '2026-07-20 16:30:00',
        noApplicableMetrics: false,
        images: [
            {
                id: 1, sourceMediaId: 401, imageStage: 'before', imageRole: 'detail',
                fileName: '裂隙灌浆-操作前-01.jpg', fileUrl: '/mock/comparison/crack-before.svg',
                thumbnailUrl: '/mock/comparison/crack-before.svg', targetPart: '车轮右侧',
                shootingPosition: '右前45°', shootingTime: '2026-07-17 09:15:00',
                photographer: '李四', imageDescription: '裂隙灌浆前局部状态', sequenceNo: 1,
                isPrimary: true, manuallyUploaded: false, stepName: '裂隙灌浆加固', sourceModule: '修复过程记录'
            },
            {
                id: 2, sourceMediaId: 402, imageStage: 'during', imageRole: 'detail',
                fileName: '裂隙灌浆-操作中-01.jpg', fileUrl: '/mock/comparison/crack-during.svg',
                thumbnailUrl: '/mock/comparison/crack-during.svg', targetPart: '车轮右侧',
                shootingPosition: '右前45°', shootingTime: '2026-07-17 10:12:00',
                photographer: '李四', imageDescription: '第一轮低压灌浆过程', sequenceNo: 2,
                isPrimary: false, manuallyUploaded: false, stepName: '裂隙灌浆加固', sourceModule: '修复过程记录'
            },
            {
                id: 3, sourceMediaId: 499, imageStage: 'after', imageRole: 'detail',
                fileName: '裂隙灌浆-操作后-01.jpg', fileUrl: '/mock/comparison/crack-after.svg',
                thumbnailUrl: '/mock/comparison/crack-after.svg', targetPart: '车轮右侧',
                shootingPosition: '右前45°', shootingTime: '2026-07-19 15:20:00',
                photographer: '李四', imageDescription: '裂隙灌浆完成后的局部状态', sequenceNo: 3,
                isPrimary: true, manuallyUploaded: false, stepName: '裂隙灌浆加固', sourceModule: '修复过程记录'
            }
        ],
        diseases: [{
            id: 1, diseaseRecordId: 1, diseaseName: '裂隙', originalSeverity: 'severe',
            developmentStatus: '发展中', beforeStatus: '严重、持续发展',
            afterStatus: '裂隙已填充，暂未发现继续发展', treatmentEffect: 'controlled',
            effectDescription: '裂隙得到控制，局部结构稳定性提高', monitoringRequired: true
        }],
        metrics: [
            {
                id: 1, metricName: '裂隙最大宽度', metricCategory: 'structure',
                beforeValue: 3.2, afterValue: 0.4, valueUnit: 'mm',
                expectedDirection: 'decrease', resultStatus: 'improved', description: '裂隙宽度明显降低'
            },
            {
                id: 2, metricName: '局部表面硬度', metricCategory: 'strength',
                beforeValue: 42, afterValue: 68, valueUnit: 'HA',
                expectedDirection: 'increase', resultStatus: 'improved', description: '表面强度有所提高'
            }
        ],
        evaluation: {
            diseaseControlScore: 4.5, structuralStabilityScore: 4.2, appearanceScore: 4,
            materialCompatibilityScore: 4.1, informationPreservationScore: 4.6, distinguishabilityScore: 4,
            overallScore: 4.23, diseaseControlComment: '主要裂隙得到有效控制',
            structuralComment: '局部结构稳定性明显提高', appearanceComment: '处理区域与周围表面基本协调',
            compatibilityComment: '暂未发现明显材料不兼容现象',
            informationPreservationComment: '原始表面及裂隙形态信息得到保留',
            sideEffectComment: '局部颜色略有加深，需要持续观察',
            remainingIssue: '应继续监测裂隙宽度和颜色变化',
            finalConclusion: '本次裂隙灌浆加固基本达到预期目标',
            evaluator: '王五', evaluationDate: '2026-07-20'
        },
        monitoring: {
            unresolvedDiseases: '局部颜色变化仍需观察', stabilityConcern: '注浆材料长期稳定性',
            reviewPart: '车轮右侧裂隙', indicators: '裂隙宽度、表面色差',
            cycle: '30天', warningConditions: '裂隙宽度增加超过0.2mm',
            requiresRework: false, notes: '复查时使用相同机位和光照条件。'
        }
    },
    {
        id: 2, projectId: project.id, artifactId: project.artifactId, processId: 1, stepId: 6,
        comparisonCode: 'CMP-M45C1-002', comparisonTitle: '车厢表面风化加固前后对比',
        comparisonType: 'before_after', targetPart: '车厢表面', shootingPosition: '左侧',
        beforeSummary: '表面局部粉化，颗粒胶结能力较弱。',
        afterSummary: '', comparisonDescription: '', overallEffect: '', evaluationStatus: 'draft',
        overallComparison: false, selectedForArchive: false, selectedForRestoration: false,
        selectedAsMonitoringBaseline: false, evaluator: '', evaluationDate: '',
        updateTime: '2026-07-23 17:10:00', noApplicableMetrics: false,
        images: [
            {
                id: 4, sourceMediaId: 601, imageStage: 'before', imageRole: 'detail',
                fileName: '车厢风化-操作前.jpg', fileUrl: '/mock/comparison/weather-before.svg',
                thumbnailUrl: '/mock/comparison/weather-before.svg', targetPart: '车厢表面',
                shootingPosition: '左侧', shootingTime: '2026-07-21 09:10:00', photographer: '赵宁',
                imageDescription: '车厢表面加固前粉化状态', sequenceNo: 1, isPrimary: true,
                manuallyUploaded: false, stepName: '表面渗透加固', sourceModule: '修复过程记录'
            },
            {
                id: 5, sourceMediaId: 602, imageStage: 'after', imageRole: 'detail',
                fileName: '车厢风化-操作后.jpg', fileUrl: '/mock/comparison/weather-after.svg',
                thumbnailUrl: '/mock/comparison/weather-after.svg', targetPart: '车厢表面',
                shootingPosition: '左前30°', shootingTime: '2026-07-23 16:40:00', photographer: '赵宁',
                imageDescription: '车厢表面加固后状态', sequenceNo: 2, isPrimary: true,
                manuallyUploaded: false, stepName: '表面渗透加固', sourceModule: '修复过程记录'
            }
        ],
        diseases: [{
            id: 2, diseaseRecordId: 2, diseaseName: '风化', originalSeverity: 'moderate',
            developmentStatus: '缓慢发展', beforeStatus: '表层颗粒松散',
            afterStatus: '', treatmentEffect: '', effectDescription: '', monitoringRequired: true
        }],
        metrics: [], evaluation: baseEvaluation(),
        monitoring: {
            unresolvedDiseases: '', stabilityConcern: '', reviewPart: '车厢表面',
            indicators: '', cycle: '', warningConditions: '', requiresRework: false, notes: ''
        }
    }
]

export const createEmptyComparison = (project, context, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId,
    processId: context.process.id, stepId: null,
    comparisonCode: `CMP-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-3)}`,
    comparisonTitle: '', comparisonType: 'before_after', targetPart: '', shootingPosition: '',
    beforeSummary: '', afterSummary: '', comparisonDescription: '', overallEffect: '',
    evaluationStatus: 'draft', overallComparison: false, selectedForArchive: false,
    selectedForRestoration: false, selectedAsMonitoringBaseline: false, evaluator: '',
    evaluationDate: '', updateTime: '', noApplicableMetrics: false,
    images: [], diseases: [], metrics: [], evaluation: baseEvaluation(),
    monitoring: {
        unresolvedDiseases: '', stabilityConcern: '', reviewPart: '', indicators: '',
        cycle: '', warningConditions: '', requiresRework: false, notes: ''
    }
})
