package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.MonitoringService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class MonitoringController {
    private final MonitoringService service;

    public MonitoringController(MonitoringService service) {
        this.service = service;
    }

    @GetMapping("/projects")
    public Result<List<Map<String, Object>>> projects() {
        return Result.success(service.getProjects());
    }

    @PostMapping("/projects")
    public Result<Map<String, Object>> createProject(@RequestBody Map<String, Object> data) {
        return Result.success(service.createProject(data));
    }

    @GetMapping("/projects/{projectId}")
    public Result<Map<String, Object>> project(@PathVariable Long projectId) {
        var project = service.getProject(projectId);
        return project == null ? Result.error("保护修复项目不存在") : Result.success(project);
    }

    @PutMapping("/projects/{projectId}")
    public Result<Void> updateProject(@PathVariable Long projectId, @RequestBody Map<String, Object> data) {
        service.updateProject(projectId, data);
        return Result.success();
    }

    @DeleteMapping("/projects/{projectId}")
    public Result<Void> deleteProject(@PathVariable Long projectId) {
        service.deleteProject(projectId);
        return Result.success();
    }

    @GetMapping("/projects/{projectId}/quick-record")
    public Result<Map<String, Object>> quickRecord(@PathVariable Long projectId) {
        return Result.success(service.getQuickRecord(projectId));
    }

    @PutMapping("/projects/{projectId}/quick-record")
    public Result<Map<String, Object>> saveQuickRecord(@PathVariable Long projectId,
                                                         @RequestBody Map<String, Object> data) {
        return Result.success(service.saveQuickRecord(projectId, data));
    }

    @PostMapping(value = "/projects/{projectId}/quick-record/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadQuickRecordMedia(
        @PathVariable Long projectId, @RequestPart("file") MultipartFile file,
        @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadQuickRecordMedia(projectId, file, metadata));
    }

    @DeleteMapping("/quick-record-media/{mediaId}")
    public Result<Void> deleteQuickRecordMedia(@PathVariable Long mediaId) {
        service.deleteQuickRecordMedia(mediaId);
        return Result.success();
    }

    @GetMapping("/artifacts/search")
    public Result<List<Map<String, Object>>> artifacts(@RequestParam(defaultValue = "") String keyword) {
        return Result.success(service.searchArtifacts(keyword));
    }

    @GetMapping("/projects/{projectId}/monitoring-sources")
    public Result<Map<String, Object>> sources(@PathVariable Long projectId) {
        return Result.success(service.getSources(projectId));
    }

    @GetMapping("/projects/{projectId}/monitoring-statistics")
    public Result<Map<String, Object>> statistics(@PathVariable Long projectId) {
        return Result.success(service.getStatistics(projectId));
    }

    @GetMapping("/projects/{projectId}/comparison-summary")
    public Result<Map<String, Object>> comparisonSummary(@PathVariable Long projectId) {
        return Result.success(service.getComparisonSummary(projectId));
    }

    @GetMapping("/projects/{projectId}/restoration-summary")
    public Result<Map<String, Object>> restorationSummary(@PathVariable Long projectId) {
        return Result.success(service.getRestorationSummary(projectId));
    }

    @GetMapping("/projects/{projectId}/monitoring-plans")
    public Result<List<Map<String, Object>>> plans(@PathVariable Long projectId) {
        return Result.success(service.getPlans(projectId));
    }

    @PutMapping("/projects/{projectId}/monitoring-workbench")
    public Result<List<Map<String, Object>>> saveWorkbench(
        @PathVariable Long projectId, @RequestBody Map<String, List<Map<String, Object>>> body) {
        service.saveWorkbench(projectId, body.getOrDefault("plans", List.of()));
        return Result.success(service.getPlans(projectId));
    }

    @PostMapping("/monitoring-tasks/{taskId}/{action}")
    public Result<Void> taskAction(@PathVariable Long taskId, @PathVariable String action) {
        String status = switch (action) {
            case "start" -> "in_progress";
            case "complete" -> "completed";
            case "cancel" -> "cancelled";
            default -> throw new IllegalArgumentException("不支持的任务操作");
        };
        service.changeTaskStatus(taskId, status);
        return Result.success();
    }

    @PostMapping("/monitoring-alerts/{alertId}/{action}")
    public Result<Void> alertAction(@PathVariable Long alertId, @PathVariable String action,
                                    @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> data = body == null ? new java.util.HashMap<>() : new java.util.HashMap<>(body);
        if (!data.containsKey("alertStatus")) {
            data.put("alertStatus", switch (action) {
                case "confirm" -> "confirmed";
                case "process" -> "processing";
                case "resolve" -> "resolved";
                case "close" -> "closed";
                case "false-alarm" -> "false_alarm";
                default -> throw new IllegalArgumentException("不支持的预警操作");
            });
        }
        service.updateAlert(alertId, data);
        return Result.success();
    }

    @PostMapping("/monitoring-alerts/{alertId}/create-project")
    public Result<Map<String, Object>> createProjectFromAlert(
        @PathVariable Long alertId,
        @RequestBody(required = false) Map<String, Object> body) {
        return Result.success(service.createProjectFromAlert(
            alertId, body == null ? Map.of() : body));
    }

    @PostMapping(value = "/monitoring-records/{recordId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadMedia(
        @PathVariable Long recordId, @RequestPart("file") MultipartFile file,
        @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadMedia(recordId, file, metadata));
    }

    @GetMapping("/monitoring-media/{mediaId}/content")
    public ResponseEntity<byte[]> media(@PathVariable Long mediaId) {
        return binary(service.getMedia(mediaId));
    }

    @GetMapping("/comparison-media/{mediaId}/content")
    public ResponseEntity<byte[]> comparisonMedia(@PathVariable Long mediaId) {
        return binary(service.getSourceMedia("comparison", mediaId));
    }

    private ResponseEntity<byte[]> binary(Map<String, Object> media) {
        if (media == null) return ResponseEntity.notFound().build();
        if (media.get("fileData") == null && media.get("fileUrl") != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, String.valueOf(media.get("fileUrl"))).build();
        }
        if (media.get("fileData") == null) return ResponseEntity.notFound().build();
        String name = java.net.URLEncoder.encode(String.valueOf(media.get("fileName")), StandardCharsets.UTF_8)
            .replace("+", "%20");
        MediaType type;
        try { type = MediaType.parseMediaType(String.valueOf(media.get("contentType"))); }
        catch (Exception e) { type = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok()
            .contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + name)
            .body((byte[]) media.get("fileData"));
    }

    @DeleteMapping("/monitoring-media/{mediaId}")
    public Result<Void> deleteMedia(@PathVariable Long mediaId) {
        service.deleteMedia(mediaId);
        return Result.success();
    }
}
