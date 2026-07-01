package com.itheima.bigevent.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.bigevent.mapper.ArticleMapper;
import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.service.ArticleService;
import com.itheima.bigevent.utils.ThreadLocalUtil;

//文章实现类
@Service
public class ArticleServiceImpl implements ArticleService
{
    @Autowired
    //在实现类的方法中，我们需要调用mapper层的方法
    private ArticleMapper articleMapper;
    @Override
    public void add(Article article)
    {
        //补充属性值
        article.setCreate_time(LocalDateTime.now());
        article.setUpdate_time(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer)map.get("id"); 
        article.setCreate_user(userId); 
        articleMapper.add(article);
    }
    @Override
    public PageBean<Article> list( Integer pageNum,Integer pageSize,Integer category_id, String state)
    {


        //创建pagebean对象
        PageBean<Article> pageBean = new PageBean<>();




        //调用pagehelper函数
        PageHelper.startPage(pageNum, pageSize);

        //引入userid
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer)map.get("id");
        List<Article> as = articleMapper.list(userId, category_id, state);
        //page提供了方法，可以获取PageHelper分页查询后，得到的总记录条数和当前页数
        PageInfo<Article> pageInfo = new PageInfo<>(as);

        //把数据填充到pagebean对象中
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());
        return pageBean;
    }

    @Override
    public void update(Article article)
    {

        // 自动设置更新时间
        article.setUpdate_time(LocalDateTime.now());

        // 执行更新
        articleMapper.update(article);
    }

    @Override
    public void delete(Integer id)
    {
        articleMapper.delete(id);
    }
}
