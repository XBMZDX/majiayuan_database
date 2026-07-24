package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.ProcessService;
import com.itheima.bigevent.utils.ConservationOssStorage;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProcessServiceImpl implements ProcessService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;
    private final ArchiveService archiveService;

    public ProcessServiceImpl(JdbcTemplate jdbc, ObjectMapper objectMapper, ArchiveService archiveService) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.objectMapper = objectMapper;
        this.archiveService = archiveService;
    }

    @PostConstruct
    public void allowProcessWithoutArchive() {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='conservation_process'
              AND COLUMN_NAME='archive_id' AND IS_NULLABLE='NO'
            """, Integer.class);
        if (count != null && count > 0) {
            jdbc.execute("ALTER TABLE conservation_process MODIFY archive_id BIGINT NULL");
        }
    }

    @Override
    public Map<String, Object> getWorkbench(Long projectId) {
        Map<String, Object> archiveWorkbench = archiveService.getWorkbench(projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", archiveWorkbench.get("project"));
        result.put("archive", archiveWorkbench.get("archive"));
        result.put("archiveWorkspace", archiveWorkbench.get("workspace"));
        Map<String, Object> archiveWorkspace = map(archiveWorkbench.get("workspace"));
        result.put("formalPlan", archiveWorkspace == null ? null : archiveWorkspace.get("plan"));

        Map<String, Object> process = one("""
            SELECT id,project_id AS projectId,archive_id AS archiveId,process_code AS processCode,
                   process_name AS processName,process_status AS processStatus,execution_mode AS executionMode,
                   supervisor,start_date AS startDate,expected_end_date AS expectedEndDate,
                   actual_end_date AS actualEndDate,total_steps AS totalSteps,completed_steps AS completedSteps,
                   progress,execution_summary AS executionSummary,final_result AS finalResult,
                   pause_json AS pauseJson,update_time AS updateTime
            FROM conservation_process WHERE project_id=? AND deleted=0
            """, projectId);
        if (process == null) {
            result.put("processRecord", null);
            result.put("steps", List.of());
            return result;
        }
        Object pauseJson = process.remove("pauseJson");
        process.put("pauseRecord", pauseJson == null ? null : jsonMap(pauseJson));
        Long processId = longValue(process.get("id"));
        List<Map<String, Object>> steps = jdbc.query("""
            SELECT id,step_json AS stepJson FROM conservation_process_step
            WHERE process_id=? ORDER BY sequence_no,id
            """, (rs, n) -> {
                Map<String, Object> step = jsonMap(rs.getObject("stepJson"));
                step.put("id", rs.getLong("id"));
                List<Map<String, Object>> media = media(rs.getLong("id"));
                if (!media.isEmpty()) step.put("media", media);
                return step;
            }, processId);
        refreshDiseaseReferences(steps, archiveWorkspace == null ? List.of() : list(archiveWorkspace.get("diseaseRecords")));
        result.put("processRecord", process);
        result.put("steps", steps);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> create(Long projectId) {
        Map<String, Object> existing = one("SELECT id FROM conservation_process WHERE project_id=? AND deleted=0", projectId);
        if (existing != null) return getWorkbench(projectId);
        Map<String, Object> context = archiveService.getWorkbench(projectId);
        Map<String, Object> archive = map(context.get("archive"));
        Map<String, Object> workspace = map(context.get("workspace"));
        Map<String, Object> plan = workspace == null ? null : map(workspace.get("plan"));
        Map<String, Object> project = map(context.get("project"));
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        String code = text(project.get("artifactCode")).replaceAll("[^A-Za-z0-9]", "");
        if (code.isBlank()) code = "PROJECT" + projectId;
        String executionMode = plan == null ? "manual"
            : "completed".equals(text(plan.get("planStatus"))) ? "formal" : "draft_plan";
        var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        namedJdbc.update("""
            INSERT INTO conservation_process
            (project_id,archive_id,process_code,process_name,process_status,execution_mode,supervisor,
             expected_end_date,total_steps,completed_steps,progress)
            VALUES (:projectId,:archiveId,:processCode,:processName,'not_started',:executionMode,:supervisor,
                    :expectedEndDate,0,0,0)
            """, new MapSqlParameterSource()
            .addValue("projectId", projectId).addValue("archiveId", archive == null ? null : archive.get("id"))
            .addValue("processCode", "CP-" + code + "-" + String.format("%03d", projectId))
            .addValue("processName", text(project.get("artifactName")) + "保护修复执行过程")
            .addValue("executionMode", executionMode)
            .addValue("supervisor", project.get("principal"))
            .addValue("expectedEndDate", date(project.get("expectedEndDate"))), key, new String[]{"id"});
        regenerateStepsInternal(key.getKey().longValue(), projectId, workspace);
        return getWorkbench(projectId);
    }

    @Override
    @Transactional
    public Map<String, Object> save(Long processId, Map<String, Object> body) {
        Map<String, Object> current = requireProcess(processId);
        Map<String, Object> process = map(body.get("processRecord"));
        List<Map<String, Object>> steps = list(body.get("steps"));
        if (process == null) throw new IllegalArgumentException("修复过程信息不能为空");
        namedJdbc.update("""
            UPDATE conservation_process SET process_name=:processName,process_status=:processStatus,
             execution_mode=:executionMode,supervisor=:supervisor,start_date=:startDate,
             expected_end_date=:expectedEndDate,actual_end_date=:actualEndDate,total_steps=:totalSteps,
             completed_steps=:completedSteps,progress=:progress,execution_summary=:executionSummary,
             final_result=:finalResult,pause_json=:pauseJson WHERE id=:id AND deleted=0
            """, params(process).addValue("id", processId)
            .addValue("startDate", date(process.get("startDate")))
            .addValue("expectedEndDate", date(process.get("expectedEndDate")))
            .addValue("actualEndDate", date(process.get("actualEndDate")))
            .addValue("pauseJson", process.get("pauseRecord") == null ? null : json(process.get("pauseRecord"))));
        Set<Long> retainedStepIds = new HashSet<>();
        for (Map<String, Object> step : steps) {
            Long id = longValue(step.get("id"));
            if (id != null) retainedStepIds.add(id);
        }
        List<Long> oldStepIds = jdbc.queryForList(
            "SELECT id FROM conservation_process_step WHERE process_id=?", Long.class, processId);
        for (Long oldStepId : oldStepIds) {
            if (!retainedStepIds.contains(oldStepId)) {
                jdbc.update("DELETE FROM conservation_process_media WHERE step_id=?", oldStepId);
            }
        }
        jdbc.update("DELETE FROM conservation_process_step WHERE process_id=?", processId);
        for (Map<String, Object> step : steps) insertStep(processId, longValue(current.get("projectId")), step);
        syncProjectAndArchive(longValue(current.get("projectId")), process, steps);
        return getWorkbench(longValue(current.get("projectId")));
    }

    @Override
    @Transactional
    public Map<String, Object> regenerateSteps(Long processId) {
        Map<String, Object> process = requireProcess(processId);
        if (!"not_started".equals(text(process.get("processStatus")))) {
            throw new IllegalStateException("修复过程开始后不能重新生成正式步骤");
        }
        Long projectId = longValue(process.get("projectId"));
        Map<String, Object> context = archiveService.getWorkbench(projectId);
        regenerateStepsInternal(processId, projectId, map(context.get("workspace")));
        return getWorkbench(projectId);
    }

    @Override
    public Map<String, Object> getSummary(Long projectId) {
        Map<String, Object> process = one("""
            SELECT id,process_status AS processStatus,total_steps AS totalSteps,
                   completed_steps AS completedSteps,progress,update_time AS updateTime
            FROM conservation_process WHERE project_id=? AND deleted=0
            """, projectId);
        if (process == null) return mapOf("exists", false, "totalSteps", 0, "completedSteps", 0, "progress", 0);
        process.put("exists", true);
        process.put("processing", count("SELECT COUNT(*) FROM conservation_process_step WHERE process_id=? AND step_status='in_progress'", process.get("id")));
        process.put("pending", count("SELECT COUNT(*) FROM conservation_process_step WHERE process_id=? AND step_status='pending'", process.get("id")));
        process.put("unresolvedIssues", unresolvedIssueCount(longValue(process.get("id"))));
        return process;
    }

    @Override
    public Map<String, Object> uploadMedia(Long stepId, MultipartFile file, Map<String, String> metadata) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的影像文件");
        if (file.getSize() > 50L * 1024 * 1024) throw new IllegalArgumentException("单个文件不能超过50MB");
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("修复过程影像仅支持图片或视频");
        }
        Map<String, Object> step = one("SELECT process_id AS processId,project_id AS projectId FROM conservation_process_step WHERE id=?", stepId);
        if (step == null) throw new IllegalArgumentException("修复步骤不存在，请先保存步骤");
        try {
            Map<String, String> stored = ConservationOssStorage.upload("process-media", file);
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_process_media
                (process_id,step_id,project_id,media_stage,original_name,content_type,file_size,file_url,oss_object_key,
                 title,shooting_time,shooting_position,target_part,photographer,description,
                 selected_for_comparison,selected_for_archive,selected_for_restoration)
                VALUES (:processId,:stepId,:projectId,:mediaStage,:originalName,:contentType,:fileSize,:fileUrl,:ossObjectKey,
                 :title,:shootingTime,:shootingPosition,:targetPart,:photographer,:description,
                 :selectedForComparison,:selectedForArchive,:selectedForRestoration)
                """, new MapSqlParameterSource(step).addValue("stepId", stepId)
                .addValue("mediaStage", metadata.get("mediaStage"))
                .addValue("originalName", file.getOriginalFilename()).addValue("contentType", contentType)
                .addValue("fileSize", file.getSize()).addValue("fileUrl", stored.get("fileUrl"))
                .addValue("ossObjectKey", stored.get("objectKey"))
                .addValue("title", metadata.get("title")).addValue("shootingTime", dateTime(metadata.get("shootingTime")))
                .addValue("shootingPosition", metadata.get("shootingPosition")).addValue("targetPart", metadata.get("targetPart"))
                .addValue("photographer", metadata.get("photographer")).addValue("description", metadata.get("description"))
                .addValue("selectedForComparison", bool(metadata.get("selectedForComparison")))
                .addValue("selectedForArchive", bool(metadata.get("selectedForArchive")))
                .addValue("selectedForRestoration", bool(metadata.get("selectedForRestoration"))),
                key, new String[]{"id"});
            return mediaMetadata(key.getKey().longValue());
        } catch (Exception e) {
            throw new IllegalStateException("过程影像上传到 OSS 失败", e);
        }
    }

    @Override
    public Map<String, Object> getMedia(Long mediaId) {
        return one("SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData,file_url AS fileUrl FROM conservation_process_media WHERE id=?", mediaId);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        jdbc.update("DELETE FROM conservation_process_media WHERE id=?", mediaId);
    }

    private void regenerateStepsInternal(Long processId, Long projectId, Map<String, Object> workspace) {
        jdbc.update("DELETE FROM conservation_process_media WHERE process_id=?", processId);
        jdbc.update("DELETE FROM conservation_process_step WHERE process_id=?", processId);
        if (workspace == null) {
            jdbc.update("UPDATE conservation_process SET total_steps=0,completed_steps=0,progress=0 WHERE id=?", processId);
            return;
        }
        List<Map<String, Object>> measures = list(workspace.get("planDiseaseList"));
        List<Map<String, Object>> diseases = list(workspace.get("diseaseRecords"));
        List<Map<String, Object>> planMaterials = list(workspace.get("planMaterials"));
        long baseId = System.currentTimeMillis();
        int sequence = 1;
        for (Map<String, Object> measure : measures) {
            Map<String, Object> step = defaultStep(baseId + sequence, sequence, measure, diseases, planMaterials);
            insertStep(processId, projectId, step);
            sequence++;
        }
        jdbc.update("UPDATE conservation_process SET total_steps=?,completed_steps=0,progress=0 WHERE id=?", measures.size(), processId);
    }

    private Map<String, Object> defaultStep(Long id, int sequence, Map<String, Object> measure,
                                            List<Map<String, Object>> diseases, List<Map<String, Object>> planMaterials) {
        List<Long> diseaseIds = new ArrayList<>();
        Object ids = measure.get("diseaseRecordIds");
        if (ids instanceof List<?> list) for (Object item : list) diseaseIds.add(longValue(item));
        else if (measure.get("diseaseRecordId") != null) diseaseIds.add(longValue(measure.get("diseaseRecordId")));
        List<Map<String, Object>> related = diseases.stream()
            .filter(item -> diseaseIds.contains(longValue(item.get("id"))))
            .map(item -> mapOf("id", item.get("id"), "diseaseRecordId", item.get("id"),
                "diseaseName", item.get("diseaseName"), "severity", item.get("severity"),
                "developmentStatus", item.get("developmentStatus"), "partName", item.get("partName"),
                "treatmentStatus", "untreated", "monitoringRequired", false)).toList();
        String method = text(measure.get("treatmentStrategy"));
        String name = method.isBlank() ? "方案措施" + sequence : method.split("[、，,；;]")[0];
        Map<String, Object> step = mapOf(
            "id", id, "processId", null, "planMeasureId", measure.get("id"), "parentStepId", null,
            "stepCode", "STEP-" + String.format("%02d", sequence), "stepName", name,
            "stepType", "other", "stepStatus", "pending", "sequenceNo", sequence, "progressWeight", 10,
            "plannedStartTime", "", "plannedEndTime", "", "actualStartTime", "", "actualEndTime", "",
            "operatorName", "", "assistantNames", "", "operationLocation", "文物保护实验室",
            "targetPart", diseaseParts(related, text(measure.get("diseaseName"))), "plannedMethod", method, "actualMethod", "",
            "operationDescription", "", "resultDescription", "", "deviationFlag", false,
            "deviationLevel", "minor", "deviationReason", "", "adjustmentDescription", "",
            "requiresMedia", true, "requiresQualityCheck", true, "requiresMonitoring", false,
            "generateRestorationResult", false, "qualityStatus", "", "completionRate", 0,
            "temporaryStep", false, "temporaryReason", "", "nonDiseaseStep", related.isEmpty(),
            "noMaterialRequired", false, "requiresPlanChange", false,
            "planMaterials", new ArrayList<>(planMaterials), "relatedDiseases", new ArrayList<>(related),
            "operationLogs", new ArrayList<>(), "materials", new ArrayList<>(), "tools", new ArrayList<>(),
            "processParameters", new ArrayList<>(), "environments", new ArrayList<>(), "media", new ArrayList<>(),
            "issues", new ArrayList<>(), "qualityChecks", new ArrayList<>(),
            "result", mapOf("targetCompleted", "", "diseaseTreatmentEffect", "", "artifactStateChange", "",
                "actualResult", "", "expectedReached", "", "sideEffects", "", "remainingProblems", "",
                "allowNextStep", true, "monitoringRequired", false, "monitoringObject", "",
                "monitoringIndicators", "", "monitoringCycle", "", "warningConditions", "",
                "monitoringAdvice", "", "finalConclusion", "")
        );
        return step;
    }

    /**
     * 修复步骤只保存病害记录 ID；名称、部位和风险属性以病害调查的当前数据为准。
     * 这样病害调查被修改后，过程记录重新加载即可获得同步后的引用信息。
     */
    private void refreshDiseaseReferences(List<Map<String, Object>> steps, List<Map<String, Object>> diseaseRecords) {
        if (steps.isEmpty() || diseaseRecords.isEmpty()) return;
        Map<Long, Map<String, Object>> diseasesById = new HashMap<>();
        for (Map<String, Object> record : diseaseRecords) {
            Long id = longValue(record.get("id"));
            if (id != null) diseasesById.put(id, record);
        }
        for (Map<String, Object> step : steps) {
            List<Map<String, Object>> related = list(step.get("relatedDiseases"));
            if (related.isEmpty()) continue;
            List<String> oldNames = related.stream().map(item -> text(item.get("diseaseName")))
                .filter(name -> !name.isBlank()).toList();
            String oldDiseaseParts = diseaseParts(related, "");
            boolean generatedTargetPart = !bool(step.get("temporaryStep"))
                && (text(step.get("targetPart")).isBlank()
                    || text(step.get("targetPart")).equals(String.join("、", oldNames))
                    || text(step.get("targetPart")).equals(oldDiseaseParts));
            boolean changed = false;
            for (Map<String, Object> relation : related) {
                Map<String, Object> current = diseasesById.get(longValue(relation.get("diseaseRecordId")));
                if (current == null) continue;
                for (String key : List.of("diseaseName", "severity", "developmentStatus", "partName")) {
                    Object value = current.get(key);
                    if (!Objects.equals(relation.get(key), value)) {
                        relation.put(key, value);
                        changed = true;
                    }
                }
            }
            if (generatedTargetPart) {
                String currentTargetPart = diseaseParts(related, "");
                if (!currentTargetPart.isBlank() && !currentTargetPart.equals(text(step.get("targetPart")))) {
                    step.put("targetPart", currentTargetPart);
                    changed = true;
                }
            }
            if (changed) {
                namedJdbc.update("UPDATE conservation_process_step SET target_part=:targetPart,step_json=:stepJson WHERE id=:id",
                    new MapSqlParameterSource().addValue("id", step.get("id"))
                        .addValue("targetPart", step.get("targetPart")).addValue("stepJson", json(step)));
            }
        }
    }

    private String diseaseParts(List<Map<String, Object>> diseases, String fallback) {
        List<String> parts = diseases.stream().map(item -> text(item.get("partName")))
            .filter(part -> !part.isBlank()).distinct().toList();
        return parts.isEmpty() ? fallback : String.join("、", parts);
    }

    private void insertStep(Long processId, Long projectId, Map<String, Object> step) {
        Long id = longValue(step.get("id"));
        if (id == null) {
            id = System.currentTimeMillis() + Math.abs(new Random().nextInt(100000));
            step.put("id", id);
        }
        step.put("processId", processId);
        namedJdbc.update("""
            INSERT INTO conservation_process_step
            (id,process_id,project_id,step_code,step_name,step_type,step_status,sequence_no,
             progress_weight,operator_name,target_part,planned_start_time,planned_end_time,
             actual_start_time,actual_end_time,completion_rate,temporary_step,requires_monitoring,step_json)
            VALUES (:id,:processId,:projectId,:stepCode,:stepName,:stepType,:stepStatus,:sequenceNo,
             :progressWeight,:operatorName,:targetPart,:plannedStartTime,:plannedEndTime,
             :actualStartTime,:actualEndTime,:completionRate,:temporaryStep,:requiresMonitoring,:stepJson)
            """, params(step).addValue("id", id).addValue("processId", processId).addValue("projectId", projectId)
            .addValue("plannedStartTime", dateTime(step.get("plannedStartTime")))
            .addValue("plannedEndTime", dateTime(step.get("plannedEndTime")))
            .addValue("actualStartTime", dateTime(step.get("actualStartTime")))
            .addValue("actualEndTime", dateTime(step.get("actualEndTime")))
            .addValue("stepJson", json(step)));
        for (Map<String, Object> media : list(step.get("media"))) {
            Long mediaId = longValue(media.get("id"));
            if (mediaId == null) continue;
            namedJdbc.update("""
                UPDATE conservation_process_media SET media_stage=:mediaStage,title=:title,
                 shooting_time=:shootingTime,shooting_position=:shootingPosition,target_part=:targetPart,
                 photographer=:photographer,description=:description,
                 selected_for_comparison=:selectedForComparison,selected_for_archive=:selectedForArchive,
                 selected_for_restoration=:selectedForRestoration
                WHERE id=:id AND step_id=:stepId
                """, new MapSqlParameterSource(media).addValue("id", mediaId).addValue("stepId", id)
                .addValue("shootingTime", dateTime(media.get("shootingTime"))));
        }
    }

    private void syncProjectAndArchive(Long projectId, Map<String, Object> process, List<Map<String, Object>> steps) {
        String status = text(process.get("processStatus"));
        String stage = "completed".equals(status) ? "evaluation" : "not_started".equals(status) ? "planning" : "repair";
        int progress = intValue(process.get("progress"));
        jdbc.update("UPDATE conservation_project SET current_stage=?,progress=GREATEST(progress,?),update_time=NOW() WHERE id=?",
            stage, Math.min(90, 30 + progress * 60 / 100), projectId);
        Map<String, Object> archiveWorkbench = archiveService.getWorkbench(projectId);
        Map<String, Object> archive = map(archiveWorkbench.get("archive"));
        Map<String, Object> workspace = map(archiveWorkbench.get("workspace"));
        if (archive == null || workspace == null) return;
        workspace.put("processSummary", mapOf(
            "planned", steps.size(),
            "completed", steps.stream().filter(x -> Set.of("completed", "skipped").contains(text(x.get("stepStatus")))).count(),
            "processing", steps.stream().filter(x -> "in_progress".equals(text(x.get("stepStatus")))).count(),
            "pending", steps.stream().filter(x -> "pending".equals(text(x.get("stepStatus")))).count(),
            "progress", progress, "processStatus", status,
            "steps", steps.stream().map(x -> mapOf("id", x.get("id"), "title", x.get("stepName"),
                "name", x.get("stepName"), "status", x.get("stepStatus"), "statusText", x.get("stepStatus"),
                "date", x.get("actualStartTime"), "operator", x.get("operatorName"),
                "imageCount", list(x.get("media")).size())).toList()
        ));
        archiveService.save(longValue(archive.get("id")), mapOf(
            "archive", archive, "workspace", workspace, "completeness", archive.get("completenessRate")
        ));
    }

    private List<Map<String, Object>> media(Long stepId) {
        return jdbc.query("""
            SELECT id,media_stage AS mediaStage,original_name AS fileName,file_size AS fileSize,title,
                   shooting_time AS shootingTime,shooting_position AS shootingPosition,target_part AS targetPart,
                   photographer,description,selected_for_comparison AS selectedForComparison,
                   selected_for_archive AS selectedForArchive,selected_for_restoration AS selectedForRestoration,
                   COALESCE(file_url,CONCAT('/api/conservation/process-media/',id,'/content')) AS fileUrl
            FROM conservation_process_media WHERE step_id=? ORDER BY create_time,id
            """, this::camelMap, stepId);
    }

    private Map<String, Object> mediaMetadata(Long id) {
        return one("""
            SELECT id,media_stage AS mediaStage,original_name AS fileName,file_size AS fileSize,title,
                   shooting_time AS shootingTime,shooting_position AS shootingPosition,target_part AS targetPart,
                   photographer,description,selected_for_comparison AS selectedForComparison,
                   selected_for_archive AS selectedForArchive,selected_for_restoration AS selectedForRestoration,
                   COALESCE(file_url,CONCAT('/api/conservation/process-media/',id,'/content')) AS fileUrl
            FROM conservation_process_media WHERE id=?
            """, id);
    }

    private int unresolvedIssueCount(Long processId) {
        int count = 0;
        for (Map<String, Object> row : jdbc.query("SELECT step_json AS stepJson FROM conservation_process_step WHERE process_id=?", this::camelMap, processId)) {
            Map<String, Object> step = jsonMap(row.get("stepJson"));
            count += (int) list(step.get("issues")).stream()
                .filter(x -> !Set.of("resolved", "closed").contains(text(x.get("issueStatus")))).count();
        }
        return count;
    }

    private Map<String, Object> requireProcess(Long id) {
        Map<String, Object> process = one("SELECT id,project_id AS projectId,process_status AS processStatus FROM conservation_process WHERE id=? AND deleted=0", id);
        if (process == null) throw new IllegalArgumentException("修复过程不存在");
        return process;
    }

    private MapSqlParameterSource params(Map<String, Object> source) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        source.forEach(p::addValue);
        String[] keys = {"processName","processStatus","executionMode","supervisor","totalSteps","completedSteps",
            "progress","executionSummary","finalResult","stepCode","stepName","stepType","stepStatus","sequenceNo",
            "progressWeight","operatorName","targetPart","completionRate","temporaryStep","requiresMonitoring"};
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
            if (value instanceof Number n && Set.of("selectedForComparison","selectedForArchive",
                "selectedForRestoration").contains(key)) value = n.intValue() != 0;
            result.put(key, value);
        }
        return result;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException("修复过程数据无法序列化", e); }
    }

    private Map<String, Object> jsonMap(Object value) {
        if (value == null || value.toString().isBlank()) return new LinkedHashMap<>();
        try { return objectMapper.readValue(value.toString(), new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("数据库中的修复过程数据无法解析", e); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
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

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private static Long longValue(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        if (value instanceof Number number) {
            return Long.valueOf(number.longValue());
        }
        return Long.valueOf(value.toString());
    }

    private int intValue(Object value) {
        return value instanceof Number n ? n.intValue()
            : value == null || value.toString().isBlank() ? 0 : Integer.parseInt(value.toString());
    }

    private boolean bool(Object value) {
        return value instanceof Boolean b ? b : "true".equalsIgnoreCase(Objects.toString(value, ""));
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
        if (text.length() == 10) return date(text).atStartOfDay();
        try { return LocalDateTime.parse(text.substring(0, Math.min(19, text.length())), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
        catch (Exception e) { return null; }
    }
}
