package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.AnalysisResult;
import com.itheima.bigevent.pojo.DetectionAnalysis;
import com.itheima.bigevent.pojo.ExperimentResult;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.AnalysisResultMapper;
import com.itheima.bigevent.mapper.ExperimentResultMapper;
import com.itheima.bigevent.service.DetectionArtifactOverviewService;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private DetectionArtifactOverviewService overviewService;

    @GetMapping
    public Result<List<DetectionAnalysis>> list() { return Result.success(mapper.list()); }

    @GetMapping("/artifact-overview")
    public Result<Map<String, Object>> artifactOverview() {
        return Result.success(overviewService.getOverview());
    }

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

    /**
     * 以文物为维度维护检测方法。保留未变方法及其结果，只为新增/移除的方法创建或删除数据。
     */
    @PutMapping("/artifact-methods")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> updateArtifactMethods(@RequestBody Map<String, Object> body) {
        String artifactCode = text(body.get("artifactCode"));
        String artifactName = text(body.get("artifactName"));
        if (artifactCode.isBlank() && artifactName.isBlank()) return Result.error("文物编号和文物名称不能同时为空");

        Set<String> desiredMethods = methodSet(body.get("methods"));
        List<DetectionAnalysis> detections = mapper.list().stream()
            .filter(item -> sameArtifact(artifactCode, artifactName, item))
            .toList();
        Set<String> existingMethods = new LinkedHashSet<>();
        int removedMethodCount = 0;

        for (DetectionAnalysis detection : detections) {
            Set<String> originalMethods = methodSet(detection.getPurpose());
            if (originalMethods.isEmpty()) continue;
            existingMethods.addAll(originalMethods);

            Set<String> removedMethods = new LinkedHashSet<>(originalMethods);
            removedMethods.removeAll(desiredMethods);
            Set<String> remainingMethods = new LinkedHashSet<>(originalMethods);
            remainingMethods.retainAll(desiredMethods);

            if (remainingMethods.isEmpty()) {
                resultMapper.deleteByDetectionId(detection.getId());
                expResultMapper.deleteByDetectionId(detection.getId());
                mapper.delete(detection.getId());
            } else if (!removedMethods.isEmpty()) {
                for (String method : removedMethods) {
                    resultMapper.deleteByDetectionIdAndMethod(detection.getId(), method);
                    expResultMapper.deleteByDetectionIdAndMethod(detection.getId(), method);
                }
                detection.setPurpose(String.join("/", remainingMethods));
                detection.setUpdateTime(LocalDateTime.now());
                mapper.update(detection);
            }
            removedMethodCount += removedMethods.size();
        }

        Set<String> addedMethods = new LinkedHashSet<>(desiredMethods);
        addedMethods.removeAll(existingMethods);
        int nextSerial = mapper.list().size() + 1;
        for (String method : addedMethods) {
            DetectionAnalysis detection = new DetectionAnalysis();
            detection.setSerialNumber(String.valueOf(nextSerial++));
            detection.setArtifactCode(artifactCode.replace('：', ':'));
            detection.setArtifactName(artifactName);
            detection.setExcavationRelic(text(body.get("excavationRelic")));
            detection.setSampleMaterial(text(body.get("sampleMaterial")));
            detection.setSampleStatus("待检测");
            detection.setPurpose(method);
            detection.setCreateTime(LocalDateTime.now());
            detection.setUpdateTime(LocalDateTime.now());
            mapper.insert(detection);
            createInitialResults(detection);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("addedMethodCount", addedMethods.size());
        result.put("removedMethodCount", removedMethodCount);
        result.put("methods", desiredMethods);
        return Result.success(result);
    }

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

    private void createInitialResults(DetectionAnalysis detection) {
        for (String method : methodSet(detection.getPurpose())) {
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setDetectionId(detection.getId());
            analysisResult.setArtifactCode(detection.getArtifactCode());
            analysisResult.setArtifactName(detection.getArtifactName());
            analysisResult.setSamplePhoto(detection.getSamplePhoto());
            analysisResult.setExperimentMethod(method);
            resultMapper.insert(analysisResult);

            ExperimentResult experimentResult = new ExperimentResult();
            experimentResult.setDetectionId(detection.getId());
            experimentResult.setExperimentName(method);
            experimentResult.setStatus("待检测");
            expResultMapper.insert(experimentResult);
        }
    }

    private boolean sameArtifact(String artifactCode, String artifactName, DetectionAnalysis detection) {
        String selectedCode = normalizeCode(artifactCode);
        if (!selectedCode.isBlank()) return selectedCode.equals(normalizeCode(detection.getArtifactCode()));
        String selectedName = text(artifactName).toLowerCase(Locale.ROOT);
        return !selectedName.isBlank() && selectedName.equals(text(detection.getArtifactName()).toLowerCase(Locale.ROOT));
    }

    private Set<String> methodSet(Object value) {
        Set<String> methods = new LinkedHashSet<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) addMethod(methods, item);
        } else {
            for (String item : text(value).split("/")) addMethod(methods, item);
        }
        return methods;
    }

    private void addMethod(Set<String> methods, Object value) {
        String method = text(value).trim();
        if (!method.isBlank()) methods.add(method);
    }

    private String normalizeCode(Object value) {
        return text(value).replace('：', ':').replaceAll("[\\s\\-_ *]", "");
    }

    private String text(Object value) { return value == null ? "" : value.toString().trim(); }
}
