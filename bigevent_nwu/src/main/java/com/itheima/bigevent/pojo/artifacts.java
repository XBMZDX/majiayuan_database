package com.itheima.bigevent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class artifacts {
    private Integer id;
    private Integer burialId;            // 所属墓葬ID
    private Integer coffinIndex;         // 所属棺序号（1=棺1, 2=棺2...）
    private Integer coffinId;            // 所属棺ID（预留）
    private Integer chariotIndex;        // 所属车序号（1=车1, 2=车2...）
    private Integer chariotId;           // 所属车ID（预留）
    private Integer serialNumber;       // 序号
    private String newArtifactCode;     // 文物新编号
    private String newArtifactName;     // 文物新名称
    private String originalArtifactCode;// 文物原始编号
    private String originalArtifactName;// 文物原名称
    private String material1;           // 材质1
    private String material2;           // 材质2
    private String completeness;        // 完整度
    private String artifactDescription; // 文物描述2
    private String quantity1;           // 数量1
    private String quantity2;           // 数量2
    private String dimensions;          // 尺寸
    private String weight;              // 重量
    private String excavationRelic;     // 出土遗迹
    private String excavationPosition;  // 出土位置
    private String excavationTime;      // 出土时间
    private String storageMethod;       // 存放方式
    private String images;              // 图片
    private String transferProcess;     // 文物流转过程
    private String restorationStatus;   // 修复、复原状况
    private String photographer;        // 拍照人
    private String draftsperson;        // 绘图人
    private String textDescriber;       // 文字描述人
    private String notes;               // 备注
    private String gradingStatus;       // 定级情况
    private String testingStatus;       // 科技检测情况
    private String testingStatusDisplay; // 联动检测分析名称
    private Integer createdBy;          // 创建人id
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
