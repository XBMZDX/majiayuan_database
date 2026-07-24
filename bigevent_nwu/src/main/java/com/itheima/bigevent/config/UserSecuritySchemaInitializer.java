package com.itheima.bigevent.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Creates the account-security tables for databases that were initialized before this feature existed. */
@Component
public class UserSecuritySchemaInitializer {
    @Autowired
    private JdbcTemplate jdbc;

    @PostConstruct
    public void initialize() {
        addColumnIfMissing("user", "last_login_time", "DATETIME NULL");
        jdbc.execute("CREATE TABLE IF NOT EXISTS user_security_log ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id INT NOT NULL,"
                + "event_type VARCHAR(50) NOT NULL,"
                + "event_detail VARCHAR(255),"
                + "ip_address VARCHAR(64),"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_user_security_log_user_time (user_id, create_time)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        jdbc.execute("CREATE TABLE IF NOT EXISTS user_session ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "session_id VARCHAR(64) NOT NULL UNIQUE,"
                + "user_id INT NOT NULL,"
                + "login_ip VARCHAR(64),"
                + "user_agent VARCHAR(500),"
                + "status VARCHAR(20) NOT NULL DEFAULT 'active',"
                + "login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "last_active_time DATETIME NULL,"
                + "logout_time DATETIME NULL,"
                + "INDEX idx_user_session_user_status (user_id, status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? AND column_name=?",
                Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            jdbc.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }
}
