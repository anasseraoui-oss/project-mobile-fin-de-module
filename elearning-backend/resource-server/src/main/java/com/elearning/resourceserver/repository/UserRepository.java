// src/main/java/com/elearning/resourceserver/repository/UserRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
