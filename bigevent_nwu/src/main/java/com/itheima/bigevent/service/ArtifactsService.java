package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.artifacts;
import java.util.List;

public interface ArtifactsService {
    // 添加文物
    void add(artifacts artifact);
    // 根据ID删除文物
    void deleteById(Integer id);
    // 批量删除文物
    void batchDelete(List<Integer> ids);
    // 修改文物信息
    void update(artifacts artifact);
    // 分页查询文物列表
    PageBean<artifacts> list(Integer pageNum, Integer pageSize, String keyword, String newArtifactName, String newArtifactCode, String material1, String excavationRelic, String completeness);
}
