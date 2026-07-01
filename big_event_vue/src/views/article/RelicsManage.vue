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

//遗址分类数据模型
const categorys = ref([])

//分页条数据模型
const pageNum = ref(1)//当前页
const total = ref(20)//总条数
const pageSize = ref(3)//每页条数

//当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
    pageSize.value = size
    //刷新列表
    relicCategoryList();
}
//当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
    pageNum.value = num
    //刷新列表
    relicCategoryList();
}

//遗迹数据展示
import { relicCategoryListService, relicAddService, relicsBatchImportService, relicDeleteService } from '@/api/Relics.js'
//声明一个函数
const relicCategoryList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
    }
    //发送请求获取分类列表
    let result = await relicCategoryListService(params);
    categorys.value = result.data.items;
    total.value = result.data.total;
}
relicCategoryList();



import { Plus } from '@element-plus/icons-vue'
//控制抽屉是否显示
const visibleDrawer = ref(false)

//添加表单数据模型
const RelicsModel = ref({
    relicCode: '',
    siteName: '',
    name: '',
    type: '',
    positionWithinSite: '',
    excavationArea: '',
    excavationUnit: '',
    era: '',
    stratigraphy: '',
    structureDescription: '',
    dimensions: '',
    orientation: '',
    burialDepth: '',
    preservationStatus: '',
    functionPurpose: '',
    culturalFeatures: '',
    relatedRelics: '',
    images: '',
})



//控制遗址详情抽屉是否显示
const visibleDetailDrawer = ref(false)
const detailData = ref({})
const detailFields = [
    { label: '名称', prop: 'name' },
    { label: '别名', prop: 'alias' },
    { label: '时代', prop: 'era' },
    { label: '省份', prop: 'locationProvince' },
    { label: '城市', prop: 'locationCity' },
    { label: '分类', prop: 'category' },
    { label: '保护等级', prop: 'protectionLevel' },
    { label: '发现年份', prop: 'discoveryYear' },
    { label: '挖掘年份', prop: 'excavationYear' },
    { label: '面积大小', prop: 'areaSize' },
    { label: '纬度', prop: 'latitude' },
    { label: '经度', prop: 'longitude' },
    { label: '详细地址', prop: 'locationDetail', span: 2 },
    { label: '遗址描述', prop: 'description', span: 2 },
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


//上传成功的回调函数
const uploadSuccess = (result) => {
    HeritageSitesModel.value.coverImage = result.data;
    console.log(result.data);
}
//添加遗迹
const relicAdd = async () => {
    let result = await relicAddService(RelicsModel.value)
    ElMessage.success(result.message ? result.message : '添加成功')

    //关闭抽屉
    visibleDrawer.value = false;
    //刷新列表
    relicCategoryList();
}
import { ElMessage, ElMessageBox } from 'element-plus'



//删除遗迹
const deleteRelic = (row) => {
    //提示用户--确认框
    ElMessageBox.confirm(
        '你确认删除该遗迹吗？',
        '温馨提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            //调用接口
            console.log("删除的遗迹 id =")
            let result = await relicDeleteService(row.id);
            ElMessage({
                type: 'success',
                message: '成功删除',
            })
            //刷新列表
            relicCategoryList();
        })
        .catch(() => {
            ElMessage({
                type: 'info',
                message: '已取消',
            })
        })



}

const showDialog = (row) => {
    dialogVisible.value = true; title.value = '编辑遗址'
    //数据拷贝
    HeritageSitesModel.value.siteCode = row.siteCode;
    HeritageSitesModel.value.name = row.name;
    HeritageSitesModel.value.alias = row.alias;
    HeritageSitesModel.value.locationProvince = row.locationProvince;
    HeritageSitesModel.value.locationCity = row.locationCity;
    HeritageSitesModel.value.locationDetail = row.locationDetail;
    HeritageSitesModel.value.latitude = row.latitude;
    HeritageSitesModel.value.longitude = row.longitude;
    HeritageSitesModel.value.era = row.era;
    HeritageSitesModel.value.category = row.category;
    HeritageSitesModel.value.protectionLevel = row.protectionLevel;
    HeritageSitesModel.value.discoveryYear = row.discoveryYear;
    HeritageSitesModel.value.excavationYear = row.excavationYear;
    HeritageSitesModel.value.areaSize = row.areaSize;
    HeritageSitesModel.value.description = row.description;
    HeritageSitesModel.value.id = row.id;
}
const visibleEditDrawer = ref(false)
//更新遗址
const updateSite = async () => {
    //调用接口
    let result = await siteUpdateService(editData.value)
    ElMessage.success(result.message ? result.message : '编辑成功')
    //获取所有遗址函数
    siteCategoryList();
    visibleEditDrawer.value = false
}
// 控制编辑抽屉显示

