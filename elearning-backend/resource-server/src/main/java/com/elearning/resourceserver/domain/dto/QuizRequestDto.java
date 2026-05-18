package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class QuizRequestDto {
    @NotNull(message = "L'ID du cours est obligatoire")
    private UUID courseId;
    @NotNull(message = "Le titre du quiz est obligatoire")
    private String title;

    @NotNull(message = "Le score de passage est obligatoire")
    @Positive(message = "Le score doit être positif")
    private Integer passScore;
    
    @NotNull(message = "Le nombre maximum de tentatives est obligatoire")
    @Positive(message = "Le nombre de tentatives doit être positif")
    private Integer maxAttempts;
    
    @NotNull(message = "La durée (en secondes) est obligatoire")
    @Positive(message = "La durée doit être positive")
    private Integer timeLimit;
    
    @NotEmpty(message = "Un quiz doit contenir au moins une question")
    private List<QuestionDto> questions;

    @Data
    public static class QuestionDto {
        @NotNull(message = "Le texte de la question est obligatoire")
        private String question;
        @NotNull(message = "Le type de question est obligatoire")
        private String type;
        private String options; // JSON String
        @NotNull(message = "La réponse correcte est obligatoire")
        private String correctAnswer; // JSON String
        private Integer points = 1;
        private Integer orderIndex;
    }
}
