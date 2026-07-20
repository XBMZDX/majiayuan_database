package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.RestorationService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class RestorationController {
    private final RestorationService service;

    public RestorationController(RestorationService service) {
        this.service = service;
    }

    @GetMapping("/projects/{projectId}/restoration-workbench")
    public Result<Map<String, Object>> workbench(@PathVariable Long projectId) {
        return Result.success(service.getWorkbench(projectId));
    }

    @PutMapping("/projects/{projectId}/restoration-workbench")
    public Result<Map<String, Object>> save(@PathVariable Long projectId,
                                            @RequestBody Map<String, List<Map<String, Object>>> body) {
        return Result.success(service.saveWorkbench(projectId, body.getOrDefault("results", List.of())));
    }

    @DeleteMapping("/restoration-results/{resultId}")
    public Result<Void> deleteResult(@PathVariable Long resultId) {
        service.deleteResult(resultId);
        return Result.success();
    }

    @PostMapping(value = "/restoration-results/{resultId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadMedia(
        @PathVariable Long resultId, @RequestPart("file") MultipartFile file,
        @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadMedia(resultId, file, metadata));
    }

    @GetMapping("/restoration-media/{mediaId}/content")
    public ResponseEntity<byte[]> media(@PathVariable Long mediaId) {
        return binary(service.getMedia(mediaId));
    }

    @DeleteMapping("/restoration-media/{mediaId}")
    public Result<Void> deleteMedia(@PathVariable Long mediaId) {
        service.deleteMedia(mediaId);
        return Result.success();
    }

    @PostMapping(value = "/restoration-results/{resultId}/models", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadModel(
        @PathVariable Long resultId, @RequestPart("file") MultipartFile file,
        @RequestParam Map<String, String> metadata) {
        return Result.success(service.uploadModel(resultId, file, metadata));
    }

    @GetMapping("/restoration-models/{modelId}/content")
    public ResponseEntity<byte[]> model(@PathVariable Long modelId) {
        return binary(service.getModel(modelId));
    }

    @DeleteMapping("/restoration-models/{modelId}")
    public Result<Void> deleteModel(@PathVariable Long modelId) {
        service.deleteModel(modelId);
        return Result.success();
    }

    @GetMapping("/restoration-results/{resultId}/versions/{versionId}")
    public Result<Map<String, Object>> version(@PathVariable Long resultId, @PathVariable Long versionId) {
        return Result.success(service.getVersion(resultId, versionId));
    }

    @PostMapping("/restoration-results/{resultId}/versions/{versionId}/restore")
    public Result<Map<String, Object>> restoreVersion(@PathVariable Long resultId, @PathVariable Long versionId) {
        return Result.success(service.restoreVersion(resultId, versionId));
    }

    private ResponseEntity<byte[]> binary(Map<String, Object> file) {
        if (file == null || file.get("fileData") == null) return ResponseEntity.notFound().build();
        String name = URLEncoder.encode(String.valueOf(file.get("fileName")), StandardCharsets.UTF_8)
            .replace("+", "%20");
        MediaType type;
        try { type = MediaType.parseMediaType(String.valueOf(file.get("contentType"))); }
        catch (Exception e) { type = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + name)
            .body((byte[]) file.get("fileData"));
    }
}