// 编辑表单数据
const editData = ref({})
const openEdit = (row) => {
    editData.value = { ...row }  // 拷贝当前行数据
    visibleEditDrawer.value = true
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
                        relicCode: item['遗迹编号'] || item['relicCode'] || '',
                        siteId: item['遗址编号'] || item['siteId'] || '',
                        siteName: item['所属遗址'] || item['siteName'] || '',
                        name: item['遗迹名称'] || item['name'] || '',
                        type: item['遗迹类型'] || item['type'] || '',
                        positionWithinSite: item['位置'] || item['positionWithinSite'] || '',
                        excavationArea: item['发掘区域'] || item['excavationArea'] || '',
                        excavationUnit: item['发掘单位'] || item['excavationUnit'] || '',
                        era: item['所属时代'] || item['era'] || '',
                        stratigraphy: item['地层关系'] || item['stratigraphy'] || '',
                        structureDescription: item['结构描述'] || item['structureDescription'] || '',
                        dimensions: item['尺寸'] || item['dimensions'] || '',
                        orientation: item['方向'] || item['orientation'] || '',
                        burialDepth: item['深度'] || item['burialDepth'] || '',
                        preservationStatus: item['保存状况'] || item['preservationStatus'] || '',
                        functionPurpose: item['遗迹描述'] || item['functionPurpose'] || '',
                        culturalFeatures: item['文化特征'] || item['culturalFeatures'] || '',
                        relatedRelics: item['相关遗迹'] || item['relatedRelics'] || ''
                    }
                })
                
                // 过滤掉必填字段为空的数据
                const validData = importData.filter(item => {
                    return item.relicCode && item.name && item.siteId
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
                const result = await relicsBatchImportService(validData)
                
                importing.value = false
                importResult.value = {
                    success: true,
                    message: `导入成功，共导入${validData.length}条数据`
                }
                ElMessage.success(result.message || '导入成功')
                // 刷新列表
                relicCategoryList()
                
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
                <span>遗迹管理</span>
                <div class="extra">
                    <el-button type="primary" @click="visibleDrawer = true">添加遗迹</el-button>
                    <el-button type="success" @click="visibleImportDrawer = true">导入遗迹</el-button>
                </div>
            </div>
        </template>
        <!-- 遗迹列表 -->
        <el-table :data="categorys" style="width: 100%">
            <el-table-column label="序号" width="100" type="index"> </el-table-column>
            <el-table-column label="遗迹编号" prop="relicCode"></el-table-column>
            <el-table-column label="遗迹名称" prop="name"></el-table-column>
            <el-table-column label="所属遗址" prop="siteName"></el-table-column>
            <el-table-column label="遗迹类型" prop="type"> </el-table-column>
            <el-table-column label="所属时代" prop="era"> </el-table-column>
            <el-table-column label="尺寸" prop="dimensions"> </el-table-column>
            <el-table-column label="操作" width="150">
                <template #default="{ row }">
                    <el-button :icon="Check" circle plain type="success" @click="openDetail(row)"></el-button>
                    <el-button :icon="Edit" circle plain type="primary" @click="openEdit(row)"></el-button>
                    <el-button :icon="Delete" circle plain type="danger" @click="deleteRelic(row)"></el-button>
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
    <!-- 添加遗址抽屉 -->
    <el-drawer v-model="visibleDrawer" title="添加遗址" direction="rtl" size="50%">
        <!-- 添加遗址表单 -->
        <el-form :model="RelicsModel" label-width="100px">
            <!--遗址信息 -->
            <el-form-item label="遗迹编号" prop="relicCode">
                <el-input v-model="RelicsModel.relicCode" placeholder="请输入遗迹编号"></el-input>
            </el-form-item>
            <el-form-item label="遗址id" prop="siteId">
                <el-input v-model="RelicsModel.siteId" placeholder="请输入所属遗址id"></el-input>
            </el-form-item>
            <el-form-item label="所属遗址" prop="siteName">
                <el-input v-model="RelicsModel.siteName" placeholder="请输入所属遗址"></el-input>
            </el-form-item>

            <el-form-item label="遗迹名称" prop="name">
                <el-input v-model="RelicsModel.name" placeholder="请输入遗迹名称"></el-input>
            </el-form-item>

            <el-form-item label="遗迹类型" prop="type">
                <el-input v-model="RelicsModel.type" placeholder="请输入遗迹类型"></el-input>
            </el-form-item>

            <el-form-item label="位置" prop="positionWithinSite">
                <el-input v-model="RelicsModel.positionWithinSite" placeholder="请输入位置"></el-input>
            </el-form-item>

            <!-- 非必填项 -->
            <el-form-item label="发掘区域" prop="excavationArea">
                <el-input v-model="RelicsModel.excavationArea" placeholder="请输入发掘区域"></el-input>
            </el-form-item>

            <el-form-item label="发掘单位" prop="excavationUnit">
                <el-input v-model="RelicsModel.excavationUnit" placeholder="请输入发掘单位"></el-input>
            </el-form-item>

            <el-form-item label="所属时代" prop="era">
                <el-input v-model="RelicsModel.era" />
            </el-form-item>

            <el-form-item label="地层关系" prop="stratigraphy">
                <el-input v-model="RelicsModel.stratigraphy" placeholder="请输入遗址类别"></el-input>
            </el-form-item>

            <el-form-item label="结构描述" prop="structureDescription">
                <el-input v-model="RelicsModel.structureDescription" placeholder="请输入结构描述"></el-input>
            </el-form-item>

            <el-form-item label="尺寸" prop="dimensions">
                <el-input v-model="RelicsModel.dimensions" placeholder="请输入尺寸"></el-input>
            </el-form-item>

            <el-form-item label="方向" prop="orientation">
                <el-input v-model="RelicsModel.orientation" placeholder="请输入发掘年份"></el-input>
            </el-form-item>

            <el-form-item label="深度" prop="depth">
                <el-input v-model="RelicsModel.depth" placeholder="请输入深度"></el-input>
            </el-form-item>
            <el-form-item label="保存状况" prop="preservationStatus">
                <el-input v-model="RelicsModel.preservationStatus" placeholder="请输入保存状况"></el-input>
            </el-form-item>
            <el-form-item label="遗迹描述" prop="functionPurpose">
                <div class="editor">
                    <quill-editor theme="snow" v-model:content="RelicsModel.functionPurpose" contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>
            <!--
                auto-upload:设置是否自动上传
                action:设置服务器接口路径
                name:设置上传文件字段名
                headers:设置上传的请求头
                on-success:设置上传成功的回调函数
             -->
            <el-form-item label="遗迹图片">
                <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                    name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="uploadSuccess">
                    <img v-if="RelicsModel.images" :src="RelicsModel.images" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="relicAdd">发布</el-button>
                <el-button type="info" @click="visibleDrawer = false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-drawer>


    <!-- 遗址详情抽屉 -->
    <el-drawer v-model="visibleDetailDrawer" title="遗址详情" direction="rtl" size="50%">
        <!-- 遗址详情内容 -->
        <el-descriptions :column="2" border>
            <el-descriptions-item v-for="item in detailFields" :key="item.prop" :label="item.label"
                :span="item.span || 1">
                {{ detailData[item.prop] || '—' }}
            </el-descriptions-item>
        </el-descriptions>
    </el-drawer>

    <!-- 遗址编辑抽屉 -->
    <el-drawer v-model="visibleEditDrawer" title="编辑遗址" direction="rtl" size="50%">
        <!-- 遗址编辑表单 -->
        <el-form :model="editData" label-width="100px">
            <!--遗址信息 -->
            <el-form-item label="遗址编号" prop="siteCode">
                <el-input v-model="editData.siteCode" placeholder="请输入遗址编号"></el-input>
            </el-form-item>

            <el-form-item label="遗址名称" prop="name">
                <el-input v-model="editData.name" placeholder="请输入遗址名称"></el-input>
            </el-form-item>

            <el-form-item label="所在省份" prop="locationProvince">
                <el-input v-model="editData.locationProvince" placeholder="请输入省份"></el-input>
            </el-form-item>

            <el-form-item label="纬度" prop="latitude">
                <el-input v-model="editData.latitude" placeholder="请输入纬度"></el-input>
            </el-form-item>

            <el-form-item label="经度" prop="longitude">
                <el-input v-model="editData.longitude" placeholder="请输入经度"></el-input>
            </el-form-item>

            <!-- 非必填项 -->
            <el-form-item label="城市">
                <el-input v-model="editData.locationCity" placeholder="请输入城市"></el-input>
            </el-form-item>

            <el-form-item label="详细地址">
                <el-input v-model="editData.locationDetail" placeholder="请输入详细地址"></el-input>
            </el-form-item>

            <el-form-item label="年代">
                <el-input v-model="editData.era" />
            </el-form-item>

            <el-form-item label="遗址类别">
                <el-input v-model="editData.category" placeholder="请输入遗址类别"></el-input>
            </el-form-item>

            <el-form-item label="保护级别">
                <el-input v-model="editData.protectionLevel" placeholder="请输入保护级别"></el-input>
            </el-form-item>

            <el-form-item label="发现年份">
                <el-input v-model="editData.discoveryYear" placeholder="请输入发现年份"></el-input>
            </el-form-item>

            <el-form-item label="发掘年份">
                <el-input v-model="editData.excavationYear" placeholder="请输入发掘年份"></el-input>
            </el-form-item>

            <el-form-item label="面积(㎡)">
                <el-input v-model="editData.areaSize" placeholder="请输入面积"></el-input>
            </el-form-item>
            <el-form-item label="遗址描述">
                <div class="editor">
                    <quill-editor :key="editData.id || 'new'" theme="snow" v-model:content="editData.description"
                        contentType="html">
                    </quill-editor>
                </div>
            </el-form-item>
            <!--
                auto-upload:设置是否自动上传
                action:设置服务器接口路径
                name:设置上传文件字段名
                headers:设置上传的请求头
                on-success:设置上传成功的回调函数
             -->
            <el-form-item label="遗址图片">
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
            <el-button type="primary" @click="updateSite">更新</el-button>
            <el-button type="info" @click="dialogVisible = false">取消</el-button>
        </el-form-item>
    </el-drawer>

    <!-- 导入遗迹抽屉 -->
    <el-drawer v-model="visibleImportDrawer" title="导入遗迹" direction="rtl" size="50%">
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