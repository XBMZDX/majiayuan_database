<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

// 墓葬列表（全部详情）
const burialList = ref([])

const fetchBurialList = async () => {
    const res = await request.get('/admin/burial/list/simple')
    const simpleList = res.data || []
    const details = await Promise.all(
        simpleList.map(b => request.get(`/admin/burial/${b.id}`).then(r => r.data))
    )
    burialList.value = details
}

// ========== 详情对话框（双击打开） ==========
const detailVisible = ref(false)
const detailData = ref({})
const detailBackup = ref({})
const detailEditMode = ref(false)

// 双击卡片打开详情
const openDetail = (burial) => {
    detailData.value = { ...burial }
    detailEditMode.value = false
    detailVisible.value = true
}

// 进入编辑模式
const enterEditMode = () => {
    detailBackup.value = { ...detailData.value }
    detailEditMode.value = true
}

// 取消编辑
const cancelEdit = () => {
    detailData.value = { ...detailBackup.value }
    detailEditMode.value = false
}

// 保存编辑
const saveEdit = async () => {
    await request.put(`/admin/burial/${detailData.value.id}`, detailData.value)
    ElMessage.success('保存成功')
    detailEditMode.value = false
    fetchBurialList()
}

// 新建弹窗
const showDialog = ref(false)
const defaultForm = () => ({
    burialNo: '', name: '', siteName: '', era: '',
    burialType: '', excavationDate: null,
    hasCoffin: false, hasChariot: false,
    coffinCount: 0, coffinMaterial: '', coffinDecoration: '', skeletonStatus: '',
    chariotCount: 0, horseCount: 0, chariotDecoration: '', chariotType: '',
    artifactCount: 0, bonePreservation: '', status: '待发掘', notes: ''
})
const form = ref(defaultForm())

const openDialog = () => { form.value = defaultForm(); showDialog.value = true }
const submitBurial = async () => {
    try {
        const data = { ...form.value }
        // 没有名称时用编号作为名称
        if (!data.name) data.name = data.burialNo || '未命名墓葬'
        data.coffinCount = parseInt(data.coffinCount) || 0
        data.chariotCount = parseInt(data.chariotCount) || 0
        data.horseCount = parseInt(data.horseCount) || 0
        data.artifactCount = parseInt(data.artifactCount) || 0
        await request.post('/admin/burial', data)
        ElMessage.success('新增成功')
        showDialog.value = false
        fetchBurialList()
    } catch (err) {
        ElMessage.error(err.message || '保存失败')
    }
}

onMounted(() => { fetchBurialList() })
</script>

