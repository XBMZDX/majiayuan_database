package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.CompletenessCategory;
import java.util.List;

/**
 * 完整度分类 Service 接口
 */
public interface CompletenessCategoryService {

    /**
     * 获取完整度分类树形数据（供前端 Cascader 使用）
     */
    List<CompletenessCategory> getTree();
}