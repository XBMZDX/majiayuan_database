<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { artifactsBatchImportService } from '@/api/Artifacts.js'
import * as XLSX from 'xlsx'

// 墓葬列表 + 选中
const burialList = ref([])
const CACHE_KEY = 'tomb_excavation_burial'
const selectedBurialId = ref(null)
try { const v = sessionStorage.getItem(CACHE_KEY); if (v) selectedBurialId.value = parseInt(v) } catch(e) {}

// 统计数据
const stats = ref({ total: 0, materials: [], completeness: [] })
// 出土文物列表
const artifacts = ref([])

// 加载墓葬下拉
const fetchBurialList = async () => {
    const res = await request.get('/admin/burial/list/simple')
    burialList.value = res.data || []
    if (selectedBurialId.value && !burialList.value.find(b => b.id === selectedBurialId.value)) selectedBurialId.value = null
    if (!selectedBurialId.value && burialList.value.length > 0) selectedBurialId.value = burialList.value[0].id
    if (selectedBurialId.value) await loadBurialData()
}
const onBurialChange = () => { sessionStorage.setItem(CACHE_KEY, selectedBurialId.value); loadBurialData() }

// 从文物列表计算统计数据
const calcStats = (list) => {
    const total = list.length
    const matMap = {}; const compMap = {}
    list.forEach(a => {
        const m = a.material1 || '未知'; matMap[m] = (matMap[m] || 0) + 1
        const c = a.completeness || '未知'; compMap[c] = (compMap[c] || 0) + 1
    })
    return {
        total,
        materials: Object.entries(matMap).map(([name, count]) => ({ name, count })),
        completeness: Object.entries(compMap).map(([name, count]) => ({ name, count }))
    }
}

// 切换墓葬 → 刷新统计 + 文物表
const loadBurialData = async () => {
    if (!selectedBurialId.value) return
    const artRes = await request.get(`/admin/burial/${selectedBurialId.value}/artifacts`)
    // 墓葬出土只显示非棺文物（coffin_index 为空或0）
    artifacts.value = (artRes.data || []).filter(a => !a.coffinIndex || a.coffinIndex === 0)
    stats.value = calcStats(artifacts.value)
    pageNum.value = 1
}

// 分页（前端分页）
const pageNum = ref(1)
const pageSize = ref(10)
const pagedArtifacts = computed(() => {
    const start = (pageNum.value - 1) * pageSize.value
    return artifacts.value.slice(start, start + pageSize.value)
})
const onPageChange = (num) => { pageNum.value = num }

// 详情
const detailVisible = ref(false)
const detailData = ref({})
const openDetail = (row) => { detailData.value = { ...row }; detailVisible.value = true }

