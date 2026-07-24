<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Picture, Upload } from '@element-plus/icons-vue'
import { deleteQuickRecordMedia, getQuickRecord, saveQuickRecord, uploadQuickRecordMedia } from '@/api/conservationProject'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId) || null)
const loading = ref(true)
const saving = ref(false)
const project = ref(null)
const record = reactive({ issueDescription: '', treatmentMethod: '', operatorName: '', recordDate: '', conclusion: '', remark: '', recordStatus: 'draft', media: [] })
const beforeMedia = computed(() => record.media.filter(item => item.mediaRole === 'before'))
const afterMedia = computed(() => record.media.filter(item => item.mediaRole === 'after'))

const apply = data => {
    project.value = data.project || null
    Object.assign(record, { issueDescription: '', treatmentMethod: '', operatorName: '', recordDate: '', conclusion: '', remark: '', recordStatus: 'draft', media: [], ...(data.record || {}) })
}
const load = async () => {
    loading.value = true
    try {
        const response = await getQuickRecord(projectId.value)
        apply(response.data || {})
        if (project.value?.recordMode !== 'quick') {
            ElMessage.warning('该项目不是快速记录模式')
            router.replace('/conservation/overview')
        }
    } catch (error) {
        ElMessage.error(error?.message || '快速记录加载失败')
    } finally { loading.value = false }
}
const save = async completed => {
    if (!record.issueDescription?.trim()) return ElMessage.warning('请填写修复问题或现状')
    if (!record.treatmentMethod?.trim()) return ElMessage.warning('请填写处理方式')
    if (completed && !record.conclusion?.trim()) return ElMessage.warning('完成处理前请填写修复结论')
    saving.value = true
    try {
        const response = await saveQuickRecord(projectId.value, { ...record, completed })
        apply(response.data || {})
        ElMessage.success(completed ? '快速修复记录已完成' : '快速修复记录已保存')
    } catch (error) {
        ElMessage.error(error?.message || '保存失败')
    } finally { saving.value = false }
}
const upload = async (uploadFile, role) => {
    const raw = uploadFile.raw
    if (!raw) return
    const data = new FormData()
    data.append('file', raw)
    data.append('mediaRole', role)
    try {
        const response = await uploadQuickRecordMedia(projectId.value, data)
        record.media.push(response.data)
        ElMessage.success(role === 'before' ? '修复前照片已上传' : '修复后照片已上传')
    } catch (error) {
        ElMessage.error(error?.message || '图片上传失败')
    }
}
const removeMedia = async media => {
    try {
        await ElMessageBox.confirm(`确认删除“${media.fileName}”吗？`, '删除图片', { type: 'warning' })
        await deleteQuickRecordMedia(media.id)
        record.media.splice(record.media.indexOf(media), 1)
    } catch (error) {
        if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '删除失败')
    }
}
onMounted(load)
</script>

