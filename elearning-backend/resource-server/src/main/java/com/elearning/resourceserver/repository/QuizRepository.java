// src/main/java/com/elearning/resourceserver/repository/QuizRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findByCourseId(UUID courseId);
}
