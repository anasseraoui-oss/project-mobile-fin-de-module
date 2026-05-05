package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.PresenceStatus;
import com.elearning.resourceserver.domain.enums.ValidationMethod;
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
@Table(name = "presences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"apprenant_id", "seance_id"})
})
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "seance_id", nullable = false)
    private UUID seanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", insertable = false, updatable = false)
    private Seance seance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresenceStatus status;

    private LocalDateTime markedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationMethod validationMethod;

    private String ipAddress;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
