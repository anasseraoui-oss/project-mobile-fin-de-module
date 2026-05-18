package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequestDto {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    
    @NotNull(message = "L'ordre d'affichage est obligatoire")
    private Integer orderIndex;

    private Integer estimatedDuration;
    
    // Ajoutés:
    private Integer presenceThreshold;
    private Integer quizPassScore;
    
    @NotNull(message = "L'ID de la formation est obligatoire")
    private java.util.UUID formationId;
}
