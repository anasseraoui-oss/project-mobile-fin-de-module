// src/main/java/com/elearning/resourceserver/repository/OrganisationRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
}
