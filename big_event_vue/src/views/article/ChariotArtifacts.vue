<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { artifactsBatchImportService } from '@/api/Artifacts.js'
import * as XLSX from 'xlsx'

// 墓葬列表（只显示有车的）
const allBurials = ref([])
const burialList = computed(() => allBurials.value.filter(b => b.hasChariot && b.chariotCount > 0))
const CACHE_KEY = 'chariot_artifacts_burial'
const selectedBurialId = ref(null)
try { const v = sessionStorage.getItem(CACHE_KEY); if (v) selectedBurialId.value = parseInt(v) } catch(e) {}
const selectedBurial = computed(() => allBurials.value.find(b => b.id === selectedBurialId.value))
const chariotCount = computed(() => selectedBurial.value?.chariotCount || 0)
const selectedChariotIndex = ref(1)
const chariotOptions = computed(() => Array.from({length: chariotCount.value}, (_, i) => ({label: '车' + (i+1), value: i+1})))

const stats = ref({ total: 0, materials: [], completeness: [] })
const artifacts = ref([])

const calcStats = (list) => {
    const total = list.length; const matMap = {}; const compMap = {}
    list.forEach(a => { const m = a.material1 || '未知'; matMap[m] = (matMap[m] || 0) + 1; const c = a.completeness || '未知'; compMap[c] = (compMap[c] || 0) + 1 })
    return { total, materials: Object.entries(matMap).map(([n,c])=>({name:n,count:c})), completeness: Object.entries(compMap).map(([n,c])=>({name:n,count:c})) }
}

const fetchBurialList = async () => {
    const res = await request.get('/admin/burial/list/simple')
    allBurials.value = res.data || []
    if (selectedBurialId.value && !burialList.value.find(b => b.id === selectedBurialId.value)) selectedBurialId.value = null
    if (!selectedBurialId.value && burialList.value.length > 0) selectedBurialId.value = burialList.value[0].id
    if (selectedBurialId.value) { selectedChariotIndex.value = 1; await loadArtifacts() }
}

const onBurialChange = () => { sessionStorage.setItem(CACHE_KEY, selectedBurialId.value); selectedChariotIndex.value = 1; loadArtifacts() }
const onChariotChange = () => { loadArtifacts() }

const loadArtifacts = async () => {
    if (!selectedBurialId.value) { artifacts.value = []; stats.value = { total: 0, materials: [], completeness: [] }; return }
    const artRes = await request.get(`/admin/burial/${selectedBurialId.value}/artifacts`)
    artifacts.value = (artRes.data || []).filter(a => a.chariotIndex === selectedChariotIndex.value)
    stats.value = calcStats(artifacts.value); pageNum.value = 1
}

