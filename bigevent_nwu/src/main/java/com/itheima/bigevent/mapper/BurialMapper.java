package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Burial;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BurialMapper {

    // 简易列表（下拉用）
    @Select("SELECT id, burial_no, name, has_coffin, coffin_count FROM burial ORDER BY id DESC")
    List<Burial> listSimple();

    // 根据ID查询
    @Select("SELECT * FROM burial WHERE id = #{id}")
    Burial findById(Integer id);

    // 新增
    @Insert("INSERT INTO burial (burial_no, name, site_id, site_name, era, burial_type, excavation_date, has_coffin, has_chariot, coffin_count, coffin_material, coffin_decoration, skeleton_status, chariot_count, horse_count, chariot_decoration, chariot_type, artifact_count, bone_preservation, status, notes, created_by, create_time, update_time) VALUES (#{burialNo}, #{name}, #{siteId}, #{siteName}, #{era}, #{burialType}, #{excavationDate}, #{hasCoffin}, #{hasChariot}, #{coffinCount}, #{coffinMaterial}, #{coffinDecoration}, #{skeletonStatus}, #{chariotCount}, #{horseCount}, #{chariotDecoration}, #{chariotType}, #{artifactCount}, #{bonePreservation}, #{status}, #{notes}, #{createdBy}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Burial burial);

    // 更新
    @Update("UPDATE burial SET burial_no=#{burialNo}, name=#{name}, site_id=#{siteId}, site_name=#{siteName}, era=#{era}, burial_type=#{burialType}, excavation_date=#{excavationDate}, has_coffin=#{hasCoffin}, has_chariot=#{hasChariot}, coffin_count=#{coffinCount}, coffin_material=#{coffinMaterial}, coffin_decoration=#{coffinDecoration}, skeleton_status=#{skeletonStatus}, chariot_count=#{chariotCount}, horse_count=#{horseCount}, chariot_decoration=#{chariotDecoration}, chariot_type=#{chariotType}, artifact_count=#{artifactCount}, bone_preservation=#{bonePreservation}, status=#{status}, notes=#{notes}, update_time=#{updateTime} WHERE id=#{id}")
    void update(Burial burial);
}