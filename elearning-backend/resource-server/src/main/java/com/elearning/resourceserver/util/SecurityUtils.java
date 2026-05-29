package com.elearning.resourceserver.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    public static UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("Utilisateur non authentifié ou token invalide");
        }
        var jwt = (Jwt) auth.getPrincipal();
        String id = jwt.getClaimAsString("id");
        if (id != null && !id.isBlank()) {
            return UUID.fromString(id);
        }
        return UUID.fromString(jwt.getSubject());
    }

    public static String getCurrentUserRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "";
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
    }

    public static UUID getCurrentOrganisationId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt)) {
            return null;
        }
        var jwt = (Jwt) auth.getPrincipal();
        String orgId = jwt.getClaimAsString("organisationId");
        return orgId != null ? UUID.fromString(orgId) : null;
    }
}
