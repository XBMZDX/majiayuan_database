package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ArchiveService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class ArchiveController {
    private final ArchiveService service;

    public ArchiveController(ArchiveService service) {
        this.service = service;
    }

    @GetMapping("/projects/{projectId}/archive")
    public Result<Map<String, Object>> workbench(@PathVariable Long projectId) {
        return Result.success(service.getWorkbench(projectId));
    }

    @PostMapping("/projects/{projectId}/archive")
    public Result<Map<String, Object>> create(@PathVariable Long projectId,
                                              @RequestBody(required = false) Map<String, Object> data) {
        return Result.success(service.create(projectId, data == null ? Map.of() : data));
    }

    @GetMapping("/projects/{projectId}/detection-candidates")
    public Result<List<Map<String, Object>>> detectionCandidates(@PathVariable Long projectId) {
        return Result.success(service.getDetectionCandidates(projectId));
    }

    @PutMapping("/archives/{archiveId}")
    public Result<Map<String, Object>> save(@PathVariable Long archiveId, @RequestBody Map<String, Object> data) {
        return Result.success(service.save(archiveId, data));
    }

    @PostMapping("/archives/{archiveId}/finalize")
    public Result<Map<String, Object>> finalizeArchive(@PathVariable Long archiveId,
                                                       @RequestBody Map<String, Object> data) {
        return Result.success(service.finalizeArchive(archiveId, data));
    }

    @PostMapping("/archives/{archiveId}/revision")
    public Result<Map<String, Object>> revision(@PathVariable Long archiveId, @RequestBody Map<String, Object> data) {
        return Result.success(service.createRevision(archiveId, data));
    }

    @GetMapping("/archives/{archiveId}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long archiveId,
                                         @RequestParam(defaultValue = "docx") String format) {
        Map<String, Object> file = service.exportArchive(archiveId, format);
        String name = URLEncoder.encode(String.valueOf(file.get("fileName")), StandardCharsets.UTF_8)
            .replace("+", "%20");
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(String.valueOf(file.get("contentType"))))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + name)
            .body((byte[]) file.get("fileData"));
    }

    @PostMapping(value = "/archives/{archiveId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> upload(@PathVariable Long archiveId,
                                              @RequestPart("file") MultipartFile file,
                                              @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadAttachment(archiveId, file, metadata));
    }

    @GetMapping("/archive-attachments/{attachmentId}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long attachmentId) {
        Map<String, Object> attachment = service.getAttachment(attachmentId);
        if (attachment == null) return ResponseEntity.notFound().build();
        String name = URLEncoder.encode(String.valueOf(attachment.get("fileName")), StandardCharsets.UTF_8)
            .replace("+", "%20");
        MediaType type;
        try {
            type = MediaType.parseMediaType(String.valueOf(attachment.get("contentType")));
        } catch (Exception ignored) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
            .contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + name)
            .body((byte[]) attachment.get("fileData"));
    }

    @DeleteMapping("/archive-attachments/{attachmentId}")
    public Result<Void> delete(@PathVariable Long attachmentId) {
        service.deleteAttachment(attachmentId);
        return Result.success();
    }
}