<template>
    <div class="tomb-page">
        <!-- 顶部操作栏 -->
        <div class="tomb-toolbar">
            <span class="tomb-count">共 {{ burialList.length }} 座墓葬</span>
            <el-button type="primary" :icon="Plus" @click="openDialog" size="large">添加新墓葬</el-button>
        </div>

        <!-- 墓葬卡片网格 -->
        <div class="tomb-grid" v-if="burialList.length > 0">
            <div class="tomb-card" v-for="b in burialList" :key="b.id" @dblclick="openDetail(b)">
                <div class="card-header">
                    <span class="card-title">{{ b.name && b.name !== b.burialNo ? b.burialNo + ' ' + b.name : b.burialNo }}</span>
                    <el-tag :type="b.status === '已发掘' ? 'success' : b.status === '发掘中' ? 'warning' : 'danger'" size="small">{{ b.status }}</el-tag>
                </div>
                <el-divider style="margin:8px 0" />
                <div class="info-item"><label>年代</label><span>{{ b.era || '-' }}</span></div>
                <div class="info-item"><label>墓葬形制</label><span>{{ b.burialType || '-' }}</span></div>
                <div class="info-item"><label>发掘时间</label><span>{{ b.excavationDate || '-' }}</span></div>
                <div class="info-item"><label>人骨保存</label><span>{{ b.bonePreservation || '-' }}</span></div>
                <el-divider style="margin:8px 0" />
                <div style="font-size:12px;font-weight:600;margin-bottom:4px">葬具配置</div>
                <div class="info-item"><label>有棺</label><span>{{ b.hasCoffin ? '是（' + b.coffinCount + '）' : '否' }}</span></div>
                <div class="info-item"><label>有车</label><span>{{ b.hasChariot ? '是（车' + b.chariotCount + ' 马' + b.horseCount + '）' : '否' }}</span></div>
            </div>
        </div>
        <div v-else class="tomb-empty">暂无墓葬数据，请添加新墓葬</div>
    </div>

    <!-- 新增墓葬对话框 -->
    <el-dialog v-model="showDialog" title="新建墓葬" width="760px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="form" label-width="90px">
            <el-divider content-position="left">基本信息</el-divider>
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="墓葬编号"><el-input v-model="form.burialNo" placeholder="如 M001" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年代"><el-input v-model="form.era" placeholder="如 战国中期" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="墓葬形制"><el-input v-model="form.burialType" placeholder="如 竖穴土坑" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发掘时间"><el-date-picker v-model="form.excavationDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="墓葬状态">
                    <el-select v-model="form.status" style="width:100%"><el-option label="待发掘" value="待发掘" /><el-option label="发掘中" value="发掘中" /><el-option label="已发掘" value="已发掘" /></el-select>
                </el-form-item></el-col>
            </el-row>

            <el-divider content-position="left">葬具配置</el-divider>
            <el-row :gutter="16" align="middle">
                <el-col :span="12">
                    <div style="display:flex;align-items:center;gap:8px">
                        <el-checkbox v-model="form.hasCoffin">有棺</el-checkbox>
                        <template v-if="form.hasCoffin">
                            <span style="font-size:13px;color:#606266">棺数</span>
                            <el-input v-model="form.coffinCount" placeholder="0" style="width:80px" />
                        </template>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div style="display:flex;align-items:center;gap:8px">
                        <el-checkbox v-model="form.hasChariot">有车</el-checkbox>
                        <template v-if="form.hasChariot">
                            <span style="font-size:13px;color:#606266">车数</span>
                            <el-input v-model="form.chariotCount" placeholder="0" style="width:70px" />
                            <span style="font-size:13px;color:#606266">马数</span>
                            <el-input v-model="form.horseCount" placeholder="0" style="width:70px" />
                        </template>
                    </div>
                </el-col>
            </el-row>

            <el-divider content-position="left">其他信息</el-divider>
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="人骨保存"><el-select v-model="form.bonePreservation" style="width:100%"><el-option label="完整" value="完整" /><el-option label="基本完整" value="基本完整" /><el-option label="残" value="残" /><el-option label="腐朽" value="腐朽" /></el-select></el-form-item></el-col>
            </el-row>
            <el-form-item label="备注"><el-input v-model="form.notes" type="textarea" :rows="2" placeholder="如 墓底有腰坑，殉人1具" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="showDialog = false">取消</el-button>
            <el-button type="primary" @click="submitBurial">保存</el-button>
        </template>
    </el-dialog>

    <!-- 墓葬详情对话框（双击打开） -->
    <el-dialog v-model="detailVisible" :title="detailEditMode ? '编辑墓葬' : '墓葬详情'" width="680px" :close-on-click-modal="false">
        <!-- 只读模式 -->
        <template v-if="!detailEditMode">
            <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="墓葬编号">{{ detailData.burialNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="年代">{{ detailData.era || '-' }}</el-descriptions-item>
                <el-descriptions-item label="墓葬形制">{{ detailData.burialType || '-' }}</el-descriptions-item>
                <el-descriptions-item label="发掘时间">{{ detailData.excavationDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="墓葬状态">
                    <el-tag :type="detailData.status === '已发掘' ? 'success' : detailData.status === '发掘中' ? 'warning' : 'danger'" size="small">{{ detailData.status || '-' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="人骨保存">{{ detailData.bonePreservation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="有棺">{{ detailData.hasCoffin ? '是（' + detailData.coffinCount + '）' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="有车">{{ detailData.hasChariot ? '是（车' + detailData.chariotCount + ' 马' + detailData.horseCount + '）' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top:20px;text-align:right">
                <el-button type="primary" :icon="Edit" @click="enterEditMode">编辑</el-button>
            </div>
        </template>
        <!-- 编辑模式 -->
        <template v-else>
            <el-form :model="detailData" label-width="90px">
                <el-divider content-position="left">基本信息</el-divider>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="墓葬编号"><el-input v-model="detailData.burialNo" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="年代"><el-input v-model="detailData.era" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="墓葬形制"><el-input v-model="detailData.burialType" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="发掘时间"><el-date-picker v-model="detailData.excavationDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="墓葬状态">
                        <el-select v-model="detailData.status" style="width:100%"><el-option label="待发掘" value="待发掘" /><el-option label="发掘中" value="发掘中" /><el-option label="已发掘" value="已发掘" /></el-select>
                    </el-form-item></el-col>
                </el-row>
                <el-divider content-position="left">葬具配置</el-divider>
                <el-row :gutter="16" align="middle">
                    <el-col :span="12">
                        <div style="display:flex;align-items:center;gap:8px">
                            <el-checkbox v-model="detailData.hasCoffin">有棺</el-checkbox>
                            <template v-if="detailData.hasCoffin">
                                <span style="font-size:13px;color:#606266">棺数</span>
                                <el-input v-model="detailData.coffinCount" style="width:80px" />
                            </template>
                        </div>
                    </el-col>
                    <el-col :span="12">
                        <div style="display:flex;align-items:center;gap:8px">
                            <el-checkbox v-model="detailData.hasChariot">有车</el-checkbox>
                            <template v-if="detailData.hasChariot">
                                <span style="font-size:13px;color:#606266">车数</span>
                                <el-input v-model="detailData.chariotCount" style="width:70px" />
                                <span style="font-size:13px;color:#606266">马数</span>
                                <el-input v-model="detailData.horseCount" style="width:70px" />
                            </template>
                        </div>
                    </el-col>
                </el-row>
                <el-divider content-position="left">其他信息</el-divider>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="人骨保存">
                        <el-select v-model="detailData.bonePreservation" style="width:100%"><el-option label="完整" value="完整" /><el-option label="基本完整" value="基本完整" /><el-option label="残" value="残" /><el-option label="腐朽" value="腐朽" /></el-select>
                    </el-form-item></el-col>
                </el-row>
                <el-form-item label="备注"><el-input v-model="detailData.notes" type="textarea" :rows="2" /></el-form-item>
            </el-form>
            <div style="margin-top:20px;text-align:right">
                <el-button @click="cancelEdit">取消</el-button>
                <el-button type="primary" @click="saveEdit">保存</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<style scoped>
.tomb-page { padding: 0; }
.tomb-toolbar { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; margin-bottom: 16px; border-bottom: 1px solid #E5E6EB; }
.tomb-count { font-size: 14px; color: #666; }
.tomb-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.tomb-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 8px; padding: 16px; transition: box-shadow 0.2s; }
.tomb-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 700; color: #1D2129; }
.info-item { margin-bottom: 6px; }
.info-item label { font-size: 11px; color: #999; display: block; margin-bottom: 1px; }
.info-item span { font-size: 13px; color: #1D2129; }
.tomb-empty { text-align: center; padding: 60px; color: #999; font-size: 14px; }
</style>