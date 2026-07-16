<!-- 实验图像模块（主组件） -->
<script setup>
import { ref, computed, watch } from 'vue'
import { Upload, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import ImageCard from './ImageCard.vue'
import ImageUploadDialog from './ImageUploadDialog.vue'

const props = defineProps({ experiment: Object })
const emit = defineEmits(['update'])

const allCategories = ['全部', '光谱图', 'CT图像', '显微照片', '检测照片', '三维模型截图', '其他']
const activeCategory = ref('全部')

// 解析图片数据
const images = ref([])
watch(() => props.experiment, () => {
    try { images.value = JSON.parse(props.experiment?.images || '[]') } catch { images.value = [] }
}, { immediate: true })

const filteredImages = computed(() =>
    activeCategory.value === '全部' ? images.value : images.value.filter(i => i.category === activeCategory.value)
)

// 持久化
const persist = () => {
    emit('update', JSON.stringify(images.value))
}

// 上传弹窗
const uploadVisible = ref(false)
const onUploadSubmit = (img) => {
    images.value.unshift(img)
    persist()
}

// 预览
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewList = computed(() => filteredImages.value.map(i => i.url))
const previewImage = computed(() => filteredImages.value[previewIndex.value] || {})
const openPreview = (img, idx) => {
    previewIndex.value = idx
    // 重建 filteredImages 的 url 列表
    previewVisible.value = true
}

// 下载
const downloadImage = (img) => {
    if (!img.url) return
    const a = document.createElement('a')
    a.href = img.url; a.download = img.title || 'image'
    document.body.appendChild(a); a.click(); document.body.removeChild(a)
}

// 删除
const deleteImage = (img) => {
    ElMessageBox.confirm('确定删除该图片？', '提示', { type: 'warning' }).then(() => {
        const idx = images.value.findIndex(i => i.id === img.id)
        if (idx >= 0) { images.value.splice(idx, 1); persist() }
    }).catch(() => {})
}

const refresh = () => {
    try { images.value = JSON.parse(props.experiment?.images || '[]') } catch { images.value = [] }
}
</script>

<template>
    <el-card shadow="never" class="mod-card">
        <template #header>
            <div class="card-header">
                <span class="mod-title">🖼 实验图像</span>
                <div class="header-actions">
                    <el-button size="small" :icon="Upload" type="primary" @click="uploadVisible = true">上传图片</el-button>
                    <el-button size="small" :icon="Refresh" @click="refresh">刷新</el-button>
                </div>
            </div>
        </template>

        <!-- 分类筛选 -->
        <div class="cat-bar" v-if="images.length">
            <el-radio-group v-model="activeCategory" size="small">
                <el-radio-button v-for="c in allCategories" :key="c" :value="c">{{ c }}</el-radio-button>
            </el-radio-group>
        </div>

        <!-- 图片墙 -->
        <div class="gallery" v-if="filteredImages.length">
            <ImageCard v-for="(img, i) in filteredImages" :key="img.id" :image="img" :index="i"
                       :allImages="filteredImages"
                       @preview="openPreview" @download="downloadImage" @delete="deleteImage" />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
            <el-empty description="暂无实验图像" :image-size="60">
                <template #default>
                    <span style="color:#999;font-size:13px">点击右上角 <b>上传图片</b> 添加实验图片</span>
                </template>
            </el-empty>
        </div>
    </el-card>

    <!-- 全屏预览 -->
    <el-dialog v-model="previewVisible" :title="previewImage.title || '图片预览'" width="80%" top="5vh" destroy-on-close>
        <div style="text-align:center" v-if="previewImage.url">
            <el-image :src="previewImage.url" fit="contain" style="max-height:65vh" :preview-src-list="previewList" :initial-index="previewIndex" preview-teleported />
        </div>
        <div class="preview-meta" v-if="previewImage.url">
            <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="图片名称">{{ previewImage.title || '-' }}</el-descriptions-item>
                <el-descriptions-item label="所属分类">{{ previewImage.category || '-' }}</el-descriptions-item>
                <el-descriptions-item label="上传时间">{{ (previewImage.uploadTime || '').substring(0, 10) || '-' }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ previewImage.description || '-' }}</el-descriptions-item>
            </el-descriptions>
        </div>
        <template #footer>
            <el-button @click="previewVisible = false">关闭</el-button>
            <el-button type="primary" @click="downloadImage(previewImage)" :icon="Download">下载</el-button>
        </template>
    </el-dialog>

    <ImageUploadDialog v-model="uploadVisible" @submit="onUploadSubmit" />
</template>

<style scoped>
.mod-card { margin-bottom: 20px; border: 1px solid #E5E6EB; border-radius: 10px; }
.mod-title { font-size: 15px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.cat-bar { margin-bottom: 14px; }
.gallery { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px; }
.empty-state { text-align: center; padding: 30px 0; }
.preview-meta { margin-top: 16px; }
@media (max-width: 900px) { .gallery { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .gallery { grid-template-columns: 1fr; } }
</style>
