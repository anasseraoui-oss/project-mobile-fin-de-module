package com.elearning.resourceserver.domain;

import com.elearning.resourceserver.domain.enums.AuthProvider;
import com.elearning.resourceserver.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;
    private String avatarKey;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Boolean isActive = true;
}
