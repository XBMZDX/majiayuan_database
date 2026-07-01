<script setup>
import {
    Edit,
    Check,
    Delete
} from '@element-plus/icons-vue'

import { ref } from 'vue'
//引入富文本编辑器
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

//文物检测数据模型
const detections = ref([])

//分页条数据模型
const pageNum = ref(1)//当前页
const total = ref(20)//总条数
const pageSize = ref(3)//每页条数

//当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
    pageSize.value = size
    //刷新列表
    detectionList();
}
//当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
    pageNum.value = num
    //刷新列表
    detectionList();
}

//文物检测数据展示
import { detectionListService, detectionAddService, detectionDeleteService, detectionUpdateService } from '@/api/ArtifactDetection.js'
//声明一个函数
const detectionList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
    }
    //发送请求获取文物检测列表
    let result = await detectionListService(params);
    detections.value = result.data.items;
    total.value = result.data.total;
}
detectionList();

import { Plus } from '@element-plus/icons-vue'
//控制抽屉是否显示
const visibleDrawer = ref(false)

//添加表单数据模型
const DetectionModel = ref({
    artifactId: '',
    artifactName: '',
    detectionType: '',
    instrumentName: '',
    detectionDate: '',
    detectionParameters: '',
    detectionResults: '',
    analyst: '',
    remarks: ''
})

//控制文物检测详情抽屉是否显示
const visibleDetailDrawer = ref(false)
const detailData = ref({})
const detailFields = [
    { label: '文物编号', prop: 'artifactId' },
    { label: '文物名称', prop: 'artifactName' },
    { label: '检测类型', prop: 'detectionType' },
    { label: '仪器名称', prop: 'instrumentName' },
    { label: '检测日期', prop: 'detectionDate' },
    { label: '检测参数', prop: 'detectionParameters' },
    { label: '检测结果', prop: 'detectionResults', span: 2 },
    { label: '分析人员', prop: 'analyst' },
    { label: '备注', prop: 'remarks', span: 2 },
    { label: '创建时间', prop: 'createTime' },
    { label: '更新时间', prop: 'updateTime' }
]
const openDetail = (row) => {
    detailData.value = { ...row }
    visibleDetailDrawer.value = true
}

//导入token
import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore()

//重置表单数据
const resetForm = () => {
    DetectionModel.value = {
        artifactId: '',
        artifactName: '',
        detectionType: '',
        instrumentName: '',
        detectionDate: '',
        detectionParameters: '',
        detectionResults: '',
        analyst: '',
        remarks: ''
    };
}

//添加文物检测
const detectionAdd = async () => {
    let result = await detectionAddService(DetectionModel.value)
    ElMessage.success(result.message ? result.message : '添加成功')

    //重置表单
    resetForm();
    //关闭抽屉
    visibleDrawer.value = false;
    //刷新列表
    detectionList();
}
import { ElMessage, ElMessageBox } from 'element-plus'

//删除文物检测
const deleteDetection = (row) => {
    //提示用户--确认框
    ElMessageBox.confirm(
        '你确认删除该检测记录吗？',
        '温馨提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            //调用接口
            console.log("删除的检测记录 id =")
            let result = await detectionDeleteService(row.id);
            ElMessage({
                type: 'success',
                message: '成功删除',
            })
            //刷新列表
            detectionList();
        })
        .catch(() => {
            ElMessage({
                type: 'info',
                message: '已取消',
            })
        })
}

// 控制编辑抽屉显示
const visibleEditDrawer = ref(false)

// 编辑表单数据
const editData = ref({})
const openEdit = (row) => {
    editData.value = { ...row }  // 拷贝当前行数据
    visibleEditDrawer.value = true
}

