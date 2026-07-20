package com.itheima.bigevent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RestorationService {
    Map<String, Object> getWorkbench(Long projectId);
    Map<String, Object> saveWorkbench(Long projectId, List<Map<String, Object>> results);
    void deleteResult(Long resultId);
    Map<String, Object> uploadMedia(Long resultId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getMedia(Long mediaId);
    void deleteMedia(Long mediaId);
    Map<String, Object> uploadModel(Long resultId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getModel(Long modelId);
    void deleteModel(Long modelId);
    Map<String, Object> getVersion(Long resultId, Long versionId);
    Map<String, Object> restoreVersion(Long resultId, Long versionId);
}
