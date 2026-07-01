package com.itheima.bigevent.controller;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ArticleService;
import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/article")
@CrossOrigin
public class ArticleController
{
    //需要调用service层方法完成，所以需要先注入一个service层对象
    @Autowired
    private ArticleService articleService;


    @PostMapping
    public Result add(@RequestBody @Validated Article article)
    {
        articleService.add(article);
        return Result.success();
    }
    
    @GetMapping
    public Result<PageBean<Article>> list( Integer pageNum,Integer pageSize,@RequestParam(required = false) Integer category_id,@RequestParam(required = false) String state)
    {
       PageBean<Article> pb =  articleService.list(pageNum,pageSize,category_id,state);
       return Result.success(pb);
    }

    //文章更新
    @PutMapping
    public Result update(@RequestBody @Validated Article article)
    {
        articleService.update(article);
        return Result.success();
    }

    //文章删除
     @DeleteMapping
    public Result delete(@RequestParam Integer id)
    {
        articleService.delete(id);
        return Result.success();
    }
}
