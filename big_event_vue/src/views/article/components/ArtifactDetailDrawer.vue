<script setup>
import { computed, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    artifactsUpdateService,
    artifactImagesListService,
    artifactImageUploadService,
    artifactImageDeleteService,
    artifactImageSetCoverService
} from '@/api/Artifacts.js'

const props = defineProps({
    modelValue: { type: Boolean, default: false },
    artifact: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
    get: () => props.modelValue,
    set: value => emit('update:modelValue', value)
})
const detailData = ref({})
const detailBackup = ref({})
const detailEditMode = ref(false)
const galleryVisible = ref(false)
const imageLoading = ref(false)
const imageUploading = ref(false)
const artifactImages = ref([])

const getTestingStatusText = row => row?.testingStatusDisplay || '无'
const getArtifactImage = row => {
    const value = row?.images
    if (!value) return ''
    try {
        const parsed = JSON.parse(value)
        if (Array.isArray(parsed)) {
            const first = parsed.find(Boolean)
            return typeof first === 'string' ? first : first?.url || ''
        }
    } catch (e) {}
    return String(value).split(',').map(item => item.trim()).filter(Boolean)[0] || ''
}
const isCoverImage = image => image?.isCover === true || Number(image?.isCover) === 1 || String(image?.isCover) === 'true'
const imagePreviewList = () => artifactImages.value.map(item => item.imageUrl).filter(Boolean)
const getCoverImage = () => {
    const cover = artifactImages.value.find(isCoverImage)
    return cover?.imageUrl || artifactImages.value[0]?.imageUrl || getArtifactImage(detailData.value)
}
const loadArtifactImages = async () => {
    if (!detailData.value.id) { artifactImages.value = []; return }
    imageLoading.value = true
    try {
        const result = await artifactImagesListService(detailData.value.id)
        artifactImages.value = result.data || []
    } catch (error) {
        artifactImages.value = []
        ElMessage.error('图片图库加载失败')
    } finally {
        imageLoading.value = false
    }
}
const initializeDetail = async () => {
    detailData.value = { ...(props.artifact || {}) }
    detailEditMode.value = false
    await loadArtifactImages()
}
watch(() => props.modelValue, value => { if (value) initializeDetail() })
watch(() => props.artifact, () => { if (props.modelValue) initializeDetail() }, { deep: true })

const openGallery = () => { galleryVisible.value = true; loadArtifactImages() }
const notifyChanged = () => emit('saved', { ...detailData.value })
const uploadArtifactGalleryImage = async ({ file }) => {
    imageUploading.value = true
    try {
        const formData = new FormData()
        formData.append('file', file)
        await artifactImageUploadService(detailData.value.id, formData)
        await loadArtifactImages()
        detailData.value.images = getCoverImage()
        notifyChanged()
        ElMessage.success('图片上传成功')
    } finally {
        imageUploading.value = false
    }
}
const setArtifactCover = async image => {
    await artifactImageSetCoverService(image.id)
    await loadArtifactImages()
    detailData.value.images = image.imageUrl
    notifyChanged()
    ElMessage.success('封面设置成功')
}
const deleteArtifactImage = image => {
    ElMessageBox.confirm('确定删除这张图片吗？删除后不会在图库中显示。', '删除图片', { type: 'warning' })
        .then(async () => {
            await artifactImageDeleteService(image.id)
            await loadArtifactImages()
            detailData.value.images = getCoverImage()
            notifyChanged()
            ElMessage.success('图片已删除')
        })
        .catch(() => {})
}
const enterDetailEditMode = () => { detailBackup.value = { ...detailData.value }; detailEditMode.value = true }
const cancelDetailEdit = () => { detailData.value = { ...detailBackup.value }; detailEditMode.value = false }
const saveDetailEdit = async () => {
    await artifactsUpdateService(detailData.value)
    detailEditMode.value = false
    notifyChanged()
    ElMessage.success('保存成功')
}
</script>

