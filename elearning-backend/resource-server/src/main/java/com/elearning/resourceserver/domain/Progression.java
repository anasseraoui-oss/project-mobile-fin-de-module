package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.QuizStatus;
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
@Table(name = "progressions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"apprenant_id", "cours_id"})
})
public class Progression {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "cours_id", nullable = false)
    private UUID coursId;

    @Column(name = "formation_id", nullable = false)
    private UUID formationId;

    @Column(nullable = false)
    private Double presenceRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus quizStatus = QuizStatus.NON_COMMENCE;

    private LocalDateTime completionDate;

    @Column(nullable = false)
    private Boolean isUnlocked = false;

    private LocalDateTime unlockedAt;
}
