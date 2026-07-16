package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class ExperimentResult {
    private Integer id;
    private Integer detectionId;
    private String experimentName;
    private String status;
    private String resultData;
    private String images;
    private String attachments;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
