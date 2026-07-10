package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/workflow")
@CrossOrigin
public class WorkflowController {

    @Autowired private WorkflowTreeMapper treeMapper;
    @Autowired private WorkflowTimelineMapper timelineMapper;

    // ========== 流程树 ==========
    @GetMapping("/tree")
    public Result<List<Map<String,Object>>> getTree() { return Result.success(treeMapper.list()); }

    @PutMapping("/tree")
    public Result saveTree(@RequestBody List<Map<String,Object>> nodes) {
        treeMapper.deleteAll();
        for (int i = 0; i < nodes.size(); i++) {
            treeMapper.insert(nodes.get(i).get("label").toString(), i + 1);
        }
        return Result.success();
    }

    // ========== 时间轴 ==========
    @GetMapping("/timeline")
    public Result<List<Map<String,Object>>> getTimeline() { return Result.success(timelineMapper.list()); }

    @PutMapping("/timeline")
    public Result saveTimeline(@RequestBody List<Map<String,Object>> items) {
        timelineMapper.deleteAll();
        for (Map<String,Object> item : items) {
            timelineMapper.insert(
                Integer.parseInt(item.get("flowId").toString()),
                item.get("date") != null ? item.get("date").toString() : null,
                item.get("title") != null ? item.get("title").toString() : "",
                item.get("desc") != null ? item.get("desc").toString() : ""
            );
        }
        return Result.success();
    }

    // 内联 Mapper
    @Mapper interface WorkflowTreeMapper {
        @Select("SELECT id, label FROM workflow_tree ORDER BY sort_order")
        List<Map<String,Object>> list();
        @Delete("DELETE FROM workflow_tree") void deleteAll();
        @Insert("INSERT INTO workflow_tree (label, sort_order) VALUES (#{label}, #{order})")
        void insert(String label, int order);
    }

    @Mapper interface WorkflowTimelineMapper {
        @Select("SELECT id, flow_id, event_date AS date, title, description AS `desc` FROM workflow_timeline ORDER BY event_date")
        List<Map<String,Object>> list();
        @Delete("DELETE FROM workflow_timeline") void deleteAll();
        @Insert("INSERT INTO workflow_timeline (flow_id, event_date, title, description) VALUES (#{flowId}, #{date}, #{title}, #{desc})")
        void insert(int flowId, String date, String title, String desc);
    }
}
