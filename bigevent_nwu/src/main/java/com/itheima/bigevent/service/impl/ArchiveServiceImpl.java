package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.utils.ConservationOssStorage;
import com.itheima.bigevent.service.DiseaseSurveyService;
import com.itheima.bigevent.service.MonitoringService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
public class ArchiveServiceImpl implements ArchiveService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;
    private final DiseaseSurveyService diseaseService;
    private final MonitoringService monitoringService;
    private final ArchiveDocumentExporter documentExporter;

    public ArchiveServiceImpl(JdbcTemplate jdbc, ObjectMapper objectMapper,
                              DiseaseSurveyService diseaseService, MonitoringService monitoringService,
                              ArchiveDocumentExporter documentExporter) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.objectMapper = objectMapper;
        this.diseaseService = diseaseService;
        this.monitoringService = monitoringService;
        this.documentExporter = documentExporter;
    }

    @Override
    public Map<String, Object> getWorkbench(Long projectId) {
        Map<String, Object> project = monitoringService.getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        Map<String, Object> archive = one("""
            SELECT id,project_id AS projectId,artifact_id AS artifactId,archive_code AS archiveCode,
                   archive_title AS archiveTitle,archive_type AS archiveType,compiler,
                   CASE WHEN archive_status NOT IN ('compiling','completed','archived')
                        THEN 'compiling' ELSE archive_status END AS archiveStatus,
                   current_version AS currentVersion,executive_summary AS executiveSummary,
                   protection_goal AS protectionGoal,conservation_basis AS conservationBasis,
                   final_conclusion AS finalConclusion,source_survey_id AS sourceSurveyId,
                   source_survey_version AS sourceSurveyVersion,compiled_date AS compiledDate,
                   completeness_rate AS completenessRate,workspace_json AS workspaceJson,
                   update_time AS updateTime
            FROM conservation_archive WHERE project_id=? AND deleted=0
            """, projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", project);
        result.put("artifact", artifactInfo(longValue(project.get("artifactId"))));
        if (archive == null) {
            result.put("archive", null);
            result.put("workspace", null);
            result.put("revisions", List.of());
            return result;
        }
        Map<String, Object> workspace = jsonMap(archive.remove("workspaceJson"));
        Map<String, Object> plan = map(workspace.get("plan"));
        if (plan != null && !Set.of("draft", "completed").contains(text(plan.get("planStatus")))) {
            plan.put("planStatus", "completed");
        }
        if (plan != null) plan.remove("reviewer");
        mergeCurrentSurvey(projectId, archive, workspace);
        workspace.put("attachments", attachments(longValue(archive.get("id"))));
        result.put("archive", archive);
        result.put("workspace", workspace);
        result.put("revisions", revisions(longValue(archive.get("id"))));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> create(Long projectId, Map<String, Object> data) {
        if (one("SELECT id FROM conservation_archive WHERE project_id=? AND deleted=0", projectId) != null) {
            return getWorkbench(projectId);
        }
        Map<String, Object> project = monitoringService.getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        Map<String, Object> workspace = defaultWorkspace(projectId);
        String artifactCode = text(project.get("artifactCode"));
        String archiveCode = "CA-" + (artifactCode.isBlank() ? "PROJECT" + projectId
            : artifactCode.replaceAll("[^A-Za-z0-9]", "")) + "-" + String.format("%03d", projectId);
        String title = text(project.get("artifactName")) + "保护修复档案";
        var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        namedJdbc.update("""
            INSERT INTO conservation_archive
            (project_id,artifact_id,archive_code,archive_title,archive_type,compiler,archive_status,
             current_version,compiled_date,workspace_json)
            VALUES (:projectId,:artifactId,:archiveCode,:archiveTitle,'comprehensive',:compiler,
                    'compiling','V1.0',:compiledDate,:workspaceJson)
            """, new MapSqlParameterSource()
            .addValue("projectId", projectId).addValue("artifactId", project.get("artifactId"))
            .addValue("archiveCode", archiveCode).addValue("archiveTitle", title)
            .addValue("compiler", text(project.get("principal"))).addValue("compiledDate", LocalDate.now())
            .addValue("workspaceJson", json(workspace)), key, new String[]{"id"});
        Long archiveId = key.getKey().longValue();
        insertRevision(archiveId, "V1.0", "initial", "建立保护修复档案并初始化业务引用",
            text(project.get("principal")), Map.of("archiveCode", archiveCode, "archiveTitle", title), workspace);
        return getWorkbench(projectId);
    }

    @Override
    public List<Map<String, Object>> getDetectionCandidates(Long projectId) {
        Map<String, Object> project = monitoringService.getProject(projectId);
        if (project == null) {
            throw new IllegalArgumentException("保护修复项目不存在");
        }
        String artifactCode = text(project.get("artifactCode")).trim();
        String artifactName = text(project.get("artifactName")).trim();
        if (artifactCode.isBlank() && artifactName.isBlank()) return List.of();
        String artifactCondition = artifactCode.isBlank()
            ? "TRIM(d.artifact_name)=?"
            : "TRIM(d.artifact_code)=?";
        String artifactValue = artifactCode.isBlank() ? artifactName : artifactCode;
        List<Map<String, Object>> candidates = jdbc.query("""
            SELECT ar.id,ar.id AS analysisResultId,ar.detection_id AS detectionId,
                   er.id AS experimentResultId,
                   COALESCE(NULLIF(ar.artifact_code,''),d.artifact_code) AS artifactCode,
                   COALESCE(NULLIF(ar.artifact_name,''),d.artifact_name) AS artifactName,
                   COALESCE(NULLIF(er.experiment_name,''),NULLIF(ar.experiment_method,''),'综合检测') AS experimentName,
                   COALESCE(NULLIF(d.instrument_name,''),NULLIF(ar.experiment_method,''),'检测分析') AS experimentType,
                   COALESCE(NULLIF(ar.experiment_method,''),NULLIF(d.sample_method,''),'未填写') AS method,
                   COALESCE(NULLIF(ar.instrument_model,''),NULLIF(d.instrument_model,''),'未填写') AS instrumentModel,
                   COALESCE(NULLIF(ar.test_params,''),d.test_params) AS testParams,
                   COALESCE(NULLIF(ar.detection_purpose,''),d.purpose) AS detectionPurpose,
                   d.sample_position AS samplePosition,
                   d.sample_material AS sampleMaterial,d.sample_status AS sampleStatus,
                   d.manager,d.sampler,er.status AS resultStatus,er.result_data AS resultDataJson,
                   er.notes AS notesJson,er.attachments AS attachmentsJson,er.images AS imagesJson,
                   d.analysis_report AS analysisReport,
                   COALESCE(er.update_time,ar.update_time,d.update_time,d.create_time) AS detectionTime
            FROM detection_analysis d
            JOIN analysis_results ar ON ar.detection_id=d.id
            LEFT JOIN experiment_results er ON er.detection_id=ar.detection_id
                 AND COALESCE(er.experiment_name,'')=COALESCE(ar.experiment_method,'')
            WHERE %s
            ORDER BY COALESCE(er.update_time,ar.update_time,d.update_time,d.create_time) DESC,ar.id DESC
            """.formatted(artifactCondition), this::camelMap, artifactValue);
        for (Map<String, Object> candidate : candidates) normalizeDetectionCandidate(candidate);
        return candidates;
    }

    @Override
    @Transactional
    public Map<String, Object> save(Long archiveId, Map<String, Object> data) {
        Map<String, Object> current = requireArchive(archiveId);
        Map<String, Object> archive = map(data.get("archive"));
        Map<String, Object> workspace = map(data.get("workspace"));
        if (workspace == null) throw new IllegalArgumentException("档案编制内容不能为空");
        if (archive == null) archive = new HashMap<>();
        namedJdbc.update("""
            UPDATE conservation_archive SET archive_title=:archiveTitle,archive_type=:archiveType,
              compiler=:compiler,
              executive_summary=:executiveSummary,protection_goal=:protectionGoal,
              conservation_basis=:conservationBasis,final_conclusion=:finalConclusion,
              completeness_rate=:completenessRate,workspace_json=:workspaceJson
            WHERE id=:id AND deleted=0
            """, params(archive).addValue("id", archiveId)
            .addValue("completenessRate", intValue(data.get("completeness")))
            .addValue("workspaceJson", json(workspace)));
        syncAdvice(longValue(current.get("projectId")), workspace);
        return getWorkbench(longValue(current.get("projectId")));
    }

    @Override
    @Transactional
    public Map<String, Object> finalizeArchive(Long archiveId, Map<String, Object> data) {
        Map<String, Object> saved = save(archiveId, data);
        Map<String, Object> archive = map(saved.get("archive"));
        Map<String, Object> workspace = map(saved.get("workspace"));
        jdbc.update("UPDATE conservation_archive SET archive_status='completed' WHERE id=?", archiveId);
        archive.put("archiveStatus", "completed");
        insertRevision(archiveId, text(archive.get("currentVersion")), "final", "档案定稿并生成内容快照",
            text(archive.get("compiler")), archive, workspace);
        return getWorkbench(longValue(archive.get("projectId")));
    }

    @Override
    public Map<String, Object> exportArchive(Long archiveId, String format) {
        Map<String, Object> archive = requireArchive(archiveId);
        return documentExporter.export(getWorkbench(longValue(archive.get("projectId"))), format);
    }

    @Override
    @Transactional
    public Map<String, Object> createRevision(Long archiveId, Map<String, Object> data) {
        Map<String, Object> saved = save(archiveId, data);
        Map<String, Object> archive = map(saved.get("archive"));
        Map<String, Object> workspace = map(saved.get("workspace"));
        String version = text(data.get("versionNo"));
        if (version.isBlank()) throw new IllegalArgumentException("版本号不能为空");
        jdbc.update("UPDATE conservation_archive SET current_version=? WHERE id=?", version, archiveId);
        archive.put("currentVersion", version);
        insertRevision(archiveId, version, "manual", text(data.get("revisionDescription")),
            text(data.get("operator")), archive, workspace);
        return getWorkbench(longValue(archive.get("projectId")));
    }

    @Override
    public Map<String, Object> uploadAttachment(Long archiveId, MultipartFile file, Map<String, String> metadata) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的文件");
        if (file.getSize() > 50L * 1024 * 1024) throw new IllegalArgumentException("单个文件不能超过50MB");
        Map<String, Object> archive = requireArchive(archiveId);
        try {
            Map<String, String> stored = ConservationOssStorage.upload("archive-attachments", file);
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_archive_attachment
                (archive_id,project_id,file_name,file_type,content_type,file_size,file_url,oss_object_key,
                 source_module,section_name,version_no,description,uploaded_by)
                VALUES (:archiveId,:projectId,:fileName,:fileType,:contentType,:fileSize,:fileUrl,:ossObjectKey,
                 :sourceModule,:sectionName,:versionNo,:description,:uploadedBy)
                """, new MapSqlParameterSource()
                .addValue("archiveId", archiveId).addValue("projectId", archive.get("projectId"))
                .addValue("fileName", Optional.ofNullable(file.getOriginalFilename()).orElse("attachment"))
                .addValue("fileType", metadata.get("fileType"))
                .addValue("contentType", Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                .addValue("fileSize", file.getSize()).addValue("fileUrl", stored.get("fileUrl"))
                .addValue("ossObjectKey", stored.get("objectKey"))
                .addValue("sourceModule", metadata.get("sourceModule")).addValue("sectionName", metadata.get("sectionName"))
                .addValue("versionNo", metadata.get("version")).addValue("description", metadata.get("description"))
                .addValue("uploadedBy", metadata.get("uploadedBy")), key, new String[]{"id"});
            return attachmentMetadata(key.getKey().longValue());
        } catch (Exception e) {
            throw new IllegalStateException("附件上传到 OSS 失败", e);
        }
    }

    @Override
    public Map<String, Object> getAttachment(Long attachmentId) {
        return one("SELECT file_name AS fileName,content_type AS contentType,file_data AS fileData,file_url AS fileUrl FROM conservation_archive_attachment WHERE id=?", attachmentId);
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        jdbc.update("DELETE FROM conservation_archive_attachment WHERE id=?", attachmentId);
    }

    private Map<String, Object> defaultWorkspace(Long projectId) {
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("surveyReferenceUpdated", false);
        mergeCurrentSurvey(projectId, new HashMap<>(), w);
        w.put("detectionReferences", new ArrayList<>());
        w.put("principles", mapOf("options", List.of("最小干预原则", "保持原状原则", "历史真实性原则",
            "可辨识性原则", "可再处理性原则", "兼容性原则", "安全性原则", "预防性保护原则"),
            "selected", new ArrayList<>(), "notes", new LinkedHashMap<>(), "custom", ""));
        w.put("goals", emptyMap("overall","diseaseControl","structuralStability","appearance","informationRetention","displayUse","longTerm"));
        w.put("plan", emptyMap("planCode","planName","planGoal","technicalBasis","selectedMethod","expectedResult",
            "riskAnalysis","safetyRequirements","environmentRequirements","reversibilityDescription",
            "compatibilityDescription","emergencyMeasures","compiler","compiledDate"));
        map(w.get("plan")).put("planStatus", "draft");
        w.put("planDiseaseList", new ArrayList<>());
        w.put("materialTab", "materials");
        w.put("materialDictionary", new ArrayList<>());
        w.put("planMaterials", new ArrayList<>());
        w.put("tools", new ArrayList<>());
        w.put("processParameters", emptyMap("operationMethod","applicationOrder","materialConcentration",
            "dryingTime","operationTimes","temperature","humidity","parameterLimit","qualityControl","emergencyRequirement"));
        w.put("processSummary", mapOf("planned", 0, "completed", 0, "processing", 0, "pending", 0, "steps", new ArrayList<>()));
        w.put("comparisons", new ArrayList<>());
        w.put("restorationResults", new ArrayList<>());
        w.put("evaluation", emptyMap("diseaseControl","structuralChange","surfaceStrength","appearanceCoordination",
            "colorChange","glossChange","materialCompatibility","goalAchievement","remainingIssues",
            "acceptanceConclusion","evaluator","evaluationDate"));
        map(w.get("evaluation")).put("hasSideEffects", "pending");
        map(w.get("evaluation")).put("retestIds", new ArrayList<>());
        w.put("retestOptions", new ArrayList<>());
        w.put("advice", emptyMap("temperatureRange","humidityRange","lighting","airQuality","packaging","display",
            "handling","shockproof","reviewCycle","monitorDiseases","monitoringIndicators","followUpAdvice","warningConditions"));
        w.put("attachments", new ArrayList<>());
        return w;
    }

    private Map<String, Object> artifactInfo(Long artifactId) {
        if (artifactId == null) return null;
        return one("""
            SELECT a.id,
                   COALESCE(NULLIF(a.new_artifact_code,''),a.original_artifact_code,'') AS artifactCode,
                   COALESCE(NULLIF(a.new_artifact_name,''),a.original_artifact_name,'') AS artifactName,
                   CONCAT_WS('、',NULLIF(a.material1,''),NULLIF(a.material2,'')) AS material,
                   a.completeness,a.artifact_description AS artifactDescription,
                   a.quantity1,a.quantity2,a.dimensions,a.weight,
                   a.excavation_relic AS excavationRelic,a.excavation_position AS excavationPosition,
                   a.excavation_time AS excavationTime,a.storage_method AS storageMethod,
                   a.transfer_process AS transferProcess,a.restoration_status AS restorationStatus,
                   a.photographer,a.draftsperson,a.text_describer AS textDescriber,
                   a.notes,a.grading_status AS gradingStatus,
                   COALESCE(
                       (SELECT image_url FROM artifact_image image
                        WHERE image.artifact_id=a.id AND image.deleted=0
                        ORDER BY image.is_cover DESC,image.sort_order ASC,image.id ASC LIMIT 1),
                       NULLIF(a.images,'')
                   ) AS coverImageUrl
            FROM artifacts a WHERE a.id=?
            """, artifactId);
    }

    private void mergeCurrentSurvey(Long projectId, Map<String, Object> archive, Map<String, Object> workspace) {
        Map<String, Object> disease = diseaseService.getWorkbench(projectId);
        Map<String, Object> survey = map(disease.get("survey"));
        workspace.put("survey", survey == null ? mapOf("status", "draft") : survey);
        workspace.put("diseaseRecords", disease.getOrDefault("records", new ArrayList<>()));
        if (survey != null) {
            archive.put("sourceSurveyId", survey.get("id"));
            archive.put("sourceSurveyVersion", "V1.0");
        }
    }

    private void syncAdvice(Long projectId, Map<String, Object> workspace) {
        Map<String, Object> advice = map(workspace.get("advice"));
        if (advice == null) return;
        namedJdbc.update("""
            INSERT INTO conservation_archive_advice
            (project_id,temperature_range,humidity_range,lighting,air_quality,packaging,handling,
             shockproof,review_cycle,monitor_diseases,monitoring_indicators,follow_up_advice,warning_conditions)
            VALUES (:projectId,:temperatureRange,:humidityRange,:lighting,:airQuality,:packaging,:handling,
             :shockproof,:reviewCycle,:monitorDiseases,:monitoringIndicators,:followUpAdvice,:warningConditions)
            ON DUPLICATE KEY UPDATE temperature_range=VALUES(temperature_range),humidity_range=VALUES(humidity_range),
             lighting=VALUES(lighting),air_quality=VALUES(air_quality),packaging=VALUES(packaging),
             handling=VALUES(handling),shockproof=VALUES(shockproof),review_cycle=VALUES(review_cycle),
             monitor_diseases=VALUES(monitor_diseases),monitoring_indicators=VALUES(monitoring_indicators),
             follow_up_advice=VALUES(follow_up_advice),warning_conditions=VALUES(warning_conditions)
            """, params(advice).addValue("projectId", projectId));
    }

    private void normalizeDetectionCandidate(Map<String, Object> candidate) {
        Map<String, Object> notes = jsonObject(candidate.remove("notesJson"));
        List<Map<String, Object>> attachments = jsonObjects(candidate.remove("attachmentsJson"));
        Object resultData = candidate.remove("resultDataJson");
        Object images = candidate.remove("imagesJson");
        Object analysisReport = candidate.remove("analysisReport");
        String conclusion = firstText(notes.get("conclusion"), notes.get("analysis"), notes.get("remark"));
        candidate.put("conclusion", conclusion.isBlank() ? "尚未填写实验结论" : conclusion);
        candidate.put("reportName", attachments.isEmpty()
            ? firstText(analysisReport)
            : firstText(attachments.getFirst().get("name"), attachments.getFirst().get("fileName")));
        candidate.put("detector", firstText(candidate.get("manager"), candidate.get("sampler")));
        candidate.put("purpose", firstText(candidate.get("detectionPurpose"), "保护修复技术依据"));
        candidate.put("relatedDisease", "");
        candidate.put("detectionDate", Optional.ofNullable(candidate.remove("detectionTime"))
            .map(Object::toString).map(value -> value.substring(0, Math.min(10, value.length()))).orElse(""));
        candidate.put("sourceBusinessType", "analysis_result");
        candidate.put("sourceBusinessId", candidate.get("analysisResultId"));
        candidate.put("hasResult", !conclusion.isBlank() || !text(resultData).isBlank()
            || !attachments.isEmpty() || !text(images).isBlank());
        candidate.remove("manager");
        candidate.remove("sampler");
        candidate.remove("detectionPurpose");
    }

    private Map<String, Object> jsonObject(Object value) {
        if (value == null || value.toString().isBlank()) return new LinkedHashMap<>();
        try { return objectMapper.readValue(value.toString(), new TypeReference<>() {}); }
        catch (Exception ignored) { return new LinkedHashMap<>(); }
    }

    private List<Map<String, Object>> jsonObjects(Object value) {
        if (value == null || value.toString().isBlank()) return new ArrayList<>();
        try { return objectMapper.readValue(value.toString(), new TypeReference<>() {}); }
        catch (Exception ignored) { return new ArrayList<>(); }
    }

    private String firstText(Object... values) {
        for (Object value : values) {
            String result = text(value);
            if (!result.isBlank()) return result;
        }
        return "";
    }

    private void insertRevision(Long archiveId, String version, String type, String description,
                                String operator, Map<String, Object> archive, Map<String, Object> workspace) {
        jdbc.update("""
            INSERT INTO conservation_archive_revision
            (archive_id,version_no,revision_type,revision_description,operator,
             archive_snapshot_json,workspace_snapshot_json) VALUES (?,?,?,?,?,CAST(? AS JSON),CAST(? AS JSON))
            """, archiveId, version, type, description, operator, json(archive), json(workspace));
    }

    private List<Map<String, Object>> revisions(Long id) {
        return jdbc.query("""
            SELECT id,version_no AS versionNo,revision_type AS revisionType,
                   revision_description AS revisionDescription,operator,create_time AS createTime
            FROM conservation_archive_revision WHERE archive_id=? ORDER BY create_time DESC,id DESC
            """, this::camelMap, id);
    }

    private List<Map<String, Object>> attachments(Long id) {
        return jdbc.query("""
            SELECT id,file_name AS fileName,file_type AS fileType,file_size AS fileSize,
                   source_module AS sourceModule,section_name AS sectionName,uploaded_by AS uploadedBy,
                   create_time AS uploadTime,version_no AS version,description,
                   COALESCE(file_url,CONCAT('/api/conservation/archive-attachments/',id,'/content')) AS fileUrl
            FROM conservation_archive_attachment WHERE archive_id=? ORDER BY create_time DESC,id DESC
            """, this::camelMap, id);
    }

    private Map<String, Object> attachmentMetadata(Long id) {
        return one("""
            SELECT id,file_name AS fileName,file_type AS fileType,file_size AS fileSize,
                   source_module AS sourceModule,section_name AS sectionName,uploaded_by AS uploadedBy,
                   create_time AS uploadTime,version_no AS version,description,
                   COALESCE(file_url,CONCAT('/api/conservation/archive-attachments/',id,'/content')) AS fileUrl
            FROM conservation_archive_attachment WHERE id=?
            """, id);
    }

    private Map<String, Object> requireArchive(Long id) {
        Map<String, Object> result = one("SELECT id,project_id AS projectId FROM conservation_archive WHERE id=? AND deleted=0", id);
        if (result == null) throw new IllegalArgumentException("保护修复档案不存在");
        return result;
    }

    private MapSqlParameterSource params(Map<String, Object> source) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        source.forEach(p::addValue);
        String[] keys = {"archiveTitle","archiveType","compiler","executiveSummary",
            "protectionGoal","conservationBasis","finalConclusion","temperatureRange","humidityRange","lighting",
            "airQuality","packaging","handling","shockproof","reviewCycle","monitorDiseases",
            "monitoringIndicators","followUpAdvice","warningConditions"};
        for (String key : keys) if (!p.hasValue(key)) p.addValue(key, null);
        return p;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException("档案内容不是有效的JSON数据", e); }
    }

    private Map<String, Object> jsonMap(Object value) {
        if (value == null || value.toString().isBlank()) return new LinkedHashMap<>();
        try { return objectMapper.readValue(value.toString(), new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("数据库中的档案内容无法解析", e); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private Map<String, Object> emptyMap(String... keys) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : keys) result.put(key, "");
        return result;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.query(sql, this::camelMap, args);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private Map<String, Object> camelMap(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) result.put(meta.getColumnLabel(i), rs.getObject(i));
        return result;
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

    private String text(Object value) {
        return Objects.toString(value, "").trim();
    }
}
