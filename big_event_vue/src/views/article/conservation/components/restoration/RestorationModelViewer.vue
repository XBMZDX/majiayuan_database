<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js'
import { FBXLoader } from 'three/examples/jsm/loaders/FBXLoader.js'
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js'
import { PLYLoader } from 'three/examples/jsm/loaders/PLYLoader.js'
import { getRestorationModelContent } from '@/api/conservationRestoration'

const props = defineProps({
    visible: Boolean,
    model: { type: Object, default: null },
    parts: { type: Array, default: () => [] },
    readOnly: Boolean
})
const emit = defineEmits(['update:visible', 'dirty'])

const host = ref(null)
const loading = ref(false)
const error = ref('')
const objectNames = ref([])
const selectedPartId = ref(null)
const annotationMode = ref(false)
const selectedPart = computed(() => props.parts.find(item => item.id === selectedPartId.value) || null)
const layerParts = computed(() => props.parts.filter(item => item.modelLayer))

let scene
let camera
let renderer
let controls
let rootObject
let animationFrame
let resizeObserver
let objectUrl
let selectionHelper
let annotationsGroup
const raycaster = new THREE.Raycaster()
const pointer = new THREE.Vector2()

const modelFormat = computed(() => {
    const explicit = String(props.model?.fileFormat || '').toLowerCase().replace('.', '')
    if (explicit) return explicit
    return String(props.model?.fileName || '').split('.').pop()?.toLowerCase() || ''
})

const close = () => emit('update:visible', false)

const initScene = () => {
    if (!host.value) return
    disposeScene()
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0x18201d)
    camera = new THREE.PerspectiveCamera(45, 1, 0.01, 100000)
    camera.position.set(3, 2, 4)
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.15
    host.value.appendChild(renderer.domElement)
    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.screenSpacePanning = true
    scene.add(new THREE.HemisphereLight(0xffffff, 0x35453f, 2.2))
    const key = new THREE.DirectionalLight(0xffffff, 3)
    key.position.set(4, 6, 5)
    scene.add(key)
    const fill = new THREE.DirectionalLight(0x9bc9ff, 1.2)
    fill.position.set(-5, 2, -4)
    scene.add(fill)
    annotationsGroup = new THREE.Group()
    annotationsGroup.name = '__restoration_annotations__'
    scene.add(annotationsGroup)
    renderer.domElement.addEventListener('pointerdown', onPointerDown)
    resizeObserver = new ResizeObserver(resize)
    resizeObserver.observe(host.value)
    resize()
    animate()
}

const resize = () => {
    if (!host.value || !renderer || !camera) return
    const width = Math.max(320, host.value.clientWidth)
    const height = Math.max(360, host.value.clientHeight)
    renderer.setSize(width, height, false)
    camera.aspect = width / height
    camera.updateProjectionMatrix()
}

const animate = () => {
    animationFrame = requestAnimationFrame(animate)
    controls?.update()
    renderer?.render(scene, camera)
}

const disposeObject = object => {
    object?.traverse(child => {
        child.geometry?.dispose?.()
        const materials = Array.isArray(child.material) ? child.material : [child.material]
        materials.filter(Boolean).forEach(material => {
            Object.values(material).forEach(value => value?.isTexture && value.dispose())
            material.dispose?.()
        })
    })
}

const disposeScene = () => {
    if (animationFrame) cancelAnimationFrame(animationFrame)
    animationFrame = null
    resizeObserver?.disconnect()
    resizeObserver = null
    if (renderer?.domElement) renderer.domElement.removeEventListener('pointerdown', onPointerDown)
    disposeObject(rootObject)
    disposeObject(annotationsGroup)
    renderer?.dispose()
    renderer?.domElement?.remove()
    if (objectUrl) URL.revokeObjectURL(objectUrl)
    objectUrl = null
    scene = camera = renderer = controls = rootObject = selectionHelper = annotationsGroup = null
}

