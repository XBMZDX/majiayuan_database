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

//文章分类数据模型
const categorys = ref([])

//分页条数据模型
const pageNum = ref(1)//当前页
const total = ref(20)//总条数
const pageSize = ref(3)//每页条数

//当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
    pageSize.value = size
    //刷新列表
    siteCategoryList();
}
//当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
    pageNum.value = num
    //刷新列表
    siteCategoryList();
}

import { siteCategoryListService, siteAddService, siteDeleteService, siteUpdateService, siteBatchImportService } from '@/api/HeritageSites.js'
//声明一个函数
const siteCategoryList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
    }
    //发送请求获取分类列表
    let result = await siteCategoryListService(params);
    categorys.value = result.data.items;
    total.value = result.data.total;
}
siteCategoryList();
import { Plus } from '@element-plus/icons-vue'
//控制抽屉是否显示
const visibleDrawer = ref(false)

//添加表单数据模型
const HeritageSitesModel = ref({
    siteCode: '',
    name: '',
    alias: '',
    locationProvince: '',
    locationCity: '',
    locationDetail: '',
    latitude: '',
    longitude: '',
    era: '',
    category: '',
    protectionLevel: '',
    discoveryYear: '',
    excavationYear: '',
    areaSize: '',
    description: '',
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
//添加遗址
const siteAdd = async () => {
    let result = await siteAddService(HeritageSitesModel.value)
    ElMessage.success(result.message ? result.message : '添加成功')

    //关闭抽屉
    visibleDrawer.value = false;
    //刷新列表
    siteCategoryList();
}
import { ElMessage, ElMessageBox } from 'element-plus'

//删除遗址
const deleteSite = (row) => {
    //提示用户--确认框
    ElMessageBox.confirm(
        '你确认删除该遗址吗？',
        '温馨提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            //调用接口
            console.log("删除的遗址 id =", row.id);
            let result = await siteDeleteService(row.id);
            ElMessage({
                type: 'success',
                message: '成功删除',
            })
            //刷新列表
            //获取所有遗址函数
            siteCategoryList();
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
//控制编辑抽屉显示

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
                        siteCode: item['遗址编号'] || item['siteCode'] || '',
                        name: item['遗址名称'] || item['name'] || '',
                        alias: item['别名'] || item['alias'] || '',
                        locationProvince: item['省份'] || item['locationProvince'] || '',
                        locationCity: item['城市'] || item['locationCity'] || '',
                        locationDetail: item['详细地址'] || item['locationDetail'] || '',
                        latitude: item['纬度'] || item['latitude'] || '',
                        longitude: item['经度'] || item['longitude'] || '',
                        era: item['年代'] || item['era'] || '',
                        category: item['遗址类别'] || item['category'] || '',
                        protectionLevel: item['保护级别'] || item['protectionLevel'] || '',
                        discoveryYear: item['发现年份'] || item['discoveryYear'] || '',
                        excavationYear: item['发掘年份'] || item['excavationYear'] || '',
                        areaSize: item['面积'] || item['areaSize'] || '',
                        description: item['遗址描述'] || item['description'] || ''
                    }
                })
                
                // 过滤掉必填字段为空的数据
                const validData = importData.filter(item => {
                    return item.siteCode && item.name
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
                const result = await siteBatchImportService(validData)
                
                importing.value = false
                importResult.value = {
                    success: true,
                    message: `导入成功，共导入${validData.length}条数据`
                }
                ElMessage.success(result.message || '导入成功')
                // 刷新列表
                siteCategoryList()
                
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
                <span>遗址管理</span>
                <div class="extra">
                    <el-button type="primary" @click="visibleDrawer = true">添加遗址</el-button>
                    <el-button type="success" @click="visibleImportDrawer = true">导入遗址</el-button>
                </div>
            </div>
        </template>
        <el-table :data="categorys" style="width: 100%">
            <el-table-column label="序号" width="100" type="index"> </el-table-column>
            <el-table-column label="遗址编号" prop="siteCode"></el-table-column>
            <el-table-column label="遗址名称" prop="name"></el-table-column>
            <el-table-column label="省份" prop="locationProvince"> </el-table-column>
            <el-table-column label="遗址类别" prop="category"> </el-table-column>
            <el-table-column label="所属时代" prop="era"> </el-table-column>
            <el-table-column label="遗址面积" prop="areaSize"> </el-table-column>
            <el-table-column label="操作" width="150">
                <template #default="{ row }">
                    <el-button :icon="Check" circle plain type="success" @click="openDetail(row)"></el-button>
                    <el-button :icon="Edit" circle plain type="primary" @click="openEdit(row)"></el-button>
                    <el-button :icon="Delete" circle plain type="danger" @click="deleteSite(row)"></el-button>
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
        <el-form :model="HeritageSitesModel" label-width="100px">
            <!--遗址信息 -->
            <el-form-item label="遗址编号" prop="siteCode">
                <el-input v-model="HeritageSitesModel.siteCode" placeholder="请输入遗址编号"></el-input>
            </el-form-item>

            <el-form-item label="遗址名称" prop="name">
                <el-input v-model="HeritageSitesModel.name" placeholder="请输入遗址名称"></el-input>
            </el-form-item>

            <el-form-item label="所在省份" prop="locationProvince">
                <el-input v-model="HeritageSitesModel.locationProvince" placeholder="请输入省份"></el-input>
            </el-form-item>

            <el-form-item label="纬度" prop="latitude">
                <el-input v-model="HeritageSitesModel.latitude" placeholder="请输入纬度"></el-input>
            </el-form-item>

            <el-form-item label="经度" prop="longitude">
                <el-input v-model="HeritageSitesModel.longitude" placeholder="请输入经度"></el-input>
            </el-form-item>

            <!-- 非必填项 -->
            <el-form-item label="城市">
                <el-input v-model="HeritageSitesModel.locationCity" placeholder="请输入城市"></el-input>
            </el-form-item>

            <el-form-item label="详细地址">
                <el-input v-model="HeritageSitesModel.locationDetail" placeholder="请输入详细地址"></el-input>
            </el-form-item>

            <el-form-item label="年代">
                <el-input v-model="HeritageSitesModel.era" />
            </el-form-item>

            <el-form-item label="遗址类别">
                <el-input v-model="HeritageSitesModel.category" placeholder="请输入遗址类别"></el-input>
            </el-form-item>

            <el-form-item label="保护级别">
                <el-input v-model="HeritageSitesModel.protectionLevel" placeholder="请输入保护级别"></el-input>
            </el-form-item>

            <el-form-item label="发现年份">
                <el-input v-model="HeritageSitesModel.discoveryYear" placeholder="请输入发现年份"></el-input>
            </el-form-item>

            <el-form-item label="发掘年份">
                <el-input v-model="HeritageSitesModel.excavationYear" placeholder="请输入发掘年份"></el-input>
            </el-form-item>

            <el-form-item label="面积(㎡)">
                <el-input v-model="HeritageSitesModel.areaSize" placeholder="请输入面积"></el-input>
            </el-form-item>
            <el-form-item label="遗址描述">
                <div class="editor">
                    <quill-editor theme="snow" v-model:content="HeritageSitesModel.description" contentType="html">
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
                    <img v-if="HeritageSitesModel.coverImage" :src="HeritageSitesModel.coverImage" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="siteAdd">发布</el-button>
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
                    <quill-editor :key="editData.id || 'new'"  theme="snow" v-model:content="editData.description" contentType="html">
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

    <!-- 导入遗址抽屉 -->
    <el-drawer v-model="visibleImportDrawer" title="导入遗址" direction="rtl" size="50%">
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