<template>
    <main class="quick-page" v-loading="loading">
        <header class="page-header">
            <div><el-button link :icon="ArrowLeft" @click="router.push('/conservation/overview')">返回项目总览</el-button><h1>快速修复记录</h1><p>适用于简单、小型文物的清洁、加固和局部处理；无需填写完整修复流程。</p></div>
            <div class="actions"><el-button :loading="saving" @click="save(false)">保存草稿</el-button><el-button type="success" :icon="Check" :loading="saving" @click="save(true)">完成处理</el-button></div>
        </header>
        <section v-if="project" class="project-summary"><b>{{ project.artifactCode }} {{ project.artifactName }}</b><span>项目：{{ project.projectName }}</span><span>负责人：{{ project.principal || '-' }}</span><el-tag :type="record.recordStatus === 'completed' ? 'success' : 'warning'">{{ record.recordStatus === 'completed' ? '已完成' : '记录中' }}</el-tag></section>
        <section class="record-card">
            <h2>基本记录</h2>
            <el-form label-position="top"><div class="form-grid">
                <el-form-item class="wide" label="修复问题 / 现状"><el-input v-model="record.issueDescription" type="textarea" :rows="3" placeholder="例如：珠体表面附着污渍，穿孔边缘局部松动" /></el-form-item>
                <el-form-item class="wide" label="处理方式"><el-input v-model="record.treatmentMethod" type="textarea" :rows="3" placeholder="例如：干式软毛刷清洁；使用适宜材料对松动部位进行局部加固" /></el-form-item>
                <el-form-item label="操作人"><el-input v-model="record.operatorName" /></el-form-item>
                <el-form-item label="处理日期"><el-date-picker v-model="record.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
                <el-form-item class="wide" label="修复结论"><el-input v-model="record.conclusion" type="textarea" :rows="3" placeholder="例如：表面清洁完成，结构稳定，无需进一步处理" /></el-form-item>
                <el-form-item class="wide" label="备注（可选）"><el-input v-model="record.remark" type="textarea" :rows="2" /></el-form-item>
            </div></el-form>
        </section>
        <section class="photo-grid">
            <article class="record-card"><div class="section-title"><div><h2>修复前照片</h2><p>建议至少上传一张，记录处理前状态。</p></div><el-upload :show-file-list="false" :auto-upload="false" accept="image/*" @change="file => upload(file, 'before')"><el-button type="primary" plain :icon="Upload">上传照片</el-button></el-upload></div><div v-if="beforeMedia.length" class="photos"><figure v-for="media in beforeMedia" :key="media.id"><img :src="media.fileUrl" :alt="media.fileName"><figcaption>{{ media.fileName }}<el-button link type="danger" @click="removeMedia(media)">删除</el-button></figcaption></figure></div><el-empty v-else :image-size="52" description="尚未上传修复前照片"><Picture /></el-empty></article>
            <article class="record-card"><div class="section-title"><div><h2>修复后照片</h2><p>完成处理后上传，用于直观记录结果。</p></div><el-upload :show-file-list="false" :auto-upload="false" accept="image/*" @change="file => upload(file, 'after')"><el-button type="success" plain :icon="Upload">上传照片</el-button></el-upload></div><div v-if="afterMedia.length" class="photos"><figure v-for="media in afterMedia" :key="media.id"><img :src="media.fileUrl" :alt="media.fileName"><figcaption>{{ media.fileName }}<el-button link type="danger" @click="removeMedia(media)">删除</el-button></figcaption></figure></div><el-empty v-else :image-size="52" description="尚未上传修复后照片"><Picture /></el-empty></article>
        </section>
    </main>
</template>

<style scoped>
.quick-page{max-width:1180px;margin:0 auto;padding:24px;background:#f4f6f5;min-height:calc(100vh - 64px)}.page-header{display:flex;justify-content:space-between;gap:20px;align-items:center;margin-bottom:16px}.page-header h1{margin:7px 0;font-size:25px;color:#29453c}.page-header p{margin:0;color:#708078;font-size:13px}.actions{display:flex;gap:10px}.project-summary,.record-card{background:#fff;border:1px solid #e2e9e5;border-radius:10px;padding:18px;box-shadow:0 1px 2px #00000005}.project-summary{display:flex;align-items:center;gap:18px;margin-bottom:16px;color:#66766e;font-size:13px}.project-summary b{font-size:16px;color:#30473e}.project-summary .el-tag{margin-left:auto}.record-card h2{margin:0;color:#39584b;font-size:17px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 18px;margin-top:12px}.wide{grid-column:1/-1}.photo-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-top:16px}.section-title{display:flex;justify-content:space-between;gap:12px;align-items:flex-start}.section-title p{margin:5px 0 14px;color:#89958f;font-size:12px}.photos{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px}.photos figure{margin:0;border:1px solid #e6ece8;border-radius:7px;overflow:hidden;background:#fafcfa}.photos img{width:100%;height:175px;object-fit:cover;display:block}.photos figcaption{padding:7px 9px;font-size:12px;display:flex;justify-content:space-between;gap:6px;align-items:center;word-break:break-all}@media(max-width:720px){.quick-page{padding:14px}.page-header,.project-summary{align-items:flex-start;flex-direction:column}.project-summary .el-tag{margin-left:0}.form-grid,.photo-grid{grid-template-columns:1fr}.photos{grid-template-columns:1fr}.actions{width:100%}.actions .el-button{flex:1}}
</style>
