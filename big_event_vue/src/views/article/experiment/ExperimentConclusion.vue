<!-- 实验结论模块（主组件） -->
<script setup>
import { ref, watch } from 'vue'
import { Edit, Check, Close } from '@element-plus/icons-vue'
import ConclusionEditor from './ConclusionEditor.vue'
import AISummaryCard from './AISummaryCard.vue'

const props = defineProps({ experiment: Object })
const emit = defineEmits(['update'])

const editMode = ref(false)

// 解析数据
const parseData = () => {
    try { return JSON.parse(props.experiment?.notes || '{}') } catch { return {} }
}

const local = ref({ conclusion: '', analysis: '', remark: '' })
const backup = ref({})

watch(() => props.experiment, () => { local.value = parseData() }, { immediate: true })

const enterEdit = () => { backup.value = { ...local.value }; editMode.value = true }
const save = () => {
    emit('update', JSON.stringify({ ...local.value }))
    editMode.value = false
}
const cancel = () => { local.value = { ...backup.value }; editMode.value = false }

const hasContent = () => local.value.conclusion || local.value.analysis || local.value.remark
</script>

<template>
    <el-card shadow="never" class="mod-card">
        <template #header>
            <div class="card-header">
                <span class="mod-title">📝 实验结论</span>
                <div class="header-actions">
                    <template v-if="!editMode">
                        <el-button size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
                    </template>
                    <template v-else>
                        <el-button size="small" :icon="Check" type="primary" @click="save">保存</el-button>
                        <el-button size="small" :icon="Close" @click="cancel">取消</el-button>
                    </template>
                </div>
            </div>
        </template>

        <!-- 编辑模式 -->
        <ConclusionEditor v-if="editMode" v-model="local" />

        <!-- 阅读模式 -->
        <div v-else>
            <template v-if="hasContent()">
                <div class="read-section" v-if="local.conclusion">
                    <h4>一、实验结论</h4>
                    <div class="read-text">{{ local.conclusion }}</div>
                </div>
                <div class="read-section" v-if="local.analysis">
                    <h4>二、结果分析</h4>
                    <div class="read-text">{{ local.analysis }}</div>
                </div>
                <div class="read-section" v-if="local.remark">
                    <h4>三、备注说明</h4>
                    <div class="read-text">{{ local.remark }}</div>
                </div>
            </template>
            <el-empty v-else description="暂无实验结论，请点击编辑进行填写。" :image-size="60" />
        </div>

        <!-- AI 预留 -->
        <AISummaryCard />
    </el-card>
</template>

<style scoped>
.mod-card { margin-bottom: 20px; border: 1px solid #E5E6EB; border-radius: 10px; }
.mod-title { font-size: 15px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }

.read-section { margin-bottom: 20px; }
.read-section h4 { margin: 0 0 8px; font-size: 14px; font-weight: 600; color: #1D2129; }
.read-text { font-size: 14px; line-height: 1.8; color: #4E5969; white-space: pre-wrap; background: #F7F8FA; border-radius: 8px; padding: 14px 18px; }
</style>
