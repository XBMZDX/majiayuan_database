package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/digital-archive/overview")
@CrossOrigin
public class ArchiveOverviewController {

    @Autowired private ArchiveOverviewMapper mapper;

    @GetMapping("/summary")
    public Result<Map<String,Object>> summary(
            @RequestParam(required = false) String sourceModule,
            @RequestParam(required = false) String resourceType) {
        Map<String,Object> p = new HashMap<>();
        if (sourceModule != null && !sourceModule.isEmpty()) p.put("sourceModule", sourceModule);
        if (resourceType != null && !resourceType.isEmpty()) p.put("resourceType", resourceType);

        Map<String,Object> r = new LinkedHashMap<>();
        r.put("totalResources", mapper.countTotal(p));
        r.put("imageCount", mapper.countByType(p, "image"));
        r.put("documentCount", mapper.countByTypes(p, Arrays.asList("document","report","spreadsheet")));
        r.put("reportCount", 0L);
        r.put("videoCount", mapper.countByTypes(p, Arrays.asList("video","audio")));
        r.put("modelCount", mapper.countByType(p, "model_3d"));
        r.put("relatedResourceCount", mapper.countRelated(p));
        r.put("incompleteResourceCount", mapper.countIncomplete(p));
        r.put("totalStorageBytes", Optional.ofNullable(mapper.sumStorage(p)).orElse(0L));
        return Result.success(r);
    }

    @GetMapping("/source-distribution")
    public Result<List<Map<String,Object>>> sourceDistribution() {
        return Result.success(mapper.sourceDistribution());
    }

    @GetMapping("/type-distribution")
    public Result<List<Map<String,Object>>> typeDistribution() {
        return Result.success(mapper.typeDistribution());
    }

    @GetMapping("/recent-resources")
    public Result<List<Map<String,Object>>> recentResources(
            @RequestParam(required = false, defaultValue = "6") int limit) {
        return Result.success(mapper.recentResources(limit));
    }

    @GetMapping("/issues")
    public Result<List<Map<String,Object>>> issues() {
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(issue("missing_relation","未关联业务对象","warning", mapper.countNoRelation()));
        list.add(issue("missing_metadata","缺少必要元数据","warning", mapper.countMissingMeta()));
        list.add(issue("duplicate","疑似重复资源","info", mapper.countDuplicates()));
        list.add(issue("preview_failed","预览失败","danger", mapper.countByStatus("preview_failed")));
        list.add(issue("damaged","文件损坏","danger", mapper.countByStatus("damaged")));
        return Result.success(list);
    }

    private Map<String,Object> issue(String type, String name, String level, Long count) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("issueType", type); m.put("issueName", name); m.put("issueLevel", level); m.put("issueCount", count != null ? count : 0);
        return m;
    }

    @Mapper interface ArchiveOverviewMapper {
        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 <if test='p.sourceModule!=null'> AND source_module=#{p.sourceModule}</if> <if test='p.resourceType!=null'> AND resource_type=#{p.resourceType}</if></script>")
        Long countTotal(@Param("p") Map<String,Object> p);

        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND resource_type=#{type} <if test='p.sourceModule!=null'> AND source_module=#{p.sourceModule}</if></script>")
        Long countByType(@Param("p") Map<String,Object> p, @Param("type") String type);

        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND resource_type IN <foreach collection='types' item='t' open='(' separator=',' close=')'>#{t}</foreach> <if test='p.sourceModule!=null'> AND source_module=#{p.sourceModule}</if></script>")
        Long countByTypes(@Param("p") Map<String,Object> p, @Param("types") List<String> types);

        @Select("SELECT COUNT(DISTINCT r.id) FROM digital_resource r INNER JOIN digital_resource_relation rel ON rel.resource_id=r.id WHERE r.deleted=0")
        Long countRelated(@Param("p") Map<String,Object> p);

        @Select("SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND (data_status IS NULL OR data_status!='complete' OR resource_name IS NULL OR TRIM(resource_name)='' OR source_module IS NULL OR TRIM(source_module)='')")
        Long countIncomplete(@Param("p") Map<String,Object> p);

        @Select("SELECT COALESCE(SUM(file_size),0) FROM digital_resource WHERE deleted=0")
        Long sumStorage(@Param("p") Map<String,Object> p);

        @Select("SELECT source_module AS sourceModule, COUNT(*) AS totalCount, COALESCE(SUM(CASE WHEN resource_type='image' THEN 1 ELSE 0 END),0) AS imageCount, COALESCE(SUM(CASE WHEN resource_type IN ('document','report','spreadsheet') THEN 1 ELSE 0 END),0) AS documentCount, COALESCE(SUM(CASE WHEN resource_type='model_3d' THEN 1 ELSE 0 END),0) AS modelCount, COALESCE(SUM(file_size),0) AS storageBytes FROM digital_resource WHERE deleted=0 GROUP BY source_module ORDER BY totalCount DESC")
        List<Map<String,Object>> sourceDistribution();

        @Select("SELECT resource_type AS resourceType, COUNT(*) AS resourceCount, COALESCE(SUM(file_size),0) AS storageBytes FROM digital_resource WHERE deleted=0 GROUP BY resource_type ORDER BY resourceCount DESC")
        List<Map<String,Object>> typeDistribution();

        @Select("SELECT r.id,r.resource_code AS resourceCode,r.resource_name AS resourceName, r.original_file_name AS originalFileName,r.resource_type AS resourceType, r.source_module AS sourceModule,r.file_size AS fileSize, r.data_status AS dataStatus,r.resource_status AS resourceStatus, r.uploaded_by AS uploadedBy,r.upload_time AS uploadTime, COALESCE(rel.relation_name,'') AS primaryObjName FROM digital_resource r LEFT JOIN digital_resource_relation rel ON rel.resource_id=r.id AND rel.is_primary=1 WHERE r.deleted=0 ORDER BY r.upload_time DESC,r.id DESC LIMIT #{limit}")
        List<Map<String,Object>> recentResources(int limit);

        @Select("SELECT COUNT(*) FROM digital_resource r WHERE r.deleted=0 AND NOT EXISTS (SELECT 1 FROM digital_resource_relation rel WHERE rel.resource_id=r.id)")
        Long countNoRelation();

        @Select("SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND (resource_name IS NULL OR TRIM(resource_name)='' OR resource_type IS NULL OR TRIM(resource_type)='' OR source_module IS NULL OR TRIM(source_module)='')")
        Long countMissingMeta();

        @Select("SELECT COUNT(*) FROM (SELECT file_hash FROM digital_resource WHERE deleted=0 AND file_hash IS NOT NULL AND file_hash!='' GROUP BY file_hash HAVING COUNT(*)>1) dup")
        Long countDuplicates();

        @Select("SELECT COUNT(*) FROM digital_resource WHERE deleted=0 AND resource_status=#{status}")
        Long countByStatus(String status);
    }
}
