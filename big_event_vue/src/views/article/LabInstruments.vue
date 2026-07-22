<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { useTokenStore } from '@/stores/token.js'

const router = useRouter()
const token = computed(() => useTokenStore().token)
const list = ref([])
const loading = ref(false)
const filters = ref({ material: '', purpose: '', nonDestructive: '', requiresSampling: '', location: '' })
const dialogVisible = ref(false)
const editingId = ref(null)
const defaultForm = () => ({
    name: '', image: '', scope: '', location: '', model: '', conditions: '', method: '', methodName: '',
    applicableMaterials: '', researchPurposes: '', nonDestructive: false, requiresSampling: false, mainOutputs: ''
})
const form = ref(defaultForm())

const splitValues = value => String(value || '')
    .split(/[\/，,；;]/)
    .map(item => item.trim())
    .filter(Boolean)

const toBoolean = value => value === true || value === 1 || value === '1' || value === 'true'
const yesNo = value => toBoolean(value) ? '是' : '否'

const fetchList = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/lab-instrument')
        list.value = res.data || []
    } finally {
        loading.value = false
    }
}

const materialOptions = computed(() => [...new Set(list.value.flatMap(item => splitValues(item.applicableMaterials)))])
const purposeOptions = computed(() => [...new Set(list.value.flatMap(item => splitValues(item.researchPurposes)))])
const locationOptions = computed(() => [...new Set(list.value.map(item => item.location).filter(Boolean))])

const filteredList = computed(() => list.value.filter(item => {
    const materials = splitValues(item.applicableMaterials)
    const purposes = splitValues(item.researchPurposes)
    return (!filters.value.material || materials.includes(filters.value.material))
        && (!filters.value.purpose || purposes.includes(filters.value.purpose))
        && (filters.value.nonDestructive === '' || toBoolean(item.nonDestructive) === (filters.value.nonDestructive === 'true'))
        && (filters.value.requiresSampling === '' || toBoolean(item.requiresSampling) === (filters.value.requiresSampling === 'true'))
        && (!filters.value.location || item.location === filters.value.location)
}))

const resetFilters = () => { filters.value = { material: '', purpose: '', nonDestructive: '', requiresSampling: '', location: '' } }
const openMethod = url => window.open(url, '_blank', 'noopener')
const viewDetectedArtifacts = item => router.push({ path: '/detection/overview', query: { method: item.name } })
const openAdd = () => { editingId.value = null; form.value = defaultForm(); dialogVisible.value = true }
const openEdit = item => {
    editingId.value = item.id
    form.value = { ...defaultForm(), ...item, nonDestructive: toBoolean(item.nonDestructive), requiresSampling: toBoolean(item.requiresSampling) }
    dialogVisible.value = true
}
const saveForm = async () => {
    if (!form.value.name.trim()) { ElMessage.warning('请填写实验名称'); return }
    if (editingId.value) {
        await request.put(`/admin/lab-instrument/${editingId.value}`, form.value)
        ElMessage.success('检测实验已更新')
    } else {
        await request.post('/admin/lab-instrument', form.value)
        ElMessage.success('检测实验已新增')
    }
    dialogVisible.value = false
    await fetchList()
}
const removeItem = async item => {
    try {
        await ElMessageBox.confirm(`确定删除“${item.name}”吗？删除后该实验将不再出现在实验总览中。`, '删除检测实验', { type: 'warning' })
        await request.delete(`/admin/lab-instrument/${item.id}`)
        ElMessage.success('检测实验已删除')
        await fetchList()
    } catch (_) {
        // 取消删除时不需要提示。
    }
}
const onImageUploaded = response => { form.value.image = response.data || '' }
const onMethodUploaded = response => { form.value.method = response.data || '' }
const captureMethodName = file => { form.value.methodName = file.name; return true }

onMounted(fetchList)
</script>

