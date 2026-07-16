<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import ExperimentData from './experiment/ExperimentData.vue'
import ExperimentImage from './experiment/ExperimentImage.vue'
import ExperimentConclusion from './experiment/ExperimentConclusion.vue'
import ExperimentAttachment from './experiment/ExperimentAttachment.vue'

const route = useRoute()
const router = useRouter()
const result = ref(null)
const experiments = ref([])
const selectedExp = ref(null)
const loading = ref(false)

// ========== 上一条 / 下一条 ==========
const siblingIds = ref([])
const currentIdx = computed(() => siblingIds.value.indexOf(Number(route.params.id)))
const hasPrev = computed(() => currentIdx.value > 0)
const hasNext = computed(() => currentIdx.value < siblingIds.value.length - 1)
const goPrev = () => { if (hasPrev.value) router.replace('/detection/experiment/' + siblingIds.value[currentIdx.value - 1]) }
const goNext = () => { if (hasNext.value) router.replace('/detection/experiment/' + siblingIds.value[currentIdx.value + 1]) }
const goBack = () => {
    if (window.history.length > 1) router.back()
    else router.push('/detection/result')
}

const fetchDetail = async () => {
    const id = route.params.id
    if (!id) return
    loading.value = true
    // 加载 ID 列表
    try { siblingIds.value = JSON.parse(sessionStorage.getItem('expIds') || '[]') } catch { siblingIds.value = [] }
    const res = await request.get('/admin/analysis-result/' + id)
    result.value = res.data || null
    if (result.value?.detectionId) {
        const expRes = await request.get('/admin/experiment-result/by-detection/' + result.value.detectionId)
        experiments.value = expRes.data || []
        const match = experiments.value.find(e => e.experimentName === result.value.experimentMethod)
        selectedExp.value = match || experiments.value[0] || null
    }
    loading.value = false
}

const refreshExp = async () => {
    if (!result.value?.detectionId) return
    const expRes = await request.get('/admin/experiment-result/by-detection/' + result.value.detectionId)
    experiments.value = expRes.data || []
    const match = experiments.value.find(e => e.id === selectedExp.value?.id)
    if (match) selectedExp.value = match
}

const onDataUpdate = async (data) => {
    if (!selectedExp.value?.id) return
    await request.put('/admin/experiment-result/' + selectedExp.value.id, { ...selectedExp.value, resultData: data })
    refreshExp()
}
const onNotesUpdate = async (data) => {
    if (!selectedExp.value?.id) return
    await request.put('/admin/experiment-result/' + selectedExp.value.id, { ...selectedExp.value, notes: data })
    refreshExp()
}
const onAttUpdate = async (data) => {
    if (!selectedExp.value?.id) return
    await request.put('/admin/experiment-result/' + selectedExp.value.id, { ...selectedExp.value, attachments: data })
    refreshExp()
}
const onImageUpdate = async (data) => {
    if (!selectedExp.value?.id) return
    await request.put('/admin/experiment-result/' + selectedExp.value.id, { ...selectedExp.value, images: data })
    refreshExp()
}

onMounted(fetchDetail)
</script>

<template>
    <div class="detail-root">
        <div class="detail-page" v-loading="loading">
            <template v-if="result">
                <!-- 统一大卡片 -->
                <el-card shadow="never" class="report-card">
                    <!-- 卡片标题 = 导航栏 -->
                    <template #header>
                        <div class="card-nav">
                            <div class="nav-left">
                                <el-button :icon="ArrowLeft" text size="large" @click="goBack" class="back-btn">
                                    返回实验列表
                                </el-button>
                            </div>
                            <div class="nav-center">
                                <span class="nav-title">{{ result.experimentMethod || '实验' }} — {{ result.artifactName || '未知' }}</span>
                            </div>
                            <div class="nav-right">
                                <el-button size="small" :disabled="!hasPrev" @click="goPrev">◀ 上一个</el-button>
                                <el-button size="small" :disabled="!hasNext" @click="goNext">下一个 ▶</el-button>
                            </div>
                        </div>
                    </template>

                    <!-- 卡片内容 -->
                    <div class="report-body">
                        <!-- 元信息 -->
                        <div class="report-meta">
                            <el-tag size="small" effect="plain" type="primary">{{ result.experimentMethod }}</el-tag>
                            <el-tag size="small" effect="plain">{{ result.artifactCode }}</el-tag>
                            <el-tag size="small" effect="plain" type="success">{{ selectedExp?.status || '待检测' }}</el-tag>
                        </div>

                        <!-- 实验切换 Tabs -->
                        <el-tabs v-if="experiments.length > 1" v-model="selectedExp" type="card" style="margin-top:8px">
                            <el-tab-pane v-for="exp in experiments" :key="exp.id" :label="exp.experimentName" :name="exp" />
                        </el-tabs>

                        <!-- 四个展示模块 -->
                        <ExperimentData :experiment="selectedExp" @update="onDataUpdate" />
                        <ExperimentImage :experiment="selectedExp" @update="onImageUpdate" />
                        <ExperimentConclusion :experiment="selectedExp" @update="onNotesUpdate" />
                        <ExperimentAttachment :experiment="selectedExp" @update="onAttUpdate" />
                    </div>
                </el-card>
            </template>
            <el-empty v-else description="未找到实验数据" :image-size="100" style="margin-top:80px" />
        </div>
    </div>
</template>

<style scoped>
.detail-root { min-height: 100vh; background: #F5F7FA; }
.detail-page { max-width: 1000px; margin: 0 auto; padding: 24px 0 60px; }

/* 统一大卡片 */
.report-card { border: 1px solid #E5E6EB; border-radius: 10px; overflow: hidden; }
.report-card :deep(.el-card__header) { padding: 0 24px; border-bottom: 1px solid #E5E6EB; }
.report-card :deep(.el-card__body) { padding: 0; }

/* 卡片内导航 */
.card-nav { display: flex; align-items: center; justify-content: space-between; height: 52px; }
.nav-left, .nav-right { display: flex; align-items: center; gap: 8px; flex: 1; }
.nav-center { flex: 2; text-align: center; }
.nav-title { font-size: 15px; font-weight: 600; color: #1D2129; }
.nav-right { justify-content: flex-end; }
.back-btn { font-size: 15px; transition: color .15s; }
.back-btn:hover { color: #409EFF !important; }

/* 卡片内容 */
.report-body { padding: 20px 28px 28px; }
.report-meta { display: flex; gap: 8px; flex-wrap: wrap; }

/* 页面过渡 */
.detail-root { animation: fadeIn .3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateX(10px); } to { opacity: 1; transform: translateX(0); } }
</style>
