package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/coffin-workflow")
@CrossOrigin
public class CoffinWorkflowController {

    @Autowired private CoffinWorkflowTreeMapper treeMapper;
    @Autowired private CoffinWorkflowTimelineMapper timelineMapper;

    // ========== 流程树 ==========
    @GetMapping("/tree")
    public Result<List<Map<String,Object>>> getTree(@RequestParam(required = false) Integer coffinId) {
        return Result.success(treeMapper.listByCoffin(coffinId));
    }

    @PutMapping("/tree")
    public Result saveTree(@RequestBody Map<String,Object> body) {
        int coffinId = Integer.parseInt(body.get("coffinId").toString());
        List<Map<String,Object>> nodes = (List<Map<String,Object>>) body.get("nodes");
        treeMapper.deleteByCoffin(coffinId);
        for (int i = 0; i < nodes.size(); i++) {
            treeMapper.insert(coffinId, nodes.get(i).get("label").toString(), i + 1);
        }
        return Result.success();
    }

    // ========== 时间轴 ==========
    @GetMapping("/timeline")
    public Result<List<Map<String,Object>>> getTimeline(@RequestParam(required = false) Integer coffinId) {
        return Result.success(timelineMapper.listByCoffin(coffinId));
    }

    @PutMapping("/timeline")
    public Result saveTimeline(@RequestBody Map<String,Object> body) {
        int coffinId = Integer.parseInt(body.get("coffinId").toString());
        List<Map<String,Object>> items = (List<Map<String,Object>>) body.get("items");
        timelineMapper.deleteByCoffin(coffinId);
        for (Map<String,Object> item : items) {
            timelineMapper.insert(
                coffinId,
                Integer.parseInt(item.get("flowId").toString()),
                item.get("date") != null ? item.get("date").toString() : null,
                item.get("title") != null ? item.get("title").toString() : "",
                item.get("status") != null ? item.get("status").toString() : "pending"
            );
        }
        return Result.success();
    }

    // 内联 Mapper
    @Mapper interface CoffinWorkflowTreeMapper {
        @Select("SELECT id, label FROM coffin_workflow_tree WHERE coffin_id = #{coffinId} ORDER BY sort_order")
        List<Map<String,Object>> listByCoffin(Integer coffinId);
        @Delete("DELETE FROM coffin_workflow_tree WHERE coffin_id = #{coffinId}") void deleteByCoffin(int coffinId);
        @Insert("INSERT INTO coffin_workflow_tree (coffin_id, label, sort_order) VALUES (#{coffinId}, #{label}, #{order})")
        void insert(int coffinId, String label, int order);
    }

    @Mapper interface CoffinWorkflowTimelineMapper {
        @Select("SELECT id, flow_id AS flowId, event_date AS date, title, status FROM coffin_workflow_timeline WHERE coffin_id = #{coffinId} ORDER BY event_date")
        List<Map<String,Object>> listByCoffin(Integer coffinId);
        @Delete("DELETE FROM coffin_workflow_timeline WHERE coffin_id = #{coffinId}") void deleteByCoffin(int coffinId);
        @Insert("INSERT INTO coffin_workflow_timeline (coffin_id, flow_id, event_date, title, status) VALUES (#{coffinId}, #{flowId}, #{date}, #{title}, #{status})")
        void insert(int coffinId, int flowId, String date, String title, String status);
    }

    // ========== 备注 ==========
    @Autowired private CoffinWorkflowNoteMapper noteMapper;

    @GetMapping("/note/{timelineId}")
    public Result<List<Map<String,Object>>> getNotes(@PathVariable int timelineId) {
        return Result.success(noteMapper.listByTimeline(timelineId));
    }
    @PostMapping("/note")
    public Result addNote(@RequestBody Map<String,Object> note) {
        noteMapper.insert(
            Integer.parseInt(note.get("timelineId").toString()),
            note.get("noteType") != null ? note.get("noteType").toString() : "",
            note.get("content") != null ? note.get("content").toString() : ""
        );
        return Result.success();
    }
    @DeleteMapping("/note/{id}")
    public Result deleteNote(@PathVariable int id) { noteMapper.delete(id); return Result.success(); }

    @Mapper interface CoffinWorkflowNoteMapper {
        @Select("SELECT id, timeline_id AS timelineId, note_type AS noteType, content, create_time AS createTime FROM coffin_workflow_note WHERE timeline_id = #{timelineId} ORDER BY create_time")
        List<Map<String,Object>> listByTimeline(int timelineId);
        @Insert("INSERT INTO coffin_workflow_note (timeline_id, note_type, content) VALUES (#{timelineId}, #{type}, #{content})")
        void insert(int timelineId, String type, String content);
        @Delete("DELETE FROM coffin_workflow_note WHERE id = #{id}") void delete(int id);
    }

    // ========== 媒体 ==========
    @Autowired private CoffinWorkflowMediaMapper mediaMapper;

    @GetMapping("/media/{timelineId}")
    public Result<List<Map<String,Object>>> getMedia(@PathVariable int timelineId) {
        return Result.success(mediaMapper.listByTimeline(timelineId));
    }
    @PostMapping("/media")
    public Result addMedia(@RequestBody Map<String,Object> media) {
        mediaMapper.insert(
            Integer.parseInt(media.get("timelineId").toString()),
            media.get("mediaType") != null ? media.get("mediaType").toString() : "",
            media.get("fileName") != null ? media.get("fileName").toString() : "",
            media.get("fileUrl") != null ? media.get("fileUrl").toString() : "",
            media.get("description") != null ? media.get("description").toString() : ""
        );
        return Result.success();
    }
    @DeleteMapping("/media/{id}")
    public Result deleteMedia(@PathVariable int id) { mediaMapper.delete(id); return Result.success(); }

    @Mapper interface CoffinWorkflowMediaMapper {
        @Select("SELECT id, timeline_id AS timelineId, media_type AS mediaType, file_name AS fileName, file_url AS fileUrl, description, create_time AS createTime FROM coffin_workflow_media WHERE timeline_id = #{timelineId} ORDER BY create_time")
        List<Map<String,Object>> listByTimeline(int timelineId);
        @Insert("INSERT INTO coffin_workflow_media (timeline_id, media_type, file_name, file_url, description) VALUES (#{timelineId}, #{type}, #{name}, #{url}, #{desc})")
        void insert(int timelineId, String type, String name, String url, String desc);
        @Delete("DELETE FROM coffin_workflow_media WHERE id = #{id}") void delete(int id);
    }
}
