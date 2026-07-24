package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.artifacts;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArtifactsMapper {
    @Insert("INSERT INTO artifacts (burial_id, coffin_index, coffin_id, chariot_id, serial_number, new_artifact_code, new_artifact_name, original_artifact_code, original_artifact_name, material1, material2, completeness, artifact_description, quantity1, quantity2, dimensions, weight, excavation_relic, excavation_position, excavation_time, storage_method, images, transfer_process, restoration_status, photographer, draftsperson, text_describer, notes, grading_status, testing_status, created_by, create_time, update_time) VALUES (#{burialId}, #{coffinIndex}, #{coffinId}, #{chariotId}, #{serialNumber}, #{newArtifactCode}, #{newArtifactName}, #{originalArtifactCode}, #{originalArtifactName}, #{material1}, #{material2}, #{completeness}, #{artifactDescription}, #{quantity1}, #{quantity2}, #{dimensions}, #{weight}, #{excavationRelic}, #{excavationPosition}, #{excavationTime}, #{storageMethod}, #{images}, #{transferProcess}, #{restorationStatus}, #{photographer}, #{draftsperson}, #{textDescriber}, #{notes}, #{gradingStatus}, #{testingStatus}, #{createdBy}, #{createTime}, #{updateTime})")
    void add(artifacts artifact);

    @Delete("DELETE FROM artifacts WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT serial_number FROM artifacts WHERE id = #{id}")
    Integer getSerialNumberById(Integer id);

    @Select("SELECT COALESCE(MAX(CAST(serial_number AS DECIMAL(20,0))), 0) FROM artifacts")
    Integer getMaxSerialNumber();

    @Select("SELECT MIN(a.serial_number) + 1 FROM artifacts a WHERE NOT EXISTS (SELECT 1 FROM artifacts b WHERE b.serial_number = a.serial_number + 1)")
    Integer findFirstGapSerialNumber();

    @Update("UPDATE artifacts SET serial_number = serial_number - 1 WHERE serial_number > #{deletedSerialNumber}")
    void decrementAfterDelete(Integer deletedSerialNumber);

    void batchDelete(List<Integer> ids);

    @Select("SELECT id FROM artifacts ORDER BY CAST(serial_number AS DECIMAL(20,0)) ASC, id ASC")
    List<Integer> listIdsInSerialOrder();

    @Update("UPDATE artifacts SET serial_number = #{serialNumber} WHERE id = #{id}")
    void updateSerialNumber(@Param("id") Integer id, @Param("serialNumber") Integer serialNumber);

    @Update("UPDATE artifacts SET testing_status = NULL WHERE testing_status IS NOT NULL AND testing_status <> ''")
    void clearManualTestingStatus();

    void update(artifacts artifact);

    List<artifacts> list(String keyword, String newArtifactName, String newArtifactCode, String material1, String excavationRelic, String completeness);

    @Select("SELECT * FROM artifacts WHERE burial_id = #{burialId} ORDER BY CAST(serial_number AS DECIMAL(20,0)) ASC, id ASC")
    List<artifacts> listByBurial(Integer burialId);

    @Select("SELECT * FROM artifacts WHERE coffin_id = #{coffinId} ORDER BY CAST(serial_number AS DECIMAL(20,0)) ASC, id ASC")
    List<artifacts> listByCoffin(Integer coffinId);

    @Select("SELECT COUNT(*) FROM artifacts WHERE coffin_id = #{coffinId}")
    Integer countByCoffin(Integer coffinId);

    @Select("SELECT COALESCE(NULLIF(material1,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE coffin_id = #{coffinId} GROUP BY material1")
    List<Map<String, Object>> coffinMaterialDistribution(Integer coffinId);

    @Select("SELECT COALESCE(NULLIF(completeness,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE coffin_id = #{coffinId} GROUP BY completeness")
    List<Map<String, Object>> coffinCompletenessDistribution(Integer coffinId);

    @Select("SELECT COUNT(*) FROM artifacts WHERE burial_id = #{burialId}")
    Integer countByBurial(Integer burialId);

    @Select("SELECT COALESCE(NULLIF(material1,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE burial_id = #{burialId} GROUP BY material1")
    List<Map<String, Object>> materialDistribution(Integer burialId);

    @Select("SELECT COALESCE(NULLIF(completeness,''),'未知') AS name, COUNT(*) AS count FROM artifacts WHERE burial_id = #{burialId} GROUP BY completeness")
    List<Map<String, Object>> completenessDistribution(Integer burialId);
}
