package com.elearning.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/**",
                    "/oauth2/token",
                    "/oauth2/revoke",
                    "/oauth2/introspect"
                )
            )
            // STATELESS uniquement pour les endpoints /api/**
            // NOTE: formLogin ci-dessous requiert une session pour le flow PKCE browser
            // Le filter chain @Order(1) (AuthorizationServer) gère /oauth2/token directement
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/register", "/api/auth/**").permitAll()
                .requestMatchers("/login", "/error", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            // Retourner 401 JSON pour les clients API (Android) au lieu de rediriger
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String accept = request.getHeader("Accept");
                    String contentType = request.getHeader("Content-Type");
                    boolean isBrowserRequest = accept != null && accept.contains("text/html")
                            && (contentType == null || !contentType.contains("application/x-www-form-urlencoded"));
                    if (isBrowserRequest) {
                        response.sendRedirect("/login");
                    } else {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(401);
                        response.getWriter().write(
                            "{\"error\":\"unauthorized\",\"message\":\"Authentication required\"}"
                        );
                    }
                })
            )
            // Garder formLogin pour le flow PKCE browser (Authorization Code)
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
