package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.LabInstrument;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.LabInstrumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/lab-instrument")
@CrossOrigin
public class LabInstrumentController {

    @Autowired
    private LabInstrumentMapper mapper;

    @GetMapping
    public Result<List<LabInstrument>> list() {
        return Result.success(mapper.list());
    }

    @GetMapping("/{id}")
    public Result<LabInstrument> detail(@PathVariable Integer id) {
        return Result.success(mapper.findById(id));
    }

    @PostMapping
    public Result add(@RequestBody LabInstrument instrument) {
        applyDisplayDefaults(instrument);
        mapper.insert(instrument);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody LabInstrument instrument) {
        instrument.setId(id);
        applyDisplayDefaults(instrument);
        mapper.update(instrument);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        mapper.delete(id);
        return Result.success();
    }

    private void applyDisplayDefaults(LabInstrument instrument) {
        if (instrument.getNonDestructive() == null) instrument.setNonDestructive(false);
        if (instrument.getRequiresSampling() == null) instrument.setRequiresSampling(false);
    }
}
