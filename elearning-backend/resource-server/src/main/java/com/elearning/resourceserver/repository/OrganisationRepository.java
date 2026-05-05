package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Organisation;
import com.elearning.resourceserver.domain.enums.OrganisationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findBySlug(String slug);

    Optional<Organisation> findByIsDefaultTrue();

    List<Organisation> findByStatus(OrganisationStatus status);

    boolean existsBySlug(String slug);
}
