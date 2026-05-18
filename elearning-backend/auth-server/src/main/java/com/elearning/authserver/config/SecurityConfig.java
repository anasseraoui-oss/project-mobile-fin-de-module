package com.elearning.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/register", "/api/auth/**").permitAll()
                .requestMatchers("/login", "/error", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            // Fallback pour le login standard si l'utilisateur ne choisit pas le SSO fédéré
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
