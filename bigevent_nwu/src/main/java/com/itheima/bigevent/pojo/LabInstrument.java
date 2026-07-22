package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabInstrument {
    private Integer id;
    private String name;
    private String image;
    private String scope;
    private String location;
    private String model;
    private String conditions;
    private String method;
    private String methodName;
    private String applicableMaterials;
    private String researchPurposes;
    private Boolean nonDestructive;
    private Boolean requiresSampling;
    private String mainOutputs;
    private Integer appliedArtifactCount;
    private Integer completedDetectionCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
