package com.elearning.authserver.config;

import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
public class JwtCustomizerConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                // Collecter les rôles et permissions
                var roles = context.getPrincipal().getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                
                context.getClaims().claim("roles", roles);

                // Mapping SSO Fédéré (Identity Broker) : Extraction des claims depuis Google/Facebook
                if (context.getPrincipal().getPrincipal() instanceof OAuth2User oauth2User) {
                    Object email = oauth2User.getAttributes().get("email");
                    Object name = oauth2User.getAttributes().get("name");
                    Object picture = oauth2User.getAttributes().get("picture");

                    if (email != null) context.getClaims().claim("email", email);
                    if (name != null) context.getClaims().claim("name", name);
                    if (picture != null) context.getClaims().claim("picture", picture);
                }
            }

            // OIDC id_token mappings
            if (OidcIdToken.class.isAssignableFrom(context.getTokenType().getClass()) || 
                "id_token".equals(context.getTokenType().getValue())) {
                // Ajout possible de claims spécifiques à OIDC si nécessaire
            }
        };
    }
}
