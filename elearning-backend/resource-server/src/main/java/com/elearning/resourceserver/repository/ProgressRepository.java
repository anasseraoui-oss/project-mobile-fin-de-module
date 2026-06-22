package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    Optional<Progress> findByUserIdAndSeanceId(UUID userId, UUID seanceId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.user.id = :userId AND p.seance.course.id = :courseId AND p.isCompleted = true")
    long countCompletedByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.isCompleted = false")
    List<Progress> findPendingSyncByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(p.watchedSeconds), 0) FROM Progress p WHERE p.user.id = :userId")
    Long sumWatchedSecondsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.user.id = :userId AND p.isCompleted = true")
    long countCompletedSeancesByUserId(@Param("userId") UUID userId);
}
