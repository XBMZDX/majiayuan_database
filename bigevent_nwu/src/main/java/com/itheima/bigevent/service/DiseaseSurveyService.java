package com.itheima.bigevent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DiseaseSurveyService {
    Map<String, Object> getWorkbench(Long projectId);
    Map<String, Object> saveWorkbench(Long projectId, Map<String, Object> survey,
                                      List<Map<String, Object>> records, boolean submit);
    Map<String, Object> uploadMedia(Long recordId, MultipartFile file, Map<String, String> metadata);
    Map<String, Object> getMedia(Long mediaId);
    Map<String, Object> saveAnnotations(Long mediaId, List<Map<String, Object>> annotations);
    void deleteMedia(Long mediaId);
}
