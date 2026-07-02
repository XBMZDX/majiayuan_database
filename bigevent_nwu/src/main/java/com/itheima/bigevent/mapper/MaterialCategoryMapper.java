package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.MaterialCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 材质分类 Mapper 接口
 */
@Mapper
public interface MaterialCategoryMapper {

    /**
     * 查询所有材质分类，按排序字段排列
     */
    @Select("SELECT id, parent_id, name, level, sort_order FROM material_categories ORDER BY level, sort_order")
    List<MaterialCategory> findAll();
}