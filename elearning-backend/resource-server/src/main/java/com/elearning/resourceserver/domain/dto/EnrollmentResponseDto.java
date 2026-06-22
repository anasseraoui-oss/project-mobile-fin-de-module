package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EnrollmentResponseDto {
    private UUID inscriptionId;
    private UUID formationId;
    private String status;
    private LocalDateTime enrolledAt;
    private boolean paymentRequired;
}
