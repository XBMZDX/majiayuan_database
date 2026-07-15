package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.AnalysisResult;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AnalysisResultMapper {

    @Select("SELECT * FROM analysis_results ORDER BY id DESC")
    List<AnalysisResult> list();

    @Insert("INSERT INTO analysis_results (detection_id,artifact_code,artifact_name,sample_photo,experiment_method) VALUES (#{detectionId},#{artifactCode},#{artifactName},#{samplePhoto},#{experimentMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AnalysisResult r);

    @Update("UPDATE analysis_results SET detection_purpose=#{detectionPurpose},instrument_model=#{instrumentModel},test_params=#{testParams},update_time=NOW() WHERE id=#{id}")
    void update(AnalysisResult r);

    @Delete("DELETE FROM analysis_results WHERE id = #{id}")
    void delete(Integer id);

    @Delete("DELETE FROM analysis_results WHERE detection_id = #{detectionId}")
    void deleteByDetectionId(Integer detectionId);
}
