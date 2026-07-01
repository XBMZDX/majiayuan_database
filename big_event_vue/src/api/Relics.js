//导入request工具类
import request from '@/utils/request.js'
import { useTokenStore } from '../stores/token'

//遗迹分类列表查询
export const relicCategoryListService = (params) => {
    return request.get('/relics',{params:params})
}
//遗迹添加
export const relicAddService = (relicData)=>{
    return request.post('/relics',relicData)
}

//遗迹批量导入
export const relicsBatchImportService = (relicsData)=>{
    return request.post('/relics/batch', relicsData)
}

//遗迹删除
export const relicDeleteService = (id)=>{
    return request.delete('/relics?id=' + id)
}