const pageNum = ref(1); const pageSize = ref(10)
const pagedArtifacts = computed(() => artifacts.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const onPageChange = (num) => { pageNum.value = num }

const detailVisible = ref(false); const detailData = ref({})
const openDetail = (row) => { detailData.value = { ...row }; detailVisible.value = true }

const batchMode = ref(false); const selectedRows = ref([])
const enterBatchMode = () => { batchMode.value = true; selectedRows.value = [] }
const cancelBatchMode = () => { batchMode.value = false; selectedRows.value = [] }
const handleSelectionChange = (rows) => { selectedRows.value = rows }
const confirmBatchDelete = () => {
    if (!selectedRows.value.length) { ElMessage.warning('请至少选择一件'); return }
    ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 件文物？`, '批量删除确认', { confirmButtonText:'确认删除', cancelButtonText:'取消', type:'warning' })
        .then(async () => { await request.post('/artifacts/batch-delete', selectedRows.value.map(r=>r.id)); ElMessage.success('删除成功'); cancelBatchMode(); loadArtifacts() }).catch(()=>{})
}

const importVisible = ref(false); const importFile = ref(null)
const handleImportFile = (file) => { importFile.value = file.raw }
const handleImport = async () => {
    if (!importFile.value) { ElMessage.warning('请选择文件'); return }
    const reader = new FileReader()
    reader.onload = async (e) => {
        const wb = XLSX.read(new Uint8Array(e.target.result), {type:'array'})
        const data = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        await artifactsBatchImportService(data.map(item => ({
            burialId: selectedBurialId.value, chariotIndex: selectedChariotIndex.value, serialNumber: item['序号'],
            newArtifactCode: item['文物新编号']||'', newArtifactName: item['文物新名称']||'',
            originalArtifactCode: item['文物原始编号']||'', originalArtifactName: item['文物原名称']||'',
            material1: item['材质1']||'', material2: item['材质2']||'', completeness: item['完整度']||'',
            quantity1: item['数量1']||null, quantity2: item['数量2']||null, dimensions: item['尺寸']||'', weight: item['重量']||'',
            excavationRelic: item['出土遗迹']||'', excavationPosition: item['出土位置']||'', excavationTime: item['出土时间']||'',
            transferProcess: item['文物流转过程']||'', restorationStatus: item['修复、复原状况']||'',
            photographer: item['拍照人']||'', draftsperson: item['绘图人']||'', textDescriber: item['文字描述人']||'',
            notes: item['备注']||'', gradingStatus: item['定级情况']||'', testingStatus: item['科技检测情况']||''
        })))
        ElMessage.success('导入成功'); importVisible.value = false; loadArtifacts()
    }; reader.readAsArrayBuffer(importFile.value)
}

const downloadTemplate = () => { const h = ['序号','文物新编号','文物新名称','文物原始编号','文物原名称','材质1','材质2','完整度','视觉特征','数量1','数量2','尺寸','重量','出土遗迹','出土位置','出土时间','存放方式','文物流转过程','修复复原状况','拍照人','绘图人','文字描述人','备注','定级情况','科技检测情况']; const ws = XLSX.utils.aoa_to_sheet([h]); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb,ws,'模板'); XLSX.writeFile(wb,'文物导入模板.xlsx') }

const addVisible = ref(false)
const addForm = ref({ newArtifactCode:'',newArtifactName:'',originalArtifactCode:'',originalArtifactName:'',material1:'',material2:'',completeness:'',artifactDescription:'',quantity1:null,quantity2:null,dimensions:'',weight:'',excavationRelic:'',excavationPosition:'',excavationTime:'',transferProcess:'',restorationStatus:'',photographer:'',draftsperson:'',textDescriber:'',notes:'',gradingStatus:'',testingStatus:'' })
const submitAdd = async () => { await request.post('/artifacts', { ...addForm.value, burialId: selectedBurialId.value, chariotIndex: selectedChariotIndex.value }); ElMessage.success('添加成功'); addVisible.value = false; loadArtifacts() }

onMounted(() => { fetchBurialList() })
</script>

<template>
    <div class="chariot-page">
        <div class="toolbar">
            <div style="display:flex;gap:12px;align-items:center">
                <el-select v-model="selectedBurialId" placeholder="请选择墓葬" @change="onBurialChange" style="width:280px" size="large">
                    <el-option v-for="b in burialList" :key="b.id" :label="(b.burialNo||'') + (b.name && b.name !== b.burialNo ? ' ' + b.name : '')" :value="b.id" />
                </el-select>
                <el-select v-if="chariotCount >= 2" v-model="selectedChariotIndex" placeholder="请选择车" @change="onChariotChange" style="width:100px" size="large">
                    <el-option v-for="c in chariotOptions" :key="c.value" :label="c.label" :value="c.value" />
                </el-select>
                <span v-if="chariotCount === 1" style="font-size:14px;color:#1D2129">车1</span>
            </div>
            <div class="toolbar-actions">
                <el-button type="primary" @click="addVisible = true">添加文物</el-button>
                <el-button type="success" @click="importVisible = true">导入文物</el-button>
                <el-button type="danger" @click="enterBatchMode" :disabled="batchMode">批量删除</el-button>
            </div>
        </div>
        <div class="stats-row">
            <div class="stat-card"><div class="stat-value">{{ stats.total }}</div><div class="stat-label">车出土文物总数</div></div>
            <div class="stat-card"><div class="stat-tags"><el-tag v-for="m in stats.materials" :key="m.name" size="small" type="info" effect="plain">{{ m.name }} {{ m.count }}</el-tag><span v-if="!stats.materials.length" class="empty-hint">暂无</span></div><div class="stat-label">材质分布</div></div>
            <div class="stat-card"><div class="stat-tags"><el-tag v-for="c in stats.completeness" :key="c.name" size="small" type="success" effect="plain">{{ c.name }} {{ c.count }}</el-tag><span v-if="!stats.completeness.length" class="empty-hint">暂无</span></div><div class="stat-label">完整度分布</div></div>
        </div>
        <el-card class="table-card" shadow="never">
            <template #header><span style="font-weight:600">车出土文物列表</span></template>
            <div v-if="batchMode" class="batch-bar"><span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 件文物</span><div><el-button type="danger" @click="confirmBatchDelete">确认删除</el-button><el-button @click="cancelBatchMode">取消</el-button></div></div>
            <el-table :data="pagedArtifacts" style="width:100%" @selection-change="handleSelectionChange">
                <el-table-column v-if="batchMode" type="selection" width="50" />
                <el-table-column label="序号" prop="serialNumber" width="60" />
                <el-table-column label="文物编号" width="140"><template #default="{ row }">{{ row.newArtifactCode || row.originalArtifactCode || '-' }}</template></el-table-column>
                <el-table-column label="文物名称" width="160"><template #default="{ row }">{{ row.newArtifactName || row.originalArtifactName || '-' }}</template></el-table-column>
                <el-table-column label="出土遗迹" prop="excavationRelic" width="140" />
                <el-table-column label="出土位置" prop="excavationPosition" width="140" />
                <el-table-column label="材质" prop="material1" width="120" />
                <el-table-column label="完整度" prop="completeness" width="80" />
                <el-table-column label="数量" prop="quantity1" width="70" />
                <el-table-column label="科技检测情况" prop="testingStatus" width="140" />
                <el-table-column label="操作" width="80" fixed="right"><template #default="{ row }"><el-link type="primary" @click="openDetail(row)">详情</el-link></template></el-table-column>
                <template #empty><el-empty description="该车暂无出土文物" /></template>
            </el-table>
            <el-pagination v-model:current-page="pageNum" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" :total="artifacts.length" background style="margin-top:16px;justify-content:flex-end" @current-change="onPageChange" />
        </el-card>
    </div>
    <el-drawer v-model="detailVisible" title="文物详情" direction="rtl" size="55%"><el-descriptions :column="2" border>
        <el-descriptions-item label="序号">{{ detailData.serialNumber ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="文物新编号">{{ detailData.newArtifactCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文物新名称">{{ detailData.newArtifactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文物原始编号">{{ detailData.originalArtifactCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文物原名称">{{ detailData.originalArtifactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="材质1">{{ detailData.material1 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="材质2">{{ detailData.material2 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完整度">{{ detailData.completeness || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量1">{{ detailData.quantity1 ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量2">{{ detailData.quantity2 ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="尺寸">{{ detailData.dimensions || '-' }}</el-descriptions-item>
        <el-descriptions-item label="重量">{{ detailData.weight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出土遗迹">{{ detailData.excavationRelic || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出土位置">{{ detailData.excavationPosition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出土时间">{{ detailData.excavationTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="存放方式">{{ detailData.storageMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拍照人">{{ detailData.photographer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="绘图人">{{ detailData.draftsperson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文字描述人">{{ detailData.textDescriber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="定级情况">{{ detailData.gradingStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科技检测情况">{{ detailData.testingStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文物流转过程" :span="2">{{ detailData.transferProcess || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修复复原状况" :span="2">{{ detailData.restorationStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
        <el-descriptions-item label="视觉特征" :span="2"><div v-html="detailData.artifactDescription || '-'" /></el-descriptions-item>
    </el-descriptions></el-drawer>
    <el-dialog v-model="addVisible" title="添加文物" width="760px" :close-on-click-modal="false" destroy-on-close><el-form :model="addForm" label-width="100px"><el-row :gutter="16">
        <el-col :span="12"><el-form-item label="文物新编号"><el-input v-model="addForm.newArtifactCode" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="文物新名称"><el-input v-model="addForm.newArtifactName" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="原始编号"><el-input v-model="addForm.originalArtifactCode" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="原名称"><el-input v-model="addForm.originalArtifactName" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="材质1"><el-input v-model="addForm.material1" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="材质2"><el-input v-model="addForm.material2" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="完整度"><el-input v-model="addForm.completeness" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="视觉特征"><el-input v-model="addForm.artifactDescription" type="textarea" :rows="2" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="数量1"><el-input v-model="addForm.quantity1" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="数量2"><el-input v-model="addForm.quantity2" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="尺寸"><el-input v-model="addForm.dimensions" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="重量"><el-input v-model="addForm.weight" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="出土遗迹"><el-input v-model="addForm.excavationRelic" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="出土位置"><el-input v-model="addForm.excavationPosition" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="出土时间"><el-input v-model="addForm.excavationTime" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="存放方式"><el-input v-model="addForm.storageMethod" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="文物流转过程"><el-input v-model="addForm.transferProcess" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="修复复原状况"><el-input v-model="addForm.restorationStatus" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="拍照人"><el-input v-model="addForm.photographer" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="绘图人"><el-input v-model="addForm.draftsperson" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="文字描述人"><el-input v-model="addForm.textDescriber" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="定级情况"><el-input v-model="addForm.gradingStatus" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="科技检测情况"><el-input v-model="addForm.testingStatus" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="备注"><el-input v-model="addForm.notes" type="textarea" :rows="2" /></el-form-item></el-col>
    </el-row></el-form><template #footer><el-button @click="addVisible = false">取消</el-button><el-button type="primary" @click="submitAdd">保存</el-button></template></el-dialog>
    <el-dialog v-model="importVisible" title="导入文物" width="500px"><el-form label-width="100px"><el-form-item><el-button type="success" @click="downloadTemplate">获取模板</el-button></el-form-item><el-form-item label="Excel文件"><el-upload :auto-upload="false" :on-change="handleImportFile" accept=".xlsx,.xls" :limit="1"><el-button type="primary">选择文件</el-button></el-upload></el-form-item></el-form><template #footer><el-button @click="importVisible = false">取消</el-button><el-button type="primary" @click="handleImport">开始导入</el-button></template></el-dialog>
</template>

<style scoped>
.chariot-page { padding: 0; }
.toolbar { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; margin-bottom: 16px; border-bottom: 1px solid #E5E6EB; }
.toolbar-actions { display: flex; gap: 8px; }
.stats-row { display: flex; gap: 16px; margin-bottom: 16px; }
.stat-card { flex: 1; background: #fff; border: 1px solid #E5E6EB; border-radius: 8px; padding: 16px 20px; }
.stat-value { font-size: 28px; font-weight: 700; color: #1D2129; }
.stat-tags { display: flex; flex-wrap: wrap; gap: 6px; min-height: 28px; align-items: center; }
.stat-label { font-size: 12px; color: #8C8C8C; margin-top: 8px; }
.empty-hint { font-size: 13px; color: #C0C4CC; }
.table-card { border: 1px solid #E5E6EB; border-radius: 8px; }
.batch-bar { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; margin-bottom: 10px; background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; }
.batch-info { color: #e64242; font-size: 14px; }
</style>
