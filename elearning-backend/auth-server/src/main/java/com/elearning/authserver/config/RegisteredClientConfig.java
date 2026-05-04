package com.elearning.authserver.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;

@Configuration
public class RegisteredClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient androidElearningApp = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("android-elearning-app")
                // Aucune authentification cliente (pas de client_secret) requis pour les clients publics
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("com.elearning://callback")
                .postLogoutRedirectUri("com.elearning://logout")
                
                // OIDC & Custom Scopes
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
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
