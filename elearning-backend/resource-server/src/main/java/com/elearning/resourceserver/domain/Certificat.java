package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"apprenant_id", "formation_id"})
})
public class Certificat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "formation_id", nullable = false)
    private UUID formationId;

    @Column(name = "organisation_id")
    private UUID organisationId;

    private LocalDateTime issuedAt = LocalDateTime.now();

    private String pdfKey;

    @Column(unique = true, nullable = false)
    private UUID verificationCode = UUID.randomUUID();

    @Column(precision = 5, scale = 2)
    private BigDecimal averageScore;

    private LocalDateTime expiresAt;
}
