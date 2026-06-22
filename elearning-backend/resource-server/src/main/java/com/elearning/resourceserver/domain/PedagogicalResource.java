package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.ResourceType;
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
@Table(name = "pedagogical_resources")
public class PedagogicalResource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "formation_id", nullable = false)
    private UUID formationId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "seance_id")
    private UUID seanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Column(nullable = false)
    private String title;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName = "elearning-media";

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "checksum_sha256")
    private String checksumSha256;

    @Column(name = "is_downloadable", nullable = false)
    private Boolean isDownloadable = true;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
