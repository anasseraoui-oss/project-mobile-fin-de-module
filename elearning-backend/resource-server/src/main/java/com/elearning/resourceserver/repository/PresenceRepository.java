package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, UUID> {

    boolean existsByApprenantIdAndSeanceId(UUID apprenantId, UUID seanceId);

    @Query("SELECT p FROM Presence p WHERE p.seanceId = :seanceId")
    List<Presence> findBySeanceId(@Param("seanceId") UUID seanceId);

    @Query("SELECT COUNT(p) FROM Presence p " +
           "WHERE p.apprenantId = :apprenantId " +
           "AND p.seance.course.id = :coursId " +
           "AND (p.status = 'PRESENT' OR p.status = 'RETARD')")
    long countPresentByApprenantAndCours(@Param("apprenantId") UUID apprenantId,
                                         @Param("coursId") UUID coursId);
}
