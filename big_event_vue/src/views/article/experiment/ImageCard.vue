<!-- 单张图片卡片 -->
<script setup>
import { Download, Delete } from '@element-plus/icons-vue'

const props = defineProps({ image: Object, index: Number, allImages: Array })
const emit = defineEmits(['preview', 'download', 'delete'])

const cats = { '光谱图': '🔬', 'CT图像': '🩻', '显微照片': '🔍', '检测照片': '📷', '三维模型截图': '🧊', '其他': '🖼' }
</script>

<template>
    <div class="img-card">
        <div class="img-thumb" @click="emit('preview', image, index)">
            <el-image :src="image.url || ''" fit="cover" style="width:100%;height:160px;border-radius:6px">
                <template #error><div class="img-placeholder">{{ cats[image.category] || '🖼' }}</div></template>
            </el-image>
            <div class="img-overlay">
                <el-button size="small" circle type="primary" @click.stop="emit('preview', image, index)">🔍</el-button>
                <el-button size="small" circle @click.stop="emit('download', image)"><el-icon><Download /></el-icon></el-button>
                <el-button size="small" circle type="danger" @click.stop="emit('delete', image)"><el-icon><Delete /></el-icon></el-button>
            </div>
        </div>
        <div class="img-info">
            <div class="img-title">{{ image.title || '未命名' }}</div>
            <div class="img-meta">
                <el-tag size="small" type="info">{{ image.category || '其他' }}</el-tag>
                <span class="img-time">{{ (image.uploadTime || '').substring(0,10) }}</span>
            </div>
        </div>
    </div>
</template>

<style scoped>
.img-card { background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; transition: box-shadow .15s; }
.img-card:hover { box-shadow: 0 2px 10px rgba(0,0,0,0.08); }
.img-thumb { position: relative; cursor: pointer; }
.img-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; gap: 8px; background: rgba(0,0,0,0.35); opacity: 0; transition: opacity .2s; }
.img-thumb:hover .img-overlay { opacity: 1; }
.img-placeholder { height: 160px; display: flex; align-items: center; justify-content: center; font-size: 40px; background: #F5F7FA; }
.img-info { padding: 10px 12px; }
.img-title { font-size: 13px; font-weight: 600; color: #1D2129; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.img-meta { display: flex; justify-content: space-between; align-items: center; }
.img-time { font-size: 11px; color: #C0C4CC; }
</style>
