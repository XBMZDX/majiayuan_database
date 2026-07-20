package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.ComparisonService;
import com.itheima.bigevent.service.ProcessService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ComparisonServiceImpl implements ComparisonService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;
    private final ProcessService processService;
    private final ArchiveService archiveService;

    public ComparisonServiceImpl(JdbcTemplate jdbc, ObjectMapper objectMapper,
                                 ProcessService processService, ArchiveService archiveService) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.objectMapper = objectMapper;
        this.processService = processService;
        this.archiveService = archiveService;
    }

    @Override
    public Map<String, Object> getWorkbench(Long projectId) {
        Map<String, Object> processContext = processService.getWorkbench(projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", processContext.get("project"));
        result.put("processRecord", processContext.get("processRecord"));
        result.put("steps", processContext.getOrDefault("steps", List.of()));
        Map<String, Object> archiveWorkspace = map(processContext.get("archiveWorkspace"));
        result.put("diseases", archiveWorkspace == null ? List.of()
            : archiveWorkspace.getOrDefault("diseaseRecords", List.of()));
        result.put("sourceMedia", sourceMedia(projectId));
        List<Map<String, Object>> groups = jdbc.query("""
            SELECT comparison_json AS comparisonJson FROM conservation_comparison
            WHERE project_id=? ORDER BY update_time DESC,id DESC
            """, (rs, rowNum) -> jsonMap(rs.getObject("comparisonJson")), projectId);
        result.put("groups", groups);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> saveWorkbench(Long projectId, List<Map<String, Object>> groups) {
        Map<String, Object> project = map(processService.getWorkbench(projectId).get("project"));
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        List<Long> oldIds = jdbc.queryForList(
            "SELECT id FROM conservation_comparison WHERE project_id=?", Long.class, projectId);
        for (Long id : oldIds) {
            jdbc.update("DELETE FROM conservation_comparison_metric WHERE comparison_id=?", id);
            jdbc.update("DELETE FROM conservation_comparison_media WHERE comparison_id=?", id);
        }
        jdbc.update("DELETE FROM conservation_comparison WHERE project_id=?", projectId);
        Set<Long> groupIds = new HashSet<>();
        Set<Long> metricIds = new HashSet<>();
        Set<Long> mediaIds = new HashSet<>();
        for (Map<String, Object> group : groups) {
            prepareIds(group, groupIds, metricIds, mediaIds);
            insertGroup(projectId, project, group);
        }
        syncArchive(projectId, groups);
        return getWorkbench(projectId);
    }

    private void insertGroup(Long projectId, Map<String, Object> project, Map<String, Object> group) {
        Long id = longValue(group.get("id"));
        Map<String, Object> evaluation = map(group.get("evaluation"));
        Map<String, Object> monitoring = map(group.get("monitoring"));
        String status = text(group.get("evaluationStatus"));
        if (status.isBlank()) status = "draft";
        namedJdbc.update("""
            INSERT INTO conservation_comparison
            (id,project_id,artifact_id,process_id,step_id,comparison_code,comparison_title,
             comparison_type,target_part,shooting_position,before_summary,after_summary,
             comparison_description,overall_effect,remaining_issue,monitoring_review_part,
             monitoring_notes,evaluator,evaluation_date,comparison_status,overall_comparison,
             no_applicable_metrics,selected_for_archive,selected_for_restoration,
             selected_as_monitoring_baseline,comparison_json)
            VALUES (:id,:projectId,:artifactId,:processId,:stepId,:comparisonCode,:comparisonTitle,
             :comparisonType,:targetPart,:shootingPosition,:beforeSummary,:afterSummary,
             :comparisonDescription,:overallEffect,:remainingIssue,:monitoringReviewPart,
             :monitoringNotes,:evaluator,:evaluationDate,:comparisonStatus,:overallComparison,
             :noApplicableMetrics,:selectedForArchive,:selectedForRestoration,
             :selectedAsMonitoringBaseline,:comparisonJson)
            """, params(group).addValue("id", id).addValue("projectId", projectId)
            .addValue("artifactId", project.get("artifactId"))
            .addValue("remainingIssue", evaluation == null ? null : evaluation.get("remainingIssue"))
            .addValue("monitoringReviewPart", monitoring == null ? null : monitoring.get("reviewPart"))
            .addValue("monitoringNotes", monitoring == null ? null : monitoring.get("notes"))
            .addValue("evaluator", evaluation == null ? group.get("evaluator") : evaluation.get("evaluator"))
            .addValue("evaluationDate", date(evaluation == null ? group.get("evaluationDate") : evaluation.get("evaluationDate")))
            .addValue("comparisonStatus", status).addValue("comparisonJson", json(group)));
        for (Map<String, Object> metric : list(group.get("metrics"))) insertMetric(id, metric);
        for (Map<String, Object> image : list(group.get("images"))) insertImage(id, projectId, image);
    }

    private void insertMetric(Long comparisonId, Map<String, Object> metric) {
        Long id = longValue(metric.get("id"));
        namedJdbc.update("""
            INSERT INTO conservation_comparison_metric
            (id,comparison_id,metric_name,metric_category,before_value,after_value,value_unit,
             expected_direction,result_status,description)
            VALUES (:id,:comparisonId,:metricName,:metricCategory,:beforeValue,:afterValue,:valueUnit,
             :expectedDirection,:resultStatus,:description)
            """, params(metric).addValue("id", id).addValue("comparisonId", comparisonId));
    }

    private void insertImage(Long comparisonId, Long projectId, Map<String, Object> image) {
        Long id = longValue(image.get("id"));
        Long sourceId = longValue(image.get("sourceMediaId"));
        Map<String, Object> source = sourceId == null ? null : one("""
            SELECT original_name AS fileName,content_type AS contentType,file_size AS fileSize,file_data AS fileData
            FROM conservation_process_media WHERE id=? AND project_id=?
            """, sourceId, projectId);
        if (sourceId != null && source == null) {
            throw new IllegalArgumentException("对比影像不属于当前保护修复项目");
        }
        namedJdbc.update("""
            INSERT INTO conservation_comparison_media
            (id,comparison_id,source_media_id,image_stage,image_role,original_name,content_type,
             file_size,file_data,target_part,shooting_position,shooting_time,photographer,
             description,sequence_no,is_primary,source_module)
            VALUES (:id,:comparisonId,:sourceMediaId,:imageStage,:imageRole,:fileName,:contentType,
             :fileSize,:fileData,:targetPart,:shootingPosition,:shootingTime,:photographer,
             :imageDescription,:sequenceNo,:isPrimary,:sourceModule)
            """, params(image).addValue("id", id).addValue("comparisonId", comparisonId)
            .addValue("sourceMediaId", sourceId)
            .addValue("fileName", source == null ? image.get("fileName") : source.get("fileName"))
            .addValue("contentType", source == null ? null : source.get("contentType"))
            .addValue("fileSize", source == null ? null : source.get("fileSize"))
            .addValue("fileData", source == null ? null : source.get("fileData"))
            .addValue("shootingTime", dateTime(image.get("shootingTime"))));
    }

    private void prepareIds(Map<String, Object> group, Set<Long> groupIds,
                            Set<Long> metricIds, Set<Long> mediaIds) {
        group.put("id", uniqueId(longValue(group.get("id")), groupIds));
        group.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        for (Map<String, Object> metric : list(group.get("metrics"))) {
            metric.put("id", uniqueId(longValue(metric.get("id")), metricIds));
        }
        for (Map<String, Object> image : list(group.get("images"))) {
            Long requestedId = longValue(image.get("id"));
            Long sourceId = longValue(image.get("sourceMediaId"));
            if (Objects.equals(requestedId, sourceId)) requestedId = null;
            image.put("id", uniqueId(requestedId, mediaIds));
            image.remove("fileUrl");
            image.remove("thumbnailUrl");
        }
    }

    private Long uniqueId(Long requested, Set<Long> used) {
        Long id = requested;
        while (id == null || id <= 0 || used.contains(id)) {
            id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        }
        used.add(id);
        return id;
    }

    private List<Map<String, Object>> sourceMedia(Long projectId) {
        return jdbc.query("""
            SELECT m.id,m.id AS sourceMediaId,m.process_id AS processId,m.step_id AS stepId,
                   s.step_name AS stepName,s.step_status AS stepStatus,m.media_stage AS imageStage,
                   'detail' AS imageRole,m.original_name AS fileName,
                   CONCAT('/api/conservation/process-media/',m.id,'/content') AS fileUrl,
                   CONCAT('/api/conservation/process-media/',m.id,'/content') AS thumbnailUrl,
                   m.target_part AS targetPart,m.shooting_position AS shootingPosition,
                   m.shooting_time AS shootingTime,m.photographer,
                   m.description AS imageDescription,m.selected_for_comparison AS selectedForComparison,
                   '修复过程记录' AS sourceModule,m.project_id AS projectId
            FROM conservation_process_media m
            JOIN conservation_process_step s ON s.id=m.step_id
            WHERE m.project_id=? ORDER BY m.selected_for_comparison DESC,s.sequence_no,m.create_time,m.id
            """, this::camelMap, projectId);
    }

    private void syncArchive(Long projectId, List<Map<String, Object>> groups) {
        Map<String, Object> workbench = archiveService.getWorkbench(projectId);
        Map<String, Object> archive = map(workbench.get("archive"));
        Map<String, Object> workspace = map(workbench.get("workspace"));
        if (archive == null || workspace == null) return;
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            if (!bool(group.get("selectedForArchive"))) continue;
            Map<String, Object> evaluation = map(group.get("evaluation"));
            List<Map<String, Object>> images = list(group.get("images"));
            summaries.add(mapOf(
                "id", group.get("id"), "title", group.get("comparisonTitle"), "part", group.get("targetPart"),
                "disease", list(group.get("diseases")).stream().map(x -> text(x.get("diseaseName")))
                    .filter(x -> !x.isBlank()).reduce((a, b) -> a + "、" + b).orElse(""),
                "step", group.get("stepId"), "description",
                evaluation == null ? group.get("comparisonDescription") : evaluation.get("finalConclusion"),
                "evaluationStatus", group.get("evaluationStatus"),
                "beforeImage", imageUrl(images, "before"), "afterImage", imageUrl(images, "after")
            ));
        }
        workspace.put("comparisons", summaries);
        archiveService.save(longValue(archive.get("id")), mapOf(
            "archive", archive, "workspace", workspace, "completeness", archive.get("completenessRate")
        ));
    }

    private String imageUrl(List<Map<String, Object>> images, String stage) {
        return images.stream().filter(x -> stage.equals(text(x.get("imageStage"))))
            .map(x -> "/api/conservation/comparison-media/" + x.get("id") + "/content")
            .findFirst().orElse("");
    }

    private MapSqlParameterSource params(Map<String, Object> source) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        source.forEach(p::addValue);
        String[] keys = {"processId","stepId","comparisonCode","comparisonTitle","comparisonType","targetPart",
            "shootingPosition","beforeSummary","afterSummary","comparisonDescription","overallEffect",
            "overallComparison","noApplicableMetrics","selectedForArchive","selectedForRestoration",
            "selectedAsMonitoringBaseline","metricName","metricCategory","beforeValue","afterValue",
            "valueUnit","expectedDirection","resultStatus","description","imageStage","imageRole",
            "photographer","imageDescription","sequenceNo","isPrimary","sourceModule"};
        for (String key : keys) if (!p.hasValue(key)) p.addValue(key, null);
        return p;
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
            if (value instanceof Number n && Set.of("selectedForComparison").contains(key)) {
                value = n.intValue() != 0;
            }
            result.put(key, value);
        }
        return result;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException("对比数据无法序列化", e); }
    }

    private Map<String, Object> jsonMap(Object value) {
        if (value == null) return new LinkedHashMap<>();
        try { return objectMapper.readValue(String.valueOf(value), new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("数据库中的对比数据无法解析", e); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : new ArrayList<>();
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private static Long longValue(Object value) {
        return value instanceof Number n ? n.longValue()
            : value == null || value.toString().isBlank() ? null : Long.valueOf(value.toString());
    }

    private boolean bool(Object value) {
        return value instanceof Boolean b ? b : value instanceof Number n ? n.intValue() != 0
            : "true".equalsIgnoreCase(Objects.toString(value, ""));
    }

    private String text(Object value) {
        return Objects.toString(value, "").trim();
    }

    private LocalDate date(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        try { return LocalDate.parse(value.toString().substring(0, 10)); }
        catch (Exception e) { return null; }
    }

    private LocalDateTime dateTime(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        String text = value.toString().replace('T', ' ');
        try { return LocalDateTime.parse(text.substring(0, Math.min(19, text.length())),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
        catch (Exception e) { return null; }
    }
}
