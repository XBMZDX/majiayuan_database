package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.CompletenessCategory;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.CompletenessCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 完整度分类 Controller
 * GET /completeness-categories/tree — 返回树形数据供前端 Cascader 使用
 */
@RestController
@RequestMapping("/completeness-categories")
@CrossOrigin
public class CompletenessCategoryController {

    @Autowired
    private CompletenessCategoryService service;

    @GetMapping("/tree")
    public Result<List<CompletenessCategory>> getTree() {
        return Result.success(service.getTree());
    }
}