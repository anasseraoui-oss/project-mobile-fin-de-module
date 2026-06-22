package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CertificatResponseDto {
    private UUID id;
    private UUID formationId;
    private String formationTitle;
    private UUID apprenantId;
    private String learnerName;
    private LocalDateTime issuedAt;
    private BigDecimal averageScore;
    private Integer score;
    private Integer maxScore;
    private String pdfKey;
    private String downloadUrl;
    private UUID verificationCode;
}
