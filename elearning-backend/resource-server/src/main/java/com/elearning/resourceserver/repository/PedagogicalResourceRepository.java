package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.PedagogicalResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedagogicalResourceRepository extends JpaRepository<PedagogicalResource, UUID> {
    List<PedagogicalResource> findBySeanceIdOrderByCreatedAtAsc(UUID seanceId);
    List<PedagogicalResource> findByCourseIdOrderByCreatedAtAsc(UUID courseId);
    List<PedagogicalResource> findByFormationIdOrderByCreatedAtAsc(UUID formationId);
    boolean existsByObjectKey(String objectKey);
}
