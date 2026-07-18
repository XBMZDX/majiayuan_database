export const mockProjects = {
    1: {
        id: 1, projectCode: 'CR-M45C1-001', projectName: 'M45-C1马车本体保护修复',
        artifactId: 101, artifactCode: 'M45-C1', artifactName: '马车本体', artifactCategory: '车马器',
        material: '土体、木质、金属复合材质', era: '战国晚期', excavationLocation: '甘肃省张家川马家塬遗址',
        tombCode: 'M45', relicUnit: 'M45-C1', artifactLevel: '待定级', collectionUnit: '马家塬考古工作站',
        extractionDate: '2025-11-18', projectType: 'comprehensive', status: 'active', currentStage: 'planning',
        riskLevel: 'high', progress: 25, principal: '张三', department: '文物保护实验室',
        startDate: '2025-12-19', expectedEndDate: '2026-12-31',
        summary: '针对M45-C1马车本体土体、木质与金属复合结构开展整体稳定、病害控制和信息保存工作。'
    },
    2: {
        id: 2, projectCode: 'CR-M16G1-002', projectName: 'M16-G1铜鼎除锈保护', artifactId: 102,
        artifactCode: 'M16-G1', artifactName: '铜鼎', artifactCategory: '青铜器', material: '青铜', era: '战国晚期',
        excavationLocation: '马家塬遗址M16', tombCode: 'M16', relicUnit: 'M16-G1', artifactLevel: '三级',
        collectionUnit: '马家塬考古工作站', extractionDate: '2026-01-08', projectType: 'special',
        status: 'active', currentStage: 'protection', riskLevel: 'medium', progress: 45, principal: '李四',
        department: '金属文物保护组', startDate: '2026-02-10', expectedEndDate: '2026-10-30',
        summary: '铜鼎腐蚀产物分析、稳定化处理及表面封护项目。'
    },
    3: {
        id: 3, projectCode: 'CR-M62G2-003', projectName: 'M62-G2铜盘复原', artifactId: 103,
        artifactCode: 'M62-G2', artifactName: '铜盘', artifactCategory: '青铜器', material: '青铜', era: '战国晚期',
        excavationLocation: '马家塬遗址M62', tombCode: 'M62', relicUnit: 'M62-G2', artifactLevel: '待定级',
        collectionUnit: '马家塬考古工作站', extractionDate: '2025-07-18', projectType: 'restoration',
        status: 'active', currentStage: 'restoration', riskLevel: 'low', progress: 82, principal: '王五',
        department: '文物保护实验室', startDate: '2025-08-01', expectedEndDate: '2026-09-30',
        summary: '依据残片拼接关系完成铜盘实体复原与数字复原。'
    },
    4: {
        id: 4, projectCode: 'CR-M25-004', projectName: 'M25漆器保护修复', artifactId: 104,
        artifactCode: 'M25-L1', artifactName: '漆盒', artifactCategory: '漆木器', material: '木胎漆器', era: '战国晚期',
        excavationLocation: '马家塬遗址M25', tombCode: 'M25', relicUnit: 'M25-L1', artifactLevel: '二级',
        collectionUnit: '马家塬考古工作站', extractionDate: '2024-05-20', projectType: 'comprehensive',
        status: 'completed', currentStage: 'completed', riskLevel: 'medium', progress: 100, principal: '赵六',
        department: '有机质文物保护组', startDate: '2024-06-01', expectedEndDate: '2026-06-30',
        summary: '漆皮回贴、木胎稳定与预防性保护项目。'
    },
    5: {
        id: 5, projectCode: 'CR-M45-005', projectName: 'M45出土陶器保护', artifactId: 105,
        artifactCode: 'M45-T01', artifactName: '彩绘陶罐', artifactCategory: '陶器', material: '彩绘陶', era: '战国晚期',
        excavationLocation: '马家塬遗址M45', tombCode: 'M45', relicUnit: 'M45-T01', artifactLevel: '待定级',
        collectionUnit: '马家塬考古工作站', extractionDate: '2026-06-25', projectType: 'preventive',
        status: 'draft', currentStage: 'pendingSurvey', riskLevel: 'high', progress: 0, principal: '张三',
        department: '文物保护实验室', startDate: '2026-07-01', expectedEndDate: '2027-01-30',
        summary: '彩绘陶罐现场提取后稳定性评估与保护。'
    }
}

