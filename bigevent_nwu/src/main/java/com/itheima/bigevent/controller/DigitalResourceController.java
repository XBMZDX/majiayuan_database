package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.utils.AliOssUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.UUID;
import java.util.Locale;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
// Vue 开发代理会移除 /api 前缀，因此控制器路径保持项目内部的真实路径。
@RequestMapping("/digital-resources")
@CrossOrigin
public class DigitalResourceController {

    @Autowired private ResourceMapper mapper;
    @Autowired private JdbcTemplate jdbc;

    /**
     * init.sql 中的 CREATE TABLE IF NOT EXISTS 不会升级已存在的旧表。
     * 启动时补齐数字资源版本表的新增字段，确保旧数据库也能上传资源。
     */
    @PostConstruct
    public void ensureDigitalResourceVersionSchema() {
        if (!tableExists("digital_resource_version")) return;
        addColumnIfMissing("digital_resource_version", "version_type", "VARCHAR(30)");
        addColumnIfMissing("digital_resource_version", "original_file_name", "VARCHAR(255)");
        addColumnIfMissing("digital_resource_version", "file_extension", "VARCHAR(30)");
        addColumnIfMissing("digital_resource_version", "file_size", "BIGINT DEFAULT 0");
        addColumnIfMissing("digital_resource_version", "file_url", "VARCHAR(1000)");
        addColumnIfMissing("digital_resource_version", "content_type", "VARCHAR(100)");
        addColumnIfMissing("digital_resource_version", "version_status", "VARCHAR(30) DEFAULT 'current'");
        addColumnIfMissing("digital_resource_version", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?",
            Integer.class, tableName);
        return count != null && count > 0;
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? AND column_name=?",
            Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            jdbc.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    @GetMapping("/summary")
    public Result<Map<String,Object>> summary(
            @RequestParam(required = false) String sourceModule,
            @RequestParam(required = false) String resourceType) {
        Map<String,Object> p = new HashMap<>();
        if (sourceModule != null && !sourceModule.isEmpty()) p.put("sourceModule", sourceModule);
        if (resourceType != null && !resourceType.isEmpty()) p.put("resourceType", resourceType);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("totalResources", mapper.countAll(p));
        r.put("imageCount", mapper.countByType(p, "image"));
        r.put("documentCount", mapper.countByTypes(p, Arrays.asList("document","report","spreadsheet")));
        r.put("videoCount", mapper.countByTypes(p, Arrays.asList("video","audio")));
        r.put("modelCount", mapper.countByType(p, "model_3d"));
        r.put("relatedCount", mapper.countRelated(p));
        r.put("incompleteCount", mapper.countIncomplete(p));
        r.put("totalStorageBytes", Optional.ofNullable(mapper.sumStorage(p)).orElse(0L));
        return Result.success(r);
    }

    @GetMapping
    public Result<Map<String,Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String sourceModule,
            @RequestParam(required = false) String resourceStatus,
            @RequestParam(required = false) String dataStatus,
            @RequestParam(required = false, defaultValue = "false") boolean deleted,
            @RequestParam(required = false, defaultValue = "false") boolean documentOnly,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String,Object>> list = mapper.list(keyword, resourceType, sourceModule, resourceStatus, dataStatus, deleted, documentOnly, offset, pageSize);
        long total = mapper.count(keyword, resourceType, sourceModule, resourceStatus, dataStatus, deleted, documentOnly);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("records", list); r.put("total", total); r.put("pageNum", pageNum); r.put("pageSize", pageSize);
        return Result.success(r);
    }

    @GetMapping("/{id}")
    public Result<Map<String,Object>> detail(@PathVariable Long id) {
        Map<String,Object> d = mapper.detail(id);
        if (d != null) {
            d.put("relations", mapper.relations(id));
            d.put("versions", mapper.versions(id));
            d.put("tags", mapper.tags(id));
            d.put("logs", mapper.logs(id));
        }
        return Result.success(d);
    }

