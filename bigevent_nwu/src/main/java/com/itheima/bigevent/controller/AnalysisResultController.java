package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.AnalysisResult;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.mapper.AnalysisResultMapper;
import com.itheima.bigevent.mapper.ArtifactImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/analysis-result")
@CrossOrigin
public class AnalysisResultController {

    @Autowired
    private AnalysisResultMapper mapper;
    @Autowired
    private ArtifactImageMapper artifactImageMapper;

    @GetMapping
    public Result<List<AnalysisResult>> list() {
        List<AnalysisResult> results = mapper.list();
        results.forEach(this::applyArtifactCover);
        return Result.success(results);
    }

    @GetMapping("/{id}")
    public Result<AnalysisResult> detail(@PathVariable Integer id) {
        AnalysisResult result = mapper.findById(id);
        applyArtifactCover(result);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody AnalysisResult r) {
        r.setId(id); mapper.update(r); return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        mapper.delete(id); return Result.success();
    }

    private void applyArtifactCover(AnalysisResult result) {
        if (result == null) return;
        if ((result.getArtifactCode() == null || result.getArtifactCode().isBlank())
            && (result.getArtifactName() == null || result.getArtifactName().isBlank())) return;
        String coverUrl = artifactImageMapper.findArtifactCoverByIdentity(result.getArtifactCode(), result.getArtifactName());
        if (coverUrl != null && !coverUrl.isBlank()) result.setSamplePhoto(coverUrl);
    }
}
