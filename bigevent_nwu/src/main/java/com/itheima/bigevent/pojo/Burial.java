package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor @Data
public class Burial {
    private Integer id;
    private String burialNo;
    private String name;
    private Integer siteId;
    private String siteName;
    private String era;
    private String burialType;
    private LocalDate excavationDate;
    private Boolean hasCoffin;
    private Boolean hasChariot;
    private Integer coffinCount;
    private String coffinMaterial;
    private String coffinDecoration;
    private String skeletonStatus;
    private Integer chariotCount;
    private Integer horseCount;
    private String chariotDecoration;
    private String chariotType;
    private Integer artifactCount;
    private String bonePreservation;
    private String status;
    private String notes;
    private Integer createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}