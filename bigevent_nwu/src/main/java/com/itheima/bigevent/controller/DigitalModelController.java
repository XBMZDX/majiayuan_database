package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/digital-models")
@CrossOrigin
public class DigitalModelController {

    @Autowired private ModelMapper mapper;

    @GetMapping("/summary")
    public Result<Map<String,Object>> summary(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        Map<String,Object> p = new HashMap<>();
        if (objectType != null) p.put("objectType", objectType);
        if (objectId != null) p.put("objectId", objectId);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("totalCount", mapper.countAll(p));
        r.put("scanCount", mapper.countByType(p, "scan"));
        r.put("pointCloudCount", mapper.countByType(p, "point_cloud"));
        r.put("restoredCount", mapper.countByType(p, "restored"));
        r.put("keyModelCount", mapper.countKey(p));
        r.put("incompleteCount", mapper.countIncomplete(p));
        r.put("collectionCount", mapper.countColl(p));
        return Result.success(r);
    }

    @GetMapping
    public Result<Map<String,Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String,Object>> list = mapper.list(keyword, modelType, objectType, objectId, offset, pageSize);
        long total = mapper.count(keyword, modelType, objectType, objectId);
        Map<String,Object> r = new LinkedHashMap<>(); r.put("records", list); r.put("total", total); r.put("pageNum", pageNum); r.put("pageSize", pageSize);
        return Result.success(r);
    }

    @GetMapping("/{resourceId}")
    public Result<Map<String,Object>> detail(@PathVariable Long resourceId) {
        return Result.success(mapper.detail(resourceId));
    }

    @GetMapping("/{resourceId}/metadata")
    public Result<Map<String,Object>> metadata(@PathVariable Long resourceId) {
        Map<String,Object> m = mapper.getMetadata(resourceId);
        if (m == null) {
            Map<String,Object> res = mapper.resourceBasic(resourceId);
            if (res != null && "model_3d".equals(res.get("resourceType"))) {
                mapper.initMetadata(resourceId);
                m = mapper.getMetadata(resourceId);
            }
        }
        return Result.success(m);
    }

    @PutMapping("/{resourceId}/metadata")
    public Result updateMetadata(@PathVariable Long resourceId, @RequestBody Map<String,Object> body) {
        mapper.upsertMetadata(resourceId, body); return Result.success();
    }

    @GetMapping("/collections")
    public Result<List<Map<String,Object>>> collections(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId) {
        return Result.success(mapper.collections(objectType, objectId));
    }

    @PostMapping("/collections")
    public Result createCollection(@RequestBody Map<String,Object> body) {
        String code = "MDL-" + System.currentTimeMillis();
        String name = body.get("collectionName") != null ? body.get("collectionName").toString() : "";
        String type = body.get("collectionType") != null ? body.get("collectionType").toString() : "custom";
        mapper.insertCollection(code, name, type); return Result.success();
    }

    @DeleteMapping("/collections/{id}")
    public Result deleteCollection(@PathVariable Long id) { mapper.deleteCollection(id); return Result.success(); }

    @PostMapping("/collections/{id}/items")
    public Result addItems(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rids = (List<Integer>) body.get("resourceIds");
        if (rids != null) for (Integer rid : rids) try { mapper.insertItem(id, rid.longValue()); } catch(Exception e) {}
        mapper.updateItemCount(id); return Result.success();
    }

    @Mapper interface ModelMapper {
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countAll(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' AND m.model_type=#{type} <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countByType(@Param("p") Map<String,Object> p, @Param("type") String type);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' AND m.is_key_model=1 <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countKey(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' AND m.metadata_status!='complete' <if test='p.objectType!=null'> AND m.primary_object_type=#{p.objectType}</if> <if test='p.objectId!=null'> AND m.primary_object_id=#{p.objectId}</if></script>")
        Long countIncomplete(@Param("p") Map<String,Object> p);
        @Select("<script>SELECT COUNT(*) FROM digital_model_collection WHERE deleted=0</script>")
        Long countColl(@Param("p") Map<String,Object> p);

        @Select("<script>SELECT r.id AS resourceId,r.resource_code AS resourceCode,r.resource_name AS resourceName,r.original_file_name AS originalFileName,r.file_extension AS fileExtension,r.file_size AS fileSize,r.thumbnail_url AS thumbnailUrl,r.preview_url AS previewUrl,r.upload_time AS uploadTime,m.model_title AS modelTitle,m.model_type AS modelType,m.model_stage AS modelStage,m.model_format AS modelFormat,m.primary_object_code AS primaryObjectCode,m.primary_object_name AS primaryObjectName,m.scale_unit AS scaleUnit,m.real_scale AS realScale,m.vertex_count AS vertexCount,m.face_count AS faceCount,m.point_count AS pointCount,m.texture_count AS textureCount,m.layer_count AS layerCount,m.acquisition_date AS acquisitionDate,m.quality_level AS qualityLevel,m.metadata_status AS metadataStatus,m.preview_status AS previewStatus,m.is_key_model AS keyModel FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' <if test='keyword!=null and keyword!=\"\"'> AND (r.resource_name LIKE CONCAT('%',#{keyword},'%') OR m.model_title LIKE CONCAT('%',#{keyword},'%') OR m.primary_object_name LIKE CONCAT('%',#{keyword},'%'))</if> <if test='modelType!=null and modelType!=\"\"'> AND m.model_type=#{modelType}</if> <if test='objectType!=null and objectType!=\"\"'> AND m.primary_object_type=#{objectType}</if> <if test='objectId!=null'> AND m.primary_object_id=#{objectId}</if> ORDER BY r.update_time DESC LIMIT #{offset},#{pageSize}</script>")
        List<Map<String,Object>> list(String keyword, String modelType, String objectType, Long objectId, int offset, int pageSize);

        @Select("<script>SELECT COUNT(*) FROM digital_resource r INNER JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.deleted=0 AND r.resource_type='model_3d' <if test='keyword!=null and keyword!=\"\"'> AND (r.resource_name LIKE CONCAT('%',#{keyword},'%') OR m.model_title LIKE CONCAT('%',#{keyword},'%'))</if> <if test='modelType!=null and modelType!=\"\"'> AND m.model_type=#{modelType}</if> <if test='objectType!=null and objectType!=\"\"'> AND m.primary_object_type=#{objectType}</if> <if test='objectId!=null'> AND m.primary_object_id=#{objectId}</if></script>")
        long count(String keyword, String modelType, String objectType, Long objectId);

        @Select("SELECT r.*, m.* FROM digital_resource r LEFT JOIN digital_model_metadata m ON m.resource_id=r.id WHERE r.id=#{id}")
        Map<String,Object> detail(Long id);
        @Select("SELECT * FROM digital_model_metadata WHERE resource_id=#{id}")
        Map<String,Object> getMetadata(Long id);
        @Select("SELECT resource_type AS resourceType FROM digital_resource WHERE id=#{id}")
        Map<String,Object> resourceBasic(Long id);
        @Insert("INSERT INTO digital_model_metadata (resource_id,model_type,model_stage,metadata_status) VALUES (#{rid},'scan','raw','incomplete')")
        void initMetadata(Long rid);
        @Update("<script>UPDATE digital_model_metadata SET model_title=COALESCE(#{p.modelTitle},model_title), model_type=COALESCE(#{p.modelType},model_type), model_stage=COALESCE(#{p.modelStage},model_stage), primary_object_type=COALESCE(#{p.primaryObjectType},primary_object_type), primary_object_id=COALESCE(#{p.primaryObjectId},primary_object_id), primary_object_code=COALESCE(#{p.primaryObjectCode},primary_object_code), primary_object_name=COALESCE(#{p.primaryObjectName},primary_object_name), scale_unit=COALESCE(#{p.scaleUnit},scale_unit), real_scale=COALESCE(#{p.realScale},real_scale), acquisition_method=COALESCE(#{p.acquisitionMethod},acquisition_method), acquisition_device=COALESCE(#{p.acquisitionDevice},acquisition_device), acquisition_date=COALESCE(#{p.acquisitionDate},acquisition_date), processing_software=COALESCE(#{p.processingSoftware},processing_software), vertex_count=COALESCE(#{p.vertexCount},vertex_count), face_count=COALESCE(#{p.faceCount},face_count), point_count=COALESCE(#{p.pointCount},point_count), model_description=COALESCE(#{p.modelDescription},model_description), quality_level=COALESCE(#{p.qualityLevel},quality_level), is_key_model=COALESCE(#{p.isKeyModel},is_key_model) WHERE resource_id=#{rid}</script>")
        void upsertMetadata(@Param("rid") Long rid, @Param("p") Map<String,Object> body);

        @Select("<script>SELECT * FROM digital_model_collection WHERE deleted=0 <if test='objectType!=null'> AND object_type=#{objectType}</if> <if test='objectId!=null'> AND object_id=#{objectId}</if> ORDER BY update_time DESC</script>")
        List<Map<String,Object>> collections(String objectType, Long objectId);
        @Insert("INSERT INTO digital_model_collection (collection_code,collection_name,collection_type) VALUES (#{code},#{name},#{type})")
        void insertCollection(String code, String name, String type);
        @Update("UPDATE digital_model_collection SET deleted=1 WHERE id=#{id}")
        void deleteCollection(Long id);
        @Insert("INSERT IGNORE INTO digital_model_collection_item (collection_id,resource_id) VALUES (#{cid},#{rid})")
        void insertItem(Long cid, Long rid);
        @Update("UPDATE digital_model_collection SET item_count=(SELECT COUNT(*) FROM digital_model_collection_item WHERE collection_id=#{id}) WHERE id=#{id}")
        void updateItemCount(Long id);
    }
}
