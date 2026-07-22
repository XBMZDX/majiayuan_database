package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Burial;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.mapper.BurialMapper;
import com.itheima.bigevent.mapper.ArtifactsMapper;
import com.itheima.bigevent.service.ArtifactDetectionStatusService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/burial")
@CrossOrigin
public class BurialController {

    @Autowired
    private BurialMapper burialMapper;

    @Autowired
    private ArtifactsMapper artifactsMapper;
    @Autowired
    private ArtifactDetectionStatusService artifactDetectionStatusService;

    // 墓葬出土文物列表
    @GetMapping("/{id}/artifacts")
    public Result<List<artifacts>> burialArtifacts(@PathVariable Integer id) {
        return Result.success(artifactDetectionStatusService.decorate(artifactsMapper.listByBurial(id)));
    }

    // 墓葬文物统计（总数 + 材质分布 + 完整度分布）
    @GetMapping("/{id}/artifact-stats")
    public Result<Map<String, Object>> artifactStats(@PathVariable Integer id) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", artifactsMapper.countByBurial(id));
        stats.put("materials", artifactsMapper.materialDistribution(id));
        stats.put("completeness", artifactsMapper.completenessDistribution(id));
        return Result.success(stats);
    }

    // 简易列表（下拉用）
    @GetMapping("/list/simple")
    public Result<List<Burial>> listSimple() {
        return Result.success(burialMapper.listSimple());
    }

    // 根据ID查询详情
    @GetMapping("/{id}")
    public Result<Burial> findById(@PathVariable Integer id) {
        return Result.success(burialMapper.findById(id));
    }

    // 新增墓葬
    @PostMapping
    public Result add(@RequestBody Burial burial) {
        Map<String, Object> map = ThreadLocalUtil.get();
        burial.setCreatedBy((Integer) map.get("id"));
        burial.setCreateTime(LocalDateTime.now());
        burial.setUpdateTime(LocalDateTime.now());
        if (burial.getStatus() == null) burial.setStatus("待发掘");
        burialMapper.insert(burial);
        return Result.success(burial);
    }

    // 更新墓葬
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Burial burial) {
        burial.setId(id);
        burial.setUpdateTime(LocalDateTime.now());
        burialMapper.update(burial);
        return Result.success();
    }
}
