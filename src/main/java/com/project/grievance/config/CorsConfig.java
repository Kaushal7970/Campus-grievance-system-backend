package com.project.grievance.config;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private final Environment environment;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                String allowedOriginsProperty = environment.getProperty(
                        "app.cors.allowed-origins",
                    "https://*.onrender.com,https://campus-grievance-system-frontend.onrender.com,https://grievance-frontend.onrender.com,http://localhost:5173,http://localhost:3000,http://localhost:3001,http://localhost:3002"
                );
            String[] allowedOrigins = Objects.requireNonNull(
                Arrays.stream(allowedOriginsProperty.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toArray(String[]::new)
            );
                registry.addMapping("/**")
                        // Accept both exact origins and patterns (e.g. https://*.onrender.com)
                        .allowedOriginPatterns(allowedOrigins)
                        .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}