const parseModel = async blob => {
    const format = modelFormat.value
    objectUrl = URL.createObjectURL(blob)
    if (['glb', 'gltf'].includes(format)) {
        const gltf = await new GLTFLoader().loadAsync(objectUrl)
        return gltf.scene
    }
    if (format === 'obj') return new OBJLoader().loadAsync(objectUrl)
    if (format === 'fbx') return new FBXLoader().loadAsync(objectUrl)
    if (format === 'stl') {
        const geometry = new STLLoader().parse(await blob.arrayBuffer())
        geometry.computeVertexNormals()
        return new THREE.Mesh(geometry, new THREE.MeshStandardMaterial({ color: 0xb7c7c0, roughness: 0.72, metalness: 0.08 }))
    }
    if (format === 'ply') {
        const geometry = new PLYLoader().parse(await blob.arrayBuffer())
        geometry.computeVertexNormals()
        const material = new THREE.MeshStandardMaterial({
            color: geometry.hasAttribute('color') ? 0xffffff : 0xb7c7c0,
            vertexColors: geometry.hasAttribute('color'),
            roughness: 0.72
        })
        return new THREE.Mesh(geometry, material)
    }
    throw new Error(`暂不支持 ${format || '未知'} 格式的浏览器渲染，请上传 GLB、GLTF、OBJ、FBX、STL 或 PLY 文件`)
}

const prepareNodes = object => {
    let index = 1
    const names = []
    object.traverse(node => {
        if (!node.isMesh) return
        node.name ||= `模型部件-${index++}`
        node.userData.restorationNode = true
        names.push(node.name)
        node.castShadow = true
        node.receiveShadow = true
    })
    objectNames.value = [...new Set(names)]
}

const fitCamera = object => {
    const box = new THREE.Box3().setFromObject(object)
    if (box.isEmpty()) return
    const size = box.getSize(new THREE.Vector3())
    const center = box.getCenter(new THREE.Vector3())
    const radius = Math.max(size.x, size.y, size.z, 0.01)
    camera.near = Math.max(radius / 1000, 0.001)
    camera.far = radius * 1000
    camera.position.copy(center).add(new THREE.Vector3(radius * 1.35, radius * 0.9, radius * 1.35))
    camera.updateProjectionMatrix()
    controls.target.copy(center)
    controls.update()
}

const loadModel = async () => {
    if (!props.model?.id || !scene) return
    loading.value = true
    error.value = ''
    try {
        const response = await getRestorationModelContent(props.model.id)
        rootObject = await parseModel(response.data)
        rootObject.name ||= props.model.modelName || '复原模型'
        prepareNodes(rootObject)
        scene.add(rootObject)
        fitCamera(rootObject)
        applyLayers()
        rebuildAnnotations()
    } catch (reason) {
        console.error(reason)
        error.value = reason.message || '三维模型读取或解析失败'
        ElMessage.error(error.value)
    } finally {
        loading.value = false
    }
}

const findObject = name => {
    if (!rootObject || !name) return null
    return rootObject.getObjectByName(name) || null
}

const applyLayers = () => {
    if (!rootObject) return
    for (const part of layerParts.value) {
        const object = findObject(part.modelObjectName)
        if (object) object.visible = part.layerVisible !== false
    }
}

const labelSprite = part => {
    const canvas = document.createElement('canvas')
    canvas.width = 512
    canvas.height = 128
    const context = canvas.getContext('2d')
    context.fillStyle = 'rgba(25, 34, 31, .88)'
    context.roundRect(4, 4, 504, 120, 18)
    context.fill()
    context.strokeStyle = '#e0b77b'
    context.lineWidth = 4
    context.stroke()
    context.fillStyle = '#fff'
    context.font = 'bold 34px sans-serif'
    context.fillText(part.partName || '三维标注', 25, 52)
    context.fillStyle = '#cbd6d1'
    context.font = '24px sans-serif'
    context.fillText(String(part.annotationText || '未填写说明').slice(0, 28), 25, 92)
    const texture = new THREE.CanvasTexture(canvas)
    texture.colorSpace = THREE.SRGBColorSpace
    const material = new THREE.SpriteMaterial({ map: texture, depthTest: false, transparent: true })
    const sprite = new THREE.Sprite(material)
    sprite.name = `annotation-${part.id}`
    sprite.userData.annotation = true
    sprite.userData.partId = part.id
    const position = part.annotationPosition
    sprite.position.set(Number(position.x) || 0, Number(position.y) || 0, Number(position.z) || 0)
    const box = rootObject ? new THREE.Box3().setFromObject(rootObject) : null
    const scale = box && !box.isEmpty() ? Math.max(box.getSize(new THREE.Vector3()).length() * 0.16, 0.1) : 1
    sprite.scale.set(scale * 2.8, scale * 0.7, 1)
    return sprite
}

