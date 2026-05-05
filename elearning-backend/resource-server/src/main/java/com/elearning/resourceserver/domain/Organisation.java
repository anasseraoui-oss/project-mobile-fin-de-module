package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.OrganisationStatus;
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
@Table(name = "organisations")
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String logoKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String sector;
    private String website;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganisationStatus status = OrganisationStatus.PENDING;

    private LocalDateTime validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
