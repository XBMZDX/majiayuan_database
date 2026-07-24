package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ProcessService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class ProcessController {
    private final ProcessService service;

    public ProcessController(ProcessService service) {
        this.service = service;
    }

    @GetMapping("/projects/{projectId}/process-workbench")
    public Result<Map<String, Object>> workbench(@PathVariable Long projectId) {
        return Result.success(service.getWorkbench(projectId));
    }

    @PostMapping("/projects/{projectId}/processes")
    public Result<Map<String, Object>> create(@PathVariable Long projectId) {
        return Result.success(service.create(projectId));
    }

    @PutMapping("/processes/{processId}/workbench")
    public Result<Map<String, Object>> save(@PathVariable Long processId, @RequestBody Map<String, Object> body) {
        return Result.success(service.save(processId, body));
    }

    @PostMapping("/processes/{processId}/generate-steps")
    public Result<Map<String, Object>> regenerate(@PathVariable Long processId) {
        return Result.success(service.regenerateSteps(processId));
    }

    @GetMapping("/projects/{projectId}/process-summary")
    public Result<Map<String, Object>> summary(@PathVariable Long projectId) {
        return Result.success(service.getSummary(projectId));
    }

    @PostMapping(value = "/process-steps/{stepId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> upload(@PathVariable Long stepId, @RequestPart("file") MultipartFile file,
                                              @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadMedia(stepId, file, metadata));
    }

    @GetMapping("/process-media/{mediaId}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long mediaId) {
        Map<String, Object> media = service.getMedia(mediaId);
        if (media == null) return ResponseEntity.notFound().build();
        if (media.get("fileData") == null && media.get("fileUrl") != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, String.valueOf(media.get("fileUrl"))).build();
        }
        if (media.get("fileData") == null) return ResponseEntity.notFound().build();
        String name = URLEncoder.encode(String.valueOf(media.get("fileName")), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType type;
        try { type = MediaType.parseMediaType(String.valueOf(media.get("contentType"))); }
        catch (Exception ignored) { type = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + name)
            .body((byte[]) media.get("fileData"));
    }

    @DeleteMapping("/process-media/{mediaId}")
    public Result<Void> delete(@PathVariable Long mediaId) {
        service.deleteMedia(mediaId);
        return Result.success();
    }
}
