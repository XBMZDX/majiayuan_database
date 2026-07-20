package com.itheima.bigevent;

import com.itheima.bigevent.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RestorationServiceTests {
    @Autowired RestorationService restorationService;
    @Autowired MonitoringService monitoringService;
    @Autowired ArchiveService archiveService;
    @Autowired ProcessService processService;

    @Test
    void structuredResultMediaModelArchiveAndMonitoringRoundTrip() throws Exception {
        Map<String, Object> project = monitoringService.createProject(mapOf(
            "projectCode", "RST-TEST-" + System.nanoTime(), "projectName", "复原成果接口事务测试项目",
            "artifactName", "测试文物", "projectType", "综合", "riskLevel", "medium"));
        Long projectId = ((Number) project.get("id")).longValue();
        Map<String, Object> archiveWorkbench = archiveService.create(projectId, Map.of());
        Map<String, Object> archive = map(archiveWorkbench.get("archive"));
        Map<String, Object> workspace = map(archiveWorkbench.get("workspace"));
        map(workspace.get("plan")).put("planStatus", "submitted");
        workspace.put("planDiseaseList", new ArrayList<>(List.of(mapOf(
            "id", 97001L, "diseaseRecordIds", List.of(), "diseaseName", "整体缺损",
            "treatmentStrategy", "实体补配与数字复原"))));
        archiveService.save(((Number) archive.get("id")).longValue(),
            Map.of("archive", archive, "workspace", workspace, "completeness", 40));

        Map<String, Object> processWorkbench = processService.create(projectId);
        Map<String, Object> process = map(processWorkbench.get("processRecord"));
        Map<String, Object> step = list(processWorkbench.get("steps")).getFirst();
        byte[] processBytes = {1, 3, 5, 7};
        Map<String, Object> processMedia = processService.uploadMedia(
            ((Number) step.get("id")).longValue(),
            new MockMultipartFile("file", "restoration-source.png", "image/png", processBytes),
            Map.of("mediaStage", "after", "title", "复原操作后", "selectedForRestoration", "true"));

        Map<String, Object> part = mapOf(
            "id", 97011L, "partCode", "PART-01", "partName", "实体补配构件",
            "partType", "physical_completion", "targetLocation", "缺损部位",
            "scopeDescription", "可拆卸补配", "materialName", "惰性补配材料",
            "technique", "机械连接", "evidenceLevel", "A", "confidenceLevel", "high",
            "removable", true, "reversible", true, "reversibleDescription", "可整体拆除",
            "distinguishable", true, "displayStyle", "虚线", "annotationText", "实体补配",
            "percentageValue", 20, "sortOrder", 1, "modelLayer", true,
            "modelObjectName", "Mesh_01", "layerVisible", false,
            "annotationPosition", mapOf("x", 1.25, "y", 2.5, "z", -0.75));
        Map<String, Object> source = mapOf(
            "id", 97012L, "restorationPartId", 97011L, "sourceType", "process_step",
            "sourceTitle", "实体补配修复步骤", "businessType", "process_step",
            "businessId", step.get("id"), "supportDescription", "记录实际材料与连接方式",
            "reliability", "high", "evidenceLevel", "A", "sortOrder", 1);
        Map<String, Object> referencedMedia = mapOf(
            "id", 97013L, "restorationPartId", 97011L, "sourceMediaId", processMedia.get("id"),
            "sourceBusinessType", "process_step", "sourceBusinessId", step.get("id"),
            "mediaStage", "final", "mediaType", "image", "fileName", processMedia.get("fileName"),
            "title", "复原完成状态", "description", "引用修复过程影像",
            "isPrimary", true, "selectedForArchive", true,
            "selectedAsMonitoringBaseline", true, "sortOrder", 1);
        Map<String, Object> version = mapOf(
            "id", 97014L, "versionNo", "V1.0", "versionName", "完成版本",
            "versionType", "final", "changeDescription", "完成实体补配",
            "evidenceLevel", "A", "confidenceLevel", "high", "isCurrent", true,
            "isRecommended", true, "archived", false, "creator", "测试人员");
        Map<String, Object> result = mapOf(
            "id", 97010L, "processId", process.get("id"), "resultCode", "RR-TEST-97010",
            "resultName", "实体补配复原成果", "restorationType", "physical",
            "restorationCategory", "missing_part", "targetPart", "整体缺损部位",
            "restorationScope", "缺失构件实体补配", "restorationPurpose", "恢复结构完整性",
            "basisSummary", "依据修复步骤和原始连接关系", "methodSummary", "可拆卸机械连接",
            "resultSummary", "补配完成且状态稳定", "uncertaintySummary", "表面纹理保守处理",
            "evidenceLevel", "A", "confidenceLevel", "high", "resultStatus", "completed",
            "currentVersion", "V1.0", "completionRate", 100, "overallScore", 4.8,
            "selectedForArchive", true, "recommendedResult", true, "requiresMonitoring", true,
            "principal", "测试人员", "participantNames", "测试参与人",
            "startDate", "2026-07-18", "completionDate", "2026-07-20",
            "stepIds", List.of(step.get("id")), "comparisonIds", List.of(),
            "diseaseIds", List.of(), "detectionIds", List.of(),
            "methodParameters", List.of(mapOf("id", 97015L, "name", "接缝宽度",
                "value", "0.8", "unit", "mm", "description", "完成值")),
            "evaluation", mapOf("evidenceScore", 5, "structuralScore", 4.5,
                "finalConclusion", "符合可辨识和可逆原则", "evaluator", "测试人员",
                "evaluationDate", "2026-07-20"),
            "monitoring", mapOf("partIds", List.of(97011L), "indicators", "接缝宽度、材料脱落",
                "cycle", "每30天", "baselineMediaId", 97013L,
                "warningConditions", "接缝增宽超过0.2mm", "note", "固定机位复查"),
            "parts", List.of(part), "sources", List.of(source),
            "media", List.of(referencedMedia), "models", List.of(), "versions", List.of(version));

        Map<String, Object> saved = restorationService.saveWorkbench(projectId, List.of(result));
        Map<String, Object> restored = list(saved.get("results")).getFirst();
        assertThat(list(restored.get("parts"))).hasSize(1);
        assertThat(list(restored.get("parts")).getFirst())
            .containsEntry("modelObjectName", "Mesh_01")
            .containsEntry("layerVisible", false);
        assertThat(((Number) map(list(restored.get("parts")).getFirst()
            .get("annotationPosition")).get("x")).doubleValue()).isEqualTo(1.25);
        assertThat(list(restored.get("sources"))).hasSize(1);
        assertThat(list(restored.get("versions"))).hasSize(1);
        Long referencedId = ((Number) list(restored.get("media")).getFirst().get("id")).longValue();
        assertThat((byte[]) restorationService.getMedia(referencedId).get("fileData"))
            .containsExactly(processBytes);

        byte[] mediaBytes = {2, 4, 6, 8};
        Map<String, Object> uploadedMedia = restorationService.uploadMedia(97010L,
            new MockMultipartFile("file", "final.png", "image/png", mediaBytes),
            Map.of("mediaStage", "final", "title", "最终成果图"));
        byte[] modelBytes = {9, 8, 7, 6, 5};
        Map<String, Object> uploadedModel = restorationService.uploadModel(97010L,
            new MockMultipartFile("file", "restored.glb", "model/gltf-binary", modelBytes),
            Map.of("modelName", "测试复原模型", "modelType", "restored", "scaleUnit", "mm"));
        assertThat((byte[]) restorationService.getMedia(
            ((Number) uploadedMedia.get("id")).longValue()).get("fileData")).containsExactly(mediaBytes);
        assertThat((byte[]) restorationService.getModel(
            ((Number) uploadedModel.get("id")).longValue()).get("fileData")).containsExactly(modelBytes);

        List<Map<String, Object>> loadedResults = list(restorationService.getWorkbench(projectId).get("results"));
        restorationService.saveWorkbench(projectId, loadedResults);
        assertThat((byte[]) restorationService.getModel(
            ((Number) uploadedModel.get("id")).longValue()).get("fileData")).containsExactly(modelBytes);
        assertThat(list(map(archiveService.getWorkbench(projectId).get("workspace"))
            .get("restorationResults"))).hasSize(1);
        assertThat(list(monitoringService.getSources(projectId).get("restorations"))).hasSize(1);

        Map<String, Object> changed = loadedResults.getFirst();
        Map<String, Object> oldVersion = list(changed.get("versions")).getFirst();
        oldVersion.put("isCurrent", false);
        changed.put("resultName", "修改后的复原成果");
        changed.put("currentVersion", "V1.1");
        list(changed.get("versions")).add(mapOf(
            "id", 97016L, "versionNo", "V1.1", "versionName", "修改版本",
            "versionType", "revision", "changeDescription", "修改成果名称",
            "evidenceLevel", "A", "confidenceLevel", "high", "isCurrent", true,
            "creator", "测试人员"));
        restorationService.saveWorkbench(projectId, loadedResults);

        Map<String, Object> historical = restorationService.getVersion(
            97010L, ((Number) oldVersion.get("id")).longValue());
        assertThat(map(historical.get("snapshot")).get("resultName")).isEqualTo("实体补配复原成果");
        Map<String, Object> restoredWorkbench = restorationService.restoreVersion(
            97010L, ((Number) oldVersion.get("id")).longValue());
        assertThat(list(restoredWorkbench.get("results")).getFirst().get("resultName"))
            .isEqualTo("实体补配复原成果");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) { return (Map<String, Object>) value; }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) { return (List<Map<String, Object>>) value; }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
