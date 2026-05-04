// src/main/java/com/elearning/resourceserver/domain/Seance.java
package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "seances")
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String type; // LIVE, RECORDED

    private String videoKey;
    private Integer durationSeconds;
    private String meetLink;
    private LocalDateTime scheduledAt;
    
    private Integer orderIndex;
    private Boolean isPublished = false;
}
