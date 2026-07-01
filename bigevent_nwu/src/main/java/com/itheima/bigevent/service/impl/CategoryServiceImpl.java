package com.itheima.bigevent.service.impl;
import java.time.LocalDateTime;
//import java.util.Locale.Category;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itheima.bigevent.mapper.CategoryMapper;
import com.itheima.bigevent.service.CategoryService;
import com.itheima.bigevent.utils.ThreadLocalUtil; 
import com.itheima.bigevent.pojo.Category;
import java.util.List;
//把CategoryService的实现类对象交给ioc容器，添加service注解
@Service
//创建接口实现类
public class CategoryServiceImpl implements CategoryService
{
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public void add(Category category)
    {
        //补充属性值
        category.setUpdate_time(LocalDateTime.now());
        category.setCreate_time(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer)map.get("id");
        category.setCreate_user(userId);
        categoryMapper.add(category);
    }
    @Override
    public List<Category> list()
    {
        //调用mapper层方法，我们需要获得当前用户的id,需要获取id
        //调用Threadlocalutil.get()获取id
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer)map.get("id");   
        return categoryMapper.list(userId);
    }

    @Override
    public Category findById(Integer id)
    {
        Category c = categoryMapper.findById(id);
        return c;
    } 

    //更新分类
    @Override
    public void update(Category category)
    {
        category.setUpdate_time(LocalDateTime.now());
        categoryMapper.update(category);
    }

    //删除
    @Override
    public void delete(Integer id)
    {
        categoryMapper.deleteById(id);
    }
}