const rebuildAnnotations = () => {
    if (!annotationsGroup) return
    while (annotationsGroup.children.length) {
        const child = annotationsGroup.children.pop()
        child.material?.map?.dispose()
        child.material?.dispose()
    }
    props.parts
        .filter(part => part.annotationPosition && Object.keys(part.annotationPosition).length)
        .forEach(part => annotationsGroup.add(labelSprite(part)))
}

const selectPart = part => {
    selectedPartId.value = part.id
    if (selectionHelper) scene?.remove(selectionHelper)
    const object = findObject(part.modelObjectName)
    if (object) {
        selectionHelper = new THREE.BoxHelper(object, 0xf0b86b)
        scene.add(selectionHelper)
        const box = new THREE.Box3().setFromObject(object)
        if (!box.isEmpty()) controls.target.copy(box.getCenter(new THREE.Vector3()))
    } else selectionHelper = null
}

const updateBinding = (part, value) => {
    part.modelObjectName = value || ''
    part.modelLayer = Boolean(value)
    if (part.layerVisible === undefined || part.layerVisible === null) part.layerVisible = true
    applyLayers()
    selectPart(part)
    emit('dirty')
}

const updateVisibility = part => {
    const object = findObject(part.modelObjectName)
    if (object) object.visible = part.layerVisible !== false
    emit('dirty')
}

const clearAnnotation = part => {
    part.annotationPosition = {}
    rebuildAnnotations()
    emit('dirty')
}

const onPointerDown = event => {
    if (!renderer || !camera || !rootObject) return
    const rect = renderer.domElement.getBoundingClientRect()
    pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
    raycaster.setFromCamera(pointer, camera)
    const hits = raycaster.intersectObject(rootObject, true).filter(hit => hit.object.visible)
    if (!hits.length) return
    const hit = hits[0]
    if (annotationMode.value && selectedPart.value && !props.readOnly) {
        selectedPart.value.annotationPosition = {
            x: Number(hit.point.x.toFixed(6)),
            y: Number(hit.point.y.toFixed(6)),
            z: Number(hit.point.z.toFixed(6))
        }
        if (!selectedPart.value.modelObjectName) updateBinding(selectedPart.value, hit.object.name)
        rebuildAnnotations()
        emit('dirty')
        annotationMode.value = false
        ElMessage.success('三维标注位置已记录')
        return
    }
    const part = props.parts.find(item => item.modelObjectName === hit.object.name)
    if (part) selectPart(part)
}

watch(
    () => [props.visible, props.model?.id],
    async ([visible]) => {
        if (!visible) {
            disposeScene()
            return
        }
        await nextTick()
        initScene()
        await loadModel()
    }
)
watch(
    () => props.parts.map(part => [
        part.id, part.modelLayer, part.modelObjectName, part.layerVisible,
        part.annotationText, part.annotationPosition?.x, part.annotationPosition?.y, part.annotationPosition?.z
    ]),
    () => {
        applyLayers()
        rebuildAnnotations()
    },
    { deep: true }
)
onBeforeUnmount(disposeScene)
</script>

