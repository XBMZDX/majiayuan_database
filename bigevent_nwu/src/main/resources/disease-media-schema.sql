CREATE TABLE IF NOT EXISTS conservation_disease_media (
    id                BIGINT       PRIMARY KEY AUTO_INCREMENT,
    disease_record_id INT          NOT NULL,
    survey_id         INT          NOT NULL,
    project_id        INT          NOT NULL,
    file_name         VARCHAR(255) NOT NULL,
    content_type      VARCHAR(120) NOT NULL,
    file_size         BIGINT       NOT NULL,
    file_data         LONGBLOB     NOT NULL,
    title             VARCHAR(255),
    description       VARCHAR(1000),
    annotations_json  JSON,
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_disease_media_record (disease_record_id),
    INDEX idx_disease_media_survey (survey_id),
    INDEX idx_disease_media_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病害调查影像与标注表';
