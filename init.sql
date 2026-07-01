-- ============================================
-- 苏尔汉河流域矿冶遗址数据库 初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS nwsite
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE nwsite;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS user (
    id          INT           PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)   NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    nickname    VARCHAR(10),
    email       VARCHAR(100),
    user_pic    VARCHAR(500)  COMMENT '用户头像地址',
    token_version INT         DEFAULT 0 COMMENT 'JWT token版本号，修改密码后+1',
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 文章分类表
CREATE TABLE IF NOT EXISTS category (
    id             INT          PRIMARY KEY AUTO_INCREMENT,
    category_name  VARCHAR(50)  NOT NULL,
    category_alias VARCHAR(50)  NOT NULL,
    create_user    INT,
    create_time    DATETIME,
    update_time    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 文章表
CREATE TABLE IF NOT EXISTS article (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(10)  NOT NULL,
    content     TEXT         NOT NULL,
    cover_img   VARCHAR(500),
    state       VARCHAR(10)  DEFAULT '草稿' COMMENT '发布状态: 已发布|草稿',
    category_id INT          NOT NULL,
    create_user INT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 遗址表（heritage_sites）
CREATE TABLE IF NOT EXISTS heritage_sites (
    id                INT            PRIMARY KEY AUTO_INCREMENT,
    site_code         VARCHAR(50)    COMMENT '遗址编号',
    name              VARCHAR(200)   NOT NULL COMMENT '遗址名称',
    alias             VARCHAR(200)   COMMENT '别名',
    location_province VARCHAR(50)    COMMENT '所在省份',
    location_city     VARCHAR(50)    COMMENT '所在城市',
    location_detail   VARCHAR(500)   COMMENT '详细地址',
    latitude          DOUBLE         COMMENT '纬度',
    longitude         DOUBLE         COMMENT '经度',
    era               VARCHAR(100)   COMMENT '年代',
    category          VARCHAR(100)   COMMENT '遗址类别',
    protection_level  VARCHAR(50)    COMMENT '保护级别',
    discovery_year    INT            COMMENT '发现年份',
    excavation_year   INT            COMMENT '发掘年份',
    area_size         DOUBLE         COMMENT '面积（平方米）',
    description       TEXT           COMMENT '遗址描述',
    cultural_value    TEXT           COMMENT '文化价值',
    current_status    VARCHAR(50)    COMMENT '现状',
    cover_image       VARCHAR(500)   COMMENT '封面图片URL',
    created_by        INT            COMMENT '创建人ID',
    create_time       DATETIME,
    update_time       DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 遗迹表（relics）
CREATE TABLE IF NOT EXISTS relics (
    id                    INT            PRIMARY KEY AUTO_INCREMENT,
    relic_code            VARCHAR(50)    COMMENT '遗迹编号',
    site_id               INT            COMMENT '所属遗址ID',
    site_name             VARCHAR(200)   COMMENT '所属遗址名称',
    name                  VARCHAR(200)   COMMENT '遗迹名称',
    type                  VARCHAR(100)   COMMENT '遗迹类型',
    position_within_site  VARCHAR(200)   COMMENT '在遗址中位置',
    excavation_area       DOUBLE         COMMENT '发掘面积',
    excavation_unit       VARCHAR(200)   COMMENT '发掘单位',
    era                   VARCHAR(100)   COMMENT '年代',
    stratigraphy          VARCHAR(500)   COMMENT '地层关系',
    structure_description TEXT           COMMENT '结构描述',
    dimensions            VARCHAR(200)   COMMENT '尺寸',
    orientation           VARCHAR(50)    COMMENT '朝向',
    burial_depth          DOUBLE         COMMENT '埋深',
    preservation_status   VARCHAR(50)    COMMENT '保存状况',
    function_purpose      VARCHAR(200)   COMMENT '功能用途',
    cultural_features     TEXT           COMMENT '文化特征',
    related_relics        TEXT           COMMENT '相关遗迹',
    images                TEXT           COMMENT '图片URL（JSON数组）',
    drawings              TEXT           COMMENT '图纸URL（JSON数组）',
    notes                 TEXT           COMMENT '备注',
    created_by            INT            COMMENT '创建人ID',
    create_time           DATETIME,
    update_time           DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 遗物表（artifacts）
CREATE TABLE IF NOT EXISTS artifacts (
    id                    INT            PRIMARY KEY AUTO_INCREMENT,
    artifact_code         VARCHAR(50)    COMMENT '遗物编号',
    site_id               INT            COMMENT '所属遗址ID',
    site_name             VARCHAR(200)   COMMENT '所属遗址名称',
    relic_id              INT            COMMENT '所属遗迹ID',
    relic_name            VARCHAR(200)   COMMENT '所属遗迹名称',
    name                  VARCHAR(200)   COMMENT '遗物名称',
    category              VARCHAR(100)   COMMENT '类别',
    sub_category          VARCHAR(100)   COMMENT '子类别',
    material              VARCHAR(100)   COMMENT '材质',
    era                   VARCHAR(100)   COMMENT '年代',
    size                  VARCHAR(100)   COMMENT '尺寸',
    weight                DOUBLE         COMMENT '重量（克）',
    color                 VARCHAR(50)    COMMENT '颜色',
    texture               VARCHAR(100)   COMMENT '质地',
    decoration            VARCHAR(200)   COMMENT '纹饰',
    inscription           VARCHAR(500)   COMMENT '铭文/文字',
    production_technique  VARCHAR(200)   COMMENT '制作工艺',
    usage_function        VARCHAR(200)   COMMENT '用途功能',
    discovery_context     TEXT           COMMENT '出土背景',
    preservation_condition VARCHAR(100)  COMMENT '保存状况',
    restoration_info      TEXT           COMMENT '修复信息',
    current_location      VARCHAR(200)   COMMENT '当前存放地点',
    museum_number         VARCHAR(100)   COMMENT '博物馆编号',
    images                TEXT           COMMENT '图片URL（JSON数组）',
    model3d_url           VARCHAR(500)   COMMENT '3D模型URL',
    research_notes        TEXT           COMMENT '研究备注',
    bibliography          TEXT           COMMENT '参考文献',
    cultural_value        TEXT           COMMENT '文化价值',
    created_by            INT            COMMENT '创建人ID',
    verification_status   VARCHAR(50)    DEFAULT '待审核' COMMENT '审核状态',
    verification_notes    TEXT           COMMENT '审核备注',
    create_time           DATETIME,
    update_time           DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;