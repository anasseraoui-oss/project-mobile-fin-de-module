package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Formation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormationRepository extends JpaRepository<Formation, UUID> {

    @Query("SELECT f FROM Formation f WHERE f.isPublished = true " +
           "AND (:level IS NULL OR f.level = :level) " +
           "AND (:language IS NULL OR f.language = :language) " +
           "AND (:search IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Formation> findByFilters(@Param("level") String level, 
                                  @Param("language") String language, 
                                  @Param("search") String search, 
                                  Pageable pageable);

    @Query("SELECT f FROM Formation f WHERE f.isPublished = true")
    List<Formation> findAllPublished();

    List<Formation> findByOrganisationId(UUID organisationId);

    @Query("SELECT e.formation FROM Enrollment e WHERE e.user.id = :userId")
    List<Formation> findEnrolledFormationsByUserId(@Param("userId") UUID userId);
}
