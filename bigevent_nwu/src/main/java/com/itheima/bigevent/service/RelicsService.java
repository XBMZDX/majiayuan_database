package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.relics;
import java.util.List;

public interface RelicsService {
    // 新增遗迹
    void add(relics relic);
    
    // 根据ID删除遗迹
    void deleteById(Integer id);
    
    // 修改遗迹
    void update(relics relic);
    
    
    // 分页查询遗迹列表
    PageBean<relics> list(Integer pageNum, Integer pageSize, String name, String relicCode, String siteName, String type);
    
    // 批量导入遗迹数据
    void batchImport(List<relics> relicsList);
}
