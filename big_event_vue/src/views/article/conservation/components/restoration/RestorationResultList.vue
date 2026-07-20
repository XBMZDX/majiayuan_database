<script setup>
import { computed, ref } from 'vue'
import { Delete, Plus, Search } from '@element-plus/icons-vue'
const props = defineProps({ results: { type: Array, default: () => [] }, activeId: { type: [Number,String], default: null } })
defineEmits(['select','create','delete'])
const keyword=ref(''), type=ref(''), category=ref(''), status=ref(''), evidence=ref('')
const typeMap={physical:'实体复原',digital_2d:'二维数字',digital_3d:'三维数字',hybrid:'实体+数字',conceptual:'学术推测'}
const categoryMap={structure:'结构复原',shape:'形态复原',assembly:'装配复原',missing_part:'缺失复原',pattern:'纹饰复原',color:'色彩复原',scene:'场景复原',other:'其他'}
const statusMap={draft:['记录中','info'],in_progress:['持续更新','primary'],completed:['资料完整','success']}
const confidenceMap={high:'高',medium:'中',low:'低',undetermined:'未确定'}
const primary=result=>result.media.find(item=>item.isPrimary)||result.media[0]
const filtered=computed(()=>props.results.filter(item=>(!keyword.value||`${item.resultName}${item.resultCode}${item.targetPart}`.includes(keyword.value))&&(!type.value||item.restorationType===type.value)&&(!category.value||item.restorationCategory===category.value)&&(!status.value||item.resultStatus===status.value)&&(!evidence.value||item.evidenceLevel===evidence.value)))
</script>
<template>
    <aside class="result-list">
        <div class="head"><div><b>复原成果</b><span>{{ filtered.length }} / {{ results.length }}</span></div><el-button link type="primary" :icon="Plus" @click="$emit('create')">新建</el-button></div>
        <div class="filters"><el-input v-model="keyword" clearable :prefix-icon="Search" placeholder="搜索成果、编号或部位" /><div>
            <el-select v-model="type" clearable placeholder="复原类型"><el-option v-for="(label,key) in typeMap" :key="key" :label="label" :value="key" /></el-select>
            <el-select v-model="category" clearable placeholder="复原类别"><el-option v-for="(label,key) in categoryMap" :key="key" :label="label" :value="key" /></el-select>
            <el-select v-model="status" clearable placeholder="状态"><el-option v-for="(item,key) in statusMap" :key="key" :label="item[0]" :value="key" /></el-select>
            <el-select v-model="evidence" clearable placeholder="证据等级"><el-option v-for="item in ['A','B','C','D']" :key="item" :label="`等级${item}`" :value="item" /></el-select>
        </div></div>
        <div class="cards">
            <article v-for="item in filtered" :key="item.id" :class="{active:item.id===activeId}" @click="$emit('select',item.id)">
                <div class="preview"><img v-if="primary(item)" :src="primary(item).thumbnailUrl||primary(item).fileUrl" :alt="item.resultName"><span v-else>暂无主预览图</span><div><el-tag size="small" effect="dark">{{ typeMap[item.restorationType] }}</el-tag><el-tag size="small" type="warning" effect="dark">{{ categoryMap[item.restorationCategory] }}</el-tag></div></div>
                <div class="title"><h3>{{ item.resultName||'未命名成果' }}</h3><el-tag :type="statusMap[item.resultStatus]?.[1]" size="small">{{ statusMap[item.resultStatus]?.[0] }}</el-tag></div>
                <p>{{ item.resultCode }} · {{ item.targetPart||'未填写部位' }}</p>
                <dl><div><dt>证据等级</dt><dd>{{ item.evidenceLevel||'-' }}</dd></div><div><dt>可信度</dt><dd>{{ confidenceMap[item.confidenceLevel] }}</dd></div><div><dt>完整度</dt><dd>{{ item.completionRate||0 }}%</dd></div></dl>
                <el-progress :percentage="item.completionRate||0" :stroke-width="6" :show-text="false" />
                <div class="flags"><el-tag v-if="item.selectedForArchive" size="small" type="success">正式档案</el-tag><el-button link type="danger" :icon="Delete" @click.stop="$emit('delete',item)" /></div>
            </article>
            <el-empty v-if="!filtered.length" description="暂无符合条件的成果" :image-size="60" />
        </div>
    </aside>
</template>
<style scoped>
.result-list{height:100%;display:flex;flex-direction:column;overflow:hidden;background:#f6f8f7;border-right:1px solid #dfe4e1}.head{display:flex;justify-content:space-between;align-items:center;padding:15px 13px 9px}.head b{color:#30443c;font-size:15px}.head span{margin-left:7px;color:#929b98;font-size:10px}.filters{padding:0 10px 10px;border-bottom:1px solid #e1e5e3}.filters>div{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:7px}.cards{flex:1;overflow-y:auto;padding:9px}.cards article{margin-bottom:10px;padding:10px;background:#fff;border:1px solid #e0e5e2;border-radius:8px;cursor:pointer;transition:.2s}.cards article:hover{border-color:#85aa9d}.cards article.active{border-color:#3e7563;box-shadow:inset 3px 0 #3e7563,0 5px 14px rgb(45 87 73 / 10%)}.preview{height:108px;display:grid;place-items:center;overflow:hidden;position:relative;background:#e9edeb;border-radius:5px;color:#99a29f;font-size:10px}.preview img{width:100%;height:100%;object-fit:cover}.preview>div{position:absolute;left:6px;bottom:6px;display:flex;gap:4px}.title{display:flex;justify-content:space-between;gap:7px;align-items:flex-start;margin-top:9px}.title h3{margin:0;color:#34473f;font-size:13px;line-height:1.45}.cards article>p{margin:4px 0 8px;color:#9a7b5a;font-size:9px}dl{display:grid;grid-template-columns:1fr 1fr 1fr;margin:0 0 7px}dt{color:#9aa29f;font-size:9px}dd{margin:2px 0 0;color:#5e6a65;font-size:10px}.flags{min-height:24px;display:flex;align-items:center;gap:4px;margin-top:6px}.flags :deep(.el-button){margin-left:auto}@media(max-width:920px){.result-list{width:315px}}
</style>
