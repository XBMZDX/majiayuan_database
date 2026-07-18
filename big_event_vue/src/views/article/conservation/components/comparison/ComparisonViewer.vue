<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import {
    Aim, FullScreen, Minus, Plus, RefreshLeft, RefreshRight, Rank, ScaleToOriginal, Switch
} from '@element-plus/icons-vue'

const props = defineProps({
    images: { type: Array, default: () => [] },
    shootingPosition: { type: String, default: '' },
    readOnly: Boolean
})
const emit = defineEmits(['dirty', 'reorder'])

const mode = ref('side')
const sliderPosition = ref(50)
const syncView = ref(true)
const activeSide = ref('before')
const stageIndex = ref(0)
const fullTarget = ref(null)
const dragging = reactive({ active: false, side: '', startX: 0, startY: 0, originX: 0, originY: 0 })
const views = reactive({
    before: { scale: 1, x: 0, y: 0, rotation: 0 },
    after: { scale: 1, x: 0, y: 0, rotation: 0 }
})

const before = computed(() => props.images.find(item => item.imageStage === 'before' && item.isPrimary)
    || props.images.find(item => item.imageStage === 'before'))
const after = computed(() => props.images.find(item => item.imageStage === 'after' && item.isPrimary)
    || props.images.find(item => item.imageStage === 'after'))
const sequence = computed(() => [...props.images].sort((a, b) => a.sequenceNo - b.sequenceNo))
const stageLabel = { before: '修复前', during: '修复中', after: '修复后', follow_up: '后期复查', reference: '参考影像' }
const mismatch = computed(() => before.value && after.value && (
    before.value.targetPart !== after.value.targetPart
    || before.value.shootingPosition !== after.value.shootingPosition
))
const imageStyle = side => {
    const view = views[side]
    return {
        transform: `translate(${view.x}px, ${view.y}px) scale(${view.scale}) rotate(${view.rotation}deg)`
    }
}

const applyView = (callback) => {
    if (syncView.value) {
        callback(views.before)
        callback(views.after)
    } else callback(views[activeSide.value])
}
const zoom = delta => applyView(view => { view.scale = Math.min(4, Math.max(.5, +(view.scale + delta).toFixed(2))) })
const rotate = delta => applyView(view => { view.rotation = (view.rotation + delta) % 360 })
const reset = () => applyView(view => Object.assign(view, { scale: 1, x: 0, y: 0, rotation: 0 }))
const original = () => applyView(view => Object.assign(view, { scale: 1.6, x: 0, y: 0 }))

const startDrag = (event, side) => {
    activeSide.value = side
    dragging.active = true
    dragging.side = side
    dragging.startX = event.clientX
    dragging.startY = event.clientY
    dragging.originX = views[side].x
    dragging.originY = views[side].y
    event.currentTarget.setPointerCapture?.(event.pointerId)
}
const onDrag = event => {
    if (!dragging.active) return
    const dx = event.clientX - dragging.startX
    const dy = event.clientY - dragging.startY
    if (syncView.value) {
        views.before.x = views.after.x = dragging.originX + dx
        views.before.y = views.after.y = dragging.originY + dy
    } else {
        views[dragging.side].x = dragging.originX + dx
        views[dragging.side].y = dragging.originY + dy
    }
}
const endDrag = () => { dragging.active = false }
const openFullscreen = async () => {
    await nextTick()
    if (fullTarget.value?.requestFullscreen) await fullTarget.value.requestFullscreen()
}
const moveSequence = (index, direction) => {
    const target = index + direction
    if (target < 0 || target >= sequence.value.length) return
    const reordered = [...sequence.value]
    const [item] = reordered.splice(index, 1)
    reordered.splice(target, 0, item)
    reordered.forEach((image, order) => { image.sequenceNo = order + 1 })
    stageIndex.value = target
    emit('reorder', reordered)
    emit('dirty')
}

watch(() => props.images, () => {
    stageIndex.value = 0
    sliderPosition.value = 50
    Object.assign(views.before, { scale: 1, x: 0, y: 0, rotation: 0 })
    Object.assign(views.after, { scale: 1, x: 0, y: 0, rotation: 0 })
}, { deep: false })
</script>

