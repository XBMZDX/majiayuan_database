<script setup>
import { Edit, Check, Delete, Plus, Search } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import * as XLSX from 'xlsx'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import request from '@/utils/request.js'
import StatisticsCards from '@/components/StatisticsCards.vue'
import { artifactsListService, artifactsAddService, artifactsDeleteService, artifactsUpdateService, artifactsBatchImportService, artifactsBatchDeleteService } from '@/api/Artifacts.js'

const tokenStore = useTokenStore()

// ========== 统计数据卡片 ==========
const stats = ref({ artifactCount: 0, siteCount: 0, relicCount: 0, detectionCount: 0 })
const fetchStats = async () => { try { const r = await request.get('/stats'); if (r.data) stats.value = r.data } catch (e) {} }

// ========== 材质级联选择器 ==========
const materialTree = ref([]) // 材质树数据，供 Cascader 使用

// 请求后端获取材质分类树
const fetchMaterialTree = async () => {
    const result = await request.get('/material-categories/tree')
    // 将后端树转换为 Element Plus Cascader 格式: {value, label, children}
    materialTree.value = convertToCascaderOptions(result.data)
}

// 递归转换树形数据 — value 统一使用 name，保证 Cascader 显示一致
const convertToCascaderOptions = (nodes) => {
    if (!nodes) return []
    return nodes.map(node => ({
        value: node.name,     // 所有层级统一用 name 做 value，避免显示 ID
        label: node.name,
        children: node.children ? convertToCascaderOptions(node.children) : undefined
    }))
}

// 根据 Cascader 选中的路径数组，拼接为存储字符串（如 "金属/金"、"人工硅酸盐/釉砂/铅白"）
const getMaterialPath = (cascaderValues) => {
    return cascaderValues ? cascaderValues.join('/') : ''
}

// 根据存储的材质路径字符串，反向查找 Cascader 所需的 value 数组（value 即 name，直接 split 即可）
const findCascaderPath = (pathStr) => {
    return pathStr ? pathStr.split('/') : []
}

// ========== 完整度级联选择器（与材质分类相同模式） ==========
const completenessTree = ref([]) // 完整度树数据

// 请求后端获取完整度分类树
const fetchCompletenessTree = async () => {
    const result = await request.get('/completeness-categories/tree')
    completenessTree.value = convertToCascaderOptions(result.data)
}

// 初始化加载所有树数据和筛选选项
onMounted(() => { fetchMaterialTree(); fetchCompletenessTree(); fetchFilterOptions(); fetchStats() })

//文物数据模型
const artifacts = ref([])

//分页条数据模型
const pageNum = ref(1)
const total = ref(20)
const pageSize = ref(10)

const onSizeChange = (size) => { pageSize.value = size; artifactsList() }
const onCurrentChange = (num) => { pageNum.value = num; artifactsList() }

//搜索条件
const searchParams = ref({ newArtifactName: '', newArtifactCode: '', material1: '', excavationRelic: '', completeness: '' })
const searchKeyword = ref('') // 模糊搜索关键词

// 下拉筛选器数据
const relicNames = ref([])          // 遗迹名称列表
const materialOptions = ref([])     // 材质选项（扁平化）
const completenessOptions = ref([]) // 完整度选项（扁平化）

// 扁平化树形数据为下拉选项（输入已是 {value, label, children} 格式）
const flattenTree = (nodes, prefix = '') => {
    let result = []
    for (const node of nodes) {
        const path = prefix ? prefix + ' / ' + node.label : node.label
        result.push({ value: node.value, label: path })
        if (node.children) {
            result = result.concat(flattenTree(node.children, path))
        }
    }
    return result
}

// 加载下拉数据：材质和完整度只取一级分类
const fetchFilterOptions = async () => {
    try {
        const [matRes, compRes, relicRes] = await Promise.all([
            request.get('/material-categories/tree'),
            request.get('/completeness-categories/tree'),
            request.get('/relics/names')
        ])
        // 只取一级分类名称，筛选时 LIKE 匹配会自动覆盖所有子级
        materialOptions.value = (matRes.data || []).map(n => ({ value: n.name, label: n.name }))
        completenessOptions.value = (compRes.data || []).map(n => ({ value: n.name, label: n.name }))
        relicNames.value = relicRes.data || []
    } catch (e) { /* ignore */ }
}

