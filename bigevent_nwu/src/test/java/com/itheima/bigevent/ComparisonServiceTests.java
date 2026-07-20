package com.itheima.bigevent;

import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.ComparisonService;
import com.itheima.bigevent.service.MonitoringService;
import com.itheima.bigevent.service.ProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ComparisonServiceTests {
    @Autowired ComparisonService comparisonService;
    @Autowired ProcessService processService;
    @Autowired ArchiveService archiveService;
    @Autowired MonitoringService monitoringService;
    @Autowired JdbcTemplate jdbc;

    @Test
    void comparisonRoundTripCopiesProcessMediaAndSyncsArchive() throws Exception {
        Map<String, Object> project = monitoringService.createProject(mapOf(
            "projectCode", "CMP-TEST-" + System.nanoTime(),
            "projectName", "前后对比接口事务测试项目",
            "artifactName", "测试文物",
            "projectType", "综合",
            "riskLevel", "medium"
        ));
        Long projectId = ((Number) project.get("id")).longValue();
        Map<String, Object> archiveWorkbench = archiveService.create(projectId, Map.of());
        Map<String, Object> archive = map(archiveWorkbench.get("archive"));
        Map<String, Object> workspace = map(archiveWorkbench.get("workspace"));
        Map<String, Object> plan = map(workspace.get("plan"));
        plan.put("planStatus", "submitted");
        workspace.put("planDiseaseList", new ArrayList<>(List.of(mapOf(
            "id", 98001L, "diseaseRecordIds", List.of(), "diseaseName", "整体",
            "treatmentStrategy", "清理与加固"
        ))));
        archiveWorkbench = archiveService.save(((Number) archive.get("id")).longValue(),
            Map.of("archive", archive, "workspace", workspace, "completeness", 30));

        Map<String, Object> processWorkbench = processService.create(projectId);
        Map<String, Object> process = map(processWorkbench.get("processRecord"));
        Map<String, Object> step = list(processWorkbench.get("steps")).getFirst();
        Long stepId = ((Number) step.get("id")).longValue();
        byte[] beforeBytes = {1, 2, 3, 4};
        byte[] afterBytes = {5, 6, 7, 8};
        Map<String, Object> before = processService.uploadMedia(stepId,
            new MockMultipartFile("file", "before.png", "image/png", beforeBytes),
            Map.of("mediaStage", "before", "title", "修复前", "selectedForComparison", "true"));
        Map<String, Object> after = processService.uploadMedia(stepId,
            new MockMultipartFile("file", "after.png", "image/png", afterBytes),
            Map.of("mediaStage", "after", "title", "修复后", "selectedForComparison", "true"));

        Map<String, Object> group = mapOf(
            "id", 98010L, "processId", process.get("id"), "stepId", stepId,
            "comparisonCode", "CP-TEST-98010", "comparisonTitle", "清理加固前后对比",
            "comparisonType", "before_after", "targetPart", "整体",
            "beforeSummary", "表面积尘且局部松动", "afterSummary", "积尘清除且结构稳定",
            "comparisonDescription", "同部位同角度影像对比", "overallEffect", "good",
            "evaluationStatus", "completed", "overallComparison", false,
            "noApplicableMetrics", false, "selectedForArchive", true,
            "selectedForRestoration", false, "selectedAsMonitoringBaseline", true,
            "evaluation", mapOf("overallScore", 92, "remainingIssue", "继续观察",
                "finalConclusion", "修复效果良好", "evaluator", "测试人员",
                "evaluationDate", "2026-07-20"),
            "monitoring", mapOf("reviewPart", "整体", "notes", "每月复查"),
            "metrics", List.of(mapOf("id", 98011L, "metricName", "松动点数量",
                "metricCategory", "structure", "beforeValue", 3, "afterValue", 0,
                "valueUnit", "处", "expectedDirection", "decrease",
                "resultStatus", "achieved", "description", "达到预期")),
            "diseases", List.of(),
            "images", List.of(
                image(before, "before", 1),
                image(after, "after", 2)
            )
        );

        Map<String, Object> saved = comparisonService.saveWorkbench(projectId, List.of(group));
        List<Map<String, Object>> groups = list(saved.get("groups"));
        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().get("comparisonTitle")).isEqualTo("清理加固前后对比");
        assertThat(list(groups.getFirst().get("metrics"))).hasSize(1);
        assertThat(list(groups.getFirst().get("images"))).hasSize(2);

        Long comparisonId = ((Number) groups.getFirst().get("id")).longValue();
        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM conservation_comparison_metric WHERE comparison_id=?",
            Integer.class, comparisonId)).isEqualTo(1);
        List<byte[]> media = jdbc.query(
            "SELECT file_data FROM conservation_comparison_media WHERE comparison_id=? ORDER BY sequence_no",
            (rs, rowNum) -> rs.getBytes(1), comparisonId);
        assertThat(media).containsExactly(beforeBytes, afterBytes);

        Map<String, Object> archived = archiveService.getWorkbench(projectId);
        assertThat(list(map(archived.get("workspace")).get("comparisons"))).hasSize(1);
    }

    private Map<String, Object> image(Map<String, Object> source, String stage, int sequence) {
        return mapOf("id", source.get("id"), "sourceMediaId", source.get("id"),
            "imageStage", stage, "imageRole", "detail", "fileName", source.get("fileName"),
            "targetPart", "整体", "sequenceNo", sequence, "isPrimary", true,
            "sourceModule", "修复过程记录", "imageDescription", stage + "影像");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return (List<Map<String, Object>>) value;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
