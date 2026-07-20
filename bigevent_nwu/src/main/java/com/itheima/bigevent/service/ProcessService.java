package com.itheima.bigevent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ProcessService {
    Map<String, Object> getWorkbench(Long projectId);
    Map<String, Object> create(Long projectId);
    Map<String, Object> save(Long processId, Map<String, Object> body);
    Map<String, Object> regenerateSteps(Long processId);
    Map<String, Object> getSummary(Long projectId);
    Map<String, Object> uploadMedia(Long stepId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getMedia(Long mediaId);
    void deleteMedia(Long mediaId);
}
