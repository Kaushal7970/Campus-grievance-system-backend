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
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/ai/**")).permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/faculty/**").hasAnyRole("FACULTY", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/users").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Faculty dropdown for assignment (should not require full user management)
                .requestMatchers(HttpMethod.GET, "/api/users/faculty").hasAnyRole("HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")

                // User management must be admin-only
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Activity logs must be admin-only
                .requestMatchers("/api/audit-logs").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/audit-logs/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Notifications are for authenticated users
                .requestMatchers("/api/notifications").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()

                // Announcements
                .requestMatchers(HttpMethod.POST, "/api/announcements/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")
                .requestMatchers(HttpMethod.DELETE, "/api/announcements/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")

                // Grievance RBAC
                .requestMatchers(HttpMethod.GET, "/api/grievance/all").hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")
                .requestMatchers(HttpMethod.GET, "/api/grievance/faculty/**").hasAnyRole("FACULTY", "HOD", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/grievance/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/grievance/create").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/grievance/assign/**").hasAnyRole("HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/grievance/update/**").hasAnyRole("FACULTY", "HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/grievance/update-with-remarks/**").hasAnyRole("FACULTY", "HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/grievance/*/escalate").hasAnyRole("HOD", "PRINCIPAL", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/grievance/*/comments").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/grievance/*/comments").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/grievance/*/chat").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/grievance/*/chat").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/grievance/*/attachments").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/grievance/*/attachments").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/grievance/attachments/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}