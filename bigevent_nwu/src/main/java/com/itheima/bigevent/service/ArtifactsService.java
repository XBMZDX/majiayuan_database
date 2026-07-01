package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.artifacts;

public interface ArtifactsService {
    // 添加遗物
    void add(artifacts artifact);
    // 根据ID删除遗物
    void deleteById(Integer id);
    // 修改遗物信息
    void update(artifacts artifact);
    // 分页查询遗物列表
    PageBean<artifacts> list(Integer pageNum, Integer pageSize, String name, String artifactCode, String siteName, String relicName, String category, String material, String era);
}
