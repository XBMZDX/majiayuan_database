package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.MaterialCategoryMapper;
import com.itheima.bigevent.pojo.MaterialCategory;
import com.itheima.bigevent.service.MaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 材质分类 Service 实现类
 * 使用 Stream 流构建树形结构
 */
@Service
public class MaterialCategoryServiceImpl implements MaterialCategoryService {

    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;

    @Override
    public List<MaterialCategory> getTree() {
        // 1. 查询所有材质分类数据
        List<MaterialCategory> allCategories = materialCategoryMapper.findAll();

        // 2. 筛选出一级分类（parentId == 0）
        List<MaterialCategory> level1List = allCategories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        // 3. 递归组装子节点：为每个一级节点挂载二级，二级挂载三级
        for (MaterialCategory level1 : level1List) {
            // 找当前一级节点下的二级节点
            List<MaterialCategory> children = getChildren(level1.getId(), allCategories);
            level1.setChildren(children);
        }

        return level1List;
    }

    /**
     * 递归查找子节点
     * @param parentId      父节点ID
     * @param allCategories 所有材质数据
     * @return 子节点列表（已递归挂载）
     */
    private List<MaterialCategory> getChildren(Integer parentId, List<MaterialCategory> allCategories) {
        // 筛选出 parentId 匹配的直接子节点
        List<MaterialCategory> children = allCategories.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .collect(Collectors.toList());

        // 递归为每个子节点挂载它的下一级
        for (MaterialCategory child : children) {
            List<MaterialCategory> grandChildren = getChildren(child.getId(), allCategories);
            child.setChildren(grandChildren.isEmpty() ? null : grandChildren);
        }

        return children;
    }
}