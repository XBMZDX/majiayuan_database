package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.service.ArtifactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artifacts")
@CrossOrigin
public class ArtifactsController {

    @Autowired
    private ArtifactsService artifactsService;

    // 添加文物
    @PostMapping
    public Result add(@RequestBody artifacts artifact) {
        artifactsService.add(artifact);
        return Result.success();
    }

    // 根据ID删除文物
    @DeleteMapping
    public Result deleteById(Integer id) {
        artifactsService.deleteById(id);
        return Result.success();
    }

    // 批量删除文物
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        artifactsService.batchDelete(ids);
        return Result.success();
    }

    // 修改文物信息
    @PutMapping
    public Result update(@RequestBody artifacts artifact) {
        artifactsService.update(artifact);
        return Result.success();
    }

    // 分页查询文物列表
    @GetMapping
    public Result<PageBean<artifacts>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String newArtifactName,
            @RequestParam(required = false) String newArtifactCode,
            @RequestParam(required = false) String material1,
            @RequestParam(required = false) String excavationRelic,
            @RequestParam(required = false) String completeness) {
        PageBean<artifacts> pageBean = artifactsService.list(pageNum, pageSize, newArtifactName, newArtifactCode, material1, excavationRelic, completeness);
        return Result.success(pageBean);
    }
}