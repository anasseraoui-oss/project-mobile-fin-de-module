package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.QuizReponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizReponseRepository extends JpaRepository<QuizReponse, UUID> {

    List<QuizReponse> findByQuestionId(UUID questionId);

    @Query("SELECT r FROM QuizReponse r WHERE r.questionId = :questionId AND r.isCorrect = true")
    List<QuizReponse> findCorrectByQuestionId(@Param("questionId") UUID questionId);
}
