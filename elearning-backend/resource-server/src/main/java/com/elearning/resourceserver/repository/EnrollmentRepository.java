// src/main/java/com/elearning/resourceserver/repository/EnrollmentRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByUserIdAndFormationId(UUID userId, UUID formationId);
    Optional<Enrollment> findByUserIdAndFormationId(UUID userId, UUID formationId);
    List<Enrollment> findByUserId(UUID userId);
}
