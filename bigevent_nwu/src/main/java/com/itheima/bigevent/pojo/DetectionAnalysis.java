package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class DetectionAnalysis {
    private Integer id;
    private String serialNumber;
    private String artifactCode;
    private String artifactName;
    private String excavationRelic;
    private String samplePosition;
    private String sampleMaterial;
    private String sampleStatus;
    private String sampleQuantity;
    private String sampleMethod;
    private String purpose;
    private String instrumentName;
    private String instrumentModel;
    private String testParams;
    private String storageLocation;
    private String departureTime;
    private String destination;
    private String samplePhoto;
    private String analysisData;
    private String analysisReport;
    private String manager;
    private String sampler;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
