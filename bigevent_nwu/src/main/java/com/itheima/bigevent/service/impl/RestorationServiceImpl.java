package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RestorationServiceImpl implements RestorationService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;
    private final ProcessService processService;
    private final ComparisonService comparisonService;
    private final ArchiveService archiveService;

    public RestorationServiceImpl(JdbcTemplate jdbc, ObjectMapper objectMapper,
                                  ProcessService processService, ComparisonService comparisonService,
                                  ArchiveService archiveService) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.objectMapper = objectMapper;
        this.processService = processService;
        this.comparisonService = comparisonService;
        this.archiveService = archiveService;
    }

    @PostConstruct
    public void ensureVisualizationColumns() {
        addColumnIfMissing("conservation_restoration_part", "model_object_name",
            "ALTER TABLE conservation_restoration_part ADD COLUMN model_object_name VARCHAR(255)");
        addColumnIfMissing("conservation_restoration_part", "layer_visible",
            "ALTER TABLE conservation_restoration_part ADD COLUMN layer_visible TINYINT DEFAULT 1");
        addColumnIfMissing("conservation_restoration_part", "annotation_position_json",
            "ALTER TABLE conservation_restoration_part ADD COLUMN annotation_position_json JSON");
    }

    @Override
    public Map<String, Object> getWorkbench(Long projectId) {
        Map<String, Object> process = processService.getWorkbench(projectId);
        Map<String, Object> comparison = comparisonService.getWorkbench(projectId);
        Map<String, Object> archiveWorkspace = map(process.get("archiveWorkspace"));
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("archive", process.get("archive"));
        context.put("archiveWorkspace", archiveWorkspace);
        context.put("process", process.get("processRecord"));
        context.put("steps", process.getOrDefault("steps", List.of()));
        context.put("comparisons", comparison.getOrDefault("groups", List.of()));
        context.put("diseases", archiveWorkspace == null ? List.of()
            : archiveWorkspace.getOrDefault("diseaseRecords", List.of()));
        context.put("detections", archiveWorkspace == null ? List.of()
            : archiveWorkspace.getOrDefault("detectionReferences", List.of()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", process.get("project"));
        result.put("context", context);
        result.put("results", loadResults(projectId));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> saveWorkbench(Long projectId, List<Map<String, Object>> results) {
        Map<String, Object> project = map(processService.getWorkbench(projectId).get("project"));
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        Set<Long> resultIds = new HashSet<>();
        Set<Long> partIds = new HashSet<>();
        Set<Long> sourceIds = new HashSet<>();
        Set<Long> mediaIds = new HashSet<>();
        Set<Long> modelIds = new HashSet<>();
        Set<Long> versionIds = new HashSet<>();
        for (Map<String, Object> result : results) {
            prepareIds(result, resultIds, partIds, sourceIds, mediaIds, modelIds, versionIds);
            saveResult(projectId, project, result);
        }
        if (!results.isEmpty()) {
            jdbc.update("UPDATE conservation_project SET current_stage='restoration' WHERE id=? AND current_stage<>'completed'",
                projectId);
        }
        syncArchive(projectId, results);
        return getWorkbench(projectId);
    }

    @Override
    @Transactional
    public void deleteResult(Long resultId) {
        Map<String, Object> result = one("""
            SELECT project_id AS projectId
            FROM conservation_restoration_result WHERE id=? AND deleted=0
            """, resultId);
        if (result == null) return;
        jdbc.update("UPDATE conservation_restoration_result SET deleted=1 WHERE id=?", resultId);
        Long projectId = longValue(result.get("projectId"));
        syncArchive(projectId, loadResults(projectId));
    }

    @Override
    @Transactional
    public Map<String, Object> uploadMedia(Long resultId, MultipartFile file, Map<String, String> metadata) {
        requireResult(resultId);
        validateFile(file, 50L * 1024 * 1024, Set.of("image/", "video/"), "成果媒体仅支持图片或视频");
        try {
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_restoration_media
                (result_id,source_business_type,source_business_id,media_stage,media_type,
                 original_name,content_type,file_size,file_data,title,description,is_primary,
                 selected_for_archive,selected_as_monitoring_baseline,sort_order)
                VALUES (:resultId,'restoration',:resultId,:mediaStage,:mediaType,
                 :fileName,:contentType,:fileSize,:fileData,:title,:description,:isPrimary,
                 :selectedForArchive,:selectedAsMonitoringBaseline,:sortOrder)
                """, new MapSqlParameterSource("resultId", resultId)
                .addValue("mediaStage", metadata.getOrDefault("mediaStage", "final"))
                .addValue("mediaType", metadata.getOrDefault("mediaType", "image"))
                .addValue("fileName", file.getOriginalFilename()).addValue("contentType", file.getContentType())
                .addValue("fileSize", file.getSize()).addValue("fileData", file.getBytes())
                .addValue("title", metadata.getOrDefault("title", file.getOriginalFilename()))
                .addValue("description", metadata.get("description"))
                .addValue("isPrimary", bool(metadata.get("isPrimary")))
                .addValue("selectedForArchive", bool(metadata.get("selectedForArchive")))
                .addValue("selectedAsMonitoringBaseline", bool(metadata.get("selectedAsMonitoringBaseline")))
                .addValue("sortOrder", integer(metadata.get("sortOrder"))), key, new String[]{"id"});
            return mediaMetadata(key.getKey().longValue());
        } catch (Exception e) {
            throw new IllegalStateException("复原成果媒体写入MySQL失败", e);
        }
    }

    @Override
    public Map<String, Object> getMedia(Long mediaId) {
        Map<String, Object> media = one("""
            SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData,
                   source_business_type AS sourceBusinessType,source_media_id AS sourceMediaId
            FROM conservation_restoration_media WHERE id=?
            """, mediaId);
        if (media == null || media.get("fileData") != null) return media;
        Long sourceId = longValue(media.get("sourceMediaId"));
        if (sourceId == null) return media;
        return switch (text(media.get("sourceBusinessType"))) {
            case "process_step" -> one("""
                SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData
                FROM conservation_process_media WHERE id=?
                """, sourceId);
            case "comparison" -> one("""
                SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData
                FROM conservation_comparison_media WHERE id=?
                """, sourceId);
            default -> media;
        };
    }

    @Override
    public void deleteMedia(Long mediaId) {
        jdbc.update("DELETE FROM conservation_restoration_media WHERE id=?", mediaId);
    }

    @Override
    @Transactional
    public Map<String, Object> uploadModel(Long resultId, MultipartFile file, Map<String, String> metadata) {
        requireResult(resultId);
        validateFile(file, 500L * 1024 * 1024,
            Set.of("model/", "application/octet-stream", "application/zip", "application/gltf"),
            "不支持的三维模型文件");
        try {
            String name = Objects.toString(file.getOriginalFilename(), "");
            String format = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toUpperCase() : "";
            if (!Set.of("GLB", "GLTF", "OBJ", "FBX", "STL", "PLY").contains(format)) {
                throw new IllegalArgumentException("WebGL查看器仅支持GLB、GLTF、OBJ、FBX、STL和PLY模型");
            }
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_restoration_model
                (result_id,model_name,model_type,original_name,content_type,file_size,file_data,
                 file_format,scale_unit,coordinate_system,polygon_count,texture_count,model_stage,
                 model_description,supports_layer,supports_annotation,is_primary)
                VALUES (:resultId,:modelName,:modelType,:fileName,:contentType,:fileSize,:fileData,
                 :fileFormat,:scaleUnit,:coordinateSystem,:polygonCount,:textureCount,:modelStage,
                 :modelDescription,:supportsLayer,:supportsAnnotation,:isPrimary)
                """, new MapSqlParameterSource("resultId", resultId)
                .addValue("modelName", metadata.getOrDefault("modelName", name))
                .addValue("modelType", metadata.getOrDefault("modelType", "restored"))
                .addValue("fileName", name).addValue("contentType", file.getContentType())
                .addValue("fileSize", file.getSize()).addValue("fileData", file.getBytes())
                .addValue("fileFormat", metadata.getOrDefault("fileFormat", format))
                .addValue("scaleUnit", metadata.get("scaleUnit"))
                .addValue("coordinateSystem", metadata.get("coordinateSystem"))
                .addValue("polygonCount", longValue(metadata.get("polygonCount")))
                .addValue("textureCount", integer(metadata.get("textureCount")))
                .addValue("modelStage", metadata.getOrDefault("modelStage", "draft"))
                .addValue("modelDescription", metadata.get("modelDescription"))
                .addValue("supportsLayer", bool(metadata.get("supportsLayer")))
                .addValue("supportsAnnotation", bool(metadata.get("supportsAnnotation")))
                .addValue("isPrimary", bool(metadata.get("isPrimary"))), key, new String[]{"id"});
            return modelMetadata(key.getKey().longValue());
        } catch (Exception e) {
            throw new IllegalStateException("三维模型写入MySQL失败", e);
        }
    }

    @Override
    public Map<String, Object> getModel(Long modelId) {
        return one("""
            SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData
            FROM conservation_restoration_model WHERE id=?
            """, modelId);
    }

    @Override
    public void deleteModel(Long modelId) {
        jdbc.update("DELETE FROM conservation_restoration_model WHERE id=?", modelId);
    }

    @Override
    public Map<String, Object> getVersion(Long resultId, Long versionId) {
        Map<String, Object> version = one("""
            SELECT id,result_id AS resultId,version_no AS versionNo,version_name AS versionName,
                   version_type AS versionType,change_description AS changeDescription,
                   evidence_level AS evidenceLevel,confidence_level AS confidenceLevel,
                   is_current AS isCurrent,snapshot_json AS snapshotJson,creator,create_time AS createTime
            FROM conservation_restoration_version WHERE id=? AND result_id=?
            """, versionId, resultId);
        if (version == null) throw new IllegalArgumentException("复原成果历史版本不存在");
        version.put("snapshot", jsonMap(version.remove("snapshotJson")));
        return version;
    }

    @Override
    @Transactional
    public Map<String, Object> restoreVersion(Long resultId, Long versionId) {
        Map<String, Object> version = getVersion(resultId, versionId);
        Map<String, Object> historical = map(version.get("snapshot"));
        if (historical == null || historical.isEmpty()) throw new IllegalStateException("该版本没有可恢复的历史快照");
        Map<String, Object> identity = one("""
            SELECT project_id AS projectId FROM conservation_restoration_result
            WHERE id=? AND deleted=0
            """, resultId);
        if (identity == null) throw new IllegalArgumentException("复原成果不存在");
        Long projectId = longValue(identity.get("projectId"));
        List<Map<String, Object>> results = loadResults(projectId);
        Map<String, Object> result = results.stream()
            .filter(item -> Objects.equals(longValue(item.get("id")), resultId))
            .findFirst().orElseThrow(() -> new IllegalArgumentException("复原成果不存在"));
        applySnapshot(result, historical);
        for (Map<String, Object> item : list(result.get("versions"))) {
            boolean current = Objects.equals(longValue(item.get("id")), versionId);
            item.put("isCurrent", current);
            item.put("isRecommended", false);
        }
        result.put("currentVersion", version.get("versionNo"));
        Set<Long> restoredPartIds = ids(list(result.get("parts")).stream()
            .map(part -> part.get("id")).toList());
        for (Map<String, Object> media : list(result.get("media"))) {
            Long partId = longValue(media.get("restorationPartId"));
            if (partId != null && !restoredPartIds.contains(partId)) media.put("restorationPartId", null);
        }
        Map<String, Object> project = map(processService.getWorkbench(projectId).get("project"));
        saveResult(projectId, project, result);
        List<Map<String, Object>> restoredResults = loadResults(projectId);
        syncArchive(projectId, restoredResults);
        return getWorkbench(projectId);
    }

    private void saveResult(Long projectId, Map<String, Object> project, Map<String, Object> result) {
        Long id = longValue(result.get("id"));
        Map<String, Object> evaluation = map(result.get("evaluation"));
        Map<String, Object> monitoring = map(result.get("monitoring"));
        Long archiveId = nullableLong("SELECT id FROM conservation_archive WHERE project_id=? AND deleted=0", projectId);
        namedJdbc.update("""
            INSERT INTO conservation_restoration_result
            (id,project_id,artifact_id,archive_id,process_id,comparison_id,result_code,result_name,
             restoration_type,restoration_category,target_part,restoration_scope,restoration_purpose,
             basis_summary,method_summary,result_summary,uncertainty_summary,evidence_level,confidence_level,
             result_status,current_version,completion_rate,overall_score,evaluation_conclusion,final_conclusion,
             selected_for_archive,recommended_result,requires_monitoring,principal,participant_names,
             start_date,completion_date,monitoring_indicators,monitoring_cycle,monitoring_baseline_id,
             warning_conditions,monitoring_note,step_ids_json,comparison_ids_json,disease_ids_json,
             detection_ids_json,method_parameters_json,evaluation_json,deleted)
            VALUES (:id,:projectId,:artifactId,:archiveId,:processId,:comparisonId,:resultCode,:resultName,
             :restorationType,:restorationCategory,:targetPart,:restorationScope,:restorationPurpose,
             :basisSummary,:methodSummary,:resultSummary,:uncertaintySummary,:evidenceLevel,:confidenceLevel,
             :resultStatus,:currentVersion,:completionRate,:overallScore,:evaluationConclusion,:finalConclusion,
             :selectedForArchive,:recommendedResult,:requiresMonitoring,:principal,:participantNames,
             :startDate,:completionDate,:monitoringIndicators,:monitoringCycle,:monitoringBaselineId,
             :warningConditions,:monitoringNote,:stepIdsJson,:comparisonIdsJson,:diseaseIdsJson,
             :detectionIdsJson,:methodParametersJson,:evaluationJson,0)
            ON DUPLICATE KEY UPDATE result_code=VALUES(result_code),result_name=VALUES(result_name),
             restoration_type=VALUES(restoration_type),restoration_category=VALUES(restoration_category),
             target_part=VALUES(target_part),restoration_scope=VALUES(restoration_scope),
             restoration_purpose=VALUES(restoration_purpose),basis_summary=VALUES(basis_summary),
             method_summary=VALUES(method_summary),result_summary=VALUES(result_summary),
             uncertainty_summary=VALUES(uncertainty_summary),evidence_level=VALUES(evidence_level),
             confidence_level=VALUES(confidence_level),result_status=VALUES(result_status),
             current_version=VALUES(current_version),completion_rate=VALUES(completion_rate),
             overall_score=VALUES(overall_score),evaluation_conclusion=VALUES(evaluation_conclusion),
             final_conclusion=VALUES(final_conclusion),selected_for_archive=VALUES(selected_for_archive),
             recommended_result=VALUES(recommended_result),requires_monitoring=VALUES(requires_monitoring),
             principal=VALUES(principal),participant_names=VALUES(participant_names),
             start_date=VALUES(start_date),completion_date=VALUES(completion_date),
             monitoring_indicators=VALUES(monitoring_indicators),monitoring_cycle=VALUES(monitoring_cycle),
             monitoring_baseline_id=VALUES(monitoring_baseline_id),warning_conditions=VALUES(warning_conditions),
             monitoring_note=VALUES(monitoring_note),step_ids_json=VALUES(step_ids_json),
             comparison_ids_json=VALUES(comparison_ids_json),disease_ids_json=VALUES(disease_ids_json),
             detection_ids_json=VALUES(detection_ids_json),method_parameters_json=VALUES(method_parameters_json),
             evaluation_json=VALUES(evaluation_json),deleted=0
            """, params(result).addValue("id", id).addValue("projectId", projectId)
            .addValue("artifactId", project.get("artifactId")).addValue("archiveId", archiveId)
            .addValue("processId", result.get("processId"))
            .addValue("comparisonId", firstLong(result.get("comparisonIds")))
            .addValue("evaluationConclusion", evaluation == null ? null : evaluation.get("finalConclusion"))
            .addValue("finalConclusion", evaluation == null ? null : evaluation.get("finalConclusion"))
            .addValue("startDate", date(result.get("startDate"))).addValue("completionDate", date(result.get("completionDate")))
            .addValue("monitoringIndicators", monitoring == null ? null : monitoring.get("indicators"))
            .addValue("monitoringCycle", monitoring == null ? null : monitoring.get("cycle"))
            .addValue("monitoringBaselineId", monitoring == null ? null : monitoring.get("baselineMediaId"))
            .addValue("warningConditions", monitoring == null ? null : monitoring.get("warningConditions"))
            .addValue("monitoringNote", monitoring == null ? null : monitoring.get("note"))
            .addValue("stepIdsJson", json(result.getOrDefault("stepIds", List.of())))
            .addValue("comparisonIdsJson", json(result.getOrDefault("comparisonIds", List.of())))
            .addValue("diseaseIdsJson", json(result.getOrDefault("diseaseIds", List.of())))
            .addValue("detectionIdsJson", json(result.getOrDefault("detectionIds", List.of())))
            .addValue("methodParametersJson", json(result.getOrDefault("methodParameters", List.of())))
            .addValue("evaluationJson", json(result.getOrDefault("evaluation", Map.of()))));

        Map<Long, Map<String, Object>> oldMedia = byId(jdbc.query("""
            SELECT id,source_media_id AS sourceMediaId,source_business_type AS sourceBusinessType,
                   source_business_id AS sourceBusinessId,original_name AS fileName,content_type AS contentType,
                   file_size AS fileSize,file_data AS fileData FROM conservation_restoration_media WHERE result_id=?
            """, this::camelMap, id));
        Map<Long, Map<String, Object>> oldModels = byId(jdbc.query("""
            SELECT id,original_name AS fileName,content_type AS contentType,file_size AS fileSize,file_data AS fileData
            FROM conservation_restoration_model WHERE result_id=?
            """, this::camelMap, id));
        jdbc.update("DELETE FROM conservation_restoration_source WHERE result_id=?", id);
        jdbc.update("DELETE FROM conservation_restoration_part WHERE result_id=?", id);
        jdbc.update("DELETE FROM conservation_restoration_media WHERE result_id=?", id);
        jdbc.update("DELETE FROM conservation_restoration_model WHERE result_id=?", id);
        jdbc.update("DELETE FROM conservation_restoration_version WHERE result_id=?", id);
        for (Map<String, Object> part : list(result.get("parts"))) insertPart(id, part, monitoring);
        for (Map<String, Object> source : list(result.get("sources"))) insertSource(id, source);
        for (Map<String, Object> media : list(result.get("media"))) insertMedia(id, media, oldMedia.get(longValue(media.get("id"))));
        for (Map<String, Object> model : list(result.get("models"))) insertModel(id, model, oldModels.get(longValue(model.get("id"))));
        for (Map<String, Object> version : list(result.get("versions"))) insertVersion(id, version, result);
    }

    private void insertPart(Long resultId, Map<String, Object> part, Map<String, Object> monitoring) {
        namedJdbc.update("""
            INSERT INTO conservation_restoration_part
            (id,result_id,part_code,part_name,part_type,target_location,scope_description,material_name,
             technique,evidence_level,confidence_level,removable,reversible,reversible_description,
             distinguishable,display_style,annotation_text,percentage_value,sort_order,model_layer,
             model_object_name,layer_visible,annotation_position_json,selected_for_monitoring)
            VALUES (:id,:resultId,:partCode,:partName,:partType,:targetLocation,:scopeDescription,:materialName,
             :technique,:evidenceLevel,:confidenceLevel,:removable,:reversible,:reversibleDescription,
             :distinguishable,:displayStyle,:annotationText,:percentageValue,:sortOrder,:modelLayer,
             :modelObjectName,:layerVisible,:annotationPositionJson,:selectedForMonitoring)
            """, params(part).addValue("resultId", resultId).addValue("selectedForMonitoring",
            monitoring != null && ids(monitoring.get("partIds")).contains(longValue(part.get("id"))))
            .addValue("annotationPositionJson", part.get("annotationPosition") == null
                ? null : json(part.get("annotationPosition"))));
    }

    private void insertSource(Long resultId, Map<String, Object> source) {
        namedJdbc.update("""
            INSERT INTO conservation_restoration_source
            (id,result_id,part_id,source_type,source_title,business_type,business_id,support_description,
             reliability,evidence_level,file_name,file_url,sort_order)
            VALUES (:id,:resultId,:restorationPartId,:sourceType,:sourceTitle,:businessType,:businessId,
             :supportDescription,:reliability,:evidenceLevel,:fileName,:fileUrl,:sortOrder)
            """, params(source).addValue("resultId", resultId));
    }

    private void insertMedia(Long resultId, Map<String, Object> media, Map<String, Object> old) {
        Long sourceMediaId = longValue(media.get("sourceMediaId"));
        String sourceType = text(media.get("sourceBusinessType"));
        if (sourceMediaId != null && "process_step".equals(sourceType)
            && count("""
                SELECT COUNT(*) FROM conservation_process_media m
                JOIN conservation_restoration_result r ON r.id=?
                WHERE m.id=? AND m.project_id=r.project_id
                """, resultId, sourceMediaId) == 0) {
            throw new IllegalArgumentException("引用的修复过程影像不属于当前项目");
        }
        if (sourceMediaId != null && "comparison".equals(sourceType)
            && count("""
                SELECT COUNT(*) FROM conservation_comparison_media m
                JOIN conservation_comparison c ON c.id=m.comparison_id
                JOIN conservation_restoration_result r ON r.id=?
                WHERE m.id=? AND c.project_id=r.project_id
                """, resultId, sourceMediaId) == 0) {
            throw new IllegalArgumentException("引用的前后对比影像不属于当前项目");
        }
        namedJdbc.update("""
            INSERT INTO conservation_restoration_media
            (id,result_id,part_id,source_media_id,source_business_type,source_business_id,media_stage,
             media_type,original_name,content_type,file_size,file_data,title,description,is_primary,
             selected_for_archive,selected_as_monitoring_baseline,sort_order)
            VALUES (:id,:resultId,:restorationPartId,:sourceMediaId,:sourceBusinessType,:sourceBusinessId,
             :mediaStage,:mediaType,:fileName,:contentType,:fileSize,:fileData,:title,:description,
             :isPrimary,:selectedForArchive,:selectedAsMonitoringBaseline,:sortOrder)
            """, params(media).addValue("resultId", resultId)
            .addValue("contentType", old == null ? null : old.get("contentType"))
            .addValue("fileSize", old == null ? null : old.get("fileSize"))
            .addValue("fileData", old == null ? null : old.get("fileData"))
            .addValue("fileName", value(media.get("fileName"), old == null ? null : old.get("fileName"))));
    }

    private void insertModel(Long resultId, Map<String, Object> model, Map<String, Object> old) {
        namedJdbc.update("""
            INSERT INTO conservation_restoration_model
            (id,result_id,model_name,model_type,original_name,content_type,file_size,file_data,file_format,
             scale_unit,coordinate_system,polygon_count,texture_count,model_stage,model_description,
             supports_layer,supports_annotation,is_primary)
            VALUES (:id,:resultId,:modelName,:modelType,:fileName,:contentType,:fileSize,:fileData,:fileFormat,
             :scaleUnit,:coordinateSystem,:polygonCount,:textureCount,:modelStage,:modelDescription,
             :supportsLayer,:supportsAnnotation,:isPrimary)
            """, params(model).addValue("resultId", resultId)
            .addValue("contentType", old == null ? null : old.get("contentType"))
            .addValue("fileSize", old == null ? model.get("fileSize") : old.get("fileSize"))
            .addValue("fileData", old == null ? null : old.get("fileData"))
            .addValue("fileName", value(model.get("fileName"), old == null ? null : old.get("fileName"))));
    }

    private void insertVersion(Long resultId, Map<String, Object> version, Map<String, Object> result) {
        Object historicalSnapshot = version.get("snapshot");
        Object savedSnapshot = bool(version.get("isCurrent")) || historicalSnapshot == null
            ? snapshot(result)
            : historicalSnapshot;
        namedJdbc.update("""
            INSERT INTO conservation_restoration_version
            (id,result_id,version_no,version_name,version_type,change_description,evidence_level,
             confidence_level,is_current,is_recommended,archived,snapshot_json,creator)
            VALUES (:id,:resultId,:versionNo,:versionName,:versionType,:changeDescription,:evidenceLevel,
             :confidenceLevel,:isCurrent,:isRecommended,:archived,:snapshotJson,:creator)
            """, params(version).addValue("resultId", resultId)
            .addValue("isRecommended", false)
            .addValue("snapshotJson", json(savedSnapshot)));
    }

    private List<Map<String, Object>> loadResults(Long projectId) {
        List<Map<String, Object>> results = jdbc.query("""
            SELECT id,project_id AS projectId,artifact_id AS artifactId,archive_id AS archiveId,
                   process_id AS processId,result_code AS resultCode,result_name AS resultName,
                   restoration_type AS restorationType,restoration_category AS restorationCategory,
                   target_part AS targetPart,restoration_scope AS restorationScope,
                   restoration_purpose AS restorationPurpose,basis_summary AS basisSummary,
                   method_summary AS methodSummary,result_summary AS resultSummary,
                   uncertainty_summary AS uncertaintySummary,evidence_level AS evidenceLevel,
                   confidence_level AS confidenceLevel,result_status AS resultStatus,
                   current_version AS currentVersion,completion_rate AS completionRate,
                   overall_score AS overallScore,evaluation_conclusion AS evaluationConclusion,
                   selected_for_archive AS selectedForArchive,recommended_result AS recommendedResult,
                   requires_monitoring AS requiresMonitoring,principal,participant_names AS participantNames,
                   start_date AS startDate,completion_date AS completionDate,
                   monitoring_indicators AS monitoringIndicators,monitoring_cycle AS monitoringCycle,
                   monitoring_baseline_id AS monitoringBaselineId,warning_conditions AS warningConditions,
                   monitoring_note AS monitoringNote,step_ids_json AS stepIdsJson,
                   comparison_ids_json AS comparisonIdsJson,disease_ids_json AS diseaseIdsJson,
                   detection_ids_json AS detectionIdsJson,method_parameters_json AS methodParametersJson,
                   evaluation_json AS evaluationJson,update_time AS updateTime
            FROM conservation_restoration_result WHERE project_id=? AND deleted=0
            ORDER BY recommended_result DESC,update_time DESC,id DESC
            """, this::camelMap, projectId);
        for (Map<String, Object> result : results) hydrateResult(result);
        return results;
    }

    private void hydrateResult(Map<String, Object> result) {
        Long id = longValue(result.get("id"));
        result.put("stepIds", jsonList(result.remove("stepIdsJson")));
        result.put("comparisonIds", jsonList(result.remove("comparisonIdsJson")));
        result.put("diseaseIds", jsonList(result.remove("diseaseIdsJson")));
        result.put("detectionIds", jsonList(result.remove("detectionIdsJson")));
        result.put("methodParameters", jsonList(result.remove("methodParametersJson")));
        result.put("evaluation", jsonMap(result.remove("evaluationJson")));
        result.put("parts", jdbc.query("""
            SELECT id,part_code AS partCode,part_name AS partName,part_type AS partType,
                   target_location AS targetLocation,scope_description AS scopeDescription,
                   material_name AS materialName,technique,evidence_level AS evidenceLevel,
                   confidence_level AS confidenceLevel,removable,reversible,
                   reversible_description AS reversibleDescription,distinguishable,
                   display_style AS displayStyle,annotation_text AS annotationText,
                   percentage_value AS percentageValue,sort_order AS sortOrder,
                   model_layer AS modelLayer,model_object_name AS modelObjectName,
                   layer_visible AS layerVisible,annotation_position_json AS annotationPositionJson,
                   selected_for_monitoring AS selectedForMonitoring
            FROM conservation_restoration_part WHERE result_id=? ORDER BY sort_order,id
            """, this::camelMap, id));
        for (Map<String, Object> part : list(result.get("parts"))) {
            part.put("annotationPosition", jsonMap(part.remove("annotationPositionJson")));
        }
        result.put("sources", jdbc.query("""
            SELECT id,part_id AS restorationPartId,source_type AS sourceType,source_title AS sourceTitle,
                   business_type AS businessType,business_id AS businessId,
                   support_description AS supportDescription,reliability,evidence_level AS evidenceLevel,
                   file_name AS fileName,file_url AS fileUrl,sort_order AS sortOrder
            FROM conservation_restoration_source WHERE result_id=? ORDER BY sort_order,id
            """, this::camelMap, id));
        List<Map<String, Object>> media = jdbc.query("""
            SELECT id,part_id AS restorationPartId,source_media_id AS sourceMediaId,
                   source_business_type AS sourceBusinessType,source_business_id AS sourceBusinessId,
                   media_stage AS mediaStage,media_type AS mediaType,original_name AS fileName,
                   title,description,is_primary AS isPrimary,selected_for_archive AS selectedForArchive,
                   selected_as_monitoring_baseline AS selectedAsMonitoringBaseline,sort_order AS sortOrder,
                   CONCAT('/api/conservation/restoration-media/',id,'/content') AS fileUrl,
                   CONCAT('/api/conservation/restoration-media/',id,'/content') AS thumbnailUrl
            FROM conservation_restoration_media WHERE result_id=? ORDER BY sort_order,id
            """, this::camelMap, id);
        result.put("media", media);
        result.put("models", jdbc.query("""
            SELECT id,model_name AS modelName,model_type AS modelType,original_name AS fileName,
                   file_format AS fileFormat,file_size AS fileSize,scale_unit AS scaleUnit,
                   coordinate_system AS coordinateSystem,polygon_count AS polygonCount,
                   texture_count AS textureCount,model_stage AS modelStage,
                   model_description AS modelDescription,supports_layer AS supportsLayer,
                   supports_annotation AS supportsAnnotation,is_primary AS isPrimary,
                   CONCAT('/api/conservation/restoration-models/',id,'/content') AS fileUrl
            FROM conservation_restoration_model WHERE result_id=? ORDER BY id
            """, this::camelMap, id));
        result.put("versions", jdbc.query("""
            SELECT id,version_no AS versionNo,version_name AS versionName,version_type AS versionType,
                   change_description AS changeDescription,evidence_level AS evidenceLevel,
                   confidence_level AS confidenceLevel,is_current AS isCurrent,
                   snapshot_json AS snapshotJson,creator,create_time AS createTime
            FROM conservation_restoration_version WHERE result_id=? ORDER BY create_time DESC,id DESC
            """, this::camelMap, id));
        for (Map<String, Object> version : list(result.get("versions"))) {
            version.put("snapshot", jsonMap(version.remove("snapshotJson")));
            version.put("isRecommended", false);
        }
        String status = text(result.get("resultStatus"));
        if (Set.of("reviewed", "archived").contains(status)) result.put("resultStatus", "completed");
        if ("returned".equals(status)) result.put("resultStatus", "in_progress");
        result.put("recommendedResult", false);
        List<Object> selectedPartIds = list(result.get("parts")).stream()
            .filter(x -> bool(x.get("selectedForMonitoring"))).map(x -> x.get("id")).toList();
        result.put("monitoring", mapOf("partIds", selectedPartIds,
            "indicators", result.remove("monitoringIndicators"), "cycle", result.remove("monitoringCycle"),
            "baselineMediaId", result.remove("monitoringBaselineId"),
            "warningConditions", result.remove("warningConditions"), "note", result.remove("monitoringNote")));
    }

    private void prepareIds(Map<String, Object> result, Set<Long> resultIds, Set<Long> partIds,
                            Set<Long> sourceIds, Set<Long> mediaIds, Set<Long> modelIds,
                            Set<Long> versionIds) {
        result.put("id", uniqueId(longValue(result.get("id")), resultIds));
        result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        for (Map<String, Object> part : list(result.get("parts"))) part.put("id", uniqueId(longValue(part.get("id")), partIds));
        for (Map<String, Object> source : list(result.get("sources"))) source.put("id", uniqueId(longValue(source.get("id")), sourceIds));
        for (Map<String, Object> media : list(result.get("media"))) {
            media.put("id", uniqueId(longValue(media.get("id")), mediaIds));
            media.remove("fileUrl");
            media.remove("thumbnailUrl");
        }
        for (Map<String, Object> model : list(result.get("models"))) {
            model.put("id", uniqueId(longValue(model.get("id")), modelIds));
            model.remove("fileUrl");
            model.remove("previewImageUrl");
        }
        for (Map<String, Object> version : list(result.get("versions"))) version.put("id", uniqueId(longValue(version.get("id")), versionIds));
    }

    private void syncArchive(Long projectId, List<Map<String, Object>> results) {
        Map<String, Object> workbench = archiveService.getWorkbench(projectId);
        Map<String, Object> archive = map(workbench.get("archive"));
        Map<String, Object> workspace = map(workbench.get("workspace"));
        if (archive == null || workspace == null) return;
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (Map<String, Object> result : results) {
            if (!bool(result.get("selectedForArchive"))) continue;
            Map<String, Object> primaryMedia = list(result.get("media")).stream()
                .filter(x -> bool(x.get("isPrimary"))).findFirst().orElse(null);
            Map<String, Object> primaryModel = list(result.get("models")).stream()
                .filter(x -> bool(x.get("isPrimary"))).findFirst().orElse(null);
            summaries.add(mapOf("id", result.get("id"), "name", result.get("resultName"),
                "type", result.get("restorationType"), "status", result.get("resultStatus"),
                "confidence", result.get("confidenceLevel"),
                "image", primaryMedia == null ? "" : "/api/conservation/restoration-media/" + primaryMedia.get("id") + "/content",
                "model", primaryModel == null ? "" : primaryModel.get("fileName"),
                "version", result.get("currentVersion"), "description",
                value(map(result.get("evaluation")) == null ? null : map(result.get("evaluation")).get("finalConclusion"),
                    result.get("resultSummary"))));
        }
        workspace.put("restorationResults", summaries);
        archiveService.save(longValue(archive.get("id")), mapOf("archive", archive,
            "workspace", workspace, "completeness", archive.get("completenessRate")));
    }

    private Map<String, Object> mediaMetadata(Long id) {
        return one("""
            SELECT id,part_id AS restorationPartId,source_media_id AS sourceMediaId,
                   source_business_type AS sourceBusinessType,source_business_id AS sourceBusinessId,
                   media_stage AS mediaStage,media_type AS mediaType,original_name AS fileName,
                   title,description,is_primary AS isPrimary,selected_for_archive AS selectedForArchive,
                   selected_as_monitoring_baseline AS selectedAsMonitoringBaseline,sort_order AS sortOrder,
                   CONCAT('/api/conservation/restoration-media/',id,'/content') AS fileUrl,
                   CONCAT('/api/conservation/restoration-media/',id,'/content') AS thumbnailUrl
            FROM conservation_restoration_media WHERE id=?
            """, id);
    }

    private Map<String, Object> modelMetadata(Long id) {
        return one("""
            SELECT id,model_name AS modelName,model_type AS modelType,original_name AS fileName,
                   file_format AS fileFormat,file_size AS fileSize,scale_unit AS scaleUnit,
                   coordinate_system AS coordinateSystem,polygon_count AS polygonCount,
                   texture_count AS textureCount,model_stage AS modelStage,
                   model_description AS modelDescription,supports_layer AS supportsLayer,
                   supports_annotation AS supportsAnnotation,is_primary AS isPrimary,
                   CONCAT('/api/conservation/restoration-models/',id,'/content') AS fileUrl
            FROM conservation_restoration_model WHERE id=?
            """, id);
    }

    private void requireResult(Long id) {
        if (count("SELECT COUNT(*) FROM conservation_restoration_result WHERE id=? AND deleted=0", id) == 0) {
            throw new IllegalArgumentException("复原成果不存在，请先保存成果");
        }
    }

    private void validateFile(MultipartFile file, long max, Set<String> allowed, String message) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择文件");
        if (file.getSize() > max) throw new IllegalArgumentException("文件大小超过限制");
        String type = Objects.toString(file.getContentType(), "application/octet-stream");
        if (allowed.stream().noneMatch(type::startsWith)) throw new IllegalArgumentException(message);
    }

    private MapSqlParameterSource params(Map<String, Object> source) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        source.forEach(p::addValue);
        String[] keys = {"processId","resultCode","resultName","restorationType","restorationCategory",
            "targetPart","restorationScope","restorationPurpose","basisSummary","methodSummary",
            "resultSummary","uncertaintySummary","evidenceLevel","confidenceLevel","resultStatus",
            "currentVersion","completionRate","overallScore","selectedForArchive","recommendedResult",
            "requiresMonitoring","principal","participantNames","partCode","partName","partType",
            "targetLocation","scopeDescription","materialName","technique","removable","reversible",
            "reversibleDescription","distinguishable","displayStyle","annotationText","percentageValue",
            "sortOrder","modelLayer","modelObjectName","layerVisible","restorationPartId","sourceType","sourceTitle","businessType",
            "businessId","supportDescription","reliability","fileName","fileUrl","sourceMediaId",
            "sourceBusinessType","sourceBusinessId","mediaStage","mediaType","title","description",
            "isPrimary","selectedForArchive","selectedAsMonitoringBaseline","modelName","modelType",
            "fileFormat","fileSize","scaleUnit","coordinateSystem","polygonCount","textureCount",
            "modelStage","modelDescription","supportsLayer","supportsAnnotation","versionNo",
            "versionName","versionType","changeDescription","isCurrent","isRecommended","archived","creator"};
        for (String key : keys) if (!p.hasValue(key)) p.addValue(key, null);
        return p;
    }

    private Map<String, Object> snapshot(Map<String, Object> result) {
        String[] keys = {"resultName","restorationType","restorationCategory","targetPart",
            "restorationScope","restorationPurpose","basisSummary","methodSummary","resultSummary",
            "uncertaintySummary","evidenceLevel","confidenceLevel","principal","participantNames",
            "stepIds","comparisonIds","diseaseIds","detectionIds","parts","sources",
            "methodParameters","evaluation","monitoring","requiresMonitoring"};
        Map<String, Object> snapshot = new LinkedHashMap<>();
        for (String key : keys) snapshot.put(key, result.get(key));
        return snapshot;
    }

    private void applySnapshot(Map<String, Object> result, Map<String, Object> snapshot) {
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            if (!Set.of("id","projectId","archiveId","processId","resultCode","models","media","versions").contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addColumnIfMissing(String table, String column, String ddl) {
        Integer tableCount = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?
            """, Integer.class, table);
        if (tableCount == null || tableCount == 0) return;
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?
            """, Integer.class, table, column);
        if (count != null && count == 0) jdbc.execute(ddl);
    }

    private Map<Long, Map<String, Object>> byId(List<Map<String, Object>> rows) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Map<String, Object> row : rows) result.put(longValue(row.get("id")), row);
        return result;
    }

    private Long nullableLong(String sql, Object... args) {
        List<Long> values = jdbc.query(sql, (rs, i) -> rs.getLong(1), args);
        return values.isEmpty() ? null : values.getFirst();
    }

    private int count(String sql, Object... args) {
        return Objects.requireNonNull(jdbc.queryForObject(sql, Integer.class, args));
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.query(sql, this::camelMap, args);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private Map<String, Object> camelMap(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String key = meta.getColumnLabel(i);
            Object value = rs.getObject(i);
            if (value instanceof Number && Set.of("selectedForArchive","recommendedResult","requiresMonitoring",
                "removable","reversible","distinguishable","modelLayer","layerVisible","selectedForMonitoring","isPrimary",
                "selectedAsMonitoringBaseline","supportsLayer","supportsAnnotation","isCurrent",
                "isRecommended","archived").contains(key)) value = ((Number) value).intValue() != 0;
            result.put(key, value);
        }
        return result;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException("复原成果动态数据无法序列化", e); }
    }

    private List<Object> jsonList(Object value) {
        if (value == null) return new ArrayList<>();
        try { return objectMapper.readValue(String.valueOf(value), new TypeReference<>() {}); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    private Map<String, Object> jsonMap(Object value) {
        if (value == null) return new LinkedHashMap<>();
        try { return objectMapper.readValue(String.valueOf(value), new TypeReference<>() {}); }
        catch (Exception e) { return new LinkedHashMap<>(); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : new ArrayList<>();
    }

    private Set<Long> ids(Object value) {
        Set<Long> result = new HashSet<>();
        if (value instanceof List<?> list) for (Object item : list) {
            Long id = longValue(item);
            if (id != null) result.add(id);
        }
        return result;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private Long uniqueId(Long requested, Set<Long> used) {
        Long id = requested;
        while (id == null || id <= 0 || used.contains(id)) {
            id = UUID.randomUUID().getMostSignificantBits() & ((1L << 52) - 1);
        }
        used.add(id);
        return id;
    }

    private Long firstLong(Object value) {
        return value instanceof List<?> list && !list.isEmpty() ? longValue(list.getFirst()) : null;
    }

    private static Long longValue(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        return value instanceof Number n ? n.longValue() : Long.valueOf(value.toString());
    }

    private int integer(Object value) {
        return value == null || value.toString().isBlank() ? 0 : Integer.parseInt(value.toString());
    }

    private boolean bool(Object value) {
        return value instanceof Boolean b ? b : value instanceof Number n ? n.intValue() != 0
            : "true".equalsIgnoreCase(Objects.toString(value, ""));
    }

    private String text(Object value) {
        return Objects.toString(value, "").trim();
    }

    private Object value(Object preferred, Object fallback) {
        return preferred == null || preferred.toString().isBlank() ? fallback : preferred;
    }

    private LocalDate date(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        try { return LocalDate.parse(value.toString().substring(0, 10)); }
        catch (Exception e) { return null; }
    }
}
