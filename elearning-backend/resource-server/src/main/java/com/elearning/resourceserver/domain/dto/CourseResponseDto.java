package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CourseResponseDto {
    private UUID id;
    private String title;
    private String description;
    private UUID formationId;
    private Integer orderIndex;
    private Boolean isPublished;
    
    private Integer seancesCount;
    private Boolean quizExists;
}