<template>
    <el-drawer v-model="visible" :title="detailEditMode ? '编辑文物' : '文物详情'" direction="rtl" size="55%" destroy-on-close>
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
                <el-descriptions-item label="拍照人">{{ detailData.photographer || '—' }}</el-descriptions-item>
                <el-descriptions-item label="绘图人">{{ detailData.draftsperson || '—' }}</el-descriptions-item>
                <el-descriptions-item label="文字描述人">{{ detailData.textDescriber || '—' }}</el-descriptions-item>
                <el-descriptions-item label="定级情况">{{ detailData.gradingStatus || '—' }}</el-descriptions-item>
                <el-descriptions-item label="已做检测方法">{{ getTestingStatusText(detailData) }}</el-descriptions-item>
                <el-descriptions-item label="文物流转过程" :span="2">{{ detailData.transferProcess || '—' }}</el-descriptions-item>
                <el-descriptions-item label="修复复原状况" :span="2">{{ detailData.restorationStatus || '—' }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '—' }}</el-descriptions-item>
                <el-descriptions-item label="视觉特征" :span="2"><div v-html="detailData.artifactDescription || '—'" /></el-descriptions-item>
                <el-descriptions-item label="文物图片" :span="2">
                    <div class="artifact-image-summary">
                        <el-image v-if="getCoverImage()" :src="getCoverImage()" :preview-src-list="imagePreviewList().length ? imagePreviewList() : [getCoverImage()]" fit="cover" class="detail-artifact-image" />
                        <div class="artifact-image-info">
                            <div class="artifact-image-title">{{ artifactImages.length ? `共 ${artifactImages.length} 张图片` : '暂无图库图片' }}</div>
                            <div class="artifact-image-tip">可在详情中集中上传多张图片、设置封面或删除图片。</div>
                            <el-button type="primary" size="small" @click="openGallery">管理图库</el-button>
                        </div>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ detailData.createTime || '—' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ detailData.updateTime || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div class="drawer-actions"><el-button type="primary" @click="enterDetailEditMode">编辑</el-button></div>
        </template>
        <template v-else>
            <el-form :model="detailData" label-width="110px"><el-row :gutter="20">
                <el-col :span="12">
                    <el-form-item label="文物新编号"><el-input v-model="detailData.newArtifactCode" /></el-form-item>
                    <el-form-item label="文物新名称"><el-input v-model="detailData.newArtifactName" /></el-form-item>
                    <el-form-item label="原始编号"><el-input v-model="detailData.originalArtifactCode" /></el-form-item>
                    <el-form-item label="原名称"><el-input v-model="detailData.originalArtifactName" /></el-form-item>
                    <el-form-item label="材质"><el-input v-model="detailData.material1" /></el-form-item>
                    <el-form-item label="材质2"><el-input v-model="detailData.material2" /></el-form-item>
                    <el-form-item label="完整度"><el-input v-model="detailData.completeness" /></el-form-item>
                    <el-form-item label="视觉特征"><div class="editor"><QuillEditor :key="'detail-' + detailData.id" theme="snow" v-model:content="detailData.artifactDescription" content-type="html" /></div></el-form-item>
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
                    <el-form-item label="文物流转过程"><el-input v-model="detailData.transferProcess" type="textarea" :rows="2" /></el-form-item>
                    <el-form-item label="修复复原状况"><el-input v-model="detailData.restorationStatus" /></el-form-item>
                    <el-form-item label="拍照人"><el-input v-model="detailData.photographer" /></el-form-item>
                    <el-form-item label="绘图人"><el-input v-model="detailData.draftsperson" /></el-form-item>
                    <el-form-item label="文字描述人"><el-input v-model="detailData.textDescriber" /></el-form-item>
                    <el-form-item label="定级情况"><el-input v-model="detailData.gradingStatus" /></el-form-item>
                    <el-form-item label="已做检测方法"><el-input :model-value="getTestingStatusText(detailData)" disabled /></el-form-item>
                    <el-form-item label="备注"><el-input v-model="detailData.notes" type="textarea" :rows="2" /></el-form-item>
                    <el-form-item label="图片图库"><div class="artifact-image-editor-entry"><el-image v-if="getCoverImage()" :src="getCoverImage()" :preview-src-list="imagePreviewList().length ? imagePreviewList() : [getCoverImage()]" fit="cover" class="detail-artifact-image" /><el-empty v-else description="暂无图片" :image-size="70" /><el-button type="primary" plain @click="openGallery">管理图库</el-button></div></el-form-item>
                </el-col>
            </el-row></el-form>
            <div class="drawer-actions"><el-button type="primary" @click="saveDetailEdit">保存</el-button><el-button @click="cancelDetailEdit">取消</el-button></div>
        </template>
    </el-drawer>
    <el-dialog v-model="galleryVisible" title="文物详情图片图库" width="760px" append-to-body>
        <div class="gallery-toolbar"><div><div class="gallery-title">{{ detailData.newArtifactName || detailData.originalArtifactName || '当前文物' }}</div><div class="gallery-subtitle">{{ detailData.newArtifactCode || detailData.originalArtifactCode || '未填写编号' }}</div></div><el-upload action="#" multiple :show-file-list="false" :http-request="uploadArtifactGalleryImage" accept="image/*"><el-button type="primary" :loading="imageUploading"><el-icon><Plus /></el-icon>上传图片</el-button></el-upload></div>
        <el-empty v-if="!imageLoading && artifactImages.length === 0" description="暂无图片，点击右上角上传" />
        <div v-else v-loading="imageLoading" class="gallery-grid"><div v-for="image in artifactImages" :key="image.id" class="gallery-card"><el-image :src="image.imageUrl" :preview-src-list="imagePreviewList()" fit="cover" class="gallery-image" /><div class="gallery-card-body"><div class="gallery-name" :title="image.imageName">{{ image.imageName || '未命名图片' }}</div><el-tag v-if="isCoverImage(image)" type="success" size="small">封面</el-tag><span v-else class="gallery-time">{{ image.uploadTime || '' }}</span></div><div class="gallery-card-actions"><el-button v-if="!isCoverImage(image)" type="primary" link @click="setArtifactCover(image)">设为封面</el-button><span v-else class="cover-placeholder">当前封面</span><el-button type="danger" link @click="deleteArtifactImage(image)">删除</el-button></div></div></div>
    </el-dialog>
