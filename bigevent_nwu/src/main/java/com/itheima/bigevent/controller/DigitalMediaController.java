package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/digital-media")
@CrossOrigin
public class DigitalMediaController {

    @Autowired private MediaMapper mapper;

    // ========== 统计 ==========
    @GetMapping("/summary")
    public Result<Map<String,Object>> summary(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        Map<String,Object> p = new HashMap<>();
        if (objectType != null && !objectType.isEmpty()) p.put("objectType", objectType);
        if (objectId != null) p.put("objectId", objectId);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("totalCount", mapper.countMedia(p));
        r.put("imageCount", mapper.countByType(p, "image"));
        r.put("videoCount", mapper.countByType(p, "video"));
        r.put("audioCount", mapper.countByType(p, "audio"));
        r.put("keyMediaCount", mapper.countKey(p));
        r.put("incompleteCount", mapper.countIncomplete(p));
        r.put("collectionCount", mapper.countCollections(p));
        r.put("comparisonCount", mapper.countComparisons(p));
        return Result.success(r);
    }

    // ========== 查询 ==========
    @GetMapping
    public Result<Map<String,Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) String mediaStage,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId,
            @RequestParam(required = false) String shootingPart,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String,Object>> list = mapper.list(keyword, mediaType, mediaStage, objectType, objectId, shootingPart, offset, pageSize);
        long total = mapper.count(keyword, mediaType, mediaStage, objectType, objectId, shootingPart);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("records", list); r.put("total", total); r.put("pageNum", pageNum); r.put("pageSize", pageSize);
        return Result.success(r);
    }

    @GetMapping("/{resourceId}")
    public Result<Map<String,Object>> detail(@PathVariable Long resourceId) {
        Map<String,Object> d = mapper.mediaDetail(resourceId);
        return Result.success(d);
    }

    // ========== 元数据 ==========
    @GetMapping("/{resourceId}/metadata")
    public Result<Map<String,Object>> metadata(@PathVariable Long resourceId) {
        Map<String,Object> m = mapper.getMetadata(resourceId);
        if (m == null) {
            // Auto-init
            Map<String,Object> res = mapper.resourceBasic(resourceId);
            if (res != null) {
                String rt = res.get("resourceType") != null ? res.get("resourceType").toString() : "image";
                mapper.initMetadata(resourceId, rt);
                m = mapper.getMetadata(resourceId);
            }
        }
        return Result.success(m);
    }

    @PutMapping("/{resourceId}/metadata")
    public Result updateMetadata(@PathVariable Long resourceId, @RequestBody Map<String,Object> body) {
        mapper.upsertMetadata(resourceId, body); return Result.success();
    }

    // ========== 阶段分组 ==========
    @GetMapping("/stage-groups")
    public Result<List<Map<String,Object>>> stageGroups(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        return Result.success(mapper.stageGroups(objectType, objectId));
    }

    // ========== 时间轴 ==========
    @GetMapping("/timeline")
    public Result<List<Map<String,Object>>> timeline(
            @RequestParam String objectType,
            @RequestParam Long objectId) {
        return Result.success(mapper.timeline(objectType, objectId));
    }

    // ========== 集合 ==========
    @GetMapping("/collections")
    public Result<List<Map<String,Object>>> collections(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        return Result.success(mapper.collections(objectType, objectId));
    }

    @PostMapping("/collections")
    public Result createCollection(@RequestBody Map<String,Object> body) {
        String code = "MED-" + System.currentTimeMillis();
        String name = body.get("collectionName") != null ? body.get("collectionName").toString() : "";
        String type = body.get("collectionType") != null ? body.get("collectionType").toString() : "custom";
        mapper.insertCollection(code, name, type);
        return Result.success();
    }

    @DeleteMapping("/collections/{id}")
    public Result deleteCollection(@PathVariable Long id) {
        mapper.deleteCollection(id); return Result.success();
    }

    @PostMapping("/collections/{id}/items")
    public Result addCollectionItems(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rids = (List<Integer>) body.get("resourceIds");
        if (rids != null) for (Integer rid : rids) {
            try { mapper.insertCollectionItem(id, rid.longValue()); } catch(Exception e) {}
        }
        mapper.updateCollectionCount(id);
        return Result.success();
    }

    // ========== 对比 ==========
    @GetMapping("/comparisons")
    public Result<List<Map<String,Object>>> comparisons(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        return Result.success(mapper.comparisons(objectType, objectId));
    }

    @PostMapping("/comparisons")
    public Result createComparison(@RequestBody Map<String,Object> body) {
        String code = "CMP-" + System.currentTimeMillis();
        String name = body.get("comparisonName") != null ? body.get("comparisonName").toString() : "";
        Long beforeId = body.get("beforeResourceId") != null ? Long.parseLong(body.get("beforeResourceId").toString()) : 0L;
        Long afterId = body.get("afterResourceId") != null ? Long.parseLong(body.get("afterResourceId").toString()) : 0L;
        mapper.insertComparison(code, name, beforeId, afterId);
        return Result.success();
    }

    // ========== 标记 ==========
    @GetMapping("/{resourceId}/markers")
    public Result<List<Map<String,Object>>> markers(@PathVariable Long resourceId) {
        return Result.success(mapper.markers(resourceId));
    }

    @PostMapping("/{resourceId}/markers")
    public Result createMarker(@PathVariable Long resourceId, @RequestBody Map<String,Object> body) {
        String type = body.get("markerType") != null ? body.get("markerType").toString() : "";
        String name = body.get("markerName") != null ? body.get("markerName").toString() : "";
        double x = body.get("positionX") != null ? Double.parseDouble(body.get("positionX").toString()) : 0.5;
        double y = body.get("positionY") != null ? Double.parseDouble(body.get("positionY").toString()) : 0.5;
        mapper.insertMarker(resourceId, type, name, x, y);
        return Result.success();
    }

    // ========== 内联 Mapper ==========
    @Mapper interface MediaMapper {
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countMedia(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type=#{type} <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countByType(@Param("p") Map<String,Object> p, @Param("type") String type);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') AND m.is_key_media=1 <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countKey(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') AND m.metadata_status!='complete' <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countIncomplete(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_media_collection WHERE deleted=0 <if test='p.objectType!=null'> AND object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND object_id=#{p.objectId}</if></script>")
        Long countCollections(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_media_comparison WHERE deleted=0 <if test='p.objectType!=null'> AND object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND object_id=#{p.objectId}</if></script>")
        Long countComparisons(@Param("p") Map<String,Object> p);

        @Select("<script>SELECT r.id AS resourceId,r.resource_code AS resourceCode,r.resource_name AS resourceName,r.original_file_name AS originalFileName,r.resource_type AS mediaType,r.file_extension AS fileExtension,r.file_size AS fileSize,r.file_url AS fileUrl,COALESCE(NULLIF(r.thumbnail_url,''),CASE WHEN r.resource_type='image' THEN r.file_url ELSE NULL END) AS thumbnailUrl,COALESCE(NULLIF(r.preview_url,''),r.file_url) AS previewUrl,r.upload_time AS uploadTime,m.media_title AS mediaTitle,m.media_stage AS mediaStage,m.media_subtype AS mediaSubtype,m.primary_object_code AS primaryObjectCode,m.primary_object_name AS primaryObjectName,m.shooting_part AS shootingPart,m.shooting_date AS shootingDate,m.photographer,m.is_key_media AS keyMedia,m.metadata_status AS metadataStatus,m.quality_level AS qualityLevel,m.sort_time AS sortTime FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') <if test='keyword!=null and keyword!=\"\"'> AND (r.resource_name LIKE CONCAT('%',#{keyword},'%') OR r.original_file_name LIKE CONCAT('%',#{keyword},'%') OR m.media_title LIKE CONCAT('%',#{keyword},'%') OR m.primary_object_code LIKE CONCAT('%',#{keyword},'%') OR m.primary_object_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='mediaType!=null and mediaType!=\"\"'> AND r.resource_type=#{mediaType}</if> <if test='mediaStage!=null and mediaStage!=\"\"'> AND m.media_stage=#{mediaStage}</if> <if test='objectType!=null and objectType!=\"\"'> AND m.primary_object_type=#{objectType}</if> <if test='objectId!=null'> AND m.primary_object_id=#{objectId}</if> <if test='shootingPart!=null and shootingPart!=\"\"'> AND m.shooting_part=#{shootingPart}</if> ORDER BY m.sort_time DESC LIMIT #{offset},#{pageSize}</script>")
        List<Map<String,Object>> list(String keyword, String mediaType, String mediaStage, String objectType, Long objectId, String shootingPart, int offset, int pageSize);

        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') <if test='keyword!=null and keyword!=\"\"'> AND (r.resource_name LIKE CONCAT('%',#{keyword},'%') OR m.media_title LIKE CONCAT('%',#{keyword},'%') OR m.primary_object_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='mediaType!=null and mediaType!=\"\"'> AND r.resource_type=#{mediaType}</if> <if test='mediaStage!=null and mediaStage!=\"\"'> AND m.media_stage=#{mediaStage}</if> <if test='objectType!=null and objectType!=\"\"'> AND m.primary_object_type=#{objectType}</if> <if test='objectId!=null'> AND m.primary_object_id=#{objectId}</if> <if test='shootingPart!=null and shootingPart!=\"\"'> AND m.shooting_part=#{shootingPart}</if></script>")
        long count(String keyword, String mediaType, String mediaStage, String objectType, Long objectId, String shootingPart);

        @Select("SELECT r.id AS resourceId,r.resource_code AS resourceCode,r.resource_name AS resourceName,r.original_file_name AS originalFileName,r.resource_type AS mediaType,r.file_extension AS fileExtension,r.file_size AS fileSize,r.file_url AS fileUrl,COALESCE(NULLIF(r.thumbnail_url,''),CASE WHEN r.resource_type='image' THEN r.file_url ELSE NULL END) AS thumbnailUrl,COALESCE(NULLIF(r.preview_url,''),r.file_url) AS previewUrl,r.upload_time AS uploadTime,m.media_title AS mediaTitle,m.media_stage AS mediaStage,m.media_subtype AS mediaSubtype,m.primary_object_code AS primaryObjectCode,m.primary_object_name AS primaryObjectName,m.shooting_part AS shootingPart,m.shooting_date AS shootingDate,m.photographer,m.is_key_media AS keyMedia,m.metadata_status AS metadataStatus,m.quality_level AS qualityLevel,m.sort_time AS sortTime FROM digital_resource r LEFT JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.id=#{id}")
        Map<String,Object> mediaDetail(Long id);

        @Select("SELECT * FROM digital_media_metadata WHERE resource_id=#{id}")
        Map<String,Object> getMetadata(Long id);
        @Select("SELECT resource_type AS resourceType FROM digital_resource WHERE id=#{id}")
        Map<String,Object> resourceBasic(Long id);
        @Insert("INSERT INTO digital_media_metadata (resource_id,media_type,metadata_status) VALUES (#{rid},#{type},'incomplete')")
        void initMetadata(Long rid, String type);
        @Update("<script>UPDATE digital_media_metadata SET media_title=COALESCE(#{p.mediaTitle},media_title), media_stage=COALESCE(#{p.mediaStage},media_stage), media_subtype=COALESCE(#{p.mediaSubtype},media_subtype), primary_object_type=COALESCE(#{p.primaryObjectType},primary_object_type), primary_object_id=COALESCE(#{p.primaryObjectId},primary_object_id), primary_object_code=COALESCE(#{p.primaryObjectCode},primary_object_code), primary_object_name=COALESCE(#{p.primaryObjectName},primary_object_name), shooting_part=COALESCE(#{p.shootingPart},shooting_part), shooting_angle=COALESCE(#{p.shootingAngle},shooting_angle), shooting_date=COALESCE(#{p.shootingDate},shooting_date), photographer=COALESCE(#{p.photographer},photographer), has_scale=COALESCE(#{p.hasScale},has_scale), scale_unit=COALESCE(#{p.scaleUnit},scale_unit), quality_level=COALESCE(#{p.qualityLevel},quality_level), is_key_media=COALESCE(#{p.isKeyMedia},is_key_media), media_description=COALESCE(#{p.mediaDescription},media_description) WHERE resource_id=#{rid}</script>")
        void upsertMetadata(@Param("rid") Long rid, @Param("p") Map<String,Object> body);

        @Select("<script>SELECT m.media_stage AS mediaStage, COUNT(*) AS totalCount, SUM(CASE WHEN r.resource_type='image' THEN 1 ELSE 0 END) AS imageCount FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') <if test='objectType!=null'> AND m.primary_object_type=#{objectType}</if> <if test='objectId!=null'> AND m.primary_object_id=#{objectId}</if> GROUP BY m.media_stage ORDER BY totalCount DESC</script>")
        List<Map<String,Object>> stageGroups(String objectType, Long objectId);

        @Select("SELECT r.id AS resourceId,r.resource_name AS resourceName,m.media_title AS mediaTitle,m.media_stage AS mediaStage,r.resource_type AS mediaType,COALESCE(NULLIF(r.thumbnail_url,''),CASE WHEN r.resource_type='image' THEN r.file_url ELSE NULL END) AS thumbnailUrl,m.shooting_part AS shootingPart,m.shooting_date AS shootingDate,m.sort_time AS sortTime FROM digital_resource r INNER JOIN digital_media_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type IN ('image','video','audio') AND m.primary_object_type=#{objectType} AND m.primary_object_id=#{objectId} ORDER BY m.sort_time ASC")
        List<Map<String,Object>> timeline(String objectType, Long objectId);

        @Select("<script>SELECT * FROM digital_media_collection WHERE deleted=0 <if test='objectType!=null'> AND object_type=#{objectType}</if> <if test='objectId!=null'> AND object_id=#{objectId}</if> ORDER BY update_time DESC</script>")
        List<Map<String,Object>> collections(String objectType, Long objectId);
        @Insert("INSERT INTO digital_media_collection (collection_code,collection_name,collection_type) VALUES (#{code},#{name},#{type})")
        void insertCollection(String code, String name, String type);
        @Update("UPDATE digital_media_collection SET deleted=1 WHERE id=#{id}")
        void deleteCollection(Long id);
        @Insert("INSERT IGNORE INTO digital_media_collection_item (collection_id,resource_id) VALUES (#{cid},#{rid})")
        void insertCollectionItem(Long cid, Long rid);
        @Update("UPDATE digital_media_collection SET item_count=(SELECT COUNT(*) FROM digital_media_collection_item WHERE collection_id=#{id}) WHERE id=#{id}")
        void updateCollectionCount(Long id);

        @Select("<script>SELECT c.*, r1.thumbnail_url AS beforeThumbnail, r2.thumbnail_url AS afterThumbnail FROM digital_media_comparison c LEFT JOIN digital_resource r1 ON r1.id=c.before_resource_id LEFT JOIN digital_resource r2 ON r2.id=c.after_resource_id WHERE c.deleted=0 <if test='objectType!=null'> AND c.object_type=#{objectType}</if> <if test='objectId!=null'> AND c.object_id=#{objectId}</if> ORDER BY c.create_time DESC</script>")
        List<Map<String,Object>> comparisons(String objectType, Long objectId);
        @Insert("INSERT INTO digital_media_comparison (comparison_code,comparison_name,before_resource_id,after_resource_id) VALUES (#{code},#{name},#{beforeId},#{afterId})")
        void insertComparison(String code, String name, Long beforeId, Long afterId);

        @Select("SELECT * FROM digital_media_marker WHERE resource_id=#{id} ORDER BY create_time")
        List<Map<String,Object>> markers(Long id);
        @Insert("INSERT INTO digital_media_marker (resource_id,marker_type,marker_name,position_x,position_y) VALUES (#{rid},#{type},#{name},#{x},#{y})")
        void insertMarker(Long rid, String type, String name, double x, double y);
    }
}
