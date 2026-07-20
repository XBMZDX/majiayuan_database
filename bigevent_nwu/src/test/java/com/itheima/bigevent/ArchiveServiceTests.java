package com.itheima.bigevent;

import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArchiveServiceTests {
    @Autowired ArchiveService archiveService;
    @Autowired MonitoringService monitoringService;
    @Autowired JdbcTemplate jdbc;

    @Test
    void archiveLifecycleAndMysqlAttachmentAreAvailable() throws Exception {
        Long projectId = ((Number) monitoringService.getProjects().getFirst().get("id")).longValue();
        Map<String, Object> workbench = archiveService.create(projectId, Map.of());
        Map<String, Object> archive = map(workbench.get("archive"));
        Map<String, Object> workspace = map(workbench.get("workspace"));

        assertThat(archive.get("id")).isNotNull();
        assertThat(workspace).containsKeys("survey", "diseaseRecords", "plan", "advice", "attachments");

        archive.put("executiveSummary", "接口测试摘要");
        Map<String, Object> saved = archiveService.save(
            ((Number) archive.get("id")).longValue(),
            Map.of("archive", archive, "workspace", workspace, "completeness", 25)
        );
        assertThat(map(saved.get("archive")).get("executiveSummary")).isEqualTo("接口测试摘要");

        var file = new MockMultipartFile("file", "archive-test.txt", "text/plain",
            "archive attachment".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> attachment = archiveService.uploadAttachment(
            ((Number) archive.get("id")).longValue(), file,
            Map.of("fileType", "其他", "sourceModule", "档案自身附件", "version", "V1.0")
        );
        Map<String, Object> binary = archiveService.getAttachment(((Number) attachment.get("id")).longValue());
        assertThat(binary.get("fileData")).isEqualTo(file.getBytes());

        Long archiveId = ((Number) archive.get("id")).longValue();
        Map<String, Object> finalized = archiveService.finalizeArchive(
            archiveId, Map.of("archive", archive, "workspace", workspace, "completeness", 90)
        );
        assertThat(map(finalized.get("archive")).get("archiveStatus")).isEqualTo("completed");

        byte[] word = (byte[]) archiveService.exportArchive(archiveId, "docx").get("fileData");
        byte[] pdf = (byte[]) archiveService.exportArchive(archiveId, "pdf").get("fileData");
        assertThat(new String(Arrays.copyOf(word, 2), StandardCharsets.ISO_8859_1)).isEqualTo("PK");
        assertThat(new String(Arrays.copyOf(pdf, 4), StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void detectionCandidatesComeFromTheRealDetectionTables() {
        Map<String, Object> project = monitoringService.getProjects().getFirst();
        Long projectId = ((Number) project.get("id")).longValue();
        String artifactCode = Objects.toString(project.get("artifactCode"), "").trim();
        String artifactName = Objects.toString(project.get("artifactName"), "").trim();
        assertThat(artifactCode.isBlank() && artifactName.isBlank()).isFalse();

        Number detectionId = new SimpleJdbcInsert(jdbc)
            .withTableName("detection_analysis")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "artifact_code", artifactCode,
                "artifact_name", artifactName,
                "sample_position", "测试取样点",
                "purpose", "保护修复材料选择",
                "instrument_name", "测试仪器",
                "instrument_model", "TEST-01",
                "manager", "接口测试员"
            ));
        String method = "接口检测-" + UUID.randomUUID();
        Number analysisResultId = new SimpleJdbcInsert(jdbc)
            .withTableName("analysis_results")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "detection_id", detectionId,
                "artifact_code", artifactCode,
                "artifact_name", artifactName,
                "experiment_method", method,
                "detection_purpose", "判断修复材料适用性",
                "instrument_model", "TEST-01"
            ));
        new SimpleJdbcInsert(jdbc)
            .withTableName("experiment_results")
            .execute(Map.of(
                "detection_id", detectionId,
                "experiment_name", method,
                "status", "已完成",
                "result_data", "{\"fields\":[],\"rows\":[{\"value\":1}]}",
                "attachments", "[{\"name\":\"接口检测报告.pdf\"}]",
                "notes", "{\"conclusion\":\"测试结果稳定\",\"analysis\":\"满足修复要求\"}"
            ));

        List<Map<String, Object>> candidates = archiveService.getDetectionCandidates(projectId);
        Map<String, Object> candidate = candidates.stream()
            .filter(item -> analysisResultId.longValue() == ((Number) item.get("analysisResultId")).longValue())
            .findFirst()
            .orElseThrow();
        assertThat(candidate)
            .containsEntry("conclusion", "测试结果稳定")
            .containsEntry("reportName", "接口检测报告.pdf")
            .containsEntry("purpose", "判断修复材料适用性")
            .containsEntry("hasResult", true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return new HashMap<>((Map<String, Object>) value);
    }
}
