package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.ExperimentResult;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExperimentResultMapper {

    @Select("SELECT * FROM experiment_results WHERE detection_id = #{detectionId} ORDER BY id")
    List<ExperimentResult> listByDetection(Integer detectionId);

    @Insert("INSERT INTO experiment_results (detection_id, experiment_name, status, result_data, images, attachments, notes) VALUES (#{detectionId}, #{experimentName}, #{status}, #{resultData}, #{images}, #{attachments}, #{notes})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperimentResult r);

    @Update("UPDATE experiment_results SET status=#{status}, result_data=#{resultData}, images=#{images}, attachments=#{attachments}, notes=#{notes}, update_time=NOW() WHERE id=#{id}")
    void update(ExperimentResult r);

    @Delete("DELETE FROM experiment_results WHERE detection_id = #{detectionId}")
    void deleteByDetectionId(Integer detectionId);
}
