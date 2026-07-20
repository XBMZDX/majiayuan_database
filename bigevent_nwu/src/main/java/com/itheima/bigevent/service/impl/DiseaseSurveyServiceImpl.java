package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.service.DiseaseSurveyService;
import com.itheima.bigevent.service.MonitoringService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DiseaseSurveyServiceImpl implements DiseaseSurveyService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final MonitoringService projectService;
    private final ObjectMapper objectMapper;

    public DiseaseSurveyServiceImpl(JdbcTemplate jdbc, MonitoringService projectService,
                                    ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.projectService = projectService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> getWorkbench(Long projectId) {
        Map<String, Object> project = projectService.getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");

        Map<String, Object> survey = one("""
            SELECT id,project_id AS projectId,survey_code AS surveyCode,survey_date AS surveyDate,
                   surveyor,survey_location AS surveyLocation,preservation_status AS preservationStatus,
                   structural_stability AS structuralStability,environment_summary AS environmentSummary,
                   overall_risk_level AS overallRiskLevel,summary,status
            FROM conservation_disease_survey
            WHERE project_id=? AND deleted=0 ORDER BY id DESC LIMIT 1
            """, projectId);
        List<Map<String, Object>> records = survey == null ? List.of() : jdbc.query("""
            SELECT id,survey_id AS surveyId,project_id AS projectId,disease_type_id AS diseaseTypeId,
                   disease_name AS diseaseName,disease_category AS diseaseCategory,severity,
                   development_status AS developmentStatus,extent_value AS extentValue,
                   extent_unit AS extentUnit,part_name AS partName,side,
                   position_description AS positionDescription,morphology,cause_analysis AS causeAnalysis,
                   structural_impact AS structuralImpact,emergency,recommended_action AS recommendedAction,
                   sort_order AS sortOrder,
                   (SELECT COUNT(*) FROM conservation_disease_media m
                    WHERE m.disease_record_id=conservation_disease_record.id) AS mediaCount
            FROM conservation_disease_record
            WHERE survey_id=? AND deleted=0 ORDER BY sort_order,id
            """, this::mapRow, survey.get("id"));
        for (Map<String, Object> record : records) {
            record.put("media", mediaList(number(record.get("id"))));
        }
        List<Map<String, Object>> types = jdbc.query("""
            SELECT id,code,name,category,applicable_material AS applicableMaterial,description,
                   default_risk_weight AS defaultRiskWeight,enabled,sort_order AS sortOrder
            FROM conservation_disease_type WHERE enabled=1 ORDER BY sort_order,id
            """, this::mapRow);

        if (survey == null) {
            survey = new LinkedHashMap<>();
            survey.put("id", null);
            survey.put("projectId", projectId);
            survey.put("surveyCode", "DS-" + projectId + "-" + LocalDate.now().toString().replace("-", ""));
            survey.put("surveyDate", LocalDate.now());
            survey.put("preservationStatus", "fair");
            survey.put("structuralStability", "stable");
            survey.put("status", "draft");
        }
        return Map.of("project", project, "survey", survey, "records", records, "diseaseTypes", types);
    }

    @Override
    @Transactional
    public Map<String, Object> saveWorkbench(Long projectId, Map<String, Object> survey,
                                             List<Map<String, Object>> records, boolean submit) {
        Map<String, Object> project = projectService.getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        if (submit) validateSubmission(survey, records);
        String risk = records.isEmpty()
            ? Objects.toString(project.get("riskLevel"), "medium")
            : calculateRisk(records);
        Long surveyId = number(survey.get("id"));
        MapSqlParameterSource params = surveyParams(projectId, survey, risk, submit ? "submitted" : "draft");
        if (surveyId == null || !belongsToProject(surveyId, projectId)) {
            GeneratedKeyHolder key = new GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_disease_survey
                (project_id,survey_code,survey_date,surveyor,survey_location,preservation_status,
                 structural_stability,environment_summary,overall_risk_level,summary,status)
                VALUES
                (:projectId,:surveyCode,:surveyDate,:surveyor,:surveyLocation,:preservationStatus,
                 :structuralStability,:environmentSummary,:overallRiskLevel,:summary,:status)
                """, params, key, new String[]{"id"});
            surveyId = key.getKey().longValue();
        } else {
            params.addValue("id", surveyId);
            namedJdbc.update("""
                UPDATE conservation_disease_survey SET survey_code=:surveyCode,survey_date=:surveyDate,
                surveyor=:surveyor,survey_location=:surveyLocation,preservation_status=:preservationStatus,
                structural_stability=:structuralStability,environment_summary=:environmentSummary,
                overall_risk_level=:overallRiskLevel,summary=:summary,status=:status
                WHERE id=:id AND project_id=:projectId AND deleted=0
                """, params);
        }

        Set<Long> existingIds = new LinkedHashSet<>(jdbc.query(
            "SELECT id FROM conservation_disease_record WHERE survey_id=? AND deleted=0",
            (rs, rowNum) -> rs.getLong(1), surveyId
        ));
        Set<Long> retainedIds = new LinkedHashSet<>();
        int order = 0;
        for (Map<String, Object> record : records) {
            MapSqlParameterSource recordParams = recordParams(projectId, surveyId, record, order++);
            Long recordId = number(record.get("id"));
            if (recordId != null && existingIds.contains(recordId)) {
                recordParams.addValue("id", recordId);
                namedJdbc.update("""
                    UPDATE conservation_disease_record SET disease_type_id=:diseaseTypeId,
                    disease_name=:diseaseName,disease_category=:diseaseCategory,severity=:severity,
                    development_status=:developmentStatus,extent_value=:extentValue,extent_unit=:extentUnit,
                    part_name=:partName,side=:side,position_description=:positionDescription,
                    morphology=:morphology,cause_analysis=:causeAnalysis,structural_impact=:structuralImpact,
                    emergency=:emergency,recommended_action=:recommendedAction,sort_order=:sortOrder
                    WHERE id=:id AND survey_id=:surveyId AND project_id=:projectId AND deleted=0
                    """, recordParams);
                retainedIds.add(recordId);
            } else {
                GeneratedKeyHolder key = new GeneratedKeyHolder();
                namedJdbc.update("""
                    INSERT INTO conservation_disease_record
                    (survey_id,project_id,disease_type_id,disease_name,disease_category,severity,
                     development_status,extent_value,extent_unit,part_name,side,position_description,
                     morphology,cause_analysis,structural_impact,emergency,recommended_action,sort_order)
                    VALUES
                    (:surveyId,:projectId,:diseaseTypeId,:diseaseName,:diseaseCategory,:severity,
                     :developmentStatus,:extentValue,:extentUnit,:partName,:side,:positionDescription,
                     :morphology,:causeAnalysis,:structuralImpact,:emergency,:recommendedAction,:sortOrder)
                    """, recordParams, key, new String[]{"id"});
                retainedIds.add(key.getKey().longValue());
            }
        }
        for (Long removedId : existingIds) {
            if (!retainedIds.contains(removedId)) {
                jdbc.update("DELETE FROM conservation_disease_media WHERE disease_record_id=?", removedId);
                jdbc.update("DELETE FROM conservation_disease_record WHERE id=?", removedId);
            }
        }
        if (submit) {
            jdbc.update("""
                UPDATE conservation_project SET risk_level=?,current_stage='planning',
                progress=GREATEST(progress,30),status=IF(status='draft','active',status)
                WHERE id=? AND deleted=0
                """, risk, projectId);
        } else {
            jdbc.update("UPDATE conservation_project SET risk_level=? WHERE id=? AND deleted=0", risk, projectId);
        }
        return getWorkbench(projectId);
    }

    @Override
    public Map<String, Object> uploadMedia(Long recordId, MultipartFile file,
                                           Map<String, String> metadata) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的病害图片");
        if (file.getSize() > 20L * 1024 * 1024) throw new IllegalArgumentException("单张图片不能超过20MB");
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        if (!contentType.startsWith("image/")) throw new IllegalArgumentException("病害影像仅支持图片文件");
        Map<String, Object> record = one("""
            SELECT survey_id AS surveyId,project_id AS projectId
            FROM conservation_disease_record WHERE id=? AND deleted=0
            """, recordId);
        if (record == null) throw new IllegalArgumentException("病害记录不存在，请先保存病害信息");
        try {
            GeneratedKeyHolder key = new GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_disease_media
                (disease_record_id,survey_id,project_id,file_name,content_type,file_size,file_data,
                 title,description,annotations_json)
                VALUES
                (:recordId,:surveyId,:projectId,:fileName,:contentType,:fileSize,:fileData,
                 :title,:description,CAST(:annotations AS JSON))
                """, new MapSqlParameterSource(record)
                .addValue("recordId", recordId)
                .addValue("fileName", Optional.ofNullable(file.getOriginalFilename()).orElse("disease-image"))
                .addValue("contentType", contentType)
                .addValue("fileSize", file.getSize())
                .addValue("fileData", file.getBytes())
                .addValue("title", text(metadata.get("title")))
                .addValue("description", text(metadata.get("description")))
                .addValue("annotations", "[]"), key, new String[]{"id"});
            return mediaMetadata(key.getKey().longValue());
        } catch (Exception exception) {
            throw new IllegalStateException("病害图片写入MySQL失败", exception);
        }
    }

    @Override
    public Map<String, Object> getMedia(Long mediaId) {
        return one("""
            SELECT file_name AS fileName,content_type AS contentType,file_data AS fileData
            FROM conservation_disease_media WHERE id=?
            """, mediaId);
    }

    @Override
    public Map<String, Object> saveAnnotations(Long mediaId,
                                                List<Map<String, Object>> annotations) {
        if (annotations.size() > 200) throw new IllegalArgumentException("单张图片最多保存200个标注");
        int updated = jdbc.update("""
            UPDATE conservation_disease_media SET annotations_json=CAST(? AS JSON) WHERE id=?
            """, json(annotations), mediaId);
        if (updated == 0) throw new IllegalArgumentException("病害图片不存在");
        return mediaMetadata(mediaId);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        jdbc.update("DELETE FROM conservation_disease_media WHERE id=?", mediaId);
    }

    private void validateSubmission(Map<String, Object> survey, List<Map<String, Object>> records) {
        if (blank(survey.get("surveyDate")) || blank(survey.get("surveyor"))) {
            throw new IllegalArgumentException("请填写调查日期和调查人");
        }
        if (records.isEmpty()) throw new IllegalArgumentException("请至少添加一条病害记录");
        for (Map<String, Object> record : records) {
            if (blank(record.get("diseaseName"))) throw new IllegalArgumentException("病害名称不能为空");
            String severity = Objects.toString(record.get("severity"), "");
            if (Set.of("severe", "critical").contains(severity) && blank(record.get("recommendedAction"))) {
                throw new IllegalArgumentException("严重或危急病害必须填写处理建议");
            }
        }
    }

    private String calculateRisk(List<Map<String, Object>> records) {
        long severe = records.stream().filter(item ->
            Set.of("severe", "critical").contains(Objects.toString(item.get("severity"), ""))).count();
        boolean critical = records.stream().anyMatch(item ->
            "critical".equals(item.get("severity")) ||
            ("severe".equals(item.get("severity")) && "overall".equals(item.get("structuralImpact"))));
        if (critical || severe >= 2) return "high";
        boolean medium = records.stream().anyMatch(item ->
            Set.of("moderate", "severe").contains(Objects.toString(item.get("severity"), "")));
        return medium ? "medium" : "low";
    }

    private boolean belongsToProject(Long surveyId, Long projectId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM conservation_disease_survey
            WHERE id=? AND project_id=? AND deleted=0
            """, Integer.class, surveyId, projectId);
        return count != null && count > 0;
    }

    private MapSqlParameterSource surveyParams(Long projectId, Map<String, Object> survey,
                                               String risk, String status) {
        return new MapSqlParameterSource()
            .addValue("projectId", projectId)
            .addValue("surveyCode", text(survey.get("surveyCode")))
            .addValue("surveyDate", date(survey.get("surveyDate")))
            .addValue("surveyor", text(survey.get("surveyor")))
            .addValue("surveyLocation", text(survey.get("surveyLocation")))
            .addValue("preservationStatus", text(survey.get("preservationStatus")))
            .addValue("structuralStability", text(survey.get("structuralStability")))
            .addValue("environmentSummary", text(survey.get("environmentSummary")))
            .addValue("overallRiskLevel", risk)
            .addValue("summary", text(survey.get("summary")))
            .addValue("status", status);
    }

    private MapSqlParameterSource recordParams(Long projectId, Long surveyId,
                                               Map<String, Object> record, int order) {
        return new MapSqlParameterSource()
            .addValue("surveyId", surveyId).addValue("projectId", projectId)
            .addValue("diseaseTypeId", number(record.get("diseaseTypeId")))
            .addValue("diseaseName", text(record.get("diseaseName")))
            .addValue("diseaseCategory", text(record.get("diseaseCategory")))
            .addValue("severity", text(record.get("severity")))
            .addValue("developmentStatus", text(record.get("developmentStatus")))
            .addValue("extentValue", decimalText(record.get("extentValue")))
            .addValue("extentUnit", text(record.get("extentUnit")))
            .addValue("partName", text(record.get("partName")))
            .addValue("side", text(record.get("side")))
            .addValue("positionDescription", text(record.get("positionDescription")))
            .addValue("morphology", text(record.get("morphology")))
            .addValue("causeAnalysis", text(record.get("causeAnalysis")))
            .addValue("structuralImpact", text(record.get("structuralImpact")))
            .addValue("emergency", Boolean.TRUE.equals(record.get("emergency")) ? 1 : 0)
            .addValue("recommendedAction", text(record.get("recommendedAction")))
            .addValue("sortOrder", order);
    }

    private List<Map<String, Object>> mediaList(Long recordId) {
        if (recordId == null) return new ArrayList<>();
        List<Map<String, Object>> media = jdbc.query("""
            SELECT id,disease_record_id AS diseaseRecordId,survey_id AS surveyId,
                   project_id AS projectId,file_name AS fileName,content_type AS contentType,
                   file_size AS fileSize,title,description,annotations_json AS annotations,
                   create_time AS createTime
            FROM conservation_disease_media
            WHERE disease_record_id=? ORDER BY create_time,id
            """, this::mapRow, recordId);
        for (Map<String, Object> item : media) normalizeMedia(item);
        return media;
    }

    private Map<String, Object> mediaMetadata(Long mediaId) {
        Map<String, Object> media = one("""
            SELECT id,disease_record_id AS diseaseRecordId,survey_id AS surveyId,
                   project_id AS projectId,file_name AS fileName,content_type AS contentType,
                   file_size AS fileSize,title,description,annotations_json AS annotations,
                   create_time AS createTime
            FROM conservation_disease_media WHERE id=?
            """, mediaId);
        if (media == null) throw new IllegalArgumentException("病害图片不存在");
        normalizeMedia(media);
        return media;
    }

    private void normalizeMedia(Map<String, Object> media) {
        media.put("fileUrl", "/api/conservation/disease-media/" + media.get("id") + "/content");
        Object annotations = media.get("annotations");
        if (annotations == null || annotations.toString().isBlank()) {
            media.put("annotations", new ArrayList<>());
            return;
        }
        try {
            media.put("annotations", objectMapper.readValue(
                annotations.toString(), new TypeReference<List<Map<String, Object>>>() {}
            ));
        } catch (Exception exception) {
            media.put("annotations", new ArrayList<>());
        }
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception exception) { throw new IllegalArgumentException("图片标注数据格式不正确", exception); }
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.query(sql, this::mapRow, args);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int index = 1; index <= meta.getColumnCount(); index++) {
            String key = meta.getColumnLabel(index);
            Object value = rs.getObject(index);
            if (Set.of("emergency", "enabled").contains(key) && value instanceof Number number) {
                value = number.intValue() != 0;
            }
            result.put(key, value);
        }
        return result;
    }

    private Long number(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        return value instanceof Number number ? number.longValue() : Long.valueOf(value.toString());
    }

    private String text(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString().trim();
    }

    private Object decimalText(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    private LocalDate date(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        String text = value.toString().substring(0, Math.min(10, value.toString().length()));
        try { return LocalDate.parse(text); }
        catch (DateTimeParseException exception) { throw new IllegalArgumentException("调查日期格式不正确"); }
    }

    private boolean blank(Object value) {
        return value == null || value.toString().isBlank();
    }
}
