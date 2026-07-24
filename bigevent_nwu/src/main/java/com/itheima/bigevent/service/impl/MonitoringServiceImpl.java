package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.service.MonitoringService;
import com.itheima.bigevent.utils.ConservationOssStorage;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class MonitoringServiceImpl implements MonitoringService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public MonitoringServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    @PostConstruct
    public void ensureAlertProjectColumns() {
        addColumnIfMissing("conservation_project", "source_project_id",
            "ALTER TABLE conservation_project ADD COLUMN source_project_id INT COMMENT '来源保护修复项目ID'");
        addColumnIfMissing("conservation_project", "source_alert_id",
            "ALTER TABLE conservation_project ADD COLUMN source_alert_id BIGINT COMMENT '来源监测预警ID'");
        addColumnIfMissing("conservation_project", "source_monitoring_record_id",
            "ALTER TABLE conservation_project ADD COLUMN source_monitoring_record_id BIGINT COMMENT '来源监测记录ID'");
        addColumnIfMissing("monitoring_alert", "created_project_id",
            "ALTER TABLE monitoring_alert ADD COLUMN created_project_id INT COMMENT '由预警创建的保护修复项目ID'");
        addColumnIfMissing("monitoring_alert", "project_created_time",
            "ALTER TABLE monitoring_alert ADD COLUMN project_created_time DATETIME COMMENT '保护修复项目创建时间'");
        addColumnIfMissing("conservation_project", "record_mode",
            "ALTER TABLE conservation_project ADD COLUMN record_mode VARCHAR(20) DEFAULT 'standard' COMMENT '建档模式：quick/standard/full'");
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS conservation_quick_record (
                id BIGINT PRIMARY KEY AUTO_INCREMENT, project_id INT NOT NULL,
                issue_description TEXT, treatment_method TEXT, operator_name VARCHAR(100), record_date DATE,
                conclusion TEXT, remark TEXT, record_status VARCHAR(20) DEFAULT 'draft',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY uk_quick_record_project (project_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS conservation_quick_record_media (
                id BIGINT PRIMARY KEY AUTO_INCREMENT, quick_record_id BIGINT NOT NULL,
                media_role VARCHAR(20) NOT NULL, original_name VARCHAR(255) NOT NULL,
                content_type VARCHAR(120), file_size BIGINT, file_url VARCHAR(1000) NOT NULL,
                oss_object_key VARCHAR(600), description VARCHAR(1000),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_quick_record_media_record (quick_record_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
    }

    @Override
    public List<Map<String, Object>> getProjects() {
        List<Map<String, Object>> projects = jdbc.query("""
            SELECT p.id, p.project_code AS projectCode, p.project_name AS projectName,
                   p.artifact_id AS artifactId,
                   COALESCE(NULLIF(p.artifact_code,''), a.new_artifact_code, a.original_artifact_code, '') AS artifactCode,
                   COALESCE(NULLIF(p.artifact_name,''), a.new_artifact_name, a.original_artifact_name, '') AS artifactName,
                   CONCAT_WS('、', a.material1, a.material2) AS material,
                   COALESCE(a.excavation_relic, '') AS tombCode,
                   p.project_type AS projectType,p.record_mode AS recordMode,p.status,p.current_stage AS currentStage,
                   p.risk_level AS riskLevel, p.progress, p.principal, p.department,
                   p.start_date AS startDate, p.expected_end_date AS expectedEndDate,
                   p.summary, p.source_project_id AS sourceProjectId,
                   p.source_alert_id AS sourceAlertId,
                   p.source_monitoring_record_id AS sourceMonitoringRecordId,
                   p.update_time AS updateTime
            FROM conservation_project p
            LEFT JOIN artifacts a ON a.id=p.artifact_id
            WHERE p.deleted=0 ORDER BY p.update_time DESC, p.id DESC
            """, this::camelMap);
        for (var project : projects) {
            Long id = longValue(project.get("id"));
            project.put("diseases", jdbc.query("""
                SELECT disease_name AS name,
                       CASE severity WHEN 'critical' THEN '危急' WHEN 'severe' THEN '严重'
                       WHEN 'moderate' THEN '中度' ELSE '轻微' END AS level
                FROM conservation_disease_record
                WHERE project_id=? AND deleted=0 ORDER BY emergency DESC, sort_order LIMIT 3
                """, this::camelMap, id));
        }
        return projects;
    }

    @Override
    public Map<String, Object> getProject(Long projectId) {
        var rows = jdbc.query("""
            SELECT p.id, p.project_code AS projectCode, p.project_name AS projectName,
                   p.artifact_id AS artifactId,
                   COALESCE(NULLIF(p.artifact_code,''), a.new_artifact_code, a.original_artifact_code, '') AS artifactCode,
                   COALESCE(NULLIF(p.artifact_name,''), a.new_artifact_name, a.original_artifact_name, '') AS artifactName,
                   CONCAT_WS('、', a.material1, a.material2) AS material,
                   p.project_type AS projectType,p.record_mode AS recordMode,p.status,p.current_stage AS currentStage,
                   p.risk_level AS riskLevel, p.progress, p.principal, p.department,
                   p.start_date AS startDate, p.expected_end_date AS expectedEndDate, p.summary,
                   p.source_project_id AS sourceProjectId, p.source_alert_id AS sourceAlertId,
                   p.source_monitoring_record_id AS sourceMonitoringRecordId
            FROM conservation_project p
            LEFT JOIN artifacts a ON a.id = p.artifact_id
            WHERE p.id = ? AND p.deleted = 0
            """, this::camelMap, projectId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public Map<String, Object> createProject(Map<String, Object> data) {
        String recordMode = normalizeRecordMode(text(data.get("recordMode")));
        String currentStage = "quick".equals(recordMode) ? "quick_record" : "pendingSurvey";
        Long artifactId = resolveArtifactId(data);
        var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        namedJdbc.update("""
            INSERT INTO conservation_project
            (project_code,project_name,artifact_id,artifact_code,artifact_name,project_type,status,current_stage,risk_level,
             record_mode,progress,principal,department,start_date,expected_end_date,summary)
            VALUES
            (:projectCode,:projectName,:artifactId,:artifactCode,:artifactName,:projectType,'draft',:currentStage,:riskLevel,
             :recordMode,0,:principal,:department,:startDate,:expectedEndDate,:summary)
            """, params(data).addValue("startDate", date(data.get("startDate")))
                .addValue("expectedEndDate", date(data.get("expectedEndDate")))
                .addValue("recordMode", recordMode).addValue("currentStage", currentStage)
                .addValue("artifactId", artifactId), key, new String[]{"id"});
        return getProject(requiredGeneratedId(key, "新建保护修复项目"));
    }

    @Override
    public void updateProject(Long projectId, Map<String, Object> data) {
        Long artifactId = resolveArtifactId(data);
        update("""
            UPDATE conservation_project SET project_code=:projectCode,project_name=:projectName,
            artifact_id=:artifactId,artifact_code=:artifactCode,artifact_name=:artifactName,
            project_type=:projectType,record_mode=:recordMode,status=:status,current_stage=:currentStage,
            risk_level=:riskLevel,progress=:progress,principal=:principal,department=:department,
            start_date=:startDate,expected_end_date=:expectedEndDate,summary=:summary
            WHERE id=:id AND deleted=0
            """, params(data).addValue("id", projectId).addValue("recordMode", normalizeRecordMode(text(data.get("recordMode"))))
                .addValue("startDate", date(data.get("startDate")))
                .addValue("expectedEndDate", date(data.get("expectedEndDate"))).addValue("artifactId", artifactId));
    }

    @Override
    public Map<String, Object> getQuickRecord(Long projectId) {
        Map<String, Object> project = getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        Map<String, Object> record = one("""
            SELECT id,project_id AS projectId,issue_description AS issueDescription,
                   treatment_method AS treatmentMethod,operator_name AS operatorName,
                   record_date AS recordDate,conclusion,remark,record_status AS recordStatus,
                   create_time AS createTime,update_time AS updateTime
            FROM conservation_quick_record WHERE project_id=?
            """, projectId);
        if (record == null) {
            record = new LinkedHashMap<>();
            record.put("projectId", projectId);
            record.put("operatorName", text(project.get("principal")));
            record.put("recordDate", LocalDate.now().toString());
            record.put("recordStatus", "draft");
        }
        Long recordId = longValue(record.get("id"));
        if (recordId == null) {
            record.put("media", List.of());
        } else {
            record.put("media", jdbc.query("""
                SELECT id,quick_record_id AS quickRecordId,media_role AS mediaRole,
                       original_name AS fileName,content_type AS contentType,file_size AS fileSize,
                       file_url AS fileUrl,description,create_time AS createTime
                FROM conservation_quick_record_media WHERE quick_record_id=? ORDER BY create_time,id
                """, this::camelMap, recordId));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", project);
        result.put("record", record);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> saveQuickRecord(Long projectId, Map<String, Object> data) {
        Map<String, Object> project = getProject(projectId);
        if (project == null) throw new IllegalArgumentException("保护修复项目不存在");
        if (!"quick".equals(text(project.get("recordMode")))) {
            throw new IllegalStateException("当前项目不是快速记录模式");
        }
        boolean completed = Boolean.TRUE.equals(data.get("completed"));
        namedJdbc.update("""
            INSERT INTO conservation_quick_record
            (project_id,issue_description,treatment_method,operator_name,record_date,conclusion,remark,record_status)
            VALUES (:projectId,:issueDescription,:treatmentMethod,:operatorName,:recordDate,:conclusion,:remark,:recordStatus)
            ON DUPLICATE KEY UPDATE issue_description=VALUES(issue_description),treatment_method=VALUES(treatment_method),
            operator_name=VALUES(operator_name),record_date=VALUES(record_date),conclusion=VALUES(conclusion),
            remark=VALUES(remark),record_status=VALUES(record_status)
            """, params(data).addValue("projectId", projectId)
            .addValue("recordDate", date(data.get("recordDate")))
            .addValue("recordStatus", completed ? "completed" : "draft"));
        jdbc.update("""
            UPDATE conservation_project SET status=?,current_stage=?,progress=? WHERE id=? AND deleted=0
            """, completed ? "completed" : "active", completed ? "completed" : "quick_record",
            completed ? 100 : 50, projectId);
        return getQuickRecord(projectId);
    }

    @Override
    @Transactional
    public Map<String, Object> uploadQuickRecordMedia(Long projectId, MultipartFile file, Map<String, String> metadata) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择图片");
        if (file.getSize() > 20L * 1024 * 1024) throw new IllegalArgumentException("单张图片不能超过20MB");
        String contentType = Objects.toString(file.getContentType(), "");
        if (!contentType.startsWith("image/")) throw new IllegalArgumentException("快速记录仅支持上传图片");
        Map<String, Object> project = getProject(projectId);
        if (project == null || !"quick".equals(text(project.get("recordMode")))) {
            throw new IllegalArgumentException("快速记录项目不存在");
        }
        jdbc.update("""
            INSERT IGNORE INTO conservation_quick_record (project_id,operator_name,record_date,record_status)
            VALUES (?,?,?, 'draft')
            """, projectId, text(project.get("principal")), LocalDate.now());
        Map<String, Object> savedRecord = one("SELECT id FROM conservation_quick_record WHERE project_id=?", projectId);
        Long recordId = savedRecord == null ? null : longValue(savedRecord.get("id"));
        if (recordId == null) {
            throw new IllegalStateException("快速记录保存失败：未获得记录主键");
        }
        try {
            Map<String, String> stored = ConservationOssStorage.upload("quick-record-media", file);
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO conservation_quick_record_media
                (quick_record_id,media_role,original_name,content_type,file_size,file_url,oss_object_key,description)
                VALUES (:recordId,:mediaRole,:fileName,:contentType,:fileSize,:fileUrl,:ossObjectKey,:description)
                """, new MapSqlParameterSource()
                .addValue("recordId", recordId).addValue("mediaRole", text(metadata.getOrDefault("mediaRole", "other")))
                .addValue("fileName", Objects.toString(file.getOriginalFilename(), "image"))
                .addValue("contentType", contentType).addValue("fileSize", file.getSize())
                .addValue("fileUrl", stored.get("fileUrl")).addValue("ossObjectKey", stored.get("objectKey"))
                .addValue("description", metadata.get("description")), key, new String[]{"id"});
            return one("""
                SELECT id,quick_record_id AS quickRecordId,media_role AS mediaRole,
                       original_name AS fileName,content_type AS contentType,file_size AS fileSize,
                       file_url AS fileUrl,description,create_time AS createTime
                FROM conservation_quick_record_media WHERE id=?
                """, requiredGeneratedId(key, "快速记录图片"));
        } catch (Exception exception) {
            throw new IllegalStateException("快速记录图片上传到 OSS 失败", exception);
        }
    }

    @Override
    public void deleteQuickRecordMedia(Long mediaId) {
        jdbc.update("DELETE FROM conservation_quick_record_media WHERE id=?", mediaId);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        if (getProject(projectId) == null) {
            throw new IllegalArgumentException("保护修复项目不存在或已删除");
        }
        deleteProjectOssFiles(projectId);

        // 保留由当前项目预警创建的后续项目，但移除已删除项目的来源引用。
        jdbc.update("""
            UPDATE conservation_project
            SET source_project_id=NULL,source_alert_id=NULL,source_monitoring_record_id=NULL
            WHERE source_project_id=?
            """, projectId);
        jdbc.update("""
            UPDATE monitoring_alert
            SET created_project_id=NULL,project_created_time=NULL
            WHERE created_project_id=? AND project_id<>?
            """, projectId, projectId);

        // 快速修复记录。
        jdbc.update("""
            DELETE media FROM conservation_quick_record_media media
            JOIN conservation_quick_record record ON record.id=media.quick_record_id
            WHERE record.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM conservation_quick_record WHERE project_id=?", projectId);

        // 病害调查。
        jdbc.update("DELETE FROM conservation_disease_media WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_disease_record WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_disease_survey WHERE project_id=?", projectId);

        // 档案及修复过程。
        jdbc.update("""
            DELETE revision FROM conservation_archive_revision revision
            JOIN conservation_archive archive ON archive.id=revision.archive_id
            WHERE archive.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM conservation_archive_attachment WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_archive_advice WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_archive WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_process_media WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_process_step WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM conservation_process WHERE project_id=?", projectId);

        // 修复前后对比。
        jdbc.update("""
            DELETE metric FROM conservation_comparison_metric metric
            JOIN conservation_comparison comparison ON comparison.id=metric.comparison_id
            WHERE comparison.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE media FROM conservation_comparison_media media
            JOIN conservation_comparison comparison ON comparison.id=media.comparison_id
            WHERE comparison.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM conservation_comparison WHERE project_id=?", projectId);

        // 文物复原成果。
        jdbc.update("""
            DELETE source FROM conservation_restoration_source source
            JOIN conservation_restoration_result result ON result.id=source.result_id
            WHERE result.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE part FROM conservation_restoration_part part
            JOIN conservation_restoration_result result ON result.id=part.result_id
            WHERE result.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE media FROM conservation_restoration_media media
            JOIN conservation_restoration_result result ON result.id=media.result_id
            WHERE result.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE model FROM conservation_restoration_model model
            JOIN conservation_restoration_result result ON result.id=model.result_id
            WHERE result.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE version FROM conservation_restoration_version version
            JOIN conservation_restoration_result result ON result.id=version.result_id
            WHERE result.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM conservation_restoration_result WHERE project_id=?", projectId);

        // 后续监测（先删最底层数据，再删计划）。
        jdbc.update("DELETE FROM monitoring_media WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM monitoring_alert WHERE project_id=?", projectId);
        jdbc.update("""
            DELETE value_record FROM monitoring_value value_record
            JOIN monitoring_record record ON record.id=value_record.record_id
            WHERE record.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM monitoring_record WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM monitoring_task WHERE project_id=?", projectId);
        jdbc.update("""
            DELETE baseline FROM monitoring_baseline baseline
            JOIN monitoring_plan plan ON plan.id=baseline.plan_id
            WHERE plan.project_id=?
            """, projectId);
        jdbc.update("""
            DELETE indicator_record FROM monitoring_indicator indicator_record
            JOIN monitoring_plan plan ON plan.id=indicator_record.plan_id
            WHERE plan.project_id=?
            """, projectId);
        jdbc.update("DELETE FROM monitoring_target WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM monitoring_plan WHERE project_id=?", projectId);

        jdbc.update("DELETE FROM conservation_project WHERE id=?", projectId);
    }

    private void deleteProjectOssFiles(Long projectId) {
        Set<String> objectKeys = new LinkedHashSet<>();
        addOssObjectKeys(objectKeys, """
            SELECT media.oss_object_key FROM conservation_quick_record_media media
            JOIN conservation_quick_record record ON record.id=media.quick_record_id
            WHERE record.project_id=?
            """, projectId);
        addOssObjectKeys(objectKeys, "SELECT oss_object_key FROM conservation_disease_media WHERE project_id=?", projectId);
        addOssObjectKeys(objectKeys, "SELECT oss_object_key FROM conservation_archive_attachment WHERE project_id=?", projectId);
        addOssObjectKeys(objectKeys, "SELECT oss_object_key FROM conservation_process_media WHERE project_id=?", projectId);
        addOssObjectKeys(objectKeys, """
            SELECT media.oss_object_key FROM conservation_comparison_media media
            JOIN conservation_comparison comparison ON comparison.id=media.comparison_id
            WHERE comparison.project_id=?
            """, projectId);
        addOssObjectKeys(objectKeys, """
            SELECT media.oss_object_key FROM conservation_restoration_media media
            JOIN conservation_restoration_result result ON result.id=media.result_id
            WHERE result.project_id=?
            """, projectId);
        addOssObjectKeys(objectKeys, """
            SELECT model.oss_object_key FROM conservation_restoration_model model
            JOIN conservation_restoration_result result ON result.id=model.result_id
            WHERE result.project_id=?
            """, projectId);
        addOssObjectKeys(objectKeys, "SELECT oss_object_key FROM monitoring_media WHERE project_id=?", projectId);
        try {
            for (String objectKey : objectKeys) ConservationOssStorage.delete(objectKey);
        } catch (Exception exception) {
            throw new IllegalStateException("项目 OSS 文件删除失败，已停止删除项目：" + exception.getMessage(), exception);
        }
    }

    private void addOssObjectKeys(Set<String> target, String sql, Long projectId) {
        for (String objectKey : jdbc.queryForList(sql, String.class, projectId)) {
            if (objectKey != null && !objectKey.isBlank()) target.add(objectKey);
        }
    }

    @Override
    public List<Map<String, Object>> searchArtifacts(String keyword) {
        String value = "%" + Objects.toString(keyword, "").trim() + "%";
        return jdbc.query("""
            SELECT id,COALESCE(NULLIF(new_artifact_code,''), original_artifact_code, '') AS code,
                   COALESCE(NULLIF(new_artifact_name,''), original_artifact_name, '') AS name,
                   CONCAT_WS('、',material1,material2) AS material,
                   excavation_relic AS tombCode
            FROM artifacts
            WHERE new_artifact_code LIKE ? OR original_artifact_code LIKE ?
               OR new_artifact_name LIKE ? OR original_artifact_name LIKE ?
            ORDER BY id DESC LIMIT 30
            """, this::camelMap, value, value, value, value);
    }

    private Long resolveArtifactId(Map<String, Object> data) {
        Long selectedId = longValue(data.get("artifactId"));
        Integer selectedCount = selectedId == null ? 0 : jdbc.queryForObject(
            "SELECT COUNT(*) FROM artifacts WHERE id=?", Integer.class, selectedId);
        if (selectedId != null && selectedCount != null && selectedCount > 0) {
            return selectedId;
        }

        String code = normalizeArtifactCode(text(data.get("artifactCode")));
        String name = text(data.get("artifactName"));
        Long codeId = code.isBlank() ? null : uniqueArtifactId("""
            SELECT id FROM artifacts
            WHERE REPLACE(REPLACE(REPLACE(REPLACE(TRIM(COALESCE(new_artifact_code,'')),'：',':'),' ',''),'-',''),'*','')=?
               OR REPLACE(REPLACE(REPLACE(REPLACE(TRIM(COALESCE(original_artifact_code,'')),'：',':'),' ',''),'-',''),'*','')=?
            """, code, code);
        Long nameId = name.isBlank() ? null : uniqueArtifactId("""
            SELECT id FROM artifacts
            WHERE TRIM(COALESCE(new_artifact_name,''))=? OR TRIM(COALESCE(original_artifact_name,''))=?
            """, name, name);

        // 编号与名称均能匹配但指向不同文物时，不自动绑定，避免将项目关联到错误文物。
        if (codeId != null && nameId != null && !Objects.equals(codeId, nameId)) {
            return null;
        }
        return codeId != null ? codeId : nameId;
    }

    private Long uniqueArtifactId(String sql, Object... args) {
        List<Long> ids = jdbc.queryForList(sql, Long.class, args);
        return ids.size() == 1 ? ids.getFirst() : null;
    }

    private String normalizeArtifactCode(String value) {
        return value.replace('：', ':').replace(" ", "").replace("-", "").replace("*", "");
    }

    @Override
    public Map<String, Object> getSources(Long projectId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("archiveAdvice", one("""
            SELECT id, project_id AS projectId, temperature_range AS temperatureRange,
                   humidity_range AS humidityRange, lighting, air_quality AS airQuality,
                   packaging, handling, shockproof, review_cycle AS reviewCycle,
                   monitor_diseases AS monitorDiseases, monitoring_indicators AS monitoringIndicators,
                   follow_up_advice AS followUpAdvice, warning_conditions AS warningConditions
            FROM conservation_archive_advice WHERE project_id = ?
            """, projectId));
        result.put("diseases", jdbc.query("""
            SELECT id, project_id AS projectId, disease_name AS diseaseName,
                   disease_category AS diseaseCategory, severity,
                   development_status AS developmentStatus, extent_value AS extentValue,
                   extent_unit AS extentUnit, part_name AS partName, side,
                   position_description AS positionDescription, morphology,
                   cause_analysis AS causeAnalysis, structural_impact AS structuralImpact,
                   emergency, recommended_action AS recommendedAction
            FROM conservation_disease_record
            WHERE project_id = ? AND deleted = 0 ORDER BY emergency DESC, sort_order, id
            """, this::camelMap, projectId));
        result.put("processSteps", List.of());

        List<Map<String, Object>> comparisons = jdbc.query("""
            SELECT id, comparison_code AS comparisonCode, comparison_title AS comparisonTitle,
                   target_part AS targetPart, shooting_position AS shootingPosition,
                   after_summary AS afterSummary, remaining_issue AS remainingIssue,
                   monitoring_review_part AS monitoringReviewPart, monitoring_notes AS monitoringNotes,
                   evaluation_date AS evaluationDate
            FROM conservation_comparison
            WHERE project_id = ? AND selected_as_monitoring_baseline = 1 ORDER BY id
            """, this::camelMap, projectId);
        for (var comparison : comparisons) {
            Long id = longValue(comparison.get("id"));
            comparison.put("metrics", jdbc.query("""
                SELECT id, metric_name AS metricName, metric_category AS metricCategory,
                       before_value AS beforeValue, after_value AS afterValue,
                       value_unit AS valueUnit, expected_direction AS expectedDirection
                FROM conservation_comparison_metric WHERE comparison_id = ? ORDER BY id
                """, this::camelMap, id));
            comparison.put("images", jdbc.query("""
                SELECT id, image_stage AS imageStage, image_role AS imageRole,
                       original_name AS fileName, target_part AS targetPart,
                       shooting_position AS shootingPosition, shooting_time AS shootingTime,
                       description AS imageDescription,
                       COALESCE(file_url,CONCAT('/api/conservation/comparison-media/', id, '/content')) AS fileUrl
                FROM conservation_comparison_media WHERE comparison_id = ? ORDER BY id
                """, this::camelMap, id));
            comparison.put("monitoring", Map.of(
                "reviewPart", Objects.toString(comparison.get("monitoringReviewPart"), ""),
                "notes", Objects.toString(comparison.get("monitoringNotes"), "")
            ));
            comparison.put("evaluation", Map.of("remainingIssue", Objects.toString(comparison.get("remainingIssue"), "")));
        }
        result.put("comparisons", comparisons);

        List<Map<String, Object>> restorations = jdbc.query("""
            SELECT id, result_code AS resultCode, result_name AS resultName,
                   restoration_type AS restorationType, target_part AS targetPart,
                   result_status AS resultStatus, completion_date AS completionDate,
                   current_version AS currentVersion, final_conclusion AS finalConclusion,
                   monitoring_indicators AS monitoringIndicators, monitoring_cycle AS monitoringCycle,
                   warning_conditions AS warningConditions, monitoring_note AS monitoringNote
            FROM conservation_restoration_result
            WHERE project_id = ? AND requires_monitoring = 1 AND deleted=0 ORDER BY id
            """, this::camelMap, projectId);
        for (var restoration : restorations) {
            Long id = longValue(restoration.get("id"));
            var parts = jdbc.query("""
                SELECT id, part_name AS partName, part_type AS partType,
                       target_location AS targetLocation, material_name AS materialName,
                       technique, selected_for_monitoring AS selectedForMonitoring
                FROM conservation_restoration_part WHERE result_id = ? ORDER BY id
                """, this::camelMap, id);
            var media = jdbc.query("""
                SELECT id, part_id AS restorationPartId, media_stage AS mediaStage,
                       original_name AS fileName, description, is_primary AS isPrimary,
                       selected_as_monitoring_baseline AS selectedAsMonitoringBaseline,
                       COALESCE(file_url,CONCAT('/api/conservation/restoration-media/', id, '/content')) AS fileUrl
                FROM conservation_restoration_media WHERE result_id = ? ORDER BY id
                """, this::camelMap, id);
            restoration.put("parts", parts);
            restoration.put("media", media);
            restoration.put("evaluation", Map.of("finalConclusion", Objects.toString(restoration.get("finalConclusion"), "")));
            restoration.put("monitoring", Map.of(
                "partIds", parts.stream().filter(x -> bool(x.get("selectedForMonitoring"))).map(x -> x.get("id")).toList(),
                "indicators", Objects.toString(restoration.get("monitoringIndicators"), ""),
                "cycle", Objects.toString(restoration.get("monitoringCycle"), ""),
                "warningConditions", Objects.toString(restoration.get("warningConditions"), ""),
                "note", Objects.toString(restoration.get("monitoringNote"), "")
            ));
        }
        result.put("restorations", restorations);
        return result;
    }

    @Override
    public List<Map<String, Object>> getPlans(Long projectId) {
        List<Map<String, Object>> plans = jdbc.query("""
            SELECT p.id, p.project_id AS projectId, p.artifact_id AS artifactId,
                   p.archive_id AS archiveId, p.plan_code AS planCode, p.plan_name AS planName,
                   p.plan_type AS planType, p.plan_status AS planStatus,
                   p.monitoring_purpose AS monitoringPurpose, p.monitoring_scope AS monitoringScope,
                   p.overall_strategy AS overallStrategy, p.responsible_person AS responsiblePerson,
                   p.participant_names AS participantNames, p.monitoring_location AS monitoringLocation,
                   p.start_date AS startDate, p.expected_end_date AS expectedEndDate,
                   p.next_monitoring_date AS nextMonitoringDate,
                   p.default_frequency_value AS defaultFrequencyValue,
                   p.default_frequency_unit AS defaultFrequencyUnit,
                   p.auto_generate_task AS autoGenerateTask, p.alert_enabled AS alertEnabled,
                   p.execution_count AS executionCount, p.overdue_count AS overdueCount,
                   p.completion_rate AS completionRate,
                   (SELECT COUNT(*) FROM monitoring_alert a WHERE a.plan_id=p.id
                    AND a.alert_status NOT IN ('resolved','closed','false_alarm')) AS openAlertCount
            FROM monitoring_plan p WHERE p.project_id = ? AND p.deleted = 0 ORDER BY p.id DESC
            """, this::camelMap, projectId);
        for (var plan : plans) hydratePlan(plan);
        return plans;
    }

    private void hydratePlan(Map<String, Object> plan) {
        Long planId = longValue(plan.get("id"));
        List<Map<String, Object>> targets = jdbc.query("""
            SELECT id, plan_id AS planId, project_id AS projectId, target_type AS targetType,
                   target_name AS targetName, source_business_type AS sourceBusinessType,
                   source_business_id AS sourceBusinessId, target_part AS targetPart,
                   target_location AS targetLocation, risk_level AS riskLevel,
                   priority_level AS priorityLevel, monitoring_reason AS monitoringReason,
                   current_status AS currentStatus, requires_image AS requiresImage,
                   shooting_position AS shootingPosition, enabled
            FROM monitoring_target WHERE plan_id = ? AND deleted = 0 ORDER BY id
            """, this::camelMap, planId);
        for (var target : targets) {
            Long targetId = longValue(target.get("id"));
            target.put("indicators", jdbc.query("""
                SELECT id, target_id AS targetId, plan_id AS planId, indicator_code AS indicatorCode,
                       indicator_name AS indicatorName, indicator_category AS indicatorCategory,
                       data_type AS dataType, value_unit AS valueUnit, baseline_value AS baselineValue,
                       normal_min AS normalMin, normal_max AS normalMax, warning_min AS warningMin,
                       warning_max AS warningMax, critical_min AS criticalMin, critical_max AS criticalMax,
                       change_warning_value AS changeWarningValue, change_warning_rate AS changeWarningRate,
                       expected_direction AS expectedDirection, observation_method AS observationMethod,
                       instrument_name AS instrumentName, required_flag AS required
                FROM monitoring_indicator WHERE target_id = ? ORDER BY sort_order, id
                """, this::camelMap, targetId));
            target.put("baseline", one("""
                SELECT id, plan_id AS planId, target_id AS targetId, indicator_id AS indicatorId,
                       source_business_type AS sourceBusinessType, source_business_id AS sourceBusinessId,
                       baseline_date AS baselineDate, baseline_value AS baselineValue,
                       baseline_unit AS baselineUnit, baseline_status AS baselineStatus,
                       baseline_description AS baselineDescription, baseline_media_id AS baselineMediaId,
                       version_no AS versionNo, is_current AS isCurrent, created_by AS createdBy,
                       CASE
                         WHEN source_business_type='comparison_after' THEN (SELECT COALESCE(file_url,CONCAT('/api/conservation/comparison-media/', id, '/content')) FROM conservation_comparison_media WHERE id=baseline_media_id)
                         WHEN source_business_type='restoration' THEN (SELECT COALESCE(file_url,CONCAT('/api/conservation/restoration-media/', id, '/content')) FROM conservation_restoration_media WHERE id=baseline_media_id)
                         ELSE NULL
                       END AS baselineFileUrl
                FROM monitoring_baseline WHERE target_id = ? AND is_current = 1 ORDER BY id DESC LIMIT 1
                """, targetId));
        }
        plan.put("targets", targets);
        plan.put("tasks", jdbc.query("""
            SELECT id, plan_id AS planId, project_id AS projectId, task_code AS taskCode,
                   task_name AS taskName, task_type AS taskType, task_status AS taskStatus,
                   planned_date AS plannedDate, due_date AS dueDate,
                   actual_start_time AS actualStartTime, actual_end_time AS actualEndTime,
                   responsible_person AS responsiblePerson, participant_names AS participantNames,
                   target_count AS targetCount, completed_target_count AS completedTargetCount,
                   completion_rate AS completionRate, overall_result AS overallResult,
                   summary, generated_automatically AS generatedAutomatically
            FROM monitoring_task WHERE plan_id = ? ORDER BY planned_date DESC, id DESC
            """, this::camelMap, planId));

        List<Map<String, Object>> records = jdbc.query("""
            SELECT id, task_id AS taskId, plan_id AS planId, target_id AS targetId,
                   project_id AS projectId, record_code AS recordCode,
                   monitoring_date AS monitoringDate, monitor_person AS monitorPerson,
                   monitoring_location AS monitoringLocation, overall_status AS overallStatus,
                   comparison_result AS comparisonResult,
                   observation_description AS observationDescription,
                   change_description AS changeDescription, result_conclusion AS resultConclusion,
                   requires_recheck AS requiresRecheck, requires_intervention AS requiresIntervention,
                   requires_new_disease_survey AS requiresNewDiseaseSurvey,
                   requires_new_project AS requiresNewProject,
                   next_monitoring_date AS nextMonitoringDate, monitoring_status AS monitoringStatus
            FROM monitoring_record WHERE plan_id = ? AND deleted = 0 ORDER BY monitoring_date, id
            """, this::camelMap, planId);
        for (var record : records) {
            Long recordId = longValue(record.get("id"));
            record.put("values", jdbc.query("""
                SELECT id, record_id AS recordId, indicator_id AS indicatorId,
                       indicator_name AS indicatorName, value_number AS valueNumber,
                       value_text AS valueText, value_unit AS valueUnit,
                       baseline_value AS baselineValue, previous_value AS previousValue,
                       change_value AS changeValue, change_rate AS changeRate,
                       result_level AS resultLevel, result_description AS resultDescription,
                       manually_confirmed AS manuallyConfirmed
                FROM monitoring_value WHERE record_id = ? ORDER BY id
                """, this::camelMap, recordId));
            record.put("media", jdbc.query("""
                SELECT id, record_id AS recordId, target_id AS targetId, media_role AS role,
                       original_name AS fileName, content_type AS contentType, file_size AS fileSize,
                       shooting_position AS shootingPosition, shooting_time AS shootingTime,
                       title, description,
                       COALESCE(file_url,CONCAT('/api/conservation/monitoring-media/', id, '/content')) AS fileUrl
                FROM monitoring_media WHERE record_id = ? ORDER BY id
                """, this::camelMap, recordId));
        }
        plan.put("records", records);
        plan.put("alerts", jdbc.query("""
            SELECT id, project_id AS projectId, plan_id AS planId, task_id AS taskId,
                   record_id AS recordId, target_id AS targetId, indicator_id AS indicatorId,
                   alert_code AS alertCode, alert_level AS alertLevel, alert_title AS alertTitle,
                   alert_description AS alertDescription, trigger_type AS triggerType,
                   trigger_value AS triggerValue, threshold_description AS thresholdDescription,
                   alert_status AS alertStatus, discovered_time AS discoveredTime,
                   confirmed_time AS confirmedTime, confirmed_by AS confirmedBy,
                   immediate_action AS immediateAction, treatment_advice AS treatmentAdvice,
                   requires_recheck AS requiresRecheck,
                   requires_disease_survey AS requiresDiseaseSurvey,
                   requires_intervention AS requiresIntervention,
                   requires_new_project AS requiresNewProject,
                   created_project_id AS createdProjectId,
                   project_created_time AS projectCreatedTime
            FROM monitoring_alert WHERE plan_id = ? ORDER BY discovered_time DESC, id DESC
            """, this::camelMap, planId));
    }

    @Override
    public Map<String, Object> getStatistics(Long projectId) {
        return one("""
            SELECT COUNT(*) AS plans,
                   SUM(plan_status='active') AS activePlans,
                   (SELECT COUNT(*) FROM monitoring_target t WHERE t.project_id=? AND t.deleted=0) AS targets,
                   (SELECT COUNT(*) FROM monitoring_task t WHERE t.project_id=? AND t.task_status='completed') AS completedTasks,
                   (SELECT COUNT(*) FROM monitoring_task t WHERE t.project_id=? AND t.task_status IN ('pending','in_progress','overdue')) AS pendingTasks,
                   (SELECT COUNT(*) FROM monitoring_alert a WHERE a.project_id=? AND a.alert_status NOT IN ('resolved','closed','false_alarm')) AS openAlerts,
                   (SELECT COUNT(*) FROM monitoring_target t WHERE t.project_id=? AND t.risk_level='high' AND t.deleted=0) AS highRiskTargets,
                   MIN(next_monitoring_date) AS nextMonitoringDate, MAX(update_time) AS updateTime
            FROM monitoring_plan WHERE project_id=? AND deleted=0
            """, projectId, projectId, projectId, projectId, projectId, projectId);
    }

    @Override
    public Map<String, Object> getComparisonSummary(Long projectId) {
        requireProject(projectId);
        return one("""
            SELECT COUNT(*) AS total,
                   COALESCE(SUM(comparison_status IN ('completed','reviewed','archived')),0) AS completed,
                   COALESCE(SUM(selected_for_archive=1),0) AS selectedForArchive,
                   COALESCE(SUM(selected_as_monitoring_baseline=1),0) AS monitoringBaselines,
                   MAX(update_time) AS updateTime
            FROM conservation_comparison WHERE project_id=?
            """, projectId);
    }

    @Override
    public Map<String, Object> getRestorationSummary(Long projectId) {
        requireProject(projectId);
        Map<String, Object> summary = one("""
            SELECT COUNT(*) AS total,
                   COALESCE(SUM(restoration_type='physical'),0) AS physical,
                   COALESCE(SUM(restoration_type LIKE 'digital_%'),0) AS digital,
                   COALESCE(SUM(result_status IN ('completed','reviewed','archived')),0) AS completed,
                   COALESCE(SUM(result_status IN ('draft','in_progress')),0) AS processing,
                   COALESCE(SUM(selected_for_archive=1),0) AS selectedForArchive,
                   MAX(update_time) AS updateTime
            FROM conservation_restoration_result WHERE project_id=? AND deleted=0
            """, projectId);
        Map<String, Object> recommended = one("""
            SELECT result_name AS resultName FROM conservation_restoration_result
            WHERE project_id=? AND recommended_result=1 AND deleted=0
            ORDER BY update_time DESC,id DESC LIMIT 1
            """, projectId);
        summary.put("recommended", recommended == null ? "" : recommended.get("resultName"));
        return summary;
    }

    private void requireProject(Long projectId) {
        if (getProject(projectId) == null) throw new IllegalArgumentException("保护修复项目不存在");
    }

    @Override
    @Transactional
    public void saveWorkbench(Long projectId, List<Map<String, Object>> plans) {
        List<Long> oldPlanIds = jdbc.queryForList("SELECT id FROM monitoring_plan WHERE project_id=?", Long.class, projectId);
        if (!oldPlanIds.isEmpty()) {
            String ids = String.join(",", Collections.nCopies(oldPlanIds.size(), "?"));
            jdbc.update("DELETE v FROM monitoring_value v JOIN monitoring_record r ON r.id=v.record_id WHERE r.plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_alert WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_record WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_task WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_baseline WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_indicator WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_target WHERE plan_id IN (" + ids + ")", oldPlanIds.toArray());
            jdbc.update("DELETE FROM monitoring_plan WHERE id IN (" + ids + ")", oldPlanIds.toArray());
        }
        for (Map<String, Object> plan : plans) insertPlan(projectId, plan);
        jdbc.update("""
            UPDATE conservation_project SET current_stage='monitoring',
            progress=GREATEST(progress,95), update_time=NOW() WHERE id=?
            """, projectId);
    }

    private void insertPlan(Long projectId, Map<String, Object> p) {
        Long planId = requiredId(p);
        update("""
            INSERT INTO monitoring_plan
            (id,project_id,artifact_id,archive_id,plan_code,plan_name,plan_type,plan_status,
             monitoring_purpose,monitoring_scope,overall_strategy,responsible_person,participant_names,
             monitoring_location,start_date,expected_end_date,next_monitoring_date,
             default_frequency_value,default_frequency_unit,auto_generate_task,alert_enabled,
             execution_count,overdue_count,completion_rate)
            VALUES
            (:id,:projectId,:artifactId,:archiveId,:planCode,:planName,:planType,:planStatus,
             :monitoringPurpose,:monitoringScope,:overallStrategy,:responsiblePerson,:participantNames,
             :monitoringLocation,:startDate,:expectedEndDate,:nextMonitoringDate,
             :defaultFrequencyValue,:defaultFrequencyUnit,:autoGenerateTask,:alertEnabled,
             :executionCount,:overdueCount,:completionRate)
            """, params(p).addValue("projectId", projectId)
                .addValue("startDate", date(p.get("startDate"))).addValue("expectedEndDate", date(p.get("expectedEndDate")))
                .addValue("nextMonitoringDate", date(p.get("nextMonitoringDate"))));

        for (Map<String, Object> target : list(p, "targets")) {
            Long targetId = requiredId(target);
            update("""
                INSERT INTO monitoring_target
                (id,plan_id,project_id,target_type,target_name,source_business_type,source_business_id,
                 target_part,target_location,risk_level,priority_level,monitoring_reason,current_status,
                 requires_image,shooting_position,enabled)
                VALUES (:id,:planId,:projectId,:targetType,:targetName,:sourceBusinessType,:sourceBusinessId,
                 :targetPart,:targetLocation,:riskLevel,:priorityLevel,:monitoringReason,:currentStatus,
                 :requiresImage,:shootingPosition,:enabled)
                """, params(target).addValue("planId", planId).addValue("projectId", projectId));
            for (Map<String, Object> indicator : list(target, "indicators")) {
                update("""
                    INSERT INTO monitoring_indicator
                    (id,target_id,plan_id,indicator_code,indicator_name,indicator_category,data_type,
                     value_unit,baseline_value,normal_min,normal_max,warning_min,warning_max,
                     critical_min,critical_max,change_warning_value,change_warning_rate,
                     expected_direction,observation_method,instrument_name,required_flag)
                    VALUES (:id,:targetId,:planId,:indicatorCode,:indicatorName,:indicatorCategory,:dataType,
                     :valueUnit,:baselineValue,:normalMin,:normalMax,:warningMin,:warningMax,
                     :criticalMin,:criticalMax,:changeWarningValue,:changeWarningRate,
                     :expectedDirection,:observationMethod,:instrumentName,:required)
                    """, params(indicator).addValue("targetId", targetId).addValue("planId", planId));
            }
            Map<String, Object> baseline = map(target.get("baseline"));
            if (baseline != null) {
                update("""
                    INSERT INTO monitoring_baseline
                    (id,plan_id,target_id,indicator_id,source_business_type,source_business_id,
                     baseline_date,baseline_value,baseline_unit,baseline_status,baseline_description,
                     baseline_media_id,version_no,is_current,created_by)
                    VALUES (:id,:planId,:targetId,:indicatorId,:sourceBusinessType,:sourceBusinessId,
                     :baselineDate,:baselineValue,:baselineUnit,:baselineStatus,:baselineDescription,
                     :baselineMediaId,:versionNo,:isCurrent,:createdBy)
                    """, params(baseline).addValue("planId", planId).addValue("targetId", targetId)
                        .addValue("baselineDate", date(baseline.get("baselineDate"))));
            }
        }
        for (Map<String, Object> task : list(p, "tasks")) {
            update("""
                INSERT INTO monitoring_task
                (id,plan_id,project_id,task_code,task_name,task_type,task_status,planned_date,due_date,
                 actual_start_time,actual_end_time,responsible_person,participant_names,target_count,
                 completed_target_count,completion_rate,overall_result,summary,generated_automatically)
                VALUES (:id,:planId,:projectId,:taskCode,:taskName,:taskType,:taskStatus,:plannedDate,:dueDate,
                 :actualStartTime,:actualEndTime,:responsiblePerson,:participantNames,:targetCount,
                 :completedTargetCount,:completionRate,:overallResult,:summary,:generatedAutomatically)
                """, params(task).addValue("planId", planId).addValue("projectId", projectId)
                    .addValue("plannedDate", date(task.get("plannedDate"))).addValue("dueDate", date(task.get("dueDate")))
                    .addValue("actualStartTime", dateTime(task.get("actualStartTime"))).addValue("actualEndTime", dateTime(task.get("actualEndTime"))));
        }
        for (Map<String, Object> record : list(p, "records")) {
            Long recordId = requiredId(record);
            update("""
                INSERT INTO monitoring_record
                (id,task_id,plan_id,target_id,project_id,record_code,monitoring_date,monitor_person,
                 monitoring_location,overall_status,comparison_result,observation_description,
                 change_description,result_conclusion,requires_recheck,requires_intervention,
                 requires_new_disease_survey,requires_new_project,next_monitoring_date,monitoring_status,
                 submitted_time)
                VALUES (:id,:taskId,:planId,:targetId,:projectId,:recordCode,:monitoringDate,:monitorPerson,
                 :monitoringLocation,:overallStatus,:comparisonResult,:observationDescription,
                 :changeDescription,:resultConclusion,:requiresRecheck,:requiresIntervention,
                 :requiresNewDiseaseSurvey,:requiresNewProject,:nextMonitoringDate,:monitoringStatus,
                 :submittedTime)
                """, params(record).addValue("planId", planId).addValue("projectId", projectId)
                    .addValue("monitoringDate", dateTime(record.get("monitoringDate")))
                    .addValue("nextMonitoringDate", date(record.get("nextMonitoringDate")))
                    .addValue("submittedTime", "submitted".equals(record.get("monitoringStatus")) ? LocalDateTime.now() : null));
            for (Map<String, Object> value : list(record, "values")) {
                update("""
                    INSERT INTO monitoring_value
                    (id,record_id,indicator_id,indicator_name,value_number,value_text,value_unit,
                     baseline_value,previous_value,change_value,change_rate,result_level,
                     result_description,manually_confirmed)
                    VALUES (:id,:recordId,:indicatorId,:indicatorName,:valueNumber,:valueText,:valueUnit,
                     :baselineValue,:previousValue,:changeValue,:changeRate,:resultLevel,
                     :resultDescription,:manuallyConfirmed)
                    """, params(value).addValue("recordId", recordId));
            }
        }
        for (Map<String, Object> alert : list(p, "alerts")) {
            update("""
                INSERT INTO monitoring_alert
                (id,project_id,plan_id,task_id,record_id,target_id,indicator_id,alert_code,
                 alert_level,alert_title,alert_description,trigger_type,trigger_value,
                 threshold_description,alert_status,discovered_time,confirmed_time,confirmed_by,
                immediate_action,treatment_advice,requires_recheck,requires_disease_survey,
                 requires_intervention,requires_new_project,created_project_id,project_created_time)
                VALUES (:id,:projectId,:planId,:taskId,:recordId,:targetId,:indicatorId,:alertCode,
                 :alertLevel,:alertTitle,:alertDescription,:triggerType,:triggerValue,
                 :thresholdDescription,:alertStatus,:discoveredTime,:confirmedTime,:confirmedBy,
                 :immediateAction,:treatmentAdvice,:requiresRecheck,:requiresDiseaseSurvey,
                 :requiresIntervention,:requiresNewProject,:createdProjectId,:projectCreatedTime)
                """, params(alert).addValue("projectId", projectId).addValue("planId", planId)
                    .addValue("discoveredTime", dateTime(alert.get("discoveredTime")))
                    .addValue("confirmedTime", dateTime(alert.get("confirmedTime")))
                    .addValue("projectCreatedTime", dateTime(alert.get("projectCreatedTime"))));
        }
    }

    @Override
    public void changeTaskStatus(Long taskId, String status) {
        String timeColumn = "in_progress".equals(status) ? ", actual_start_time=NOW()" :
            "completed".equals(status) ? ", actual_end_time=NOW(), completion_rate=100" : "";
        jdbc.update("UPDATE monitoring_task SET task_status=?" + timeColumn + " WHERE id=?", status, taskId);
    }

    @Override
    public void updateAlert(Long alertId, Map<String, Object> data) {
        update("""
            UPDATE monitoring_alert SET alert_status=:alertStatus, confirmed_time=COALESCE(confirmed_time,NOW()),
            confirmed_by=:confirmedBy, immediate_action=:immediateAction, treatment_advice=:treatmentAdvice,
            requires_recheck=:requiresRecheck, requires_disease_survey=:requiresDiseaseSurvey,
            requires_intervention=:requiresIntervention, requires_new_project=:requiresNewProject,
            resolved_time=IF(:alertStatus IN ('resolved','closed'),NOW(),resolved_time)
            WHERE id=:id
            """, params(data).addValue("id", alertId));
    }

    @Override
    @Transactional
    public Map<String, Object> createProjectFromAlert(Long alertId, Map<String, Object> data) {
        Map<String, Object> alert = one("""
            SELECT ma.id, ma.project_id AS sourceProjectId, ma.record_id AS recordId,
                   ma.target_id AS targetId, ma.indicator_id AS indicatorId,
                   ma.alert_code AS alertCode, ma.alert_level AS alertLevel,
                   ma.alert_title AS alertTitle, ma.alert_description AS alertDescription,
                   ma.trigger_value AS triggerValue,
                   ma.threshold_description AS thresholdDescription,
                   ma.alert_status AS alertStatus, ma.treatment_advice AS treatmentAdvice,
                   ma.created_project_id AS createdProjectId,
                   cp.project_code AS sourceProjectCode, cp.project_name AS sourceProjectName,
                   cp.artifact_id AS artifactId,
                   COALESCE(NULLIF(cp.artifact_code,''), a.new_artifact_code, '') AS artifactCode,
                   COALESCE(NULLIF(cp.artifact_name,''), a.new_artifact_name, '') AS artifactName,
                   cp.principal, cp.department,
                   mt.target_name AS targetName, mt.target_part AS targetPart,
                   mt.target_location AS targetLocation,
                   mi.indicator_name AS indicatorName, mi.value_unit AS valueUnit,
                   mr.monitoring_date AS monitoringDate, mr.result_conclusion AS monitoringConclusion
            FROM monitoring_alert ma
            JOIN conservation_project cp ON cp.id=ma.project_id AND cp.deleted=0
            LEFT JOIN artifacts a ON a.id=cp.artifact_id
            LEFT JOIN monitoring_target mt ON mt.id=ma.target_id
            LEFT JOIN monitoring_indicator mi ON mi.id=ma.indicator_id
            LEFT JOIN monitoring_record mr ON mr.id=ma.record_id
            WHERE ma.id=?
            FOR UPDATE
            """, alertId);
        if (alert == null) throw new IllegalArgumentException("监测预警不存在，请先保存预警信息");

        Long existingProjectId = longValue(alert.get("createdProjectId"));
        if (existingProjectId != null) {
            Map<String, Object> existing = getProject(existingProjectId);
            if (existing != null) {
                existing.put("alreadyCreated", true);
                existing.put("sourceAlertCode", alert.get("alertCode"));
                return existing;
            }
        }

        String artifactName = text(alert.get("artifactName"));
        if (artifactName.isBlank()) throw new IllegalArgumentException("来源项目缺少文物名称，无法创建新项目");
        String alertTitle = defaultText(alert.get("alertTitle"), "监测异常");
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectCode", inputText(data, "projectCode", "CR-ALERT-" + alertId));
        projectData.put("projectName", inputText(data, "projectName", artifactName + alertTitle + "保护修复项目"));
        projectData.put("artifactId", alert.get("artifactId"));
        projectData.put("artifactCode", alert.get("artifactCode"));
        projectData.put("artifactName", artifactName);
        projectData.put("projectType", inputText(data, "projectType", "综合"));
        projectData.put("riskLevel", inputText(data, "riskLevel",
            "critical".equals(alert.get("alertLevel")) ? "high" : "medium"));
        projectData.put("principal", inputText(data, "principal", text(alert.get("principal"))));
        projectData.put("department", inputText(data, "department", text(alert.get("department"))));
        projectData.put("startDate", inputText(data, "startDate", LocalDate.now().toString()));
        projectData.put("expectedEndDate", data.get("expectedEndDate"));

        String sourceSummary = """
            来源：后续监测预警 %s（来源项目：%s，监测记录：%s）
            预警内容：%s
            监测对象：%s；触发值：%s；阈值说明：%s
            监测结论：%s
            处理建议：%s
            """.formatted(
                defaultText(alert.get("alertCode"), String.valueOf(alertId)),
                defaultText(alert.get("sourceProjectCode"), String.valueOf(alert.get("sourceProjectId"))),
                defaultText(alert.get("recordId"), "无"),
                defaultText(alert.get("alertDescription"), alertTitle),
                defaultText(alert.get("targetName"), defaultText(alert.get("targetPart"), "未填写")),
                defaultText(alert.get("triggerValue"), "未填写"),
                defaultText(alert.get("thresholdDescription"), "未填写"),
                defaultText(alert.get("monitoringConclusion"), "未填写"),
                defaultText(alert.get("treatmentAdvice"), "未填写")
            ).trim();
        String customSummary = text(data.get("summary"));
        projectData.put("summary", customSummary.isBlank() ? sourceSummary : customSummary + "\n\n" + sourceSummary);

        Map<String, Object> created = createProject(projectData);
        Long createdProjectId = longValue(created.get("id"));
        jdbc.update("""
            UPDATE conservation_project
            SET source_project_id=?, source_alert_id=?, source_monitoring_record_id=?
            WHERE id=?
            """, alert.get("sourceProjectId"), alertId, alert.get("recordId"), createdProjectId);
        jdbc.update("""
            UPDATE monitoring_alert
            SET created_project_id=?, project_created_time=NOW(), requires_new_project=1,
                alert_status=CASE WHEN alert_status IN ('new','confirmed') THEN 'processing' ELSE alert_status END
            WHERE id=?
            """, createdProjectId, alertId);
        if (alert.get("recordId") != null) {
            jdbc.update("UPDATE monitoring_record SET requires_new_project=1 WHERE id=?",
                alert.get("recordId"));
        }

        created = getProject(createdProjectId);
        created.put("alreadyCreated", false);
        created.put("sourceAlertCode", alert.get("alertCode"));
        return created;
    }

    @Override
    public Map<String, Object> uploadMedia(Long recordId, MultipartFile file, Map<String, String> metadata) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的文件");
        if (file.getSize() > 50L * 1024 * 1024) throw new IllegalArgumentException("单个文件不能超过50MB");
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        if (!contentType.startsWith("image/") && !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("仅支持图片或PDF文件");
        }
        Map<String, Object> record = one("SELECT project_id AS projectId,plan_id AS planId,task_id AS taskId,target_id AS targetId FROM monitoring_record WHERE id=?", recordId);
        if (record == null) throw new IllegalArgumentException("监测记录不存在");
        try {
            Map<String, String> stored = ConservationOssStorage.upload("monitoring-media", file);
            var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
            namedJdbc.update("""
                INSERT INTO monitoring_media
                (project_id,plan_id,task_id,record_id,target_id,media_role,original_name,
                 content_type,file_size,file_url,oss_object_key,shooting_position,shooting_time,title,description,created_by)
                VALUES
                (:projectId,:planId,:taskId,:recordId,:targetId,:mediaRole,:originalName,
                 :contentType,:fileSize,:fileUrl,:ossObjectKey,:shootingPosition,:shootingTime,:title,:description,:createdBy)
                """, new MapSqlParameterSource(record)
                    .addValue("recordId", recordId).addValue("mediaRole", metadata.getOrDefault("mediaRole", "current"))
                    .addValue("originalName", file.getOriginalFilename()).addValue("contentType", contentType)
                    .addValue("fileSize", file.getSize()).addValue("fileUrl", stored.get("fileUrl"))
                    .addValue("ossObjectKey", stored.get("objectKey"))
                    .addValue("shootingPosition", metadata.get("shootingPosition"))
                    .addValue("shootingTime", dateTime(metadata.get("shootingTime")))
                    .addValue("title", metadata.get("title")).addValue("description", metadata.get("description"))
                    .addValue("createdBy", metadata.get("createdBy")), key, new String[]{"id"});
            Long id = key.getKey().longValue();
            return Map.of("id", id, "recordId", recordId, "fileName", Objects.toString(file.getOriginalFilename(), ""),
                "contentType", contentType, "fileSize", file.getSize(),
                "fileUrl", stored.get("fileUrl"),
                "role", metadata.getOrDefault("mediaRole", "current"));
        } catch (Exception e) {
            throw new IllegalStateException("文件上传到 OSS 失败", e);
        }
    }

    @Override
    public Map<String, Object> getMedia(Long mediaId) {
        return one("SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData,file_url AS fileUrl FROM monitoring_media WHERE id=?", mediaId);
    }

    @Override
    public Map<String, Object> getSourceMedia(String sourceType, Long mediaId) {
        String table = switch (sourceType) {
            case "comparison" -> "conservation_comparison_media";
            case "restoration" -> "conservation_restoration_media";
            default -> throw new IllegalArgumentException("不支持的影像来源");
        };
        return one("SELECT original_name AS fileName,content_type AS contentType,file_data AS fileData,file_url AS fileUrl FROM " + table + " WHERE id=?", mediaId);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        jdbc.update("DELETE FROM monitoring_media WHERE id=?", mediaId);
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
            if (value instanceof Number && isBooleanKey(key)) value = ((Number) value).intValue() != 0;
            result.put(key, value);
        }
        return result;
    }

    private boolean isBooleanKey(String key) {
        return Set.of("emergency","enabled","requiresImage","required","isCurrent","autoGenerateTask",
            "alertEnabled","generatedAutomatically","requiresRecheck","requiresIntervention",
            "requiresNewDiseaseSurvey","requiresNewProject","manuallyConfirmed","selectedForMonitoring",
            "isPrimary","selectedAsMonitoringBaseline").contains(key);
    }

    private void update(String sql, MapSqlParameterSource params) {
        namedJdbc.update(sql, params);
    }

    private MapSqlParameterSource params(Map<String, Object> map) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        map.forEach(params::addValue);
        String[] defaults = {"projectCode","projectName","projectType","recordMode","status","currentStage","progress",
            "principal","department","summary","riskLevel","artifactId","artifactCode","artifactName","archiveId","planCode","planName","planType","planStatus",
            "monitoringPurpose","monitoringScope","overallStrategy","responsiblePerson","participantNames",
            "monitoringLocation","defaultFrequencyValue","defaultFrequencyUnit","autoGenerateTask","alertEnabled",
            "executionCount","overdueCount","completionRate","targetType","targetName","sourceBusinessType",
            "sourceBusinessId","targetPart","targetLocation","riskLevel","priorityLevel","monitoringReason",
            "currentStatus","requiresImage","shootingPosition","enabled","indicatorCode","indicatorName",
            "indicatorCategory","dataType","valueUnit","baselineValue","normalMin","normalMax","warningMin",
            "warningMax","criticalMin","criticalMax","changeWarningValue","changeWarningRate",
            "expectedDirection","observationMethod","instrumentName","required","indicatorId","baselineUnit",
            "baselineStatus","baselineDescription","baselineMediaId","versionNo","isCurrent","createdBy",
            "taskId","taskCode","taskName","taskType","taskStatus","responsiblePerson","participantNames",
            "targetCount","completedTargetCount","overallResult","summary","generatedAutomatically",
            "targetId","recordId","recordCode","monitorPerson","overallStatus","comparisonResult",
            "observationDescription","changeDescription","resultConclusion","requiresRecheck",
            "requiresIntervention","requiresNewDiseaseSurvey","requiresNewProject","monitoringStatus",
            "valueNumber","valueText","previousValue","changeValue","changeRate","resultLevel",
            "resultDescription","manuallyConfirmed","alertCode","alertLevel","alertTitle","alertDescription",
            "triggerType","triggerValue","thresholdDescription","alertStatus","confirmedBy","immediateAction",
            "treatmentAdvice","requiresDiseaseSurvey","createdProjectId","issueDescription","treatmentMethod",
            "operatorName","recordDate","conclusion","remark","recordStatus"};
        for (String key : defaults) if (!params.hasValue(key)) params.addValue(key, null);
        return params;
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

    private String text(Object value) {
        return Objects.toString(value, "").trim();
    }

    private String normalizeRecordMode(String value) {
        return Set.of("quick", "standard", "full").contains(value) ? value : "standard";
    }

    private String defaultText(Object value, String fallback) {
        String result = text(value);
        return result.isBlank() ? fallback : result;
    }

    private String inputText(Map<String, Object> data, String key, String fallback) {
        return defaultText(data.get(key), fallback);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private Long requiredId(Map<String, Object> map) {
        Long id = longValue(map.get("id"));
        if (id == null) {
            id = System.currentTimeMillis() + Math.abs(new Random().nextInt(10000));
            map.put("id", id);
        }
        return id;
    }

    private Long requiredGeneratedId(org.springframework.jdbc.support.GeneratedKeyHolder key, String operation) {
        Number generatedId = key.getKey();
        if (generatedId == null) {
            throw new IllegalStateException(operation + "保存失败：数据库未返回主键");
        }
        return generatedId.longValue();
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

    private boolean bool(Object value) {
        return value instanceof Boolean b ? b : value instanceof Number n && n.intValue() != 0;
    }

    private LocalDate date(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        String text = value.toString().trim().replace('/', '-');
        if (text.length() >= 10) text = text.substring(0, 10);
        try { return LocalDate.parse(text); } catch (DateTimeParseException e) { return null; }
    }

    private LocalDateTime dateTime(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        String text = value.toString().trim().replace('/', '-').replace('T', ' ');
        if (text.length() == 10) return date(text).atStartOfDay();
        try {
            return LocalDateTime.parse(text.substring(0, Math.min(text.length(), 19)), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try { return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss")); }
            catch (Exception ignored) { return null; }
        }
    }
}
