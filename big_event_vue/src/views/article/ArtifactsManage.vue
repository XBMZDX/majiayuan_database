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
//引入xlsx库用于解析Excel文件
import * as XLSX from 'xlsx'

//文物数据模型
const artifacts = ref([])

//分页条数据模型
const pageNum = ref(1)//当前页
const total = ref(20)//总条数
const pageSize = ref(3)//每页条数

//当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
    pageSize.value = size
    //刷新列表
    artifactsList();
}
//当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
    pageNum.value = num
    //刷新列表
    artifactsList();
}

import { artifactsListService, artifactsAddService, artifactsDeleteService, artifactsUpdateService, artifactsBatchImportService } from '@/api/Artifacts.js'
//声明一个函数
const artifactsList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
    }
    //发送请求获取文物列表
    let result = await artifactsListService(params);
    artifacts.value = result.data.items;
    total.value = result.data.total;
}
artifactsList();
import { Plus } from '@element-plus/icons-vue'
//控制抽屉是否显示
const visibleDrawer = ref(false)

//添加表单数据模型
const ArtifactsModel = ref({
    artifactCode: '',
    name: '',
    category: '',
    era: '',
    material: '',
    size: '',
    weight: '',
    discoverySite: '',
    discoveryYear: '',
    preservationStatus: '',
    description: '',
    coverImage: ''
})

//导入token
import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore()

//控制文物详情抽屉是否显示
const visibleDetailDrawer = ref(false)
const detailData = ref({})
const detailFields = [
    { label: '文物编号', prop: 'artifactCode' },
    { label: '文物名称', prop: 'name' },
    { label: '文物类别', prop: 'category' },
    { label: '年代', prop: 'era' },
    { label: '材质', prop: 'material' },
    { label: '尺寸', prop: 'size' },
    { label: '重量', prop: 'weight' },
    { label: '发现地点', prop: 'discoverySite' },
    { label: '发现年份', prop: 'discoveryYear' },
    { label: '保存状态', prop: 'preservationStatus' },
    { label: '文物描述', prop: 'description', span: 2 },
    { label: '创建时间', prop: 'createTime' },
    { label: '更新时间', prop: 'updateTime' }
]
const openDetail = (row) => {
    detailData.value = { ...row }
    visibleDetailDrawer.value = true
}

//上传成功的回调函数
const uploadSuccess = (result) => {
    ArtifactsModel.value.coverImage = result.data;
    console.log(result.data);
}

//重置表单数据
const resetForm = () => {
    ArtifactsModel.value = {
        artifactCode: '',
        name: '',
        category: '',
        era: '',
        material: '',
        size: '',
        weight: '',
        discoverySite: '',
        discoveryYear: '',
        preservationStatus: '',
        description: '',
        coverImage: ''
    };
}

//添加文物
const artifactsAdd = async () => {
    let result = await artifactsAddService(ArtifactsModel.value)
    ElMessage.success(result.message ? result.message : '添加成功')

    //重置表单
    resetForm();
    //关闭抽屉
    visibleDrawer.value = false;
    //刷新列表
    artifactsList();
}
import { ElMessage, ElMessageBox } from 'element-plus'