<template>
    <div class="experiment-page" v-loading="loading">
        <section class="page-intro">
            <div>
                <h2>检测实验总览</h2>
                <p>浏览可开展的检测实验，了解其适用对象、研究用途和可获得的资料。</p>
            </div>
            <div class="intro-actions">
                <span class="experiment-count">共 {{ filteredList.length }} 项实验</span>
                <el-button type="primary" :icon="Plus" @click="openAdd">新增检测实验</el-button>
            </div>
        </section>

        <el-card shadow="never" class="filter-card">
            <div class="filter-bar">
                <el-select v-model="filters.material" clearable filterable placeholder="适用文物材质">
                    <el-option v-for="item in materialOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="filters.purpose" clearable filterable placeholder="研究目的">
                    <el-option v-for="item in purposeOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="filters.nonDestructive" clearable placeholder="是否无损">
                    <el-option label="无损检测" value="true" />
                    <el-option label="非无损检测" value="false" />
                </el-select>
                <el-select v-model="filters.requiresSampling" clearable placeholder="是否需要取样">
                    <el-option label="需要取样" value="true" />
                    <el-option label="无需取样" value="false" />
                </el-select>
                <el-select v-model="filters.location" clearable filterable placeholder="实验地点">
                    <el-option v-for="item in locationOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-button @click="resetFilters">重置</el-button>
            </div>
        </el-card>

        <div v-if="filteredList.length" class="experiment-grid">
            <article v-for="item in filteredList" :key="item.id" class="experiment-card">
                <div class="card-heading">
                    <div>
                        <h3>{{ item.name }}</h3>
                        <span class="location">{{ item.location || '未填写实验地点' }}</span>
                    </div>
                    <div class="card-tools">
                        <div class="feature-tags">
                        <el-tag :type="toBoolean(item.nonDestructive) ? 'success' : 'info'" size="small">{{ toBoolean(item.nonDestructive) ? '无损检测' : '非无损检测' }}</el-tag>
                        <el-tag :type="toBoolean(item.requiresSampling) ? 'warning' : 'success'" size="small">{{ toBoolean(item.requiresSampling) ? '需要取样' : '无需取样' }}</el-tag>
                        </div>
                        <div class="card-actions">
                            <el-button text type="primary" :icon="Edit" @click="openEdit(item)">编辑</el-button>
                            <el-button text type="danger" :icon="Delete" @click="removeItem(item)">删除</el-button>
                        </div>
                    </div>
                </div>

                <div class="instrument-image">
                    <el-image v-if="item.image" :src="item.image" fit="cover" :preview-src-list="[item.image]" />
                    <div v-else class="image-placeholder">暂无仪器照片</div>
                </div>

                <div class="card-content">
                    <div class="info-row">
                        <span class="info-label">适用文物材质</span>
                        <div class="tag-list">
                            <el-tag v-for="value in splitValues(item.applicableMaterials)" :key="value" size="small" effect="plain">{{ value }}</el-tag>
                            <span v-if="!splitValues(item.applicableMaterials).length" class="empty-value">未填写</span>
                        </div>
                    </div>
                    <div class="info-row"><span class="info-label">适用场景</span><span class="info-value">{{ item.scope || '未填写' }}</span></div>
                    <div class="info-row">
                        <span class="info-label">可解决的问题</span>
                        <div class="tag-list">
                            <el-tag v-for="value in splitValues(item.researchPurposes)" :key="value" size="small" type="success" effect="plain">{{ value }}</el-tag>
                            <span v-if="!splitValues(item.researchPurposes).length" class="empty-value">未填写</span>
                        </div>
                    </div>
                    <div class="info-row"><span class="info-label">主要产出</span><span class="info-value">{{ item.mainOutputs || '未填写' }}</span></div>
                    <div class="info-row"><span class="info-label">检测条件</span><span class="info-value">{{ item.conditions || '未填写' }}</span></div>
                    <div class="info-row"><span class="info-label">仪器型号</span><span class="info-value">{{ item.model || '未填写' }}</span></div>
                    <div class="info-row">
                        <span class="info-label">参考方法文档</span>
                        <el-link v-if="item.method" type="primary" :underline="false" @click="openMethod(item.method)">{{ item.methodName || '查看方法文档' }}</el-link>
                        <span v-else class="empty-value">未上传</span>
                    </div>
                </div>

                <div class="stat-row">
                    <div><strong>{{ item.appliedArtifactCount || 0 }}</strong><span>已应用文物</span></div>
                    <div><strong>{{ item.completedDetectionCount || 0 }}</strong><span>已完成检测</span></div>
                </div>
                <el-button type="primary" plain class="detected-button" @click="viewDetectedArtifacts(item)">查看已检测文物</el-button>
            </article>
        </div>
        <el-empty v-else description="没有符合筛选条件的检测实验" :image-size="110">
            <el-button @click="resetFilters">清除筛选</el-button>
        </el-empty>

        <el-dialog v-model="dialogVisible" :title="editingId ? '编辑检测实验' : '新增检测实验'" width="760px" destroy-on-close :close-on-click-modal="false">
            <el-form :model="form" label-width="112px" class="experiment-form">
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="实验名称" required><el-input v-model="form.name" placeholder="例如：超景深显微镜观察" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="实验地点"><el-input v-model="form.location" placeholder="例如：实验室 A-102" /></el-form-item></el-col>
                    <el-col :span="24"><el-form-item label="适用场景"><el-input v-model="form.scope" type="textarea" :rows="2" placeholder="说明该实验适用的文物情况或研究场景" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="适用文物材质"><el-input v-model="form.applicableMaterials" placeholder="多个值请用 / 分隔" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="研究目的"><el-input v-model="form.researchPurposes" placeholder="多个值请用 / 分隔" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="是否无损"><el-switch v-model="form.nonDestructive" active-text="无损" inactive-text="非无损" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="是否需要取样"><el-switch v-model="form.requiresSampling" active-text="需要" inactive-text="无需" /></el-form-item></el-col>
                    <el-col :span="24"><el-form-item label="主要产出"><el-input v-model="form.mainOutputs" type="textarea" :rows="2" placeholder="例如：显微图像、表面形貌描述、测量数据" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="仪器型号"><el-input v-model="form.model" placeholder="填写仪器型号" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="检测条件"><el-input v-model="form.conditions" placeholder="填写环境、倍率或其他条件" /></el-form-item></el-col>
                </el-row>
                <el-divider content-position="left">仪器照片</el-divider>
                <el-form-item label="上传照片">
                    <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="false" :on-success="onImageUploaded" accept="image/*">
                        <el-button>选择照片</el-button>
                    </el-upload>
                    <el-link v-if="form.image" type="primary" style="margin-left: 12px" @click="openMethod(form.image)">查看当前照片</el-link>
                </el-form-item>
                <el-divider content-position="left">参考方法文档</el-divider>
                <el-form-item label="上传文档">
                    <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="false" :before-upload="captureMethodName" :on-success="onMethodUploaded" accept=".pdf,.doc,.docx">
                        <el-button>选择文档</el-button>
                    </el-upload>
                    <el-link v-if="form.method" type="primary" style="margin-left: 12px" @click="openMethod(form.method)">{{ form.methodName || '查看当前文档' }}</el-link>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveForm">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.experiment-page { padding: 2px 0 24px; }
