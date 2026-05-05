package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.enums.FormationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormationRepository extends JpaRepository<Formation, UUID> {

    /**
     * RB-10: Only show formations where status=PUBLIEE AND organisation.status=ACTIVE
     */
    @Query("SELECT f FROM Formation f JOIN Organisation o ON f.organisationId = o.id " +
           "WHERE f.status = 'PUBLIEE' AND o.status = 'ACTIVE' " +
           "AND (:level IS NULL OR f.level = :level) " +
           "AND (:language IS NULL OR f.language = :language) " +
           "AND (:search IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Formation> findByFilters(@Param("level") String level,
                                  @Param("language") String language,
                                  @Param("search") String search,
                                  Pageable pageable);

    List<Formation> findByOrganisationId(UUID organisationId);

    List<Formation> findByFormateurId(UUID formateurId);

    Optional<Formation> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