export const archiveSectionDefinitions = [
    ['project', '文物与项目信息', 'project', false, 5],
    ['condition', '保存现状', 'disease_survey', false, 10],
    ['disease', '病害调查', 'disease_survey', false, 15],
    ['detection', '检测分析依据', 'detection', false, 10],
    ['principle', '保护修复原则与目标', 'manual', true, 5],
    ['plan', '保护修复方案', 'manual', true, 15],
    ['material', '材料、工具与工艺', 'manual', true, 10],
    ['process', '修复过程记录', 'process', false, 10],
    ['comparison', '修复前后对比', 'comparison', false, 5],
    ['restoration', '文物复原成果', 'restoration', false, 5],
    ['evaluation', '效果评估', 'manual', true, 5],
    ['advice', '保存环境与后续建议', 'manual', true, 3],
    ['attachment', '影像及附件档案', 'attachment', true, 2]
].map(([code, name, sourceType, editable, weight]) => ({ code, name, sourceType, editable, weight }))

export const createMockArchive = (project) => ({
    id: project.id === 1 ? 1 : Date.now(), projectId: project.id, artifactId: project.artifactId,
    archiveCode: `CA-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-${String(project.id).padStart(3, '0')}`,
    archiveTitle: `${project.artifactCode}${project.artifactName}保护修复档案`, archiveType: 'comprehensive',
    compiler: project.principal || '', reviewer: '', approvalPerson: '', archiveStatus: 'compiling',
    currentVersion: 'V1.0', executiveSummary: '', protectionGoal: '', conservationBasis: '', finalConclusion: '',
    sourceSurveyId: 1, sourceSurveyVersion: 'V1.0', compiledDate: '2026-07-15',
    submittedTime: '', updateTime: '2026-07-17 10:30:00'
})

