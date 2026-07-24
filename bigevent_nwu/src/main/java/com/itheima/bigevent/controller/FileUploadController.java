package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.utils.AliOssUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Shared upload endpoint for business modules that only need an OSS URL. */
@RestController
@CrossOrigin
public class FileUploadController {
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx",
            "txt", "csv", "zip", "rar", "mp4", "mp3", "wav", "obj", "fbx", "glb", "gltf"
    );

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return Result.error("请选择要上传的文件");
        if (file.getSize() > MAX_FILE_SIZE) return Result.error("单个文件不能超过 50MB");

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = originalName.lastIndexOf('.');
        String extension = dot < 0 ? "" : originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) return Result.error("不支持的文件格式");

        String objectName = "general-upload/" + UUID.randomUUID() + "." + extension;
        return Result.success(AliOssUtil.uploadFile(objectName, file.getInputStream()));
    }
}
