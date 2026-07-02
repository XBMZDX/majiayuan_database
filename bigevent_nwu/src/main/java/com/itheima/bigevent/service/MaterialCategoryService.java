package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.MaterialCategory;
import java.util.List;

/**
 * 材质分类 Service 接口
 */
public interface MaterialCategoryService {

    /**
     * 获取材质分类树形数据（供前端 Cascader 使用）
     * @return 树形结构的材质列表
     */
    List<MaterialCategory> getTree();
}