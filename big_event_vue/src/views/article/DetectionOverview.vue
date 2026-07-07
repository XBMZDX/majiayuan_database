<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const list = ref([])
const stats = ref({ total: 0, relicDistribution: [], purposeDistribution: [] })

const calcStats = (data) => {
    const relicMap = {}; const purposeMap = {}
    data.forEach(d => {
        const r = d.excavationRelic || '未知'; relicMap[r] = (relicMap[r] || 0) + 1
        const p = d.purpose || '未知'; purposeMap[p] = (purposeMap[p] || 0) + 1
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

const searchParams = ref({ artifactCode: '', artifactName: '', excavationRelic: '', sampleMaterial: '' })
const filtered = computed(() => list.value.filter(r =>
    (!searchParams.value.artifactCode || (r.artifactCode||'').includes(searchParams.value.artifactCode)) &&
    (!searchParams.value.artifactName || (r.artifactName||'').includes(searchParams.value.artifactName)) &&
    (!searchParams.value.excavationRelic || (r.excavationRelic||'').includes(searchParams.value.excavationRelic)) &&
    (!searchParams.value.sampleMaterial || (r.sampleMaterial||'').includes(searchParams.value.sampleMaterial))
))
const pagedFiltered = computed(() => filtered.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const handleSearch = () => { pageNum.value = 1 }
const handleReset = () => { searchParams.value = { artifactCode: '', artifactName: '', excavationRelic: '', sampleMaterial: '' }; pageNum.value = 1 }

const detailVisible = ref(false); const detailData = ref({})
const openDetail = (row) => { detailData.value = { ...row }; detailVisible.value = true }

const del = (row) => { ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }).then(async () => { await request.delete('/admin/detection/' + row.id); ElMessage.success('删除成功'); fetchList() }).catch(() => {}) }

const batchMode = ref(false); const selectedRows = ref([])
const enterBatchMode = () => { batchMode.value = true; selectedRows.value = [] }
const cancelBatchMode = () => { batchMode.value = false; selectedRows.value = [] }
const confirmBatchDelete = () => {
    if (!selectedRows.value.length) { ElMessage.warning('请至少选择一条'); return }
    ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条？`, '批量删除', { confirmButtonText:'确认删除', cancelButtonText:'取消', type:'warning' })
        .then(async () => { await request.post('/admin/detection/batch-delete', selectedRows.value.map(r=>r.id)); ElMessage.success('删除成功'); cancelBatchMode(); fetchList() }).catch(()=>{})
}

const addVisible = ref(false)
const addForm = ref({ artifactCode:'',artifactName:'',excavationRelic:'',samplePosition:'',sampleMaterial:'',sampleStatus:'',sampleQuantity:'',sampleMethod:'',purpose:'',storageLocation:'',departureTime:'',destination:'',samplePhoto:'',analysisData:'',analysisReport:'',manager:'',sampler:'',notes:'' })
const submitAdd = async () => { await request.post('/admin/detection', addForm.value); ElMessage.success('添加成功'); addVisible.value = false; fetchList() }

const editVisible = ref(false); const editData = ref({})
const openEdit = (row) => { editData.value = { ...row }; editVisible.value = true }
const submitEdit = async () => { await request.put('/admin/detection/' + editData.value.id, editData.value); ElMessage.success('保存成功'); editVisible.value = false; fetchList() }

onMounted(() => { fetchList() })
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

        <div class="filter-bar">
            <el-input v-model="searchParams.artifactCode" placeholder="文物编号" clearable style="width:160px" @keyup.enter="handleSearch" @clear="handleSearch" />
            <el-input v-model="searchParams.artifactName" placeholder="文物名称" clearable style="width:160px" @keyup.enter="handleSearch" @clear="handleSearch" />
            <el-input v-model="searchParams.excavationRelic" placeholder="出土遗迹" clearable style="width:160px" @keyup.enter="handleSearch" @clear="handleSearch" />
            <el-input v-model="searchParams.sampleMaterial" placeholder="样品材质" clearable style="width:160px" @keyup.enter="handleSearch" @clear="handleSearch" />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
        </div>

        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="header">
                    <span style="font-weight:600">检测分析总览</span>
                    <div>
                        <el-button type="primary" @click="addVisible = true">添加检测</el-button>
                        <el-button type="danger" @click="enterBatchMode" :disabled="batchMode">批量删除</el-button>
                    </div>
                </div>
            </template>
            <div v-if="batchMode" class="batch-bar"><span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 条</span><div><el-button type="danger" @click="confirmBatchDelete">确认删除</el-button><el-button @click="cancelBatchMode">取消</el-button></div></div>
            <el-table :data="pagedFiltered" @selection-change="(rows) => selectedRows = rows">
                <el-table-column v-if="batchMode" type="selection" width="50" />
                <el-table-column label="序号" prop="serialNumber" width="60" />
                <el-table-column label="文物编号" prop="artifactCode" width="120" />
                <el-table-column label="文物名称" prop="artifactName" width="140" />
                <el-table-column label="出土遗迹" prop="excavationRelic" width="120" />
                <el-table-column label="分析/取样部位" prop="samplePosition" width="130" />
                <el-table-column label="样品材质" prop="sampleMaterial" width="100" />
                <el-table-column label="样品状态" prop="sampleStatus" width="90" />
                <el-table-column label="样品数量" prop="sampleQuantity" width="80" />
                <el-table-column label="取样方法" prop="sampleMethod" width="120" />
                <el-table-column label="目的" prop="purpose" width="100" />
                <el-table-column label="取样照片" prop="samplePhoto" width="100" />
                <el-table-column label="分析数据" prop="analysisData" width="100" />
                <el-table-column label="分析报告" prop="analysisReport" width="100" />
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{row}"><el-link type="primary" @click="openDetail(row)">详情</el-link>&nbsp;<el-link type="primary" @click="openEdit(row)">编辑</el-link>&nbsp;<el-link type="danger" @click="del(row)">删除</el-link></template>
                </el-table-column>
                <template #empty><el-empty description="暂无数据" /></template>
            </el-table>
            <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" :total="filtered.length" background style="margin-top:16px;justify-content:flex-end" />
        </el-card>
    </div>

    <el-dialog v-model="detailVisible" title="检测详情" width="700px">
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
            <el-descriptions-item label="取样照片">{{ detailData.samplePhoto || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分析数据" :span="2">{{ detailData.analysisData || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分析报告" :span="2">{{ detailData.analysisReport || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文物管理人">{{ detailData.manager || '-' }}</el-descriptions-item>
            <el-descriptions-item label="取样人">{{ detailData.sampler || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
        </el-descriptions>
    </el-dialog>

    <el-dialog v-model="addVisible" title="添加检测" width="700px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="addForm" label-width="100px"><el-row :gutter="16">
            <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="addForm.artifactCode" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="addForm.artifactName" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出土遗迹"><el-input v-model="addForm.excavationRelic" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="addForm.samplePosition" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="addForm.sampleMaterial" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品状态"><el-input v-model="addForm.sampleStatus" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品数量"><el-input v-model="addForm.sampleQuantity" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样方法"><el-input v-model="addForm.sampleMethod" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="目的"><el-input v-model="addForm.purpose" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="存放位置"><el-input v-model="addForm.storageLocation" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出库时间"><el-input v-model="addForm.departureTime" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="去处"><el-input v-model="addForm.destination" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样照片"><el-input v-model="addForm.samplePhoto" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="分析数据"><el-input v-model="addForm.analysisData" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="分析报告"><el-input v-model="addForm.analysisReport" /></el-form-item></el-col>
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
            <el-col :span="12"><el-form-item label="出土遗迹"><el-input v-model="editData.excavationRelic" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样部位"><el-input v-model="editData.samplePosition" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品材质"><el-input v-model="editData.sampleMaterial" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品状态"><el-input v-model="editData.sampleStatus" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="样品数量"><el-input v-model="editData.sampleQuantity" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样方法"><el-input v-model="editData.sampleMethod" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="目的"><el-input v-model="editData.purpose" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="存放位置"><el-input v-model="editData.storageLocation" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="出库时间"><el-input v-model="editData.departureTime" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="去处"><el-input v-model="editData.destination" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样照片"><el-input v-model="editData.samplePhoto" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="分析数据"><el-input v-model="editData.analysisData" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="分析报告"><el-input v-model="editData.analysisReport" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="管理人"><el-input v-model="editData.manager" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="取样人"><el-input v-model="editData.sampler" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="备注"><el-input v-model="editData.notes" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row></el-form>
        <template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" @click="submitEdit">保存</el-button></template>
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
.filter-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.table-card { border: 1px solid #E5E6EB; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.batch-bar { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; margin-bottom: 10px; background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; }
.batch-info { color: #e64242; font-size: 14px; }
</style>
