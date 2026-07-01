package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.artifacts;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArtifactsMapper {
    // 添加遗物
    @Insert("INSERT INTO artifacts (artifact_code, site_id, site_name, relic_id, relic_name, name, category, sub_category, material, era, size, weight, color, texture, decoration, inscription, production_technique, usage_function, discovery_context, preservation_condition, restoration_info, current_location, museum_number, images, model3d_url, research_notes, bibliography, cultural_value, created_by, create_time, update_time, verification_status, verification_notes) VALUES (#{artifactCode}, #{siteId}, #{siteName}, #{relicId}, #{relicName}, #{name}, #{category}, #{subCategory}, #{material}, #{era}, #{size}, #{weight}, #{color}, #{texture}, #{decoration}, #{inscription}, #{productionTechnique}, #{usageFunction}, #{discoveryContext}, #{preservationCondition}, #{restorationInfo}, #{currentLocation}, #{museumNumber}, #{images}, #{model3dUrl}, #{researchNotes}, #{bibliography}, #{culturalValue}, #{createdBy}, #{createTime}, #{updateTime}, #{verificationStatus}, #{verificationNotes})")
    void add(artifacts artifact);
    // 根据ID删除遗物
    @Delete("DELETE FROM artifacts WHERE id = #{id}")
    void deleteById(Integer id);
    // 修改遗物信息
    void update(artifacts artifact);
    // 查询遗物列表 - 使用XML配置的动态SQL
    List<artifacts> list(String name, String artifactCode, String siteName, String relicName, String category, String material, String era);
}
