import { createMockArchive, createMockArchiveWorkspace } from './archiveMock'
import { createMockProcess, createMockProcessSteps } from './processMock'
import { createMockComparisonGroups } from './comparisonMock'

const emptyEvaluation = () => ({
    evidenceScore: 0, structuralScore: 0, authenticityScore: 0, informationScore: 0,
    distinguishabilityScore: 0, reversibilityScore: 0, displayScore: 0, uncertaintyScore: 0,
    evidenceComment: '', structuralComment: '', authenticityComment: '',
    distinguishabilityComment: '', reversibilityComment: '', uncertaintyComment: '',
    academicComment: '', finalConclusion: '', evaluator: '', evaluationDate: ''
})

export const createRestorationContext = project => {
    const archive = createMockArchive(project)
    const archiveWorkspace = createMockArchiveWorkspace(project)
    const process = createMockProcess(project)
    const steps = createMockProcessSteps(project)
    const comparisons = createMockComparisonGroups(project)
    comparisons[0].selectedForRestoration = true
    return {
        archive, archiveWorkspace, process, steps, comparisons,
        diseases: archiveWorkspace.diseaseRecords,
        detections: archiveWorkspace.detectionReferences
    }
}

export const createEmptyRestoration = (project, processId = 1, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId, archiveId: project.id,
    processId, comparisonIds: [], stepIds: [], diseaseIds: [], detectionIds: [],
    resultCode: `RR-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-3)}`,
    resultName: '', restorationType: 'digital_3d', restorationCategory: 'structure',
    targetPart: '', restorationScope: '', restorationPurpose: '', basisSummary: '',
    methodSummary: '', resultSummary: '', uncertaintySummary: '', evidenceLevel: '',
    confidenceLevel: 'undetermined', resultStatus: 'draft', currentVersion: 'V1.0',
    overallScore: 0, evaluationConclusion: '', selectedForArchive: false,
    recommendedResult: false, requiresMonitoring: false, principal: project.principal || '',
    participantNames: '', startDate: '', completionDate: '', updateTime: '',
    parts: [], sources: [], media: [], models: [], versions: [], methodParameters: [],
    evaluation: emptyEvaluation(),
    monitoring: { partIds: [], indicators: '', cycle: '', baselineMediaId: null, warningConditions: '', note: '' }
})

