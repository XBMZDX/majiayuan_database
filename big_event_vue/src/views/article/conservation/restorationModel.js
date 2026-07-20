const emptyEvaluation = () => ({
    evidenceScore: 0, structuralScore: 0, authenticityScore: 0, informationScore: 0,
    distinguishabilityScore: 0, reversibilityScore: 0, displayScore: 0, uncertaintyScore: 0,
    evidenceComment: '', structuralComment: '', authenticityComment: '',
    distinguishabilityComment: '', reversibilityComment: '', uncertaintyComment: '',
    academicComment: '', finalConclusion: '', evaluator: '', evaluationDate: ''
})

export const createEmptyRestoration = (project, processId = null, seed = Date.now()) => ({
    id: seed, projectId: project.id, artifactId: project.artifactId, archiveId: null,
    processId, comparisonIds: [], stepIds: [], diseaseIds: [], detectionIds: [],
    resultCode: `RR-${String(project.artifactCode || `PROJECT${project.id}`).replace(/[^A-Za-z0-9]/g, '')}-${String(seed).slice(-6)}`,
    resultName: '', restorationType: 'digital_3d', restorationCategory: 'structure',
    targetPart: '', restorationScope: '', restorationPurpose: '', basisSummary: '',
    methodSummary: '', resultSummary: '', uncertaintySummary: '', evidenceLevel: '',
    confidenceLevel: 'undetermined', resultStatus: 'draft', currentVersion: 'V1.0',
    completionRate: 0, overallScore: 0, evaluationConclusion: '',
    selectedForArchive: false, recommendedResult: false, requiresMonitoring: false,
    principal: project.principal || '', participantNames: '', startDate: '',
    completionDate: '', updateTime: '', parts: [], sources: [], media: [], models: [],
    versions: [], methodParameters: [], evaluation: emptyEvaluation(),
    monitoring: {
        partIds: [], indicators: '', cycle: '', baselineMediaId: null,
        warningConditions: '', note: ''
    }
})

export const normalizeRestorations = results => results.map(result => ({
    ...result,
    stepIds: result.stepIds || [],
    comparisonIds: result.comparisonIds || [],
    diseaseIds: result.diseaseIds || [],
    detectionIds: result.detectionIds || [],
    parts: (result.parts || []).map(part => ({
        modelObjectName: '', layerVisible: true, annotationPosition: {}, ...part
    })),
    sources: result.sources || [],
    media: result.media || [],
    models: result.models || [],
    versions: (result.versions || []).map(version => ({ isRecommended: false, snapshot: {}, ...version })),
    methodParameters: result.methodParameters || [],
    evaluation: { ...emptyEvaluation(), ...result.evaluation },
    monitoring: {
        partIds: [], indicators: '', cycle: '', baselineMediaId: null,
        warningConditions: '', note: '', ...result.monitoring
    }
}))
