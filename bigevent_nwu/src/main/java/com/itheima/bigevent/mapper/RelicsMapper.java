package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.relics;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RelicsMapper {
    // 新增遗迹
    void add(relics relic);
    
    // 根据ID删除遗迹
    @Delete("DELETE FROM relics WHERE id = #{id}")
    void deleteById(Integer id);
    
    // 修改遗迹（动态更新）
    void update(relics relic);
     
    // 分页查询遗迹列表
    List<relics> list(String name, String relicCode, String siteName, String type);
    
    // 批量插入遗迹数据
    void batchInsert(List<relics> relicsList);

    // 获取所有遗迹名称（供下拉筛选使用）
    @Select("SELECT DISTINCT name FROM relics WHERE name IS NOT NULL AND name != '' ORDER BY name")
    List<String> getNames();
}
