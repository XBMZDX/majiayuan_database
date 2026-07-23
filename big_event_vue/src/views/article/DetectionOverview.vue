<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const overview = ref({ items: [], methods: [], stats: {} })
const viewMode = ref('list')
const pageNum = ref(1)
const pageSize = ref(10)
const addVisible = ref(false)
const addForm = ref(defaultAddForm())
const methodSaving = ref(false)
const methodEditorForm = ref({ artifactCode: '', artifactName: '', excavationRelic: '', sampleMaterial: '', samplePosition: '', sampleStatus: '', manager: '', sampler: '', notes: '', methods: [], originalMethods: [] })
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailEditMode = ref(false)
const detailSource = ref({})
const artifactDetail = ref({ records: [] })
// 默认聚焦已有检测记录的文物；需要补检时可切换为全部或仅未检测。
const filters = ref({ detectionScope: 'detected', sourceType: '', material: '', method: '', resultStatus: '', keyword: '' })

function defaultAddForm() {
    return {
        artifactCode: '', artifactName: '', excavationRelic: '', sampleMaterial: '', samplePosition: '',
        sampleStatus: '待检测', purposeList: [], manager: '', sampler: '', notes: ''
    }
}

const items = computed(() => overview.value.items || [])
const methods = computed(() => overview.value.methods || [])
const stats = computed(() => overview.value.stats || {})
const materialOptions = computed(() => [...new Set(items.value.map(item => item.material).filter(Boolean))])
const filteredItems = computed(() => items.value.filter(item => {
    const keyword = filters.value.keyword.trim().toLowerCase()
    const matchesKeyword = !keyword || [item.artifactCode, item.artifactName, item.excavationRelic]
        .some(value => String(value || '').toLowerCase().includes(keyword))
    const matchesMethod = !filters.value.method || item.methods?.some(method => method.name === filters.value.method)
    const hasDetection = Boolean(item.methods?.length)
    const matchesDetectionScope = filters.value.detectionScope === 'all'
        || (filters.value.detectionScope === 'detected' && hasDetection)
        || (filters.value.detectionScope === 'notDetected' && !hasDetection)
    return matchesKeyword
        && matchesDetectionScope
        && (!filters.value.sourceType || item.sourceType === filters.value.sourceType)
        && (!filters.value.material || item.material === filters.value.material)
        && matchesMethod
        && (!filters.value.resultStatus || item.resultStatus === filters.value.resultStatus)
}))
const pagedItems = computed(() => filteredItems.value.slice((pageNum.value - 1) * pageSize.value, pageNum.value * pageSize.value))
const addMethodOptions = computed(() => [...new Set(methods.value.concat(addForm.value.purposeList || []))])

const fetchOverview = async () => {
    loading.value = true
    try {
        const response = await request.get('/admin/detection/artifact-overview')
        overview.value = response.data || { items: [], methods: [], stats: {} }
    } finally {
        loading.value = false
    }
}

