<!-- 右侧病害调查档案 -->
<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
const token = computed(() => useTokenStore().token)

const props = defineProps({ survey: Object, activeDisease: Object, diseaseTypes: Array })
const emit = defineEmits(['update:survey', 'update:disease', 'upload-media'])

// Section 1: 整体保存状态
const surveyForm = ref({})
watch(() => props.survey, (v) => { surveyForm.value = { ...(v || {}) } }, { immediate: true })
const onSurveyChange = () => emit('update:survey', { ...surveyForm.value })

// Section 2: 病害基本信息
const diseaseForm = ref({})
watch(() => props.activeDisease, (v) => { diseaseForm.value = { ...(v || {}) } }, { immediate: true })
const onDiseaseChange = () => emit('update:disease', { ...diseaseForm.value })

const onTypeChange = (typeId) => {
    const t = props.diseaseTypes?.find(d => d.id === typeId)
    if (t) {
        diseaseForm.value.diseaseName = t.name || diseaseForm.value.diseaseName
        diseaseForm.value.diseaseCategory = t.category || diseaseForm.value.diseaseCategory
    }
}

// Section 3: 位置（简化版）
const locations = ref([])
watch(() => props.activeDisease, () => { locations.value = [] }, { immediate: true })
const addLocation = () => { locations.value.push({ partName: '', side: '整体', description: '' }) }
const removeLocation = (i) => { locations.value.splice(i, 1) }

// Section 5: 成因因素
const causeFactors = ['自然环境','埋藏环境','人为活动','实验室环境变化','材质自身因素','温湿度波动','水盐迁移','氧化腐蚀','微生物作用','机械外力','其他']
const selectedFactors = ref([])
watch(() => props.activeDisease, () => { selectedFactors.value = [] })
</script>

