package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.heritageSites;
import java.util.List;

public interface HeritageSitesService {
    // 新增遗址
    void add(heritageSites heritageSite);
    
    // 删除遗址
    void delete(Integer id);
    
    // 修改遗址
    void update(heritageSites heritageSite);
    
    // 分页查询遗址列表
    PageBean<heritageSites> list(Integer pageNum, Integer pageSize, String name, String locationProvince, String locationCity, String protectionLevel);
    
    // 批量导入遗址数据
    void batchImport(List<heritageSites> heritageSitesList);
}
