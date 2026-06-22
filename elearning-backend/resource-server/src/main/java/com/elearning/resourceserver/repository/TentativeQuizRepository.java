package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.TentativeQuiz;
import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TentativeQuizRepository extends JpaRepository<TentativeQuiz, UUID> {

    int countByApprenantIdAndQuizId(UUID apprenantId, UUID quizId);

    List<TentativeQuiz> findByApprenantIdAndQuizIdOrderByAttemptNumberDesc(UUID apprenantId, UUID quizId);

    @Query("SELECT t FROM TentativeQuiz t " +
           "JOIN FETCH t.quiz q " +
           "JOIN FETCH q.course c " +
           "JOIN FETCH c.formation f " +
           "WHERE t.apprenantId = :apprenantId " +
           "AND t.status <> 'EN_COURS' " +
           "ORDER BY t.submittedAt DESC NULLS LAST, t.startedAt DESC")
    List<TentativeQuiz> findCompletedHistoryByApprenantId(@Param("apprenantId") UUID apprenantId);

    Optional<TentativeQuiz> findFirstByApprenantIdAndQuizIdOrderByAttemptNumberDesc(UUID apprenantId, UUID quizId);

    Optional<TentativeQuiz> findFirstByApprenantIdAndQuizIdAndStatus(
            UUID apprenantId, UUID quizId, TentativeQuizStatus status);

    @Query("SELECT AVG(t.score) FROM TentativeQuiz t " +
           "WHERE t.apprenantId = :apprenantId " +
           "AND t.status = 'VALIDEE' " +
           "AND t.quiz.course.formation.id = :formationId")
    Double findAverageScoreByApprenantAndFormation(
            @Param("apprenantId") UUID apprenantId,
            @Param("formationId") UUID formationId);

    void deleteByApprenantIdAndQuizId(UUID apprenantId, UUID quizId);
}
