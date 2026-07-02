package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.CompletenessCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 完整度分类 Mapper 接口
 */
@Mapper
public interface CompletenessCategoryMapper {

    @Select("SELECT id, parent_id, name, level, sort_order FROM completeness_categories ORDER BY level, sort_order")
    List<CompletenessCategory> findAll();
}