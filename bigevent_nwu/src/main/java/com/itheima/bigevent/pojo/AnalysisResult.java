package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class AnalysisResult {
    private Integer id;
    private Integer detectionId;
    private String artifactCode;
    private String artifactName;
    private String samplePhoto;
    private String sampleStatus;
    private String experimentMethod;
    private String detectionPurpose;
    private String instrumentModel;
    private String testParams;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
