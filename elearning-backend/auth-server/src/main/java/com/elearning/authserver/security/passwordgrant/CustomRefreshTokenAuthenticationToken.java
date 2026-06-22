package com.elearning.authserver.security.passwordgrant;

import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class CustomRefreshTokenAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String refreshToken;
    private final Set<String> scopes;

    public CustomRefreshTokenAuthenticationToken(String refreshToken, Set<String> scopes,
            Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.REFRESH_TOKEN, clientPrincipal, additionalParameters);
        this.refreshToken = refreshToken;
        this.scopes = scopes;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
