<script setup>
/**
 * 数据统计卡片组件 — 4 个横向并排指标看板
 * 数据通过 props 传入，便于后续对接接口
 */
import { Document, Flag, Monitor } from '@element-plus/icons-vue'

defineProps({
    artifactCount: { type: Number, default: 0 }, // 文物总数
    relicCount:    { type: Number, default: 0 }, // 遗迹总数
    detectionCount:{ type: Number, default: 0 }, // 检测项目总数
})

const cards = [
    { label: '文物总数', icon: Document, key: 'artifactCount' },
    { label: '遗迹总数', icon: Flag,     key: 'relicCount' },
    { label: '检测项目总数', icon: Monitor, key: 'detectionCount' },
]
</script>

<template>
    <div class="stats-row">
        <el-card v-for="card in cards" :key="card.key" class="stat-card" shadow="never">
            <el-row align="middle" justify="space-between" style="width:100%">
                <!-- 左侧：图标 + 标签（垂直居中） -->
                <el-col :span="16">
                    <div class="stat-left">
                        <el-icon :size="28" color="#8C8C8C"><component :is="card.icon" /></el-icon>
                        <span class="stat-label">{{ card.label }}</span>
                    </div>
                </el-col>
                <!-- 右侧：统计数字 -->
                <el-col :span="8" style="text-align:right">
                    <span class="stat-value">{{ $props[card.key]?.toLocaleString() || '0' }}</span>
                </el-col>
            </el-row>
        </el-card>
    </div>
</template>

<style scoped>
.stats-row {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
}
.stat-card {
    flex: 1;
    border-radius: 8px;
    border: 1px solid #E5E6EB;
    :deep(.el-card__body) { padding: 20px 24px; }
}
.stat-left {
    display: flex;
    align-items: center;
    gap: 10px;
}
.stat-label {
    font-size: 13px;
    color: #8C8C8C;
}
.stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #1D2129;
    line-height: 1.3;
}
</style>