package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class artifacts {
    private Integer id; // 遗物ID
    private String artifactCode; // 遗物编号
    private Integer siteId; // 所属遗址ID
    private String siteName; // 遗址名称
    private Integer relicId; // 所属遗迹ID
    private String relicName; // 遗迹名称
    private String name; // 遗物名称
    private String category; // 遗物类别
    private String subCategory; // 子类别
    private String material; // 材质
    private String era; // 时代
    private String size; // 尺寸
    private Double weight; // 重量
    private String color; // 颜色
    private String texture; // 质地
    private String decoration; // 纹饰
    private String inscription; // 铭文/刻符
    private String productionTechnique; // 制作工艺
    private String usageFunction; // 使用功能
    private String discoveryContext; // 出土背景
    private String preservationCondition; // 保存状况
    private String restorationInfo; // 修复信息
    private String currentLocation; // 现藏地点
    private String museumNumber; // 馆藏编号
    private String images; // 遗物图片
    private String model3dUrl; // 3D模型URL
    private String researchNotes; // 研究记录
    private String bibliography; // 参考文献
    private String culturalValue; // 文化价值
    private Integer createdBy; // 创建人id
    private String verificationStatus; // 审核状态
    private String verificationNotes; // 审核意见
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
