<!-- 编辑区域 -->
<script setup>
const model = defineModel(Object)
</script>
<template>
    <div class="editor-sections">
        <div class="ed-section">
            <h4>一、实验结论</h4>
            <el-input v-model="model.conclusion" type="textarea" :rows="5"
                placeholder="请输入实验结论，支持多段文本。后续可替换为富文本编辑器（如 Quill、Tiptap）..." />
        </div>
        <div class="ed-section">
            <h4>二、结果分析</h4>
            <el-input v-model="model.analysis" type="textarea" :rows="5"
                placeholder="请详细分析实验数据。支持长文本、多段落。预留图片/表格插入位置..." />
        </div>
        <div class="ed-section">
            <h4>三、备注说明</h4>
            <el-input v-model="model.remark" type="textarea" :rows="4"
                placeholder="实验注意事项、后续建议、数据说明、特殊情况等..." />
        </div>
    </div>
</template>
<style scoped>
.editor-sections { display: flex; flex-direction: column; gap: 20px; }
.ed-section h4 { margin: 0 0 8px; font-size: 14px; font-weight: 600; color: #1D2129; }
</style>
