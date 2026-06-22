package com.elearning.authserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.util.StringUtils;

@Configuration
public class AuthorizationServerSettingsConfig {

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${auth-server.issuer:${AUTH_SERVER_ISSUER:}}") String issuer) {
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (StringUtils.hasText(issuer)) {
            builder.issuer(issuer);
        }
        return builder.build();
    }
}
