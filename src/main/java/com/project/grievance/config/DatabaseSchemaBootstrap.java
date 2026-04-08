package com.project.grievance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseSchemaBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaBootstrap.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaBootstrap(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String productName = detectDatabaseProductName();
        if (productName == null) {
            return;
        }

        if (productName.toLowerCase().contains("postgres")) {
            ensureUsersColumnsPostgres();
        }
    }

    private String detectDatabaseProductName() {
        try (Connection c = dataSource.getConnection()) {
            return c.getMetaData().getDatabaseProductName();
        } catch (Exception ex) {
            // If DB is not reachable, we'll fail later anyway; keep logs actionable.
            log.warn("Could not detect database product name: {}", ex.getMessage());
            return null;
        }
    }

    private void ensureUsersColumnsPostgres() {
        // Keep this idempotent and safe for repeated startups.
        // Column names follow Spring Boot default physical naming strategy (snake_case).
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS sms_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20)");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_storage_path VARCHAR(700)");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS theme_id VARCHAR(32)");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMPTZ");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(50)");
        } catch (Exception ex) {
            // Don't crash the app if this best-effort bootstrap fails; logs will guide fixes.
            log.warn("DB bootstrap for users table failed: {}", ex.getMessage());
        }
    }
}
