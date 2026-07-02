package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 材质分类实体类 - 支持树形结构（级联选择器）
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MaterialCategory {
    private Integer id;           // 主键ID
    private Integer parentId;     // 父级ID，0表示一级分类
    private String name;          // 材质名称
    private Integer level;        // 层级: 1/2/3
    private Integer sortOrder;    // 排序

    // 子节点（非数据库字段，用于构建树形结构）
    private List<MaterialCategory> children;
}