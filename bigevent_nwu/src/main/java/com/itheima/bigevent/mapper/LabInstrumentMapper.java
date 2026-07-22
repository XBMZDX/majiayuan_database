package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.LabInstrument;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LabInstrumentMapper {

    @Select("""
        SELECT li.*,
               (
                   SELECT COUNT(DISTINCT COALESCE(NULLIF(TRIM(da.artifact_code), ''),
                                                  NULLIF(TRIM(da.artifact_name), ''),
                                                  CONCAT('detection-', da.id)))
                   FROM detection_analysis da
                   WHERE FIND_IN_SET(
                       li.name COLLATE utf8mb4_unicode_ci,
                       REPLACE(COALESCE(da.purpose, '') COLLATE utf8mb4_unicode_ci, '/', ',')
                   ) > 0
               ) AS applied_artifact_count,
               (
                   SELECT COUNT(DISTINCT er.id)
                   FROM experiment_results er
                   WHERE er.experiment_name COLLATE utf8mb4_unicode_ci = li.name COLLATE utf8mb4_unicode_ci
                     AND er.status = '已完成'
               ) AS completed_detection_count
        FROM lab_instruments li
        ORDER BY li.id DESC
        """)
    List<LabInstrument> list();

    @Select("SELECT * FROM lab_instruments WHERE id = #{id}")
    LabInstrument findById(Integer id);

    @Insert("INSERT INTO lab_instruments (name, image, scope, location, model, conditions, method, method_name, applicable_materials, research_purposes, non_destructive, requires_sampling, main_outputs) VALUES (#{name}, #{image}, #{scope}, #{location}, #{model}, #{conditions}, #{method}, #{methodName}, #{applicableMaterials}, #{researchPurposes}, #{nonDestructive}, #{requiresSampling}, #{mainOutputs})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabInstrument instrument);

    @Update("UPDATE lab_instruments SET name=#{name}, image=#{image}, scope=#{scope}, location=#{location}, model=#{model}, conditions=#{conditions}, method=#{method}, method_name=#{methodName}, applicable_materials=#{applicableMaterials}, research_purposes=#{researchPurposes}, non_destructive=#{nonDestructive}, requires_sampling=#{requiresSampling}, main_outputs=#{mainOutputs} WHERE id=#{id}")
    void update(LabInstrument instrument);

    @Delete("DELETE FROM lab_instruments WHERE id = #{id}")
    void delete(Integer id);
}
