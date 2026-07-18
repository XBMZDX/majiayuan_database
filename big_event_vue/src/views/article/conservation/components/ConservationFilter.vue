<!-- 搜索与筛选工具栏 -->
<script setup>
import { ref } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'

const emit = defineEmits(['search', 'reset', 'create'])
const form = ref({ keyword: '', status: '', currentStage: '', riskLevel: '', principal: '', dateRange: [] })
const showMore = ref(false)

const statusOptions = [
    { label: '全部状态', value: '' }, { label: '草稿', value: 'draft' },
    { label: '进行中', value: 'active' }, { label: '已完成', value: 'completed' },
    { label: '暂停', value: 'suspended' }, { label: '已归档', value: 'archived' }
]
const stageOptions = [
    { label: '全部阶段', value: '' }, { label: '待调查', value: 'pendingSurvey' },
    { label: '病害调查', value: 'disease' }, { label: '方案制定', value: 'planning' },
    { label: '保护处理', value: 'protection' }, { label: '修复处理', value: 'repair' },
    { label: '复原处理', value: 'restoration' }, { label: '效果评估', value: 'evaluation' },
    { label: '后续监测', value: 'monitoring' }
]
const riskOptions = [
    { label: '全部风险', value: '' }, { label: '高风险', value: 'high' },
    { label: '中风险', value: 'medium' }, { label: '低风险', value: 'low' }
]

const handleSearch = () => emit('search', { ...form.value })
const handleReset = () => { form.value = { keyword: '', status: '', currentStage: '', riskLevel: '', principal: '', dateRange: [] }; emit('reset') }
</script>

<template>
    <div class="filter-bar">
        <div class="filter-row">
            <el-input v-model="form.keyword" placeholder="项目编号/名称/文物编号" clearable style="width:220px" size="default" @keyup.enter="handleSearch" />
            <el-select v-model="form.status" placeholder="状态" clearable size="default" style="width:120px">
                <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="form.currentStage" placeholder="当前阶段" clearable size="default" style="width:130px">
                <el-option v-for="o in stageOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="form.riskLevel" placeholder="风险等级" clearable size="default" style="width:120px">
                <el-option v-for="o in riskOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <template v-if="showMore">
                <el-input v-model="form.principal" placeholder="负责人" clearable size="default" style="width:120px" />
                <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" size="default" style="width:240px" value-format="YYYY-MM-DD" />
            </template>
            <el-button :icon="Search" type="primary" size="default" @click="handleSearch">查询</el-button>
            <el-button size="default" @click="handleReset">重置</el-button>
            <el-button type="primary" size="default" :icon="Plus" @click="emit('create')">新建项目</el-button>
            <el-button text size="small" @click="showMore = !showMore">{{ showMore ? '收起' : '更多筛选' }}</el-button>
        </div>
    </div>
</template>

<style scoped>
.filter-bar { margin-bottom: 16px; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
</style>
