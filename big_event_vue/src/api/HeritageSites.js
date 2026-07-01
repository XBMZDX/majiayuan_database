//导入request工具类
import request from '@/utils/request.js'
import { useTokenStore } from '../stores/token'
//后端接口

//遗址分类列表查询
export const siteCategoryListService = (params) => {
    return request.get('/heritage-sites',{params:params})
}


//遗址添加
export const siteAddService = (siteData)=>{
    return request.post('/heritage-sites',siteData)
}

//遗址更新
export const siteUpdateService = (siteData)=>{
    return request.put('/heritage-sites',siteData)
}

 //遗址删除
 export const siteDeleteService =(id)=>{
     return request.delete('/heritage-sites?id='+id)
}

//遗址批量导入
export const siteBatchImportService = (sitesData)=>{
    return request.post('/heritage-sites/batch', sitesData)
}
