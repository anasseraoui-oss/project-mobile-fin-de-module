package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.dto.AdminStatsDto;
import com.elearning.resourceserver.domain.dto.OrganisationStatsDto;
import com.elearning.resourceserver.domain.dto.TrainerStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StatsRepository extends JpaRepository<Formation, UUID> {

    @Query(value = """
        SELECT 
            COUNT(DISTINCT f.id) AS total_formations,
            COUNT(DISTINCT e.user_id) AS total_apprenants,
            COALESCE(AVG(
                (SELECT COUNT(p.id) * 100.0 / NULLIF(COUNT(s.id), 0)
                 FROM seances s 
                 LEFT JOIN progress p ON p.seance_id = s.id 
                 JOIN courses c ON s.course_id = c.id
                 WHERE c.formation_id = f.id AND p.is_completed = true)
            ), 0) AS avg_completion_rate,
            COALESCE(SUM(f.price), 0) AS revenus
        FROM formations f
        LEFT JOIN enrollments e ON e.formation_id = f.id
        WHERE f.organisation_id = :orgId
        """, nativeQuery = true)
    OrganisationStatsDto getOrganisationStats(@Param("orgId") UUID orgId);

    @Query(value = """
        SELECT 
            COUNT(DISTINCT c.id) AS total_courses,
            COUNT(DISTINCT e.user_id) AS total_apprenants,
            COALESCE(AVG(qa.score), 0) AS avg_quiz_score
        FROM courses c
        LEFT JOIN formations f ON c.formation_id = f.id
        LEFT JOIN enrollments e ON e.formation_id = f.id
        LEFT JOIN quizzes q ON q.course_id = c.id
        LEFT JOIN quiz_attempts qa ON qa.quiz_id = q.id
        -- WHERE c.trainer_id = :trainerId
        """, nativeQuery = true)
    TrainerStatsDto getTrainerStats(@Param("trainerId") UUID trainerId);
    
    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM organisations) AS total_organisations,
            (SELECT COUNT(*) FROM formations) AS total_formations,
            (SELECT COUNT(*) FROM users) AS total_users,
            (SELECT COUNT(*) FROM enrollments WHERE enrolled_at > NOW() - INTERVAL '30 days') AS enrollments_last_month
        """, nativeQuery = true)
    AdminStatsDto getAdminStats();
}