<template>
    <el-dialog
        :model-value="visible"
        :title="`三维模型查看器 · ${model?.modelName || ''}`"
        width="min(1280px, 97vw)"
        top="3vh"
        destroy-on-close
        @close="close"
    >
        <div class="viewer-shell">
            <aside>
                <div class="model-meta">
                    <b>{{ model?.fileName }}</b>
                    <span>{{ modelFormat.toUpperCase() }} · {{ model?.scaleUnit || '未填写比例单位' }}</span>
                    <small>拖动旋转 · 滚轮缩放 · 右键平移 · 点击模型选择已绑定部件</small>
                </div>
                <div class="layer-heading">
                    <b>模型图层与部件</b>
                    <el-tag size="small">{{ objectNames.length }} 个模型节点</el-tag>
                </div>
                <div v-if="parts.length" class="layer-list">
                    <article
                        v-for="part in parts"
                        :key="part.id"
                        :class="{ active: selectedPart?.id === part.id }"
                        @click="selectPart(part)"
                    >
                        <div><b>{{ part.partName }}</b><small>{{ part.modelObjectName || '尚未绑定模型节点' }}</small></div>
                        <el-switch
                            v-model="part.layerVisible"
                            :disabled="readOnly || !part.modelObjectName"
                            @click.stop
                            @change="updateVisibility(part)"
                        />
                        <el-select
                            :model-value="part.modelObjectName"
                            clearable
                            filterable
                            :disabled="readOnly"
                            placeholder="绑定模型节点"
                            @click.stop
                            @change="value => updateBinding(part, value)"
                        >
                            <el-option v-for="name in objectNames" :key="name" :label="name" :value="name" />
                        </el-select>
                    </article>
                </div>
                <el-empty v-else description="请先建立复原组成部分" :image-size="55" />
                <div v-if="selectedPart" class="annotation-panel">
                    <b>三维标注 · {{ selectedPart.partName }}</b>
                    <p>{{ selectedPart.annotationText || '可先在组成部分中填写标注说明' }}</p>
                    <small v-if="selectedPart.annotationPosition?.x !== undefined">
                        X {{ selectedPart.annotationPosition.x }} · Y {{ selectedPart.annotationPosition.y }} · Z {{ selectedPart.annotationPosition.z }}
                    </small>
                    <div>
                        <el-button
                            :type="annotationMode ? 'warning' : 'primary'"
                            :disabled="readOnly"
                            @click="annotationMode = !annotationMode"
                        >
                            {{ annotationMode ? '请点击模型表面' : '在模型上放置标注' }}
                        </el-button>
                        <el-button
                            :disabled="readOnly || selectedPart.annotationPosition?.x === undefined"
                            @click="clearAnnotation(selectedPart)"
                        >清除</el-button>
                    </div>
                </div>
            </aside>
            <main ref="host" v-loading="loading" class="webgl-host">
                <el-result v-if="error" icon="error" title="模型无法渲染" :sub-title="error" />
                <div v-if="annotationMode" class="annotation-tip">标注模式：点击模型表面确定三维坐标</div>
            </main>
        </div>
        <template #footer><el-button @click="close">关闭查看器</el-button></template>
    </el-dialog>
</template>

<style scoped>
.viewer-shell{display:grid;grid-template-columns:330px minmax(0,1fr);height:min(720px,78vh);overflow:hidden;border:1px solid #dce4e0;border-radius:8px}
.viewer-shell>aside{display:flex;flex-direction:column;min-height:0;padding:15px;background:#f5f8f6;border-right:1px solid #dce4e0}
.model-meta{display:flex;flex-direction:column;gap:5px;padding-bottom:12px;border-bottom:1px solid #dce4e0}.model-meta span,.model-meta small{color:#7f8d87;font-size:10px;line-height:1.5}
.layer-heading{display:flex;justify-content:space-between;align-items:center;margin:14px 0 8px}.layer-list{min-height:0;overflow-y:auto}
.layer-list article{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:8px;margin-bottom:7px;padding:10px;background:#fff;border:1px solid #dce4e0;border-radius:6px;cursor:pointer}.layer-list article.active{border-color:#4d806e;box-shadow:inset 3px 0 #4d806e}.layer-list article>div{min-width:0}.layer-list b,.layer-list small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.layer-list small{margin-top:3px;color:#8c9893;font-size:9px}.layer-list :deep(.el-select){grid-column:1/-1;width:100%}
.annotation-panel{margin-top:auto;padding-top:13px;border-top:1px solid #dce4e0}.annotation-panel p,.annotation-panel small{display:block;color:#74817c;font-size:10px;line-height:1.6}.annotation-panel>div{display:flex;margin-top:9px}
.webgl-host{min-width:0;position:relative;overflow:hidden;background:#18201d}.webgl-host :deep(canvas){display:block;width:100%;height:100%}.webgl-host :deep(.el-loading-mask){background:rgba(24,32,29,.78)}
.webgl-host :deep(.el-result){position:absolute;inset:0;z-index:2;background:#fff}.annotation-tip{position:absolute;top:14px;left:50%;z-index:3;transform:translateX(-50%);padding:8px 14px;color:#fff;background:#b57a2e;border-radius:20px;font-size:11px;pointer-events:none}
@media(max-width:800px){.viewer-shell{grid-template-columns:1fr;height:auto}.viewer-shell>aside{max-height:330px}.webgl-host{height:480px}}
</style>
