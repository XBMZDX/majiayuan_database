package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Category;
import com.itheima.bigevent.pojo.Result;

import java.util.List;

//创建一个接口
public interface CategoryService
{
    //新增分类
    void add(Category category);

    //列表查询
    List<Category> list();

    //根据id查询列表
    Category findById(Integer id);

    //更新分类
    void update(Category category);

    //删除

    void delete(Integer id);
}
