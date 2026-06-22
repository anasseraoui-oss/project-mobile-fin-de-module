package com.elearning.authserver.controller;

import com.elearning.authserver.dto.GoogleLoginRequest;
import com.elearning.authserver.dto.TokenResponse;
import com.elearning.authserver.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> loginWithGoogle(
            @RequestBody GoogleLoginRequest request) {
        TokenResponse tokens = googleAuthService
            .authenticateWithGoogle(request.getIdToken());
        return ResponseEntity.ok(tokens);
    }
}