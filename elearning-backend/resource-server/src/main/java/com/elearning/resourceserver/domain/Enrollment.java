// src/main/java/com/elearning/resourceserver/domain/Enrollment.java
package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "formation_id"})
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @Column(updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    private LocalDateTime completedAt;
    private String certificateKey;
}
