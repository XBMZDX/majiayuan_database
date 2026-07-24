package com.itheima.bigevent.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 兼容已有本地数据库：保护修复模块的新文件改存 OSS，MySQL 仅保存链接和元数据。
 * 旧 file_data 保留为可空字段，供历史文件在迁移完成前继续读取。
 */
@Component
public class ConservationOssSchemaInitializer {
    private static final List<String> MEDIA_TABLES = List.of(
        "conservation_disease_media",
        "conservation_archive_attachment",
        "conservation_process_media",
        "conservation_comparison_media",
        "conservation_restoration_media",
        "conservation_restoration_model",
        "monitoring_media"
    );

    private final JdbcTemplate jdbc;

    public ConservationOssSchemaInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void ensureColumns() {
        for (String table : MEDIA_TABLES) {
            if (!tableExists(table)) continue;
            addColumnIfMissing(table, "file_url", "VARCHAR(1000) NULL");
            addColumnIfMissing(table, "oss_object_key", "VARCHAR(600) NULL");
            jdbc.execute("ALTER TABLE " + table + " MODIFY file_data LONGBLOB NULL");
        }
    }

    private boolean tableExists(String table) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?
            """, Integer.class, table);
        return count != null && count > 0;
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?
            """, Integer.class, table, column);
        if (count == null || count == 0) {
            jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
