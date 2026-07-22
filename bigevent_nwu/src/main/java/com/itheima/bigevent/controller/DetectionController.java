package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.AnalysisResult;
import com.itheima.bigevent.pojo.DetectionAnalysis;
import com.itheima.bigevent.pojo.ExperimentResult;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.AnalysisResultMapper;
import com.itheima.bigevent.mapper.ExperimentResultMapper;
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
    @Autowired
    private ExperimentResultMapper expResultMapper;

    @GetMapping
    public Result<List<DetectionAnalysis>> list() { return Result.success(mapper.list()); }

    @PostMapping
    public Result add(@RequestBody DetectionAnalysis d) {
        normalizeArtifactCode(d);
        d.setCreateTime(LocalDateTime.now()); d.setUpdateTime(LocalDateTime.now());
        mapper.insert(d);
        // 拆分目的，每个实验名创建一条分析结果 + 一条实验结果
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
                // 同步创建实验结果行
                ExperimentResult er = new ExperimentResult();
                er.setDetectionId(d.getId());
                er.setExperimentName(expName);
                er.setStatus("待检测");
                expResultMapper.insert(er);
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
        normalizeArtifactCode(d);
        d.setId(id); d.setUpdateTime(LocalDateTime.now()); mapper.update(d);
        // 1) 保存 analysis_results 用户数据
        Map<String, AnalysisResult> savedAr = new HashMap<>();
        for (AnalysisResult old : resultMapper.listByDetectionId(id)) {
            String k = old.getExperimentMethod() != null ? old.getExperimentMethod() : "";
            savedAr.put(k, old);
        }
        // 2) 保存 experiment_results 数据
        Map<String, ExperimentResult> savedEr = new HashMap<>();
        for (ExperimentResult old : expResultMapper.listByDetection(id)) {
            String k = old.getExperimentName() != null ? old.getExperimentName() : "";
            savedEr.put(k, old);
        }
        // 3) 删除旧行
        resultMapper.deleteByDetectionId(id);
        expResultMapper.deleteByDetectionId(id);
        // 4) 按新 purpose 重建 analysis_results + experiment_results
        String purpose = d.getPurpose();
        if (purpose != null && !purpose.isEmpty()) {
            for (String name : purpose.split("/")) {
                String expName = name.trim();
                if (expName.isEmpty()) continue;
                // 重建 analysis_result
                AnalysisResult r = new AnalysisResult();
                r.setDetectionId(id);
                r.setArtifactCode(d.getArtifactCode());
                r.setArtifactName(d.getArtifactName());
                r.setSamplePhoto(d.getSamplePhoto());
                r.setExperimentMethod(expName);
                AnalysisResult savedArRow = savedAr.get(expName);
                if (savedArRow != null) {
                    r.setDetectionPurpose(savedArRow.getDetectionPurpose());
                    r.setInstrumentModel(savedArRow.getInstrumentModel());
                    r.setTestParams(savedArRow.getTestParams());
                }
                resultMapper.insert(r);
                // 重建 experiment_result
                ExperimentResult er = new ExperimentResult();
                er.setDetectionId(id);
                er.setExperimentName(expName);
                ExperimentResult savedErRow = savedEr.get(expName);
                if (savedErRow != null) {
                    er.setStatus(savedErRow.getStatus());
                    er.setResultData(savedErRow.getResultData());
                    er.setImages(savedErRow.getImages());
                    er.setAttachments(savedErRow.getAttachments());
                    er.setNotes(savedErRow.getNotes());
                } else {
                    er.setStatus("待检测");
                }
                expResultMapper.insert(er);
            }
        } else {
            AnalysisResult r = new AnalysisResult();
            r.setDetectionId(id);
            r.setArtifactCode(d.getArtifactCode());
            r.setArtifactName(d.getArtifactName());
            r.setSamplePhoto(d.getSamplePhoto());
            AnalysisResult savedArRow = savedAr.get("");
            if (savedArRow != null) {
                r.setDetectionPurpose(savedArRow.getDetectionPurpose());
                r.setInstrumentModel(savedArRow.getInstrumentModel());
                r.setTestParams(savedArRow.getTestParams());
            }
            resultMapper.insert(r);
            ExperimentResult er = new ExperimentResult();
            er.setDetectionId(id);
            ExperimentResult savedErRow = savedEr.get("");
            if (savedErRow != null) {
                er.setStatus(savedErRow.getStatus());
                er.setResultData(savedErRow.getResultData());
                er.setImages(savedErRow.getImages());
                er.setAttachments(savedErRow.getAttachments());
                er.setNotes(savedErRow.getNotes());
            } else { er.setStatus("待检测"); }
            expResultMapper.insert(er);
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

    private void normalizeArtifactCode(DetectionAnalysis d) {
        if (d != null && d.getArtifactCode() != null) {
            d.setArtifactCode(d.getArtifactCode().trim().replace('：', ':'));
        }
    }
}
