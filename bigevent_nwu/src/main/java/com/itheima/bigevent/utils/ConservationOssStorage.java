package com.itheima.bigevent.utils;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** 保护修复模块统一的 OSS 文件存储入口。 */
public final class ConservationOssStorage {
    private ConservationOssStorage() { }

    public static Map<String, String> upload(String module, MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        try (InputStream input = file.getInputStream()) {
            return upload(module, originalName, input);
        }
    }

    public static Map<String, String> upload(String module, String originalName, InputStream input) throws Exception {
        originalName = originalName == null || originalName.isBlank() ? "file" : originalName;
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) extension = originalName.substring(dot);
        String objectKey = "conservation/" + module + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;
        String fileUrl = AliOssUtil.uploadFile(objectKey, input);
        if (fileUrl == null || fileUrl.isBlank()) throw new IllegalStateException("OSS 文件上传失败");
        Map<String, String> result = new LinkedHashMap<>();
        result.put("objectKey", objectKey);
        result.put("fileUrl", fileUrl);
        return result;
    }

    public static void delete(String objectKey) throws Exception {
        if (objectKey == null || objectKey.isBlank()) return;
        if (!objectKey.startsWith("conservation/")) {
            throw new IllegalArgumentException("拒绝删除非保护修复模块的 OSS 文件");
        }
        AliOssUtil.deleteFile(objectKey);
    }
}