.page-intro { display: flex; justify-content: space-between; gap: 20px; align-items: flex-end; margin-bottom: 18px; }
.page-intro h2 { margin: 0 0 7px; color: #1d2129; font-size: 21px; }
.page-intro p { margin: 0; color: #86909c; font-size: 14px; }
.intro-actions { display: flex; align-items: center; gap: 14px; }
.experiment-count { color: #4e5969; font-size: 14px; white-space: nowrap; }
.filter-card { margin-bottom: 18px; border-color: #e5e6eb; }
.filter-bar { display: grid; grid-template-columns: repeat(5, minmax(130px, 1fr)) auto; gap: 12px; }
.experiment-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.experiment-card { display: flex; flex-direction: column; overflow: hidden; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; transition: box-shadow .2s, transform .2s; }
.experiment-card:hover { box-shadow: 0 5px 18px rgba(31, 35, 41, .10); transform: translateY(-2px); }
.card-heading { display: flex; justify-content: space-between; gap: 10px; padding: 17px 17px 13px; }
.card-heading h3 { margin: 0 0 6px; color: #1d2129; font-size: 16px; line-height: 1.4; }
.location { color: #86909c; font-size: 12px; }
.card-tools { display: flex; min-width: 130px; flex-direction: column; align-items: flex-end; gap: 7px; }
.feature-tags, .card-actions { display: flex; align-items: flex-start; justify-content: flex-end; flex-wrap: wrap; gap: 5px; }
.card-actions :deep(.el-button) { margin-left: 0; padding: 0 2px; font-size: 12px; }
.instrument-image { height: 178px; background: #f2f3f5; }
.instrument-image :deep(.el-image) { width: 100%; height: 100%; display: block; }
.image-placeholder { display: grid; height: 100%; place-items: center; color: #86909c; font-size: 13px; }
.card-content { display: grid; gap: 10px; padding: 16px 17px; }
.info-row { display: grid; grid-template-columns: 88px minmax(0, 1fr); align-items: start; gap: 8px; font-size: 13px; line-height: 1.55; }
.info-label { color: #86909c; }
.info-value { color: #4e5969; word-break: break-word; }
.empty-value { color: #c9cdd4; }
.tag-list { display: flex; flex-wrap: wrap; gap: 5px; }
.stat-row { display: grid; grid-template-columns: 1fr 1fr; margin: auto 17px 14px; border: 1px solid #e5e6eb; border-radius: 7px; overflow: hidden; }
.stat-row div { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 9px; }
.stat-row div + div { border-left: 1px solid #e5e6eb; }
.stat-row strong { color: #1d2129; font-size: 18px; }
.stat-row span { color: #86909c; font-size: 12px; }
.detected-button { margin: 0 17px 17px; }
.experiment-form :deep(.el-switch) { --el-switch-on-color: #14a67b; }
@media (max-width: 1280px) { .experiment-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .filter-bar { grid-template-columns: repeat(3, minmax(150px, 1fr)); } }
@media (max-width: 720px) { .page-intro { align-items: flex-start; flex-direction: column; } .intro-actions { width: 100%; justify-content: space-between; } .experiment-grid, .filter-bar { grid-template-columns: 1fr; } }
</style>
