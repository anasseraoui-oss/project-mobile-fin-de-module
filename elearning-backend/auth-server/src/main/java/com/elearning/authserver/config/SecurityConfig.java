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
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/error").permitAll()
                .anyRequest().authenticated()
            )
            // L'ajout de oauth2Login permet l'intégration Identity Broker (SSO Fédéré Google/Facebook)
            .oauth2Login(Customizer.withDefaults())
            // Fallback pour le login standard si l'utilisateur ne choisit pas le SSO fédéré
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
