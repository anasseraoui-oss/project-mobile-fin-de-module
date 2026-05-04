package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "formations")
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String thumbnailKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    private String level; // ENUM: BEGINNER, INTERMEDIATE, ADVANCED
    private String language;
    private Double price;
    private Boolean isPublished = false;

    // Hibernate mapping for UUID array using custom type or simple string split
    @Column(columnDefinition = "uuid[]")
    private UUID[] prerequisiteIds;

    private String certificateTemplateKey;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
