package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.FormationLevel;
import com.elearning.resourceserver.domain.enums.FormationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "formations")
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImageKey;

    @Enumerated(EnumType.STRING)
    private FormationLevel level;

    private String language;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    private String currency = "MAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormationStatus status = FormationStatus.BROUILLON;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "formateur_id")
    private UUID formateurId;

    private Integer totalDuration;
    private Integer maxStudents;

    @Column(columnDefinition = "TEXT")
    private String prerequisitesText;

    @Column(name = "category_id")
    private String categoryId;

    @Column(nullable = false)
    private Boolean certified = false;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime publishedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPublished() {
        return this.status == FormationStatus.PUBLIEE;
    }
}
