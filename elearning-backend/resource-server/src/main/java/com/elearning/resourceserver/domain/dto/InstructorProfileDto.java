package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class InstructorProfileDto {
    private UUID id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String certificationStatus;
    private String levelLabel;
    private String organisationName;
}