const artifactsList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        newArtifactName: searchParams.value.newArtifactName,
        newArtifactCode: searchParams.value.newArtifactCode,
        material1: searchParams.value.material1,
        excavationRelic: searchParams.value.excavationRelic,
        completeness: searchParams.value.completeness,
    }
    let result = await artifactsListService(params);
    artifacts.value = result.data.items;
    total.value = result.data.total;
}
artifactsList();

//筛选条件变化时自动刷新
const handleFilterChange = () => { pageNum.value = 1; artifactsList() }

//重置所有筛选条件
const handleReset = () => {
    searchParams.value = { newArtifactName: '', newArtifactCode: '', material1: '', excavationRelic: '', completeness: '' }
    searchKeyword.value = ''
    handleFilterChange()
}

// 搜索框回车触发
const handleSearchEnter = () => {
    // 关键词同时匹配名称和编号
    searchParams.value.newArtifactName = searchKeyword.value
    searchParams.value.newArtifactCode = searchKeyword.value
    handleFilterChange()
}

//控制抽屉
const visibleDrawer = ref(false)

//添加表单
const getDefaultForm = () => ({
    serialNumber: null,
    newArtifactCode: '',
    newArtifactName: '',
    originalArtifactCode: '',
    originalArtifactName: '',
    material1: '',
    material2: '',
    completeness: '',
    artifactDescription: '',
    quantity1: null,
    quantity2: null,
    dimensions: '',
    weight: '',
    excavationRelic: '',
    excavationPosition: '',
    excavationTime: '',
    storageMethod: '',
    storageLocation: '',
    images: '',
    transferProcess: '',
    restorationStatus: '',
    photographer: '',
    draftsperson: '',
    textDescriber: '',
    notes: '',
    gradingStatus: '',
    testingStatus: ''
})

const ArtifactsModel = ref(getDefaultForm())

// 添加表单的级联选择值（独立于 Model，保存时再转换路径）
const addMaterial1Cascader = ref([])
const addCompletenessCascader = ref([])

const resetForm = () => {
    ArtifactsModel.value = getDefaultForm()
    addMaterial1Cascader.value = []
    addCompletenessCascader.value = []
}

const uploadSuccess = (result) => { ArtifactsModel.value.images = result.data; }

//添加文物
const artifactsAdd = async () => {
    let result = await artifactsAddService(ArtifactsModel.value)
    ElMessage.success(result.message || '添加成功')
    resetForm()
    visibleDrawer.value = false
    artifactsList()
}

//删除文物
const deleteArtifact = (row) => {
    ElMessageBox.confirm('你确认删除该文物吗？', '温馨提示', {
        confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning',
    }).then(async () => {
        await artifactsDeleteService(row.id)
        ElMessage({ type: 'success', message: '成功删除' })
        artifactsList()
    }).catch(() => { ElMessage({ type: 'info', message: '已取消' }) })
}

// ========== 批量删除 ==========
const batchMode = ref(false)               // 是否处于批量删除模式
const selectedRows = ref([])               // 已选中的行数据
const selectionRef = ref(null)             // el-table 的 ref

// 进入批量删除模式
const enterBatchMode = () => { batchMode.value = true; selectedRows.value = [] }

// 取消批量删除模式
const cancelBatchMode = () => { batchMode.value = false; selectedRows.value = [] }

// 多选变化回调
const handleSelectionChange = (rows) => { selectedRows.value = rows }

// 确认批量删除
const confirmBatchDelete = () => {
    if (selectedRows.value.length === 0) { ElMessage.warning('请至少选择一件文物'); return }
    const count = selectedRows.value.length
    ElMessageBox.confirm(
        `确定要删除选中的 ${count} 件文物吗？此操作不可恢复。`,
        '批量删除确认',
        { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
        const ids = selectedRows.value.map(r => r.id)
        await artifactsBatchDeleteService(ids)
        ElMessage({ type: 'success', message: `成功删除 ${count} 件文物` })
        cancelBatchMode()
        artifactsList()
    }).catch(() => { ElMessage({ type: 'info', message: '已取消' }) })
}

//编辑
const visibleEditDrawer = ref(false)
const editData = ref({})
const editMaterial1Cascader = ref([])
const editCompletenessCascader = ref([])
const openEdit = (row) => {
    editData.value = { ...row }
    // 将存储的路径字符串反向转换为 Cascader 选中值
    editMaterial1Cascader.value = findCascaderPath(row.material1 || '')
    editCompletenessCascader.value = findCascaderPath(row.completeness || '')
    visibleEditDrawer.value = true
}

