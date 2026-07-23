package com.itheima.bigevent.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArtifactImageMapper {

    @Select("SELECT id,artifact_id AS artifactId,image_url AS imageUrl,image_name AS imageName,description,is_cover AS isCover,sort_order AS sortOrder,upload_time AS uploadTime FROM artifact_image WHERE artifact_id=#{artifactId} AND deleted=0 ORDER BY is_cover DESC,sort_order ASC,id ASC")
    List<Map<String, Object>> listByArtifact(Integer artifactId);

    @Select("SELECT COUNT(*) FROM artifact_image WHERE artifact_id=#{artifactId} AND deleted=0")
    int countByArtifact(Integer artifactId);

    @Select("SELECT COALESCE(MAX(sort_order),0)+1 FROM artifact_image WHERE artifact_id=#{artifactId} AND deleted=0")
    int nextSort(Integer artifactId);

    @Insert("INSERT INTO artifact_image (artifact_id,image_url,image_name,description,is_cover,sort_order) VALUES (#{artifactId},#{url},#{name},#{description},#{cover},#{sortOrder})")
    void insert(@Param("artifactId") Integer artifactId,
                @Param("url") String url,
                @Param("name") String name,
                @Param("description") String description,
                @Param("sortOrder") int sortOrder,
                @Param("cover") boolean cover);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();

    @Select("SELECT id,artifact_id AS artifactId,image_url AS imageUrl,image_name AS imageName,description,is_cover AS isCover,sort_order AS sortOrder,upload_time AS uploadTime FROM artifact_image WHERE id=#{id} AND deleted=0")
    Map<String, Object> detail(Long id);

    @Update("UPDATE artifact_image SET is_cover=0 WHERE artifact_id=#{artifactId}")
    void clearCover(Integer artifactId);

    @Update("UPDATE artifact_image SET is_cover=1 WHERE id=#{id}")
    void setCover(Long id);

    @Update("<script>UPDATE artifact_image SET image_name=COALESCE(#{p.imageName},image_name),description=COALESCE(#{p.description},description),sort_order=COALESCE(#{p.sortOrder},sort_order),update_time=NOW() WHERE id=#{id}</script>")
    void update(@Param("id") Long id, @Param("p") Map<String, Object> body);

    @Update("UPDATE artifact_image SET deleted=1,update_time=NOW() WHERE id=#{id}")
    void softDelete(Long id);

    @Select("SELECT id,artifact_id AS artifactId,image_url AS imageUrl,image_name AS imageName,description,is_cover AS isCover,sort_order AS sortOrder,upload_time AS uploadTime FROM artifact_image WHERE artifact_id=#{artifactId} AND deleted=0 ORDER BY sort_order ASC,id ASC LIMIT 1")
    Map<String, Object> firstActive(Integer artifactId);

    @Update("UPDATE artifacts SET images=#{url},update_time=NOW() WHERE id=#{artifactId}")
    void updateArtifactCover(@Param("artifactId") Integer artifactId, @Param("url") String url);

    @Select("<script>" +
        "SELECT COALESCE((SELECT ai.image_url FROM artifact_image ai WHERE ai.artifact_id=a.id AND ai.deleted=0 ORDER BY ai.is_cover DESC,ai.sort_order ASC,ai.id ASC LIMIT 1),a.images) " +
        "FROM artifacts a WHERE " +
        "<choose>" +
        "<when test='artifactCode != null and artifactCode != \"\"'>" +
        "(CONVERT(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(COALESCE(a.new_artifact_code,'')),'：',':'),' ',''),'-',''),'*','') USING utf8mb4) COLLATE utf8mb4_unicode_ci = " +
        "CONVERT(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(#{artifactCode}),'：',':'),' ',''),'-',''),'*','') USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
        "OR CONVERT(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(COALESCE(a.original_artifact_code,'')),'：',':'),' ',''),'-',''),'*','') USING utf8mb4) COLLATE utf8mb4_unicode_ci = " +
        "CONVERT(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(#{artifactCode}),'：',':'),' ',''),'-',''),'*','') USING utf8mb4) COLLATE utf8mb4_unicode_ci)" +
        "</when>" +
        "<otherwise>" +
        "(CONVERT(TRIM(COALESCE(a.new_artifact_name,'')) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(#{artifactName}) USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
        "OR CONVERT(TRIM(COALESCE(a.original_artifact_name,'')) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(#{artifactName}) USING utf8mb4) COLLATE utf8mb4_unicode_ci)" +
        "</otherwise>" +
        "</choose> ORDER BY a.id DESC LIMIT 1" +
        "</script>")
    String findArtifactCoverByIdentity(@Param("artifactCode") String artifactCode, @Param("artifactName") String artifactName);
}
