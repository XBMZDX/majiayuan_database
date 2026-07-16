<!-- 实验图像 -->
<script setup>
import { computed } from 'vue'
const props = defineProps({ experiment: Object })
const images = computed(() => {
    try { return JSON.parse(props.experiment?.images || '[]') } catch { return [] }
})
</script>
<template>
    <el-card shadow="never" class="mod-card">
        <template #header><span class="mod-title">🖼 实验图像</span></template>
        <div v-if="images.length" class="gallery-grid">
            <el-image v-for="(url, i) in images" :key="i" :src="url" fit="cover"
                style="height:180px;border-radius:8px;cursor:pointer" :preview-src-list="images" :initial-index="i" />
        </div>
        <el-empty v-else description="暂无实验图像" :image-size="60" />
    </el-card>
</template>
<style scoped>
.mod-card { margin-bottom: 20px; border: 1px solid #E5E6EB; border-radius: 10px; }
.mod-title { font-size: 15px; font-weight: 600; }
.gallery-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
</style>
