package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.heritageSites;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface HeritageSitesMapper {
    // 新增遗址
    @Insert("INSERT INTO heritage_sites (site_code, name, alias, location_province, location_city, location_detail, latitude, longitude, era, category, protection_level, discovery_year, excavation_year, area_size, description, cultural_value, current_status, cover_image, created_by, create_time, update_time) VALUES (#{siteCode}, #{name}, #{alias}, #{locationProvince}, #{locationCity}, #{locationDetail}, #{latitude}, #{longitude}, #{era}, #{category}, #{protectionLevel}, #{discoveryYear}, #{excavationYear}, #{areaSize}, #{description}, #{culturalValue}, #{currentStatus}, #{coverImage}, #{createdBy}, #{createTime}, #{updateTime})")
    void add(heritageSites heritageSite);
    
    // 删除遗址
    @Delete("DELETE FROM heritage_sites WHERE id = #{id}")
    void delete(Integer id);
    
    // 修改遗址 - 使用XML配置的动态SQL
    void update(heritageSites heritageSite);
    
    // 查询遗址列表 - 使用XML配置的动态SQL
    List<heritageSites> list(String name, String locationProvince, String locationCity, String protectionLevel);
    
    // 批量插入遗址数据
    void batchInsert(List<heritageSites> heritageSitesList);
}