const resetFilters = () => {
    filters.value = { detectionScope: 'detected', sourceType: '', material: '', method: '', resultStatus: '', keyword: '' }
    pageNum.value = 1
}
const handleFilter = () => { pageNum.value = 1 }
const statusType = status => ({ '未检测': 'info', '检测进行中': 'warning', '已有部分结果': 'primary', '结果完整': 'success', '无结果': 'danger' })[status] || 'info'
const methodStatus = (item, methodName) => item.methods?.find(method => method.name === methodName)
const matrixCellType = method => {
    if (!method) return 'info'
    if (method.complete) return 'success'
    if (method.status === '检测中') return 'warning'
    if (method.status === '已完成') return 'primary'
    return 'info'
}
const matrixCellText = method => !method ? '未做' : method.complete ? '完成' : method.status === '检测中' ? '进行中' : method.status === '已完成' ? '待补充' : '无结果'
const openResult = method => {
    if (!method?.analysisResultId) {
        ElMessage.warning('该检测方法尚未生成可查看的分析结果')
        return
    }
    router.push(`/detection/experiment/${method.analysisResultId}`)
}
const appendPhotoUrls = (target, value) => {
    if (!value) return
    if (Array.isArray(value)) {
        value.forEach(item => appendPhotoUrls(target, item))
        return
    }
    if (typeof value === 'object') {
        appendPhotoUrls(target, value.url || value.imageUrl || value.fileUrl || value.path)
        return
    }
    const text = String(value).trim()
    if (!text) return
    try {
        const parsed = JSON.parse(text)
        if (parsed !== text) {
            appendPhotoUrls(target, parsed)
            return
        }
    } catch (error) { /* 兼容历史数据中以逗号分隔的图片地址 */ }
    text.split(/[\n,;]/).map(item => item.trim()).filter(Boolean).forEach(url => target.add(url))
}
const recordPhotoUrls = record => {
    const urls = new Set()
    appendPhotoUrls(urls, record?.detection?.samplePhoto)
    ;(record?.analysisResults || []).forEach(item => appendPhotoUrls(urls, item.samplePhoto))
    ;(record?.experimentResults || []).forEach(item => appendPhotoUrls(urls, item.images))
    return [...urls]
}
const loadArtifactDetail = async () => {
    const row = detailSource.value
    if (!row) return
    artifactDetail.value = {
        artifactCode: row.artifactCode || '', artifactName: row.artifactName || '',
        excavationRelic: row.excavationRelic || '', sampleMaterial: row.material || '',
        sourceType: row.sourceType || '', records: []
    }
    detailLoading.value = true
    try {
        const response = await request.get('/admin/detection/artifact-detail', {
            params: {
                artifactCode: row.artifactCode || '', artifactName: row.artifactName || '',
                excavationRelic: row.excavationRelic || '', sampleMaterial: row.material || '', sourceType: row.sourceType || ''
            }
        })
        artifactDetail.value = response.data || artifactDetail.value
    } catch (error) {
        ElMessage.error('检测详情加载失败')
    } finally {
        detailLoading.value = false
    }
}
const openArtifactDetail = async row => {
    detailSource.value = row
    detailEditMode.value = false
    detailVisible.value = true
    await loadArtifactDetail()
}
const openAdd = () => { addForm.value = defaultAddForm(); addVisible.value = true }
const submitAdd = async () => {
    if (!addForm.value.artifactCode.trim() && !addForm.value.artifactName.trim()) {
        ElMessage.warning('请至少填写文物编号或文物名称')
        return
    }
    if (!addForm.value.purposeList.length) {
        ElMessage.warning('请至少选择一种检测方法')
        return
    }
    const data = {
        ...addForm.value,
        artifactCode: addForm.value.artifactCode.trim().replace(/：/g, ':'),
        purpose: addForm.value.purposeList.join('/'),
        serialNumber: String((items.value.length || 0) + 1),
        sampleQuantity: '', sampleMethod: '', instrumentName: '', instrumentModel: '', testParams: '',
        storageLocation: '', departureTime: '', destination: '', samplePhoto: '', analysisData: '', analysisReport: ''
    }
    delete data.purposeList
    await request.post('/admin/detection', data)
    ElMessage.success('检测记录已新增')
    addVisible.value = false
    await fetchOverview()
}

const startArtifactDetailEdit = () => {
    const currentMethods = [...new Set((artifactDetail.value.records || [])
        .flatMap(record => String(record.detection?.purpose || '').split('/'))
        .map(item => item.trim()).filter(Boolean))]
    const reference = artifactDetail.value.records?.[0]?.detection || {}
    methodEditorForm.value = {
        artifactCode: artifactDetail.value.artifactCode || '',
        artifactName: artifactDetail.value.artifactName || '',
        excavationRelic: artifactDetail.value.excavationRelic || '',
        sampleMaterial: artifactDetail.value.sampleMaterial || '',
        samplePosition: reference.samplePosition || '',
        sampleStatus: reference.sampleStatus || '',
        manager: reference.manager || '',
        sampler: reference.sampler || '',
        notes: reference.notes || '',
        methods: [...currentMethods],
        originalMethods: [...currentMethods]
    }
    detailEditMode.value = true
}
const saveArtifactMethods = async () => {
    const methodsToSave = [...new Set(methodEditorForm.value.methods.map(item => String(item || '').trim()).filter(Boolean))]
    const hasRemovedMethod = methodEditorForm.value.originalMethods.some(item => !methodsToSave.includes(item))
    if (hasRemovedMethod) {
        try {
            await ElMessageBox.confirm('移除检测方法会同时删除该方法对应的分析结果和实验结果，是否继续？', '确认移除检测方法', { type: 'warning' })
        } catch (error) { return }
    }
    methodSaving.value = true
    try {
        const response = await request.put('/admin/detection/artifact-methods', {
            artifactCode: methodEditorForm.value.artifactCode,
            artifactName: methodEditorForm.value.artifactName,
            excavationRelic: methodEditorForm.value.excavationRelic,
            sampleMaterial: methodEditorForm.value.sampleMaterial,
            samplePosition: methodEditorForm.value.samplePosition,
            sampleStatus: methodEditorForm.value.sampleStatus,
            manager: methodEditorForm.value.manager,
            sampler: methodEditorForm.value.sampler,
            notes: methodEditorForm.value.notes,
            methods: methodsToSave
        })
        ElMessage.success(`检测记录已保存：新增 ${response.data?.addedMethodCount || 0} 项，移除 ${response.data?.removedMethodCount || 0} 项`)
        detailEditMode.value = false
        await fetchOverview()
        await loadArtifactDetail()
    } finally {
        methodSaving.value = false
    }
}

