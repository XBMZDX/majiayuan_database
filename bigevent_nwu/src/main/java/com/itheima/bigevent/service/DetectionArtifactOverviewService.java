package com.itheima.bigevent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 将文物、检测任务和实验结果汇总为面向“检测分析总览”的只读数据。
 */
@Service
public class DetectionArtifactOverviewService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getOverview() {
        // 每条 artifacts 记录都必须保留，不能用文物编号/名称去重，否则会少统计同编号或同名文物。
        List<ArtifactSummary> summaries = new ArrayList<>();
        Map<String, List<ArtifactSummary>> artifactIndex = new LinkedHashMap<>();
        // 只有检测分析中找不到实体文物的记录才按标识合并为“未关联”文物。
        Map<String, ArtifactSummary> unlinkedSummaries = new LinkedHashMap<>();
        Set<String> matrixMethods = new LinkedHashSet<>(loadLabMethods());

        for (Map<String, Object> artifact : jdbcTemplate.queryForList("""
            SELECT id, new_artifact_code AS newArtifactCode, new_artifact_name AS newArtifactName,
                   original_artifact_code AS originalArtifactCode, original_artifact_name AS originalArtifactName,
                   material1, excavation_relic AS excavationRelic,
                   coffin_index AS coffinIndex, chariot_index AS chariotIndex
            FROM artifacts
            ORDER BY id
            """)) {
            Integer artifactId = number(artifact.get("id"));
            String code = firstText(artifact.get("newArtifactCode"), artifact.get("originalArtifactCode"));
            String name = firstText(artifact.get("newArtifactName"), artifact.get("originalArtifactName"));
            ArtifactSummary summary = new ArtifactSummary("artifact:" + artifactId, code, name);
            summary.excavationRelic = text(artifact.get("excavationRelic"));
            summary.material = text(artifact.get("material1"));
            summary.sourceType = sourceType(artifact.get("coffinIndex"), artifact.get("chariotIndex"));
            summaries.add(summary);
            String identity = artifactIdentity(code, name);
            if (!"unlinked:".equals(identity)) {
                artifactIndex.computeIfAbsent(identity, ignored -> new ArrayList<>()).add(summary);
            }
        }

        Map<String, ResultSnapshot> resultSnapshots = loadResultSnapshots();
        List<Map<String, Object>> detections = jdbcTemplate.queryForList("""
            SELECT id, artifact_code AS artifactCode, artifact_name AS artifactName,
                   excavation_relic AS excavationRelic, sample_material AS sampleMaterial,
                   purpose, sample_status AS sampleStatus, create_time AS createTime, update_time AS updateTime
            FROM detection_analysis
            ORDER BY id
            """);

