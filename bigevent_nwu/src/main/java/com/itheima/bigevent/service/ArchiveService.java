package com.itheima.bigevent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ArchiveService {
    Map<String, Object> getWorkbench(Long projectId);
    Map<String, Object> create(Long projectId, Map<String, Object> data);
    java.util.List<Map<String, Object>> getDetectionCandidates(Long projectId);
    Map<String, Object> save(Long archiveId, Map<String, Object> data);
    Map<String, Object> finalizeArchive(Long archiveId, Map<String, Object> data);
    Map<String, Object> createRevision(Long archiveId, Map<String, Object> data);
    Map<String, Object> exportArchive(Long archiveId, String format);
    Map<String, Object> uploadAttachment(Long archiveId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getAttachment(Long attachmentId);
    void deleteAttachment(Long attachmentId);
}
