<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DiseaseProjectSummary from './components/disease/DiseaseProjectSummary.vue'
import DiseaseRecordList from './components/disease/DiseaseRecordList.vue'
import DiseaseSurveyPaper from './components/disease/DiseaseSurveyPaper.vue'
import DiseaseImageAnnotator from './components/disease/DiseaseImageAnnotator.vue'
import {
    deleteDiseaseMedia,
    getDiseaseWorkbench,
    getDiseaseMediaContent,
    saveDiseaseMediaAnnotations,
    saveDiseaseWorkbench,
    uploadDiseaseMedia
} from '@/api/conservationDisease'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId) || null)
const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)
const project = ref(null)
const survey = ref({})
const diseaseRecords = ref([])
const activeDisease = ref(null)
const annotationMedia = ref(null)
const annotationVisible = ref(false)
const mediaObjectUrls = new Map()

const severeCount = computed(() => diseaseRecords.value.filter(record =>
    record.severity === 'severe' || record.severity === 'critical'
).length)

const applyWorkbench = (data, preferredIndex = null) => {
    project.value = data.project
    survey.value = data.survey || {}
    diseaseRecords.value = data.records || []
    const previousId = activeDisease.value?.id
    activeDisease.value = (preferredIndex !== null ? diseaseRecords.value[preferredIndex] : null)
        || diseaseRecords.value.find(item => item.id === previousId)
        || diseaseRecords.value[0]
        || null
}

const loadMediaPreview = async media => {
    if (mediaObjectUrls.has(media.id)) {
        media.displayUrl = mediaObjectUrls.get(media.id)
        return
    }
    const response = await getDiseaseMediaContent(media.id)
    const url = URL.createObjectURL(response.data)
    mediaObjectUrls.set(media.id, url)
    media.displayUrl = url
}

const hydrateMediaPreviews = async records => {
    await Promise.all(records.flatMap(record =>
        (record.media || []).map(media => loadMediaPreview(media))
    ))
}

const load = async () => {
    loading.value = true
    try {
        if (!projectId.value) throw new Error('缺少保护修复项目编号')
        const result = await getDiseaseWorkbench(projectId.value)
        applyWorkbench(result.data)
        await hydrateMediaPreviews(diseaseRecords.value)
    } catch (error) {
        ElMessage.error(error?.message || '病害调查加载失败')
        router.replace('/conservation/overview')
    } finally {
        loading.value = false
    }
}

const selectDisease = record => {
    activeDisease.value = record
}
const addDisease = () => {
    const record = {
        id: -Date.now(),
        surveyId: survey.value.id,
        projectId: projectId.value,
        diseaseTypeId: null,
        diseaseType: '',
        diseaseName: '',
        diseaseCategory: '',
        severity: 'minor',
        developmentStatus: 'stable',
        extentValue: null,
        extentUnit: '',
        partName: '',
        side: '',
        positionDescription: '',
        morphology: '',
        causeFactors: [],
        causeAnalysis: '',
        structuralImpact: 'none',
        emergency: false,
        recommendedAction: '',
        mediaCount: 0
    }
    diseaseRecords.value.push(record)
    activeDisease.value = record
}
const deleteDisease = async record => {
    try {
        await ElMessageBox.confirm('确定删除这条病害记录吗？保存后将同步删除数据库记录。', '删除病害', {
            type: 'warning'
        })
        const index = diseaseRecords.value.findIndex(item => item.id === record.id)
        if (index >= 0) diseaseRecords.value.splice(index, 1)
        activeDisease.value = diseaseRecords.value[0] || null
    } catch {
        // 用户取消删除。
    }
}
const updateSurvey = value => {
    Object.assign(survey.value, value)
}
const updateDisease = value => {
    if (activeDisease.value) Object.assign(activeDisease.value, value)
}

const persist = async (submit, options = {}) => {
    saving.value = true
    try {
        const activeIndex = options.activeIndex ?? diseaseRecords.value.indexOf(activeDisease.value)
        const result = await saveDiseaseWorkbench(
            projectId.value,
            survey.value,
            diseaseRecords.value,
            submit
        )
        applyWorkbench(result.data, activeIndex >= 0 ? activeIndex : null)
        await hydrateMediaPreviews(diseaseRecords.value)
        if (!options.silent) {
            ElMessage.success(submit ? '病害调查已提交并更新项目风险' : '调查数据已保存到数据库')
        }
        return true
    } catch (error) {
        ElMessage.error(error?.message || '病害调查保存失败')
        return false
    } finally {
        saving.value = false
    }
}

