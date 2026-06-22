package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.SeanceStatus;
import com.elearning.resourceserver.domain.enums.SeanceType;
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
@Table(name = "seances")
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeanceType type;

    private LocalDateTime scheduledAt;

    /** Duration in minutes */
    @Column(name = "duration_seconds")
    private Integer duration;

    @Column(name = "meet_link")
    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeanceStatus status = SeanceStatus.PLANIFIEE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "course_id", insertable = false, updatable = false, nullable = false)
    private UUID coursId;

    @Column(name = "formateur_id")
    private UUID formateurId;

    private String qrCodeToken;
    private LocalDateTime qrCodeExpiresAt;

    private String videoKey;

    private String pdfKey;

    private Integer orderIndex;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
