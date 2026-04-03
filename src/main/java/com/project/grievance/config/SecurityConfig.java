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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

import com.project.grievance.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    private static String json(String value) {
        if (value == null) return "null";
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
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
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");

                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String name = auth == null ? null : auth.getName();
                    String authorities = auth == null ? "" : auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    String body = String.format(
                            "{\"status\":401,\"path\":%s,\"name\":%s,\"authorities\":%s,\"message\":%s}",
                            json(request.getRequestURI()),
                            json(name),
                            json(authorities),
                            json(ex.getMessage())
                    );
                    response.getWriter().write(body);
                })
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");

                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String name = auth == null ? null : auth.getName();
                    String authorities = auth == null ? "" : auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    String body = String.format(
                            "{\"status\":403,\"path\":%s,\"name\":%s,\"authorities\":%s,\"message\":%s}",
                            json(request.getRequestURI()),
                            json(name),
                            json(authorities),
                            json(ex.getMessage())
                    );
                    response.getWriter().write(body);
                })
            )
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
                // Student and general grievance endpoints should work for any authenticated user.
                // Fine-grained access is enforced in controllers (e.g., only owner can view their own grievances).
                .requestMatchers(new AntPathRequestMatcher("/api/grievance/**")).authenticated()
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

                // Notifications are for any authenticated users
                .requestMatchers(new AntPathRequestMatcher("/api/notifications"))
                    .authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/notifications/**"))
                    .authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}