package com.elearning.authserver.service;

import com.elearning.authserver.dto.TokenResponse;
import com.elearning.authserver.domain.User;
import com.elearning.authserver.domain.enums.AuthProvider;
import com.elearning.authserver.domain.enums.Role;
import com.elearning.authserver.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public TokenResponse authenticateWithGoogle(String idToken) {
        try {
            // 1. Vérifier le token Google
            GoogleIdTokenVerifier verifier = 
                new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken googleToken = verifier.verify(idToken);
            if (googleToken == null) {
                throw new RuntimeException("Token Google invalide"); // InvalidTokenException
            }

            GoogleIdToken.Payload payload = googleToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");
            String googleId = payload.getSubject();

            // 2. Trouver ou créer l'utilisateur
            User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setAvatarKey(pictureUrl);
                    newUser.setProvider(AuthProvider.GOOGLE);
                    newUser.setProviderId(googleId);
                    newUser.setRole(Role.APPRENANT);
                    newUser.setIsActive(true);
                    newUser.setIsEmailVerified(true);
                    return userRepository.save(newUser);
                });

            // 3. Générer JWT
            return jwtService.generateTokens(user);
        } catch (Exception e) {
            throw new RuntimeException("Google Authentication error: " + e.getMessage(), e);
        }
    }
}