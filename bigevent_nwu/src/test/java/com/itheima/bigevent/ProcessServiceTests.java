package com.itheima.bigevent;

import com.itheima.bigevent.service.ArchiveService;
import com.itheima.bigevent.service.DiseaseSurveyService;
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
    @Autowired DiseaseSurveyService diseaseSurveyService;
    @Autowired MonitoringService monitoringService;

    @Test
    void processLifecycleAndMysqlMediaAreAvailable() throws Exception {
        Map<String, Object> project = monitoringService.createProject(new LinkedHashMap<>(Map.of(
            "projectCode", "PROCESS-LIFECYCLE-" + System.nanoTime(),
            "projectName", "Process lifecycle test project",
            "artifactName", "Test artifact",
            "projectType", "comprehensive",
            "riskLevel", "medium"
        )));
        Long projectId = ((Number) project.get("id")).longValue();
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

    @Test
    void draftProcessCanBeCreatedWithoutArchiveOrFormalPlan() {
        Map<String, Object> project = monitoringService.createProject(new LinkedHashMap<>(Map.of(
            "projectCode", "PROCESS-DRAFT-" + System.nanoTime(),
            "projectName", "独立修复过程草稿测试项目",
            "artifactName", "测试文物",
            "projectType", "综合",
            "riskLevel", "medium"
        )));
        Long projectId = ((Number) project.get("id")).longValue();

        Map<String, Object> workbench = processService.create(projectId);
        Map<String, Object> process = map(workbench.get("processRecord"));

        assertThat(workbench.get("archive")).isNull();
        assertThat(process.get("archiveId")).isNull();
        assertThat(process.get("executionMode")).isEqualTo("manual");
        assertThat(list(workbench.get("steps"))).isEmpty();
    }

    @Test
    void generatedStepUsesDiseasePartAndRefreshesDiseaseReference() {
        Map<String, Object> project = monitoringService.createProject(new LinkedHashMap<>(Map.of(
            "projectCode", "PROCESS-SYNC-" + System.nanoTime(),
            "projectName", "病害引用同步测试项目",
            "artifactName", "测试文物",
            "projectType", "综合",
            "riskLevel", "medium"
        )));
        Long projectId = ((Number) project.get("id")).longValue();
        Map<String, Object> diseaseWorkbench = diseaseSurveyService.getWorkbench(projectId);
        Map<String, Object> survey = new LinkedHashMap<>(map(diseaseWorkbench.get("survey")));
        survey.put("surveyDate", "2026-07-20");
        survey.put("surveyor", "测试人员");
        Map<String, Object> disease = new LinkedHashMap<>(Map.of(
            "diseaseName", "初始裂隙",
            "diseaseCategory", "物理病害",
            "severity", "moderate",
            "developmentStatus", "stable",
            "partName", "器身",
            "extentUnit", "cm",
            "causeFactors", List.of()
        ));
        diseaseWorkbench = diseaseSurveyService.saveWorkbench(projectId, survey, List.of(disease), false);
        survey = new LinkedHashMap<>(map(diseaseWorkbench.get("survey")));
        Map<String, Object> savedDisease = list(diseaseWorkbench.get("records")).getFirst();

        Map<String, Object> archiveWorkbench = archiveService.create(projectId, Map.of());
        Map<String, Object> archive = map(archiveWorkbench.get("archive"));
        Map<String, Object> workspace = map(archiveWorkbench.get("workspace"));
        workspace.put("planDiseaseList", new ArrayList<>(List.of(new LinkedHashMap<>(Map.of(
            "id", 99001L,
            "diseaseRecordIds", List.of(savedDisease.get("id")),
            "diseaseName", savedDisease.get("diseaseName"),
            "treatmentStrategy", "local consolidation"
        )))));
        archiveService.save(((Number) archive.get("id")).longValue(),
            Map.of("archive", archive, "workspace", workspace, "completeness", 20));

        Map<String, Object> created = processService.create(projectId);
        Map<String, Object> step = list(created.get("steps")).getFirst();
        assertThat(step.get("targetPart")).isEqualTo("器身");

        savedDisease.put("diseaseName", "更新后的裂隙");
        savedDisease.put("partName", "腹部");
        diseaseSurveyService.saveWorkbench(projectId, survey, List.of(savedDisease), false);

        Map<String, Object> refreshed = processService.getWorkbench(projectId);
        Map<String, Object> refreshedStep = list(refreshed.get("steps")).getFirst();
        Map<String, Object> relatedDisease = list(refreshedStep.get("relatedDiseases")).getFirst();
        assertThat(refreshedStep.get("targetPart")).isEqualTo("腹部");
        assertThat(relatedDisease.get("diseaseName")).isEqualTo("更新后的裂隙");
        assertThat(relatedDisease.get("partName")).isEqualTo("腹部");
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
