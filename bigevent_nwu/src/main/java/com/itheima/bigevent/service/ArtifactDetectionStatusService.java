package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.artifacts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ArtifactDetectionStatusService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<artifacts> decorate(List<artifacts> artifactList) {
        if (artifactList == null || artifactList.isEmpty()) return artifactList;
        List<Map<String, Object>> detections = jdbcTemplate.queryForList("""
            SELECT id, artifact_code AS artifactCode, artifact_name AS artifactName, purpose
            FROM detection_analysis
            """);
        for (artifacts artifact : artifactList) {
            artifact.setTestingStatusDisplay(buildTestingStatusDisplay(artifact, detections));
        }
        return artifactList;
    }

    private String buildTestingStatusDisplay(artifacts artifact, List<Map<String, Object>> detections) {
        Set<String> artifactCodes = new LinkedHashSet<>();
        addIfNotBlank(artifactCodes, normalizeCode(artifact.getNewArtifactCode()));
        addIfNotBlank(artifactCodes, normalizeCode(artifact.getOriginalArtifactCode()));

        Set<String> artifactNames = new LinkedHashSet<>();
        addIfNotBlank(artifactNames, normalizeName(artifact.getNewArtifactName()));
        addIfNotBlank(artifactNames, normalizeName(artifact.getOriginalArtifactName()));

        Set<String> names = new LinkedHashSet<>();
        for (Map<String, Object> detection : detections) {
            String detectionCode = normalizeCode(detection.get("artifactCode"));
            String detectionName = normalizeName(detection.get("artifactName"));
            boolean matchedByCode = !detectionCode.isBlank() && artifactCodes.contains(detectionCode);
            boolean matchedByName = detectionCode.isBlank() && !detectionName.isBlank() && artifactNames.contains(detectionName);
            if (!matchedByCode && !matchedByName) continue;

            // 页面只展示检测分析中填写的实验方法（purpose），不使用仪器或结果状态作为替代。
            names.addAll(splitDetectionNames(Objects.toString(detection.get("purpose"), "")));
        }
        return names.isEmpty() ? "无" : String.join(" / ", names);
    }

    private String normalizeCode(Object value) {
        return Objects.toString(value, "")
            .trim()
            .replace('：', ':')
            .replaceAll("[\\s\\-_]", "");
    }

    private String normalizeName(Object value) {
        return Objects.toString(value, "").trim();
    }

    private List<String> splitDetectionNames(String value) {
        List<String> result = new ArrayList<>();
        for (String item : value.split("/")) {
            String text = item.trim();
            if (!text.isBlank()) result.add(text);
        }
        return result;
    }

    private void addIfNotBlank(Set<String> set, String value) {
        if (value != null && !value.isBlank()) set.add(value);
    }
}