<template>
    <section id="comparison-section-viewer" class="viewer-section" ref="fullTarget">
        <div class="viewer-head">
            <div class="mode-switch">
                <el-radio-group v-model="mode" size="small">
                    <el-radio-button value="side">并排对比</el-radio-button>
                    <el-radio-button value="slider">滑块对比</el-radio-button>
                    <el-radio-button value="sequence">阶段序列</el-radio-button>
                </el-radio-group>
            </div>
            <div class="tools">
                <el-button-group size="small">
                    <el-tooltip content="放大"><el-button :icon="Plus" @click="zoom(.2)" /></el-tooltip>
                    <el-tooltip content="缩小"><el-button :icon="Minus" @click="zoom(-.2)" /></el-tooltip>
                    <el-tooltip content="适应窗口"><el-button :icon="Aim" @click="reset" /></el-tooltip>
                    <el-tooltip content="原始尺寸"><el-button :icon="ScaleToOriginal" @click="original" /></el-tooltip>
                    <el-tooltip content="向左旋转"><el-button :icon="RefreshLeft" @click="rotate(-90)" /></el-tooltip>
                    <el-tooltip content="向右旋转"><el-button :icon="RefreshRight" @click="rotate(90)" /></el-tooltip>
                    <el-tooltip content="重置"><el-button :icon="Rank" @click="reset" /></el-tooltip>
                    <el-tooltip content="全屏"><el-button :icon="FullScreen" @click="openFullscreen" /></el-tooltip>
                </el-button-group>
                <el-switch v-model="syncView" inline-prompt active-text="同步" inactive-text="独立" />
                <el-radio-group v-if="!syncView" v-model="activeSide" size="small">
                    <el-radio-button value="before">前图</el-radio-button>
                    <el-radio-button value="after">后图</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <el-alert
            v-if="mismatch"
            title="修复前后影像的部位或拍摄角度不完全一致，仅适合状态参考，不建议用于精确量化。"
            type="warning"
            show-icon
            :closable="false"
        />

        <div v-if="mode === 'side'" class="side-view">
            <div v-for="(image,side) in { before, after }" :key="side" class="image-panel" :class="{ selected: activeSide === side }">
                <div class="image-label"><b>{{ side === 'before' ? '修复前' : '修复后' }}</b><span>{{ image?.shootingTime || '未选择影像' }} · {{ image?.shootingPosition || '-' }}</span></div>
                <div
                    class="image-stage"
                    @pointerdown="image && startDrag($event, side)"
                    @pointermove="onDrag"
                    @pointerup="endDrag"
                    @pointercancel="endDrag"
                >
                    <img v-if="image" :src="image.fileUrl" :alt="image.fileName" draggable="false" :style="imageStyle(side)">
                    <el-empty v-else :description="`缺少${side === 'before' ? '修复前' : '修复后'}影像`" :image-size="68" />
                </div>
            </div>
        </div>

        <div v-else-if="mode === 'slider'" class="slider-view image-stage">
            <template v-if="before && after">
                <img :src="after.fileUrl" :alt="after.fileName" draggable="false" :style="imageStyle('after')">
                <div class="before-clip" :style="{ width: `${sliderPosition}%` }">
                    <img :src="before.fileUrl" :alt="before.fileName" draggable="false" :style="{ ...imageStyle('before'), width: `${10000 / sliderPosition}%` }">
                </div>
                <span class="stage-badge before-badge">修复前</span>
                <span class="stage-badge after-badge">修复后</span>
                <div class="slider-line" :style="{ left: `${sliderPosition}%` }"><i>↔</i></div>
                <input v-model.number="sliderPosition" type="range" min="3" max="97" aria-label="前后对比滑块">
            </template>
            <el-empty v-else description="滑块对比需要同时选择修复前和修复后影像" :image-size="70" />
        </div>

        <div v-else class="sequence-view">
            <div class="sequence-main image-stage">
                <img
                    v-if="sequence[stageIndex]"
                    :src="sequence[stageIndex].fileUrl"
                    :alt="sequence[stageIndex].fileName"
                    draggable="false"
                    :style="imageStyle(sequence[stageIndex].imageStage === 'before' ? 'before' : 'after')"
                >
                <el-empty v-else description="暂无阶段影像" />
            </div>
            <div class="sequence-strip">
                <div v-for="(image,index) in sequence" :key="image.id" class="sequence-item" :class="{ active: index === stageIndex }" @click="stageIndex = index">
                    <img :src="image.thumbnailUrl || image.fileUrl" :alt="image.fileName">
                    <b>{{ stageLabel[image.imageStage] }}</b>
                    <span>{{ image.shootingTime }}</span>
                    <div v-if="!readOnly" class="sequence-order">
                        <el-button link :icon="RefreshLeft" :disabled="index === 0" @click.stop="moveSequence(index,-1)" />
                        <el-button link :icon="RefreshRight" :disabled="index === sequence.length - 1" @click.stop="moveSequence(index,1)" />
                    </div>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.viewer-section { background: #111716; color: #eef3f1; padding: 12px; border-radius: 7px; }
.viewer-section:fullscreen { padding: 24px; display: flex; flex-direction: column; background: #0e1312; }
.viewer-section:fullscreen .side-view, .viewer-section:fullscreen .slider-view, .viewer-section:fullscreen .sequence-view { flex: 1; min-height: 0; }
.viewer-section:fullscreen .image-stage { min-height: 0; height: 100%; }
.viewer-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 10px; }
.tools { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.viewer-section :deep(.el-alert) { margin-bottom: 10px; }
.side-view { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.image-panel { border: 1px solid #313b38; border-radius: 5px; overflow: hidden; }
.image-panel.selected { border-color: #72a694; }
.image-label { height: 42px; display: flex; justify-content: space-between; gap: 8px; align-items: center; padding: 0 10px; background: #1d2623; }
.image-label b { font-size: 12px; } .image-label span { color: #9caaa5; font-size: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.image-stage { position: relative; min-height: 350px; background: #0b0e0d; overflow: hidden; display: grid; place-items: center; touch-action: none; }
.image-stage img { max-width: 100%; max-height: 100%; object-fit: contain; user-select: none; transform-origin: center; transition: transform .12s ease-out; }
.slider-view { height: 440px; display: block; }
.slider-view > img, .before-clip > img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: contain; }
.before-clip { position: absolute; inset: 0 auto 0 0; overflow: hidden; z-index: 2; background: #0b0e0d; }
.before-clip > img { max-width: none; max-height: none; object-fit: contain; object-position: left center; transform-origin: center; }
.slider-line { position: absolute; z-index: 4; top: 0; bottom: 0; width: 2px; background: #fff; transform: translateX(-1px); }
.slider-line i { position: absolute; left: 50%; top: 50%; transform: translate(-50%,-50%); width: 34px; height: 34px; border-radius: 50%; background: #fff; color: #2d5e50; display: grid; place-items: center; font-style: normal; box-shadow: 0 2px 8px #0008; }
.slider-view input { position: absolute; z-index: 5; inset: 0; width: 100%; height: 100%; opacity: 0; cursor: ew-resize; }
.stage-badge { position: absolute; top: 12px; z-index: 4; padding: 4px 9px; border-radius: 4px; background: rgb(16 25 22 / 75%); font-size: 11px; }
.before-badge { left: 12px; } .after-badge { right: 12px; }
.sequence-view { display: grid; grid-template-rows: 410px auto; gap: 10px; }
.sequence-main { min-height: 0; }
.sequence-strip { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; }
.sequence-item { flex: 0 0 122px; padding: 5px; border: 1px solid #37423f; border-radius: 5px; cursor: pointer; position: relative; }
.sequence-item.active { border-color: #7eb29f; background: #21322d; }
.sequence-item img { width: 100%; height: 66px; object-fit: cover; border-radius: 3px; }
.sequence-item b, .sequence-item span { display: block; margin-top: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sequence-item b { font-size: 10px; }.sequence-item span { color: #8f9d98; font-size: 8px; }
.sequence-order { position: absolute; top: 4px; right: 4px; display: none; background: #ffffffe8; border-radius: 3px; }
.sequence-item:hover .sequence-order { display: flex; }
@media (max-width: 920px) {
    .viewer-head { align-items: flex-start; flex-direction: column; }
    .side-view { grid-template-columns: 1fr; }
    .image-stage { min-height: 280px; }
    .slider-view { height: 340px; }
}
</style>
