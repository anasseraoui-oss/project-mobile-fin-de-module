package com.elearning.authserver.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

@Configuration
public class RegisteredClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient androidElearningApp = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("elearning-mobile-client")
                // Pour le password grant, le client doit s'authentifier via client_secret dans le body
                .clientSecret(passwordEncoder.encode("elearning-mobile-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .redirectUri("com.elearning.app://oauth2redirect")
                .redirectUri("com.elearning://callback")
                .postLogoutRedirectUri("com.elearning.app://logout")
                .postLogoutRedirectUri("com.elearning://logout")

                // OIDC & Custom Scopes
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("offline_access")
                .scope("courses:read")
                .scope("courses:write")
                .scope("quiz:submit")
                .scope("admin")

                // Client Settings (exiger PKCE est implicite pour METHOD.NONE, mais recommandé en paramètre global ou true ici)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        // .requireProofKey(true) // Souvent implicite pour public clients, explicite si souhaité
                        .build())

                // Token Settings
                .tokenSettings(TokenSettings.builder()
                        // Format RS256 JWT pour Access Token, durée 15s -> minutes
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        // Refresh Token généré opaque
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(false) // Rotation Opaque via Redis
                        .build())
                .build();

        // En production, utiliser JdbcRegisteredClientRepository (ou personnalisé pour interagir avec une DB/Redis)
        return new InMemoryRegisteredClientRepository(androidElearningApp);
    }
}
