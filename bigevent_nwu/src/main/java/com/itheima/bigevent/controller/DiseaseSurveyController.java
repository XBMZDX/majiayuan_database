package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.DiseaseSurveyService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conservation")
public class DiseaseSurveyController {
    private final DiseaseSurveyService service;

    public DiseaseSurveyController(DiseaseSurveyService service) {
        this.service = service;
    }

    @GetMapping("/projects/{projectId}/disease-workbench")
    public Result<Map<String, Object>> workbench(@PathVariable Long projectId) {
        return Result.success(service.getWorkbench(projectId));
    }

    @PutMapping("/projects/{projectId}/disease-workbench")
    public Result<Map<String, Object>> saveWorkbench(
        @PathVariable Long projectId,
        @RequestBody Map<String, Object> body
    ) {
        @SuppressWarnings("unchecked")
        Map<String, Object> survey = (Map<String, Object>) body.getOrDefault("survey", Map.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records =
            (List<Map<String, Object>>) body.getOrDefault("records", List.of());
        boolean submit = Boolean.TRUE.equals(body.get("submit"));
        return Result.success(service.saveWorkbench(projectId, survey, records, submit));
    }

    @PostMapping(value = "/disease-records/{recordId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadMedia(
        @PathVariable Long recordId,
        @RequestPart("file") MultipartFile file,
        @RequestParam Map<String, String> metadata
    ) {
        return Result.success(service.uploadMedia(recordId, file, metadata));
    }

    @GetMapping("/disease-media/{mediaId}/content")
    public ResponseEntity<byte[]> mediaContent(@PathVariable Long mediaId) {
        Map<String, Object> media = service.getMedia(mediaId);
        if (media == null || media.get("fileData") == null) return ResponseEntity.notFound().build();
        String name = URLEncoder.encode(String.valueOf(media.get("fileName")), StandardCharsets.UTF_8)
            .replace("+", "%20");
        MediaType type;
        try { type = MediaType.parseMediaType(String.valueOf(media.get("contentType"))); }
        catch (Exception ignored) { type = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok()
            .contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + name)
            .body((byte[]) media.get("fileData"));
    }

    @PutMapping("/disease-media/{mediaId}/annotations")
    public Result<Map<String, Object>> saveAnnotations(
        @PathVariable Long mediaId,
        @RequestBody Map<String, List<Map<String, Object>>> body
    ) {
        return Result.success(service.saveAnnotations(
            mediaId, body.getOrDefault("annotations", List.of())
        ));
    }

    @DeleteMapping("/disease-media/{mediaId}")
    public Result<Void> deleteMedia(@PathVariable Long mediaId) {
        service.deleteMedia(mediaId);
        return Result.success();
    }
}