const updateArtifact = async () => {
    // 将 Cascader 选择转换为路径字符串存储
    let result = await artifactsUpdateService(editData.value)
    ElMessage.success(result.message || '编辑成功')
    artifactsList()
    visibleEditDrawer.value = false
}

//详情
const visibleDetailDrawer = ref(false)
const detailData = ref({})
const detailEditMode = ref(false)           // 详情页编辑模式开关
const detailMaterialCascader = ref([])      // 详情页材质级联选择值
const detailCompletenessCascader = ref([]) // 详情页完整度级联选择值
const detailBackup = ref({})                // 编辑前的原始数据备份

// 打开详情（只读模式）
const openDetail = (row) => {
    detailData.value = { ...row }
    detailEditMode.value = false
    visibleDetailDrawer.value = true
}

// 进入编辑模式（备份当前数据，初始化级联选择器）
const enterDetailEditMode = () => {
    detailBackup.value = { ...detailData.value }
    detailMaterialCascader.value = findCascaderPath(detailData.value.material1 || '')
    detailCompletenessCascader.value = findCascaderPath(detailData.value.completeness || '')
    detailEditMode.value = true
}

// 保存编辑
const saveDetailEdit = async () => {
    // 将 Cascader 选择转换为路径字符串
    // 调用更新接口
    let result = await artifactsUpdateService(detailData.value)
    ElMessage.success(result.message || '保存成功')
    detailEditMode.value = false
    artifactsList()
}

// 取消编辑（恢复备份数据）
const cancelDetailEdit = () => {
    detailData.value = { ...detailBackup.value }
    detailEditMode.value = false
}

//导入
const visibleImportDrawer = ref(false)
const importFile = ref(null)
const importResult = ref({})
const importing = ref(false)

const handleFileChange = (file) => { importFile.value = file.raw; importResult.value = {} }

// 下载导入模板
const downloadTemplate = () => {
    const headers = ['序号', '文物新编号', '文物新名称', '文物原始编号', '文物原名称',
        '材质1', '材质2', '完整度', '文物描述2', '数量1', '数量2',
        '尺寸', '重量', '出土遗迹', '出土位置', '出土时间',
        '存放方式', '存放地点', '图片', '文物流转过程', '修复、复原状况',
        '拍照人', '绘图人', '文字描述人', '备注', '定级情况', '科技检测情况']
    const ws = XLSX.utils.aoa_to_sheet([headers])
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '文物导入模板')
    XLSX.writeFile(wb, '文物导入模板.xlsx')
}

const handleImport = async () => {
    if (!importFile.value) { ElMessage.warning('请选择Excel文件'); return }
    importing.value = true
    importResult.value = {}
    try {
        const fileReader = new FileReader()
        fileReader.onload = async (e) => {
            try {
                const data = new Uint8Array(e.target.result)
                const workbook = XLSX.read(data, { type: 'array' })
                const worksheet = workbook.Sheets[workbook.SheetNames[0]]
                const jsonData = XLSX.utils.sheet_to_json(worksheet)
                if (jsonData.length === 0) {
                    importing.value = false
                    ElMessage.warning('Excel文件中没有数据')
                    return
                }
                const importData = jsonData.map(item => ({
                    serialNumber: item['序号'] || null,
                    newArtifactCode: item['文物\\n新编号\\n'] || item['文物新编号'] || '',
                    newArtifactName: item['文物\\n新名称'] || item['文物新名称'] || '',
                    originalArtifactCode: item['文物\\n原始编号\\n'] || item['文物原始编号'] || '',
                    originalArtifactName: item['文物\\n原名称'] || item['文物原名称'] || '',
                    material1: item['材质1'] || '',
                    material2: item['材质2'] || '',
                    completeness: item['完整度\\n'] || item['完整度'] || '',
                    artifactDescription: item['文物描述2\\n'] || item['文物描述2'] || '',
                    quantity1: item['数量1\\n'] || item['数量1'] || null,
                    quantity2: item['数量2\\n'] || item['数量2'] || null,
                    dimensions: item['尺寸'] || '',
                    weight: item['重量'] || '',
                    excavationRelic: item['出土遗迹'] || '',
                    excavationPosition: item['出土位置\\n'] || item['出土位置'] || '',
                    excavationTime: item['出土时间\\n'] || item['出土时间'] || '',
                    storageMethod: item['存放方式\\n'] || item['存放方式'] || '',
                    storageLocation: item['存放地点'] || '',
                    images: item['图片\\n'] || item['图片'] || '',
                    transferProcess: item['文物流转过程\\n'] || item['文物流转过程'] || '',
                    restorationStatus: item['修复、\\n复原状况\\n'] || item['修复、复原状况'] || '',
                    photographer: item['拍照人'] || '',
                    draftsperson: item['绘图人'] || '',
                    textDescriber: item['文字描述人'] || '',
                    notes: item['备注\\n（存在问题或其它说明）'] || item['备注'] || '',
                    gradingStatus: item['定级情况'] || '',
                    testingStatus: item['科技检测\\n情况'] || item['科技检测情况'] || ''
                }))
                const validData = importData.filter(item => item.newArtifactCode && item.newArtifactName)
                if (validData.length === 0) { importing.value = false; ElMessage.warning('没有有效数据'); return }
                const result = await artifactsBatchImportService(validData)
                importing.value = false
                importResult.value = { success: true, message: `导入成功，共${validData.length}条` }
                ElMessage.success(result.message || '导入成功')
                artifactsList()
            } catch (error) { importing.value = false; ElMessage.error('Excel解析失败') }
        }
        fileReader.onerror = () => { importing.value = false; ElMessage.error('文件读取失败') }
        fileReader.readAsArrayBuffer(importFile.value)
    } catch (error) { importing.value = false; ElMessage.error('导入失败') }
}
</script>

