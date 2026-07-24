package com.itheima.bigevent.service;

import java.util.Map;

/** 将旧的 MySQL 二进制媒体迁移到 OSS。 */
public interface ConservationOssMigrationService {
    Map<String, Object> migrateLegacyMedia(boolean clearLegacyData);
}
