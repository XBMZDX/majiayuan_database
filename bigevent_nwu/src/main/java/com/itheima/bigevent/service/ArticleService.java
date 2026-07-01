package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;

//service接口
public interface ArticleService
{
    //添加文章
    void add(Article article);

    //条件分页列表查询
    PageBean<Article> list( Integer pageNum,Integer pageSize,Integer category_id, String state);

    //文章更新
    void update(Article article);

    //文章删除
    void delete(Integer id);
}
