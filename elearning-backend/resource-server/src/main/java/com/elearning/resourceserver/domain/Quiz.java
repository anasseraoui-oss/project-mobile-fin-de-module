package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", unique = true)
    private Course course;

    @Column(nullable = false)
    private Integer passScore;

    private Integer maxAttempts;
    private Integer timerSeconds;
}