//更新文物检测
const updateDetection = async () => {
    //调用接口
    let result = await detectionUpdateService(editData.value)
    ElMessage.success(result.message ? result.message : '编辑成功')
    //获取所有文物检测函数
    detectionList();
  visibleEditDrawer.value = false
}
</script>
<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>文物检测信息管理</span>
                <div class="extra">
                    <el-button type="primary" @click="visibleDrawer = true">添加检测记录</el-button>
                </div>
            </div>
        </template>
        
        <!-- 文物检测列表 -->
        <el-table :data="detections" style="width: 100%">
            <el-table-column label="序号" width="100" type="index"> </el-table-column>
            <el-table-column label="出土遗址" prop="instrumentName"> </el-table-column>
            <el-table-column label="样品类型" prop="artifactId"></el-table-column>
            <el-table-column label="样品数量" prop="artifactName"></el-table-column>
            <el-table-column label="检测技术" prop="detectionType"> </el-table-column>
            <el-table-column label="数据类型" prop="instrumentName"> </el-table-column>
            <el-table-column label="数据熟练" prop="instrumentName"> </el-table-column>
            <el-table-column label="检测日期" prop="detectionDate"> </el-table-column>
            <el-table-column label="" prop="analyst"> </el-table-column>
            <el-table-column label="" width="200">
                <template #default="{ row }">
                    <el-button :icon="Check" circle plain type="success" @click="openDetail(row)"></el-button>
                    <el-button :icon="Edit" circle plain type="primary" @click="openEdit(row)"></el-button>
                    <el-button :icon="Delete" circle plain type="danger" @click="deleteDetection(row)"></el-button>
                </template>
            </el-table-column>
            <template #empty>
                <el-empty description="没有数据" />
            </template>
        </el-table>
        
        <!-- 分页条 -->
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[3, 5, 10, 15]"
            layout="jumper, total, sizes, prev, pager, next" background :total="total" @size-change="onSizeChange"
            @current-change="onCurrentChange" style="margin-top: 20px; justify-content: flex-end" />
    </el-card>
    
    <!-- 添加文物检测抽屉 -->
    <el-drawer v-model="visibleDrawer" title="添加检测记录" direction="rtl" size="50%">
        <!-- 添加文物检测表单 -->
        <el-form :model="DetectionModel" label-width="100px">
            <!--检测信息 -->
            <el-form-item label="文物编号" prop="artifactId">
                <el-input v-model="DetectionModel.artifactId" placeholder="请输入文物编号"></el-input>
            </el-form-item>

            <el-form-item label="文物名称" prop="artifactName">
                <el-input v-model="DetectionModel.artifactName" placeholder="请输入文物名称"></el-input>
            </el-form-item>

            <el-form-item label="检测类型" prop="detectionType">
                <el-input v-model="DetectionModel.detectionType" placeholder="请输入检测类型"></el-input>
            </el-form-item>

            <el-form-item label="仪器名称" prop="instrumentName">
                <el-input v-model="DetectionModel.instrumentName" placeholder="请输入仪器名称"></el-input>
            </el-form-item>

            <el-form-item label="检测日期" prop="detectionDate">
                <el-date-picker v-model="DetectionModel.detectionDate" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
            </el-form-item>

            <el-form-item label="检测参数" prop="detectionParameters">
                <el-input v-model="DetectionModel.detectionParameters" placeholder="请输入检测参数"></el-input>
            </el-form-item>

            <el-form-item label="检测结果" prop="detectionResults">
                <div class="editor">
                    <quill-editor theme="snow" v-model:content="DetectionModel.detectionResults" contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>

            <el-form-item label="分析人员" prop="analyst">
                <el-input v-model="DetectionModel.analyst" placeholder="请输入分析人员"></el-input>
            </el-form-item>

            <el-form-item label="备注" prop="remarks">
                <el-input v-model="DetectionModel.remarks" type="textarea" placeholder="请输入备注"></el-input>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="detectionAdd">保存</el-button>
                <el-button type="info" @click="visibleDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>

    <!-- 文物检测编辑抽屉 -->
    <el-drawer v-model="visibleEditDrawer" title="编辑检测记录" direction="rtl" size="50%">
        <!-- 文物检测编辑表单 -->
        <el-form :model="editData" label-width="100px">
            <!--检测信息 -->
            <el-form-item label="文物编号" prop="artifactId">
                <el-input v-model="editData.artifactId" placeholder="请输入文物编号"></el-input>
            </el-form-item>

            <el-form-item label="文物名称" prop="artifactName">
                <el-input v-model="editData.artifactName" placeholder="请输入文物名称"></el-input>
            </el-form-item>

            <el-form-item label="检测类型" prop="detectionType">
                <el-input v-model="editData.detectionType" placeholder="请输入检测类型"></el-input>
            </el-form-item>

            <el-form-item label="仪器名称" prop="instrumentName">
                <el-input v-model="editData.instrumentName" placeholder="请输入仪器名称"></el-input>
            </el-form-item>

            <el-form-item label="检测日期" prop="detectionDate">
                <el-date-picker v-model="editData.detectionDate" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
            </el-form-item>

            <el-form-item label="检测参数" prop="detectionParameters">
                <el-input v-model="editData.detectionParameters" placeholder="请输入检测参数"></el-input>
            </el-form-item>

            <el-form-item label="检测结果" prop="detectionResults">
                <div class="editor">
                    <quill-editor :key="editData.id || 'new'" theme="snow" v-model:content="editData.detectionResults" contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>

            <el-form-item label="分析人员" prop="analyst">
                <el-input v-model="editData.analyst" placeholder="请输入分析人员"></el-input>
            </el-form-item>

            <el-form-item label="备注" prop="remarks">
                <el-input v-model="editData.remarks" type="textarea" placeholder="请输入备注"></el-input>
            </el-form-item>

        </el-form>
        <el-form-item>
            <el-button type="primary" @click="updateDetection">更新</el-button>
            <el-button type="info" @click="visibleEditDrawer = false">取消</el-button>
        </el-form-item>
    </el-drawer>

    <!-- 文物检测详情抽屉 -->
    <el-drawer v-model="visibleDetailDrawer" title="检测记录详情" direction="rtl" size="50%">
        <!-- 文物检测详情内容 -->
        <el-descriptions :column="2" border>
            <el-descriptions-item v-for="item in detailFields" :key="item.prop" :label="item.label"
                :span="item.span || 1">
                {{ detailData[item.prop] || '—' }}
            </el-descriptions-item>
        </el-descriptions>
    </el-drawer>

</template>
<style lang="scss" scoped>
.page-container {
    min-height: 100%;
    box-sizing: border-box;

    .header {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
}

.editor {
    width: 100%;

    :deep(.ql-editor) {
        min-height: 200px;
    }
}
</style>