package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganisationId(UUID organisationId);

    List<User> findByOrganisationIdAndRole(UUID organisationId, Role role);
}
