package com.elearning.authserver.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}