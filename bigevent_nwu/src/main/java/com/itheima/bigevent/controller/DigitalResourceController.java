package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/digital-resources")
@CrossOrigin
public class DigitalResourceController {

    @Autowired private ResourceMapper mapper;

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
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String,Object>> list = mapper.list(keyword, resourceType, sourceModule, resourceStatus, dataStatus, deleted, offset, pageSize);
        long total = mapper.count(keyword, resourceType, sourceModule, resourceStatus, dataStatus, deleted);
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

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        mapper.updateMeta(id, body); return Result.success();
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
        if (ids != null) for (Integer id : ids) { mapper.softDelete(id.longValue()); }
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

        @Select("<script>SELECT id,resource_code AS resourceCode,resource_name AS resourceName,original_file_name AS originalFileName,resource_type AS resourceType,source_module AS sourceModule,file_extension AS fileExtension,file_size AS fileSize,thumbnail_url AS thumbnailUrl,resource_status AS resourceStatus,data_status AS dataStatus,current_version AS currentVersion,version_count AS versionCount,uploaded_by AS uploadedBy,upload_time AS uploadTime,update_time AS updateTime FROM digital_resource WHERE deleted=#{deleted} <if test='keyword!=null and keyword!=\"\"'> AND (resource_code LIKE CONCAT('%',#{keyword},'%') OR resource_name LIKE CONCAT('%',#{keyword},'%') OR original_file_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='resourceType!=null and resourceType!=\"\"'> AND resource_type=#{resourceType}</if> <if test='sourceModule!=null and sourceModule!=\"\"'> AND source_module=#{sourceModule}</if> <if test='resourceStatus!=null and resourceStatus!=\"\"'> AND resource_status=#{resourceStatus}</if> <if test='dataStatus!=null and dataStatus!=\"\"'> AND data_status=#{dataStatus}</if> ORDER BY update_time DESC LIMIT #{offset},#{pageSize}</script>")
        List<Map<String,Object>> list(String keyword, String resourceType, String sourceModule, String resourceStatus, String dataStatus, boolean deleted, int offset, int pageSize);

        @Select("<script>SELECT COUNT(*) FROM digital_resource WHERE deleted=#{deleted} <if test='keyword!=null and keyword!=\"\"'> AND (resource_code LIKE CONCAT('%',#{keyword},'%') OR resource_name LIKE CONCAT('%',#{keyword},'%') OR original_file_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='resourceType!=null and resourceType!=\"\"'> AND resource_type=#{resourceType}</if> <if test='sourceModule!=null and sourceModule!=\"\"'> AND source_module=#{sourceModule}</if> <if test='resourceStatus!=null and resourceStatus!=\"\"'> AND resource_status=#{resourceStatus}</if> <if test='dataStatus!=null and dataStatus!=\"\"'> AND data_status=#{dataStatus}</if></script>")
        long count(String keyword, String resourceType, String sourceModule, String resourceStatus, String dataStatus, boolean deleted);

        @Select("SELECT * FROM digital_resource WHERE id=#{id}")
        Map<String,Object> detail(Long id);

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
    }
}
