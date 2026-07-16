<!-- AI 实验总结（预留模块） -->
<script setup>
import { MagicStick } from '@element-plus/icons-vue'
const emit = defineEmits(['generate'])
const generateAISummary = () => { /* 预留 AI 接口 */ }
</script>
<template>
    <div class="ai-card">
        <div class="ai-header">
            <span class="ai-title">🤖 AI 实验总结</span>
            <el-tag size="small" type="warning">开发中</el-tag>
        </div>
        <div class="ai-body">
            <p class="ai-desc">AI 实验总结功能开发中……</p>
            <p class="ai-detail">未来将根据：<br>
                • 实验数据<br>
                • 实验图像<br>
                • 实验结论<br>
                自动生成实验总结。</p>
            <el-button type="primary" :icon="MagicStick" disabled @click="generateAISummary">
                AI 生成实验总结
            </el-button>
        </div>
    </div>
</template>
<style scoped>
.ai-card { background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%); border: 1px dashed #C0C4CC; border-radius: 10px; padding: 20px; margin-top: 16px; }
.ai-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.ai-title { font-size: 15px; font-weight: 600; color: #1D2129; }
.ai-body { text-align: center; padding: 10px 0; }
.ai-desc { font-size: 15px; color: #909399; margin-bottom: 8px; }
.ai-detail { font-size: 13px; color: #C0C4CC; line-height: 1.8; margin-bottom: 16px; }
</style>