const uploadMedia = async files => {
    if (!activeDisease.value?.diseaseName?.trim()) {
        return ElMessage.warning('请先填写病害名称，再上传图片')
    }
    const activeIndex = diseaseRecords.value.indexOf(activeDisease.value)
    uploading.value = true
    try {
        const saved = await persist(false, { silent: true, activeIndex })
        if (!saved || !activeDisease.value?.id || activeDisease.value.id < 0) return
        for (const file of files) {
            const formData = new FormData()
            formData.append('file', file)
            formData.append('title', file.name)
            const result = await uploadDiseaseMedia(activeDisease.value.id, formData)
            await loadMediaPreview(result.data)
            activeDisease.value.media ||= []
            activeDisease.value.media.push(result.data)
        }
        activeDisease.value.mediaCount = activeDisease.value.media.length
        ElMessage.success(`已上传 ${files.length} 张病害图片`)
    } catch (error) {
        ElMessage.error(error?.message || '病害图片上传失败')
    } finally {
        uploading.value = false
    }
}

const openAnnotator = async media => {
    try {
        if (!media?.id) return ElMessage.warning('图片尚未保存，暂时不能标注')
        if (!media.displayUrl) await loadMediaPreview(media)
        annotationMedia.value = media
        annotationVisible.value = true
    } catch (error) {
        ElMessage.error(error?.message || '病害图片加载失败，无法打开标注')
    }
}

const saveAnnotations = async annotations => {
    try {
        const result = await saveDiseaseMediaAnnotations(annotationMedia.value.id, annotations)
        Object.assign(annotationMedia.value, result.data)
        annotationVisible.value = false
        ElMessage.success('图片标注已保存到数据库')
    } catch (error) {
        ElMessage.error(error?.message || '图片标注保存失败')
    }
}

const removeMedia = async media => {
    try {
        await ElMessageBox.confirm(`确定删除图片“${media.title || media.fileName}”吗？`, '删除病害图片', {
            type: 'warning'
        })
        await deleteDiseaseMedia(media.id)
        const objectUrl = mediaObjectUrls.get(media.id)
        if (objectUrl) URL.revokeObjectURL(objectUrl)
        mediaObjectUrls.delete(media.id)
        activeDisease.value.media = activeDisease.value.media.filter(item => item.id !== media.id)
        activeDisease.value.mediaCount = activeDisease.value.media.length
        ElMessage.success('病害图片已删除')
    } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
            ElMessage.error(error?.message || '病害图片删除失败')
        }
    }
}

onMounted(load)
onBeforeUnmount(() => {
    mediaObjectUrls.forEach(url => URL.revokeObjectURL(url))
    mediaObjectUrls.clear()
})
</script>

<template>
    <div class="disease-page" v-loading="loading || saving">
        <DiseaseProjectSummary
            v-if="project"
            :project="project"
            :survey="survey"
            :diseaseCount="diseaseRecords.length"
            :severeCount="severeCount"
            @save="persist(false)"
            @submit="persist(true)"
            @back="router.push('/conservation/overview')"
        />
        <div v-if="project" class="disease-body">
            <DiseaseRecordList
                :records="diseaseRecords"
                :activeId="activeDisease?.id"
                @select="selectDisease"
                @add="addDisease"
                @delete="deleteDisease"
            />
            <DiseaseSurveyPaper
                :survey="survey"
                :activeDisease="activeDisease"
                :uploading="uploading"
                @update:survey="updateSurvey"
                @update:disease="updateDisease"
                @upload-media="uploadMedia"
                @annotate-media="openAnnotator"
                @delete-media="removeMedia"
            />
        </div>
        <DiseaseImageAnnotator
            v-model="annotationVisible"
            :media="annotationMedia"
            @save="saveAnnotations"
        />
    </div>
</template>

<style scoped>
.disease-page { display: flex; flex-direction: column; height: calc(100vh - 140px); min-height: 500px; }
.disease-body { flex: 1; display: flex; gap: 14px; min-height: 0; }
@media (max-width: 1100px) {
    .disease-body { flex-direction: column; }
}
</style>