watch(() => route.query.method, method => {
    filters.value.method = typeof method === 'string' ? method : ''
    pageNum.value = 1
}, { immediate: true })

onMounted(fetchOverview)
</script>

<template>
    <div class="detection-overview" v-loading="loading">
        <section class="page-heading">
            <div>
                <h2>检测分析总览</h2>
                <p>以文物为单位查看检测覆盖情况、最新进度和结果完整度。</p>
            </div>
            <el-button type="primary" @click="openAdd">新增检测记录</el-button>
        </section>

        <section class="status-cards">
            <el-card shadow="never"><strong>{{ stats.total || 0 }}</strong><span>文物总数</span></el-card>
            <el-card shadow="never" class="not-detected"><strong>{{ stats.notDetected || 0 }}</strong><span>未检测</span></el-card>
            <el-card shadow="never" class="in-progress"><strong>{{ stats.inProgress || 0 }}</strong><span>检测进行中</span></el-card>
            <el-card shadow="never" class="partial"><strong>{{ stats.partial || 0 }}</strong><span>已有部分结果</span></el-card>
            <el-card shadow="never" class="complete"><strong>{{ stats.complete || 0 }}</strong><span>结果完整</span></el-card>
            <el-card shadow="never" class="no-result"><strong>{{ stats.noResult || 0 }}</strong><span>无结果</span></el-card>
        </section>

        <el-card shadow="never" class="filter-card">
            <div class="filter-bar">
                <el-select v-model="filters.detectionScope" placeholder="检测范围" @change="handleFilter">
                    <el-option label="已检测文物" value="detected" />
                    <el-option label="全部文物" value="all" />
                    <el-option label="仅未检测文物" value="notDetected" />
                </el-select>
                <el-input v-model="filters.keyword" clearable placeholder="搜索文物编号、名称或出土来源" @input="handleFilter" />
                <el-select v-model="filters.sourceType" clearable placeholder="出土来源" @change="handleFilter">
                    <el-option label="墓葬" value="墓葬" />
                    <el-option label="棺" value="棺" />
                    <el-option label="车" value="车" />
                    <el-option label="未关联" value="未关联" />
                </el-select>
                <el-select v-model="filters.material" clearable filterable placeholder="文物材质" @change="handleFilter">
                    <el-option v-for="item in materialOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="filters.method" clearable filterable placeholder="检测方法" @change="handleFilter">
                    <el-option v-for="item in methods" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="filters.resultStatus" clearable placeholder="结果状态" @change="handleFilter">
                    <el-option label="未检测" value="未检测" />
                    <el-option label="检测进行中" value="检测进行中" />
                    <el-option label="已有部分结果" value="已有部分结果" />
                    <el-option label="结果完整" value="结果完整" />
                    <el-option label="无结果" value="无结果" />
                </el-select>
                <el-button @click="resetFilters">重置</el-button>
            </div>
        </el-card>

        <el-card shadow="never" class="content-card">
            <template #header>
                <div class="content-header">
                    <span>文物检测覆盖</span>
                    <el-radio-group v-model="viewMode" size="small">
                        <el-radio-button value="list">列表视图</el-radio-button>
                        <el-radio-button value="matrix">检测矩阵</el-radio-button>
                    </el-radio-group>
                </div>
            </template>

            <template v-if="viewMode === 'list'">
                <el-table :data="pagedItems" border stripe @row-dblclick="openArtifactDetail">
                    <el-table-column label="文物编号" prop="artifactCode" min-width="120"><template #default="{ row }">{{ row.artifactCode || '-' }}</template></el-table-column>
                    <el-table-column label="文物名称" prop="artifactName" min-width="130"><template #default="{ row }">{{ row.artifactName || '-' }}</template></el-table-column>
                    <el-table-column label="出土来源" min-width="150"><template #default="{ row }"><el-tag size="small" effect="plain">{{ row.sourceType }}</el-tag><span class="relic-name">{{ row.excavationRelic || '-' }}</span></template></el-table-column>
                    <el-table-column label="材质" prop="material" min-width="105"><template #default="{ row }">{{ row.material || '-' }}</template></el-table-column>
                    <el-table-column label="已做检测方法" min-width="240">
                        <template #default="{ row }">
                            <div v-if="row.methods?.length" class="method-tags">
                                <el-tag v-for="method in row.methods" :key="`${row.artifactKey}-${method.name}`" :type="matrixCellType(method)" effect="plain" class="method-tag" @click="openResult(method)">{{ method.name }}</el-tag>
                            </div>
                            <span v-else class="empty-value">未检测</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="最新检测时间" prop="latestDetectionTime" min-width="145"><template #default="{ row }">{{ row.latestDetectionTime || '-' }}</template></el-table-column>
                    <el-table-column label="结果完整度" min-width="150"><template #default="{ row }"><el-tag :type="statusType(row.resultStatus)" effect="light">{{ row.resultStatus }}</el-tag><div class="completeness-text">{{ row.resultCompleteness }}</div></template></el-table-column>
                    <el-table-column label="详情" width="92" fixed="right" align="center"><template #default="{ row }"><el-button type="primary" link @click.stop="openArtifactDetail(row)">查看</el-button></template></el-table-column>
                </el-table>
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="filteredItems.length" layout="total, sizes, prev, pager, next" background class="pagination" />
            </template>

            <div v-else class="matrix-wrap">
                <div class="matrix-legend"><el-tag type="info" size="small">未做</el-tag><el-tag type="warning" size="small">进行中</el-tag><el-tag type="primary" size="small">待补充</el-tag><el-tag type="success" size="small">完成</el-tag></div>
                <el-table :data="filteredItems" border class="matrix-table" max-height="620">
                    <el-table-column fixed="left" label="文物" min-width="180">
                        <template #default="{ row }"><strong>{{ row.artifactCode || '-' }}</strong><span class="matrix-artifact-name">{{ row.artifactName || '-' }}</span></template>
                    </el-table-column>
                    <el-table-column v-for="methodName in methods" :key="methodName" :label="methodName" min-width="112" align="center">
                        <template #default="{ row }"><el-tag :type="matrixCellType(methodStatus(row, methodName))" size="small" class="matrix-cell" @click="openResult(methodStatus(row, methodName))">{{ matrixCellText(methodStatus(row, methodName)) }}</el-tag></template>
                    </el-table-column>
                    <template #empty><el-empty description="暂无可展示的文物或检测方法" /></template>
                </el-table>
            </div>
        </el-card>

        <el-dialog v-model="addVisible" title="新增检测记录" width="720px" destroy-on-close :close-on-click-modal="false">
            <el-form :model="addForm" label-width="105px">
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="addForm.artifactCode" placeholder="可手动输入" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="addForm.artifactName" placeholder="可手动输入" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="出土来源"><el-input v-model="addForm.excavationRelic" placeholder="例如：M6/墓葬出土" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="addForm.sampleMaterial" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="addForm.samplePosition" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="样品状态"><el-select v-model="addForm.sampleStatus" style="width:100%"><el-option label="待检测" value="待检测" /><el-option label="检测中" value="检测中" /><el-option label="已完成" value="已完成" /></el-select></el-form-item></el-col>
                    <el-col :span="24"><el-form-item label="检测方法" required><el-select v-model="addForm.purposeList" multiple filterable allow-create default-first-option placeholder="选择或输入检测方法" style="width:100%"><el-option v-for="item in addMethodOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="管理人"><el-input v-model="addForm.manager" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="取样人"><el-input v-model="addForm.sampler" /></el-form-item></el-col>
                    <el-col :span="24"><el-form-item label="备注"><el-input v-model="addForm.notes" type="textarea" :rows="2" /></el-form-item></el-col>
                </el-row>
            </el-form>
            <template #footer><el-button @click="addVisible = false">取消</el-button><el-button type="primary" @click="submitAdd">保存</el-button></template>
        </el-dialog>

        <el-dialog v-model="detailVisible" title="文物检测详情" width="min(980px, 94vw)" destroy-on-close>
            <div v-loading="detailLoading" class="artifact-detail">
                <div class="detail-toolbar">
                    <span>{{ detailEditMode ? '编辑检测记录' : '检测记录详情' }}</span>
                    <el-button v-if="!detailEditMode" type="primary" @click="startArtifactDetailEdit">编辑检测记录</el-button>
                </div>
                <el-form v-if="detailEditMode" :model="methodEditorForm" label-width="105px">
                    <el-row :gutter="16">
                        <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="methodEditorForm.artifactCode" disabled /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="methodEditorForm.artifactName" disabled /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="出土来源"><el-input v-model="methodEditorForm.excavationRelic" disabled /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="methodEditorForm.sampleMaterial" disabled /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="methodEditorForm.samplePosition" /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="样品状态"><el-select v-model="methodEditorForm.sampleStatus" placeholder="请选择样品状态" clearable style="width:100%"><el-option label="待检测" value="待检测" /><el-option label="检测中" value="检测中" /><el-option label="已完成" value="已完成" /></el-select></el-form-item></el-col>
                        <el-col :span="24"><el-form-item label="检测方法" required><el-select v-model="methodEditorForm.methods" multiple filterable allow-create default-first-option placeholder="选择或输入检测方法" style="width:100%"><el-option v-for="item in methods" :key="item" :label="item" :value="item" /></el-select><div class="method-editor-tip">已有方法和结果会保留；新增方法会创建新的检测记录，移除方法会删除其对应的分析结果和实验结果。</div></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="管理人"><el-input v-model="methodEditorForm.manager" /></el-form-item></el-col>
                        <el-col :span="12"><el-form-item label="取样人"><el-input v-model="methodEditorForm.sampler" /></el-form-item></el-col>
                        <el-col :span="24"><el-form-item label="备注"><el-input v-model="methodEditorForm.notes" type="textarea" :rows="2" /></el-form-item></el-col>
                    </el-row>
                </el-form>
                <template v-else>
                <el-descriptions :column="2" border class="detail-summary">
                    <el-descriptions-item label="文物编号">{{ artifactDetail.artifactCode || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="文物名称">{{ artifactDetail.artifactName || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="出土来源">{{ artifactDetail.sourceType || '-' }}{{ artifactDetail.excavationRelic ? ` · ${artifactDetail.excavationRelic}` : '' }}</el-descriptions-item>
                    <el-descriptions-item label="样品材质">{{ artifactDetail.sampleMaterial || '-' }}</el-descriptions-item>
                </el-descriptions>

                <el-empty v-if="!detailLoading && !artifactDetail.records?.length" description="该文物暂无检测记录" :image-size="70" />
                <el-collapse v-else class="detail-records">
                    <el-collapse-item v-for="record in artifactDetail.records" :key="record.detection?.id" :name="record.detection?.id">
                        <template #title>
                            <span class="record-title">{{ record.detection?.purpose || '未填写检测方法' }}</span>
                            <el-tag size="small" :type="record.detection?.sampleStatus === '已完成' ? 'success' : record.detection?.sampleStatus === '检测中' ? 'warning' : 'info'">{{ record.detection?.sampleStatus || '待检测' }}</el-tag>
                        </template>
                        <el-descriptions :column="2" border size="small">
                            <el-descriptions-item label="取样部位">{{ record.detection?.samplePosition || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="样品状态">{{ record.detection?.sampleStatus || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="管理人">{{ record.detection?.manager || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="取样人">{{ record.detection?.sampler || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="创建时间">{{ record.detection?.createTime || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="更新时间">{{ record.detection?.updateTime || '-' }}</el-descriptions-item>
                            <el-descriptions-item label="备注" :span="2">{{ record.detection?.notes || '-' }}</el-descriptions-item>
                        </el-descriptions>

                        <section class="detail-section">
                            <h4>照片</h4>
                            <div v-if="recordPhotoUrls(record).length" class="photo-grid">
                                <el-image v-for="url in recordPhotoUrls(record)" :key="url" :src="url" :preview-src-list="recordPhotoUrls(record)" preview-teleported fit="cover" />
                            </div>
                            <el-empty v-else description="暂无样品或检测图片" :image-size="48" />
                        </section>

                        <section class="detail-section">
                            <h4>检测结果</h4>
                            <el-table v-if="record.experimentResults?.length" :data="record.experimentResults" size="small" border>
                                <el-table-column prop="experimentName" label="检测方法" min-width="170" />
                                <el-table-column prop="status" label="状态" width="110" />
                                <el-table-column prop="updateTime" label="更新时间" min-width="160"><template #default="{ row }">{{ row.updateTime || '-' }}</template></el-table-column>
                                <el-table-column prop="notes" label="结果备注" min-width="180"><template #default="{ row }">{{ row.notes || '-' }}</template></el-table-column>
                            </el-table>
                            <el-empty v-else description="暂无检测结果" :image-size="48" />
                        </section>
                    </el-collapse-item>
                </el-collapse>
                </template>
            </div>
            <template #footer>
                <el-button v-if="detailEditMode" @click="detailEditMode = false">取消编辑</el-button>
                <el-button v-else @click="detailVisible = false">关闭</el-button>
                <el-button v-if="detailEditMode" type="primary" :loading="methodSaving" @click="saveArtifactMethods">保存检测记录</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.detection-overview { padding: 2px 0 24px; }
.page-heading, .content-header { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; }
.page-heading { margin-bottom: 18px; }
.page-heading h2 { margin: 0 0 7px; font-size: 21px; color: #1d2129; }
.page-heading p { margin: 0; font-size: 14px; color: #86909c; }
.status-cards { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.status-cards :deep(.el-card__body) { display: grid; gap: 5px; padding: 15px 16px; }
.status-cards strong { font-size: 24px; color: #1d2129; }
.status-cards span { font-size: 12px; color: #86909c; }
.status-cards .in-progress strong { color: #d97706; }.status-cards .partial strong { color: #2563eb; }.status-cards .complete strong { color: #059669; }.status-cards .no-result strong { color: #dc2626; }
.filter-card, .content-card { border-color: #e5e6eb; margin-bottom: 16px; }
.filter-bar { display: grid; grid-template-columns: repeat(5, minmax(130px, 1fr)) minmax(220px, 1.3fr) auto; gap: 11px; }
.content-header { align-items: center; font-weight: 600; }
.relic-name { display: block; margin-top: 5px; color: #4e5969; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.method-tags { display: flex; flex-wrap: wrap; gap: 6px; }.method-tag, .matrix-cell { cursor: pointer; }.empty-value { color: #c9cdd4; }
.completeness-text { margin-top: 5px; color: #86909c; font-size: 12px; line-height: 1.35; }.pagination { margin-top: 16px; justify-content: flex-end; }
.method-editor-tip { margin-top: 7px; color: #86909c; font-size: 12px; line-height: 1.5; }
.artifact-detail { min-height: 180px; }.detail-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; color: #1d2129; font-weight: 600; }.detail-summary { margin-bottom: 16px; }.detail-records :deep(.el-collapse-item__header) { gap: 10px; }.record-title { font-weight: 600; color: #1d2129; }.detail-section { margin-top: 18px; }.detail-section h4 { margin: 0 0 10px; color: #303133; font-size: 14px; }.photo-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(132px, 1fr)); gap: 10px; }.photo-grid :deep(.el-image) { width: 100%; height: 112px; border-radius: 6px; border: 1px solid #e5e6eb; background: #f7f8fa; }
.matrix-wrap { overflow: auto; }.matrix-legend { display: flex; gap: 8px; margin-bottom: 12px; }.matrix-artifact-name { display: block; margin-top: 4px; color: #86909c; font-size: 12px; }
@media (max-width: 1280px) { .status-cards { grid-template-columns: repeat(3, minmax(0, 1fr)); }.filter-bar { grid-template-columns: repeat(3, minmax(150px, 1fr)); } }
@media (max-width: 760px) { .page-heading { align-items: flex-start; flex-direction: column; }.status-cards, .filter-bar { grid-template-columns: 1fr; } }
</style>
