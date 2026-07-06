package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.artifacts;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArtifactsMapper {
    // 添加文物
    @Insert("INSERT INTO artifacts (burial_id, coffin_index, coffin_id, chariot_id, serial_number, new_artifact_code, new_artifact_name, original_artifact_code, original_artifact_name, material1, material2, completeness, artifact_description, quantity1, quantity2, dimensions, weight, excavation_relic, excavation_position, excavation_time, storage_method, images, transfer_process, restoration_status, photographer, draftsperson, text_describer, notes, grading_status, testing_status, created_by, create_time, update_time) VALUES (#{burialId}, #{coffinIndex}, #{coffinId}, #{chariotId}, #{serialNumber}, #{newArtifactCode}, #{newArtifactName}, #{originalArtifactCode}, #{originalArtifactName}, #{material1}, #{material2}, #{completeness}, #{artifactDescription}, #{quantity1}, #{quantity2}, #{dimensions}, #{weight}, #{excavationRelic}, #{excavationPosition}, #{excavationTime}, #{storageMethod}, #{images}, #{transferProcess}, #{restorationStatus}, #{photographer}, #{draftsperson}, #{textDescriber}, #{notes}, #{gradingStatus}, #{testingStatus}, #{createdBy}, #{createTime}, #{updateTime})")
    void add(artifacts artifact);

    // 根据ID删除文物
    @Delete("DELETE FROM artifacts WHERE id = #{id}")
    void deleteById(Integer id);

    // 根据ID查询文物的序号（删除前获取，用于后续重排）
    @Select("SELECT serial_number FROM artifacts WHERE id = #{id}")
    Integer getSerialNumberById(Integer id);

    // 查询当前最大序号（用于新增时追加）
    @Select("SELECT COALESCE(MAX(CAST(serial_number AS UNSIGNED)), 0) FROM artifacts")
    Integer getMaxSerialNumber();

    // 查询第一个空缺序号（找到最小的 sn 使得 sn+1 不存在）
    @Select("SELECT MIN(a.serial_number) + 1 FROM artifacts a WHERE NOT EXISTS (SELECT 1 FROM artifacts b WHERE b.serial_number = a.serial_number + 1)")
    Integer findFirstGapSerialNumber();

    // 删除后，将大于被删序号的所有序号减1（保持连续性）
    @Update("UPDATE artifacts SET serial_number = serial_number - 1 WHERE serial_number > #{deletedSerialNumber}")
    void decrementAfterDelete(Integer deletedSerialNumber);

    // 批量删除文物
    void batchDelete(List<Integer> ids);

    // 批量删除后重新编号所有序号（按当前序号升序依次赋予1,2,3...）
    @Update("UPDATE artifacts SET serial_number = (@num := @num + 1) ORDER BY CAST(serial_number AS UNSIGNED) ASC")
    void renumberAllSerialNumbers();

    // 初始化 MySQL 用户变量
    @Update("SET @num = 0")
    void initRowNum();

    // 修改文物信息
    void update(artifacts artifact);

    // 查询文物列表 - 使用XML配置的动态SQL
    List<artifacts> list(String newArtifactName, String newArtifactCode, String material1, String excavationRelic, String completeness);

    // 按墓葬查询文物列表
    @Select("SELECT * FROM artifacts WHERE burial_id = #{burialId} ORDER BY CAST(serial_number AS UNSIGNED) ASC")
    List<artifacts> listByBurial(Integer burialId);

    // 按棺查询
    @Select("SELECT * FROM artifacts WHERE coffin_id = #{coffinId} ORDER BY CAST(serial_number AS UNSIGNED) ASC")
    List<artifacts> listByCoffin(Integer coffinId);

    @Select("SELECT COUNT(*) FROM artifacts WHERE coffin_id = #{coffinId}")
    Integer countByCoffin(Integer coffinId);

    @Select("SELECT COALESCE(NULLIF(material1,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE coffin_id = #{coffinId} GROUP BY material1")
    List<java.util.Map<String,Object>> coffinMaterialDistribution(Integer coffinId);

    @Select("SELECT COALESCE(NULLIF(completeness,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE coffin_id = #{coffinId} GROUP BY completeness")
    List<java.util.Map<String,Object>> coffinCompletenessDistribution(Integer coffinId);

    // 墓葬文物总数
    @Select("SELECT COUNT(*) FROM artifacts WHERE burial_id = #{burialId}")
    Integer countByBurial(Integer burialId);

    // 墓葬文物材质分布
    @Select("SELECT COALESCE(NULLIF(material1,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE burial_id = #{burialId} GROUP BY material1")
    List<java.util.Map<String,Object>> materialDistribution(Integer burialId);

    // 墓葬文物完整度分布
    @Select("SELECT COALESCE(NULLIF(completeness,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE burial_id = #{burialId} GROUP BY completeness")
    List<java.util.Map<String,Object>> completenessDistribution(Integer burialId);
}