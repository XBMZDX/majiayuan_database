<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { useTokenStore } from '@/stores/token.js'
const token = computed(() => useTokenStore().token)

const list = ref([])
const stats = ref({ total: 0, relicDistribution: [], purposeDistribution: [] })

const calcStats = (data) => {
    const relicMap = {}; const purposeMap = {}
    data.forEach(d => {
        const r = d.excavationRelic || '未知'; relicMap[r] = (relicMap[r] || 0) + 1
        // 统计时拆分 / 分隔的实验项目
        const parts = (d.purpose || '未知').split('/')
        parts.forEach(p => { const t = p.trim(); if (t) purposeMap[t] = (purposeMap[t] || 0) + 1 })
    })
    return {
        total: data.length,
        relicDistribution: Object.entries(relicMap).map(([n,c])=>({name:n,count:c})),
        purposeDistribution: Object.entries(purposeMap).map(([n,c])=>({name:n,count:c}))
    }
}

const fetchList = async () => {
    const res = await request.get('/admin/detection')
    list.value = res.data || []
    stats.value = calcStats(list.value)
    pageNum.value = 1
}

const pageNum = ref(1); const pageSize = ref(10)
const paged = computed(() => list.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))

// 实验分类（须在 purposeOptions 之前定义）
// ========== 目的：从检测实验总览加载 ==========
const labInstruments = ref([])
const fetchLabInstruments = async () => { const res = await request.get('/admin/lab-instrument'); labInstruments.value = res.data || [] }
const purposeOptions = computed(() => labInstruments.value.map(i => ({ label: i.name, value: i.name })))

const searchParams = ref({ artifactCode: '', artifactName: '', sampleMaterial: '', purpose: '' })
const filterCascader = ref([])
// 材质下拉选项（与文物信息总览保持一致的一级材质）
const materialOptions = [
    { label: '金属', value: '金属' }, { label: '人工硅酸盐', value: '人工硅酸盐' },
    { label: '玉石', value: '玉石' }, { label: '陶制', value: '陶制' },
    { label: '动物源有机质', value: '动物源有机质' }, { label: '植物源有机质', value: '植物源有机质' },
    { label: '漆器', value: '漆器' }
]

const filtered = computed(() => list.value.filter(r =>
    (!searchParams.value.artifactCode || (r.artifactCode||'').includes(searchParams.value.artifactCode)) &&
    (!searchParams.value.artifactName || (r.artifactName||'').includes(searchParams.value.artifactName)) &&
    (!filterCascader.value.length || (r.excavationRelic||'').includes(getPath(filterCascader.value))) &&
    (!searchParams.value.sampleMaterial || (r.sampleMaterial||'').includes(searchParams.value.sampleMaterial)) &&
    (!searchParams.value.purpose || (r.purpose||'').includes(searchParams.value.purpose))
))
const pagedFiltered = computed(() => filtered.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const handleSearch = () => { pageNum.value = 1 }
const handleReset = () => { searchParams.value = { artifactCode: '', artifactName: '', sampleMaterial: '', purpose: '' }; filterCascader.value = []; pageNum.value = 1 }

const detailVisible = ref(false); const detailData = ref({})
const detailEditMode = ref(false); const detailBackup = ref({})
const openDetail = (row) => { detailData.value = { ...row }; detailEditMode.value = false; detailVisible.value = true; detailCascader.value = [] }
const enterDetailEditMode = () => { detailBackup.value = { ...detailData.value }; detailEditMode.value = true; detailCascader.value = parsePath(detailData.value.excavationRelic || '') }
const cancelDetailEdit = () => { detailData.value = { ...detailBackup.value }; detailEditMode.value = false }
const saveDetailEdit = async () => { detailData.value.excavationRelic = getPath(detailCascader.value); await request.put('/admin/detection/' + detailData.value.id, detailData.value); ElMessage.success('保存成功'); detailEditMode.value = false; fetchList() }

// 照片上传（与检测实验总览完全一致）
const onDetailPhoto = (res) => { detailData.value.samplePhoto = res.data }
const onAddPhoto = (res) => { addForm.value.samplePhoto = res.data }
const onEditPhoto = (res) => { editData.value.samplePhoto = res.data }

const del = (row) => { ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }).then(async () => { await request.delete('/admin/detection/' + row.id); ElMessage.success('删除成功'); fetchList() }).catch(() => {}) }

