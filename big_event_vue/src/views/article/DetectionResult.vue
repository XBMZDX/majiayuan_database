<script setup>
import { ref, onMounted } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const list = ref([])
const fetchList = async () => { const res = await request.get('/admin/analysis-result'); list.value = res.data || [] }

// ========== 编辑弹窗 ==========
const editVisible = ref(false)
const editForm = ref({})
const openEdit = (item) => { editForm.value = { ...item }; editVisible.value = true }
const submitEdit = async () => {
    await request.put('/admin/analysis-result/' + editForm.value.id, editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false; fetchList()
}

// ========== 删除 ==========
const deleteItem = async (item) => {
    await ElMessageBox.confirm('确定删除"' + (item.artifactName || item.artifactCode) + '"吗？', '确认删除', { type: 'warning' })
    await request.delete('/admin/analysis-result/' + item.id)
    ElMessage.success('删除成功'); fetchList()
}

onMounted(fetchList)
</script>

<template>
    <div class="lab-page">
        <div class="lab-toolbar">
            <span class="lab-count">共 {{ list.length }} 个样品</span>
        </div>

        <!-- 卡片网格 -->
        <div class="lab-grid" v-if="list.length > 0">
            <div class="lab-card" v-for="item in list" :key="item.id">
                <!-- 顶部：文物名称 + 样品编号 -->
                <div class="card-top">
                    <span class="card-name">{{ item.artifactName || '-' }}</span>
                    <span class="card-code">{{ item.artifactCode || '-' }}</span>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 样品照片 -->
                <div class="card-image">
                    <el-image v-if="item.samplePhoto" :src="item.samplePhoto" fit="cover"
                              style="width:100%;height:180px;border-radius:6px" :preview-src-list="[item.samplePhoto]" />
                    <div v-else class="card-image-empty">暂无样品照片</div>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 检测目的（用户填写） + 实验方法（联动总览页"目的"） -->
                <div class="info-block">
                    <div class="info-row">
                        <span class="info-label">🔬 检测目的</span>
                        <span class="info-val">{{ item.detectionPurpose || '-' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">📋 实验方法</span>
                        <span class="info-val">{{ item.experimentMethod || '-' }}</span>
                    </div>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 仪器型号（用户填写） + 测试参数（用户填写） -->
                <div class="info-block">
                    <div class="info-row">
                        <span class="info-label">⚙ 仪器型号</span>
                        <span class="info-val">{{ item.instrumentModel || '-' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">📐 测试参数</span>
                        <span class="info-val">{{ item.testParams || '-' }}</span>
                    </div>
                </div>

                <el-divider style="margin:10px 0" />

                <!-- 操作按钮 -->
                <div class="card-bottom">
                    <el-tag :type="item.sampleStatus === '已完成' ? 'success' : item.sampleStatus === '检测中' ? 'warning' : 'info'" size="small">
                        {{ item.sampleStatus || '待检测' }}
                    </el-tag>
                    <div class="card-actions">
                        <el-button size="small" :icon="Edit" circle @click="openEdit(item)" />
                        <el-button size="small" :icon="Delete" circle type="danger" @click="deleteItem(item)" />
                    </div>
                </div>
            </div>
        </div>
        <div v-else class="lab-empty">暂无检测样品数据</div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑检测样品" width="600px" :close-on-click-modal="false" destroy-on-close>
        <el-form :model="editForm" label-width="90px">
            <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="文物编号"><el-input v-model="editForm.artifactCode" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="文物名称"><el-input v-model="editForm.artifactName" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="实验方法"><el-input :model-value="editForm.experimentMethod" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检测目的"><el-input v-model="editForm.detectionPurpose" placeholder="填写检测目的" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="仪器型号"><el-input v-model="editForm.instrumentModel" placeholder="填写仪器型号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="测试参数"><el-input v-model="editForm.testParams" placeholder="填写测试参数" /></el-form-item></el-col>
            </el-row>
        </el-form>
        <template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" @click="submitEdit">保存</el-button></template>
    </el-dialog>
</template>

<style scoped>
.lab-page { padding: 0; }
.lab-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.lab-count { font-size: 15px; color: #4E5969; font-weight: 600; }
.lab-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.lab-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 18px; transition: box-shadow 0.2s; }
.lab-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }

.card-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.card-name { font-size: 16px; font-weight: 700; color: #1D2129; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-code { font-size: 13px; color: #409EFF; font-weight: 600; white-space: nowrap; }

.card-image { height: 180px; border-radius: 6px; overflow: hidden; background: #F5F7FA; }
.card-image-empty { display: flex; align-items: center; justify-content: center; height: 100%; color: #C0C4CC; font-size: 14px; }

.info-block { display: flex; flex-direction: column; gap: 8px; }
.info-row { display: flex; align-items: center; gap: 6px; }
.info-label { font-size: 11px; color: #999; white-space: nowrap; min-width: 72px; }
.info-val { font-size: 12px; color: #4E5969; flex: 1; word-break: break-all; }

.card-bottom { display: flex; justify-content: space-between; align-items: center; }
.card-actions { display: flex; gap: 4px; }

.lab-empty { text-align: center; padding: 60px; color: #999; font-size: 14px; }

@media (max-width: 1400px) { .lab-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1000px) { .lab-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 680px) { .lab-grid { grid-template-columns: 1fr; } }
</style>