export const createMockRestorations = project => [
    {
        ...createEmptyRestoration(project, 1, 1),
        id: 1, resultCode: 'RR-M45C1-001', resultName: 'M45-C1马车整体三维数字复原',
        restorationType: 'digital_3d', restorationCategory: 'structure', targetPart: '马车整体',
        restorationScope: '车轮、车舆、车辕及其连接结构',
        restorationPurpose: '展示马车主体结构和可能的原始形态',
        basisSummary: '依据原始结构、测绘资料、左右对称关系及同类车辆资料。',
        methodSummary: '采用摄影测量、三维建模和虚拟装配，推测区域使用独立图层。',
        resultSummary: '已完成主体结构复原，局部装饰仍存在不确定性。',
        uncertaintySummary: '车舆上部装饰及部分连接构件缺乏直接证据，仅表达一种可能形态。',
        evidenceLevel: 'B', confidenceLevel: 'medium', resultStatus: 'in_progress',
        currentVersion: 'V1.2', overallScore: 4.3, evaluationConclusion: '主体结构表达合理，推测部分需继续补充专家意见。',
        selectedForArchive: true, recommendedResult: true, requiresMonitoring: false,
        principal: '张三', participantNames: '李四、王五', startDate: '2026-07-20',
        updateTime: '2026-07-27 15:20:00', comparisonIds: [1], stepIds: [5, 8],
        diseaseIds: [1, 3], detectionIds: [2],
        parts: [
            { id: 1, partCode: 'PART-01', partName: '原始保存车轮结构', partType: 'original', targetLocation: '左右车轮残存区域', scopeDescription: '保留出土时保存的车轮原始构件', materialName: '原始文物材料', technique: '三维扫描和原始模型保留', evidenceLevel: 'A', confidenceLevel: 'high', removable: false, reversible: false, reversibleDescription: '原始遗存不涉及拆除', distinguishable: true, displayStyle: '实线边框', annotationText: '原始保存部分', percentageValue: 45, sortOrder: 1, modelLayer: true },
            { id: 2, partCode: 'PART-02', partName: '修复后的原件部分', partType: 'repaired', targetLocation: '右侧车轮与基座', scopeDescription: '经加固处理但仍属于原件的结构', materialName: '原始材料及加固材料', technique: '低压灌浆与局部锚固', evidenceLevel: 'A', confidenceLevel: 'high', removable: false, reversible: false, reversibleDescription: '处理记录详见修复步骤', distinguishable: true, displayStyle: '普通填充并标注', annotationText: '原件修复部分', percentageValue: 20, sortOrder: 2, modelLayer: true },
            { id: 3, partCode: 'PART-03', partName: '右侧车轮缺损结构复原', partType: 'evidence_based', targetLocation: '右侧车轮缺失区域', scopeDescription: '依据原始弧度和左右对称关系进行数字补全', materialName: '', technique: '三维曲面重建', evidenceLevel: 'B', confidenceLevel: 'medium', removable: true, reversible: true, reversibleDescription: '数字图层可关闭和替换', distinguishable: true, displayStyle: '点状边框', annotationText: '证据支持复原部分', percentageValue: 20, sortOrder: 3, modelLayer: true },
            { id: 4, partCode: 'PART-04', partName: '车舆上部装饰结构', partType: 'inferred', targetLocation: '车舆上部', scopeDescription: '根据同类器物进行推测性数字表达', materialName: '', technique: '参考同类车辆进行三维建模', evidenceLevel: 'C', confidenceLevel: 'low', removable: true, reversible: true, reversibleDescription: '推测图层可完全隐藏', distinguishable: true, displayStyle: '半透明斜线纹理', annotationText: '推测复原，不等于原始遗存', percentageValue: 10, sortOrder: 4, modelLayer: true },
            { id: 5, partCode: 'PART-05', partName: '无法确定连接区域', partType: 'uncertain', targetLocation: '车舆与车辕连接区', scopeDescription: '现有证据不足，保留空间占位', materialName: '', technique: '不做实体补配', evidenceLevel: 'D', confidenceLevel: 'undetermined', removable: true, reversible: true, reversibleDescription: '占位层可隐藏', distinguishable: true, displayStyle: '灰色斜线纹理', annotationText: '无法确定部分', percentageValue: 5, sortOrder: 5, modelLayer: false }
        ],
        sources: [
            { id: 1, restorationPartId: 3, sourceType: 'symmetry', sourceTitle: '左右车轮结构对称关系', businessType: 'artifact', businessId: project.artifactId, supportDescription: '左侧车轮保存相对完整，支持右侧车轮弧度、轮辐数量和连接位置复原。', reliability: 'high', evidenceLevel: 'B', fileName: '左右车轮结构对比图.svg', fileUrl: '/mock/restoration-new/evidence-wheel.svg', sortOrder: 1 },
            { id: 2, restorationPartId: 4, sourceType: 'similar_artifact', sourceTitle: '同类战国时期车辆资料', businessType: 'external', businessId: null, supportDescription: '仅用于车舆上部装饰结构的可能形态表达，不作为直接证据。', reliability: 'medium', evidenceLevel: 'C', fileName: '同类车辆参考.svg', fileUrl: '/mock/restoration-new/reference.svg', sortOrder: 2 },
            { id: 3, restorationPartId: 2, sourceType: 'comparison', sourceTitle: '车轮修复前后状态对比', businessType: 'comparison', businessId: 1, supportDescription: '支持修复后的原件状态与数字扫描基准。', reliability: 'high', evidenceLevel: 'A', fileName: '裂隙灌浆后.svg', fileUrl: '/mock/comparison/crack-after.svg', sortOrder: 3 }
        ],
        media: [
            { id: 1, restorationPartId: null, sourceMediaId: 3, sourceBusinessType: 'comparison', sourceBusinessId: 1, mediaStage: 'before', mediaType: 'image', fileName: '裂隙灌浆前.jpg', fileUrl: '/mock/comparison/crack-before.svg', thumbnailUrl: '/mock/comparison/crack-before.svg', title: '复原前车轮状态', description: '引用前后对比源影像', isPrimary: false, selectedForArchive: true, selectedAsMonitoringBaseline: false, sortOrder: 1 },
            { id: 2, restorationPartId: null, sourceMediaId: null, sourceBusinessType: 'restoration', sourceBusinessId: 1, mediaStage: 'render', mediaType: 'render', fileName: 'M45C1-restored-v1.2.svg', fileUrl: '/mock/restoration-new/carriage.svg', thumbnailUrl: '/mock/restoration-new/carriage.svg', title: '马车整体三维复原V1.2', description: '当前推荐版本整体渲染图', isPrimary: true, selectedForArchive: true, selectedAsMonitoringBaseline: false, sortOrder: 2 },
            { id: 3, restorationPartId: null, sourceMediaId: null, sourceBusinessType: 'restoration', sourceBusinessId: 1, mediaStage: 'annotation', mediaType: 'drawing', fileName: 'restoration-parts.svg', fileUrl: '/mock/restoration-new/parts.svg', thumbnailUrl: '/mock/restoration-new/parts.svg', title: '复原组成部分标注图', description: '区分原始、修复、证据复原与推测区域', isPrimary: false, selectedForArchive: true, selectedAsMonitoringBaseline: false, sortOrder: 3 }
        ],
        models: [{ id: 1, modelName: 'M45-C1马车整体复原模型', modelType: 'restored', fileName: 'M45C1-restored-v1.2.glb', fileUrl: '/mock/models/M45C1-restored-v1.2.glb', previewImageUrl: '/mock/restoration-new/carriage.svg', fileFormat: 'GLB', fileSize: 245000000, scaleUnit: 'mm', coordinateSystem: '项目局部坐标系', polygonCount: 1250000, textureCount: 8, modelStage: 'draft', modelDescription: '包含原始、修复、证据复原、推测和不确定图层', supportsLayer: true, supportsAnnotation: true, isPrimary: true }],
        methodParameters: [
            { id: 1, name: '采集方法', value: '摄影测量', unit: '', description: '照片重叠率不低于80%' },
            { id: 2, name: '建模软件', value: 'Blender 4.2', unit: '', description: '模型清理与虚拟装配' },
            { id: 3, name: '面数', value: '1250000', unit: '面', description: '当前推荐模型' },
            { id: 4, name: '输出格式', value: 'GLB', unit: '', description: '保留图层信息' }
        ],
        versions: [
            { id: 1, versionNo: 'V1.0', versionName: '初始结构复原', versionType: 'initial', changeDescription: '完成主体结构初步复原', evidenceLevel: 'B', confidenceLevel: 'medium', isCurrent: false, isRecommended: false, archived: false, creator: '张三', createTime: '2026-07-20 10:30:00' },
            { id: 2, versionNo: 'V1.2', versionName: '专家意见修订版', versionType: 'revision', changeDescription: '调整车舆比例并加强推测部分标识', evidenceLevel: 'B', confidenceLevel: 'medium', isCurrent: true, isRecommended: true, archived: false, creator: '张三', createTime: '2026-07-25 14:20:00' }
        ],
        evaluation: { evidenceScore: 4.1, structuralScore: 4.3, authenticityScore: 4, informationScore: 4.5, distinguishabilityScore: 4.8, reversibilityScore: 5, displayScore: 4.5, uncertaintyScore: 4.2, evidenceComment: '主体结构具有较充分证据支持', structuralComment: '车轮与车舆连接关系基本合理', authenticityComment: '原始部分与推测部分得到明确区分', distinguishabilityComment: '通过图层、边框和纹理进行双重标识', reversibilityComment: '数字复原可随新证据更新', uncertaintyComment: '车舆上部装饰结构仍存在较大不确定性', academicComment: '可用于结构研究和展示，推测内容不宜作为定论', finalConclusion: '成果基本达到结构复原和展示要求', evaluator: '王五', evaluationDate: '2026-07-26' }
    },
    {
        ...createEmptyRestoration(project, 1, 2),
        id: 2, resultCode: 'RR-M45C1-002', resultName: '右侧车轮缺损实体补配复原',
        restorationType: 'physical', restorationCategory: 'missing_part', targetPart: '右侧车轮',
        restorationScope: '车轮局部缺失区域', restorationPurpose: '恢复车轮结构完整性并满足展示需要',
        basisSummary: '依据原始弧度、左右对称关系和连接位置。',
        methodSummary: '采用可拆卸补配结构，原件接触面设置隔离层。',
        resultSummary: '补配区域连接稳定，近距离能够辨识。',
        uncertaintySummary: '局部表面纹理采用保守处理，不复原缺乏证据的装饰。',
        evidenceLevel: 'A', confidenceLevel: 'high', resultStatus: 'completed',
        currentVersion: 'V1.0', overallScore: 4.6, evaluationConclusion: '实体补配符合可辨识和可拆卸原则。',
        selectedForArchive: true, recommendedResult: false, requiresMonitoring: true,
        principal: '张三', startDate: '2026-07-18', completionDate: '2026-07-25',
        updateTime: '2026-07-25 17:20:00', comparisonIds: [1], stepIds: [5], diseaseIds: [1],
        parts: [{ id: 21, partCode: 'PART-P01', partName: '车轮实体补配构件', partType: 'physical_completion', targetLocation: '右侧车轮缺失区域', scopeDescription: '按残存弧度制作可拆卸补配构件', materialName: '轻质惰性补配材料', technique: '机械连接与可逆粘接', evidenceLevel: 'A', confidenceLevel: 'high', removable: true, reversible: true, reversibleDescription: '拆除机械连接后可整体移除，不损伤原件', distinguishable: true, displayStyle: '虚线边界', annotationText: '实体补配，近距离可辨识', percentageValue: 100, sortOrder: 1, modelLayer: false }],
        sources: [{ id: 21, restorationPartId: 21, sourceType: 'symmetry', sourceTitle: '左右车轮弧度关系', businessType: 'artifact', businessId: project.artifactId, supportDescription: '完整侧车轮提供弧度和连接位置直接参考。', reliability: 'high', evidenceLevel: 'A', fileName: '', fileUrl: '/mock/restoration-new/evidence-wheel.svg', sortOrder: 1 }],
        media: [{ id: 21, restorationPartId: 21, sourceMediaId: null, sourceBusinessType: 'restoration', sourceBusinessId: 2, mediaStage: 'final', mediaType: 'image', fileName: 'wheel-physical-final.svg', fileUrl: '/mock/restoration-new/wheel.svg', thumbnailUrl: '/mock/restoration-new/wheel.svg', title: '车轮实体补配完成状态', description: '虚线边界标识后期补配构件', isPrimary: true, selectedForArchive: true, selectedAsMonitoringBaseline: true, sortOrder: 1 }],
        models: [], methodParameters: [{ id: 21, name: '实际材料', value: '轻质惰性补配材料', unit: '', description: '与原件接触面设置隔离层' }, { id: 22, name: '接缝宽度', value: '0.8', unit: 'mm', description: '满足可辨识要求' }],
        versions: [{ id: 21, versionNo: 'V1.0', versionName: '实体补配完成版', versionType: 'final', changeDescription: '完成实体补配并通过质量检查', evidenceLevel: 'A', confidenceLevel: 'high', isCurrent: true, isRecommended: true, archived: false, creator: '张三', createTime: '2026-07-25 16:30:00' }],
        evaluation: { evidenceScore: 4.8, structuralScore: 4.7, authenticityScore: 4.5, informationScore: 4.6, distinguishabilityScore: 4.8, reversibilityScore: 4.7, displayScore: 4.4, uncertaintyScore: 4.5, evidenceComment: '弧度与连接位置有直接证据支持', structuralComment: '补配连接稳定', authenticityComment: '未补全缺乏证据的纹饰', distinguishabilityComment: '补配边界清晰可辨', reversibilityComment: '构件可以完整拆除', uncertaintyComment: '表面纹理采用保守表达', academicComment: '符合最小干预原则', finalConclusion: '实体补配达到结构与展示目标', evaluator: '王五', evaluationDate: '2026-07-25' },
        monitoring: { partIds: [21], indicators: '接缝宽度、色差、材料脱落', cycle: '每30天', baselineMediaId: 21, warningConditions: '接缝宽度增加超过0.2mm', note: '使用相同机位与照明条件复查。' }
    }
]