const batchMode = ref(false); const selectedRows = ref([])
const enterBatchMode = () => { batchMode.value = true; selectedRows.value = [] }
const cancelBatchMode = () => { batchMode.value = false; selectedRows.value = [] }
const confirmBatchDelete = () => {
    if (!selectedRows.value.length) { ElMessage.warning('请至少选择一条'); return }
    ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条？`, '批量删除', { confirmButtonText:'确认删除', cancelButtonText:'取消', type:'warning' })
        .then(async () => { await request.post('/admin/detection/batch-delete', selectedRows.value.map(r=>r.id)); ElMessage.success('删除成功'); cancelBatchMode(); fetchList() }).catch(()=>{})
}

// 批量导入
import * as XLSX from 'xlsx'
const importVisible = ref(false); const uploadRef = ref(null); const importFile = ref(null)
// Excel日期序列号 → yyyy/MM/dd 字符串
const excelDateToStr = (val) => {
    if (!val) return ''
    if (typeof val === 'number') { const d = new Date((val - 25569) * 86400 * 1000); return d.getFullYear() + '/' + String(d.getMonth()+1).padStart(2,'0') + '/' + String(d.getDate()).padStart(2,'0') }
    return String(val)
}

const handleImportFile = (file) => { importFile.value = file.raw }
const handleImport = async () => {
    if (!importFile.value) { ElMessage.warning('请选择文件'); return }
    const reader = new FileReader()
    reader.onload = async (e) => {
        const wb = XLSX.read(new Uint8Array(e.target.result), {type:'array'})
        const data = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        // 序号自动追加到现有数据之后
        let nextSn = list.value.length + 1
        const importData = data.map(item => ({
            serialNumber: String(nextSn++),
            artifactCode: item['文物编号']||'', artifactName: item['文物名称']||'',
            excavationRelic: item['出土遗迹']||'', samplePosition: item['取样部位']||'',
            sampleMaterial: item['样品材质']||'', sampleStatus: item['样品状态']||'',
            sampleQuantity: item['样品数量']||'', sampleMethod: item['取样方法']||'',
            purpose: item['目的']||'', storageLocation: item['存放位置']||'',
            departureTime: excelDateToStr(item['出库时间']), destination: item['去处']||'',
            samplePhoto: item['取样照片']||'', analysisData: item['目的']||'', analysisReport: item['目的']||'', manager: item['文物管理人']||'',
            sampler: item['取样人']||'', notes: item['备注']||''
        }))
        for (const d of importData) await request.post('/admin/detection', d)
        ElMessage.success('导入成功'); importFile.value = null; uploadRef.value?.clearFiles(); importVisible.value = false; fetchList()
    }; reader.readAsArrayBuffer(importFile.value)
}
const downloadTemplate = () => {
    const h = ['序号','文物编号','文物名称','出土遗迹','取样部位','样品材质','样品状态','样品数量','取样方法','目的','存放位置','出库时间','去处','取样照片','文物管理人','取样人','备注']
    const ws = XLSX.utils.aoa_to_sheet([h]); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb,ws,'模板'); XLSX.writeFile(wb,'检测导入模板.xlsx')
}

// ========== 出土遗迹级联选择器 ==========
const burialData = ref([])  // 全部墓葬数据
const cascaderOptions = computed(() => burialData.value.map(b => {
    const children = []
    if (b.hasChariot) {
        const chariotChildren = []
        for (let i = 1; i <= (b.chariotCount || 0); i++) chariotChildren.push({ value: '车' + i, label: '车' + i })
        children.push({ value: '车', label: '车', children: chariotChildren.length >= 2 ? chariotChildren : undefined })
    }
    if (b.horseCount > 0) {
        const horseChildren = []
        for (let i = 1; i <= (b.horseCount || 0); i++) horseChildren.push({ value: '马' + i, label: '马' + i })
        children.push({ value: '马', label: '马', children: horseChildren.length >= 2 ? horseChildren : undefined })
    }
    children.push({ value: '墓葬出土', label: '墓葬出土' })
    return { value: (b.burialNo || '') + (b.name && b.name !== b.burialNo ? ' ' + b.name : ''), label: (b.burialNo || '') + (b.name && b.name !== b.burialNo ? ' ' + b.name : ''), children }
}))
const fetchBurialData = async () => { const res = await request.get('/admin/burial/list/simple'); burialData.value = res.data || [] }

// Cascader值 → 路径字符串
const getPath = (arr) => arr?.join('/') || ''
// 路径字符串 → Cascader数组
const parsePath = (str) => str ? str.split('/') : []

const addCascader = ref([]); const detailCascader = ref([]); const editCascader = ref([])

const addVisible = ref(false)
const addForm = ref({ artifactCode:'',artifactName:'',excavationRelic:'',samplePosition:'',sampleMaterial:'',sampleStatus:'',sampleQuantity:'',sampleMethod:'',purpose:'',storageLocation:'',departureTime:'',destination:'',samplePhoto:'',analysisData:'',analysisReport:'',manager:'',sampler:'',notes:'' })
const resetAddForm = () => {
    addForm.value = { artifactCode:'',artifactName:'',excavationRelic:'',samplePosition:'',sampleMaterial:'',sampleStatus:'',sampleQuantity:'',sampleMethod:'',purpose:'',storageLocation:'',departureTime:'',destination:'',samplePhoto:'',analysisData:'',analysisReport:'',manager:'',sampler:'',notes:'' }
    addCascader.value = []
}
const onAddClosed = () => { resetAddForm() }
const submitAdd = async () => { const d = { ...addForm.value }; d.analysisData = d.purpose; d.analysisReport = d.purpose; d.serialNumber = String(list.value.length + 1); d.excavationRelic = getPath(addCascader.value); await request.post('/admin/detection', d); ElMessage.success('添加成功'); addVisible.value = false; fetchList() }

const editVisible = ref(false); const editData = ref({})
const openEdit = (row) => { editData.value = { ...row }; editVisible.value = true; editCascader.value = parsePath(row.excavationRelic || '') }
const submitEdit = async () => { editData.value.excavationRelic = getPath(editCascader.value); await request.put('/admin/detection/' + editData.value.id, editData.value); ElMessage.success('保存成功'); editVisible.value = false; fetchList() }

onMounted(() => { fetchList(); fetchBurialData(); fetchLabInstruments() })
</script>

<template>
    <div class="detection-page">
        <div class="stats-row">
            <el-card class="stat-card" shadow="never"><div class="stat-value">{{ stats.total }}</div><div class="stat-label">检测项目总数</div></el-card>
            <el-card class="stat-card" shadow="never">
                <div class="stat-tags"><el-tag v-for="r in stats.relicDistribution" :key="r.name" size="small" type="info" effect="plain">{{ r.name }} {{ r.count }}</el-tag><span v-if="!stats.relicDistribution.length" class="empty-hint">暂无</span></div>
                <div class="stat-label">遗迹分布</div>
            </el-card>
            <el-card class="stat-card" shadow="never">
                <div class="stat-tags"><el-tag v-for="p in stats.purposeDistribution" :key="p.name" size="small" type="success" effect="plain">{{ p.name }} {{ p.count }}</el-tag><span v-if="!stats.purposeDistribution.length" class="empty-hint">暂无</span></div>
                <div class="stat-label">实验分布</div>
            </el-card>
        </div>

        <el-card shadow="never" class="filter-card">
            <div class="filter-bar">
                <el-cascader v-model="filterCascader" :options="cascaderOptions" placeholder="出土遗迹" clearable style="width:200px" @change="handleSearch" />
                <el-select v-model="searchParams.sampleMaterial" placeholder="材质" clearable @change="handleSearch" style="width:140px">
                    <el-option v-for="m in materialOptions" :key="m.value" :label="m.label" :value="m.value" />
                </el-select>
                <el-select v-model="searchParams.purpose" placeholder="目的" clearable @change="handleSearch" style="width:140px">
                    <el-option v-for="p in purposeOptions" :key="p.value" :label="p.label" :value="p.value" />
                </el-select>
                <el-button @click="handleReset">重置</el-button>
            </div>
        </el-card>

        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="header">
                    <span style="font-weight:600">检测分析总览</span>
                    <div>
                        <el-button type="primary" @click="addVisible = true">添加检测</el-button>
                        <el-button type="success" @click="importVisible = true">导入</el-button>
                        <el-button type="danger" @click="enterBatchMode" :disabled="batchMode">批量删除</el-button>
                    </div>
                </div>
            </template>
            <div v-if="batchMode" class="batch-bar"><span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 条</span><div><el-button type="danger" @click="confirmBatchDelete">确认删除</el-button><el-button @click="cancelBatchMode">取消</el-button></div></div>
            <el-table :data="pagedFiltered" style="width:100%" @selection-change="(rows) => selectedRows = rows">
                <el-table-column v-if="batchMode" type="selection" width="50" />
                <el-table-column label="序号" prop="serialNumber" />
                <el-table-column label="文物编号" prop="artifactCode" />
                <el-table-column label="文物名称" prop="artifactName" />
                <el-table-column label="出土遗迹" prop="excavationRelic" />
                <el-table-column label="分析/取样部位" prop="samplePosition" />
                <el-table-column label="样品材质" prop="sampleMaterial" />
                <el-table-column label="样品状态" prop="sampleStatus" />
                <el-table-column label="样品数量" prop="sampleQuantity" />
                <el-table-column label="取样方法" prop="sampleMethod" />
                <el-table-column label="目的" prop="purpose" />
                <el-table-column label="操作" fixed="right">
                    <template #default="{row}"><el-link type="primary" @click="openDetail(row)">详情</el-link></template>
                </el-table-column>
                <template #empty><el-empty description="暂无数据" /></template>
            </el-table>
            <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" :total="filtered.length" background style="margin-top:16px;justify-content:flex-end" />
        </el-card>
    </div>

    <!-- 详情对话框（双模式） -->
    <el-dialog v-model="detailVisible" :title="detailEditMode ? '编辑检测' : '检测详情'" width="700px">
        <template v-if="!detailEditMode">
            <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="序号">{{ detailData.serialNumber || '-' }}</el-descriptions-item>
                <el-descriptions-item label="文物编号">{{ detailData.artifactCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="文物名称">{{ detailData.artifactName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="出土遗迹">{{ detailData.excavationRelic || '-' }}</el-descriptions-item>
                <el-descriptions-item label="取样部位">{{ detailData.samplePosition || '-' }}</el-descriptions-item>
                <el-descriptions-item label="样品材质">{{ detailData.sampleMaterial || '-' }}</el-descriptions-item>
                <el-descriptions-item label="样品状态">{{ detailData.sampleStatus || '-' }}</el-descriptions-item>
                <el-descriptions-item label="样品数量">{{ detailData.sampleQuantity || '-' }}</el-descriptions-item>
                <el-descriptions-item label="取样方法">{{ detailData.sampleMethod || '-' }}</el-descriptions-item>
                <el-descriptions-item label="目的">{{ detailData.purpose || '-' }}</el-descriptions-item>
                <el-descriptions-item label="存放位置">{{ detailData.storageLocation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="出库时间">{{ detailData.departureTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="去处">{{ detailData.destination || '-' }}</el-descriptions-item>
                <el-descriptions-item label="取样照片">
                        <el-image v-if="detailData.samplePhoto" :src="detailData.samplePhoto" fit="cover" style="width:200px;height:140px;border-radius:6px" :preview-src-list="[detailData.samplePhoto]" />
                        <span v-else>-</span>
                    </el-descriptions-item>
                <el-descriptions-item label="分析数据" :span="2">{{ detailData.analysisData || '-' }}</el-descriptions-item>
                <el-descriptions-item label="分析报告" :span="2">{{ detailData.analysisReport || '-' }}</el-descriptions-item>
                <el-descriptions-item label="文物管理人">{{ detailData.manager || '-' }}</el-descriptions-item>
                <el-descriptions-item label="取样人">{{ detailData.sampler || '-' }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top:20px;text-align:right"><el-button type="primary" @click="enterDetailEditMode">编辑</el-button></div>
        </template>
        <template v-else>
            <el-form :model="detailData" label-width="100px"><el-row :gutter="16">
                <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="detailData.artifactCode" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="detailData.artifactName" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出土遗迹"><el-cascader v-model="detailCascader" :options="cascaderOptions" placeholder="请选择出土遗迹" clearable style="width:100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="detailData.samplePosition" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="detailData.sampleMaterial" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="样品状态"><el-input v-model="detailData.sampleStatus" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="样品数量"><el-input v-model="detailData.sampleQuantity" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="取样方法"><el-input v-model="detailData.sampleMethod" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="目的"><el-select :model-value="(detailData.purpose||'').split('/').filter(Boolean)" @update:model-value="detailData.purpose = ($event||[]).join('/')" multiple placeholder="选择实验项目" style="width:100%"><el-option v-for="p in purposeOptions" :key="p.value" :label="p.label" :value="p.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="存放位置"><el-input v-model="detailData.storageLocation" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出库时间"><el-input v-model="detailData.departureTime" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="去处"><el-input v-model="detailData.destination" /></el-form-item></el-col>
                <el-form-item label="取样照片">
                        <el-upload v-if="detailEditMode" action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true" :on-success="onDetailPhoto" accept="image/*">
                            <el-button type="primary">选择照片</el-button>
                        </el-upload>
                        <el-form-item v-if="detailData.samplePhoto" label="预览"><el-image :src="detailData.samplePhoto" fit="cover" style="width:200px;height:120px;border-radius:6px" /></el-form-item>
                    </el-form-item>
                <el-col :span="12"><el-form-item label="管理人"><el-input v-model="detailData.manager" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="取样人"><el-input v-model="detailData.sampler" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="备注"><el-input v-model="detailData.notes" type="textarea" :rows="2" /></el-form-item></el-col>
            </el-row></el-form>
            <div style="margin-top:20px;text-align:right"><el-button @click="cancelDetailEdit">取消</el-button><el-button type="primary" @click="saveDetailEdit">保存</el-button></div>
        </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="添加检测" width="700px" :close-on-click-modal="false" destroy-on-close @close="onAddClosed">
        <el-form :model="addForm" label-width="100px"><el-row :gutter="16">
            <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="addForm.artifactCode" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="addForm.artifactName" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出土遗迹"><el-cascader v-model="addCascader" :options="cascaderOptions" placeholder="请选择出土遗迹" clearable style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="addForm.samplePosition" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="addForm.sampleMaterial" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品状态"><el-input v-model="addForm.sampleStatus" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品数量"><el-input v-model="addForm.sampleQuantity" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样方法"><el-input v-model="addForm.sampleMethod" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="目的"><el-select :model-value="(addForm.purpose||'').split('/').filter(Boolean)" @update:model-value="addForm.purpose = ($event||[]).join('/')" multiple placeholder="选择实验项目" style="width:100%"><el-option v-for="p in purposeOptions" :key="p.value" :label="p.label" :value="p.value" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="存放位置"><el-input v-model="addForm.storageLocation" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出库时间"><el-date-picker v-model="addForm.departureTime" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY/MM/DD" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="去处"><el-input v-model="addForm.destination" /></el-form-item></el-col>
            <el-form-item label="取样照片">
<el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true" :on-success="onAddPhoto" accept="image/*">
                <el-button type="primary">选择照片</el-button>
            </el-upload>
            <el-form-item v-if="addForm.samplePhoto" label="预览"><el-image :src="addForm.samplePhoto" fit="cover" style="width:200px;height:120px;border-radius:6px" /></el-form-item>
        </el-form-item>
            <el-col :span="12"><el-form-item label="管理人"><el-input v-model="addForm.manager" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样人"><el-input v-model="addForm.sampler" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="备注"><el-input v-model="addForm.notes" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row></el-form>
        <template #footer><el-button @click="addVisible = false">取消</el-button><el-button type="primary" @click="submitAdd">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑检测" width="700px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="editData" label-width="100px"><el-row :gutter="16">
            <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="editData.artifactCode" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="editData.artifactName" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出土遗迹"><el-cascader v-model="editCascader" :options="cascaderOptions" placeholder="请选择出土遗迹" clearable style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="editData.samplePosition" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="editData.sampleMaterial" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品状态"><el-input v-model="editData.sampleStatus" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品数量"><el-input v-model="editData.sampleQuantity" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样方法"><el-input v-model="editData.sampleMethod" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="目的"><el-select :model-value="(editData.purpose||'').split('/').filter(Boolean)" @update:model-value="editData.purpose = ($event||[]).join('/')" multiple placeholder="选择实验项目" style="width:100%"><el-option v-for="p in purposeOptions" :key="p.value" :label="p.label" :value="p.value" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="存放位置"><el-input v-model="editData.storageLocation" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出库时间"><el-input v-model="editData.departureTime" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="去处"><el-input v-model="editData.destination" /></el-form-item></el-col>
            <el-form-item label="取样照片">
            <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true" :on-success="onEditPhoto" accept="image/*">
                <el-button type="primary">选择照片</el-button>
            </el-upload>
            <el-form-item v-if="editData.samplePhoto" label="预览"><el-image :src="editData.samplePhoto" fit="cover" style="width:200px;height:120px;border-radius:6px" /></el-form-item>
        </el-form-item>
            <el-col :span="12"><el-form-item label="管理人"><el-input v-model="editData.manager" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样人"><el-input v-model="editData.sampler" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="备注"><el-input v-model="editData.notes" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row></el-form>
        <template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" @click="submitEdit">保存</el-button></template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importVisible" title="导入检测数据" width="500px">
        <el-form label-width="100px">
            <el-form-item><el-button type="success" @click="downloadTemplate">获取模板</el-button></el-form-item>
            <el-form-item label="Excel文件"><el-upload ref="uploadRef" :auto-upload="false" :on-change="handleImportFile" accept=".xlsx,.xls" :limit="1"><el-button type="primary">选择文件</el-button></el-upload></el-form-item>
        </el-form>
        <template #footer><el-button @click="importVisible = false">取消</el-button><el-button type="primary" @click="handleImport">开始导入</el-button></template>
    </el-dialog>
</template>

<style scoped>
.detection-page { padding: 0; }
.stats-row { display: flex; gap: 12px; margin-bottom: 12px; }
.stat-card { flex: 1; border: 1px solid #E5E6EB; border-radius: 8px; }
.stat-tags { display: flex; flex-wrap: wrap; gap: 6px; min-height: 28px; align-items: center; }
.empty-hint { font-size: 13px; color: #C0C4CC; }
.stat-value { font-size: 28px; font-weight: 700; color: #1D2129; }
.stat-label { font-size: 12px; color: #8C8C8C; margin-top: 4px; }
.filter-card { margin-bottom: 12px; border: 1px solid #E5E6EB; border-radius: 8px; }
.filter-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.table-card { border: 1px solid #E5E6EB; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.batch-bar { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; margin-bottom: 10px; background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; }
.batch-info { color: #e64242; font-size: 14px; }
</style>
