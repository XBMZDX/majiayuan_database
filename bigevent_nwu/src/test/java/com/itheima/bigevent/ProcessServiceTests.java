package com.itheima.bigevent;

import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.MonitoringService;
import com.itheima.bigevent.service.ProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProcessServiceTests {
    @Autowired ProcessService processService;
    @Autowired ArchiveService archiveService;
    @Autowired MonitoringService monitoringService;

    @Test
    void processLifecycleAndMysqlMediaAreAvailable() throws Exception {
        Long projectId = ((Number) monitoringService.getProjects().getFirst().get("id")).longValue();
        Map<String, Object> archiveWorkbench = archiveService.create(projectId, Map.of());
        Map<String, Object> archive = map(archiveWorkbench.get("archive"));
        Map<String, Object> workspace = map(archiveWorkbench.get("workspace"));
        Map<String, Object> plan = map(workspace.get("plan"));
        plan.put("planStatus", "completed");
        plan.put("planName", "测试保护修复方案");
        List<Map<String, Object>> diseases = list(workspace.get("diseaseRecords"));
        Object diseaseId = diseases.isEmpty() ? null : diseases.getFirst().get("id");
        workspace.put("planDiseaseList", new ArrayList<>(List.of(new LinkedHashMap<>(Map.of(
            "id", 90001L,
            "diseaseRecordIds", diseaseId == null ? List.of() : List.of(diseaseId),
            "diseaseName", diseases.isEmpty() ? "整体" : diseases.getFirst().get("diseaseName"),
            "treatmentStrategy", "表面清理、局部加固"
        )))));
        archiveWorkbench = archiveService.save(((Number) archive.get("id")).longValue(),
            Map.of("archive", archive, "workspace", workspace, "completeness", 30));

        Map<String, Object> processWorkbench = processService.create(projectId);
        Map<String, Object> process = map(processWorkbench.get("processRecord"));
        List<Map<String, Object>> steps = list(processWorkbench.get("steps"));
        assertThat(process.get("id")).isNotNull();
        assertThat(steps).hasSize(1);

        Map<String, Object> firstStep = steps.getFirst();
        var image = new MockMultipartFile("file", "process-test.png", "image/png", new byte[]{1, 2, 3, 4});
        Map<String, Object> media = processService.uploadMedia(((Number) firstStep.get("id")).longValue(), image,
            Map.of("mediaStage", "before", "title", "操作前", "photographer", "测试人员"));
        assertThat(processService.getMedia(((Number) media.get("id")).longValue()).get("fileData"))
            .isEqualTo(image.getBytes());

        firstStep.put("operatorName", "测试人员");
        process.put("processStatus", "in_progress");
        process.put("totalSteps", 1);
        Map<String, Object> saved = processService.save(((Number) process.get("id")).longValue(),
            Map.of("processRecord", process, "steps", steps));
        assertThat(map(saved.get("processRecord")).get("processStatus")).isEqualTo("in_progress");
        assertThat(processService.getSummary(projectId).get("exists")).isEqualTo(true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return (List<Map<String, Object>>) value;
    }
}
