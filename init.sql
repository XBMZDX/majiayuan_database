-- ============================================
-- 马家塬数据库 初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS nwsite
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE nwsite;

-- ============================================
-- 1. 系统模块
-- ============================================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS user (
    id           INT          PRIMARY KEY AUTO_INCREMENT,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    nickname     VARCHAR(10),
    email        VARCHAR(100),
    user_pic     VARCHAR(500) COMMENT '用户头像地址',
    token_version INT         DEFAULT 0 COMMENT 'JWT token版本号，修改密码后+1',
    create_time  DATETIME,
    update_time  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 2. 遗址与遗迹模块
-- ============================================

-- 2.1 遗址表
CREATE TABLE IF NOT EXISTS heritage_sites (
    id                INT           PRIMARY KEY AUTO_INCREMENT,
    site_code         VARCHAR(50)   COMMENT '遗址编号',
    name              VARCHAR(200)  NOT NULL COMMENT '遗址名称',
    alias             VARCHAR(200)  COMMENT '别名',
    location_province VARCHAR(50)   COMMENT '所在省份',
    location_city     VARCHAR(50)   COMMENT '所在城市',
    location_detail   VARCHAR(500)  COMMENT '详细地址',
    latitude          DOUBLE        COMMENT '纬度',
    longitude         DOUBLE        COMMENT '经度',
    era               VARCHAR(100)  COMMENT '年代',
    category          VARCHAR(100)  COMMENT '遗址类别',
    protection_level  VARCHAR(50)   COMMENT '保护级别',
    discovery_year    INT           COMMENT '发现年份',
    excavation_year   INT           COMMENT '发掘年份',
    area_size         DOUBLE        COMMENT '面积（平方米）',
    description       TEXT          COMMENT '遗址描述',
    cultural_value    TEXT          COMMENT '文化价值',
    current_status    VARCHAR(50)   COMMENT '现状',
    cover_image       VARCHAR(500)  COMMENT '封面图片URL',
    created_by        INT           COMMENT '创建人ID',
    create_time       DATETIME,
    update_time       DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.2 遗迹表
CREATE TABLE IF NOT EXISTS relics (
    id                    INT           PRIMARY KEY AUTO_INCREMENT,
    relic_code            VARCHAR(50)   COMMENT '遗迹编号',
    site_id               INT           COMMENT '所属遗址ID',
    site_name             VARCHAR(200)  COMMENT '所属遗址名称',
    name                  VARCHAR(200)  COMMENT '遗迹名称',
    type                  VARCHAR(100)  COMMENT '遗迹类型',
    position_within_site  VARCHAR(200)  COMMENT '在遗址中位置',
    excavation_area       DOUBLE        COMMENT '发掘面积',
    excavation_unit       VARCHAR(200)  COMMENT '发掘单位',
    era                   VARCHAR(100)  COMMENT '年代',
    stratigraphy          VARCHAR(500)  COMMENT '地层关系',
    structure_description TEXT          COMMENT '结构描述',
    dimensions            VARCHAR(200)  COMMENT '尺寸',
    orientation           VARCHAR(50)   COMMENT '朝向',
    burial_depth          DOUBLE        COMMENT '埋深',
    preservation_status   VARCHAR(50)   COMMENT '保存状况',
    function_purpose      VARCHAR(200)  COMMENT '功能用途',
    cultural_features     TEXT          COMMENT '文化特征',
    related_relics        TEXT          COMMENT '相关遗迹',
    images                TEXT          COMMENT '图片URL',
    drawings              TEXT          COMMENT '图纸URL',
    notes                 TEXT          COMMENT '备注',
    created_by            INT           COMMENT '创建人ID',
    create_time           DATETIME,
    update_time           DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 3. 墓葬与出土模块
-- ============================================

-- 3.1 墓葬信息表
CREATE TABLE IF NOT EXISTS burial (
    id                 INT           PRIMARY KEY AUTO_INCREMENT,
    burial_no          VARCHAR(50)   COMMENT '墓葬编号',
    name               VARCHAR(200)  COMMENT '墓葬名称',
    site_id            INT           COMMENT '所属遗址ID',
    site_name          VARCHAR(200)  COMMENT '所属遗址',
    era                VARCHAR(100)  COMMENT '年代',
    burial_type        VARCHAR(100)  COMMENT '墓葬类型',
    excavation_date    DATE          COMMENT '发掘时间',
    has_coffin         TINYINT       DEFAULT 0 COMMENT '有棺',
    has_chariot        TINYINT       DEFAULT 0 COMMENT '有车',
    coffin_count       INT           DEFAULT 0 COMMENT '棺数量',
    coffin_material    VARCHAR(100)  COMMENT '棺材质',
    coffin_decoration  VARCHAR(200)  COMMENT '棺装饰',
    skeleton_status    VARCHAR(50)   COMMENT '人骨状况',
    chariot_count      INT           DEFAULT 0 COMMENT '车数量',
    horse_count        INT           DEFAULT 0 COMMENT '马数量',
    chariot_decoration VARCHAR(200)  COMMENT '车装饰',
    chariot_type       VARCHAR(200)  COMMENT '车类型',
    artifact_count     INT           DEFAULT 0 COMMENT '随葬品数量',
    bone_preservation  VARCHAR(50)   COMMENT '人骨保存',
    status             VARCHAR(20)   DEFAULT '已发掘' COMMENT '状态',
    notes              TEXT          COMMENT '备注',
    created_by         INT           COMMENT '创建人ID',
    create_time        DATETIME,
    update_time        DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓葬信息表';

-- 3.2 棺信息表
CREATE TABLE IF NOT EXISTS coffin (
    id              INT           PRIMARY KEY AUTO_INCREMENT,
    burial_id       INT           COMMENT '所属墓葬ID',
    coffin_no       VARCHAR(50)   COMMENT '棺编号',
    coffin_count    INT           DEFAULT 1 COMMENT '棺数量',
    material        VARCHAR(200)  COMMENT '棺材质',
    decoration      VARCHAR(200)  COMMENT '棺装饰',
    skeleton_status VARCHAR(50)   COMMENT '人骨保存状况',
    notes           TEXT          COMMENT '备注',
    create_time     DATETIME,
    update_time     DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棺信息表';

-- 3.3 出土文物表（遗物）
CREATE TABLE IF NOT EXISTS artifacts (
    id                    INT           PRIMARY KEY AUTO_INCREMENT,
    serial_number         VARCHAR(50)   COMMENT '序号',
    new_artifact_code     VARCHAR(100)  COMMENT '文物新编号',
    new_artifact_name     VARCHAR(200)  COMMENT '文物新名称',
    original_artifact_code VARCHAR(100) COMMENT '文物原始编号',
    original_artifact_name VARCHAR(200) COMMENT '文物原始名称',
    material1             VARCHAR(500)  COMMENT '材质1',
    material2             VARCHAR(500)  COMMENT '材质2',
    completeness          VARCHAR(500)  COMMENT '完整度',
    artifact_description  TEXT          COMMENT '文物描述',
    quantity1             VARCHAR(500)  COMMENT '数量1（件数）',
    quantity2             VARCHAR(500)  COMMENT '数量2',
    dimensions            VARCHAR(500)  COMMENT '尺寸',
    weight                VARCHAR(500)  COMMENT '重量',
    excavation_relic      VARCHAR(200)  COMMENT '出土遗迹',
    excavation_position   VARCHAR(200)  COMMENT '出土位置',
    excavation_time       VARCHAR(100)  COMMENT '出土时间',
    storage_method        VARCHAR(100)  COMMENT '存放方式',
    images                TEXT          COMMENT '图片URL',
    transfer_process      TEXT          COMMENT '流转过程',
    restoration_status    TEXT          COMMENT '修复状态',
    photographer          VARCHAR(50)   COMMENT '摄影者',
    draftsperson          VARCHAR(50)   COMMENT '绘图者',
    text_describer        VARCHAR(50)   COMMENT '文字描述者',
    notes                 TEXT          COMMENT '备注',
    grading_status        VARCHAR(100)  COMMENT '定级情况',
    testing_status        VARCHAR(200)  COMMENT '科技检测情况',
    burial_id             INT           COMMENT '所属墓葬ID',
    coffin_id             INT           COMMENT '所属棺ID',
    coffin_index          INT           DEFAULT 1 COMMENT '棺序号（1=棺1, 2=棺2...）',
    chariot_id            INT           COMMENT '所属车ID',
    chariot_index         INT           COMMENT '车序号（1=车1, 2=车2...）',
    created_by            INT           COMMENT '创建人ID',
    create_time           DATETIME,
    update_time           DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出土文物表';

-- ============================================
-- 4. 分类字典表
-- ============================================

-- 4.1 材质分类表
CREATE TABLE IF NOT EXISTS material_categories (
    id         INT          PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id  INT          DEFAULT 0 COMMENT '父级ID，0表示一级分类',
    name       VARCHAR(50)  NOT NULL COMMENT '分类名称',
    level      TINYINT      NOT NULL COMMENT '层级: 1/2/3',
    sort_order INT          DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材质分类表';

-- 4.2 完整度分类表
CREATE TABLE IF NOT EXISTS completeness_categories (
    id         INT          PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id  INT          DEFAULT 0 COMMENT '父级ID，0表示一级分类',
    name       VARCHAR(50)  NOT NULL COMMENT '完整度分类名称',
    level      TINYINT      NOT NULL COMMENT '层级: 1/2',
    sort_order INT          DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='完整度分类表';

-- ============================================
-- 5. 检测分析模块
-- ============================================

CREATE TABLE IF NOT EXISTS detection_analysis (
    id                INT           PRIMARY KEY AUTO_INCREMENT,
    serial_number     VARCHAR(50)   COMMENT '编号',
    artifact_code     VARCHAR(100)  COMMENT '文物编号',
    artifact_name     VARCHAR(200)  COMMENT '文物名称',
    excavation_relic  VARCHAR(200)  COMMENT '出土遗迹',
    sample_position   VARCHAR(200)  COMMENT '取样/取样位置',
    sample_material   VARCHAR(200)  COMMENT '样品材质',
    sample_status     VARCHAR(100)  COMMENT '样品状态',
    sample_quantity   VARCHAR(50)   COMMENT '样品数量',
    sample_method     VARCHAR(200)  COMMENT '取样方法',
    purpose           VARCHAR(200)  COMMENT '目的',
    storage_location  VARCHAR(200)  COMMENT '存放位置',
    departure_time    VARCHAR(50)   COMMENT '发出时间',
    destination       VARCHAR(200)  COMMENT '去向',
    sample_photo      VARCHAR(500)  COMMENT '取样照片',
    analysis_data     TEXT          COMMENT '分析数据',
    analysis_report   TEXT          COMMENT '分析报告',
    manager           VARCHAR(100)  COMMENT '管理人/负责人',
    sampler           VARCHAR(100)  COMMENT '取样人',
    notes             VARCHAR(500)  COMMENT '备注（归还等）',
    create_time       DATETIME,
    update_time       DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测分析表';

-- ============================================
-- 6. 墓葬工作流程模块
-- ============================================

-- 6.1 流程树
CREATE TABLE IF NOT EXISTS workflow_tree (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    burial_id   INT           COMMENT '所属墓葬ID',
    label       VARCHAR(100)  NOT NULL COMMENT '流程名称',
    sort_order  INT           DEFAULT 0 COMMENT '排序',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓葬工作流程树';

-- 6.2 时间轴
CREATE TABLE IF NOT EXISTS workflow_timeline (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    burial_id   INT           COMMENT '所属墓葬ID',
    flow_id     INT           NOT NULL COMMENT '所属流程ID',
    event_date  DATE          COMMENT '日期',
    title       VARCHAR(200)  COMMENT '标题',
    status      VARCHAR(20)   DEFAULT 'pending' COMMENT '状态: done/doing/pending',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓葬工作流时间轴';

-- 6.3 工作记录（备注）
CREATE TABLE IF NOT EXISTS workflow_note (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT          NOT NULL COMMENT '所属时间轴ID',
    note_type   VARCHAR(50)  COMMENT '备注类型',
    content     TEXT         COMMENT '内容',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓葬工作流备注';

-- 6.4 媒体文件
CREATE TABLE IF NOT EXISTS workflow_media (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT           NOT NULL COMMENT '所属时间轴ID',
    media_type  VARCHAR(50)   COMMENT '媒体类型',
    file_name   VARCHAR(200)  COMMENT '文件名',
    file_url    VARCHAR(500)  COMMENT '文件URL',
    description VARCHAR(500)  COMMENT '描述',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓葬工作流媒体文件';

-- ============================================
-- 7. 棺工作流程模块
-- ============================================

-- 7.1 流程树
CREATE TABLE IF NOT EXISTS coffin_workflow_tree (
    id         INT          PRIMARY KEY AUTO_INCREMENT,
    coffin_id  INT          NOT NULL COMMENT '所属棺ID',
    label      VARCHAR(255) COMMENT '流程名称',
    sort_order INT          COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棺工作流程树';

-- 7.2 时间轴
CREATE TABLE IF NOT EXISTS coffin_workflow_timeline (
    id         INT          PRIMARY KEY AUTO_INCREMENT,
    coffin_id  INT          NOT NULL COMMENT '所属棺ID',
    flow_id    INT          COMMENT '所属流程ID',
    event_date DATE         COMMENT '日期',
    title      VARCHAR(255) COMMENT '标题',
    status     VARCHAR(50)  DEFAULT 'pending' COMMENT '状态: done/doing/pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棺工作流时间轴';

-- 7.3 工作记录（备注）
CREATE TABLE IF NOT EXISTS coffin_workflow_note (
    id          INT         PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT         NOT NULL COMMENT '所属时间轴ID',
    note_type   VARCHAR(50) COMMENT '备注类型',
    content     TEXT        COMMENT '内容',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棺工作流备注';

-- 7.4 媒体文件
CREATE TABLE IF NOT EXISTS coffin_workflow_media (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT           NOT NULL COMMENT '所属时间轴ID',
    media_type  VARCHAR(50)   COMMENT '媒体类型',
    file_name   VARCHAR(255)  COMMENT '文件名',
    file_url    VARCHAR(500)  COMMENT '文件URL',
    description VARCHAR(500)  COMMENT '描述',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棺工作流媒体文件';

-- ============================================
-- 8. 车工作流程模块
-- ============================================

-- 8.1 流程树
CREATE TABLE IF NOT EXISTS chariot_workflow_tree (
    id            INT          PRIMARY KEY AUTO_INCREMENT,
    burial_id     INT          NOT NULL COMMENT '所属墓葬ID',
    chariot_index INT          NOT NULL DEFAULT 1 COMMENT '车序号（1=车1, 2=车2...）',
    label         VARCHAR(255) COMMENT '流程名称',
    sort_order    INT          COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车工作流程树';

-- 8.2 时间轴
CREATE TABLE IF NOT EXISTS chariot_workflow_timeline (
    id            INT          PRIMARY KEY AUTO_INCREMENT,
    burial_id     INT          NOT NULL COMMENT '所属墓葬ID',
    chariot_index INT          NOT NULL DEFAULT 1 COMMENT '车序号',
    flow_id       INT          COMMENT '所属流程ID',
    event_date    DATE         COMMENT '日期',
    title         VARCHAR(255) COMMENT '标题',
    status        VARCHAR(50)  DEFAULT 'pending' COMMENT '状态: done/doing/pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车工作流时间轴';

-- 8.3 工作记录（备注）
CREATE TABLE IF NOT EXISTS chariot_workflow_note (
    id          INT         PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT         NOT NULL COMMENT '所属时间轴ID',
    note_type   VARCHAR(50) COMMENT '备注类型',
    content     TEXT        COMMENT '内容',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车工作流备注';

-- 8.4 媒体文件
CREATE TABLE IF NOT EXISTS chariot_workflow_media (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    timeline_id INT           NOT NULL COMMENT '所属时间轴ID',
    media_type  VARCHAR(50)   COMMENT '媒体类型',
    file_name   VARCHAR(255)  COMMENT '文件名',
    file_url    VARCHAR(500)  COMMENT '文件URL',
    description VARCHAR(500)  COMMENT '描述',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车工作流媒体文件';

-- ============================================
-- 9. 仪器分析模块
-- ============================================

CREATE TABLE IF NOT EXISTS lab_instruments (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(200) NOT NULL COMMENT '实验名字',
    image       VARCHAR(500) COMMENT '实验图片URL',
    scope       VARCHAR(500) COMMENT '实验范围',
    location    VARCHAR(200) COMMENT '实验地点',
    model       TEXT         COMMENT '仪器型号',
    conditions  TEXT         COMMENT '实验条件',
    method      VARCHAR(500) COMMENT '实验方法文档URL',
    method_name VARCHAR(200) COMMENT '实验方法文档名',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪器分析表';
