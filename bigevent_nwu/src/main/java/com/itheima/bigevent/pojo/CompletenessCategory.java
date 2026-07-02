package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 完整度分类实体类 - 支持树形结构（级联选择器）
 * 与 MaterialCategory 采用相同设计模式
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletenessCategory {
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private List<CompletenessCategory> children;
}