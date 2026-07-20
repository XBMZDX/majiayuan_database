package com.itheima.bigevent;

import com.itheima.bigevent.service.DiseaseSurveyService;
import com.itheima.bigevent.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DiseaseSurveyServiceTests {
    @Autowired
    private DiseaseSurveyService diseaseService;

    @Autowired
    private MonitoringService projectService;

    @Test
    void diseaseWorkbenchPersistsAndSubmitsRealMysqlData() throws Exception {
        Map<String, Object> project = projectService.createProject(map(
            "projectCode", "DISEASE-TEST-" + System.nanoTime(),
            "projectName", "病害调查事务测试项目",
            "artifactName", "测试文物",
            "projectType", "保护",
            "riskLevel", "low"
        ));
        Long projectId = ((Number) project.get("id")).longValue();
        Map<String, Object> initial = diseaseService.getWorkbench(projectId);
        assertTrue(((List<?>) initial.get("records")).isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, Object> survey = new HashMap<>((Map<String, Object>) initial.get("survey"));
        survey.put("surveyDate", "2026-07-18");
        survey.put("surveyor", "测试人员");
        survey.put("surveyLocation", "保护实验室");
        Map<String, Object> disease = map(
            "diseaseName", "测试裂隙",
            "diseaseCategory", "structural",
            "severity", "critical",
            "developmentStatus", "rapidly_developing",
            "extentValue", 12.5,
            "extentUnit", "cm",
            "partName", "器身",
            "structuralImpact", "overall",
            "emergency", true,
            "recommendedAction", "立即临时加固"
        );

        Map<String, Object> saved =
            diseaseService.saveWorkbench(projectId, survey, List.of(disease), false);
        assertEquals(1, ((List<?>) saved.get("records")).size());
        assertEquals("draft", ((Map<?, ?>) saved.get("survey")).get("status"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> savedRecords = (List<Map<String, Object>>) saved.get("records");
        Long recordId = ((Number) savedRecords.getFirst().get("id")).longValue();
        MockMultipartFile image = new MockMultipartFile(
            "file", "裂隙.png", "image/png", "test-image".getBytes(StandardCharsets.UTF_8)
        );
        Map<String, Object> media = diseaseService.uploadMedia(
            recordId, image, Map.of("title", "裂隙正面")
        );
        Long mediaId = ((Number) media.get("id")).longValue();
        Map<String, Object> annotated = diseaseService.saveAnnotations(mediaId, List.of(map(
            "id", "annotation-1", "type", "rect", "label", "裂隙",
            "color", "#ef4444", "x", 10, "y", 20, "width", 100, "height", 40
        )));
        assertEquals(1, ((List<?>) annotated.get("annotations")).size());
        assertArrayEquals(image.getBytes(), (byte[]) diseaseService.getMedia(mediaId).get("fileData"));

        @SuppressWarnings("unchecked")
        Map<String, Object> savedSurvey = (Map<String, Object>) saved.get("survey");
        Map<String, Object> submitted =
            diseaseService.saveWorkbench(projectId, savedSurvey, savedRecords, true);
        assertEquals("submitted", ((Map<?, ?>) submitted.get("survey")).get("status"));
        Object mediaCount = ((Map<?, ?>) ((List<?>) submitted.get("records")).getFirst()).get("mediaCount");
        assertEquals(1, ((Number) mediaCount).intValue());
        assertEquals("high", projectService.getProject(projectId).get("riskLevel"));
        assertEquals("planning", projectService.getProject(projectId).get("currentStage"));
    }

    private static Map<String, Object> map(Object... values) {
        Map<String, Object> result = new HashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(String.valueOf(values[index]), values[index + 1]);
        }
        return result;
    }
}
