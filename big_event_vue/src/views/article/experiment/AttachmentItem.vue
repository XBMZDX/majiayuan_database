<!-- 单个附件项 -->
<script setup>
import { computed } from 'vue'
import { Download, Delete } from '@element-plus/icons-vue'

const props = defineProps({ attachment: Object })
const emit = defineEmits(['preview', 'download', 'delete'])

const fileIcons = { PDF: '📄', Word: '📝', Excel: '📊', ZIP: '📦', 图片: '🖼', 其他: '📎' }
const getIcon = (type) => fileIcons[type] || '📎'
const getTypeColor = (type) => {
    const map = { PDF: '#E74C3C', Word: '#2980B9', Excel: '#27AE60', ZIP: '#7F8C8D', 图片: '#E67E22', 其他: '#95A5A6' }
    return map[type] || '#95A5A6'
}

const handleClick = () => {
    const types = ['PDF', '图片']
    if (types.includes(props.attachment.type) && props.attachment.url) {
        window.open(props.attachment.url, '_blank')
    } else {
        emit('download', props.attachment)
    }
}
</script>

<template>
    <div class="att-item">
        <div class="att-icon" :style="{color: getTypeColor(attachment.type)}">{{ getIcon(attachment.type) }}</div>
        <div class="att-body" @click="handleClick">
            <div class="att-name">
                <el-link type="primary" :underline="false">{{ attachment.name || '未命名文件' }}</el-link>
            </div>
            <div class="att-meta">
                <el-tag size="small" :color="getTypeColor(attachment.type)" effect="light" style="border:none">{{ attachment.type }}</el-tag>
                <span class="att-size">{{ attachment.size || '-' }}</span>
                <span class="att-time">{{ (attachment.uploadTime || '').substring(0,10) }}</span>
                <span class="att-desc" v-if="attachment.description">{{ attachment.description }}</span>
            </div>
        </div>
        <div class="att-actions">
            <el-button size="small" :icon="Download" circle @click="emit('download', attachment)"> </el-button>
            <el-button size="small" :icon="Delete" circle type="danger" @click="emit('delete', attachment)"> </el-button>
        </div>
    </div>
</template>

<style scoped>
.att-item { display: flex; align-items: center; gap: 14px; padding: 14px 16px; background: #F7F8FA; border-radius: 10px; margin-bottom: 8px; transition: background .15s; }
.att-item:hover { background: #ECF5FF; }
.att-icon { font-size: 32px; flex-shrink: 0; }
.att-body { flex: 1; cursor: pointer; min-width: 0; }
.att-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.att-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.att-size { font-size: 12px; color: #999; }
.att-time { font-size: 12px; color: #C0C4CC; }
.att-desc { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
.att-actions { display: flex; gap: 4px; flex-shrink: 0; opacity: 0.5; transition: opacity .15s; }
.att-item:hover .att-actions { opacity: 1; }
</style>
