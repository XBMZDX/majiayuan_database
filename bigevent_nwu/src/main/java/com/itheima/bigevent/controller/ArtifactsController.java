package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.mapper.ArtifactImageMapper;
import com.itheima.bigevent.service.ArtifactsService;
import com.itheima.bigevent.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/artifacts")
@CrossOrigin
public class ArtifactsController {

    @Autowired
    private ArtifactsService artifactsService;
    @Autowired
    private ArtifactImageMapper artifactImageMapper;

    // 添加文物
    @PostMapping
    public Result add(@RequestBody artifacts artifact) {
        artifactsService.add(artifact);
        return Result.success();
    }

    // 根据ID删除文物
    @DeleteMapping
    public Result deleteById(Integer id) {
        artifactsService.deleteById(id);
        return Result.success();
    }

    // 批量导入文物
    @PostMapping("/batch")
    public Result batchImport(@RequestBody List<artifacts> list) {
        for (artifacts a : list) artifactsService.add(a);
        return Result.success();
    }

    // 批量删除文物
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        artifactsService.batchDelete(ids);
        return Result.success();
    }

    // 修改文物信息
    @PutMapping
    public Result update(@RequestBody artifacts artifact) {
        artifactsService.update(artifact);
        return Result.success();
    }

    // 分页查询文物列表
    @GetMapping
    public Result<PageBean<artifacts>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String newArtifactName,
            @RequestParam(required = false) String newArtifactCode,
            @RequestParam(required = false) String material1,
            @RequestParam(required = false) String excavationRelic,
            @RequestParam(required = false) String completeness) {
        PageBean<artifacts> pageBean = artifactsService.list(pageNum, pageSize, newArtifactName, newArtifactCode, material1, excavationRelic, completeness);
        return Result.success(pageBean);
    }

    @GetMapping("/{artifactId}/images")
    public Result<List<Map<String, Object>>> images(@PathVariable Integer artifactId) {
        return Result.success(artifactImageMapper.listByArtifact(artifactId));
    }

    @PostMapping(value = "/{artifactId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadImage(@PathVariable Integer artifactId,
                                                   @RequestPart("file") MultipartFile file,
                                                   @RequestParam Map<String, String> metadata) throws Exception {
        if (file == null || file.isEmpty()) return Result.error("请先选择图片");
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot);
        String objectName = "artifact-images/" + artifactId + "/" + UUID.randomUUID() + ext;
        String url = AliOssUtil.uploadFile(objectName, file.getInputStream());
        boolean firstImage = artifactImageMapper.countByArtifact(artifactId) == 0;
        boolean cover = firstImage || "true".equalsIgnoreCase(metadata.getOrDefault("isCover", "false"));
        if (cover) artifactImageMapper.clearCover(artifactId);
        artifactImageMapper.insert(artifactId, url, originalName,
            metadata.getOrDefault("description", ""), artifactImageMapper.nextSort(artifactId), cover);
        Long id = artifactImageMapper.lastInsertId();
        if (cover) artifactImageMapper.updateArtifactCover(artifactId, url);
        return Result.success(artifactImageMapper.detail(id));
    }

    @PutMapping("/images/{imageId}")
    public Result updateImage(@PathVariable Long imageId, @RequestBody Map<String, Object> body) {
        artifactImageMapper.update(imageId, body);
        return Result.success();
    }

    @PostMapping("/images/{imageId}/cover")
    @Transactional(rollbackFor = Exception.class)
    public Result setImageCover(@PathVariable Long imageId) {
        Map<String, Object> image = artifactImageMapper.detail(imageId);
        if (image == null) return Result.error("图片不存在");
        Integer artifactId = ((Number) image.get("artifactId")).intValue();
        String url = String.valueOf(image.get("imageUrl"));
        artifactImageMapper.clearCover(artifactId);
        artifactImageMapper.setCover(imageId);
        artifactImageMapper.updateArtifactCover(artifactId, url);
        return Result.success();
    }

    @DeleteMapping("/images/{imageId}")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteImage(@PathVariable Long imageId) {
        Map<String, Object> image = artifactImageMapper.detail(imageId);
        if (image == null) return Result.success();
        Integer artifactId = ((Number) image.get("artifactId")).intValue();
        boolean wasCover = Boolean.TRUE.equals(image.get("isCover")) || "1".equals(String.valueOf(image.get("isCover")));
        artifactImageMapper.softDelete(imageId);
        if (wasCover) {
            Map<String, Object> next = artifactImageMapper.firstActive(artifactId);
            if (next != null) {
                Long nextId = ((Number) next.get("id")).longValue();
                String nextUrl = String.valueOf(next.get("imageUrl"));
                artifactImageMapper.clearCover(artifactId);
                artifactImageMapper.setCover(nextId);
                artifactImageMapper.updateArtifactCover(artifactId, nextUrl);
            } else {
                artifactImageMapper.updateArtifactCover(artifactId, "");
            }
        }
        return Result.success();
    }

}
