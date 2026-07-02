package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.relics;
import com.itheima.bigevent.service.RelicsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relics")
@CrossOrigin
public class RelicsController {
    
    @Autowired
    private RelicsService relicsService;
    
    // 新增遗迹
    @PostMapping
    public Result add(@RequestBody relics relic) {
        relicsService.add(relic);
        return Result.success();
    }
    
    // 根据ID删除遗迹
    @DeleteMapping
    public Result delete(Integer id) {
        relicsService.deleteById(id);
        return Result.success();
    }
    
    // 修改遗迹
    @PutMapping
    public Result update(@RequestBody relics relic) {
        relicsService.update(relic);
        return Result.success();
    }
    
    // 分页查询遗迹列表
    @GetMapping
    public Result<PageBean<relics>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name,
            String relicCode,
            String siteName,
            String type) {
        // 调用Service层方法进行分页查询
        PageBean<relics> pageBean = relicsService.list(pageNum, pageSize, name, relicCode, siteName, type);
        return Result.success(pageBean);
    }
    
    // 获取所有遗迹名称（供下拉筛选）
    @GetMapping("/names")
    public Result<List<String>> getNames() {
        return Result.success(relicsService.getNames());
    }

    // 批量导入遗迹数据
    @PostMapping("/batch")
    public Result batchImport(@RequestBody List<relics> relicsList) {
        relicsService.batchImport(relicsList);
        return Result.success();
    }
}