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
    instrument_name   VARCHAR(200)  COMMENT '仪器名称',
    instrument_model  VARCHAR(200)  COMMENT '仪器型号',
    test_params       TEXT          COMMENT '测试参数',
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

-- ============================================
-- 10. 分析结果模块
-- ============================================

-- 10.1 分析结果表（检测分析结果页面卡片数据）
CREATE TABLE IF NOT EXISTS analysis_results (
    id                 INT          PRIMARY KEY AUTO_INCREMENT,
    detection_id       INT          NOT NULL COMMENT '关联检测分析ID',
    artifact_code      VARCHAR(100) COMMENT '文物编号（自动写入）',
    artifact_name      VARCHAR(200) COMMENT '文物名称（自动写入）',
    sample_photo       VARCHAR(500) COMMENT '样品照片（自动写入）',
    experiment_method  VARCHAR(200) COMMENT '实验方法（自动写入=总览目的字段）',
    detection_purpose  TEXT         COMMENT '检测目的（用户填写）',
    instrument_model   VARCHAR(200) COMMENT '仪器型号（用户填写）',
    test_params        TEXT         COMMENT '测试参数（用户填写）',
    create_time        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析结果表';

-- 10.2 实验结果表
CREATE TABLE IF NOT EXISTS experiment_results (
    id              INT          PRIMARY KEY AUTO_INCREMENT,
    detection_id    INT          NOT NULL COMMENT '关联检测分析ID',
    experiment_name VARCHAR(200) COMMENT '实验名称',
    status          VARCHAR(20)  DEFAULT '待检测' COMMENT '待检测/检测中/待审核/已完成',
    result_data     TEXT         COMMENT '实验结果数据JSON',
    images          TEXT         COMMENT '图片URL列表JSON数组',
    attachments     TEXT         COMMENT '附件列表JSON数组',
    notes           TEXT         COMMENT '工作备注',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验结果表';

-- ============================================
-- 11. 文物保护修复模块
-- ============================================

-- 11.1 保护修复项目主表
CREATE TABLE IF NOT EXISTS conservation_project (
    id                 INT          PRIMARY KEY AUTO_INCREMENT,
    project_code       VARCHAR(50)  COMMENT '项目编号',
    project_name       VARCHAR(200) COMMENT '项目名称',
    artifact_id        INT          COMMENT '关联文物ID',
    artifact_code      VARCHAR(100) COMMENT '用户录入的文物编号',
    artifact_name      VARCHAR(200) COMMENT '用户录入的文物名称',
    source_project_id  INT          COMMENT '来源保护修复项目ID',
    source_alert_id    BIGINT       COMMENT '来源监测预警ID',
    source_monitoring_record_id BIGINT COMMENT '来源监测记录ID',
    project_type       VARCHAR(20)  DEFAULT '综合' COMMENT '项目类型：保护/修复/复原/综合',
    status             VARCHAR(20)  DEFAULT 'draft' COMMENT '项目状态：draft/active/completed/suspended/archived',
    current_stage      VARCHAR(30)  DEFAULT 'pendingSurvey' COMMENT '当前阶段',
    risk_level         VARCHAR(20)  DEFAULT 'medium' COMMENT '风险等级：high/medium/low',
    progress           INT          DEFAULT 0 COMMENT '项目进度 0-100',
    principal          VARCHAR(100) COMMENT '项目负责人',
    department         VARCHAR(100) COMMENT '执行部门',
    start_date         DATE         COMMENT '开始日期',
    expected_end_date  DATE         COMMENT '预计完成日期',
    actual_end_date    DATE         COMMENT '实际完成日期',
    summary            TEXT         COMMENT '项目摘要',
    create_time        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复项目表';

-- 11.2 病害类型字典表
CREATE TABLE IF NOT EXISTS conservation_disease_type (
    id                  INT          PRIMARY KEY AUTO_INCREMENT,
    code                VARCHAR(50)  NOT NULL COMMENT '类型编码',
    name                VARCHAR(100) NOT NULL COMMENT '病害名称',
    category            VARCHAR(30)  COMMENT '类别：physical/chemical/biological/structural',
    applicable_material VARCHAR(500) COMMENT '适用材质',
    description         TEXT         COMMENT '描述',
    default_risk_weight INT          DEFAULT 1 COMMENT '默认风险权重',
    enabled             TINYINT      DEFAULT 1,
    sort_order          INT          DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害类型字典表';

-- 11.3 病害调查主表
CREATE TABLE IF NOT EXISTS conservation_disease_survey (
    id                     INT          PRIMARY KEY AUTO_INCREMENT,
    project_id             INT          NOT NULL COMMENT '关联保护修复项目ID',
    survey_code            VARCHAR(50)  COMMENT '调查编号',
    survey_date            DATE         COMMENT '调查日期',
    surveyor               VARCHAR(100) COMMENT '调查人',
    survey_location        VARCHAR(200) COMMENT '调查地点',
    preservation_status    VARCHAR(30)  COMMENT '整体保存状态：good/fair/poor/critical',
    structural_stability   VARCHAR(30)  COMMENT '结构稳定性：stable/partially_unstable/unstable/dangerous',
    environment_summary    TEXT         COMMENT '环境摘要',
    overall_risk_level     VARCHAR(20)  COMMENT '综合风险等级',
    summary                TEXT         COMMENT '调查总结',
    status                 VARCHAR(20)  DEFAULT 'draft' COMMENT '数据状态：draft/submitted',
    create_time            DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time            DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                TINYINT      DEFAULT 0,
    INDEX idx_survey_project (project_id),
    INDEX idx_survey_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害调查主表';

-- 11.4 病害明细记录表
CREATE TABLE IF NOT EXISTS conservation_disease_record (
    id                  INT            PRIMARY KEY AUTO_INCREMENT,
    survey_id           INT            NOT NULL COMMENT '所属病害调查ID',
    project_id          INT            NOT NULL COMMENT '关联保护修复项目ID',
    disease_type_id     INT            COMMENT '病害类型ID',
    disease_type        VARCHAR(100)   COMMENT '用户输入的病害类型',
    disease_name        VARCHAR(100)   COMMENT '病害名称',
    disease_category    VARCHAR(30)    COMMENT '病害类别',
    severity            VARCHAR(20)    COMMENT '严重程度：minor/moderate/severe/critical',
    development_status  VARCHAR(30)    COMMENT '发展状态',
    extent_value        DECIMAL(10,2)  COMMENT '病害范围值',
    extent_unit         VARCHAR(20)    COMMENT '范围单位',
    part_name           VARCHAR(100)   COMMENT '部位名称',
    side                VARCHAR(30)    COMMENT '方位',
    position_description VARCHAR(500)  COMMENT '位置描述',
    morphology          TEXT           COMMENT '表观形态',
    cause_factors_json  JSON           COMMENT '成因因素多选结果',
    cause_analysis      TEXT           COMMENT '成因分析',
    structural_impact   VARCHAR(30)    COMMENT '结构影响',
    emergency           TINYINT        DEFAULT 0 COMMENT '是否紧急',
    recommended_action  TEXT           COMMENT '初步处理建议',
    sort_order          INT            DEFAULT 0,
    create_time         DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT        DEFAULT 0,
    INDEX idx_disease_survey (survey_id),
    INDEX idx_disease_project (project_id),
    INDEX idx_disease_type (disease_type_id),
    INDEX idx_disease_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害明细记录表';

-- 11.4.1 病害调查影像与标注。文件二进制和标注 JSON 均直接存入 MySQL。
CREATE TABLE IF NOT EXISTS conservation_disease_media (
    id                BIGINT       PRIMARY KEY AUTO_INCREMENT,
    disease_record_id INT          NOT NULL COMMENT '关联病害记录ID',
    survey_id         INT          NOT NULL COMMENT '关联病害调查ID',
    project_id        INT          NOT NULL COMMENT '关联保护修复项目ID',
    file_name         VARCHAR(255) NOT NULL,
    content_type      VARCHAR(120) NOT NULL,
    file_size         BIGINT       NOT NULL,
    file_data         LONGBLOB     NOT NULL,
    title             VARCHAR(255),
    description       VARCHAR(1000),
    annotations_json  JSON         COMMENT '归一化矩形标注数组',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_disease_media_record (disease_record_id),
    INDEX idx_disease_media_survey (survey_id),
    INDEX idx_disease_media_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害调查影像与标注表';

-- 11.5 档案后续保存建议（监测计划来源）
CREATE TABLE IF NOT EXISTS conservation_archive_advice (
    id                     BIGINT       PRIMARY KEY AUTO_INCREMENT,
    project_id             INT          NOT NULL,
    temperature_range      VARCHAR(100),
    humidity_range         VARCHAR(100),
    lighting               VARCHAR(255),
    air_quality            VARCHAR(255),
    packaging              VARCHAR(255),
    handling               VARCHAR(255),
    shockproof             VARCHAR(255),
    review_cycle           VARCHAR(255),
    monitor_diseases       TEXT,
    monitoring_indicators  TEXT,
    follow_up_advice       TEXT,
    warning_conditions     TEXT,
    create_time            DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_archive_advice_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复档案后续保存建议';

-- 11.6 修复后对比来源（仅保存监测需要的结构化结果）
-- 保护修复档案主表。复杂编制内容保留为 JSON，核心检索字段保持结构化。
CREATE TABLE IF NOT EXISTS conservation_archive (
    id                    BIGINT       PRIMARY KEY AUTO_INCREMENT,
    project_id            INT          NOT NULL,
    artifact_id           INT,
    archive_code          VARCHAR(80)  NOT NULL,
    archive_title         VARCHAR(255) NOT NULL,
    archive_type          VARCHAR(30)  DEFAULT 'comprehensive',
    compiler              VARCHAR(100),
    archive_status        VARCHAR(30)  DEFAULT 'compiling' COMMENT '数据状态：compiling/completed/archived',
    current_version       VARCHAR(30)  DEFAULT 'V1.0',
    executive_summary     TEXT,
    protection_goal       TEXT,
    conservation_basis    TEXT,
    final_conclusion      TEXT,
    source_survey_id      BIGINT,
    source_survey_version VARCHAR(30),
    compiled_date         DATE,
    completeness_rate     INT          DEFAULT 0,
    workspace_json        JSON,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               TINYINT  DEFAULT 0,
    UNIQUE KEY uk_archive_project (project_id),
    UNIQUE KEY uk_archive_code (archive_code),
    INDEX idx_archive_status (archive_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物保护修复档案';

-- 档案版本快照。定稿和人工生成版本时保存不可变快照。
CREATE TABLE IF NOT EXISTS conservation_archive_revision (
    id                      BIGINT      PRIMARY KEY AUTO_INCREMENT,
    archive_id              BIGINT      NOT NULL,
    version_no              VARCHAR(30) NOT NULL,
    revision_type           VARCHAR(30) DEFAULT 'manual',
    revision_description    VARCHAR(1000),
    operator                VARCHAR(100),
    archive_snapshot_json   JSON,
    workspace_snapshot_json JSON,
    create_time             DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_archive_revision_parent (archive_id),
    INDEX idx_archive_revision_version (archive_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复档案版本快照';

-- 档案附件。文件二进制直接存入 MySQL。
CREATE TABLE IF NOT EXISTS conservation_archive_attachment (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    archive_id     BIGINT       NOT NULL,
    project_id     INT          NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_type      VARCHAR(50),
    content_type   VARCHAR(120),
    file_size      BIGINT,
    file_data      LONGBLOB     NOT NULL,
    source_module  VARCHAR(100),
    section_name   VARCHAR(100),
    version_no     VARCHAR(30),
    description    VARCHAR(1000),
    uploaded_by    VARCHAR(100),
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_archive_attachment_parent (archive_id),
    INDEX idx_archive_attachment_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复档案附件';

-- 修复执行过程主表。
CREATE TABLE IF NOT EXISTS conservation_process (
    id                BIGINT       PRIMARY KEY AUTO_INCREMENT,
    project_id        INT          NOT NULL,
    archive_id        BIGINT       COMMENT '可选关联档案ID，草稿过程可为空',
    process_code      VARCHAR(80)  NOT NULL,
    process_name      VARCHAR(255) NOT NULL,
    process_status    VARCHAR(30)  DEFAULT 'not_started',
    execution_mode    VARCHAR(30)  DEFAULT 'formal',
    supervisor        VARCHAR(100),
    start_date        DATE,
    expected_end_date DATE,
    actual_end_date   DATE,
    total_steps       INT          DEFAULT 0,
    completed_steps   INT          DEFAULT 0,
    progress          INT          DEFAULT 0,
    execution_summary TEXT,
    final_result      TEXT,
    pause_json        JSON,
    create_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT  DEFAULT 0,
    UNIQUE KEY uk_process_project (project_id),
    UNIQUE KEY uk_process_code (process_code),
    INDEX idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复执行过程';

-- 修复步骤表。可检索字段结构化，详细执行记录保留为 JSON。
CREATE TABLE IF NOT EXISTS conservation_process_step (
    id                 BIGINT       PRIMARY KEY,
    process_id         BIGINT       NOT NULL,
    project_id         INT          NOT NULL,
    step_code          VARCHAR(60)  NOT NULL,
    step_name          VARCHAR(255) NOT NULL,
    step_type          VARCHAR(50),
    step_status        VARCHAR(30)  DEFAULT 'pending',
    sequence_no        INT          DEFAULT 0,
    progress_weight    INT          DEFAULT 10,
    operator_name      VARCHAR(100),
    target_part        VARCHAR(255),
    planned_start_time DATETIME,
    planned_end_time   DATETIME,
    actual_start_time  DATETIME,
    actual_end_time    DATETIME,
    completion_rate    INT          DEFAULT 0,
    temporary_step     TINYINT      DEFAULT 0,
    requires_monitoring TINYINT     DEFAULT 0,
    step_json          JSON         NOT NULL,
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_process_step_parent (process_id),
    INDEX idx_process_step_project (project_id),
    INDEX idx_process_step_status (step_status),
    INDEX idx_process_step_sequence (process_id, sequence_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保护修复执行步骤';

-- 修复过程影像，文件内容直接保存到 MySQL。
CREATE TABLE IF NOT EXISTS conservation_process_media (
    id                       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    process_id               BIGINT       NOT NULL,
    step_id                  BIGINT       NOT NULL,
    project_id               INT          NOT NULL,
    media_stage              VARCHAR(30),
    original_name            VARCHAR(255) NOT NULL,
    content_type             VARCHAR(120),
    file_size                BIGINT,
    file_data                LONGBLOB     NOT NULL,
    title                    VARCHAR(255),
    shooting_time            DATETIME,
    shooting_position        VARCHAR(150),
    target_part              VARCHAR(150),
    photographer             VARCHAR(100),
    description              VARCHAR(1000),
    selected_for_comparison  TINYINT DEFAULT 0,
    selected_for_archive     TINYINT DEFAULT 0,
    selected_for_restoration TINYINT DEFAULT 0,
    create_time              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_process_media_step (step_id),
    INDEX idx_process_media_parent (process_id),
    INDEX idx_process_media_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修复过程影像';

CREATE TABLE IF NOT EXISTS conservation_comparison (
    id                              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    project_id                      INT          NOT NULL,
    artifact_id                     INT,
    process_id                      BIGINT,
    step_id                         BIGINT,
    comparison_code                 VARCHAR(60),
    comparison_title                VARCHAR(255) NOT NULL,
    comparison_type                 VARCHAR(40) DEFAULT 'before_after',
    target_part                     VARCHAR(150),
    shooting_position               VARCHAR(150),
    before_summary                  TEXT,
    after_summary                   TEXT,
    comparison_description          TEXT,
    overall_effect                  VARCHAR(40),
    remaining_issue                 TEXT,
    monitoring_review_part          VARCHAR(150),
    monitoring_notes                TEXT,
    evaluator                       VARCHAR(100),
    evaluation_date                 DATE,
    comparison_status               VARCHAR(30) DEFAULT 'draft',
    overall_comparison              TINYINT DEFAULT 0,
    no_applicable_metrics           TINYINT DEFAULT 0,
    selected_for_archive            TINYINT DEFAULT 0,
    selected_for_restoration        TINYINT DEFAULT 0,
    selected_as_monitoring_baseline TINYINT DEFAULT 0,
    comparison_json                 JSON NOT NULL,
    create_time                     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time                     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comparison_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修复前后对比监测来源';

CREATE TABLE IF NOT EXISTS conservation_comparison_metric (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    comparison_id     BIGINT NOT NULL,
    metric_name       VARCHAR(150) NOT NULL,
    metric_category   VARCHAR(50),
    before_value      DECIMAL(15,4),
    after_value       DECIMAL(15,4),
    value_unit        VARCHAR(30),
    expected_direction VARCHAR(30),
    result_status     VARCHAR(30),
    description       VARCHAR(1000),
    INDEX idx_comparison_metric_parent (comparison_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前后对比量化指标';

CREATE TABLE IF NOT EXISTS conservation_comparison_media (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    comparison_id     BIGINT NOT NULL,
    source_media_id   BIGINT,
    image_stage       VARCHAR(30),
    image_role        VARCHAR(30),
    original_name     VARCHAR(255),
    content_type      VARCHAR(100),
    file_size         BIGINT,
    file_data         LONGBLOB,
    target_part       VARCHAR(150),
    shooting_position VARCHAR(150),
    shooting_time     DATETIME,
    photographer      VARCHAR(100),
    description       TEXT,
    sequence_no       INT DEFAULT 0,
    is_primary        TINYINT DEFAULT 0,
    source_module     VARCHAR(100),
    INDEX idx_comparison_media_parent (comparison_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前后对比影像';

-- 11.7 文物复原成果
CREATE TABLE IF NOT EXISTS conservation_restoration_result (
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id               INT NOT NULL,
    artifact_id              INT,
    archive_id               BIGINT,
    process_id               BIGINT,
    comparison_id            BIGINT,
    result_code              VARCHAR(60) NOT NULL,
    result_name              VARCHAR(255) NOT NULL,
    restoration_type         VARCHAR(40) NOT NULL,
    restoration_category     VARCHAR(50),
    target_part              VARCHAR(200),
    restoration_scope        TEXT,
    restoration_purpose      TEXT,
    basis_summary            TEXT,
    method_summary           TEXT,
    result_summary           TEXT,
    uncertainty_summary      TEXT,
    evidence_level           VARCHAR(20),
    confidence_level         VARCHAR(20),
    result_status            VARCHAR(30) DEFAULT 'draft',
    current_version          VARCHAR(30) DEFAULT 'V1.0',
    completion_rate          DECIMAL(5,2) DEFAULT 0,
    overall_score            DECIMAL(5,2),
    evaluation_conclusion    TEXT,
    final_conclusion         TEXT,
    selected_for_archive     TINYINT DEFAULT 0,
    recommended_result       TINYINT DEFAULT 0,
    requires_monitoring      TINYINT DEFAULT 0,
    principal                VARCHAR(100),
    participant_names        VARCHAR(500),
    start_date               DATE,
    completion_date          DATE,
    monitoring_indicators    TEXT,
    monitoring_cycle         VARCHAR(100),
    monitoring_baseline_id   BIGINT,
    warning_conditions       TEXT,
    monitoring_note          TEXT,
    step_ids_json            JSON,
    comparison_ids_json      JSON,
    disease_ids_json         JSON,
    detection_ids_json       JSON,
    method_parameters_json   JSON,
    evaluation_json          JSON,
    deleted                  TINYINT DEFAULT 0,
    create_time              DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_restoration_result_code (result_code),
    INDEX idx_restoration_project (project_id),
    INDEX idx_restoration_status (result_status),
    INDEX idx_restoration_type (restoration_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物复原成果';

CREATE TABLE IF NOT EXISTS conservation_restoration_part (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id               BIGINT NOT NULL,
    part_code               VARCHAR(60),
    part_name               VARCHAR(255) NOT NULL,
    part_type               VARCHAR(40) NOT NULL,
    target_location         VARCHAR(300),
    scope_description       TEXT,
    material_name           VARCHAR(255),
    technique               VARCHAR(300),
    evidence_level          VARCHAR(20),
    confidence_level        VARCHAR(20),
    removable               TINYINT DEFAULT 0,
    reversible              TINYINT DEFAULT 0,
    reversible_description  TEXT,
    distinguishable         TINYINT DEFAULT 1,
    display_style           VARCHAR(100),
    annotation_text         TEXT,
    percentage_value        DECIMAL(5,2),
    sort_order              INT DEFAULT 0,
    model_layer             TINYINT DEFAULT 0,
    model_object_name       VARCHAR(255),
    layer_visible           TINYINT DEFAULT 1,
    annotation_position_json JSON,
    selected_for_monitoring TINYINT DEFAULT 0,
    create_time             DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_restoration_part_result (result_id),
    INDEX idx_restoration_part_type (part_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复原成果组成部分';

CREATE TABLE IF NOT EXISTS conservation_restoration_source (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id             BIGINT NOT NULL,
    part_id               BIGINT,
    source_type           VARCHAR(40),
    source_title          VARCHAR(255),
    business_type         VARCHAR(50),
    business_id           BIGINT,
    support_description   TEXT,
    reliability           VARCHAR(20),
    evidence_level        VARCHAR(20),
    file_name             VARCHAR(255),
    file_url              VARCHAR(500),
    sort_order            INT DEFAULT 0,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_restoration_source_result (result_id),
    INDEX idx_restoration_source_part (part_id),
    INDEX idx_restoration_source_business (business_type,business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复原依据来源';

CREATE TABLE IF NOT EXISTS conservation_restoration_media (
    id                              BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id                       BIGINT NOT NULL,
    part_id                         BIGINT,
    source_media_id                 BIGINT,
    source_business_type            VARCHAR(50) DEFAULT 'restoration',
    source_business_id              BIGINT,
    media_stage                     VARCHAR(30),
    media_type                      VARCHAR(30),
    original_name                   VARCHAR(255),
    content_type                    VARCHAR(100),
    file_size                       BIGINT,
    file_data                       LONGBLOB,
    title                           VARCHAR(255),
    description                     TEXT,
    is_primary                      TINYINT DEFAULT 0,
    selected_for_archive            TINYINT DEFAULT 0,
    selected_as_monitoring_baseline TINYINT DEFAULT 0,
    sort_order                      INT DEFAULT 0,
    create_time                     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_restoration_media_result (result_id),
    INDEX idx_restoration_media_source (source_business_type,source_media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复原成果影像';

CREATE TABLE IF NOT EXISTS conservation_restoration_model (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id             BIGINT NOT NULL,
    model_name            VARCHAR(255),
    model_type            VARCHAR(40),
    original_name         VARCHAR(255),
    content_type          VARCHAR(100),
    file_size             BIGINT,
    file_data             LONGBLOB,
    file_format           VARCHAR(30),
    scale_unit            VARCHAR(30),
    coordinate_system     VARCHAR(100),
    polygon_count         BIGINT,
    texture_count         INT,
    model_stage           VARCHAR(30),
    model_description     TEXT,
    supports_layer        TINYINT DEFAULT 0,
    supports_annotation   TINYINT DEFAULT 0,
    is_primary            TINYINT DEFAULT 0,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_restoration_model_result (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复原成果三维模型';

CREATE TABLE IF NOT EXISTS conservation_restoration_version (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id             BIGINT NOT NULL,
    version_no            VARCHAR(30),
    version_name          VARCHAR(255),
    version_type          VARCHAR(30),
    change_description    TEXT,
    evidence_level        VARCHAR(20),
    confidence_level      VARCHAR(20),
    is_current            TINYINT DEFAULT 0,
    is_recommended        TINYINT DEFAULT 0,
    archived              TINYINT DEFAULT 0,
    snapshot_json         JSON,
    creator               VARCHAR(100),
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_restoration_version_result (result_id),
    INDEX idx_restoration_version_no (version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复原成果版本';

-- ============================================
-- 12. 后续监测与风险预警模块
-- ============================================

CREATE TABLE IF NOT EXISTS monitoring_plan (
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id               INT NOT NULL,
    artifact_id              INT,
    archive_id               BIGINT,
    plan_code                VARCHAR(60) NOT NULL,
    plan_name                VARCHAR(255) NOT NULL,
    plan_type                VARCHAR(40) DEFAULT 'comprehensive',
    plan_status              VARCHAR(30) DEFAULT 'draft',
    monitoring_purpose       TEXT,
    monitoring_scope         TEXT,
    overall_strategy         TEXT,
    responsible_person       VARCHAR(100),
    participant_names        VARCHAR(255),
    monitoring_location      VARCHAR(255),
    start_date               DATE,
    expected_end_date        DATE,
    next_monitoring_date     DATE,
    default_frequency_value  INT DEFAULT 1,
    default_frequency_unit   VARCHAR(20) DEFAULT 'month',
    auto_generate_task       TINYINT DEFAULT 1,
    alert_enabled            TINYINT DEFAULT 1,
    execution_count          INT DEFAULT 0,
    overdue_count            INT DEFAULT 0,
    completion_rate          DECIMAL(6,2) DEFAULT 0,
    create_time              DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                  TINYINT DEFAULT 0,
    UNIQUE KEY uk_monitoring_plan_code (plan_code),
    INDEX idx_monitoring_plan_project (project_id),
    INDEX idx_monitoring_plan_status (plan_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后续监测计划';

CREATE TABLE IF NOT EXISTS monitoring_target (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id               BIGINT NOT NULL,
    project_id            INT NOT NULL,
    target_type           VARCHAR(40) NOT NULL,
    target_name           VARCHAR(255) NOT NULL,
    source_business_type  VARCHAR(50),
    source_business_id    BIGINT,
    target_part           VARCHAR(150),
    target_location       VARCHAR(500),
    risk_level            VARCHAR(20) DEFAULT 'medium',
    priority_level        VARCHAR(20) DEFAULT 'medium',
    monitoring_reason     TEXT,
    current_status        VARCHAR(100),
    requires_image        TINYINT DEFAULT 0,
    shooting_position     VARCHAR(150),
    enabled               TINYINT DEFAULT 1,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               TINYINT DEFAULT 0,
    INDEX idx_monitoring_target_plan (plan_id),
    INDEX idx_monitoring_target_project (project_id),
    INDEX idx_monitoring_target_source (source_business_type, source_business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测对象';

CREATE TABLE IF NOT EXISTS monitoring_indicator (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_id             BIGINT NOT NULL,
    plan_id               BIGINT NOT NULL,
    indicator_code        VARCHAR(60),
    indicator_name        VARCHAR(150) NOT NULL,
    indicator_category    VARCHAR(50),
    data_type             VARCHAR(30) DEFAULT 'number',
    value_unit            VARCHAR(30),
    baseline_value        DECIMAL(15,4),
    normal_min            DECIMAL(15,4),
    normal_max            DECIMAL(15,4),
    warning_min           DECIMAL(15,4),
    warning_max           DECIMAL(15,4),
    critical_min          DECIMAL(15,4),
    critical_max          DECIMAL(15,4),
    change_warning_value  DECIMAL(15,4),
    change_warning_rate   DECIMAL(10,2),
    expected_direction    VARCHAR(30),
    observation_method    VARCHAR(255),
    instrument_name       VARCHAR(255),
    required_flag         TINYINT DEFAULT 1,
    sort_order            INT DEFAULT 0,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_monitoring_indicator_target (target_id),
    INDEX idx_monitoring_indicator_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测指标与阈值';

CREATE TABLE IF NOT EXISTS monitoring_baseline (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id               BIGINT NOT NULL,
    target_id             BIGINT NOT NULL,
    indicator_id          BIGINT,
    source_business_type  VARCHAR(50),
    source_business_id    BIGINT,
    baseline_date         DATE,
    baseline_value        DECIMAL(15,4),
    baseline_unit         VARCHAR(30),
    baseline_status       VARCHAR(255),
    baseline_description  TEXT,
    baseline_media_id     BIGINT,
    version_no            VARCHAR(30) DEFAULT 'V1.0',
    is_current            TINYINT DEFAULT 1,
    created_by            VARCHAR(100),
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_monitoring_baseline_target (target_id),
    INDEX idx_monitoring_baseline_current (target_id, is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本化监测基准';

CREATE TABLE IF NOT EXISTS monitoring_task (
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id                  BIGINT NOT NULL,
    project_id               INT NOT NULL,
    task_code                VARCHAR(60) NOT NULL,
    task_name                VARCHAR(255) NOT NULL,
    task_type                VARCHAR(30) DEFAULT 'routine',
    task_status              VARCHAR(30) DEFAULT 'pending',
    planned_date             DATE,
    due_date                 DATE,
    actual_start_time        DATETIME,
    actual_end_time          DATETIME,
    responsible_person       VARCHAR(100),
    participant_names        VARCHAR(255),
    target_count             INT DEFAULT 0,
    completed_target_count   INT DEFAULT 0,
    completion_rate          DECIMAL(6,2) DEFAULT 0,
    overall_result           VARCHAR(30),
    summary                  TEXT,
    generated_automatically  TINYINT DEFAULT 0,
    create_time              DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_monitoring_task_code (task_code),
    INDEX idx_monitoring_task_plan (plan_id),
    INDEX idx_monitoring_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测周期任务';

CREATE TABLE IF NOT EXISTS monitoring_record (
    id                          BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id                     BIGINT NOT NULL,
    plan_id                     BIGINT NOT NULL,
    target_id                   BIGINT NOT NULL,
    project_id                  INT NOT NULL,
    record_code                 VARCHAR(60) NOT NULL,
    monitoring_date             DATETIME,
    monitor_person              VARCHAR(100),
    monitoring_location         VARCHAR(255),
    overall_status              VARCHAR(30),
    comparison_result           VARCHAR(30),
    observation_description     TEXT,
    change_description          TEXT,
    result_conclusion           TEXT,
    requires_recheck            TINYINT DEFAULT 0,
    requires_intervention       TINYINT DEFAULT 0,
    requires_new_disease_survey TINYINT DEFAULT 0,
    requires_new_project        TINYINT DEFAULT 0,
    next_monitoring_date        DATE,
    monitoring_status           VARCHAR(30) DEFAULT 'draft',
    submitted_time              DATETIME,
    create_time                 DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time                 DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                     TINYINT DEFAULT 0,
    UNIQUE KEY uk_monitoring_record_code (record_code),
    INDEX idx_monitoring_record_task (task_id),
    INDEX idx_monitoring_record_target (target_id),
    INDEX idx_monitoring_record_date (monitoring_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测执行记录';

CREATE TABLE IF NOT EXISTS monitoring_value (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id             BIGINT NOT NULL,
    indicator_id          BIGINT NOT NULL,
    indicator_name        VARCHAR(150),
    value_number          DECIMAL(15,4),
    value_text            TEXT,
    value_unit            VARCHAR(30),
    baseline_value        DECIMAL(15,4),
    previous_value        DECIMAL(15,4),
    change_value          DECIMAL(15,4),
    change_rate           DECIMAL(10,2),
    result_level          VARCHAR(30),
    result_description    VARCHAR(500),
    manually_confirmed    TINYINT DEFAULT 0,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_monitoring_value_record_indicator (record_id, indicator_id),
    INDEX idx_monitoring_value_indicator (indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测指标实测值';

CREATE TABLE IF NOT EXISTS monitoring_media (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id            INT NOT NULL,
    plan_id               BIGINT,
    task_id               BIGINT,
    record_id             BIGINT NOT NULL,
    target_id             BIGINT,
    media_role            VARCHAR(30) DEFAULT 'current',
    original_name         VARCHAR(255) NOT NULL,
    content_type          VARCHAR(100),
    file_size             BIGINT,
    file_data             LONGBLOB NOT NULL,
    shooting_position     VARCHAR(150),
    shooting_time         DATETIME,
    title                 VARCHAR(255),
    description           TEXT,
    created_by            VARCHAR(100),
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_monitoring_media_record (record_id),
    INDEX idx_monitoring_media_target (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测影像与附件（文件存入MySQL）';

CREATE TABLE IF NOT EXISTS monitoring_alert (
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id               INT NOT NULL,
    plan_id                  BIGINT NOT NULL,
    task_id                  BIGINT,
    record_id                BIGINT,
    target_id                BIGINT NOT NULL,
    indicator_id             BIGINT,
    alert_code               VARCHAR(60) NOT NULL,
    alert_level              VARCHAR(30),
    alert_title              VARCHAR(255),
    alert_description        TEXT,
    trigger_type             VARCHAR(50),
    trigger_value            VARCHAR(100),
    threshold_description    VARCHAR(500),
    alert_status             VARCHAR(30) DEFAULT 'new',
    discovered_time          DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirmed_time           DATETIME,
    confirmed_by             VARCHAR(100),
    immediate_action         TEXT,
    treatment_advice         TEXT,
    requires_recheck         TINYINT DEFAULT 0,
    requires_disease_survey  TINYINT DEFAULT 0,
    requires_intervention    TINYINT DEFAULT 0,
    requires_new_project     TINYINT DEFAULT 0,
    created_project_id       INT COMMENT '由预警创建的保护修复项目ID',
    project_created_time     DATETIME COMMENT '保护修复项目创建时间',
    resolved_time            DATETIME,
    create_time              DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_monitoring_alert_code (alert_code),
    INDEX idx_monitoring_alert_project (project_id),
    INDEX idx_monitoring_alert_plan (plan_id),
    INDEX idx_monitoring_alert_status (alert_status),
    INDEX idx_monitoring_alert_created_project (created_project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测风险预警';

-- ============================================
-- 12. 数字档案中心
-- ============================================

CREATE TABLE IF NOT EXISTS digital_resource (
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_code      VARCHAR(60)  NOT NULL,
    resource_name      VARCHAR(255) NOT NULL,
    title              VARCHAR(255),
    original_file_name VARCHAR(255),
    resource_type      VARCHAR(30)  NOT NULL,
    source_module      VARCHAR(50),
    file_extension     VARCHAR(30),
    file_size          BIGINT DEFAULT 0,
    file_url           VARCHAR(1000),
    thumbnail_url      VARCHAR(1000),
    preview_url        VARCHAR(1000),
    resource_status    VARCHAR(30) DEFAULT 'normal',
    data_status        VARCHAR(30) DEFAULT 'incomplete',
    current_version    VARCHAR(30) DEFAULT 'V1.0',
    version_count      INT DEFAULT 1,
    uploaded_by        VARCHAR(100),
    upload_time        DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description        TEXT,
    keywords           TEXT,
    deleted            TINYINT DEFAULT 0,
    deleted_by         VARCHAR(100),
    delete_time        DATETIME,
    UNIQUE KEY uk_digital_resource_code (resource_code),
    INDEX idx_digital_resource_type (resource_type),
    INDEX idx_digital_resource_source (source_module),
    INDEX idx_digital_resource_status (resource_status),
    INDEX idx_digital_resource_deleted (deleted),
    INDEX idx_digital_resource_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源主表';

CREATE TABLE IF NOT EXISTS digital_resource_version (
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id        BIGINT NOT NULL,
    version_no         VARCHAR(30) NOT NULL,
    version_name       VARCHAR(255),
    version_type       VARCHAR(30),
    original_file_name VARCHAR(255),
    file_extension     VARCHAR(30),
    file_size          BIGINT DEFAULT 0,
    file_url           VARCHAR(1000),
    content_type       VARCHAR(100),
    version_status     VARCHAR(30) DEFAULT 'current',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_digital_resource_version_resource (resource_id),
    INDEX idx_digital_resource_version_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源版本记录';

CREATE TABLE IF NOT EXISTS digital_resource_relation (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id     BIGINT NOT NULL,
    relation_type   VARCHAR(50) NOT NULL,
    relation_id     BIGINT,
    relation_code   VARCHAR(60),
    relation_name   VARCHAR(255),
    is_primary      TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_digital_resource_relation_resource (resource_id),
    INDEX idx_digital_resource_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源关联关系';

CREATE TABLE IF NOT EXISTS digital_resource_tag (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name    VARCHAR(100) NOT NULL,
    tag_type    VARCHAR(50),
    deleted     TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_digital_resource_tag_type (tag_type),
    INDEX idx_digital_resource_tag_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源标签';

CREATE TABLE IF NOT EXISTS digital_resource_tag_relation (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_resource_tag_relation (resource_id, tag_id),
    INDEX idx_digital_resource_tag_relation_resource (resource_id),
    INDEX idx_digital_resource_tag_relation_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源标签关联';

CREATE TABLE IF NOT EXISTS digital_resource_operation_log (
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id            BIGINT NOT NULL,
    operation_type         VARCHAR(50) NOT NULL,
    operation_description  TEXT,
    operation_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_digital_resource_operation_log_resource (resource_id),
    INDEX idx_digital_resource_operation_log_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字资源操作日志';

CREATE TABLE IF NOT EXISTS digital_media_metadata (
    resource_id          BIGINT PRIMARY KEY,
    media_type           VARCHAR(30),
    media_title          VARCHAR(255),
    media_stage          VARCHAR(30),
    media_subtype        VARCHAR(30),
    primary_object_type  VARCHAR(50),
    primary_object_id    BIGINT,
    primary_object_code  VARCHAR(60),
    primary_object_name  VARCHAR(255),
    shooting_part        VARCHAR(100),
    shooting_angle       VARCHAR(100),
    shooting_date        DATE,
    photographer         VARCHAR(100),
    has_scale            TINYINT DEFAULT 0,
    scale_unit           VARCHAR(20),
    quality_level        VARCHAR(30),
    is_key_media         TINYINT DEFAULT 0,
    media_description    TEXT,
    metadata_status      VARCHAR(30) DEFAULT 'incomplete',
    sort_time            DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_time          DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_digital_media_metadata_object (primary_object_type, primary_object_id),
    INDEX idx_digital_media_metadata_stage (media_stage),
    INDEX idx_digital_media_metadata_status (metadata_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字媒体元数据';

CREATE TABLE IF NOT EXISTS digital_media_collection (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_code VARCHAR(60) NOT NULL,
    collection_name VARCHAR(255) NOT NULL,
    collection_type VARCHAR(30) DEFAULT 'custom',
    object_type     VARCHAR(50),
    object_id       BIGINT,
    item_count      INT DEFAULT 0,
    deleted         TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_media_collection_code (collection_code),
    INDEX idx_digital_media_collection_object (object_type, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字媒体集合';

CREATE TABLE IF NOT EXISTS digital_media_collection_item (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_id BIGINT NOT NULL,
    resource_id   BIGINT NOT NULL,
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_media_collection_item (collection_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字媒体集合条目';

CREATE TABLE IF NOT EXISTS digital_media_comparison (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    comparison_code       VARCHAR(60) NOT NULL,
    comparison_name       VARCHAR(255),
    object_type           VARCHAR(50),
    object_id             BIGINT,
    before_resource_id    BIGINT,
    after_resource_id     BIGINT,
    comparison_description TEXT,
    overall_effect        VARCHAR(100),
    deleted               TINYINT DEFAULT 0,
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_media_comparison_code (comparison_code),
    INDEX idx_digital_media_comparison_object (object_type, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修复前后对比';

CREATE TABLE IF NOT EXISTS digital_media_marker (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id   BIGINT NOT NULL,
    marker_type   VARCHAR(50),
    marker_name   VARCHAR(255),
    position_x    DECIMAL(10,6),
    position_y    DECIMAL(10,6),
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_digital_media_marker_resource (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字媒体标注';

CREATE TABLE IF NOT EXISTS digital_model_metadata (
    resource_id          BIGINT PRIMARY KEY,
    model_type           VARCHAR(30),
    model_stage          VARCHAR(30),
    model_format         VARCHAR(30),
    model_title          VARCHAR(255),
    primary_object_type  VARCHAR(50),
    primary_object_id    BIGINT,
    primary_object_code  VARCHAR(60),
    primary_object_name  VARCHAR(255),
    scale_unit           VARCHAR(20),
    real_scale           VARCHAR(50),
    acquisition_method   VARCHAR(100),
    acquisition_device   VARCHAR(100),
    acquisition_date     DATE,
    processing_software  VARCHAR(100),
    vertex_count         BIGINT,
    face_count           BIGINT,
    point_count          BIGINT,
    texture_count        BIGINT,
    layer_count          BIGINT,
    model_description    TEXT,
    quality_level        VARCHAR(30),
    metadata_status      VARCHAR(30) DEFAULT 'incomplete',
    preview_status       VARCHAR(30),
    is_key_model         TINYINT DEFAULT 0,
    sort_time            DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_time          DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_digital_model_metadata_object (primary_object_type, primary_object_id),
    INDEX idx_digital_model_metadata_stage (model_stage),
    INDEX idx_digital_model_metadata_status (metadata_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三维模型元数据';

CREATE TABLE IF NOT EXISTS digital_model_collection (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_code VARCHAR(60) NOT NULL,
    collection_name VARCHAR(255) NOT NULL,
    collection_type VARCHAR(30) DEFAULT 'custom',
    object_type     VARCHAR(50),
    object_id       BIGINT,
    item_count      INT DEFAULT 0,
    deleted         TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_model_collection_code (collection_code),
    INDEX idx_digital_model_collection_object (object_type, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三维模型集合';

CREATE TABLE IF NOT EXISTS digital_model_collection_item (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_id  BIGINT NOT NULL,
    resource_id    BIGINT NOT NULL,
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_digital_model_collection_item (collection_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三维模型集合条目';
