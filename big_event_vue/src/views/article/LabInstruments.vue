<script setup>
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { useTokenStore } from '@/stores/token.js'
const token = computed(() => useTokenStore().token)

const list = ref([])
const fetchList = async () => { const res = await request.get('/admin/lab-instrument'); list.value = res.data || [] }

// ========== 新增/编辑弹窗 ==========
const showDialog = ref(false)
const editId = ref(null)
const defaultForm = () => ({ name: '', image: '', scope: '', location: '', model: '', conditions: '', method: '', methodName: '' })
const form = ref(defaultForm())

const openAdd = () => { form.value = defaultForm(); editId.value = null; showDialog.value = true }
const openEdit = (item) => { form.value = { ...item }; editId.value = item.id; showDialog.value = true }

const submit = async () => {
    if (!form.value.name.trim()) { ElMessage.warning('请输入实验名字'); return }
    if (editId.value) {
        await request.put('/admin/lab-instrument/' + editId.value, form.value)
        ElMessage.success('修改成功')
    } else {
        await request.post('/admin/lab-instrument', form.value)
        ElMessage.success('添加成功')
    }
    showDialog.value = false; fetchList()
}

const deleteItem = async (item) => {
    await ElMessageBox.confirm('确定删除"' + item.name + '"吗？', '确认删除', { type: 'warning' })
    await request.delete('/admin/lab-instrument/' + item.id)
    ElMessage.success('删除成功'); fetchList()
}

// 文件上传
const uploadSuccess = (res) => { form.value.image = res.data }
const uploadMethodSuccess = (res) => { form.value.method = res.data }
const beforeMethodUpload = (file) => { form.value.methodName = file.name; return true }

const openMethod = (url) => { window.open(url, '_blank') }

onMounted(fetchList)
</script>

<template>
    <div class="lab-page">
        <div class="lab-toolbar">
            <span class="lab-count">共 {{ list.length }} 台仪器</span>
            <el-button type="primary" :icon="Plus" @click="openAdd" size="large">添加仪器</el-button>
        </div>

        <!-- 卡片网格 -->
        <div class="lab-grid" v-if="list.length > 0">
            <div class="lab-card" v-for="item in list" :key="item.id">
                <!-- 顶部：实验名 + 按钮 -->
                <div class="card-top">
                    <span class="card-name">🔬 {{ item.name }}</span>
                    <div class="card-actions">
                        <el-button size="small" :icon="Edit" circle @click="openEdit(item)" />
                        <el-button size="small" :icon="Delete" circle type="danger" @click="deleteItem(item)" />
                    </div>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 仪器照片 -->
                <div class="card-image">
                    <el-image v-if="item.image" :src="item.image" fit="cover" style="width:100%;height:180px;border-radius:6px" :preview-src-list="[item.image]" />
                    <div v-else class="card-image-empty">暂无照片</div>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 实验范围 + 地点 -->
                <div class="info-row"><span class="info-label">📍 实验范围</span><span class="info-val">{{ item.scope || '-' }}</span></div>
                <div class="info-row"><span class="info-label">📍 实验地点</span><span class="info-val">{{ item.location || '-' }}</span></div>

                <el-divider style="margin:10px 0" />

                <!-- 型号 + 条件 + 方法 -->
                <div class="info-row"><span class="info-label">🔧 仪器型号</span><span class="info-val">{{ item.model || '-' }}</span></div>
                <div class="info-row"><span class="info-label">📋 实验条件</span><span class="info-val">{{ item.conditions || '-' }}</span></div>
                <div class="info-row">
                    <span class="info-label">📄 实验方法</span>
                    <span class="info-val">
                        <el-link v-if="item.method" type="primary" :underline="false" style="cursor:pointer;font-size:12px" @click="openMethod(item.method)">📎 {{ item.methodName || '查看文档' }}</el-link>
                        <span v-else>-</span>
                    </span>
                </div>
            </div>
        </div>
        <div v-else class="lab-empty">暂无仪器数据，请添加新仪器</div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑仪器' : '新增仪器'" width="680px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="form" label-width="90px">
            <el-row :gutter="16">
                <el-col :span="24"><el-form-item label="实验名字"><el-input v-model="form.name" placeholder="如 超景深三维显微镜 XH-3000" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="实验范围"><el-input v-model="form.scope" placeholder="如 金属/非金属/有机类文物" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="实验地点"><el-input v-model="form.location" placeholder="如 实验室A-102" /></el-form-item></el-col>
            </el-row>

            <el-divider content-position="left">仪器照片</el-divider>
            <el-form-item label="照片上传">
                <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true" :on-success="uploadSuccess" accept="image/*">
                    <el-button type="primary">选择照片</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item v-if="form.image" label="预览">
                <el-image :src="form.image" fit="cover" style="width:200px;height:120px;border-radius:6px" />
            </el-form-item>

            <el-divider content-position="left">技术参数</el-divider>
            <el-form-item label="仪器型号"><el-input v-model="form.model" type="textarea" :rows="2" placeholder="如 XH-3000 Pro，详细型号描述..." /></el-form-item>
            <el-form-item label="实验条件"><el-input v-model="form.conditions" type="textarea" :rows="3" placeholder="如 温度25℃ 湿度45% 真空环境..." /></el-form-item>

            <el-divider content-position="left">实验方法文档</el-divider>
            <el-form-item label="方法文档">
                <el-upload action="/api/upload" name="file" :headers="{ Authorization: token }" :show-file-list="true" :auto-upload="true" :on-success="uploadMethodSuccess" :before-upload="beforeMethodUpload" accept=".pdf,.doc,.docx">
                    <el-button type="primary">上传文档</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item v-if="form.method" label="已上传"><el-link type="primary" :underline="false" style="font-size:13px">📎 {{ form.methodName || '查看' }}</el-link></el-form-item>
        </el-form>
        <template #footer><el-button @click="showDialog = false">取消</el-button><el-button type="primary" @click="submit">确定</el-button></template>
    </el-dialog>
</template>

<style scoped>
.lab-page { padding: 0; }
.lab-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.lab-count { font-size: 15px; color: #4E5969; font-weight: 600; }
.lab-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.lab-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 18px; transition: box-shadow 0.2s; }
.lab-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.card-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 6px; }
.card-name { font-size: 16px; font-weight: 700; color: #1D2129; flex: 1; line-height: 1.4; }
.card-actions { display: flex; gap: 4px; flex-shrink: 0; }
.card-image { height: 180px; border-radius: 6px; overflow: hidden; background: #F5F7FA; }
.card-image-empty { display: flex; align-items: center; justify-content: center; height: 100%; color: #C0C4CC; font-size: 14px; }
.info-row { display: flex; align-items: flex-start; gap: 4px; margin-bottom: 6px; }
.info-label { font-size: 11px; color: #999; white-space: nowrap; min-width: 70px; }
.info-val { font-size: 12px; color: #4E5969; flex: 1; word-break: break-all; }
.lab-empty { text-align: center; padding: 60px; color: #999; font-size: 14px; }

@media (max-width: 1400px) { .lab-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1000px) { .lab-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 680px) { .lab-grid { grid-template-columns: 1fr; } }
</style>
