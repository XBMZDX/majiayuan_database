package com.itheima.bigevent.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class relics {
    private Integer id; // 遗迹ID
    @JsonProperty("relic_code")
    private String relicCode; // 遗迹编号
    @JsonProperty("site_id")
    private Integer siteId; // 所属遗址ID
    @JsonProperty("site_name")
    private String siteName; // 遗址名称
    private String name; // 遗迹名称
    private String type; // 遗迹类型
    @JsonProperty("position_within_site")
    private String positionWithinSite; // 在遗址中的位置
    @JsonProperty("excavation_area")
    private String excavationArea; // 发掘区域
    @JsonProperty("excavation_unit")
    private String excavationUnit; // 发掘单位
    private String era; // 所属时代
    private String stratigraphy; // 地层关系
    @JsonProperty("structure_description")
    private String structureDescription; // 结构描述
    private String dimensions; // 尺寸
    private String orientation; // 方向
    @JsonProperty("burial_depth")
    private Double burialDepth; // 埋藏深度
    @JsonProperty("preservation_status")
    private String preservationStatus; // 保存状况
    @JsonProperty("function_purpose")
    private String functionPurpose; // 功能用途
    @JsonProperty("cultural_features")
    private String culturalFeatures; // 文化特征
    @JsonProperty("related_relics")
    private String relatedRelics; // 相关遗迹
    private String images; // 遗迹图片URL
    private String drawings; // 线图、剖面图等（JSON数组）
    private String notes; // 备注
    @JsonProperty("created_by")
    private Integer createdBy; // 创建人id
    @JsonProperty("create_time")
    private LocalDateTime createTime; // 创建时间
    @JsonProperty("update_time")
    private LocalDateTime updateTime; // 更新时间
}