        for (Map<String, Object> detection : detections) {
            Integer detectionId = number(detection.get("id"));
            String code = text(detection.get("artifactCode"));
            String name = text(detection.get("artifactName"));
            String identity = artifactIdentity(code, name);
            List<ArtifactSummary> matchedArtifacts = artifactIndex.get(identity);
            if (matchedArtifacts == null || matchedArtifacts.isEmpty()) {
                String unlinkedKey = identity.startsWith("unlinked:") ? "unlinked:detection:" + detectionId : "unlinked:" + identity;
                ArtifactSummary summary = unlinkedSummaries.get(unlinkedKey);
                if (summary == null) {
                    summary = new ArtifactSummary(unlinkedKey, code, name);
                    summary.excavationRelic = text(detection.get("excavationRelic"));
                    summary.material = text(detection.get("sampleMaterial"));
                    summary.sourceType = "未关联";
                    unlinkedSummaries.put(unlinkedKey, summary);
                    summaries.add(summary);
                }
                matchedArtifacts = List.of(summary);
            }
            for (ArtifactSummary summary : matchedArtifacts) {
                summary.excavationRelic = text(detection.get("excavationRelic"));
                if ("未关联".equals(summary.sourceType)) summary.material = text(detection.get("sampleMaterial"));
                summary.latestDetectionTime = latest(summary.latestDetectionTime, detection.get("updateTime"), detection.get("createTime"));
                List<String> methods = splitMethods(text(detection.get("purpose")));
                for (String method : methods) {
                    matrixMethods.add(method);
                    ResultSnapshot snapshot = resultSnapshots.get(resultKey(detectionId, method));
                    summary.addMethod(method, snapshot, detectionId, text(detection.get("sampleStatus")));
                }
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        int notDetected = 0, inProgress = 0, partial = 0, complete = 0, noResult = 0;
        for (ArtifactSummary summary : summaries) {
            summary.finish();
            items.add(summary.toMap());
            switch (summary.resultStatus) {
                case "未检测" -> notDetected++;
                case "检测进行中" -> inProgress++;
                case "已有部分结果" -> partial++;
                case "结果完整" -> complete++;
                default -> noResult++;
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", items.size());
        stats.put("notDetected", notDetected);
        stats.put("inProgress", inProgress);
        stats.put("partial", partial);
        stats.put("complete", complete);
        stats.put("noResult", noResult);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("methods", new ArrayList<>(matrixMethods));
        result.put("stats", stats);
        return result;
    }

    private List<String> loadLabMethods() {
        List<String> methods = new ArrayList<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT name FROM lab_instruments WHERE name IS NOT NULL AND TRIM(name) != '' ORDER BY id")) {
            methods.add(text(row.get("name")));
        }
        return methods;
    }

    private Map<String, ResultSnapshot> loadResultSnapshots() {
        Map<String, ResultSnapshot> snapshots = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList("""
            SELECT ar.id AS analysisResultId, ar.detection_id AS detectionId,
                   ar.experiment_method AS experimentMethod,
                   er.status, er.result_data AS resultData, er.images, er.attachments, er.notes
            FROM analysis_results ar
            LEFT JOIN experiment_results er
              ON er.detection_id = ar.detection_id
             AND er.experiment_name COLLATE utf8mb4_unicode_ci = ar.experiment_method COLLATE utf8mb4_unicode_ci
            """)) {
            Integer detectionId = number(row.get("detectionId"));
            String method = text(row.get("experimentMethod"));
            if (detectionId == null || method.isBlank()) continue;
            ResultSnapshot snapshot = new ResultSnapshot();
            snapshot.analysisResultId = number(row.get("analysisResultId"));
            snapshot.status = text(row.get("status"));
            snapshot.hasResultData = hasValue(row.get("resultData"));
            snapshot.hasImages = hasValue(row.get("images"));
            snapshot.hasAttachments = hasValue(row.get("attachments"));
            snapshot.hasNotes = hasValue(row.get("notes"));
            snapshots.put(resultKey(detectionId, method), snapshot);
        }
        return snapshots;
    }

    private static final class ArtifactSummary {
        private final String key;
        private final String artifactCode;
        private final String artifactName;
        private String excavationRelic = "";
        private String material = "";
        private String sourceType = "墓葬";
        private LocalDateTime latestDetectionTime;
        private String resultStatus = "未检测";
        private String resultCompleteness = "无检测记录";
        private final Map<String, MethodSummary> methods = new LinkedHashMap<>();

        private ArtifactSummary(String key, String artifactCode, String artifactName) {
            this.artifactCode = artifactCode;
            this.artifactName = artifactName;
            this.key = key;
        }

        private void addMethod(String method, ResultSnapshot snapshot, Integer detectionId, String sampleStatus) {
            MethodSummary current = methods.computeIfAbsent(method, MethodSummary::new);
            current.merge(snapshot, detectionId, sampleStatus);
        }

        private void finish() {
            if (methods.isEmpty()) return;
            Collection<MethodSummary> values = methods.values();
            boolean anyInProgress = values.stream().anyMatch(MethodSummary::isInProgress);
            boolean allComplete = values.stream().allMatch(MethodSummary::isComplete);
            boolean anyPartial = values.stream().anyMatch(MethodSummary::hasAnyResult);
            if (anyInProgress) {
                resultStatus = "检测进行中";
                resultCompleteness = "检测尚未全部完成";
            } else if (allComplete) {
                resultStatus = "结果完整";
                resultCompleteness = "全部方法均已完成并填写结果";
            } else if (anyPartial) {
                resultStatus = "已有部分结果";
                resultCompleteness = "部分方法已有结果或附件";
            } else {
                resultStatus = "无结果";
                resultCompleteness = "已建立检测记录，尚未录入结果";
            }
        }

        private Map<String, Object> toMap() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("artifactKey", key);
            row.put("artifactCode", artifactCode);
            row.put("artifactName", artifactName);
            row.put("excavationRelic", excavationRelic);
            row.put("material", material);
            row.put("sourceType", sourceType);
            row.put("latestDetectionTime", latestDetectionTime == null ? null : TIME_FORMATTER.format(latestDetectionTime));
            row.put("resultStatus", resultStatus);
            row.put("resultCompleteness", resultCompleteness);
            row.put("methods", methods.values().stream().map(MethodSummary::toMap).toList());
            return row;
        }
    }

