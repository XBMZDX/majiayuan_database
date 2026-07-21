<script setup>
import { computed, ref, watch } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'

const props = defineProps({
    modelValue: Boolean,
    media: Object
})
const emit = defineEmits(['update:modelValue', 'save'])

const visible = computed({
    get: () => props.modelValue,
    set: value => emit('update:modelValue', value)
})
const annotations = ref([])
const selectedId = ref(null)
const imageSize = ref({ width: 1000, height: 700 })
const drawing = ref(null)
const svgRef = ref()
const defaultLabel = ref('病害区域')
const defaultColor = ref('#ef4444')

const selected = computed(() => annotations.value.find(item => item.id === selectedId.value))

const copyAnnotations = value => {
    if (!Array.isArray(value)) return []
    return value.map(item => ({
        ...item,
        id: item.id || `annotation-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
        x: Number(item.x) || 0,
        y: Number(item.y) || 0,
        width: Number(item.width) || 0,
        height: Number(item.height) || 0
    }))
}

watch(() => [props.modelValue, props.media?.id], () => {
    if (!props.modelValue) return
    // Vue 传入的 media/annotations 是响应式 Proxy，不能直接 structuredClone。
    annotations.value = copyAnnotations(props.media?.annotations)
    selectedId.value = annotations.value[0]?.id || null
}, { immediate: true })

const onImageLoad = event => {
    imageSize.value = {
        width: event.target.naturalWidth || 1000,
        height: event.target.naturalHeight || 700
    }
}

const point = event => {
    const box = svgRef.value.getBoundingClientRect()
    return {
        x: Math.max(0, Math.min(imageSize.value.width,
            (event.clientX - box.left) / box.width * imageSize.value.width)),
        y: Math.max(0, Math.min(imageSize.value.height,
            (event.clientY - box.top) / box.height * imageSize.value.height))
    }
}

const startDraw = event => {
    if (event.button !== 0) return
    const start = point(event)
    drawing.value = {
        id: `annotation-${Date.now()}`,
        type: 'rect',
        label: defaultLabel.value || '病害区域',
        color: defaultColor.value,
        x: start.x,
        y: start.y,
        width: 0,
        height: 0,
        startX: start.x,
        startY: start.y
    }
    svgRef.value.setPointerCapture(event.pointerId)
}

const moveDraw = event => {
    if (!drawing.value) return
    const current = point(event)
    drawing.value.x = Math.min(drawing.value.startX, current.x)
    drawing.value.y = Math.min(drawing.value.startY, current.y)
    drawing.value.width = Math.abs(current.x - drawing.value.startX)
    drawing.value.height = Math.abs(current.y - drawing.value.startY)
}

const finishDraw = event => {
    if (!drawing.value) return
    moveDraw(event)
    const item = drawing.value
    drawing.value = null
    if (item.width < 5 || item.height < 5) return
    delete item.startX
    delete item.startY
    annotations.value.push(item)
    selectedId.value = item.id
}

const selectAnnotation = (event, id) => {
    event.stopPropagation()
    selectedId.value = id
}

const removeSelected = () => {
    if (!selected.value) return
    annotations.value = annotations.value.filter(item => item.id !== selectedId.value)
    selectedId.value = annotations.value[0]?.id || null
}

const save = () => {
    emit('save', annotations.value.map(({ startX, startY, ...item }) => item))
}
</script>

<template>
    <el-dialog v-model="visible" title="病害图片标注" width="min(1100px, 96vw)" destroy-on-close>
        <div class="annotator">
            <div class="canvas-panel">
                <div class="hint"><Plus /> 在图片上按住鼠标拖动，框选病害区域</div>
                <div class="image-stage">
                    <img :src="media?.displayUrl" :alt="media?.title || media?.fileName" @load="onImageLoad">
                    <svg
                        ref="svgRef"
                        :viewBox="`0 0 ${imageSize.width} ${imageSize.height}`"
                        preserveAspectRatio="none"
                        @pointerdown="startDraw"
                        @pointermove="moveDraw"
                        @pointerup="finishDraw"
                        @pointercancel="drawing=null"
                    >
                        <g
                            v-for="item in annotations"
                            :key="item.id"
                            class="annotation"
                            :class="{ selected: item.id === selectedId }"
                            @pointerdown="selectAnnotation($event, item.id)"
                        >
                            <rect
                                :x="item.x" :y="item.y" :width="item.width" :height="item.height"
                                :stroke="item.color || '#ef4444'"
                            />
                            <text
                                :x="item.x + 6"
                                :y="Math.max(18, item.y + 20)"
                                :fill="item.color || '#ef4444'"
                            >{{ item.label }}</text>
                        </g>
                        <rect
                            v-if="drawing"
                            class="drawing"
                            :x="drawing.x" :y="drawing.y"
                            :width="drawing.width" :height="drawing.height"
                            :stroke="drawing.color"
                        />
                    </svg>
                </div>
            </div>
            <aside>
                <h4>新标注默认信息</h4>
                <el-input v-model="defaultLabel" placeholder="例如：裂隙、粉化区域" />
                <el-color-picker v-model="defaultColor" />
                <el-divider />
                <div class="annotation-title">
                    <h4>标注列表（{{ annotations.length }}）</h4>
                    <el-button :icon="Delete" type="danger" link :disabled="!selected" @click="removeSelected">
                        删除选中
                    </el-button>
                </div>
                <button
                    v-for="(item, index) in annotations"
                    :key="item.id"
                    class="annotation-item"
                    :class="{ active: item.id === selectedId }"
                    @click="selectedId=item.id"
                >
                    <i :style="{ background: item.color }" />
                    <span>{{ index + 1 }}. {{ item.label || '未命名区域' }}</span>
                </button>
                <el-empty v-if="!annotations.length" description="尚未添加标注" :image-size="50" />
                <template v-if="selected">
                    <el-divider />
                    <h4>编辑选中标注</h4>
                    <el-input v-model="selected.label" placeholder="标注名称" />
                    <el-color-picker v-model="selected.color" />
                </template>
            </aside>
        </div>
        <template #footer>
            <el-button @click="visible=false">取消</el-button>
            <el-button type="primary" @click="save">保存标注到数据库</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.annotator { display: grid; grid-template-columns: minmax(0, 1fr) 250px; gap: 16px; }
.canvas-panel { min-width: 0; padding: 12px; background: #f4f5f7; border-radius: 8px; text-align: center; }
.hint { display: flex; align-items: center; justify-content: center; gap: 5px; margin-bottom: 10px; color: #606266; font-size: 13px; }
.hint svg { width: 14px; }
.image-stage { position: relative; display: inline-block; max-width: 100%; line-height: 0; user-select: none; }
.image-stage img { display: block; max-width: 100%; max-height: 62vh; }
.image-stage svg { position: absolute; inset: 0; width: 100%; height: 100%; cursor: crosshair; touch-action: none; }
.annotation rect, .drawing { fill: rgba(239, 68, 68, .08); stroke-width: 3; vector-effect: non-scaling-stroke; }
.annotation text { font-size: 18px; font-weight: 700; paint-order: stroke; stroke: white; stroke-width: 4px; pointer-events: none; }
.annotation.selected rect { stroke-width: 5; stroke-dasharray: 10 5; }
aside { padding: 4px; overflow-y: auto; max-height: 68vh; }
aside h4 { margin: 6px 0 10px; }
aside > .el-input { margin-bottom: 8px; }
.annotation-title { display: flex; justify-content: space-between; align-items: center; }
.annotation-item { width: 100%; display: flex; align-items: center; gap: 8px; margin-bottom: 6px; padding: 9px; border: 1px solid #e5e7eb; border-radius: 6px; background: #fff; cursor: pointer; text-align: left; }
.annotation-item.active { border-color: #409eff; background: #ecf5ff; }
.annotation-item i { width: 12px; height: 12px; border-radius: 3px; flex: none; }
@media (max-width: 800px) {
    .annotator { grid-template-columns: 1fr; }
    aside { max-height: none; }
}
</style>