<template>
    <!-- 数据统计卡片 -->
    <StatisticsCards
        :artifact-count="stats.artifactCount"
        :site-count="stats.siteCount"
        :relic-count="stats.relicCount"
        :detection-count="stats.detectionCount"
    />

    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>文物信息总览</span>
                <div class="extra">
                    <el-button type="primary" @click="visibleDrawer = true">添加文物</el-button>
                    <el-button type="success" @click="visibleImportDrawer = true">导入文物</el-button>
                    <el-button type="danger" @click="enterBatchMode" :disabled="batchMode">批量删除</el-button>
                </div>
            </div>
        </template>

        <!-- 筛选栏：flex 横向排布 -->
        <div class="filter-bar">
            <!-- 材质筛选器 -->
            <div class="filter-item">
                <el-select v-model="searchParams.material1" placeholder="材质" clearable @change="handleFilterChange" class="filter-select">
                    <el-option v-for="item in materialOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </div>
            <!-- 遗迹筛选器 -->
            <div class="filter-item">
                <el-select v-model="searchParams.excavationRelic" placeholder="遗迹" clearable @change="handleFilterChange" class="filter-select">
                    <el-option v-for="name in relicNames" :key="name" :label="name" :value="name" />
                </el-select>
            </div>
            <!-- 完整度筛选器 -->
            <div class="filter-item">
                <el-select v-model="searchParams.completeness" placeholder="完整度" clearable @change="handleFilterChange" class="filter-select">
                    <el-option v-for="item in completenessOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </div>
            <!-- 模糊搜索框 -->
            <div class="filter-search">
                <el-input v-model="searchKeyword" placeholder="搜索文物名称/编号" clearable @keyup.enter="handleSearchEnter" @clear="handleSearchEnter" :prefix-icon="Search" />
            </div>
            <!-- 重置按钮 -->
            <el-button @click="handleReset" class="filter-reset">重置</el-button>
        </div>

        <!-- 批量删除操作栏 -->
        <div v-if="batchMode" class="batch-bar">
            <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 件文物</span>
            <div>
                <el-button type="danger" @click="confirmBatchDelete">确认删除</el-button>
                <el-button @click="cancelBatchMode">取消</el-button>
            </div>
        </div>

        <!-- 文物列表 -->
        <el-table :data="artifacts" style="width: 100%" @selection-change="handleSelectionChange" ref="selectionRef">
            <el-table-column v-if="batchMode" type="selection" width="50" />
            <el-table-column label="序号" prop="serialNumber" width="60" />
            <el-table-column label="文物编号" width="140">
                <template #default="{ row }">{{ row.newArtifactCode || row.originalArtifactCode || '—' }}</template>
            </el-table-column>
            <el-table-column label="文物名称" width="160">
                <template #default="{ row }">{{ row.newArtifactName || row.originalArtifactName || '—' }}</template>
            </el-table-column>
            <el-table-column label="出土遗迹" prop="excavationRelic" width="140" />
            <el-table-column label="出土位置" prop="excavationPosition" width="140" />
            <el-table-column label="材质" prop="material1" width="120" />
            <el-table-column label="完整度" prop="completeness" width="80" />
            <el-table-column label="数量" prop="quantity1" width="70" />
            <el-table-column label="存放地点" prop="storageLocation" width="120" />
            <el-table-column label="科技检测情况" prop="testingStatus" width="140" />
            <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                    <el-link type="primary" @click="openDetail(row)">详情</el-link>
                </template>
            </el-table-column>
            <template #empty>
                <el-empty description="没有数据" />
            </template>
        </el-table>

        <!-- 分页条 -->
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[5, 10, 20, 50]"
            layout="jumper, total, sizes, prev, pager, next" background :total="total" @size-change="onSizeChange"
            @current-change="onCurrentChange" style="margin-top:20px;justify-content:flex-end" />
    </el-card>

    <!-- 添加文物抽屉 -->
    <el-drawer v-model="visibleDrawer" title="添加文物" direction="rtl" size="55%">
        <el-form :model="ArtifactsModel" label-width="110px">
            <el-row :gutter="20">
                <el-col :span="12">
                    <el-form-item label="文物新编号"><el-input v-model="ArtifactsModel.newArtifactCode" placeholder="请输入新编号" /></el-form-item>
                    <el-form-item label="文物新名称"><el-input v-model="ArtifactsModel.newArtifactName" placeholder="请输入新名称" /></el-form-item>
                    <el-form-item label="原始编号"><el-input v-model="ArtifactsModel.originalArtifactCode" placeholder="请输入原始编号" /></el-form-item>
                    <el-form-item label="原名称"><el-input v-model="ArtifactsModel.originalArtifactName" placeholder="请输入原名称" /></el-form-item>
                    <el-form-item label="材质1">
                        <el-input v-model="ArtifactsModel.material1" placeholder="请输入材质" />
                    </el-form-item>
                    <el-form-item label="材质2"><el-input v-model="ArtifactsModel.material2" placeholder="请输入材质2" /></el-form-item>
                    <el-form-item label="完整度">
                        <el-input v-model="ArtifactsModel.completeness" placeholder="请输入完整度" />
                    </el-form-item>
                    <el-form-item label="数量1"><el-input v-model="ArtifactsModel.quantity1" placeholder="请输入数量1" /></el-form-item>
                    <el-form-item label="数量2"><el-input v-model="ArtifactsModel.quantity2" placeholder="请输入数量2" /></el-form-item>
                    <el-form-item label="尺寸"><el-input v-model="ArtifactsModel.dimensions" placeholder="请输入尺寸" /></el-form-item>
                    <el-form-item label="重量"><el-input v-model="ArtifactsModel.weight" placeholder="请输入重量" /></el-form-item>
                    <el-form-item label="出土遗迹"><el-input v-model="ArtifactsModel.excavationRelic" placeholder="请输入出土遗迹" /></el-form-item>
                    <el-form-item label="出土位置"><el-input v-model="ArtifactsModel.excavationPosition" placeholder="请输入出土位置" /></el-form-item>
                    <el-form-item label="出土时间"><el-input v-model="ArtifactsModel.excavationTime" placeholder="请输入出土时间" /></el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="存放方式"><el-input v-model="ArtifactsModel.storageMethod" placeholder="请输入存放方式" /></el-form-item>
                    <el-form-item label="存放地点"><el-input v-model="ArtifactsModel.storageLocation" placeholder="请输入存放地点" /></el-form-item>
                    <el-form-item label="文物流转过程"><el-input v-model="ArtifactsModel.transferProcess" type="textarea" :rows="2" placeholder="请输入流转过程" /></el-form-item>
                    <el-form-item label="修复复原状况"><el-input v-model="ArtifactsModel.restorationStatus" placeholder="请输入修复状况" /></el-form-item>
                    <el-form-item label="拍照人"><el-input v-model="ArtifactsModel.photographer" placeholder="请输入拍照人" /></el-form-item>
                    <el-form-item label="绘图人"><el-input v-model="ArtifactsModel.draftsperson" placeholder="请输入绘图人" /></el-form-item>
                    <el-form-item label="文字描述人"><el-input v-model="ArtifactsModel.textDescriber" placeholder="请输入文字描述人" /></el-form-item>
                    <el-form-item label="定级情况"><el-input v-model="ArtifactsModel.gradingStatus" placeholder="请输入定级情况" /></el-form-item>
                    <el-form-item label="科技检测情况"><el-input v-model="ArtifactsModel.testingStatus" placeholder="请输入检测情况" /></el-form-item>
                    <el-form-item label="备注"><el-input v-model="ArtifactsModel.notes" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
                    <el-form-item label="文物描述">
                        <div class="editor"><quill-editor theme="snow" v-model:content="ArtifactsModel.artifactDescription" contentType="html" /></div>
                    </el-form-item>
                    <el-form-item label="图片">
                        <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                            name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="uploadSuccess">
                            <img v-if="ArtifactsModel.images" :src="ArtifactsModel.images" class="avatar" />
                            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                        </el-upload>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item>
                <el-button type="primary" @click="artifactsAdd">发布</el-button>
                <el-button type="info" @click="visibleDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>

    <!-- 编辑文物抽屉 -->
    <el-drawer v-model="visibleEditDrawer" title="编辑文物" direction="rtl" size="55%">
        <el-form :model="editData" label-width="110px">
            <el-row :gutter="20">
                <el-col :span="12">
                    <el-form-item label="文物新编号"><el-input v-model="editData.newArtifactCode" /></el-form-item>
                    <el-form-item label="文物新名称"><el-input v-model="editData.newArtifactName" /></el-form-item>
                    <el-form-item label="原始编号"><el-input v-model="editData.originalArtifactCode" /></el-form-item>
                    <el-form-item label="原名称"><el-input v-model="editData.originalArtifactName" /></el-form-item>
                    <el-form-item label="材质1">
                        <el-input v-model="editData.material1" />
                    </el-form-item>
                    <el-form-item label="材质2"><el-input v-model="editData.material2" /></el-form-item>
                    <el-form-item label="完整度">
                        <el-input v-model="editData.completeness" />
                    </el-form-item>
                    <el-form-item label="数量1"><el-input v-model="editData.quantity1" /></el-form-item>
                    <el-form-item label="数量2"><el-input v-model="editData.quantity2" /></el-form-item>
                    <el-form-item label="尺寸"><el-input v-model="editData.dimensions" /></el-form-item>
                    <el-form-item label="重量"><el-input v-model="editData.weight" /></el-form-item>
                    <el-form-item label="出土遗迹"><el-input v-model="editData.excavationRelic" /></el-form-item>
                    <el-form-item label="出土位置"><el-input v-model="editData.excavationPosition" /></el-form-item>
                    <el-form-item label="出土时间"><el-input v-model="editData.excavationTime" /></el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="存放方式"><el-input v-model="editData.storageMethod" /></el-form-item>
                    <el-form-item label="存放地点"><el-input v-model="editData.storageLocation" /></el-form-item>
                    <el-form-item label="文物流转过程"><el-input v-model="editData.transferProcess" type="textarea" :rows="2" /></el-form-item>
                    <el-form-item label="修复复原状况"><el-input v-model="editData.restorationStatus" /></el-form-item>
                    <el-form-item label="拍照人"><el-input v-model="editData.photographer" /></el-form-item>
                    <el-form-item label="绘图人"><el-input v-model="editData.draftsperson" /></el-form-item>
                    <el-form-item label="文字描述人"><el-input v-model="editData.textDescriber" /></el-form-item>
                    <el-form-item label="定级情况"><el-input v-model="editData.gradingStatus" /></el-form-item>
                    <el-form-item label="科技检测情况"><el-input v-model="editData.testingStatus" /></el-form-item>
                    <el-form-item label="备注"><el-input v-model="editData.notes" type="textarea" :rows="2" /></el-form-item>
                    <el-form-item label="文物描述">
                        <div class="editor"><quill-editor :key="editData.id || 'new'" theme="snow" v-model:content="editData.artifactDescription" contentType="html" /></div>
                    </el-form-item>
                    <el-form-item label="图片">
                        <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                            name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="(res) => { editData.images = res.data }">
                            <img v-if="editData.images" :src="editData.images" class="avatar" />
                            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                        </el-upload>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item>
                <el-button type="primary" @click="updateArtifact">更新</el-button>
                <el-button type="info" @click="visibleEditDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>

    <!-- 文物详情抽屉（双模式：只读 / 编辑） -->
    <el-drawer v-model="visibleDetailDrawer" :title="detailEditMode ? '编辑文物' : '文物详情'" direction="rtl" size="55%">
        <!-- 只读模式：描述列表 -->
        <template v-if="!detailEditMode">
            <el-descriptions :column="2" border>
                <el-descriptions-item label="序号">{{ detailData.serialNumber ?? '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物新编号">{{ detailData.newArtifactCode || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物新名称">{{ detailData.newArtifactName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物原始编号">{{ detailData.originalArtifactCode || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物原名称">{{ detailData.originalArtifactName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="材质1">{{ detailData.material1 || '—' }}</el-descriptions-item>
                <el-descriptions-item label="材质2">{{ detailData.material2 || '—' }}</el-descriptions-item>
                <el-descriptions-item label="完整度">{{ detailData.completeness || '—' }}</el-descriptions-item>
                <el-descriptions-item label="数量1">{{ detailData.quantity1 ?? '—' }}</el-descriptions-item>
                <el-descriptions-item label="数量2">{{ detailData.quantity2 ?? '—' }}</el-descriptions-item>
                <el-descriptions-item label="尺寸">{{ detailData.dimensions || '—' }}</el-descriptions-item>
                <el-descriptions-item label="重量">{{ detailData.weight || '—' }}</el-descriptions-item>
                <el-descriptions-item label="出土遗迹">{{ detailData.excavationRelic || '—' }}</el-descriptions-item>
                <el-descriptions-item label="出土位置">{{ detailData.excavationPosition || '—' }}</el-descriptions-item>
                <el-descriptions-item label="出土时间">{{ detailData.excavationTime || '—' }}</el-descriptions-item>
                <el-descriptions-item label="存放方式">{{ detailData.storageMethod || '—' }}</el-descriptions-item>
                <el-descriptions-item label="存放地点">{{ detailData.storageLocation || '—' }}</el-descriptions-item>
                <el-descriptions-item label="拍照人">{{ detailData.photographer || '—' }}</el-descriptions-item>
                <el-descriptions-item label="绘图人">{{ detailData.draftsperson || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文字描述人">{{ detailData.textDescriber || '—' }}</el-descriptions-item>
                <el-descriptions-item label="定级情况">{{ detailData.gradingStatus || '—' }}</el-descriptions-item>
                <el-descriptions-item label="科技检测情况">{{ detailData.testingStatus || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物流转过程" :span="2">{{ detailData.transferProcess || '—' }}</el-descriptions-item>
                <el-descriptions-item label="修复复原状况" :span="2">{{ detailData.restorationStatus || '—' }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文物描述" :span="2"><div v-html="detailData.artifactDescription || '—'" /></el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ detailData.createTime || '—' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ detailData.updateTime || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top:20px;text-align:right">
                <el-button type="primary" @click="enterDetailEditMode">编辑</el-button>
            </div>
        </template>

        <!-- 编辑模式：可编辑表单 -->
        <template v-else>
            <el-form :model="detailData" label-width="110px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="文物新编号"><el-input v-model="detailData.newArtifactCode" /></el-form-item>
                        <el-form-item label="文物新名称"><el-input v-model="detailData.newArtifactName" /></el-form-item>
                        <el-form-item label="原始编号"><el-input v-model="detailData.originalArtifactCode" /></el-form-item>
                        <el-form-item label="原名称"><el-input v-model="detailData.originalArtifactName" /></el-form-item>
                        <el-form-item label="材质">
                            <el-input v-model="detailData.material1" />
                        </el-form-item>
                        <el-form-item label="材质2"><el-input v-model="detailData.material2" /></el-form-item>
                        <el-form-item label="完整度">
                            <el-input v-model="detailData.completeness" />
                        </el-form-item>
                        <el-form-item label="数量1"><el-input v-model="detailData.quantity1" /></el-form-item>
                        <el-form-item label="数量2"><el-input v-model="detailData.quantity2" /></el-form-item>
                        <el-form-item label="尺寸"><el-input v-model="detailData.dimensions" /></el-form-item>
                        <el-form-item label="重量"><el-input v-model="detailData.weight" /></el-form-item>
                        <el-form-item label="出土遗迹"><el-input v-model="detailData.excavationRelic" /></el-form-item>
                        <el-form-item label="出土位置"><el-input v-model="detailData.excavationPosition" /></el-form-item>
                        <el-form-item label="出土时间"><el-input v-model="detailData.excavationTime" /></el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="存放方式"><el-input v-model="detailData.storageMethod" /></el-form-item>
                        <el-form-item label="存放地点"><el-input v-model="detailData.storageLocation" /></el-form-item>
                        <el-form-item label="文物流转过程"><el-input v-model="detailData.transferProcess" type="textarea" :rows="2" /></el-form-item>
                        <el-form-item label="修复复原状况"><el-input v-model="detailData.restorationStatus" /></el-form-item>
                        <el-form-item label="拍照人"><el-input v-model="detailData.photographer" /></el-form-item>
                        <el-form-item label="绘图人"><el-input v-model="detailData.draftsperson" /></el-form-item>
                        <el-form-item label="文字描述人"><el-input v-model="detailData.textDescriber" /></el-form-item>
                        <el-form-item label="定级情况"><el-input v-model="detailData.gradingStatus" /></el-form-item>
                        <el-form-item label="科技检测情况"><el-input v-model="detailData.testingStatus" /></el-form-item>
                        <el-form-item label="备注"><el-input v-model="detailData.notes" type="textarea" :rows="2" /></el-form-item>
                        <el-form-item label="文物描述">
                            <div class="editor"><quill-editor :key="'detail-' + detailData.id" theme="snow" v-model:content="detailData.artifactDescription" contentType="html" /></div>
                        </el-form-item>
                        <el-form-item label="图片">
                            <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                                name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="(res) => { detailData.images = res.data }">
                                <img v-if="detailData.images" :src="detailData.images" class="avatar" />
                                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                            </el-upload>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <div style="margin-top:20px;text-align:right">
                <el-button type="primary" @click="saveDetailEdit">保存</el-button>
                <el-button @click="cancelDetailEdit">取消</el-button>
            </div>
        </template>
    </el-drawer>

    <!-- 导入文物抽屉 -->
    <el-drawer v-model="visibleImportDrawer" title="导入文物" direction="rtl" size="50%">
        <el-form label-width="100px">
            <el-form-item>
                <el-button type="success" @click="downloadTemplate">获取模板</el-button>
            </el-form-item>
            <el-form-item label="Excel文件">
                <el-upload class="upload-demo" action="#" :auto-upload="false" :on-change="handleFileChange"
                    :show-file-list="true" accept=".xlsx,.xls" :limit="1">
                    <el-button type="primary">选择文件</el-button>
                    <template #tip><div class="el-upload__tip">请上传Excel文件（.xlsx或.xls格式），表头需匹配模板</div></template>
                </el-upload>
            </el-form-item>
            <el-form-item v-if="importResult.success !== undefined">
                <el-alert :title="importResult.success ? '导入成功' : '导入失败'" :type="importResult.success ? 'success' : 'error'"
                    :description="importResult.message" show-icon :closable="true" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleImport" :loading="importing">开始导入</el-button>
                <el-button @click="visibleImportDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>
</template>

<style lang="scss" scoped>
.page-container {
    min-height: 100%;
    box-sizing: border-box;
    .header { display: flex; align-items: center; justify-content: space-between; }
}
/* 筛选栏：flex 横向均匀分布 */
.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { flex: 1; min-width: 120px; }
.filter-select { width: 100%; --el-fill-color-blank: #F2F3F5; --el-border-radius-base: 6px; }
.filter-select :deep(.el-input__wrapper) { background-color: #F2F3F5; border-radius: 6px; padding: 6px 12px; box-shadow: none; }
.filter-select :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px var(--el-color-primary) inset; }
.filter-search { flex: 1.5; min-width: 200px; }
.filter-reset { flex-shrink: 0; }
.avatar-uploader {
    :deep() {
        .avatar { width: 178px; height: 178px; display: block; }
        .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; overflow: hidden; }
        .el-upload:hover { border-color: var(--el-color-primary); }
        .el-icon.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 178px; height: 178px; text-align: center; }
    }
}
.editor { width: 100%; :deep(.ql-editor) { min-height: 150px; } }
.batch-bar { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; margin-bottom: 10px; background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; .batch-info { color: #e64242; font-size: 14px; } }
</style>