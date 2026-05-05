package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.CoursStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoursStatus status = CoursStatus.A_VENIR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @Column(name = "formation_id", insertable = false, updatable = false)
    private UUID formationId;

    @Column(nullable = false)
    private Integer presenceThreshold = 80;

    @Column(nullable = false)
    private Integer quizPassScore = 70;

    private Integer estimatedDuration;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seance> seances;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
