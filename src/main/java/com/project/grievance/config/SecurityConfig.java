package com.project.grievance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.project.grievance.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/public/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/ai/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"), new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/faculty/**")).hasAnyRole("FACULTY", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/users")).hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Faculty dropdown for assignment (should not require full user management)
                .requestMatchers(new AntPathRequestMatcher("/api/users/faculty", "GET")).hasAnyRole("HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")

                // User management must be admin-only
                .requestMatchers(new AntPathRequestMatcher("/api/users/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Activity logs must be admin-only
                .requestMatchers(new AntPathRequestMatcher("/api/audit-logs")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/audit-logs/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Notifications are for authenticated users
                .requestMatchers(new AntPathRequestMatcher("/api/notifications")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/notifications/**")).authenticated()

                // Announcements
                .requestMatchers(new AntPathRequestMatcher("/api/announcements/**", "POST")).hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")
                .requestMatchers(new AntPathRequestMatcher("/api/announcements/**", "DELETE")).hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")

                // Grievance RBAC
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/all", "GET")).hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/faculty/**", "GET")).hasAnyRole("FACULTY", "HOD", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/student/**", "GET")).hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/create", "POST")).hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/assign/**", "PUT")).hasAnyRole("HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/update/**", "PUT")).hasAnyRole("FACULTY", "HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/update-with-remarks/**", "PUT")).hasAnyRole("FACULTY", "HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/escalate", "POST")).hasAnyRole("HOD", "PRINCIPAL", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/comments", "POST")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/comments", "GET")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/chat", "POST")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/chat", "GET")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/attachments", "POST")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/*/attachments", "GET")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/attachments/**", "GET")).authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}