<script setup>
import { computed } from 'vue'
import { ArrowLeft, CircleCheck, FolderOpened, Menu, MoreFilled } from '@element-plus/icons-vue'

const props = defineProps({
    project: { type: Object, required: true },
    archive: { type: Object, required: true },
    completeness: { type: Number, default: 0 },
    saving: Boolean,
    dirty: Boolean
})

defineEmits(['save', 'next', 'finalize', 'revision', 'export', 'back', 'open-nav'])

const statusInfo = computed(() => {
    const map = {
        draft: ['草稿', 'info'],
        compiling: ['编制中', 'primary'],
        completed: ['已定稿', 'success'],
        archived: ['已归档', 'info']
    }
    const value = map[props.archive.archiveStatus] || [props.archive.archiveStatus || '未知', 'info']
    return { text: value[0], type: value[1] }
})

const stageText = computed(() => {
    const map = {
        pendingSurvey: '待调查', disease: '病害调查', planning: '方案制定',
        protection: '保护处理中', repair: '修复处理中', restoration: '复原处理中',
        evaluation: '效果评估', completed: '已完成', monitoring: '后续监测'
    }
    return map[props.project.currentStage] || props.project.currentStage || '-'
})
</script>

<template>
    <header class="summary-bar">
        <div class="summary-main">
            <div class="title-row">
                <el-button class="mobile-nav" :icon="Menu" circle @click="$emit('open-nav')" />
                <div>
                    <div class="eyebrow">保护修复档案编制工作台 <span v-if="dirty" class="dirty-dot">未保存</span></div>
                    <h2>{{ archive.archiveTitle }}</h2>
                </div>
            </div>
            <div class="meta-row">
                <span>档案编号 <b>{{ archive.archiveCode }}</b></span>
                <span>文物 <b>{{ project.artifactCode }} · {{ project.artifactName }}</b></span>
                <span>项目 <b>{{ project.projectCode }}</b></span>
                <span>阶段 <b>{{ stageText }}</b></span>
                <el-tag :type="statusInfo.type" size="small">{{ statusInfo.text }}</el-tag>
                <el-tag type="info" effect="plain" size="small">{{ archive.currentVersion }}</el-tag>
                <span class="complete-chip">完整度 {{ completeness }}%</span>
            </div>
            <div class="sub-row">
                <span>编制人：{{ archive.compiler || '待填写' }}</span>
                <span>最近更新：{{ archive.updateTime || '-' }}</span>
            </div>
        </div>

        <div class="summary-actions">
            <el-button :icon="FolderOpened" :loading="saving" @click="$emit('save')">保存草稿</el-button>
            <el-button type="primary" plain :loading="saving" @click="$emit('next')">保存草稿并进入修复过程</el-button>
            <el-button type="primary" :icon="CircleCheck" @click="$emit('finalize')">保存定稿</el-button>
            <el-dropdown @command="command => $emit(command)">
                <el-button :icon="MoreFilled">更多</el-button>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="revision">生成新版本</el-dropdown-item>
                        <el-dropdown-item command="export">导出 Word / PDF</el-dropdown-item>
                        <el-dropdown-item command="back" divided :icon="ArrowLeft">返回总览</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </header>
</template>

<style scoped>
.summary-bar { flex: none; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 15px 18px; background: #fff; border: 1px solid #e5e6eb; border-radius: 10px; box-shadow: 0 2px 10px rgba(29, 33, 41, .04); }
.summary-main { min-width: 0; }
.title-row { display: flex; align-items: center; gap: 10px; }
.eyebrow { margin-bottom: 4px; color: #86909c; font-size: 12px; }
h2 { margin: 0; color: #1d2129; font-size: 18px; line-height: 1.35; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta-row, .sub-row { display: flex; align-items: center; flex-wrap: wrap; gap: 6px 16px; margin-top: 8px; color: #606266; font-size: 12px; }
.meta-row b { color: #303133; font-weight: 600; }
.sub-row { margin-top: 5px; color: #a0a5ad; }
.complete-chip { padding: 3px 8px; color: #1668c4; background: #ecf5ff; border-radius: 10px; font-weight: 600; }
.dirty-dot { margin-left: 7px; color: #e6a23c; font-weight: 600; }
.summary-actions { display: flex; flex: none; align-items: center; gap: 8px; }
.mobile-nav { display: none; }
@media (max-width: 1180px) {
    .summary-bar { align-items: flex-start; }
    .summary-actions { flex-wrap: wrap; justify-content: flex-end; max-width: 260px; }
}
@media (max-width: 900px) {
    .summary-bar { flex-direction: column; }
    .summary-actions { max-width: none; width: 100%; justify-content: flex-start; }
    .mobile-nav { display: inline-flex; }
}
</style>
