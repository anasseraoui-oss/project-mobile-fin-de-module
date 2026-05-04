// src/main/java/com/elearning/resourceserver/domain/Progress.java
package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "seance_id"})
})
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id")
    private Seance seance;

    private Integer watchedSeconds = 0;
    private Boolean isCompleted = false;
    private LocalDateTime lastWatchedAt;
}
