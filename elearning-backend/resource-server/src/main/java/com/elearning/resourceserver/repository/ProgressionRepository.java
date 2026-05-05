package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Progression;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressionRepository extends JpaRepository<Progression, UUID> {

    Optional<Progression> findByApprenantIdAndCoursId(UUID apprenantId, UUID coursId);

    List<Progression> findByApprenantIdAndFormationId(UUID apprenantId, UUID formationId);

    @Query("SELECT p FROM Progression p WHERE p.apprenantId = :apprenantId")
    List<Progression> findAllByApprenantId(@Param("apprenantId") UUID apprenantId);

    @Query("SELECT p FROM Progression p WHERE p.formationId = :formationId")
    List<Progression> findAllByFormationId(@Param("formationId") UUID formationId);

    @Query("SELECT COUNT(p) FROM Progression p " +
           "WHERE p.formationId = :formationId " +
           "AND p.apprenantId = :apprenantId " +
           "AND p.quizStatus = :quizStatus")
    long countByFormationIdAndApprenantIdAndQuizStatus(
            @Param("formationId") UUID formationId,
            @Param("apprenantId") UUID apprenantId,
            @Param("quizStatus") QuizStatus quizStatus);
}
