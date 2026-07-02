package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.MaterialCategory;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.MaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 材质分类 Controller - 提供级联选择器数据
 */
@RestController
@RequestMapping("/material-categories")
@CrossOrigin
public class MaterialCategoryController {

    @Autowired
    private MaterialCategoryService materialCategoryService;

    /**
     * 获取材质分类树形数据
     * GET /material-categories/tree
     * @return 树形结构，供前端 Cascader 使用
     */
    @GetMapping("/tree")
    public Result<List<MaterialCategory>> getTree() {
        List<MaterialCategory> tree = materialCategoryService.getTree();
        return Result.success(tree);
    }
}