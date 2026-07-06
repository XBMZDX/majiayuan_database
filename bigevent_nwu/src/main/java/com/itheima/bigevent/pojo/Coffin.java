package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class Coffin {
    private Integer id;
    private Integer burialId;
    private String coffinNo;
    private Integer coffinCount;
    private String material;
    private String decoration;
    private String skeletonStatus;
    private String notes;
}
