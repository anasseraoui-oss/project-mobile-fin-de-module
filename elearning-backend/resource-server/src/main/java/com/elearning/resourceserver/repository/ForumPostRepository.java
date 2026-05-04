// src/main/java/com/elearning/resourceserver/repository/ForumPostRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {
    List<ForumPost> findBySeanceIdOrderByCreatedAt(UUID seanceId);
}
