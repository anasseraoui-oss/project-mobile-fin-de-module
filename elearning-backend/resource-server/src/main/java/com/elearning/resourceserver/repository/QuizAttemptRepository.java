// src/main/java/com/elearning/resourceserver/repository/QuizAttemptRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    int countByUserIdAndQuizId(UUID userId, UUID quizId);
    List<QuizAttempt> findByUserIdAndQuizIdOrderByAttemptedAtDesc(UUID userId, UUID quizId);
}
