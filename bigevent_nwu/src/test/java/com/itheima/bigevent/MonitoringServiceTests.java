package com.itheima.bigevent;

import com.itheima.bigevent.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MonitoringServiceTests {
    @Autowired
    private MonitoringService service;

    @Test
    void workbenchRoundTripAndMysqlMediaStorage() {
        Map<String, Object> created = service.createProject(map(
            "projectCode", "TEST-" + System.nanoTime(),
            "projectName", "监测接口事务测试项目",
            "artifactName", "用户键入的测试文物",
            "projectType", "综合",
            "riskLevel", "medium"
        ));
        Long projectId = ((Number) created.get("id")).longValue();
        assertNotNull(service.getProject(projectId));
        assertEquals("用户键入的测试文物", service.getProject(projectId).get("artifactName"));
        assertNull(service.getSources(projectId).get("archiveAdvice"));

        Map<String, Object> indicator = map(
            "id", 99003L, "indicatorCode", "TEST-WIDTH", "indicatorName", "测试宽度",
            "indicatorCategory", "structure", "dataType", "number", "valueUnit", "mm",
            "baselineValue", 0.4, "normalMin", 0, "normalMax", 0.5,
            "warningMax", 0.7, "criticalMax", 1.0, "changeWarningValue", 0.2,
            "expectedDirection", "stable", "observationMethod", "测试", "instrumentName", "测宽仪",
            "required", true
        );
        Map<String, Object> baseline = map(
            "id", 99004L, "sourceBusinessType", "manual", "baselineDate", "2026-07-20",
            "baselineValue", 0.4, "baselineUnit", "mm", "baselineStatus", "稳定",
            "baselineDescription", "测试基准", "versionNo", "V1.0", "isCurrent", true
        );
        Map<String, Object> target = map(
            "id", 99002L, "targetType", "disease", "targetName", "测试对象",
            "sourceBusinessType", "manual", "targetPart", "右侧车轮", "targetLocation", "测试位置",
            "riskLevel", "medium", "priorityLevel", "medium", "monitoringReason", "接口测试",
            "currentStatus", "稳定", "requiresImage", true, "shootingPosition", "正面",
            "enabled", true, "indicators", List.of(indicator), "baseline", baseline
        );
        Map<String, Object> task = map(
            "id", 99005L, "taskCode", "MT-TEST-99005", "taskName", "测试任务",
            "taskType", "routine", "taskStatus", "in_progress", "plannedDate", "2026-08-25",
            "dueDate", "2026-08-30", "actualStartTime", "2026-08-25 09:00:00",
            "responsiblePerson", "测试人员", "targetCount", 1, "completedTargetCount", 0,
            "completionRate", 0, "generatedAutomatically", false
        );
        Map<String, Object> value = map(
            "id", 99007L, "indicatorId", 99003L, "indicatorName", "测试宽度",
            "valueNumber", 0.5, "valueUnit", "mm", "baselineValue", 0.4,
            "previousValue", 0.4, "changeValue", 0.1, "changeRate", 25,
            "resultLevel", "normal", "resultDescription", "正常", "manuallyConfirmed", true
        );
        Map<String, Object> record = map(
            "id", 99006L, "taskId", 99005L, "targetId", 99002L, "recordCode", "MR-TEST-99006",
            "monitoringDate", "2026-08-25 09:30:00", "monitorPerson", "测试人员",
            "monitoringLocation", "实验室", "overallStatus", "stable", "comparisonResult", "same",
            "observationDescription", "稳定", "changeDescription", "无明显变化",
            "resultConclusion", "正常", "requiresRecheck", false, "requiresIntervention", false,
            "requiresNewDiseaseSurvey", false, "requiresNewProject", false,
            "nextMonitoringDate", "2026-09-25", "monitoringStatus", "draft",
            "values", List.of(value), "media", new ArrayList<>()
        );
        Map<String, Object> alert = map(
            "id", 99008L, "taskId", 99005L, "recordId", 99006L, "targetId", 99002L,
            "indicatorId", 99003L, "alertCode", "ALERT-TEST-" + System.nanoTime(),
            "alertLevel", "critical", "alertTitle", "测试宽度严重异常",
            "alertDescription", "测试指标超过严重阈值", "triggerType", "threshold",
            "triggerValue", "1.2mm", "thresholdDescription", "大于红色阈值1.0mm",
            "alertStatus", "new", "discoveredTime", "2026-08-25 10:00:00",
            "requiresRecheck", true, "requiresDiseaseSurvey", false,
            "requiresIntervention", true, "requiresNewProject", true
        );
        Map<String, Object> plan = map(
            "id", 99001L, "artifactId", null, "archiveId", null, "planCode", "MP-TEST-99001",
            "planName", "监测接口测试计划", "planType", "comprehensive", "planStatus", "active",
            "monitoringPurpose", "验证真实数据库", "monitoringScope", "测试对象",
            "overallStrategy", "结构化保存", "responsiblePerson", "测试人员",
            "monitoringLocation", "实验室", "startDate", "2026-08-01",
            "expectedEndDate", "2027-08-01", "nextMonitoringDate", "2026-09-25",
            "defaultFrequencyValue", 1, "defaultFrequencyUnit", "month",
            "autoGenerateTask", true, "alertEnabled", true, "executionCount", 0,
            "overdueCount", 0, "completionRate", 0, "targets", List.of(target),
            "tasks", List.of(task), "records", List.of(record), "alerts", List.of(alert)
        );

        service.saveWorkbench(projectId, List.of(plan));
        List<Map<String, Object>> plans = service.getPlans(projectId);
        assertEquals(1, plans.size());
        assertEquals("监测接口测试计划", plans.getFirst().get("planName"));
        assertEquals(1, ((List<?>) plans.getFirst().get("targets")).size());

        Map<String, Object> alertProject = service.createProjectFromAlert(99008L, map(
            "projectName", "预警触发的测试保护修复项目",
            "projectType", "修复",
            "expectedEndDate", "2026-12-31"
        ));
        assertEquals("预警触发的测试保护修复项目", alertProject.get("projectName"));
        assertEquals(projectId, ((Number) alertProject.get("sourceProjectId")).longValue());
        assertEquals(99008L, ((Number) alertProject.get("sourceAlertId")).longValue());
        Map<String, Object> repeated = service.createProjectFromAlert(99008L, Map.of());
        assertEquals(alertProject.get("id"), repeated.get("id"));
        assertEquals(true, repeated.get("alreadyCreated"));

        var file = new MockMultipartFile("file", "monitor.png", "image/png", new byte[]{1, 2, 3, 4});
        Map<String, Object> uploaded = service.uploadMedia(99006L, file, Map.of("mediaRole", "current"));
        Map<String, Object> stored = service.getMedia(((Number) uploaded.get("id")).longValue());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, (byte[]) stored.get("fileData"));
    }

    private static Map<String, Object> map(Object... values) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
