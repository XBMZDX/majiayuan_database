package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.CompletenessCategoryMapper;
import com.itheima.bigevent.pojo.CompletenessCategory;
import com.itheima.bigevent.service.CompletenessCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 完整度分类 Service 实现类
 * 使用 Stream 流 + 递归构建树形结构
 */
@Service
public class CompletenessCategoryServiceImpl implements CompletenessCategoryService {

    @Autowired
    private CompletenessCategoryMapper mapper;

    @Override
    public List<CompletenessCategory> getTree() {
        // 1. 查询所有数据
        List<CompletenessCategory> all = mapper.findAll();

        // 2. 筛选一级分类
        List<CompletenessCategory> level1 = all.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        // 3. 递归挂载子节点
        for (CompletenessCategory c : level1) {
            c.setChildren(getChildren(c.getId(), all));
        }
        return level1;
    }

    private List<CompletenessCategory> getChildren(Integer parentId, List<CompletenessCategory> all) {
        List<CompletenessCategory> children = all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .collect(Collectors.toList());
        for (CompletenessCategory child : children) {
            List<CompletenessCategory> grand = getChildren(child.getId(), all);
            child.setChildren(grand.isEmpty() ? null : grand);
        }
        return children;
    }
}