export const createMockArchiveWorkspace = (project) => ({
    surveyReferenceUpdated: false,
    survey: {
        id: 1, surveyCode: `DS-${project.artifactCode.replace(/[^A-Za-z0-9]/g, '')}-001`,
        surveyDate: '2026-07-15', surveyor: '张三、李四', surveyLocation: '文物保护实验室',
        preservationStatus: '保存状况较差', structuralStability: '局部不稳定',
        environmentSummary: '出土后处于临时保护环境，相对湿度波动较大；木质、土体和金属构件存在差异性失水风险。',
        overallRisk: 'high', summary: '结构性病害与表面风化并存，车轮和基座区域需优先临时支撑，随后开展分区加固。',
        status: 'submitted'
    },
    diseaseRecords: [
        { id: 1, diseaseName: '裂隙', severity: 'severe', developmentStatus: '发展中', extentValue: 35, extentUnit: 'cm', partName: '车轮', side: '右侧', positionDescription: '车轮右侧上部存在纵向贯通裂隙', morphology: '裂隙边缘松散，局部伴随粉化', causeAnalysis: '长期失水及结构受力变化', structuralImpact: '局部结构稳定性降低', emergency: true, recommendedAction: '建议优先进行临时支撑及裂隙加固', mediaCount: 3 },
        { id: 2, diseaseName: '风化', severity: 'moderate', developmentStatus: '缓慢发展', extentValue: 18, extentUnit: '%', partName: '车厢表面', side: '整体', positionDescription: '车厢表面局部粉化', morphology: '表层颗粒松散', causeAnalysis: '环境干燥及盐分迁移', structuralImpact: '暂无明显结构影响', emergency: false, recommendedAction: '建议清理浮尘后进行表面渗透加固', mediaCount: 2 },
        { id: 3, diseaseName: '坍塌', severity: 'critical', developmentStatus: '暂时稳定', extentValue: 1, extentUnit: '处', partName: '基座', side: '底部', positionDescription: '基座局部坍塌并造成支撑不足', morphology: '结构局部塌陷', causeAnalysis: '长期受力不均及支撑土松散', structuralImpact: '影响整体支撑关系', emergency: true, recommendedAction: '立即实施临时支撑和分区加固', mediaCount: 4 },
        { id: 4, diseaseName: '锈蚀', severity: 'minor', developmentStatus: '稳定', extentValue: 6, extentUnit: '处', partName: '金属连接件', side: '局部', positionDescription: '金属连接件表面存在浅层锈蚀', morphology: '红褐色疏松腐蚀产物', causeAnalysis: '埋藏环境湿度与氯离子影响', structuralImpact: '局部影响', emergency: false, recommendedAction: '待检测确认腐蚀产物后进行稳定处理', mediaCount: 2 }
    ],
    detectionReferences: [
        { id: 1, experimentName: '土体含盐量及离子组成检测', experimentType: '离子色谱', method: '水浸提-离子色谱法', instrumentModel: 'ICS-600', detectionDate: '2026-07-10', detector: '王敏', conclusion: '样品中可溶盐以硫酸根和氯离子为主，需控制加固材料含水量。', relatedDisease: '风化', purpose: '成因判断依据', reportName: 'M45-C1土体离子检测报告.pdf' },
        { id: 2, experimentName: '表面显微形貌观察', experimentType: '显微观察', method: '超景深显微观察', instrumentModel: 'VHX-7000', detectionDate: '2026-07-12', detector: '刘洋', conclusion: '颗粒间胶结明显减弱，局部可见片状剥离，支持采用低浓度多次渗透加固。', relatedDisease: '风化', purpose: '方案选择依据', reportName: 'M45-C1显微观察记录.pdf' }
    ],
    principles: {
        options: ['最小干预原则', '保持原状原则', '历史真实性原则', '艺术性原则', '可辨识性原则', '可再处理性原则', '兼容性原则', '耐久性原则', '安全性原则', '预防性保护原则'],
        selected: ['最小干预原则', '历史真实性原则', '兼容性原则', '安全性原则'],
        notes: {
            '最小干预原则': '仅对影响结构安全和病害持续发展的部位实施必要处理。',
            '历史真实性原则': '保留原始加工痕迹、使用痕迹和出土信息。',
            '兼容性原则': '所有加固与粘接材料先完成兼容性和色差试验。',
            '安全性原则': '全过程设置临时支撑并控制操作振动。'
        },
        custom: ''
    },
    goals: {
        overall: '在保留原始信息和现状真实性的前提下，控制主要病害发展，恢复局部结构稳定，为后续研究、展示和长期保存创造条件。',
        diseaseControl: '控制裂隙、坍塌与表面风化继续发展。', structuralStability: '建立可靠临时支撑并恢复车轮、基座局部稳定。',
        appearance: '补配区域与原始区域协调但保持可辨识。', informationRetention: '完整保留原始构件、加工痕迹和土体关系。',
        displayUse: '', longTerm: '建立稳定的保存环境和周期性监测机制。'
    },
    plan: {
        id: 1, planCode: 'CP-M45C1-001', planName: 'M45-C1马车本体综合保护修复方案',
        planGoal: '分阶段完成结构支撑、病害控制和预防性保护。',
        technicalBasis: '依据正式病害调查、土体离子检测、显微观察及相关文物保护技术规范。',
        selectedMethod: '先建立临时支撑，完成表面清理和材料小样试验；随后分区实施裂隙灌浆、土体渗透加固及金属连接件稳定处理。',
        expectedResult: '主要结构性病害得到控制，脆弱表层强度提高，原始信息得到最大限度保存。',
        riskAnalysis: '材料渗透可能引起局部色差，灌浆过程可能扰动脆弱表层。',
        safetyRequirements: '设置临时支撑和操作警戒区；使用有机溶剂时保持通风并佩戴防护用品。',
        environmentRequirements: '温度18–25℃，相对湿度45%–60%，避免直射光和快速失水。',
        reversibilityDescription: '粘接与表面保护材料优先选择可再处理体系，并保留材料使用记录。',
        compatibilityDescription: '正式使用前开展材料与土体、颜料层和金属构件的兼容性试验。',
        emergencyMeasures: '发现结构位移、表面明显色差或材料异常反应时立即停止操作并回退至临时支撑状态。',
        planStatus: 'draft', compiler: '张三', reviewer: '', compiledDate: '2026-07-15'
    },
    planDiseaseList: [
        { id: 1, diseaseRecordId: 1, diseaseRecordIds: [1], diseaseName: '裂隙', severity: 'severe', treatmentStrategy: '临时支撑、裂隙灌浆和局部锚固', priorityLevel: 'high', expectedResult: '控制裂隙继续发展，恢复局部结构稳定', riskDescription: '灌浆压力可能扰动表层', precautions: '低压、分次灌注，操作过程中避免二次扰动' },
        { id: 2, diseaseRecordId: 2, diseaseRecordIds: [2], diseaseName: '风化', severity: 'moderate', treatmentStrategy: '清理浮尘后进行低浓度渗透加固', priorityLevel: 'high', expectedResult: '提高土体表面颗粒粘结强度', riskDescription: '可能出现色差', precautions: '正式使用前完成材料兼容性和色差测试' }
    ],
    materialTab: 'materials',
    materialDictionary: [
        { id: 1, materialName: 'Remmers 300', materialType: '加固材料', safetyNote: '操作时保持通风并佩戴防护手套', compatibilityNote: '需先完成土体吸收率和色差试验' },
        { id: 2, materialName: 'B72', materialType: '加固与粘接材料', safetyNote: '远离火源，避免长时间吸入溶剂蒸气', compatibilityNote: '可再处理性较好，需控制浓度和光泽变化' },
        { id: 3, materialName: 'AC33', materialType: '加固材料', safetyNote: '避免接触眼睛和皮肤', compatibilityNote: '使用前进行老化与色差测试' },
        { id: 4, materialName: '无水乙醇', materialType: '溶剂', safetyNote: '易燃，远离火源并使用防爆设备', compatibilityNote: '确认不溶解原有颜料和表面材料' },
        { id: 5, materialName: '天然水硬性石灰注浆料', materialType: '注浆材料', safetyNote: '避免粉尘吸入，施工时佩戴护目镜', compatibilityNote: '与土体孔隙率和强度匹配后使用' }
    ],
    planMaterials: [
        { id: 1, materialId: 1, materialName: 'Remmers 300', materialType: '加固材料', concentration: '', mixRatio: '与无水乙醇1:1预处理', applicationMethod: '少量多次渗透', plannedAmount: '按实际吸收情况控制', targetPart: '土体及车体支撑土', purpose: '防风化加固', safetyNote: '保持通风并佩戴防护手套', compatibilityNote: '先完成吸收率和色差试验', precautions: '控制材料流淌并记录色差变化' },
        { id: 2, materialId: 2, materialName: 'B72', materialType: '加固与粘接材料', concentration: '1.5%', mixRatio: '', applicationMethod: '局部涂刷或滴加', plannedAmount: '按局部病害面积确定', targetPart: '局部脆弱构件', purpose: '二次加固', safetyNote: '', compatibilityNote: '可再处理性较好，需验证光泽变化', precautions: '先进行兼容性试验' }
    ],
    tools: [
        { id: 1, toolName: '低压注浆器', model: 'LP-20', purpose: '裂隙灌浆', parameter: '≤0.05MPa', targetPart: '车轮裂隙', safetyNote: '持续观察压力和浆液回流' },
        { id: 2, toolName: '超景深显微镜', model: 'VHX-7000', purpose: '过程观察', parameter: '20–200×', targetPart: '风化表面', safetyNote: '避免长时间强光照射' }
    ],
    processParameters: {
        operationMethod: '分区、分级、少量多次处理', applicationOrder: '临时支撑→表面清理→小样试验→结构处理→表面加固',
        materialConcentration: '按材料小样试验确定', dryingTime: '每次操作后至少24小时', operationTimes: '2–4次',
        temperature: '18–25℃', humidity: '45%–60%RH', parameterLimit: '注浆压力≤0.05MPa',
        qualityControl: '每个区域处理前后拍照、色差测量并记录材料批次和实际用量。',
        emergencyRequirement: '出现结构位移、异常变色或表面起翘时立即停工并采取临时支撑。'
    },
    processSummary: {
        planned: 8, completed: 3, processing: 1, pending: 4,
        steps: [
            { id: 1, title: '建立档案与影像基线', status: 'completed', statusText: '已完成', date: '2026-07-01', operator: '张三', disease: '全部病害', material: '无', imageCount: 18 },
            { id: 2, title: '临时支撑', status: 'completed', statusText: '已完成', date: '2026-07-05', operator: '张三、李四', disease: '裂隙、坍塌', material: '支撑材料', imageCount: 12 },
            { id: 3, title: '表面清理', status: 'completed', statusText: '已完成', date: '2026-07-08', operator: '李四', disease: '风化', material: '无水乙醇', imageCount: 9 },
            { id: 4, title: '材料小样试验', status: 'processing', statusText: '进行中', date: '2026-07-16', operator: '王敏', disease: '风化', material: 'Remmers 300、B72', imageCount: 6 },
            { id: 5, title: '裂隙处理', status: 'pending', statusText: '待开始', date: '', operator: '', disease: '裂隙', material: '待确认', imageCount: 0 }
        ]
    },
    comparisons: [{ id: 1, title: '车轮裂隙处理前后', part: '右侧车轮', disease: '裂隙', step: '裂隙处理', description: '当前仅建立修复前影像基线，修复后影像待补充。' }],
    restorationResults: [],
    evaluation: {
        diseaseControl: '', structuralChange: '', surfaceStrength: '', appearanceCoordination: '', colorChange: '',
        glossChange: '', materialCompatibility: '', goalAchievement: '', hasSideEffects: 'pending',
        remainingIssues: '', acceptanceConclusion: '', evaluator: '', evaluationDate: '', retestIds: []
    },
    retestOptions: [
        { id: 1, name: '色度复测（待完成）' }, { id: 2, name: '显微观察复测（待完成）' }, { id: 3, name: '表面强度测试（待完成）' }
    ],
    advice: {
        temperatureRange: '18–25℃', humidityRange: '45%–60%RH', lighting: '避免直射光，展示照度不高于150lx',
        airQuality: '控制粉尘和酸性气体，保持空气缓慢循环', packaging: '采用惰性缓冲材料和独立支撑结构',
        display: '', handling: '整体托架搬运，禁止直接受力于原始构件', shockproof: '运输和展示均设置减震底座',
        reviewCycle: '', monitorDiseases: '', monitoringIndicators: '', followUpAdvice: '',
        warningConditions: '裂隙宽度增长超过0.2mm或相对湿度连续24小时超出建议范围时预警'
    },
    attachments: [
        { id: 1, fileName: 'M45-C1病害调查报告V1.0.pdf', fileType: '报告', fileSize: '4.8 MB', sourceModule: '病害调查', sectionName: '病害调查', uploadedBy: '张三', uploadTime: '2026-07-15 17:30:00', version: 'V1.0', description: '最新已提交病害调查报告' },
        { id: 2, fileName: 'M45-C1土体离子检测报告.pdf', fileType: '报告', fileSize: '2.1 MB', sourceModule: '检测分析', sectionName: '检测分析依据', uploadedBy: '王敏', uploadTime: '2026-07-10 15:20:00', version: 'V1.0', description: '土体可溶盐检测结果' },
        { id: 3, fileName: 'Remmers300安全数据说明.pdf', fileType: '材料说明', fileSize: '860 KB', sourceModule: '保护修复方案', sectionName: '材料、工具与工艺', uploadedBy: '李四', uploadTime: '2026-07-16 10:18:00', version: 'V1.0', description: '材料安全数据表' },
        { id: 4, fileName: 'M45-C1修复前整体照片.jpg', fileType: '照片', fileSize: '12.6 MB', sourceModule: '修复前后对比', sectionName: '修复前后对比', uploadedBy: '赵宁', uploadTime: '2026-07-01 09:40:00', version: 'V1.0', description: '修复前整体影像基线' }
    ]
})
