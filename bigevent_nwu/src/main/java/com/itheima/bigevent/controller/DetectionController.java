package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.DetectionAnalysis;
import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/detection")
@CrossOrigin
public class DetectionController {

    @Autowired
    private DetectionMapper mapper;

    @GetMapping
    public Result<List<DetectionAnalysis>> list() { return Result.success(mapper.list()); }

    @PostMapping
    public Result add(@RequestBody DetectionAnalysis d) {
        d.setCreateTime(LocalDateTime.now()); d.setUpdateTime(LocalDateTime.now());
        mapper.insert(d); return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody DetectionAnalysis d) {
        d.setId(id); d.setUpdateTime(LocalDateTime.now()); mapper.update(d); return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) { mapper.delete(id); return Result.success(); }

    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) { mapper.batchDelete(ids); return Result.success(); }

    @Mapper
    public interface DetectionMapper {
        @Select("SELECT * FROM detection_analysis ORDER BY CAST(serial_number AS UNSIGNED) ASC")
        List<DetectionAnalysis> list();

        @Insert("INSERT INTO detection_analysis (serial_number,artifact_code,artifact_name,excavation_relic,sample_position,sample_material,sample_status,sample_quantity,sample_method,purpose,storage_location,departure_time,destination,sample_photo,analysis_data,analysis_report,manager,sampler,notes,create_time,update_time) VALUES (#{serialNumber},#{artifactCode},#{artifactName},#{excavationRelic},#{samplePosition},#{sampleMaterial},#{sampleStatus},#{sampleQuantity},#{sampleMethod},#{purpose},#{storageLocation},#{departureTime},#{destination},#{samplePhoto},#{analysisData},#{analysisReport},#{manager},#{sampler},#{notes},#{createTime},#{updateTime})")
        void insert(DetectionAnalysis d);

        @Update("UPDATE detection_analysis SET serial_number=#{serialNumber},artifact_code=#{artifactCode},artifact_name=#{artifactName},excavation_relic=#{excavationRelic},sample_position=#{samplePosition},sample_material=#{sampleMaterial},sample_status=#{sampleStatus},sample_quantity=#{sampleQuantity},sample_method=#{sampleMethod},purpose=#{purpose},storage_location=#{storageLocation},departure_time=#{departureTime},destination=#{destination},sample_photo=#{samplePhoto},analysis_data=#{analysisData},analysis_report=#{analysisReport},manager=#{manager},sampler=#{sampler},notes=#{notes},update_time=#{updateTime} WHERE id=#{id}")
        void update(DetectionAnalysis d);

        @Delete("DELETE FROM detection_analysis WHERE id=#{id}")
        void delete(Integer id);

        @Delete("<script>DELETE FROM detection_analysis WHERE id IN <foreach collection='list' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
        void batchDelete(List<Integer> ids);
    }
}
