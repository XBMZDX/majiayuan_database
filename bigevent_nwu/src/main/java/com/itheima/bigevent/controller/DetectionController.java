package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.AnalysisResult;
import com.itheima.bigevent.pojo.DetectionAnalysis;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.AnalysisResultMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/admin/detection")
@CrossOrigin
public class DetectionController {

    @Autowired
    private DetectionMapper mapper;
    @Autowired
    private AnalysisResultMapper resultMapper;

    @GetMapping
    public Result<List<DetectionAnalysis>> list() { return Result.success(mapper.list()); }

    @PostMapping
    public Result add(@RequestBody DetectionAnalysis d) {
        d.setCreateTime(LocalDateTime.now()); d.setUpdateTime(LocalDateTime.now());
        mapper.insert(d);
        // 拆分目的，每个实验名创建一条分析结果
        String purpose = d.getPurpose();
        if (purpose != null && !purpose.isEmpty()) {
            for (String name : purpose.split("/")) {
                String expName = name.trim();
                if (expName.isEmpty()) continue;
                AnalysisResult r = new AnalysisResult();
                r.setDetectionId(d.getId());
                r.setArtifactCode(d.getArtifactCode());
                r.setArtifactName(d.getArtifactName());
                r.setSamplePhoto(d.getSamplePhoto());
                r.setExperimentMethod(expName);
                resultMapper.insert(r);
            }
        } else {
            AnalysisResult r = new AnalysisResult();
            r.setDetectionId(d.getId());
            r.setArtifactCode(d.getArtifactCode());
            r.setArtifactName(d.getArtifactName());
            r.setSamplePhoto(d.getSamplePhoto());
            resultMapper.insert(r);
        }
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody DetectionAnalysis d) {
        d.setId(id); d.setUpdateTime(LocalDateTime.now()); mapper.update(d);
        // 同步联动字段：先保存用户数据，删旧建新，再恢复
        Map<String, AnalysisResult> saved = new HashMap<>();
        for (AnalysisResult old : resultMapper.listByDetectionId(id)) {
            String k = old.getExperimentMethod() != null ? old.getExperimentMethod() : "";
            saved.put(k, old);
        }
        resultMapper.deleteByDetectionId(id);
        String purpose = d.getPurpose();
        if (purpose != null && !purpose.isEmpty()) {
            for (String name : purpose.split("/")) {
                String expName = name.trim();
                if (expName.isEmpty()) continue;
                AnalysisResult r = new AnalysisResult();
                r.setDetectionId(id);
                r.setArtifactCode(d.getArtifactCode());
                r.setArtifactName(d.getArtifactName());
                r.setSamplePhoto(d.getSamplePhoto());
                r.setExperimentMethod(expName);
                // 恢复用户数据
                AnalysisResult savedRow = saved.get(expName);
                if (savedRow != null) {
                    r.setDetectionPurpose(savedRow.getDetectionPurpose());
                    r.setInstrumentModel(savedRow.getInstrumentModel());
                    r.setTestParams(savedRow.getTestParams());
                }
                resultMapper.insert(r);
            }
        } else {
            AnalysisResult r = new AnalysisResult();
            r.setDetectionId(id);
            r.setArtifactCode(d.getArtifactCode());
            r.setArtifactName(d.getArtifactName());
            r.setSamplePhoto(d.getSamplePhoto());
            AnalysisResult savedRow = saved.get("");
            if (savedRow != null) {
                r.setDetectionPurpose(savedRow.getDetectionPurpose());
                r.setInstrumentModel(savedRow.getInstrumentModel());
                r.setTestParams(savedRow.getTestParams());
            }
            resultMapper.insert(r);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) { mapper.delete(id); return Result.success(); }

    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) { mapper.batchDelete(ids); return Result.success(); }

    @Mapper
    public interface DetectionMapper {
        @Select("SELECT * FROM detection_analysis ORDER BY CAST(serial_number AS UNSIGNED) ASC")
        List<DetectionAnalysis> list();

        @Insert("INSERT INTO detection_analysis (serial_number,artifact_code,artifact_name,excavation_relic,sample_position,sample_material,sample_status,sample_quantity,sample_method,purpose,instrument_name,instrument_model,test_params,storage_location,departure_time,destination,sample_photo,analysis_data,analysis_report,manager,sampler,notes,create_time,update_time) VALUES (#{serialNumber},#{artifactCode},#{artifactName},#{excavationRelic},#{samplePosition},#{sampleMaterial},#{sampleStatus},#{sampleQuantity},#{sampleMethod},#{purpose},#{instrumentName},#{instrumentModel},#{testParams},#{storageLocation},#{departureTime},#{destination},#{samplePhoto},#{analysisData},#{analysisReport},#{manager},#{sampler},#{notes},#{createTime},#{updateTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        void insert(DetectionAnalysis d);

        @Update("UPDATE detection_analysis SET serial_number=#{serialNumber},artifact_code=#{artifactCode},artifact_name=#{artifactName},excavation_relic=#{excavationRelic},sample_position=#{samplePosition},sample_material=#{sampleMaterial},sample_status=#{sampleStatus},sample_quantity=#{sampleQuantity},sample_method=#{sampleMethod},purpose=#{purpose},instrument_name=#{instrumentName},instrument_model=#{instrumentModel},test_params=#{testParams},storage_location=#{storageLocation},departure_time=#{departureTime},destination=#{destination},sample_photo=#{samplePhoto},analysis_data=#{analysisData},analysis_report=#{analysisReport},manager=#{manager},sampler=#{sampler},notes=#{notes},update_time=#{updateTime} WHERE id=#{id}")
        void update(DetectionAnalysis d);

        @Delete("DELETE FROM detection_analysis WHERE id=#{id}")
        void delete(Integer id);

        @Delete("<script>DELETE FROM detection_analysis WHERE id IN <foreach collection='list' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
        void batchDelete(List<Integer> ids);
    }
}
