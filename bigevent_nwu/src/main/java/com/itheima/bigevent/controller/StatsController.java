package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据 Controller — 返回各模块总数供首页看板使用
 */
@RestController
@RequestMapping("/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping
    public Result<Map<String, Long>> getStats() {
        Map<String, Long> map = new HashMap<>();
        map.put("artifactCount", statsMapper.countArtifacts());
        map.put("siteCount", statsMapper.countSites());
        map.put("relicCount", statsMapper.countRelics());
        map.put("detectionCount", statsMapper.countDetectedArtifacts());
        return Result.success(map);
    }

    /**
     * 内联 Mapper — 仅在 StatsController 中使用
     */
    @Mapper
    public interface StatsMapper {
        @Select("SELECT COUNT(*) FROM artifacts")
        Long countArtifacts();

        @Select("SELECT COUNT(*) FROM heritage_sites")
        Long countSites();

        @Select("SELECT COUNT(DISTINCT excavation_relic) FROM artifacts WHERE excavation_relic IS NOT NULL AND excavation_relic != ''")
        Long countRelics();

        @Select("""
                SELECT COUNT(DISTINCT a.id)
                FROM artifacts a
                WHERE EXISTS (
                    SELECT 1
                    FROM detection_analysis da
                    WHERE (
                        da.artifact_code IS NOT NULL
                        AND TRIM(da.artifact_code) != ''
                        AND (
                            (
                                a.new_artifact_code IS NOT NULL
                                AND TRIM(a.new_artifact_code) != ''
                                AND REPLACE(REPLACE(REPLACE(REPLACE(TRIM(da.artifact_code), '：', ':'), ' ', ''), '-', ''), '_', '') COLLATE utf8mb4_unicode_ci
                                    = REPLACE(REPLACE(REPLACE(REPLACE(TRIM(a.new_artifact_code), '：', ':'), ' ', ''), '-', ''), '_', '') COLLATE utf8mb4_unicode_ci
                            )
                            OR
                            (
                                a.original_artifact_code IS NOT NULL
                                AND TRIM(a.original_artifact_code) != ''
                                AND REPLACE(REPLACE(REPLACE(REPLACE(TRIM(da.artifact_code), '：', ':'), ' ', ''), '-', ''), '_', '') COLLATE utf8mb4_unicode_ci
                                    = REPLACE(REPLACE(REPLACE(REPLACE(TRIM(a.original_artifact_code), '：', ':'), ' ', ''), '-', ''), '_', '') COLLATE utf8mb4_unicode_ci
                            )
                        )
                    )
                    OR (
                        (da.artifact_code IS NULL OR TRIM(da.artifact_code) = '')
                        AND da.artifact_name IS NOT NULL
                        AND TRIM(da.artifact_name) != ''
                        AND (
                            TRIM(da.artifact_name) COLLATE utf8mb4_unicode_ci = TRIM(COALESCE(a.new_artifact_name, '')) COLLATE utf8mb4_unicode_ci
                            OR TRIM(da.artifact_name) COLLATE utf8mb4_unicode_ci = TRIM(COALESCE(a.original_artifact_name, '')) COLLATE utf8mb4_unicode_ci
                        )
                    )
                )
                """)
        Long countDetectedArtifacts();
    }
}
