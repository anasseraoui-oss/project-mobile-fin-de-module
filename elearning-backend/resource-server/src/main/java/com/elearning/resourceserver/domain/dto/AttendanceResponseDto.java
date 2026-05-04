package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceResponseDto {
    private UUID userId;
    private String fullName; // Potentiellement recomposé ou stocké dans un claim
    private String email;
    private LocalDateTime markedAt;
}
