package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InstructorFormationSummaryDto {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String coverImageUrl;
    private Integer coursesCount;
    private Integer seancesCount;
    private Integer enrolledCount;
    private Integer totalDuration;
    private LocalDateTime updatedAt;
}
