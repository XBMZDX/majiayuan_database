package com.itheima.bigevent.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface MonitoringService {
    List<Map<String, Object>> getProjects();
    Map<String, Object> getProject(Long projectId);
    Map<String, Object> createProject(Map<String, Object> data);
    void updateProject(Long projectId, Map<String, Object> data);
    void deleteProject(Long projectId);
    List<Map<String, Object>> searchArtifacts(String keyword);
    Map<String, Object> getSources(Long projectId);
    List<Map<String, Object>> getPlans(Long projectId);
    Map<String, Object> getStatistics(Long projectId);
    Map<String, Object> getComparisonSummary(Long projectId);
    Map<String, Object> getRestorationSummary(Long projectId);
    void saveWorkbench(Long projectId, List<Map<String, Object>> plans);
    void changeTaskStatus(Long taskId, String status);
    void updateAlert(Long alertId, Map<String, Object> data);
    Map<String, Object> createProjectFromAlert(Long alertId, Map<String, Object> data);
    Map<String, Object> uploadMedia(Long recordId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getMedia(Long mediaId);
    Map<String, Object> getSourceMedia(String sourceType, Long mediaId);
    void deleteMedia(Long mediaId);
}
