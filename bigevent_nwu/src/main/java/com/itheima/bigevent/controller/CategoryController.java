package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Category;
import com.itheima.bigevent.pojo.Category.AddGroup;
import com.itheima.bigevent.pojo.Category.UpdateGroup;

import java.security.DrbgParameters.Reseed;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.CategoryService;

import com.itheima.bigevent.utils.ThreadLocalUtil;

import jakarta.annotation.Generated;

//创建一个CategoryController类
@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result add(@RequestBody @Validated(AddGroup.class) Category category)
    {
        categoryService.add(category);
        return Result.success();
    }

    //查询所有分类
    @GetMapping
    public Result<List<Category>> list()
    {
        //返回一个集合，集合中存放多个分类列表
        List<Category> cs = categoryService.list();
        return Result.success(cs);
    }

    //查询详细信息
    //与之前的/category拼接之后，就是/category/detail
   @GetMapping("/detail")
    public Result<Category> findById(Integer id)
    {
        Category c = categoryService.findById(id);
        return Result.success(c);
    }
    //更新
    @PutMapping
    public Result update(@RequestBody @Validated(UpdateGroup.class) Category category)
    {
        categoryService.update(category);
        return Result.success();
    }

    //删除
    @DeleteMapping
    public Result delete(Integer id)
    {
        categoryService.delete(id);
        return Result.success();
    }
}
