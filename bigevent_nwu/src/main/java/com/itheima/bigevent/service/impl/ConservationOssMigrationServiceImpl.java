package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.service.ConservationOssMigrationService;
import com.itheima.bigevent.utils.ConservationOssStorage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 显式调用，避免应用启动时自动批量上传历史文件。 */
@Service
public class ConservationOssMigrationServiceImpl implements ConservationOssMigrationService {
    private record TableSpec(String table, String module, String fileNameColumn) { }

    private static final List<TableSpec> TABLES = List.of(
        new TableSpec("conservation_disease_media", "disease-media", "file_name"),
        new TableSpec("conservation_archive_attachment", "archive-attachments", "file_name"),
        new TableSpec("conservation_process_media", "process-media", "original_name"),
        new TableSpec("conservation_comparison_media", "comparison-media", "original_name"),
        new TableSpec("conservation_restoration_media", "restoration-media", "original_name"),
        new TableSpec("conservation_restoration_model", "restoration-models", "original_name"),
        new TableSpec("monitoring_media", "monitoring-media", "original_name")
    );

    private final JdbcTemplate jdbc;

    public ConservationOssMigrationServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, Object> migrateLegacyMedia(boolean clearLegacyData) {
        Map<String, Object> result = new LinkedHashMap<>();
        int migrated = 0;
        for (TableSpec spec : TABLES) {
            if (!tableExists(spec.table())) continue;
            int count = 0;
            List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id,%s AS file_name,file_data
                FROM %s
                WHERE (file_url IS NULL OR file_url='') AND file_data IS NOT NULL
                """.formatted(spec.fileNameColumn(), spec.table()));
            for (Map<String, Object> row : rows) {
                byte[] bytes = (byte[]) row.get("file_data");
                if (bytes == null || bytes.length == 0) continue;
                try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
                    Map<String, String> stored = ConservationOssStorage.upload(
                        spec.module(), String.valueOf(row.get("file_name")), input);
                    jdbc.update("UPDATE " + spec.table()
                            + " SET file_url=?,oss_object_key=?,file_data=? WHERE id=?",
                        stored.get("fileUrl"), stored.get("objectKey"), clearLegacyData ? null : bytes, row.get("id"));
                    count++;
                    migrated++;
                } catch (Exception exception) {
                    throw new IllegalStateException("迁移 " + spec.table() + " 的文件 ID=" + row.get("id") + " 失败", exception);
                }
            }
            result.put(spec.table(), count);
        }
        result.put("migrated", migrated);
        result.put("legacyDataCleared", clearLegacyData);
        return result;
    }

    private boolean tableExists(String table) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?
            """, Integer.class, table);
        return count != null && count > 0;
    }
}