    /** 以附件形式转发 OSS 文件，确保浏览器执行下载而非仅预览。 */
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws Exception {
        Map<String, Object> file = mapper.downloadInfo(id);
        if (file == null || file.get("fileUrl") == null || file.get("fileUrl").toString().isBlank()) {
            throw new IllegalArgumentException("资源文件不存在或没有可下载地址");
        }
        String originalName = Optional.ofNullable(file.get("originalFileName"))
            .map(Object::toString).filter(name -> !name.isBlank()).orElse("download");
        URLConnection connection = new URL(file.get("fileUrl").toString()).openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(60_000);
        InputStream inputStream = connection.getInputStream();
        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
            .body(new InputStreamResource(inputStream));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        mapper.updateMeta(id, body); return Result.success();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String,Object>> create(
            @RequestPart("file") MultipartFile file,
            @RequestParam Map<String,String> metadata) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("请先选择要上传的文件");
        }
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        // 资源类型始终根据真实文件扩展名识别，不接受前端手工指定，避免文件类型与实际内容不一致。
        String resourceType = inferResourceType(originalName);
        String sourceModule = defaultText(metadata.get("sourceModule"), "manual");
        String resourceName = defaultText(metadata.get("resourceName"), baseName(originalName));
        String title = defaultText(metadata.get("title"), resourceName);
        String description = text(metadata.get("description"));
        String keywords = text(metadata.get("keywords"));
        String resourceStatus = defaultText(metadata.get("resourceStatus"), "normal");
        String dataStatus = defaultText(metadata.get("dataStatus"), "incomplete");
        String fileExtension = fileExtension(originalName);
        String resourceCode = "DR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        String objectName = "digital-resource/" + UUID.randomUUID() + fileExtension;
        String fileUrl = AliOssUtil.uploadFile(objectName, file.getInputStream());
        String thumbnailUrl = "image".equals(resourceType) ? fileUrl : null;
        String previewUrl = ("image".equals(resourceType) || "video".equals(resourceType) || "audio".equals(resourceType) || "model_3d".equals(resourceType))
            ? fileUrl : null;
        String uploadedBy = currentUserName();
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("resourceCode", resourceCode);
        row.put("resourceName", resourceName);
        row.put("title", title);
        row.put("originalFileName", originalName);
        row.put("resourceType", resourceType);
        row.put("sourceModule", sourceModule);
        row.put("fileExtension", fileExtension);
        row.put("fileSize", file.getSize());
        row.put("fileUrl", fileUrl);
        row.put("thumbnailUrl", thumbnailUrl);
        row.put("previewUrl", previewUrl);
        row.put("resourceStatus", resourceStatus);
        row.put("dataStatus", dataStatus);
        row.put("currentVersion", "V1.0");
        row.put("versionCount", 1);
        row.put("uploadedBy", uploadedBy);
        row.put("description", description);
        row.put("keywords", keywords);
        mapper.insertResource(row);
        Long id = mapper.lastInsertId();
        if (id != null) {
            if (Set.of("image", "video", "audio").contains(resourceType)) {
                mapper.insertMediaMetadata(id, resourceType);
            } else if ("model_3d".equals(resourceType)) {
                mapper.insertModelMetadata(id);
            }
            Map<String,Object> version = new LinkedHashMap<>();
            version.put("resourceId", id);
            version.put("versionNo", "V1.0");
            version.put("versionName", resourceName);
            version.put("versionType", "original");
            version.put("originalFileName", originalName);
            version.put("fileExtension", fileExtension);
            version.put("fileSize", file.getSize());
            version.put("fileUrl", fileUrl);
            version.put("contentType", file.getContentType());
            version.put("versionStatus", "current");
            mapper.insertVersion(version);
            mapper.logOp(id, "create", "新增资源");
            Map<String,Object> detail = mapper.detail(id);
            if (detail != null) {
                detail.put("relations", mapper.relations(id));
                detail.put("versions", mapper.versions(id));
                detail.put("tags", mapper.tags(id));
                detail.put("logs", mapper.logs(id));
                return Result.success(detail);
            }
        }
        return Result.success(row);
    }

    /**
     * Batch upload keeps one independent digital-resource record for every file.
     * The shared metadata only contains fields that are meaningful for all files,
     * such as keywords and description; names are derived from the source files.
     */
    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String,Object>> batchUpload(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam Map<String,String> metadata) {
        if (files == null || files.length == 0) {
            return Result.error("请先选择要上传的文件");
        }
        List<String> uploadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                Map<String,String> singleMetadata = new HashMap<>(metadata);
                singleMetadata.remove("resourceName");
                singleMetadata.remove("title");
                Result<Map<String,Object>> result = create(file, singleMetadata);
                if (result.getCode() != null && result.getCode() == 0) {
                    uploadedFiles.add(Optional.ofNullable(file.getOriginalFilename()).orElse("未命名文件"));
                } else {
                    failedFiles.add(Optional.ofNullable(file.getOriginalFilename()).orElse("未命名文件"));
                }
            } catch (Exception e) {
                failedFiles.add(Optional.ofNullable(file.getOriginalFilename()).orElse("未命名文件"));
            }
        }
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("successCount", uploadedFiles.size());
        data.put("failedCount", failedFiles.size());
        data.put("uploadedFiles", uploadedFiles);
        data.put("failedFiles", failedFiles);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        mapper.softDelete(id); mapper.logOp(id, "delete", "移入回收站"); return Result.success();
    }

    @PostMapping("/{id}/restore")
    public Result restore(@PathVariable Long id) {
        mapper.restore(id); mapper.logOp(id, "restore", "从回收站恢复"); return Result.success();
    }

    @DeleteMapping("/{id}/permanent")
    public Result permanentDelete(@PathVariable Long id) {
        // Check impact
        long relCount = mapper.countRel(id);
        long verCount = mapper.countVer(id);
        if (relCount > 0) {
            Map<String,Object> r = new LinkedHashMap<>();
            r.put("canDelete", false); r.put("blockReason", "仍被" + relCount + "个业务对象引用"); r.put("relationCount", relCount); r.put("versionCount", verCount);
            return Result.success(r);
        }
        mapper.deleteVersions(id); mapper.deleteRelations(id); mapper.deleteTags(id); mapper.permanentDelete(id); mapper.logOp(id, "permanent_delete", "永久删除");
        return Result.success();
    }

    @GetMapping("/{id}/operations")
    public Result<List<Map<String,Object>>> operations(@PathVariable Long id) {
        return Result.success(mapper.logs(id));
    }

    @PostMapping("/{id}/relations")
    public Result addRelation(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        String rt = body.get("relationType") != null ? body.get("relationType").toString() : "";
        Long rid = body.get("relationId") != null ? Long.parseLong(body.get("relationId").toString()) : 0L;
        String rc = body.get("relationCode") != null ? body.get("relationCode").toString() : "";
        String rn = body.get("relationName") != null ? body.get("relationName").toString() : "";
        boolean primary = body.get("isPrimary") != null && Boolean.parseBoolean(body.get("isPrimary").toString());
        if (primary) mapper.clearPrimary(id);
        mapper.insertRelation(id, rt, rid, rc, rn, primary);
        mapper.logOp(id, "add_relation", "添加关联: " + rt + "-" + rid);
        return Result.success();
    }

    @DeleteMapping("/relations/{relationId}")
    public Result removeRelation(@PathVariable Long relationId) {
        mapper.deleteRelation(relationId); return Result.success();
    }

    @PostMapping("/relations/{relationId}/set-primary")
    public Result setPrimary(@PathVariable Long relationId) {
        Map<String,Object> rel = mapper.getRelation(relationId);
        if (rel != null) {
            Long resourceId = ((Number) rel.get("resourceId")).longValue();
            mapper.clearPrimary(resourceId);
            mapper.setPrimary(relationId);
        }
        return Result.success();
    }

    @GetMapping("/{id}/versions")
    public Result<List<Map<String,Object>>> versions(@PathVariable Long id) {
        return Result.success(mapper.versions(id));
    }

    @PostMapping("/{id}/tags")
    public Result setTags(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        mapper.deleteTags(id);
        @SuppressWarnings("unchecked")
        List<Integer> tagIds = (List<Integer>) body.get("tagIds");
        if (tagIds != null) {
            for (Integer tid : tagIds) mapper.insertTagRel(id, tid.longValue());
        }
        mapper.logOp(id, "update_tags", "更新标签");
        return Result.success();
    }

    @GetMapping("/tags")
    public Result<List<Map<String,Object>>> allTags() {
        return Result.success(mapper.allTags());
    }

    @PostMapping("/tags")
    public Result createTag(@RequestBody Map<String,Object> body) {
        String name = body.get("tagName") != null ? body.get("tagName").toString() : "";
        String cat = body.get("tagCategory") != null ? body.get("tagCategory").toString() : "";
        mapper.createTag(name, cat); return Result.success();
    }

    @PostMapping("/batch/tags")
    public Result batchTags(@RequestBody Map<String,Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("resourceIds");
        @SuppressWarnings("unchecked")
        List<Integer> tids = (List<Integer>) body.get("tagIds");
        if (ids != null && tids != null) {
            for (Integer rid : ids) {
                for (Integer tid : tids) {
                    try { mapper.insertTagRel(rid.longValue(), tid.longValue()); } catch(Exception e) {}
                }
            }
        }
        return Result.success();
    }

    @PostMapping("/batch/delete")
    public Result batchDelete(@RequestBody Map<String,Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("resourceIds");
        if (ids != null) {
            for (Integer id : ids) {
                if (id == null) continue;
                mapper.softDelete(id.longValue());
                mapper.logOp(id.longValue(), "batch_delete", "批量删除资源");
            }
        }
        return Result.success();
    }

    // ========== 内联 Mapper ==========
    @Mapper interface ResourceMapper {
        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 <if test='p.sourceModule!=null'> AND source_module=#{p.sourceModule}</if> <if test='p.resourceType!=null'> AND resource_type=#{p.resourceType}</if></script>")
        Long countAll(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND resource_type=#{type} <if test='p.sourceModule!=null'> AND source_module=#{p.sourceModule}</if></script>")
        Long countByType(@Param("p") Map<String,Object> p, @Param("type") String type);
        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND resource_type IN <foreach collection='types' item='t' open='(' separator=',' close=')'>#{t}</foreach></script>")
        Long countByTypes(@Param("p") Map<String,Object> p, @Param("types") List<String> types);
        @Select("SELECT COUNT(DISTINCT r.id) FROM digital_resource r INNER JOIN digital_resource_relation rr ON rr.resource_id=r.id WHERE r.deleted=0")
        Long countRelated(@Param("p") Map<String,Object> p);
        @Select("SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND (data_status IS NULL OR data_status!='complete')")
        Long countIncomplete(@Param("p") Map<String,Object> p);
        @Select("SELECT COALESCE(SUM(file_size),0) FROM digital_resource WHERE deleted=0")
        Long sumStorage(@Param("p") Map<String,Object> p);

        @Select("<script>SELECT id,resource_code AS resourceCode,resource_name AS resourceName,original_file_name AS originalFileName,resource_type AS resourceType,source_module AS sourceModule,file_extension AS fileExtension,file_size AS fileSize,COALESCE(NULLIF(thumbnail_url,''),CASE WHEN resource_type='image' THEN file_url ELSE NULL END) AS thumbnailUrl,resource_status AS resourceStatus,data_status AS dataStatus,current_version AS currentVersion,version_count AS versionCount,uploaded_by AS uploadedBy,upload_time AS uploadTime,update_time AS updateTime FROM digital_resource WHERE deleted=#{deleted} <if test='documentOnly'> AND resource_type IN ('document','report','spreadsheet')</if> <if test='keyword!=null and keyword!=\"\"'> AND (resource_code LIKE CONCAT('%',#{keyword},'%') OR resource_name LIKE CONCAT('%',#{keyword},'%') OR original_file_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='resourceType!=null and resourceType!=\"\"'> AND resource_type=#{resourceType}</if> <if test='sourceModule!=null and sourceModule!=\"\"'> AND source_module=#{sourceModule}</if> <if test='resourceStatus!=null and resourceStatus!=\"\"'> AND resource_status=#{resourceStatus}</if> <if test='dataStatus!=null and dataStatus!=\"\"'> AND data_status=#{dataStatus}</if> ORDER BY update_time DESC LIMIT #{offset},#{pageSize}</script>")
        List<Map<String,Object>> list(String keyword, String resourceType, String sourceModule, String resourceStatus, String dataStatus, boolean deleted, boolean documentOnly, int offset, int pageSize);

        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=#{deleted} <if test='documentOnly'> AND resource_type IN ('document','report','spreadsheet')</if> <if test='keyword!=null and keyword!=\"\"'> AND (resource_code LIKE CONCAT('%',#{keyword},'%') OR resource_name LIKE CONCAT('%',#{keyword},'%') OR original_file_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='resourceType!=null and resourceType!=\"\"'> AND resource_type=#{resourceType}</if> <if test='sourceModule!=null and sourceModule!=\"\"'> AND source_module=#{sourceModule}</if> <if test='resourceStatus!=null and resourceStatus!=\"\"'> AND resource_status=#{resourceStatus}</if> <if test='dataStatus!=null and dataStatus!=\"\"'> AND data_status=#{dataStatus}</if></script>")
        long count(String keyword, String resourceType, String sourceModule, String resourceStatus, String dataStatus, boolean deleted, boolean documentOnly);

        @Select("SELECT * FROM digital_resource WHERE id=#{id}")
        Map<String,Object> detail(Long id);

        @Select("SELECT original_file_name AS originalFileName,file_url AS fileUrl FROM digital_resource WHERE id=#{id} AND deleted=0")
        Map<String,Object> downloadInfo(Long id);

        @Insert("INSERT INTO digital_resource (resource_code,resource_name,title,original_file_name,resource_type,source_module,file_extension,file_size,file_url,thumbnail_url,preview_url,resource_status,data_status,current_version,version_count,uploaded_by,upload_time,update_time,description,keywords,deleted) VALUES (#{p.resourceCode},#{p.resourceName},#{p.title},#{p.originalFileName},#{p.resourceType},#{p.sourceModule},#{p.fileExtension},#{p.fileSize},#{p.fileUrl},#{p.thumbnailUrl},#{p.previewUrl},#{p.resourceStatus},#{p.dataStatus},#{p.currentVersion},#{p.versionCount},#{p.uploadedBy},NOW(),NOW(),#{p.description},#{p.keywords},0)")
        void insertResource(@Param("p") Map<String,Object> p);

        @Select("SELECT LAST_INSERT_ID()")
        Long lastInsertId();

        @Update("<script>UPDATE digital_resource SET resource_name=COALESCE(#{p.resourceName},resource_name), title=COALESCE(#{p.title},title), source_module=COALESCE(#{p.sourceModule},source_module), resource_type=COALESCE(#{p.resourceType},resource_type), description=COALESCE(#{p.description},description), keywords=COALESCE(#{p.keywords},keywords), data_status=COALESCE(#{p.dataStatus},data_status) WHERE id=#{id}</script>")
        void updateMeta(@Param("id") Long id, @Param("p") Map<String,Object> body);

        @Update("UPDATE digital_resource SET deleted=1, deleted_by='user', delete_time=NOW() WHERE id=#{id}")
        void softDelete(Long id);
        @Update("UPDATE digital_resource SET deleted=0, deleted_by=NULL, delete_time=NULL WHERE id=#{id}")
        void restore(Long id);
        @Delete("DELETE FROM digital_resource WHERE id=#{id}")
        void permanentDelete(Long id);

        @Select("SELECT * FROM digital_resource_relation WHERE resource_id=#{id}")
        List<Map<String,Object>> relations(Long id);
        @Insert("INSERT INTO digital_resource_relation (resource_id,relation_type,relation_id,relation_code,relation_name,is_primary) VALUES (#{rid},#{rt},#{ri},#{rc},#{rn},#{p})")
        void insertRelation(Long rid, String rt, Long ri, String rc, String rn, boolean p);
        @Update("UPDATE digital_resource_relation SET is_primary=0 WHERE resource_id=#{id}")
        void clearPrimary(Long id);
        @Update("UPDATE digital_resource_relation SET is_primary=1 WHERE id=#{id}")
        void setPrimary(Long id);
        @Select("SELECT * FROM digital_resource_relation WHERE id=#{id}")
        Map<String,Object> getRelation(Long id);
        @Delete("DELETE FROM digital_resource_relation WHERE id=#{id}")
        void deleteRelation(Long id);
        @Select("SELECT COUNT(*) FROM digital_resource_relation WHERE resource_id=#{id}")
        long countRel(Long id);
        @Delete("DELETE FROM digital_resource_relation WHERE resource_id=#{id}")
        void deleteRelations(Long id);

        @Select("SELECT * FROM digital_resource_version WHERE resource_id=#{id} ORDER BY create_time DESC")
        List<Map<String,Object>> versions(Long id);
        @Select("SELECT COUNT(*) FROM digital_resource_version WHERE resource_id=#{id}")
        long countVer(Long id);
        @Delete("DELETE FROM digital_resource_version WHERE resource_id=#{id}")
        void deleteVersions(Long id);

        @Insert("INSERT INTO digital_resource_version (resource_id,version_no,version_name,version_type,original_file_name,file_extension,file_size,file_url,content_type,version_status,create_time) VALUES (#{p.resourceId},#{p.versionNo},#{p.versionName},#{p.versionType},#{p.originalFileName},#{p.fileExtension},#{p.fileSize},#{p.fileUrl},#{p.contentType},#{p.versionStatus},NOW())")
        void insertVersion(@Param("p") Map<String,Object> p);

        @Select("SELECT t.id,t.tag_name AS tagName,t.tag_type AS tagCategory FROM digital_resource_tag t INNER JOIN digital_resource_tag_relation tr ON tr.tag_id=t.id WHERE tr.resource_id=#{id}")
        List<Map<String,Object>> tags(Long id);
        @Select("SELECT * FROM digital_resource_tag WHERE deleted=0 ORDER BY tag_type,tag_name")
        List<Map<String,Object>> allTags();
        @Insert("INSERT INTO digital_resource_tag (tag_name,tag_type) VALUES (#{name},#{cat})")
        void createTag(String name, String cat);
        @Delete("DELETE FROM digital_resource_tag_relation WHERE resource_id=#{id}")
        void deleteTags(Long id);
        @Insert("INSERT IGNORE INTO digital_resource_tag_relation (resource_id,tag_id) VALUES (#{rid},#{tid})")
        void insertTagRel(Long rid, Long tid);

        @Select("SELECT * FROM digital_resource_operation_log WHERE resource_id=#{id} ORDER BY operation_time DESC LIMIT 30")
        List<Map<String,Object>> logs(Long id);
        @Insert("INSERT INTO digital_resource_operation_log (resource_id,operation_type,operation_description) VALUES (#{rid},#{type},#{desc})")
        void logOp(Long rid, String type, String desc);

        @Insert("INSERT INTO digital_media_metadata (resource_id,media_type,metadata_status) VALUES (#{rid},#{type},'incomplete')")
        void insertMediaMetadata(Long rid, String type);

        @Insert("INSERT INTO digital_model_metadata (resource_id,model_type,model_stage,metadata_status) VALUES (#{rid},'scan','raw','incomplete')")
        void insertModelMetadata(Long rid);
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultText(String value, String fallback) {
        String v = text(value);
        return v.isEmpty() ? fallback : v;
    }

    private String baseName(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(0, idx) : fileName;
    }

    private String fileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx).toLowerCase(Locale.ROOT) : "";
    }

    private String inferResourceType(String fileName) {
        String ext = fileExtension(fileName).replace(".", "");
        if (Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tif", "tiff").contains(ext)) return "image";
        if (Set.of("mp4", "mov", "avi", "mkv", "flv", "webm").contains(ext)) return "video";
        if (Set.of("mp3", "wav", "aac", "flac", "ogg").contains(ext)) return "audio";
        if (Set.of("xls", "xlsx", "csv").contains(ext)) return "spreadsheet";
        if (Set.of("pdf", "doc", "docx", "ppt", "pptx", "txt", "rtf").contains(ext)) return "document";
        if (Set.of("obj", "fbx", "gltf", "glb", "stl", "ply", "3ds").contains(ext)) return "model_3d";
        return "other";
    }

    private String currentUserName() {
        try {
            Map<String,Object> user = ThreadLocalUtil.get();
            if (user != null) {
                Object nickname = user.get("nickname");
                if (nickname != null && !nickname.toString().trim().isEmpty()) return nickname.toString().trim();
                Object username = user.get("username");
                if (username != null && !username.toString().trim().isEmpty()) return username.toString().trim();
            }
        } catch (Exception ignored) {}
        return "system";
    }
}
