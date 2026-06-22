package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    @Query("SELECT c FROM Course c WHERE c.formation.id = :formationId ORDER BY c.orderIndex ASC")
    List<Course> findByFormationIdOrderByOrderIndex(@Param("formationId") UUID formationId);

    long countByFormationId(UUID formationId);

    boolean existsByFormationIdAndTitle(UUID formationId, String title);
}
