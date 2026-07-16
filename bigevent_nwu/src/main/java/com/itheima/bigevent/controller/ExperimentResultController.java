package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.ExperimentResult;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.ExperimentResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/experiment-result")
@CrossOrigin
public class ExperimentResultController {

    @Autowired
    private ExperimentResultMapper mapper;

    @GetMapping("/by-detection/{detectionId}")
    public Result<List<ExperimentResult>> listByDetection(@PathVariable Integer detectionId) {
        return Result.success(mapper.listByDetection(detectionId));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody ExperimentResult r) {
        r.setId(id); mapper.update(r); return Result.success();
    }
}
