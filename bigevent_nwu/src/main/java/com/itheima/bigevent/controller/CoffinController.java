package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Coffin;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.ArtifactsMapper;
import com.itheima.bigevent.mapper.CoffinMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/coffin")
@CrossOrigin
public class CoffinController {

    @Autowired
    private ArtifactsMapper artifactsMapper;

    @Autowired
    private CoffinMapper coffinMapper;

    // 按墓葬查棺列表
    @GetMapping("/by-burial/{burialId}")
    public Result<List<Coffin>> byBurial(@PathVariable Integer burialId) {
        return Result.success(coffinMapper.listByBurial(burialId));
    }

    // 按墓葬查棺数量
    @GetMapping("/count-by-burial/{burialId}")
    public Result<Integer> countByBurial(@PathVariable Integer burialId) {
        return Result.success(coffinMapper.countByBurial(burialId));
    }

    // 棺内文物列表
    @GetMapping("/{id}/artifacts")
    public Result<?> artifacts(@PathVariable Integer id) {
        return Result.success(artifactsMapper.listByCoffin(id));
    }

    // 棺内文物统计
    @GetMapping("/{id}/artifact-stats")
    public Result<Map<String, Object>> stats(@PathVariable Integer id) {
        Map<String, Object> m = new HashMap<>();
        m.put("total", artifactsMapper.countByCoffin(id));
        m.put("materials", artifactsMapper.coffinMaterialDistribution(id));
        m.put("completeness", artifactsMapper.coffinCompletenessDistribution(id));
        return Result.success(m);
    }
}
