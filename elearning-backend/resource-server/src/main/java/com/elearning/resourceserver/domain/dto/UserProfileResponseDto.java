package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileResponseDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String avatarKey;
    private String avatarUrl;
    private UUID organisationId;
    private String organisationName;
    private int enrolledFormations;
    private int completedFormations;
    private int completedCourses;
    private int certificatesCount;
    private int hoursSpent;
}
