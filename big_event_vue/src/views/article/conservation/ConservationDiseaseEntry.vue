<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getConservationProjects } from '@/api/conservationProject'

const router = useRouter()
const loading = ref(true)
const hasProjects = ref(true)

onMounted(async () => {
    try {
        const result = await getConservationProjects()
        const project = (result.data || [])[0]
        if (project) {
            await router.replace(`/conservation/project/${project.id}/disease`)
            return
        }
        hasProjects.value = false
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <div class="entry-page" v-loading="loading">
        <el-empty v-if="!loading && !hasProjects" description="请先新建保护修复项目">
            <el-button type="primary" @click="router.push('/conservation/overview')">前往项目总览</el-button>
        </el-empty>
    </div>
</template>

<style scoped>
.entry-page { min-height: 420px; padding: 40px; background: #fff; border-radius: 10px; }
</style>
