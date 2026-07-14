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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