</template>

<style scoped>
.drawer-actions { margin-top: 20px; text-align: right; }
.artifact-image-summary { display: flex; align-items: center; gap: 16px; }.artifact-image-info { display: flex; flex-direction: column; gap: 8px; color: #606266; }.artifact-image-title { font-size: 15px; font-weight: 600; color: #303133; }.artifact-image-tip { font-size: 13px; color: #909399; }.artifact-image-editor-entry { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }.detail-artifact-image { width: 220px; height: 160px; border-radius: 8px; border: 1px solid #ebeef5; background: #f5f7fa; }
.gallery-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }.gallery-title { font-size: 16px; font-weight: 600; color: #303133; }.gallery-subtitle { margin-top: 4px; font-size: 13px; color: #909399; }.gallery-grid { min-height: 180px; display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; }.gallery-card { border: 1px solid #ebeef5; border-radius: 10px; overflow: hidden; background: #fff; }.gallery-image { width: 100%; height: 140px; display: block; background: #f5f7fa; }.gallery-card-body { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 10px 10px 4px; }.gallery-name { min-width: 0; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #303133; font-size: 13px; }.gallery-time { color: #909399; font-size: 12px; }.gallery-card-actions { display: flex; align-items: center; justify-content: space-between; padding: 4px 10px 10px; }.cover-placeholder { color: #67c23a; font-size: 13px; }.editor { width: 100%; }.editor :deep(.ql-editor) { min-height: 150px; }
</style>
