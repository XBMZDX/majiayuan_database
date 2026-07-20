package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ComparisonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class ComparisonController {
    private final ComparisonService service;

    public ComparisonController(ComparisonService service) {
        this.service = service;
    }

    @GetMapping("/projects/{projectId}/comparison-workbench")
    public Result<Map<String, Object>> workbench(@PathVariable Long projectId) {
        return Result.success(service.getWorkbench(projectId));
    }

    @PutMapping("/projects/{projectId}/comparison-workbench")
    public Result<Map<String, Object>> save(@PathVariable Long projectId,
                                             @RequestBody Map<String, List<Map<String, Object>>> body) {
        return Result.success(service.saveWorkbench(projectId, body.getOrDefault("groups", List.of())));
    }
}