//删除文物
const deleteArtifact = (row) => {
    //提示用户--确认框
    ElMessageBox.confirm(
        '你确认删除该文物吗？',
        '温馨提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            //调用接口
            console.log("删除的文物 id =", row.id);
            let result = await artifactsDeleteService(row.id);
            ElMessage({
                type: 'success',
                message: '成功删除',
            })
            //刷新列表
            //获取所有文物函数
            artifactsList();
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

//更新文物
const updateArtifact = async () => {
    //调用接口
    let result = await artifactsUpdateService(editData.value)
    ElMessage.success(result.message ? result.message : '编辑成功')
    //获取所有文物函数
    artifactsList();
  visibleEditDrawer.value = false
}

// 控制导入抽屉显示
const visibleImportDrawer = ref(false)
// 导入文件状态
const importFile = ref(null)
// 导入结果
const importResult = ref({})
// 导入状态
const importing = ref(false)

// 文件选择处理
const handleFileChange = (file) => {
    importFile.value = file.raw
    // 重置导入结果
    importResult.value = {}
}

// 开始导入
const handleImport = async () => {
    if (!importFile.value) {
        ElMessage.warning('请选择Excel文件')
        return
    }
    
    importing.value = true
    importResult.value = {}
    
    try {
        // 解析Excel文件
        const fileReader = new FileReader()
        fileReader.onload = async (e) => {
            try {
                const data = new Uint8Array(e.target.result)
                const workbook = XLSX.read(data, { type: 'array' })
                
                // 假设第一个工作表是数据工作表
                const firstSheetName = workbook.SheetNames[0]
                const worksheet = workbook.Sheets[firstSheetName]
                
                // 转换为JSON格式
                const jsonData = XLSX.utils.sheet_to_json(worksheet)
                
                if (jsonData.length === 0) {
                    importing.value = false
                    importResult.value = {
                        success: false,
                        message: 'Excel文件中没有数据'
                    }
                    ElMessage.warning('Excel文件中没有数据')
                    return
                }
                
                // 数据映射和验证
                const importData = jsonData.map(item => {
                    return {
                        artifactCode: item['文物编号'] || item['artifactCode'] || '',
                        siteId: item['遗址id'] || item['siteId'] || '',
                        siteName: item['所属遗址'] || item['siteName'] || '',
                        name: item['文物名称'] || item['name'] || '',
                        category: item['文物类别'] || item['category'] || '',
                        era: item['年代'] || item['era'] || '',
                        material: item['材质'] || item['material'] || '',
                        size: item['尺寸'] || item['size'] || '',
                        weight: item['重量'] || item['weight'] || '',
                        discoverySite: item['发现地点'] || item['discoverySite'] || '',
                        discoveryYear: item['发现年份'] || item['discoveryYear'] || '',
                        preservationStatus: item['保存状态'] || item['preservationStatus'] || '',
                        description: item['文物描述'] || item['description'] || ''
                    }
                })
                
                // 过滤掉必填字段为空的数据
                const validData = importData.filter(item => {
                    return item.artifactCode && item.name
                })
                
                if (validData.length === 0) {
                    importing.value = false
                    importResult.value = {
                        success: false,
                        message: '没有有效的数据可以导入，请检查Excel文件中的必填字段'
                    }
                    ElMessage.warning('没有有效的数据可以导入，请检查Excel文件中的必填字段')
                    return
                }
                
                // 调用批量导入API
                const result = await artifactsBatchImportService(validData)
                
                importing.value = false
                importResult.value = {
                    success: true,
                    message: `导入成功，共导入${validData.length}条数据`
                }
                ElMessage.success(result.message || '导入成功')
                // 刷新列表
                artifactsList()
                
            } catch (error) {
                importing.value = false
                importResult.value = {
                    success: false,
                    message: 'Excel文件解析失败：' + error.message
                }
                ElMessage.error('Excel文件解析失败')
            }
        }
        
        fileReader.onerror = () => {
            importing.value = false
            importResult.value = {
                success: false,
                message: '文件读取失败'
            }
            ElMessage.error('文件读取失败')
        }
        
        fileReader.readAsArrayBuffer(importFile.value)
        
    } catch (error) {
        importing.value = false
        importResult.value = {
            success: false,
            message: '导入失败：' + error.message
        }
        ElMessage.error('导入失败')
    }
}
</script>
<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>文物管理</span>
                <div class="extra">
                    <el-button type="primary" @click="visibleDrawer = true">添加文物</el-button>
                    <el-button type="success" @click="visibleImportDrawer = true">导入文物</el-button>
                </div>
            </div>
        </template>
        
        <!-- 文物列表 -->
        <el-table :data="artifacts" style="width: 100%">
            
 
             <el-table-column label="墓号" prop="artifactCode"></el-table-column>
            <el-table-column label="编号" prop="name"></el-table-column>
            <el-table-column label="名称" prop="artifactCode"></el-table-column>
            <el-table-column label="材质" prop="category"> </el-table-column>
            <el-table-column label="颜色" prop="era"> </el-table-column>
            <el-table-column label="数量" prop="material"> </el-table-column>
            
            <el-table-column label="类型" prop="size"> </el-table-column>
            <el-table-column label="短径" prop="material"> </el-table-column>
            <el-table-column label="长径" prop="material"> </el-table-column>
            <el-table-column label="孔径" prop="material"> </el-table-column>
        
            <el-table-column label="" width="200">
                <template #default="{ row }">
                    <el-button :icon="Check" circle plain type="success" @click="openDetail(row)"></el-button>
                    <el-button :icon="Edit" circle plain type="primary" @click="openEdit(row)"></el-button>
                    <el-button :icon="Delete" circle plain type="danger" @click="deleteArtifact(row)"></el-button>
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
    
    <!-- 添加文物抽屉 -->
    <el-drawer v-model="visibleDrawer" title="添加文物" direction="rtl" size="50%">
        <!-- 添加文物表单 -->
        <el-form :model="ArtifactsModel" label-width="100px">
            <!--文物信息 -->
            <el-form-item label="文物编号" prop="artifactCode">
                <el-input v-model="ArtifactsModel.artifactCode" placeholder="请输入文物编号"></el-input>
            </el-form-item>
            <el-form-item label="遗址id" prop="siteId">
                <el-input v-model="ArtifactsModel.siteId" placeholder="请输入所属遗址id"></el-input>
            </el-form-item>

            <el-form-item label="所属遗址" prop="siteName">
                <el-input v-model="ArtifactsModel.siteName" placeholder="请输入所属遗址"></el-input>
            </el-form-item>

            <el-form-item label="文物名称" prop="name">
                <el-input v-model="ArtifactsModel.name" placeholder="请输入文物名称"></el-input>
            </el-form-item>

            <el-form-item label="文物类别" prop="category">
                <el-input v-model="ArtifactsModel.category" placeholder="请输入文物类别"></el-input>
            </el-form-item>

            <el-form-item label="年代" prop="era">
                <el-input v-model="ArtifactsModel.era" placeholder="请输入年代"></el-input>
            </el-form-item>

            <el-form-item label="材质" prop="material">
                <el-input v-model="ArtifactsModel.material" placeholder="请输入材质"></el-input>
            </el-form-item>

            <!-- 非必填项 -->
            <el-form-item label="尺寸">
                <el-input v-model="ArtifactsModel.size" placeholder="请输入尺寸"></el-input>
            </el-form-item>

            <el-form-item label="重量">
                <el-input v-model="ArtifactsModel.weight" placeholder="请输入重量"></el-input>
            </el-form-item>

            <el-form-item label="发现地点">
                <el-input v-model="ArtifactsModel.discoverySite" placeholder="请输入发现地点"></el-input>
            </el-form-item>

            <el-form-item label="发现年份">
                <el-input v-model="ArtifactsModel.discoveryYear" placeholder="请输入发现年份"></el-input>
            </el-form-item>

            <el-form-item label="保存状态">
                <el-input v-model="ArtifactsModel.preservationStatus" placeholder="请输入保存状态"></el-input>
            </el-form-item>

            <el-form-item label="文物描述">
                <div class="editor">
                    <quill-editor theme="snow" v-model:content="ArtifactsModel.description" contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>
            
            <!-- 图片上传 -->
            <el-form-item label="文物图片">
                <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                    name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="uploadSuccess">
                    <img v-if="ArtifactsModel.coverImage" :src="ArtifactsModel.coverImage" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="artifactsAdd">发布</el-button>
                <el-button type="info" @click="visibleDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>

    <!-- 文物编辑抽屉 -->
    <el-drawer v-model="visibleEditDrawer" title="编辑文物" direction="rtl" size="50%">
        <!-- 文物编辑表单 -->
        <el-form :model="editData" label-width="100px">
            <!--文物信息 -->
            <el-form-item label="文物编号" prop="artifactCode">
                <el-input v-model="editData.artifactCode" placeholder="请输入文物编号"></el-input>
            </el-form-item>

            <el-form-item label="文物名称" prop="name">
                <el-input v-model="editData.name" placeholder="请输入文物名称"></el-input>
            </el-form-item>

            <el-form-item label="文物类别" prop="category">
                <el-input v-model="editData.category" placeholder="请输入文物类别"></el-input>
            </el-form-item>

            <el-form-item label="年代" prop="era">
                <el-input v-model="editData.era" placeholder="请输入年代"></el-input>
            </el-form-item>

            <el-form-item label="材质" prop="material">
                <el-input v-model="editData.material" placeholder="请输入材质"></el-input>
            </el-form-item>

            <!-- 非必填项 -->
            <el-form-item label="尺寸">
                <el-input v-model="editData.size" placeholder="请输入尺寸"></el-input>
            </el-form-item>

            <el-form-item label="重量">
                <el-input v-model="editData.weight" placeholder="请输入重量"></el-input>
            </el-form-item>

            <el-form-item label="发现地点">
                <el-input v-model="editData.discoverySite" placeholder="请输入发现地点"></el-input>
            </el-form-item>

            <el-form-item label="发现年份">
                <el-input v-model="editData.discoveryYear" placeholder="请输入发现年份"></el-input>
            </el-form-item>

            <el-form-item label="保存状态">
                <el-input v-model="editData.preservationStatus" placeholder="请输入保存状态"></el-input>
            </el-form-item>

            <el-form-item label="文物描述">
                <div class="editor">
                    <quill-editor :key="editData.id || 'new'"  theme="snow" v-model:content="editData.description" contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>
            
            <!-- 图片上传 -->
            <el-form-item label="文物图片">
                <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                    name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="uploadSuccess">
                    <img v-if="editData.coverImage" :src="editData.coverImage" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

        </el-form>
        <el-form-item>
            <el-button type="primary" @click="updateArtifact">更新</el-button>
            <el-button type="info" @click="visibleEditDrawer = false">取消</el-button>
        </el-form-item>
    </el-drawer>

    <!-- 文物详情抽屉 -->
    <el-drawer v-model="visibleDetailDrawer" title="文物详情" direction="rtl" size="50%">
        <!-- 文物详情内容 -->
        <el-descriptions :column="2" border>
            <el-descriptions-item v-for="item in detailFields" :key="item.prop" :label="item.label"
                :span="item.span || 1">
                {{ detailData[item.prop] || '—' }}
            </el-descriptions-item>
        </el-descriptions>
    </el-drawer>

    <!-- 导入文物抽屉 -->
    <el-drawer v-model="visibleImportDrawer" title="导入文物" direction="rtl" size="50%">
        <el-form label-width="100px">
            <el-form-item label="Excel文件">
                <el-upload
                    class="upload-demo"
                    action="#"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    :show-file-list="true"
                    accept=".xlsx,.xls"
                    :limit="1"
                >
                    <el-button type="primary">选择文件</el-button>
                    <template #tip>
                        <div class="el-upload__tip">
                            请上传Excel文件（.xlsx或.xls格式），文件大小不超过5MB
                        </div>
                    </template>
                </el-upload>
            </el-form-item>
            <el-form-item v-if="importResult.success !== undefined">
                <el-alert
                    :title="importResult.success ? '导入成功' : '导入失败'"
                    :type="importResult.success ? 'success' : 'error'"
                    :description="importResult.message"
                    show-icon
                    :closable="true"
                />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleImport" :loading="importing">开始导入</el-button>
                <el-button @click="visibleImportDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
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

.avatar-uploader {
    :deep() {
        .avatar {
            width: 178px;
            height: 178px;
            display: block;
        }

        .el-upload {
            border: 1px dashed var(--el-border-color);
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: var(--el-transition-duration-fast);
        }

        .el-upload:hover {
            border-color: var(--el-color-primary);
        }

        .el-icon.avatar-uploader-icon {
            font-size: 28px;
            color: #8c939d;
            width: 178px;
            height: 178px;
            text-align: center;
        }
    }
}

.editor {
    width: 100%;

    :deep(.ql-editor) {
        min-height: 200px;
    }
}
</style>