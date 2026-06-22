package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Inscription;
import com.elearning.resourceserver.domain.enums.InscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {

    boolean existsByApprenantIdAndFormationId(UUID apprenantId, UUID formationId);

    Optional<Inscription> findByApprenantIdAndFormationId(UUID apprenantId, UUID formationId);

    List<Inscription> findByApprenantId(UUID apprenantId);

    List<Inscription> findByFormationIdAndStatus(UUID formationId, InscriptionStatus status);

    long countByFormationId(UUID formationId);

    long countByFormationIdAndStatus(UUID formationId, InscriptionStatus status);

    boolean existsByApprenantIdAndFormationIdAndStatus(UUID apprenantId, UUID formationId, InscriptionStatus status);
}
