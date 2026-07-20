package com.itheima.bigevent;

import com.itheima.bigevent.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ConservationSummaryServiceTests {
    @Autowired
    private MonitoringService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void comparisonAndRestorationSummariesComeFromMysql() {
        Map<String, Object> project = service.createProject(map(
            "projectCode", "SUMMARY-" + System.nanoTime(),
            "projectName", "总览摘要事务测试项目",
            "artifactName", "测试文物",
            "projectType", "综合",
            "riskLevel", "medium"
        ));
        Long projectId = ((Number) project.get("id")).longValue();

        jdbc.update("""
            INSERT INTO conservation_comparison
            (project_id,comparison_title,comparison_status,selected_for_archive,selected_as_monitoring_baseline)
            VALUES (?,?,?, ?,?)
            """, projectId, "已完成对比", "completed", 1, 1);
        jdbc.update("""
            INSERT INTO conservation_comparison
            (project_id,comparison_title,comparison_status,selected_for_archive,selected_as_monitoring_baseline)
            VALUES (?,?,?, ?,?)
            """, projectId, "草稿对比", "draft", 0, 0);
        jdbc.update("""
            INSERT INTO conservation_comparison
            (project_id,comparison_title,comparison_status,selected_for_archive,selected_as_monitoring_baseline)
            VALUES (?,?,?, ?,?)
            """, projectId, "已审核对比", "reviewed", 0, 1);

        jdbc.update("""
            INSERT INTO conservation_restoration_result
            (project_id,result_name,restoration_type,result_status,selected_for_archive,recommended_result)
            VALUES (?,?,?,?,?,?)
            """, projectId, "实体成果", "physical", "completed", 1, 0);
        jdbc.update("""
            INSERT INTO conservation_restoration_result
            (project_id,result_name,restoration_type,result_status,selected_for_archive,recommended_result)
            VALUES (?,?,?,?,?,?)
            """, projectId, "推荐三维成果", "digital_3d", "in_progress", 0, 1);
        jdbc.update("""
            INSERT INTO conservation_restoration_result
            (project_id,result_name,restoration_type,result_status,selected_for_archive,recommended_result)
            VALUES (?,?,?,?,?,?)
            """, projectId, "二维成果", "digital_2d", "reviewed", 0, 0);
        jdbc.update("""
            INSERT INTO conservation_restoration_result
            (project_id,result_name,restoration_type,result_status,selected_for_archive,recommended_result)
            VALUES (?,?,?,?,?,?)
            """, projectId, "混合成果", "hybrid", "draft", 0, 0);

        Map<String, Object> comparison = service.getComparisonSummary(projectId);
        assertEquals(3, number(comparison.get("total")));
        assertEquals(2, number(comparison.get("completed")));
        assertEquals(1, number(comparison.get("selectedForArchive")));
        assertEquals(2, number(comparison.get("monitoringBaselines")));

        Map<String, Object> restoration = service.getRestorationSummary(projectId);
        assertEquals(4, number(restoration.get("total")));
        assertEquals(1, number(restoration.get("physical")));
        assertEquals(2, number(restoration.get("digital")));
        assertEquals(2, number(restoration.get("completed")));
        assertEquals(2, number(restoration.get("processing")));
        assertEquals(1, number(restoration.get("selectedForArchive")));
        assertEquals("推荐三维成果", restoration.get("recommended"));
    }

    private static int number(Object value) {
        return ((Number) value).intValue();
    }

    private static Map<String, Object> map(Object... values) {
        Map<String, Object> result = new HashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(String.valueOf(values[index]), values[index + 1]);
        }
        return result;
    }
}
