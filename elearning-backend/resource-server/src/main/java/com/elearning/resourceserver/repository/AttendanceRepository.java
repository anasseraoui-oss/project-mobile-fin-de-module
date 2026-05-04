// src/main/java/com/elearning/resourceserver/repository/AttendanceRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    boolean existsByUserIdAndSeanceId(UUID userId, UUID seanceId);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.user WHERE a.seance.id = :seanceId")
    List<Attendance> findBySeanceIdWithUser(@Param("seanceId") UUID seanceId);
}
