package com.elearning.authserver.config;

import com.elearning.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class JwtCustomizerConfig {

    private final UserRepository userRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                var roles = context.getPrincipal().getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                context.getClaims().claim("roles", roles);
                userRepository.findByEmail(context.getPrincipal().getName()).ifPresent(user -> {
                    context.getClaims().claim("id", user.getId().toString());
                    context.getClaims().claim("email", user.getEmail());
                    context.getClaims().claim("role", user.getRole().name());
                    if (user.getOrganisationId() != null) {
                        context.getClaims().claim("organisationId", user.getOrganisationId().toString());
                    }
                });

                if (context.getPrincipal().getPrincipal() instanceof OAuth2User oauth2User) {
                    Object email = oauth2User.getAttributes().get("email");
                    Object name = oauth2User.getAttributes().get("name");
                    Object picture = oauth2User.getAttributes().get("picture");

                    if (email != null) context.getClaims().claim("email", email);
                    if (name != null) context.getClaims().claim("name", name);
                    if (picture != null) context.getClaims().claim("picture", picture);
                }
            }

            if (OidcIdToken.class.isAssignableFrom(context.getTokenType().getClass())
                    || "id_token".equals(context.getTokenType().getValue())) {
                // OIDC id_token mappings can be added here.
            }
        };
    }
}
