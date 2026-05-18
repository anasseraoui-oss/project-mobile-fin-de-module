package com.elearning.resourceserver.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", unique = true)
    private Course course;

    @Column(name = "course_id", insertable = false, updatable = false)
    private UUID coursId;

    @Column(nullable = false)
    private String title;

    @Column(name = "timer_seconds")
    private Integer timeLimit;

    @Column(nullable = false)
    private Integer maxAttempts = 3;

    @Column(nullable = false)
    private Integer passScore = 70;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<QuizQuestion> questions;
}
