package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FormationRequestDto {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Le niveau est obligatoire")
    private String level;
    
    @NotBlank(message = "La langue est obligatoire")
    private String language;
    
    @NotNull(message = "Le prix ne peut pas être nul")
    @PositiveOrZero(message = "Le prix doit être positif ou nul")
    private Double price;

    private String categoryId;

    private List<String> prerequisites;

    private Boolean certified;
    
    private List<UUID> prerequisiteIds;
}
