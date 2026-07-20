package com.itheima.bigevent.service;

import java.util.List;
import java.util.Map;

public interface ComparisonService {
    Map<String, Object> getWorkbench(Long projectId);
    Map<String, Object> saveWorkbench(Long projectId, List<Map<String, Object>> groups);
}
