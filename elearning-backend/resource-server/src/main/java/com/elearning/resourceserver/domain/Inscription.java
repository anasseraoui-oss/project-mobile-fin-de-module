package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.InscriptionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"apprenant_id", "formation_id"})
})
public class Inscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "formation_id", nullable = false)
    private UUID formationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", insertable = false, updatable = false)
    private Formation formation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InscriptionStatus status = InscriptionStatus.EN_COURS;

    @Column(updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private String paymentId;

    private LocalDateTime accessExpiresAt;
}
