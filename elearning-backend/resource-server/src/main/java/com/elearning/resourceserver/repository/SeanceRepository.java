package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, UUID> {

    List<Seance> findByCoursIdOrderByOrderIndex(UUID coursId);

    @Query("SELECT s FROM Seance s WHERE s.course.id = :coursId")
    List<Seance> findByCoursId(@Param("coursId") UUID coursId);

    long countByCoursId(UUID coursId);

    @Query("SELECT s FROM Seance s WHERE s.type = 'LIVE' AND s.scheduledAt BETWEEN :fromDate AND :toDate")
    List<Seance> findUpcomingLiveSessions(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
