package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.AnalysisResult;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.AnalysisResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/analysis-result")
@CrossOrigin
public class AnalysisResultController {

    @Autowired
    private AnalysisResultMapper mapper;

    @GetMapping
    public Result<List<AnalysisResult>> list() {
        return Result.success(mapper.list());
    }

    @GetMapping("/{id}")
    public Result<AnalysisResult> detail(@PathVariable Integer id) {
        return Result.success(mapper.findById(id));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody AnalysisResult r) {
        r.setId(id); mapper.update(r); return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        mapper.delete(id); return Result.success();
    }
}
