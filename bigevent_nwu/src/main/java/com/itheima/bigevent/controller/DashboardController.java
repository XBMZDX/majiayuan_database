package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统首页展示看板接口。
 * 这个接口只做汇总读取，不改动业务数据；缺失的可选表会按 0 / 空列表返回，避免首页因为单张表未创建而不可用。
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {
    private final JdbcTemplate jdbc;
    private final Map<String, Boolean> tableCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Boolean>> columnCache = new ConcurrentHashMap<>();

    public DashboardController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("relicCount", safeCount("relics", null, false));
        summary.put("burialCount", safeCount("burial", null, false));
        summary.put("coffinCount", safeCount("coffin", null, false));
        summary.put("artifactCount", safeCount("artifacts", null, false));
        summary.put("detectionCount", safeCount("detection_analysis", null, false));
        summary.put("projectCount", safeCount("conservation_project", null, false));
        summary.put("activeProjectCount", safeCount("conservation_project", "status IN ('draft','active')", false));
        summary.put("completedProjectCount", safeCount("conservation_project", "status='completed'", false));
        summary.put("diseaseSurveyCount", safeCount("conservation_disease_survey", null, true));
        summary.put("diseaseRecordCount", safeCount("conservation_disease_record", null, true));
        summary.put("archiveCount", safeCount("conservation_archive", null, true));
        summary.put("processCount", safeCount("conservation_process", null, true));
        summary.put("comparisonCount", safeCount("conservation_comparison", null, true));
        summary.put("restorationCount", safeCount("conservation_restoration_result", null, true));
        summary.put("monitoringPlanCount", safeCount("monitoring_plan", null, true));
        summary.put("monitoringAlertCount", safeCount("monitoring_alert", null, false));
        summary.put("digitalResourceCount", safeCount("digital_resource", null, true));
        summary.put("digitalImageCount", safeCount("digital_resource", "resource_type='image'", true));
        summary.put("digitalDocumentCount", safeCount("digital_resource", "resource_type IN ('document','report','spreadsheet')", true));
        summary.put("digitalVideoAudioCount", safeCount("digital_resource", "resource_type IN ('video','audio')", true));
        summary.put("digitalModelCount", safeCount("digital_resource", "resource_type='model_3d'", true));
        summary.put("storageBytes", safeSum("digital_resource", "file_size", true));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("projectStages", projectStages());
        data.put("resourceTypes", resourceTypes(summary));
        data.put("recentProjects", recentProjects(6));
        data.put("recentResources", recentResources(6));
        data.put("recentArtifacts", recentArtifacts(6));
        data.put("recentDetections", recentDetections(6));
        data.put("recentArchives", recentArchives(6));
        data.put("recentAlerts", recentAlerts(6));
        return Result.success(data);
    }

    private List<Map<String, Object>> projectStages() {
        return List.of(
            item("待调查", safeCount("conservation_project", "current_stage='pendingSurvey'", false)),
            item("病害调查", safeCount("conservation_project", "current_stage='disease'", false)),
            item("方案制定", safeCount("conservation_project", "current_stage='planning'", false)),
            item("保护处理", safeCount("conservation_project", "current_stage='protection'", false)),
            item("修复执行", safeCount("conservation_project", "current_stage='repair'", false)),
            item("复原成果", safeCount("conservation_project", "current_stage='restoration'", false)),
            item("后续监测", safeCount("conservation_project", "current_stage='monitoring'", false)),
            item("已完成", safeCount("conservation_project", "status='completed'", false))
        );
    }

    private List<Map<String, Object>> resourceTypes(Map<String, Object> summary) {
        long total = number(summary.get("digitalResourceCount"));
        long image = number(summary.get("digitalImageCount"));
        long document = number(summary.get("digitalDocumentCount"));
        long video = number(summary.get("digitalVideoAudioCount"));
        long model = number(summary.get("digitalModelCount"));
        long other = Math.max(0, total - image - document - video - model);
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(item("图片", image));
        result.add(item("文档/报告", document));
        result.add(item("视频/音频", video));
        result.add(item("三维模型", model));
        result.add(item("其他", other));
        return result;
    }

    private List<Map<String, Object>> recentProjects(int limit) {
        if (!tableExists("conservation_project")) return List.of();
        return queryList("""
            SELECT id, project_code AS projectCode, project_name AS projectName,
                   artifact_name AS artifactName, current_stage AS currentStage,
                   status, risk_level AS riskLevel, progress, principal,
                   create_time AS createTime, update_time AS updateTime
            FROM conservation_project
            ORDER BY COALESCE(update_time, create_time) DESC, id DESC
            LIMIT ?
            """, limit);
    }

    private List<Map<String, Object>> recentResources(int limit) {
        if (!tableExists("digital_resource")) return List.of();
        String where = hasColumn("digital_resource", "deleted") ? "WHERE deleted=0" : "";
        return queryList("""
            SELECT id, resource_code AS resourceCode, resource_name AS resourceName,
                   original_file_name AS originalFileName, resource_type AS resourceType,
                   source_module AS sourceModule, resource_status AS resourceStatus,
                   data_status AS dataStatus, file_size AS fileSize,
                   upload_time AS uploadTime, update_time AS updateTime
            FROM digital_resource
            %s
            ORDER BY COALESCE(update_time, upload_time) DESC, id DESC
            LIMIT ?
            """.formatted(where), limit);
    }

    private List<Map<String, Object>> recentArtifacts(int limit) {
        if (!tableExists("artifacts")) return List.of();
        return queryList("""
            SELECT id, new_artifact_code AS artifactCode, new_artifact_name AS artifactName,
                   original_artifact_code AS originalArtifactCode,
                   original_artifact_name AS originalArtifactName,
                   material1, completeness, excavation_relic AS excavationRelic,
                   create_time AS createTime, update_time AS updateTime
            FROM artifacts
            ORDER BY COALESCE(update_time, create_time) DESC, id DESC
            LIMIT ?
            """, limit);
    }

    private List<Map<String, Object>> recentDetections(int limit) {
        if (!tableExists("detection_analysis")) return List.of();
        return queryList("""
            SELECT id, serial_number AS serialNumber, artifact_code AS artifactCode,
                   artifact_name AS artifactName, purpose, instrument_name AS instrumentName,
                   create_time AS createTime, update_time AS updateTime
            FROM detection_analysis
            ORDER BY COALESCE(update_time, create_time) DESC, id DESC
            LIMIT ?
            """, limit);
    }

    private List<Map<String, Object>> recentArchives(int limit) {
        if (!tableExists("conservation_archive")) return List.of();
        String where = hasColumn("conservation_archive", "deleted") ? "WHERE deleted=0" : "";
        return queryList("""
            SELECT id, archive_code AS archiveCode, archive_title AS archiveTitle,
                   archive_status AS archiveStatus, current_version AS currentVersion,
                   completeness_rate AS completenessRate, update_time AS updateTime
            FROM conservation_archive
            %s
            ORDER BY update_time DESC, id DESC
            LIMIT ?
            """.formatted(where), limit);
    }

    private List<Map<String, Object>> recentAlerts(int limit) {
        if (!tableExists("monitoring_alert")) return List.of();
        return queryList("""
            SELECT id, alert_code AS alertCode, alert_title AS alertTitle,
                   alert_level AS alertLevel, alert_status AS alertStatus,
                   project_id AS projectId, discovered_time AS discoveredTime
            FROM monitoring_alert
            ORDER BY discovered_time DESC, id DESC
            LIMIT ?
            """, limit);
    }

    private Map<String, Object> item(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private long safeCount(String table, String whereClause, boolean filterDeleted, Object... args) {
        if (!tableExists(table)) return 0L;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(table);
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        if (filterDeleted && hasColumn(table, "deleted")) {
            sql.append(" WHERE deleted=0");
            hasWhere = true;
        }
        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ").append(whereClause);
            if (args != null) {
                for (Object arg : args) params.add(arg);
            }
        }
        return scalarLong(sql.toString(), params.toArray());
    }

    private long safeSum(String table, String column, boolean filterDeleted) {
        if (!tableExists(table) || !hasColumn(table, column)) return 0L;
        String sql = "SELECT COALESCE(SUM(" + column + "),0) FROM " + table
            + (filterDeleted && hasColumn(table, "deleted") ? " WHERE deleted=0" : "");
        return scalarLong(sql);
    }

    private long scalarLong(String sql, Object... args) {
        try {
            Number value = jdbc.queryForObject(sql, Number.class, args);
            return value == null ? 0L : value.longValue();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private List<Map<String, Object>> queryList(String sql, Object... args) {
        try {
            return jdbc.query(sql, this::camelMap, args);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private boolean tableExists(String tableName) {
        return tableCache.computeIfAbsent(tableName, name -> {
            try {
                Integer count = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                    """, Integer.class, name);
                return count != null && count > 0;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    private boolean hasColumn(String tableName, String columnName) {
        return columnCache
            .computeIfAbsent(tableName, key -> new ConcurrentHashMap<>())
            .computeIfAbsent(columnName, name -> {
                try {
                    Integer count = jdbc.queryForObject("""
                        SELECT COUNT(*) FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                        """, Integer.class, tableName, name);
                    return count != null && count > 0;
                } catch (Exception ignored) {
                    return false;
                }
            });
    }

    private Map<String, Object> camelMap(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            result.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        return result;
    }

    private long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }
}
