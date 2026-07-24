-- 保护、修复、复原媒体统一迁移：新文件保存在 OSS，MySQL 只保存 URL、对象键和元数据。
-- 旧 file_data 暂不删除，用于兼容已存在资料；确认全部迁移后可再单独清理。
ALTER TABLE conservation_disease_media ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE conservation_archive_attachment ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE conservation_process_media ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE conservation_comparison_media ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE conservation_restoration_media ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE conservation_restoration_model ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
ALTER TABLE monitoring_media ADD COLUMN IF NOT EXISTS file_url VARCHAR(1000) NULL, ADD COLUMN IF NOT EXISTS oss_object_key VARCHAR(600) NULL, MODIFY file_data LONGBLOB NULL;
