package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.service.ArtifactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artifacts")
@CrossOrigin
public class ArtifactsController {

    @Autowired
    private ArtifactsService artifactsService;

    // 添加遗物
    @PostMapping
    public Result add(@RequestBody artifacts artifact) {
        artifactsService.add(artifact);
        return Result.success();
    }

    // 根据ID删除遗物
    @DeleteMapping
    public Result deleteById(Integer id) {
        artifactsService.deleteById(id);
        return Result.success();
    }

    // 修改遗物信息
    @PutMapping
    public Result update(@RequestBody artifacts artifact) {
        artifactsService.update(artifact);
        return Result.success();
    }

    // 分页查询遗物列表
    @GetMapping
    public Result<PageBean<artifacts>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String artifactCode,
            @RequestParam(required = false) String siteName,
            @RequestParam(required = false) String relicName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String era) {
        // 调用Service层方法获取分页结果
        PageBean<artifacts> pageBean = artifactsService.list(pageNum, pageSize, name, artifactCode, siteName, relicName, category, material, era);
        return Result.success(pageBean);
    }
}