// ========== 批量删除（与文物信息总览相同模式） ==========
const batchMode = ref(false)
const selectedRows = ref([])
const selectionRef = ref(null)
const enterBatchMode = () => { batchMode.value = true; selectedRows.value = [] }
const cancelBatchMode = () => { batchMode.value = false; selectedRows.value = [] }
const handleSelectionChange = (rows) => { selectedRows.value = rows }
const confirmBatchDelete = () => {
    if (selectedRows.value.length === 0) { ElMessage.warning('请至少选择一件文物'); return }
    ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 件文物吗？此操作不可恢复。`,
        '批量删除确认',
        { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
        const ids = selectedRows.value.map(r => r.id)
        await request.post('/artifacts/batch-delete', ids)
        ElMessage.success(`成功删除 ${ids.length} 件文物`)
        cancelBatchMode()
        loadBurialData()
    }).catch(() => { ElMessage.info('已取消') })
}

// 导入
const importVisible = ref(false)
const uploadRef = ref(null); const importFile = ref(null)
const handleImportFile = (file) => { importFile.value = file.raw }
const handleImport = async () => {
    if (!importFile.value) { ElMessage.warning('请选择文件'); return }
    const reader = new FileReader()
    reader.onload = async (e) => {
        const wb = XLSX.read(new Uint8Array(e.target.result), { type: 'array' })
        const data = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        const importData = data.map(item => ({
            burialId: selectedBurialId.value,
            // serialNumber 由后端自动分配
            newArtifactCode: item['文物新编号'] || '', newArtifactName: item['文物新名称'] || '',
            originalArtifactCode: item['文物原始编号'] || '', originalArtifactName: item['文物原名称'] || '',
            material1: item['材质1'] || '', material2: item['材质2'] || '',
            completeness: item['完整度'] || '', quantity1: item['数量1'] || null,
            quantity2: item['数量2'] || null, dimensions: item['尺寸'] || '',
            weight: item['重量'] || '', excavationRelic: item['出土遗迹'] || '',
            excavationPosition: item['出土位置'] || '', excavationTime: item['出土时间'] || '',
            transferProcess: item['文物流转过程'] || '', restorationStatus: item['修复、复原状况'] || '',
            photographer: item['拍照人'] || '', draftsperson: item['绘图人'] || '',
            textDescriber: item['文字描述人'] || '', notes: item['备注'] || '',
            gradingStatus: item['定级情况'] || '', testingStatus: item['科技检测情况'] || ''
        }))
        await artifactsBatchImportService(importData)
        ElMessage.success('导入成功'); importFile.value = null; uploadRef.value?.clearFiles()
        importVisible.value = false
        loadBurialData()
    }
    reader.readAsArrayBuffer(importFile.value)
}

// 下载模板
const downloadTemplate = () => {
    const headers = ['序号', '文物新编号', '文物新名称', '文物原始编号', '文物原名称',
        '材质1', '材质2', '完整度', '视觉特征', '数量1', '数量2',
        '尺寸', '重量', '出土遗迹', '出土位置', '出土时间',
        '存放方式', '文物流转过程', '修复复原状况',
        '拍照人', '绘图人', '文字描述人', '备注', '定级情况', '科技检测情况']
    const ws = XLSX.utils.aoa_to_sheet([headers])
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '文物导入模板')
    XLSX.writeFile(wb, '文物导入模板.xlsx')
}

// 添加文物弹窗（与文物信息总览字段完全一致）
const addVisible = ref(false)
const addForm = ref({
    newArtifactCode: '', newArtifactName: '', originalArtifactCode: '', originalArtifactName: '',
    material1: '', material2: '', completeness: '', artifactDescription: '',
    quantity1: null, quantity2: null, dimensions: '', weight: '',
    excavationRelic: '', excavationPosition: '', excavationTime: '',
    transferProcess: '', restorationStatus: '',
    photographer: '', draftsperson: '', textDescriber: '',
    notes: '', gradingStatus: '', testingStatus: ''
})
const submitAdd = async () => {
    await request.post('/artifacts', { ...addForm.value, burialId: selectedBurialId.value })
    ElMessage.success('添加成功')
    addVisible.value = false
    loadBurialData()
}

onMounted(() => { fetchBurialList() })
</script>

<template>
    <div class="excavation-page">
        <!-- 顶部墓葬切换器 + 操作按钮 -->
        <div class="toolbar">
            <el-select v-model="selectedBurialId" placeholder="请选择墓葬" @change="onBurialChange" style="width:340px" size="large">
                <el-option v-for="b in burialList" :key="b.id" :label="b.name && b.name !== b.burialNo ? b.burialNo + ' ' + b.name : b.burialNo" :value="b.id" />
            </el-select>
            <div class="toolbar-actions">
                <el-button type="primary" @click="addVisible = true">添加文物</el-button>
                <el-button type="success" @click="importVisible = true">导入文物</el-button>
                <el-button type="danger" @click="enterBatchMode" :disabled="batchMode">批量删除</el-button>
            </div>
        </div>

        <!-- 数据统计栏 -->
        <div class="stats-row">
            <div class="stat-card">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">文物总数</div>
            </div>
            <div class="stat-card">
                <div class="stat-tags">
                    <el-tag v-for="m in stats.materials" :key="m.name" size="small" type="info" effect="plain">{{ m.name }} {{ m.count }}</el-tag>
                    <span v-if="!stats.materials.length" class="empty-hint">暂无</span>
                </div>
                <div class="stat-label">材质分布</div>
            </div>
            <div class="stat-card">
                <div class="stat-tags">
                    <el-tag v-for="c in stats.completeness" :key="c.name" size="small" type="success" effect="plain">{{ c.name }} {{ c.count }}</el-tag>
                    <span v-if="!stats.completeness.length" class="empty-hint">暂无</span>
                </div>
                <div class="stat-label">完整度分布</div>
            </div>
        </div>

        <!-- 出土文物总表 -->
        <el-card class="table-card" shadow="never">
            <template #header><span style="font-weight:600">出土文物列表</span></template>
            <div v-if="batchMode" class="batch-bar">
                <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 件文物</span>
                <div>
                    <el-button type="danger" @click="confirmBatchDelete">确认删除</el-button>
                    <el-button @click="cancelBatchMode">取消</el-button>
                </div>
            </div>
            <el-table :data="pagedArtifacts" style="width:100%" @selection-change="handleSelectionChange" ref="selectionRef">
                <el-table-column v-if="batchMode" type="selection" width="50" />
                <el-table-column label="序号" prop="serialNumber" width="60" />
                <el-table-column label="文物编号" width="140">
                    <template #default="{ row }">{{ row.newArtifactCode || row.originalArtifactCode || '-' }}</template>
                </el-table-column>
                <el-table-column label="文物名称" width="160">
                    <template #default="{ row }">{{ row.newArtifactName || row.originalArtifactName || '-' }}</template>
                </el-table-column>
                <el-table-column label="出土遗迹" prop="excavationRelic" width="140" />
                <el-table-column label="出土位置" prop="excavationPosition" width="140" />
                <el-table-column label="材质" prop="material1" width="120" />
                <el-table-column label="完整度" prop="completeness" width="80" />
                <el-table-column label="数量" prop="quantity1" width="70" />
                <el-table-column label="科技检测情况" prop="testingStatus" width="140" />
                <el-table-column label="操作" width="80" fixed="right">
                    <template #default="{ row }"><el-link type="primary" @click="openDetail(row)">详情</el-link></template>
                </el-table-column>
                <template #empty><el-empty description="该墓葬暂无出土文物" /></template>
            </el-table>
            <el-pagination
                v-model:current-page="pageNum" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                :total="artifacts.length" background style="margin-top:16px;justify-content:flex-end"
                @current-change="onPageChange"
            />
        </el-card>
    </div>

    <!-- 文物详情抽屉 -->
    <el-drawer v-model="detailVisible" title="文物详情" direction="rtl" size="55%">
        <el-descriptions :column="2" border>
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
        </el-descriptions>
    </el-drawer>

    <!-- 添加文物弹窗（与文物信息总览字段完全一致） -->
    <el-dialog v-model="addVisible" title="添加文物" width="760px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="addForm" label-width="100px">
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="文物新编号"><el-input v-model="addForm.newArtifactCode" placeholder="请输入新编号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="文物新名称"><el-input v-model="addForm.newArtifactName" placeholder="请输入新名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="原始编号"><el-input v-model="addForm.originalArtifactCode" placeholder="请输入原始编号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="原名称"><el-input v-model="addForm.originalArtifactName" placeholder="请输入原名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="材质1"><el-input v-model="addForm.material1" placeholder="请输入材质" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="材质2"><el-input v-model="addForm.material2" placeholder="请输入材质2" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="完整度"><el-input v-model="addForm.completeness" placeholder="请输入完整度" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="视觉特征"><el-input v-model="addForm.artifactDescription" type="textarea" :rows="2" placeholder="请输入视觉特征" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="数量1"><el-input v-model="addForm.quantity1" placeholder="请输入数量1" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="数量2"><el-input v-model="addForm.quantity2" placeholder="请输入数量2" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="尺寸"><el-input v-model="addForm.dimensions" placeholder="请输入尺寸" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="重量"><el-input v-model="addForm.weight" placeholder="请输入重量" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出土遗迹"><el-input v-model="addForm.excavationRelic" placeholder="请输入出土遗迹" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出土位置"><el-input v-model="addForm.excavationPosition" placeholder="请输入出土位置" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出土时间"><el-input v-model="addForm.excavationTime" placeholder="请输入出土时间" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="存放方式"><el-input v-model="addForm.storageMethod" placeholder="请输入存放方式" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="文物流转过程"><el-input v-model="addForm.transferProcess" placeholder="请输入流转过程" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="修复复原状况"><el-input v-model="addForm.restorationStatus" placeholder="请输入修复状况" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="拍照人"><el-input v-model="addForm.photographer" placeholder="请输入拍照人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="绘图人"><el-input v-model="addForm.draftsperson" placeholder="请输入绘图人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="文字描述人"><el-input v-model="addForm.textDescriber" placeholder="请输入文字描述人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="定级情况"><el-input v-model="addForm.gradingStatus" placeholder="请输入定级情况" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="科技检测情况"><el-input v-model="addForm.testingStatus" placeholder="请输入检测情况" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="备注"><el-input v-model="addForm.notes" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item></el-col>
            </el-row>
        </el-form>
        <template #footer>
            <el-button @click="addVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAdd">保存</el-button>
        </template>
    </el-dialog>

    <!-- 导入文物弹窗 -->
    <el-dialog v-model="importVisible" title="导入文物" width="500px" :close-on-click-modal="false">
        <el-form label-width="100px">
            <el-form-item>
                <el-button type="success" @click="downloadTemplate">获取模板</el-button>
            </el-form-item>
            <el-form-item label="Excel文件">
                <el-upload ref="uploadRef" :auto-upload="false" :on-change="handleImportFile" accept=".xlsx,.xls" :limit="1">
                    <el-button type="primary">选择文件</el-button>
                    <template #tip>请上传Excel文件（.xlsx或.xls格式）</template>
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="importVisible = false">取消</el-button>
            <el-button type="primary" @click="handleImport">开始导入</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.excavation-page { padding: 0; }
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