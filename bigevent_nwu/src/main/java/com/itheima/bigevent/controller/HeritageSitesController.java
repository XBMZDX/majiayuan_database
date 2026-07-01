package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.heritageSites;
import com.itheima.bigevent.service.HeritageSitesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/heritage-sites")
@CrossOrigin
public class HeritageSitesController {
    
    @Autowired
    private HeritageSitesService heritageSitesService;
    
    // 新增遗址
    @PostMapping
    public Result add(@RequestBody heritageSites heritageSite) {
        heritageSitesService.add(heritageSite);
        return Result.success();
    }
    
    // 删除遗址
    @DeleteMapping
    public Result delete(Integer id) {
        heritageSitesService.delete(id);
        return Result.success();
    }
    
    // 修改遗址
    @PutMapping
    public Result update(@RequestBody heritageSites heritageSite) {
        heritageSitesService.update(heritageSite);
        return Result.success();
    }
    
    // 查询遗址列表
    @GetMapping
    public Result list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String locationProvince,
            @RequestParam(required = false) String locationCity,
            @RequestParam(required = false) String protectionLevel) {
        // 调用Service层方法获取分页结果
        var pageBean = heritageSitesService.list(pageNum, pageSize, name, locationProvince, locationCity, protectionLevel);
        return Result.success(pageBean);
    }
    
    // 批量导入遗址数据
    @PostMapping("/batch")
    public Result batchImport(@RequestBody List<heritageSites> heritageSitesList) {
        heritageSitesService.batchImport(heritageSitesList);
        return Result.success();
    }
}

