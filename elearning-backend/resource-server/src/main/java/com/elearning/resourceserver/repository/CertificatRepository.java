package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Certificat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificatRepository extends JpaRepository<Certificat, UUID> {

    Optional<Certificat> findByApprenantIdAndFormationId(UUID apprenantId, UUID formationId);

    boolean existsByApprenantIdAndFormationId(UUID apprenantId, UUID formationId);

    List<Certificat> findByApprenantId(UUID apprenantId);

    Optional<Certificat> findByVerificationCode(UUID verificationCode);
}
