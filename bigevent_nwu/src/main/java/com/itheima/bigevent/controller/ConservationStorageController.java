package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ConservationOssMigrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 本机运维接口：仅在确认 OSS 配置正确后手动执行历史文件迁移。 */
@RestController
@RequestMapping("/conservation/storage")
public class ConservationStorageController {
    private final ConservationOssMigrationService migrationService;

    public ConservationStorageController(ConservationOssMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/migrate-legacy-media")
    public Result<Map<String, Object>> migrateLegacyMedia(
        @RequestParam(defaultValue = "true") boolean clearLegacyData
    ) {
        return Result.success(migrationService.migrateLegacyMedia(clearLegacyData));
    }
}
