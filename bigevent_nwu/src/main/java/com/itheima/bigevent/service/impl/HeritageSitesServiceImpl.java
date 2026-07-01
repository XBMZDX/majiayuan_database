package com.itheima.bigevent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.bigevent.mapper.HeritageSitesMapper;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.heritageSites;
import com.itheima.bigevent.service.HeritageSitesService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class HeritageSitesServiceImpl implements HeritageSitesService {
    
    @Autowired
    private HeritageSitesMapper heritageSitesMapper;
    
    @Override
    public void add(heritageSites heritageSite) {
        // 设置创建时间和更新时间
        heritageSite.setCreateTime(LocalDateTime.now());
        heritageSite.setUpdateTime(LocalDateTime.now());
        
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        heritageSite.setCreatedBy(userId);
        
        // 调用Mapper层方法添加数据
        heritageSitesMapper.add(heritageSite);
    }
    
    @Override
    public void delete(Integer id) {
        heritageSitesMapper.delete(id);
    }
    
    @Override
    public void update(heritageSites heritageSite) {
        // 设置更新时间
        heritageSite.setUpdateTime(LocalDateTime.now());
        
        // 调用Mapper层方法更新数据
        heritageSitesMapper.update(heritageSite);
    }
    
    @Override
    public PageBean<heritageSites> list(Integer pageNum, Integer pageSize, String name, String locationProvince, String locationCity, String protectionLevel) {
        // 创建PageBean对象
        PageBean<heritageSites> pageBean = new PageBean<>();
        
        // 调用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);
        
        // 调用mapper层方法查询数据
        List<heritageSites> heritageSitesList = heritageSitesMapper.list(name, locationProvince, locationCity, protectionLevel);
        
        // 使用PageInfo获取分页信息
        PageInfo<heritageSites> pageInfo = new PageInfo<>(heritageSitesList);
        
        // 填充PageBean数据
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());
        
        return pageBean;
    }
    
    @Override
    public void batchImport(List<heritageSites> heritageSitesList) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        
        // 设置每个遗址的创建时间、更新时间和创建人ID
        for (heritageSites heritageSite : heritageSitesList) {
            heritageSite.setCreateTime(LocalDateTime.now());
            heritageSite.setUpdateTime(LocalDateTime.now());
            heritageSite.setCreatedBy(userId);
        }
        
        // 调用Mapper层方法批量插入数据
        heritageSitesMapper.batchInsert(heritageSitesList);
    }
}