    private static final class MethodSummary {
        private final String name;
        private Integer analysisResultId;
        private String status = "";
        private boolean hasResultData;
        private boolean hasImages;
        private boolean hasAttachments;
        private boolean hasNotes;
        private boolean detected;

        private MethodSummary(String name) { this.name = name; }

        private void merge(ResultSnapshot snapshot, Integer detectionId, String sampleStatus) {
            detected = true;
            if (snapshot == null) {
                if (status.isBlank()) status = sampleStatus;
                return;
            }
            if (analysisResultId == null) analysisResultId = snapshot.analysisResultId;
            hasResultData |= snapshot.hasResultData;
            hasImages |= snapshot.hasImages;
            hasAttachments |= snapshot.hasAttachments;
            hasNotes |= snapshot.hasNotes;
            status = preferredStatus(status, snapshot.status, sampleStatus);
        }

        private boolean isInProgress() { return "检测中".equals(status); }
        private boolean hasAnyResult() { return hasResultData || hasImages || hasAttachments || hasNotes || "已完成".equals(status); }
        private boolean isComplete() { return "已完成".equals(status) && hasResultData && hasNotes && (hasImages || hasAttachments); }

        private Map<String, Object> toMap() {
            Map<String, Object> method = new LinkedHashMap<>();
            method.put("name", name);
            method.put("analysisResultId", analysisResultId);
            method.put("status", status.isBlank() ? "待检测" : status);
            method.put("detected", detected);
            method.put("complete", isComplete());
            return method;
        }
    }

    private static final class ResultSnapshot {
        private Integer analysisResultId;
        private String status;
        private boolean hasResultData;
        private boolean hasImages;
        private boolean hasAttachments;
        private boolean hasNotes;
    }

    private static String preferredStatus(String current, String candidate, String fallback) {
        String value = text(candidate);
        if (value.isBlank()) value = text(fallback);
        if ("检测中".equals(value) || "检测中".equals(current)) return "检测中";
        if ("已完成".equals(value) || "已完成".equals(current)) return "已完成";
        return current.isBlank() ? value : current;
    }

    private static String sourceType(Object coffinIndex, Object chariotIndex) {
        if (positive(chariotIndex)) return "车";
        if (positive(coffinIndex)) return "棺";
        return "墓葬";
    }

    private static boolean positive(Object value) {
        Integer number = number(value);
        return number != null && number > 0;
    }

    private static LocalDateTime latest(LocalDateTime current, Object... values) {
        LocalDateTime latest = current;
        for (Object value : values) {
            LocalDateTime time = toTime(value);
            if (time != null && (latest == null || time.isAfter(latest))) latest = time;
        }
        return latest;
    }

    private static LocalDateTime toTime(Object value) {
        if (value instanceof LocalDateTime time) return time;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        return null;
    }

    private static Integer number(Object value) {
        if (value instanceof Number number) return number.intValue();
        try { return value == null ? null : Integer.valueOf(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static String artifactIdentity(String code, String name) {
        String normalizedCode = normalizeCode(code);
        if (!normalizedCode.isBlank()) return "code:" + normalizedCode;
        String normalizedName = text(name).toLowerCase(Locale.ROOT);
        return normalizedName.isBlank() ? "unlinked:" : "name:" + normalizedName;
    }

    private static String resultKey(Integer detectionId, String method) {
        return detectionId + "|" + text(method);
    }

    private static List<String> splitMethods(String value) {
        Set<String> methods = new LinkedHashSet<>();
        for (String part : text(value).split("/")) if (!part.isBlank()) methods.add(part.trim());
        return new ArrayList<>(methods);
    }

    private static boolean hasValue(Object value) {
        String text = text(value);
        return !text.isBlank() && !"[]".equals(text) && !"{}".equals(text) && !"null".equalsIgnoreCase(text);
    }

    private static String firstText(Object... values) {
        for (Object value : values) if (!text(value).isBlank()) return text(value);
        return "";
    }

    private static String text(Object value) { return value == null ? "" : value.toString().trim(); }

    private static String normalizeCode(String value) {
        return text(value).replace('：', ':').replaceAll("[\\s\\-_]", "");
    }
}
