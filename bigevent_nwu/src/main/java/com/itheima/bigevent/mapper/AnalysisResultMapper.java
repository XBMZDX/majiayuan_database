package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.AnalysisResult;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AnalysisResultMapper {

    @Select("SELECT ar.*,d.sample_status AS sample_status FROM analysis_results ar LEFT JOIN detection_analysis d ON d.id=ar.detection_id ORDER BY ar.id DESC")
    List<AnalysisResult> list();

    @Select("SELECT ar.*,d.sample_status AS sample_status FROM analysis_results ar LEFT JOIN detection_analysis d ON d.id=ar.detection_id WHERE ar.id=#{id}")
    AnalysisResult findById(Integer id);

    @Insert("INSERT INTO analysis_results (detection_id,artifact_code,artifact_name,sample_photo,experiment_method) VALUES (#{detectionId},#{artifactCode},#{artifactName},#{samplePhoto},#{experimentMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AnalysisResult r);

    @Update("UPDATE analysis_results SET detection_purpose=#{detectionPurpose},instrument_model=#{instrumentModel},test_params=#{testParams},update_time=NOW() WHERE id=#{id}")
    void update(AnalysisResult r);

    @Delete("DELETE FROM analysis_results WHERE id = #{id}")
    void delete(Integer id);

    @Select("SELECT * FROM analysis_results WHERE detection_id = #{detectionId}")
    List<AnalysisResult> listByDetectionId(Integer detectionId);

    @Delete("DELETE FROM analysis_results WHERE detection_id = #{detectionId}")
    void deleteByDetectionId(Integer detectionId);

    @Delete("DELETE FROM analysis_results WHERE detection_id = #{detectionId} AND experiment_method = #{method}")
    void deleteByDetectionIdAndMethod(@Param("detectionId") Integer detectionId, @Param("method") String method);

    @Update("UPDATE analysis_results SET detection_id=#{targetDetectionId} WHERE detection_id=#{sourceDetectionId} AND experiment_method=#{method}")
    void moveToDetection(@Param("sourceDetectionId") Integer sourceDetectionId, @Param("targetDetectionId") Integer targetDetectionId, @Param("method") String method);
}
