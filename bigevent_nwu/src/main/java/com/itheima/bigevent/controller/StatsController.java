package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据 Controller — 返回各模块总数供首页看板使用
 */
@RestController
@RequestMapping("/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping
    public Result<Map<String, Long>> getStats() {
        Map<String, Long> map = new HashMap<>();
        map.put("artifactCount", statsMapper.countArtifacts());
        map.put("siteCount", statsMapper.countSites());
        map.put("relicCount", statsMapper.countRelics());
        map.put("detectionCount", 0L); // 检测功能暂未实现
        return Result.success(map);
    }

    /**
     * 内联 Mapper — 仅在 StatsController 中使用
     */
    @Mapper
    public interface StatsMapper {
        @Select("SELECT COUNT(*) FROM artifacts")
        Long countArtifacts();

        @Select("SELECT COUNT(*) FROM heritage_sites")
        Long countSites();

        @Select("SELECT COUNT(*) FROM relics")
        Long countRelics();
    }
}