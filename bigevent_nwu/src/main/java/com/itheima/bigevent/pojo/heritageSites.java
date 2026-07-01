package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class heritageSites {
    private Integer id; // 遗址ID
    private String siteCode; // 遗址编号
    private String name; // 遗址名称
    private String alias; // 遗址别名
    private String locationProvince; // 所在省份
    private String locationCity; // 所在城市
    private String locationDetail; // 详细地址
    private Double latitude; // 纬度
    private Double longitude; // 经度
    private String era; // 所属时代
    private String category; // 遗址类别
    private String protectionLevel; // 保护级别
    private Integer discoveryYear; // 发现年份
    private Integer excavationYear; // 发掘年份
    private Double areaSize; // 遗址面积
    private String description; // 遗址描述
    private String culturalValue; // 文化价值
    private String currentStatus; // 现状描述
    private String coverImage; // 封面图片URL
    private Integer createdBy; // 创建人id
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}
