package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeanceRequestDto {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    @NotBlank(message = "Le type de séance est obligatoire (LIVE/RECORDED)")
    private String type;
    
    private LocalDateTime scheduledAt;
    private String meetLink;
    
    @NotNull(message = "L'ordre est obligatoire")
    private Integer orderIndex;
}