<template>
    <div class="paper" v-if="activeDisease">
        <!-- Section 1: 整体保存状态 -->
        <div class="section">
            <h3>整体保存状态</h3>
            <el-form :model="surveyForm" label-width="90px" size="small" @change="onSurveyChange">
                <el-row :gutter="16">
                    <el-col :span="8"><el-form-item label="调查编号"><el-input :model-value="surveyForm.surveyCode" disabled /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="调查日期"><el-date-picker v-model="surveyForm.surveyDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="调查人"><el-input v-model="surveyForm.surveyor" placeholder="调查人" /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="总体保存状态">
                        <el-select v-model="surveyForm.preservationStatus" style="width:100%">
                            <el-option label="良好" value="good" /><el-option label="一般" value="fair" /><el-option label="较差" value="poor" /><el-option label="危急" value="critical" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="结构稳定性">
                        <el-select v-model="surveyForm.structuralStability" style="width:100%">
                            <el-option label="稳定" value="stable" /><el-option label="局部不稳定" value="partially_unstable" /><el-option label="整体不稳定" value="unstable" /><el-option label="危险" value="dangerous" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="调查地点"><el-input v-model="surveyForm.surveyLocation" /></el-form-item></el-col>
                </el-row>
                <el-form-item label="环境摘要"><el-input v-model="surveyForm.environmentSummary" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="调查总结"><el-input v-model="surveyForm.summary" type="textarea" :rows="2" /></el-form-item>
            </el-form>
        </div>
        <el-divider />

        <!-- Section 2: 当前病害基本信息 -->
        <div class="section">
            <h3>病害基本信息</h3>
            <el-form :model="diseaseForm" label-width="90px" size="small" @change="onDiseaseChange">
                <el-row :gutter="16">
                    <el-col :span="8"><el-form-item label="病害类型">
                        <el-select v-model="diseaseForm.diseaseTypeId" style="width:100%" @change="onTypeChange">
                            <el-option v-for="t in diseaseTypes" :key="t.id" :label="t.name" :value="t.id" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="病害名称"><el-input v-model="diseaseForm.diseaseName" /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="病害类别"><el-input v-model="diseaseForm.diseaseCategory" /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="严重程度">
                        <el-select v-model="diseaseForm.severity" style="width:100%">
                            <el-option label="轻微" value="minor" /><el-option label="中度" value="moderate" /><el-option label="严重" value="severe" /><el-option label="危急" value="critical" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="发展状态">
                        <el-select v-model="diseaseForm.developmentStatus" style="width:100%">
                            <el-option label="稳定" value="stable" /><el-option label="缓慢发展" value="slowly_developing" /><el-option label="快速发展" value="rapidly_developing" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="结构影响">
                        <el-select v-model="diseaseForm.structuralImpact" style="width:100%">
                            <el-option label="无明显影响" value="none" /><el-option label="局部影响" value="local" /><el-option label="整体影响" value="overall" />
                        </el-select>
                    </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="病害范围"><el-input v-model="diseaseForm.extentValue" /> </el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="单位"><el-input v-model="diseaseForm.extentUnit" placeholder="cm/%/处" /></el-form-item></el-col>
                    <el-col :span="8"><el-form-item label="是否紧急">
                        <el-switch v-model="diseaseForm.emergency" />
                    </el-form-item></el-col>
                </el-row>
                <el-form-item label="表观形态"><el-input v-model="diseaseForm.morphology" type="textarea" :rows="2" /></el-form-item>
            </el-form>
        </div>
        <el-divider />

        <!-- Section 3: 病害位置 -->
        <div class="section">
            <h3>病害位置</h3>
            <div class="location-header">
                <el-form-item label="部位名称"><el-input v-model="diseaseForm.partName" size="small" style="width:200px" /></el-form-item>
                <el-form-item label="方位">
                    <el-select v-model="diseaseForm.side" size="small" style="width:120px">
                        <el-option label="正面" value="正面" /><el-option label="背面" value="背面" /><el-option label="左侧" value="左侧" /><el-option label="右侧" value="右侧" />
                        <el-option label="顶部" value="顶部" /><el-option label="底部" value="底部" /><el-option label="内部" value="内部" /><el-option label="整体" value="整体" />
                    </el-select>
                </el-form-item>
            </div>
            <el-form-item label="位置描述"><el-input v-model="diseaseForm.positionDescription" type="textarea" :rows="2" size="small" /></el-form-item>
            <el-button size="small" disabled style="margin-top:4px">病害图像标注功能待开发</el-button>
        </div>
        <el-divider />

        <!-- Section 5: 成因分析 -->
        <div class="section">
            <h3>成因分析</h3>
            <div style="margin-bottom:10px">
                <el-checkbox-group v-model="selectedFactors" size="small">
                    <el-checkbox v-for="f in causeFactors" :key="f" :label="f" style="margin-right:12px;margin-bottom:6px">{{ f }}</el-checkbox>
                </el-checkbox-group>
            </div>
            <el-form-item label="详细分析"><el-input v-model="diseaseForm.causeAnalysis" type="textarea" :rows="3" size="small" /></el-form-item>
        </div>
        <el-divider />

        <!-- Section 7: 初步处理建议 -->
        <div class="section">
            <h3>初步处理建议</h3>
            <el-form-item label="建议措施"><el-input v-model="diseaseForm.recommendedAction" type="textarea" :rows="3" size="small" /></el-form-item>
        </div>
    </div>

    <!-- 未选择病害 -->
    <div class="paper empty-paper" v-else>
        <el-empty description="请从左侧选择一条病害" :image-size="60" />
    </div>
</template>

<style scoped>
.paper { flex: 1; background: #fff; border: 1px solid #E5E6EB; border-radius: 10px; padding: 20px 24px; overflow-y: auto; }
.empty-paper { display: flex; align-items: center; justify-content: center; }
.section { margin-bottom: 12px; }
.section h3 { margin: 0 0 12px; font-size: 15px; font-weight: 700; color: #1D2129; }
.location-header { display: flex; gap: 16px; }
</style>
