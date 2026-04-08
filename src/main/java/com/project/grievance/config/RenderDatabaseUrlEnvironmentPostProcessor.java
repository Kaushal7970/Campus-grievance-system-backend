package com.project.grievance.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Render commonly provides Postgres connection info as DATABASE_URL in the form:
 *   postgres://user:pass@host:port/dbname?sslmode=require
 * Spring expects a JDBC URL, so we convert DATABASE_URL into spring.datasource.*
 * if those are not already configured.
 */
public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (StringUtils.hasText(environment.getProperty("spring.datasource.url"))) {
            return;
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            return;
        }

        ParsedDbUrl parsed = parseDatabaseUrl(databaseUrl);
        if (parsed == null || !StringUtils.hasText(parsed.jdbcUrl)) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", parsed.jdbcUrl);

        if (!StringUtils.hasText(environment.getProperty("spring.datasource.username")) && StringUtils.hasText(parsed.username)) {
            props.put("spring.datasource.username", parsed.username);
        }
        if (!StringUtils.hasText(environment.getProperty("spring.datasource.password")) && StringUtils.hasText(parsed.password)) {
            props.put("spring.datasource.password", parsed.password);
        }

        environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", props));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static ParsedDbUrl parseDatabaseUrl(String databaseUrl) {
        try {
            URI uri = URI.create(databaseUrl);
            String scheme = uri.getScheme();
            if (!StringUtils.hasText(scheme)) {
                return null;
            }

            // Render Postgres typically uses postgres:// or postgresql://
            boolean postgres = scheme.equalsIgnoreCase("postgres") || scheme.equalsIgnoreCase("postgresql");
            if (!postgres) {
                return null;
            }

            String host = uri.getHost();
            int port = (uri.getPort() == -1) ? 5432 : uri.getPort();
            String path = uri.getPath();
            String dbName = (StringUtils.hasText(path) && path.startsWith("/")) ? path.substring(1) : path;

            if (!StringUtils.hasText(host) || !StringUtils.hasText(dbName)) {
                return null;
            }

            String username = null;
            String password = null;
            String userInfo = uri.getUserInfo();
            if (StringUtils.hasText(userInfo)) {
                String[] parts = userInfo.split(":", 2);
                username = urlDecode(parts[0]);
                if (parts.length > 1) {
                    password = urlDecode(parts[1]);
                }
            }

            String query = uri.getQuery();
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            if (StringUtils.hasText(query)) {
                jdbcUrl = jdbcUrl + "?" + query;
            }

            return new ParsedDbUrl(jdbcUrl, username, password);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String urlDecode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record ParsedDbUrl(String jdbcUrl, String username, String password) {
    }
}
