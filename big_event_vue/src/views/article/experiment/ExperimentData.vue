<!-- 实验数据（动态表格） -->
<script setup>
import { ref, watch, computed } from 'vue'
import { Edit, Delete, Setting } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import FieldConfigDialog from './FieldConfigDialog.vue'
import DataEditDialog from './DataEditDialog.vue'

const props = defineProps({ experiment: Object })
const emit = defineEmits(['update'])

// 从 resultData JSON 解析字段和数据
const parseData = () => {
    try {
        const d = JSON.parse(props.experiment?.resultData || '{}')
        return { fields: d.fields || [], rows: d.rows || [] }
    } catch { return { fields: [], rows: [] } }
}

const fields = ref([])
const rows = ref([])

watch(() => props.experiment, () => {
    const d = parseData(); fields.value = d.fields; rows.value = d.rows
}, { immediate: true })

// 持久化
const persist = () => {
    emit('update', JSON.stringify({ fields: fields.value, rows: rows.value }))
}

// 配置弹窗
const configVisible = ref(false)
const onConfigSave = (f) => { fields.value = f; persist() }

// 数据弹窗
const dataVisible = ref(false)
const editRow = ref(null)
const onAdd = () => { editRow.value = null; dataVisible.value = true }
const onEdit = (row) => { editRow.value = { ...row }; dataVisible.value = true }
const onDataSave = (row) => {
    if (editRow.value) {
        const idx = rows.value.indexOf(editRow.value._origin || editRow.value)
        if (idx >= 0) rows.value[idx] = row
    } else {
        rows.value.push(row)
    }
    persist()
}
const onDelete = (row) => {
    ElMessageBox.confirm('确定删除该行数据？', '提示', { type: 'warning' }).then(() => {
        const idx = rows.value.indexOf(row)
        if (idx >= 0) rows.value.splice(idx, 1)
        persist()
    }).catch(() => {})
}
</script>

<template>
    <el-card shadow="never" class="mod-card">
        <template #header>
            <div class="card-header">
                <span class="mod-title">📊 实验数据</span>
                <div class="header-actions">
                    <el-button size="small" :icon="Setting" @click="configVisible = true">配置表格</el-button>
                    <el-button size="small" type="primary" @click="onAdd">新增数据</el-button>
                </div>
            </div>
        </template>

        <!-- 空状态 -->
        <div v-if="!fields.length && !rows.length" class="empty-state">
            <el-empty description="暂无实验数据" :image-size="60">
                <template #default>
                    <span style="color:#999;font-size:13px">点击 <b>配置表格</b> 创建数据模板，或点击 <b>新增数据</b> 录入实验数据</span>
                </template>
            </el-empty>
        </div>

        <!-- 动态表格 -->
        <div class="table-wrap" v-else>
            <el-table :data="rows" border stripe size="small" max-height="400" style="width:100%" highlight-current-row>
                <el-table-column v-for="f in fields" :key="f.field" :prop="f.field" :label="f.label" min-width="120" sortable />
                <el-table-column label="操作" width="100" fixed="right">
                    <template #default="{ row }">
                        <el-button size="small" :icon="Edit" circle @click="onEdit(row)" />
                        <el-button size="small" :icon="Delete" circle type="danger" @click="onDelete(row)" />
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </el-card>

    <FieldConfigDialog v-model="configVisible" :fields="fields" @save="onConfigSave" />
    <DataEditDialog v-model="dataVisible" :fields="fields" :editRow="editRow" @save="onDataSave" />
</template>

<style scoped>
.mod-card { margin-bottom: 20px; border: 1px solid #E5E6EB; border-radius: 10px; }
.mod-title { font-size: 15px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.table-wrap { overflow-x: auto; }
.empty-state { text-align: center; padding: 30px 0; }
</style>
